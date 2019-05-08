package com.oisp.databackend.datasources.tsdb.opentsdb.opentsdbapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oisp.databackend.config.oisp.OispConfig;
import com.oisp.databackend.datasources.tsdb.opentsdb.TsdbObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RestApi {
    private static final Logger logger = LoggerFactory.getLogger(RestApi.class);
    public static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ATTRIBUTES = "\"attributes\":";
    private static final String TAGS = "\"tags\":";
    private static final String ACCEPT = "Accept";
    private static final String CONTENTTYPE = "Content-type";
    private static final int PUTOK = 204;
    private static final int QUERYOK = 200;
    private static final String CLOSING_BRACKET = "]";
    private static final String OPENING_BRACKET = "[";

    private URI putUri;
    private URI queryUri;
    public static final int MAXCHUNKSIZE = 4000;

    public RestApi(OispConfig oispConfig) throws URISyntaxException {
        String host;
        int port;
        String scheme;
        scheme = "http";
        host = oispConfig.getBackendConfig().getTsdbProperties().getProperty(OispConfig.OISP_BACKEND_TSDB_URI);
        port = Integer.parseInt(oispConfig.getBackendConfig().getTsdbProperties().getProperty(OispConfig.OISP_BACKEND_TSDB_PORT));

        putUri = new URIBuilder()
                .setScheme(scheme)
                .setPath("/api/put")
                .setHost(host)
                .setPort(port)
                .build();
        queryUri = new URIBuilder()
                .setScheme(scheme)
                .setPath("/api/query")
                .setHost(host)
                .setPort(port)
                .setParameter("ms", null)
                .build();

    }



    public boolean put(List<TsdbObject> tsdbObjects, boolean sync) {

        List<String> jsonObjects = tsdbObjectsToJSON(tsdbObjects);

        CloseableHttpClient client = HttpClients.createDefault();

        StringEntity entity = null;
        for (String jsonObject: jsonObjects) {
            String jsonObjectWithTags = jsonObject.replaceAll(Pattern.quote(ATTRIBUTES), TAGS);
            HttpPost httpPost = new HttpPost(putUri);

            try {
                entity = new StringEntity(jsonObjectWithTags);

                httpPost.setEntity(entity);
                httpPost.setHeader(ACCEPT, CONTENT_TYPE_JSON);
                httpPost.setHeader(CONTENTTYPE, CONTENT_TYPE_JSON);

                CloseableHttpResponse response = client.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                logger.debug("StatusCode of put response: " + statusCode);
                if (statusCode != PUTOK) {
                    logger.error("Status code: {}, Error reason {}", statusCode, EntityUtils.toString(
                            response.getEntity(), "UTF-8"));
                    return false;
                }
            } catch (IOException e) {
                logger.error("Could not create JSON payload for put POST request: " + e);
            }
        }
        return true;
    }

    List<String> tsdbObjectsToJSON(List<TsdbObject> tsdbObjects) {
        ObjectMapper mapper = new ObjectMapper();
        List<String> resultObjects = new ArrayList<String>();
        String remaining = tsdbObjects.stream()
                .map((obj) -> {
                        try {
                            return mapper.writeValueAsString(obj);
                        } catch (JsonProcessingException e) {
                            logger.error("Could not convert object to JSON " + e);
                            return "";
                        }
                    })
                .reduce("", (collect, elem) -> {
                        if (collect.isEmpty() || collect.length() + elem.length() > MAXCHUNKSIZE) {
                            if (!collect.isEmpty()) {
                                resultObjects.add(collect + CLOSING_BRACKET);
                            }
                            return OPENING_BRACKET + elem;
                        } else {
                            return collect + "," + elem;
                        }
                    });
        resultObjects.add(remaining + CLOSING_BRACKET);
        return resultObjects;
    }

    public QueryResponse[] query(Query query) {
        String jsonObject = query.toString();
        String jsonObjectWithTags = jsonObject.replaceAll(Pattern.quote(ATTRIBUTES), TAGS);
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(queryUri);
        StringEntity entity = null;
        String body = null;
        try {
            entity = new StringEntity(jsonObjectWithTags);

            httpPost.setEntity(entity);
            httpPost.setHeader(ACCEPT, CONTENT_TYPE_JSON);
            httpPost.setHeader(CONTENTTYPE, CONTENT_TYPE_JSON);
            CloseableHttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            logger.debug("StatusCode of query response: " + statusCode);
            if (statusCode != QUERYOK) {
                return null;
            }
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                body = EntityUtils.toString(responseEntity);
            }
        } catch (IOException e) {
            logger.error("Could not create JSON payload for query POST request: " + e);
            return null;
        }
        return queryResponsefromString(body);
    }

    public QueryResponse[] queryResponsefromString(String jsonString) {
        QueryResponse[] obj;
        try {
            ObjectMapper mapper = new ObjectMapper();
            obj = mapper.readValue(jsonString, QueryResponse[].class);
        } catch (IOException e) {
            return null;
        }
        return obj;
    }
}
