package com.kruize.optimizer.service;

import com.kruize.optimizer.client.KruizeClient;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.context.ManagedExecutor;
import java.util.*;

@ApplicationScoped
public class WorkloadAutomatorService {

    private static final Logger LOG = Logger.getLogger(WorkloadAutomatorService.class);

    @Inject
    com.kruize.optimizer.config.TargetLabelConfig targetLabelConfig;

    @Inject
    KubernetesClient client;

    @Inject
    @RestClient
    KruizeClient kruizeClient;

    @Inject
    ManagedExecutor executor;

    @Inject
    ProfileInstallerService profileInstallerService;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "kruize.job.polling.interval")
    java.time.Duration pollingInterval;

    @Inject
    MeterRegistry registry;

    private Counter totalJobsCreated;
    private Counter totalExperimentsCreated;
    private Counter totalExperimentsProcessed;

    @PostConstruct
    void init() {
        totalJobsCreated = registry.counter("total_jobs_created");
        totalExperimentsCreated = registry.counter("total_experiments_created");
        totalExperimentsProcessed = registry.counter("total_experiments_processed");
    }

    @Scheduled(every = "${kruize.scan.interval:5m}")
    public void scanAndRegisterWorkloads() {
        LOG.info("Starting scheduled workload scan...");

        // Ensure profiles are installed
        try {
            profileInstallerService.checkAndInstallProfiles();
        } catch (Exception e) {
            LOG.error("Failed to check/install profiles: " + e.getMessage());
        }

        // Collect optimized workloads
        Set<String> namespaces = new HashSet<>();
        Set<String> workloadNames = new HashSet<>();

        try {
            // Iterate over each label configuration
            for (Map.Entry<String, String> entry : targetLabelConfig.getTargetLabels().entrySet()) {
                String labelKey = entry.getKey();
                String labelValue = entry.getValue();

                LOG.debugf("Scanning for label: %s=%s", labelKey, labelValue);

                // Deployments
                List<Deployment> deploymentList = client.apps().deployments().inAnyNamespace()
                        .withLabel(labelKey, labelValue).list().getItems();
                deploymentList.forEach(d -> {
                    namespaces.add(d.getMetadata().getNamespace());
                    workloadNames.add(d.getMetadata().getName());
                });

                // StatefulSets
                List<StatefulSet> statefulSetList = client.apps().statefulSets().inAnyNamespace()
                        .withLabel(labelKey, labelValue).list().getItems();
                statefulSetList.forEach(s -> {
                    namespaces.add(s.getMetadata().getNamespace());
                    workloadNames.add(s.getMetadata().getName());
                });

                // ReplicaSets
                List<ReplicaSet> replicaSetList = client.apps().replicaSets().inAnyNamespace()
                        .withLabel(labelKey, labelValue).list().getItems();
                replicaSetList.forEach(r -> {
                    namespaces.add(r.getMetadata().getNamespace());
                    workloadNames.add(r.getMetadata().getName());
                });
            }
        } catch (Exception e) {
            LOG.error("Error scanning Kubernetes resources: " + e.getMessage());
            return;
        }

        if (workloadNames.isEmpty()) {
            LOG.info("No optimized workloads found.");
            return;
        }

        LOG.infof("Found %d optimized workloads in %d namespaces. Initiating bulk creation...", workloadNames.size(),
                namespaces.size());

        try {
            // Construct Payload
            Map<String, Object> payload = new HashMap<>();

            Map<String, Object> filter = new HashMap<>();
            Map<String, Object> include = new HashMap<>();
            include.put("namespace", new ArrayList<>(namespaces));
            include.put("workload", new ArrayList<>(workloadNames));
            include.put("containers", List.of("")); // Empty string as per requirement
            filter.put("include", include);

            payload.put("filter", filter);
            payload.put("datasource", "prometheus-1"); // Hardcoded as per prompt
            payload.put("metadata_profile", "cluster-metadata-local-monitoring");
            payload.put("measurement_duration", "15min");

            String response = kruizeClient.bulkCreateExperiments(payload);
            LOG.infof("Bulk API Response: %s", response);

            totalJobsCreated.increment();
            // Count will be updated upon completion

            // Parse Job ID and Poll
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response);
                if (node.has("job_id")) {
                    String jobId = node.get("job_id").asText();
                    executor.submit(() -> pollJobStatus(jobId));
                }
            } catch (Exception e) {
                LOG.error("Failed to parse bulk response or start polling", e);
            }

        } catch (Exception e) {
            LOG.error("Failed to call Bulk API", e);
        }
    }

    private void pollJobStatus(String jobId) {
        LOG.infof("Starting polling for Job ID: %s", jobId);
        ObjectMapper mapper = new ObjectMapper();

        // Poll loop
        while (true) {
            try {
                Thread.sleep(pollingInterval.toMillis());

                String statusResponse = kruizeClient.getBulkJobStatus(jobId);
                JsonNode root = mapper.readTree(statusResponse);
                // Check if 'summary' node exists, otherwise use root (backward compatibility)
                JsonNode node = root.has("summary") ? root.get("summary") : root;

                String status = node.has("status") ? node.get("status").asText() : "UNKNOWN";
                LOG.infof("Job %s status: %s", jobId, status);

                if ("COMPLETED".equalsIgnoreCase(status)) {
                    int total = node.has("total_experiments") ? node.get("total_experiments").asInt() : 0;
                    int processed = node.has("processed_experiments") ? node.get("processed_experiments").asInt() : 0;

                    if (total > 0)
                        totalExperimentsCreated.increment(total);
                    if (processed > 0)
                        totalExperimentsProcessed.increment(processed);

                    LOG.infof("Job %s completed. Total: %d, Processed: %d", jobId, total, processed);
                    break;
                } else if ("FAILED".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status)) {
                    LOG.errorf("Job %s failed.", jobId);
                    break;
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOG.error("Error during job polling", e);
            }
        }
    }
}
