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

package com.oisp.databackend.datasources.tsdb.hbase;

import org.apache.hadoop.hbase.Cell;

import java.util.Arrays;

final class HbaseDataFormatter {


    private static final String KEY_DELIMITER = "\\.";

    private HbaseDataFormatter() {

    }

    public static String zeroPrefixedTimestamp(Long ts) {
        return String.format("%13d", ts);
    }

    public static Long getTimeFromKey(String key) {
        String[] parts = key.split(KEY_DELIMITER);
        return Long.parseLong(parts[2].trim());
    }

    public static String getPrefixFromKey(String key) {
        String[] parts = key.split(KEY_DELIMITER);
        return new String(parts[0] + "." + parts[1]);
    }

    public static String getAttrNameFromCell(Cell cell) {
        byte[] slice = Arrays.copyOfRange(cell.getRowArray(), cell.getQualifierOffset() + Columns.ATTRIBUTE_COLUMN_PREFIX.length(), cell.getQualifierOffset() + cell.getQualifierLength());
        return new String(slice);
    }

    public static long fixStopForExclusiveScan(long start, long stop) {
        if (stop > start && Long.MAX_VALUE - stop > 1.0) {
            return stop + 1;
        }
        return stop;
    }
}
