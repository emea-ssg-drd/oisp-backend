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

package com.oisp.databackend.handlers;

import com.oisp.databackend.api.Service;
import com.oisp.databackend.datastructures.AdvancedComponent;
import com.oisp.databackend.datastructures.ComponentDataType;
import com.oisp.databackend.datastructures.DeviceData;
import com.oisp.databackend.datastructures.requests.AdvDataInquiryRequest;
import com.oisp.databackend.datastructures.requests.DataInquiryRequest;
import com.oisp.databackend.datastructures.requests.DataSubmissionRequest;
import com.oisp.databackend.datastructures.requests.FirstLastTimestampRequest;
import com.oisp.databackend.datastructures.responses.AdvDataInquiryResponse;
import com.oisp.databackend.datastructures.responses.DataInquiryResponse;
import com.oisp.databackend.datastructures.responses.DataSubmissionResponse;
import com.oisp.databackend.datastructures.responses.FirstLastTimestampResponse;
import com.oisp.databackend.exceptions.ServiceException;
import com.oisp.databackend.exceptions.ConfigEnvironmentException;
import com.oisp.databackend.handlers.requestvalidator.AdvanceDataRequestValidator;
import com.oisp.databackend.handlers.requestvalidator.DataRequestValidator;
import com.oisp.databackend.handlers.requestvalidator.RequestValidator;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.Predicate;

@RestController
@ControllerAdvice
public class Data {

    static final String BYTEARRAY = "ByteArray";
    static final String CBORTYPE = "application/cbor";

    private static final Logger logger = LoggerFactory.getLogger(Data.class);

    @Autowired
    @Qualifier("basicDataInquiryService")
    private Service<DataInquiryRequest, DataInquiryResponse> basicDataInquiryService;
    @Autowired
    @Qualifier("advancedDataInquiryService")
    private Service<AdvDataInquiryRequest, AdvDataInquiryResponse> advancedDataInquiryService;
    @Autowired
    @Qualifier("dataSubmissionService")
    private Service<DataSubmissionRequest, DataSubmissionResponse> dataSubmissionService;
    @Autowired
    @Qualifier("firstLastTimestampService")
    private Service<FirstLastTimestampRequest, FirstLastTimestampResponse> firstLastTimestampService;

    private RequestValidator requestValidator;
    
    private static final String RESPONSE_LOG_ENTRY = "RESPONSE: {}";
    private static final String REQUEST_LOG_ENTRY = "REQUEST: aid: {}";
    private static final String DEBUG_LOG = "{}";

    @RequestMapping(value = "/v1/accounts/{accountId}/dataSubmission", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE, CBORTYPE})
    @ResponseBody
    public ResponseEntity dataSubmission(@PathVariable String accountId, @Valid @RequestBody DataSubmissionRequest request,
                                  BindingResult result) throws ServiceException, BindException {
        logger.info(REQUEST_LOG_ENTRY, accountId);
        logger.debug(DEBUG_LOG, request);

        if (result.hasErrors()) {
            throw new BindException(result);
        } else {
            dataSubmissionService.withParams(accountId, request);

            dataSubmissionService.invoke();
            ResponseEntity res = new ResponseEntity<>(HttpStatus.CREATED);
            logger.info(RESPONSE_LOG_ENTRY, res.getStatusCode());
            return res;
        }
    }

    @RequestMapping(value = "/v1/accounts/{accountId}/dataInquiry", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, CBORTYPE})
    @ResponseBody
    public ResponseEntity dataInquiry(@PathVariable String accountId, @RequestBody DataInquiryRequest request)
            throws ServiceException, ConfigEnvironmentException {
        logger.debug(REQUEST_LOG_ENTRY, accountId);
        logger.debug(DEBUG_LOG, request);
        Predicate<ComponentDataType> p1 = c -> BYTEARRAY.equals(c.getDataType());
        boolean binaryResponse = request.getComponentsWithDataType().values().stream().anyMatch(p1);

        requestValidator = new DataRequestValidator(request);
        requestValidator.validate();

        basicDataInquiryService.withParams(accountId, request);
        DataInquiryResponse dataInquiryResponse = basicDataInquiryService.invoke();

        HttpHeaders httpHeaders = new HttpHeaders();
        if (binaryResponse) {
            httpHeaders.setContentType(MediaType.parseMediaType(CBORTYPE));
        } else {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }
        ResponseEntity res = new ResponseEntity<DataInquiryResponse>(dataInquiryResponse, httpHeaders, HttpStatus.OK);
        logger.debug(RESPONSE_LOG_ENTRY, res.getStatusCode());
        logger.debug(DEBUG_LOG, dataInquiryResponse);
        return res;
    }

    @RequestMapping(value = "/v1/accounts/{accountId}/dataInquiry/advanced", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, CBORTYPE})
    @ResponseBody
    public ResponseEntity advancedDataInquiry(@PathVariable String accountId, @RequestBody final AdvDataInquiryRequest request) throws ServiceException, ConfigEnvironmentException {
        logger.info(REQUEST_LOG_ENTRY, accountId);
        logger.debug(DEBUG_LOG, request);
        Predicate<AdvancedComponent> pred = c -> BYTEARRAY.equals(c.getDataType());

        boolean binaryResponse = false;
        for (DeviceData deviceData: request.getDeviceDataList()) {
            binaryResponse = binaryResponse || deviceData.getComponents().stream().anyMatch(pred);
        }

        requestValidator = new AdvanceDataRequestValidator(request);
        requestValidator.validate();

        advancedDataInquiryService.withParams(accountId, request);
        AdvDataInquiryResponse dataInquiryResponse = advancedDataInquiryService.invoke();

        HttpHeaders httpHeaders = new HttpHeaders();
        if (binaryResponse) {
            httpHeaders.setContentType(MediaType.parseMediaType(CBORTYPE));
        } else {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }
        ResponseEntity res = new ResponseEntity<AdvDataInquiryResponse>(dataInquiryResponse, httpHeaders, HttpStatus.OK);
        logger.info(RESPONSE_LOG_ENTRY, res.getStatusCode());
        logger.debug(DEBUG_LOG, dataInquiryResponse);
        return res;
    }

    @RequestMapping(value = "/v1/accounts/{accountId}/inquiryComponentFirstAndLast", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity firstLastMeasurementTimestamp(@PathVariable String accountId,
                                                 @Valid @RequestBody final FirstLastTimestampRequest request,
                                                 BindingResult result) throws ConfigEnvironmentException, ServiceException, BindException {
        logger.info(REQUEST_LOG_ENTRY, accountId);
        logger.debug(request.toString());

        if (result.hasErrors()) {
            throw new BindException(result);
        } else {
            firstLastTimestampService.withParams(accountId, request);
            FirstLastTimestampResponse firstLastTimestampResponse = firstLastTimestampService.invoke();
            ResponseEntity res = new ResponseEntity<>(firstLastTimestampResponse, HttpStatus.OK);
            logger.info(RESPONSE_LOG_ENTRY, res.getStatusCode());
            logger.debug(DEBUG_LOG, firstLastTimestampResponse);
            return res;
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String index() throws JSONException {
        return this.version();
    }

    @RequestMapping(value = "/v1/version", method = RequestMethod.GET)
    @ResponseBody
    public String version() throws JSONException {
        JSONObject appVersion = new JSONObject();
        appVersion.put("version", System.getenv("VERSION"));
        appVersion.put("name", "backend");
        return appVersion.toString();
    }
}
