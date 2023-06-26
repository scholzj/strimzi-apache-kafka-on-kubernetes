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
   You can install it from the Operator Hub or using the YAML files:
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
   ```
   kubectl get pods
   ```
   Notice the different components being deployed.

   You can also check the status of the `Kafka` custom resource where the operator stores useful information such as the bootstrap addresses of the Kafka cluster:
   ```
   kubectl get kafka my-cluster -o yaml
   ```

## Using the Kafka cluster

5. Deploy a Kafka producer and consumer to send and receive some messages.
   You can do that using the [`02-clients.yaml` file](./02-clients.yaml).
   ```
   kubectl apply -f 02-clients.yaml
   ```
   Notice the different YAML documents in the file:
     * The two Kafka users - one for the producer and one for the consumer
     * The Kafka topic they are using to send/receive the messages
     * The actual Deployments with the producer and consumer applications and how they mount the secrets to connect to the broker

6. Once the clients are deployed, you should see two pods.
   You can check the logs to confirm they work:
   ```
   kubectl logs deployment/kafka-consumer -f
   ```
   You should see the _Hello World_ messages being received by the consumer

7. Create another user using the [`03-my-user.yaml` file](./03-my-user.yaml).
   You can create this user:
   ```
   kubectl apply -f 03-my-user.yaml
   ```

8. Once the user is created, you can:
     * Take the TLS certificate from the user secret
     * Take the external bootstrap address and the CA certificate from the status of the `Kafka` CR
   And use them to connect to the Kafka cluster from outside the Kubernetes cluster.
   You can use the Java application from [`04-external-client` directory](./04-external-client/) and run it against the cluster.

## Kafka Connect

9. Deploy Kafka Connect using the [`05-connect.yaml` file](./05-connect.yaml):
   ```
   kubetl apply -f 05-connect.yaml
   ```
   Check the YAML and notice how:
     * It creates the user and ACLs for Connect
     * Adds the Connector to the newly built container image

10. Create a connector instance using the [`06-connector.yaml` file](./06-connector.yaml):
    ```
    kubectl apply -f 06-connector.yaml
    ```
    
11. Once the connector is created, check the Connect logs to see how it logs the messages:
    ```
    kubectl logs deployment/my-connect-connect -f
    ```

## Cruise Control rebalancing

12. Use Cruise Control to rebalance the Kafka cluster.
    You can use the [`07-rebalance.yaml` file](./07-rebalance.yaml) for it.
    Trigger the rebalance using:
    ```
    kubectl apply -f 07-rebalance.yaml
    ```

13. Watch the rebalance progress:
    ```
    kubectl get kafkarebalance -w
    ```

## Clean-up

14. Delete all Strimzi resources:
    ```
    kubectl delete $(kubectl get strimzi -o name)
    ```

16. Delete the consumer and producer:
    ```
    kubectl delete -f 02-clients.yaml
    ```

15. Uninstall the Strimzi Operator.
    You can do that using the Operator Hub or using the YAML files - depending on how you installed it at the beginning.
    ```
    kubectl delete -f https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.35.1/strimzi-cluster-operator-0.35.1.yaml
    ```

