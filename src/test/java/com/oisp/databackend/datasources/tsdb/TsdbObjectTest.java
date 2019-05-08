package com.oisp.databackend.datasources.tsdb;

import static org.mockito.Mockito.verify;

import com.oisp.databackend.datasources.tsdb.opentsdb.TsdbObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

public class TsdbObjectTest {

    private final String metric = "metric";
    private final long timestamp = 999;
    private final String value = "42.4";
    private final String testAttributeKey = "testAttributeKey";
    private final String testAttributeValue = "testAttributeValue";
    private final Map<String, String> attributes = new HashMap<String, String>();

    //@Mock
    //private TsdbValueString tsdbValue;


    @Before
    public void initMocks() throws Exception {
        MockitoAnnotations.initMocks(this);
        //PowerMockito.when(tsdbValue.get()).thenReturn(value);
        attributes.put(testAttributeKey, testAttributeValue);
    }

    @Test
    public void initObject() throws Exception{

        TsdbObject tsdbObject1 = new TsdbObject()
                .withMetric(metric)
                .withTimestamp(timestamp)
                .withValue(value);
        TsdbObject tsdbObject2 = new TsdbObject(metric, value, timestamp, attributes);
        TsdbObject tsdbObject3 = new TsdbObject(metric, value, timestamp);
        TsdbObject tsdbObject4 = new TsdbObject();
        TsdbObject tsdbObject5 = new TsdbObject(tsdbObject2);

        tsdbObject4.setMetric(metric);
        tsdbObject4.setValue(value);
        tsdbObject4.setTimestamp(timestamp);
        tsdbObject4.setAttribute(testAttributeKey, testAttributeValue);

        assert tsdbObject1.getMetric() == metric;
        assert tsdbObject1.getTimestamp() == timestamp;
        assert tsdbObject1.getValue() == value;
        assert tsdbObject1.getAttributes() != null;

        assert tsdbObject2.getMetric() == metric;
        assert tsdbObject2.getTimestamp() == timestamp;
        assert (String) (tsdbObject2.getAttributes().get(testAttributeKey)) == testAttributeValue;
        assert tsdbObject2.getValue() == value;

        assert tsdbObject3.getMetric() == metric;
        assert tsdbObject3.getTimestamp() == timestamp;
        assert tsdbObject3.getValue() == value;
        assert tsdbObject3.getAttributes() != null;

        assert tsdbObject4.getMetric() == metric;
        assert tsdbObject4.getTimestamp() == timestamp;
        assert (String) (tsdbObject4.getAttributes().get(testAttributeKey)) == testAttributeValue;
        assert tsdbObject4.getValue() == value;

        assert tsdbObject4.getMetric() == metric;
        assert tsdbObject4.getTimestamp() == timestamp;
        assert (String) (tsdbObject4.getAttributes().get(testAttributeKey)) == testAttributeValue;
        assert tsdbObject4.getValue() == value;

        tsdbObject3.setAllAttributes(attributes);
        assert tsdbObject3.getAttributes() == attributes;
    }

}