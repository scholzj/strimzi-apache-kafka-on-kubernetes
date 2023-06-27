package cz.scholz.kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SslConfigs;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageConsumerSSL {
    private static final int TIMEOUT_MS = 60000;
    private static final boolean DEBUG = true;

    public static void main(String[] args) {
        // Configure logging
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");

        Map<String, Object> props = new HashMap<>();

        // General configurations
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Configure the Secret config provider to get the credentials
        props.put(ConsumerConfig.CONFIG_PROVIDERS_CONFIG, "secrets");
        props.put(ConsumerConfig.CONFIG_PROVIDERS_CONFIG + ".secrets.class", "io.strimzi.kafka.KubernetesSecretConfigProvider");

        // Configure the boostrap server
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "<bootstrap-address>:<port>");

        // Configure TLS and authentication
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
        props.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, "${secrets:myproject/my-user:user.crt}");
        props.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, "${secrets:myproject/my-user:user.key}");
        props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
        props.put(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, "${secrets:myproject/my-cluster-cluster-ca-cert:ca.crt}");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS"); // Hostname verification

        // Create the consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("kafka-test-apps"));

        // Consume the messages in loop
        while (true)
        {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(TIMEOUT_MS));

            if(records.isEmpty()) {
                System.out.println("-I- No message in topic for " + TIMEOUT_MS /1000 + " seconds. Finishing ...");
                break;
            }

            for (ConsumerRecord<String, String> record : records)
            {
                if (DEBUG)
                {
                    System.out.println("-I- received message (offset " + record.offset() + "): " + record.key() + " / " + record.value() + " (from topic " + record.topic() + ", partition " + record.partition() + ", offset " + record.offset() + ")");
                }
            }

            consumer.commitSync();
        }

        // close the consumer
        System.out.println("-I- ####################################");
        consumer.close();
    }
}