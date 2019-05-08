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

package com.oisp.databackend.datasources.tsdb;

import com.oisp.databackend.datastructures.Observation;

import java.io.IOException;
import java.util.List;


/**
 * This is the abstraction API to attach different Time Series Databases backends to OISP
 * Currently work in progress, not stable API yet
 * @version 0.1
 */
public interface TsdbAccess {

    /**
     *
     * @param observation A single Observation to send the the data backend
     * @return true if successful, false otherwise
     */
    boolean put(Observation observation);

    /**
     *
     * @param observationList A list of Observations to send to the data backend
     * @return true if successful, false otherwise
     */
    boolean put(List<Observation> observationList);

    /**
     *
     * @param tsdbQuery Query for observations to be retrieved.
     * @return list of retrieved TSDB objects
     */
    Observation[] scan(TsdbQuery tsdbQuery);

    /**
     * Can be used to scan a fixed number of samples from a timestamp, e.g. to show the 1000 most recent values
     * Useful for user interfaces with live data updates
     * @param tsdbQuery query for Observations to be retrieved.
     * @param forward order of scanning, i.e. true means from old to new timestamps, false from new to old
     * @param limit max number of samples
     * @return list of retrieved TSDB objects
     */
    Observation[] scan(TsdbQuery tsdbQuery, boolean forward, int limit);

    /**
     * For advanced searches where all attributes are considered. Some backends need a list of all attributes
     * to retrieve them all.
     * @param tsdbQuery get list of all attribute names within a time interval
     * @return Array of found tags/attribute names
     * @throws IOException
     */
    String[] scanForAttributeNames(TsdbQuery tsdbQuery) throws IOException;

    /**
     * Get list of data types which are supported by tsdb backend
     */
    List<String> getSupportedDataTypes();
}
