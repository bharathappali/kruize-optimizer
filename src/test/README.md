# Test Suite

This directory contains tests for the Kruize Optimizer application.

## Prerequisites

- Java 25 or higher

## Running Tests

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=DatasourceResourceTest
./mvnw test -Dtest=LayerResourceTest
./mvnw test -Dtest=MetadataProfileResourceTest
./mvnw test -Dtest=MetricProfileResourceTest
```

### Run Single Test Method
```bash
./mvnw test -Dtest=DatasourceResourceTest#testListDatasources_Success
```

## Test Approach

Tests mock the Kruize API client responses using JSON files. This allows testing without a running Kruize service. The application's business logic is tested, while external API calls are mocked.