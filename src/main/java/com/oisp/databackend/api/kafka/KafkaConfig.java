/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oisp.databackend.api.kafka;

import com.oisp.databackend.config.oisp.OispConfig;
import com.oisp.databackend.datastructures.Observation;
import com.oisp.databackend.exceptions.ConfigEnvironmentException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Configuration
public class KafkaConfig {

    @Autowired
    private OispConfig oispConfig;

    private final Serializer<String> keySerializer = new StringSerializer();

    private final Serializer<List<Observation>> valueSerializer = new KafkaJSONSerializer();

    @Bean
    public KafkaProducer<String, List<Observation>> kafkaProducer() throws ConfigEnvironmentException {

        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, oispConfig.getBackendConfig().getKafkaConfig().getUri());
        return new KafkaProducer<>(producerConfig, keySerializer, valueSerializer);
    }

    @Bean
    public KafkaProducer<String, String> kafkaHearbeatProducer() throws ConfigEnvironmentException {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, oispConfig.getBackendConfig().getKafkaConfig().getUri());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<String, String>(props);
    }
}