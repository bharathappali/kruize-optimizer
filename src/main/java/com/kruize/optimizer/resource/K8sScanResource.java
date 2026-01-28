package com.kruize.optimizer.resource;

import com.kruize.optimizer.model.K8sScanResult;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/scan")
public class K8sScanResource {

        @Inject
        KubernetesClient client;

        @Inject
        com.kruize.optimizer.config.TargetLabelConfig targetLabelConfig;

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public K8sScanResult scan(@QueryParam("all") boolean all) {
                K8sScanResult result = new K8sScanResult();

                // 1. Scan Namespaces
                List<Namespace> namespaces = client.namespaces().list().getItems();

                List<K8sScanResult.NamespaceInfo> nsResult = namespaces.stream()
                                .map(ns -> {
                                        boolean isOptimized = checkLabel(ns.getMetadata().getLabels());
                                        return new K8sScanResult.NamespaceInfo(ns.getMetadata().getName(), isOptimized);
                                })
                                .filter(ns -> all || ns.isKruizeOptimized())
                                .collect(Collectors.toList());

                result.setNamespaces(nsResult);

                List<K8sScanResult.WorkloadInfo> allWorkloads = new ArrayList<>();

                // 2. Scan Deployments
                List<Deployment> deployments = client.apps().deployments().inAnyNamespace().list().getItems();
                for (Deployment d : deployments) {
                        boolean isOptimized = checkLabel(d.getMetadata().getLabels());
                        if (all || isOptimized) {
                                List<K8sScanResult.ContainerInfo> containers = d.getSpec().getTemplate().getSpec()
                                                .getContainers()
                                                .stream()
                                                .map(c -> new K8sScanResult.ContainerInfo(c.getName(), c.getImage()))
                                                .collect(Collectors.toList());

                                allWorkloads.add(new K8sScanResult.WorkloadInfo(
                                                d.getMetadata().getName(),
                                                d.getMetadata().getNamespace(),
                                                "Deployment",
                                                isOptimized,
                                                containers));
                        }
                }

                // 3. Scan StatefulSets
                List<StatefulSet> statefulSets = client.apps().statefulSets().inAnyNamespace().list().getItems();
                for (StatefulSet s : statefulSets) {
                        boolean isOptimized = checkLabel(s.getMetadata().getLabels());
                        if (all || isOptimized) {
                                List<K8sScanResult.ContainerInfo> containers = s.getSpec().getTemplate().getSpec()
                                                .getContainers()
                                                .stream()
                                                .map(c -> new K8sScanResult.ContainerInfo(c.getName(), c.getImage()))
                                                .collect(Collectors.toList());

                                allWorkloads.add(new K8sScanResult.WorkloadInfo(
                                                s.getMetadata().getName(),
                                                s.getMetadata().getNamespace(),
                                                "StatefulSet",
                                                isOptimized,
                                                containers));
                        }
                }

                // 4. Scan ReplicaSets
                List<ReplicaSet> replicaSets = client.apps().replicaSets().inAnyNamespace().list().getItems();
                for (ReplicaSet r : replicaSets) {
                        boolean isOptimized = checkLabel(r.getMetadata().getLabels());
                        if (all || isOptimized) {
                                List<K8sScanResult.ContainerInfo> containers = r.getSpec().getTemplate().getSpec()
                                                .getContainers()
                                                .stream()
                                                .map(c -> new K8sScanResult.ContainerInfo(c.getName(), c.getImage()))
                                                .collect(Collectors.toList());

                                allWorkloads.add(new K8sScanResult.WorkloadInfo(
                                                r.getMetadata().getName(),
                                                r.getMetadata().getNamespace(),
                                                "ReplicaSet",
                                                isOptimized,
                                                containers));
                        }
                }

                result.setWorkloads(allWorkloads);

                return result;
        }

        private boolean checkLabel(Map<String, String> resourceLabels) {
                if (resourceLabels == null || resourceLabels.isEmpty()) {
                        return false;
                }
                // Check if any configured target label matches the resource labels
                for (Map.Entry<String, String> target : targetLabelConfig.getTargetLabels().entrySet()) {
                        if (target.getValue().equals(resourceLabels.get(target.getKey()))) {
                                return true;
                        }
                }
                return false;
        }
}
