# Strimzi: Apache Kafka on Kubernetes

This repository contains an introductory demo of Strimzi and Apache Kafka on Kubernetes.
I use it in introductory talks and workshops about Strimzi.

## Slides

The slides accompanying this demo can be found [here](https://docs.google.com/presentation/d/1pPyEThNxJKH63jjsTo0CfrVTzmih3MC7BzbZLI_1xtM/edit?usp=sharing).

## Video recording

[![Strimzi: Apache Kafka on Kubernetes (Introduction demo)](http://img.youtube.com/vi/MY_bFb--HEg/0.jpg)](http://www.youtube.com/watch?v=MY_bFb--HEg "Strimzi: Apache Kafka on Kubernetes (Introduction demo)")

## Running the demo

This demo was last used with Strimzi 0.45.0 on Kubernetes 1.32.
It might not work with other OpenShift versions, other Kubernetes distributions or a different Strimzi version.
It should be executed from a namespace named `myproject`.
It expects the namespace to be set as the default namespace.
When used with other namespaces, you might need to change the YAML files and or commands accordingly.
The commands used in the demo expect that you checked out this repository and are inside it in your terminal.

You can also check out the various tags in this repository for different variants of this demo (different Strimzi versions, Kubernetes distributions etc.)

## Prerequisites

1. Deploy Strimzi 0.45.0 in your cluster.
   You can install it from the Operator Hub or using the YAML files:
   ```
   kubectl apply -f https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.45.0/strimzi-cluster-operator-0.45.0.yaml
   ```

## Deploying Kafka cluster

_Shows how to deploy Apache Kafka cluster._

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

_Shows how to use the `KafkaTopic` and `KafkaUser` resources when deploying Kafka clients._

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

## Kafka Connect

_Demonstrates how to deploy Kafka Connect, add a connector plugin to it, and create a connector instance._

7. Deploy Kafka Connect using the [`03-connect.yaml` file](./03-connect.yaml):
   ```
   kubetl apply -f 03-connect.yaml
   ```
   Check the YAML and notice how:
     * It creates the user and ACLs for Connect
     * Adds the Connector to the newly built container image

8. Create a connector instance using the [`04-connector.yaml` file](./04-connector.yaml):
   ```
   kubectl apply -f 04-connector.yaml
   ```
    
9. Once the connector is created, check the Connect logs to see how it logs the messages:
   ```
   kubectl logs my-connect-connect-0 -f
   ```

## External access

_Shows how the operator helps with external access to the Kafka cluster using OpenShift Routes._

10. Create another user using the [`05-my-user.yaml` file](./05-my-user.yaml).
    You can create this user:
    ```
    kubectl apply -f 05-my-user.yaml
    ```

11. Once the user is created, you can:
      * Take the TLS certificate from the user secret
      * Take the external bootstrap address and the CA certificate from the status of the `Kafka` CR
    And use them to connect to the Kafka cluster from outside the Kubernetes cluster.
    You can use the Java application from [`06-external-client/` directory](./06-external-client/) and run it against the cluster.

## Broker reconfiguration

_Shows the power of the operator when changing the broker configuration._

12. Edit the Kafka cluster to change its configuration.
    You can edit the Kafka cluster with `kubectl`:
    ```
    kubectl edit kafka my-cluster
    ```
    And change the configuration in `.spec.kafka.config`.
    Set the option `compression.type` to `zstd`.
    Check how the operator changes the configuration dynamically.

13. Try another change that will require a rolling update and set the `delete.topic.enable` option to `false`.
    This change requires a rolling update which the operator will automatically do.
    Notice the order in which the brokers are rolled => the controller broker should be always rolled last and the operator makes sure the partition-replicas are in-sync.

## Cruise Control rebalancing

_Demonstrates how to rebalance a Kafka cluster using built-in Cruise Control support._

14. Use Cruise Control to rebalance the Kafka cluster.
    You can use the [`07-rebalance.yaml` file](./07-rebalance.yaml) for it.
    Trigger the rebalance using:
    ```
    kubectl apply -f 07-rebalance.yaml
    ```

15. Watch the rebalance progress:
    ```
    kubectl get kafkarebalance -w
    ```

## Rebalancing at scale-up

_Demonstrates how Strimzi automatically rebalances the cluster when the Kafka cluster is scaled-up or down._

16. Scale the Kafka cluster from 3 to 4 broker nodes:
    ```
    kubectl scale kafkanodepool brokers --replicas=4
    ```

17. Wait for the new broker to get ready, the auto-rebalance to be started, and watch the rebalance progress:
    ```
    kubectl get kafkarebalance -w
    ```

## Clean-up

18. Delete all Strimzi resources:
    ```
    kubectl delete $(kubectl get kt -o name) && kubectl delete $(kubectl get strimzi -o name)
    ```

19. Delete the consumer and producer:
    ```
    kubectl delete -f 02-clients.yaml
    ```

20. Uninstall the Strimzi Operator.
    You can do that using the Operator Hub or using the YAML files - depending on how you installed it at the beginning.
    ```
    kubectl delete -f https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.45.0/strimzi-cluster-operator-0.45.0.yaml
    ```
