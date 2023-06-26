# Strimzi: Apache Kafka on Kubernetes

This repository contains an introductory demo of Strimzi and Apache Kafka on Kubernetes.
I use it in introductory talks and workshops about Strimzi.

## Running the demo

This demo was last used with Strimzi 0.35.1 on OpenShift 4.13.
It might not work with other OpenShift versions, other Kubernetes distributions or a different Strimzi version.
It should be executed from a namespace named `myproject`.
It expects the namespace to be set as the default namespace.
When used with other namespaces, you might need to change the YAML files and or commands accordingly.
The commands used in the demo expect that you checked out this repository and are inside it in your terminal.

You can also check out the various tags in this repository for different variants of this demo (different Strimzi versions, Kubernetes distributions etc.)

## Prerequisites

1. Deploy Strimzi 0.35.1 in your cluster.
   You can install it from the OpenShift Operator Hub or using the YAML files:
   ```
   kubectl apply -f https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.35.1/strimzi-cluster-operator-0.35.1.yaml
   ```

## Deploying Kafka cluster

2. Check out the [`basic-kafka.yaml` file](./basic-kafka.yaml).
   It shows the _simplest possible Kafka installation_.
   We will not use it for this demo, but it demonstrates how much it takes to move from the most basic deployment to something much closer to production-ready Apache Kafka.

3. Now check the [`01-kafka.yaml` file](./01-kafka.yaml) which shows a Kafka cluster with a much more advanced configuration.
   It enables things such as authentication and authorization, metrics, external listener, configures resources etc.
   You can deploy this Kafka cluster using the following command:
   ```
   kubectl apply -f 01-kafka.yaml
   ```

4. Wait for the Kafka cluster to be deployed:
   ```
   kubectl wait kafka/my-cluster --for=condition=Ready --timeout=300s
   ```
   Once it is ready, you can check the running pods.
   You can also check the status of the `Kafka` custom resource where the operator stores useful information such as the bootstrap addresses of the Kafka cluster:
   ```
   kubectl get kafka my-cluster -o yaml
   ```

## Using the Kafka cluster


