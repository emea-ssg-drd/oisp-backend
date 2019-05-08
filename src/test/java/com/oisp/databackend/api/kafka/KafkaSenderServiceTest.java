/**
 * Copyright (c) 2015 Intel Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oisp.databackend.api.kafka;

import com.oisp.databackend.config.oisp.BackendConfig;
import com.oisp.databackend.config.oisp.OispConfig;
import com.oisp.databackend.datastructures.Observation;
import com.oisp.databackend.exceptions.ConfigEnvironmentException;
import kafka.admin.AdminUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.apache.kafka.clients.producer.KafkaProducer;
import kafka.utils.ZkUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest({KafkaSenderService.class, AdminUtils.class})
public class KafkaSenderServiceTest {

    public static final String TOPIC = "testTopic";

    @Mock
    ZkClient zkClient;

    @Mock
    ZkUtils zkUtils;

    @Mock
    private OispConfig serviceConfigProvider;

    @Mock
    private KafkaProducer<String, Observation> kafkaProducer;

    @Mock
    private BackendConfig backendConfig;

    @Mock
    private com.oisp.databackend.config.oisp.KafkaConfig kafkaOispConfig;

    @Mock
    private com.oisp.databackend.config.oisp.ZookeeperConfig zookeeperConfig;

    @InjectMocks
    private KafkaSenderService kafkaSenderService;

    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
	PowerMockito.whenNew(ZkClient.class).withAnyArguments().thenReturn(zkClient);
        PowerMockito.whenNew(ZkUtils.class).withAnyArguments().thenReturn(zkUtils);
        Mockito.doNothing().when(zkClient).close();

        PowerMockito.mockStatic(AdminUtils.class);
        Mockito.when(AdminUtils.topicExists(zkUtils, TOPIC)).thenReturn(true);
        PowerMockito.when(serviceConfigProvider.getBackendConfig()).thenReturn(backendConfig);
        PowerMockito.when(backendConfig.getKafkaConfig()).thenReturn(kafkaOispConfig);
        PowerMockito.when(backendConfig.getZookeeperConfig()).thenReturn(zookeeperConfig);
        PowerMockito.when(kafkaOispConfig.getUri()).thenReturn("kafka");
        Mockito.when(serviceConfigProvider.getBackendConfig().getKafkaConfig().getTopicsObservations()).thenReturn(TOPIC);
        Mockito.when(serviceConfigProvider.getBackendConfig().getKafkaConfig().getPartitions()).thenReturn(1);
        Mockito.when(serviceConfigProvider.getBackendConfig().getKafkaConfig().getReplication()).thenReturn(1);
        Mockito.when(serviceConfigProvider.getBackendConfig().getKafkaConfig().getTimeoutMs()).thenReturn(10);
        Mockito.when(serviceConfigProvider.getBackendConfig().getZookeeperConfig().getZkCluster()).thenReturn("localhost");
    }

    @After
    public void after() {
        PowerMockito.verifyStatic();
    }

    @Test
    public void testCreateTopic_topic_exist() throws ConfigEnvironmentException {
        kafkaSenderService.createTopic();
        kafkaSenderService.close();
        Mockito.verify(kafkaProducer).close();
        Mockito.verifyNoMoreInteractions(kafkaProducer);
        //assert true;
    }

    @Test
    public void testCreateTopic_topic_not_exist() throws ConfigEnvironmentException {
        Mockito.when(AdminUtils.topicExists(zkUtils, TOPIC)).thenReturn(false);
        kafkaSenderService.createTopic();
        kafkaSenderService.close();
        Mockito.verify(kafkaProducer).close();
        Mockito.verifyNoMoreInteractions(kafkaProducer);
        //assert true;
    }

    @Test
    public void testCreateTopic_error_handling() throws Exception {
        Mockito.when(AdminUtils.topicExists(zkUtils, TOPIC)).thenThrow(new ZkException());
        kafkaSenderService.createTopic();
        kafkaSenderService.close();
        Mockito.verifyNoMoreInteractions(kafkaProducer);
        //assert true;
    }

    @Test
    public void testSend() throws Exception {
        kafkaSenderService.createTopic();
        kafkaSenderService.send(Arrays.asList(new Observation()));
        Mockito.verify(kafkaProducer).send(Mockito.anyObject(), Mockito.anyObject());
        //assert true;
    }
}
