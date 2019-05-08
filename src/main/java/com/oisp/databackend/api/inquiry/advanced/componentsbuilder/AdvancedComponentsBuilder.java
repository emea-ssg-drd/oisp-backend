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

package com.oisp.databackend.api.inquiry.advanced.componentsbuilder;

import com.oisp.databackend.api.helpers.ObservationComparator;
import com.oisp.databackend.datastructures.AdvancedComponent;
import com.oisp.databackend.datastructures.ComponentDataType;
import com.oisp.databackend.datastructures.DeviceData;
import com.oisp.databackend.datastructures.Observation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AdvancedComponentsBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedComponentsBuilder.class);

    private final Map<String, ComponentDataType> componentsMetadata;
    private final Map<String, Observation[]> componentObservations;
    private ComponentsBuilderParams parameters;

    public AdvancedComponentsBuilder(Map<String, ComponentDataType> componentsMetadata,
                                     Map<String, Observation[]> componentObservations) {
        this.componentObservations = componentObservations;
        this.componentsMetadata = componentsMetadata;
    }

    public void appendComponentsDetails(List<DeviceData> deviceDataList, ComponentsBuilderParams parameters) {

        this.parameters = parameters;

        // When the returnedMeasureAttributes where "*", then the backend retrieved all tags without defining them
        // in advance. Therefore, the final list of attributes needs to be collected now
        if (parameters.getReturnedMeasureAttributes().size() == 1
                && "*".equals(parameters.getReturnedMeasureAttributes().get(0))) {
            Set<String> result = new HashSet<String>();
            for (Map.Entry<String, Observation[]> componentObservation : componentObservations.entrySet()) {
                Observation[] observations = componentObservation.getValue();
                result.addAll(Arrays.stream(observations)
                        .map(observation -> observation.getAttributes().keySet())
                        .reduce(new HashSet<String>(), (res, element) -> {
                                res.addAll(element);
                                return res;
                            })
                );
            }
            List<String> resultStringList = Arrays.asList(result.toArray(new String[result.size()]));
            parameters.setReturnedMeasureAttributes(resultStringList);
        }

        for (DeviceData device : deviceDataList) {
            logger.debug("Checking components for device {}", device.getDeviceId());
            Long first = countLowerLimit(parameters.getComponentRowStart());
            for (AdvancedComponent component : device.getComponents()) {

                Observation[] observations = componentObservations.get(component.getComponentId());
                if (observations == null) {
                    continue;
                }
                sortObservations(observations, componentsMetadata.get(component.getComponentId()));
                AdvancedComponentBuilder componentBuilder =
                        new AdvancedComponentBuilder(observations, componentsMetadata, parameters);

                componentBuilder.appendAggregations(component, first);
                componentBuilder.appendSamples(component, first);
            }
        }
    }

    private void sortObservations(Observation[] obs, final ComponentDataType componentDataType) {
        if (parameters.getSort() != null) {
            logger.debug("Sorting observations for component {}", componentDataType.getComponentId());
            Arrays.sort(obs, new ObservationComparator(parameters.getSort(), componentDataType));
        }
    }

    private Long countLowerLimit(Long componentRowStart) {
        Long first = componentRowStart;

        if (first == null || first < 0) {
            first = 0L;
        }
        return first;
    }
}

