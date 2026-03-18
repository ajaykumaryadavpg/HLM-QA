package com.tpg.methods;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.impl.FormDataImpl;
import com.microsoft.playwright.impl.RequestOptionsImpl;
import com.tpg.driver.ApiDriver;
import com.tpg.services.NovusLoggerService;
import com.tpg.utils.AppContextManager;
import com.tpg.utils.JsonUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public abstract class ApiCore<T> {

    protected ApiDriver api;
    protected String endpoint;
    protected APIResponse apiResponse;
    protected RequestOptionsImpl requestOptions;
    protected static final NovusLoggerService log = NovusLoggerService.init(ApiCore.class);

    protected ApiCore(String endpoint) {
        ApplicationContext appContext = AppContextManager.getAppContext();
        this.api = appContext.getBean(ApiDriver.class);
        this.endpoint = endpoint;
        requestOptions = new RequestOptionsImpl();
    }

    public abstract T execute();

    public ApiCore<T> withParam(String key, int param) {
        requestOptions.setQueryParam(key, param);
        return this;
    }

    public ApiCore<T> withParam(String key, String param) {
        requestOptions.setQueryParam(key, param);
        return this;
    }

    public ApiCore<T> withBasicAuth(String username, String password) {
        requestOptions.setHeader("Authorization", encoded(username, password));
        return this;
    }

    public ApiCore<T> withBody(String payload) {
        requestOptions.setData(payload);
        return this;
    }

    public ApiCore<T> jsonBody(Object payload) {
        requestOptions.setData(JsonUtil.getJsonAsString(payload));
        return this;
    }

    public ApiCore<T> withHeader(String key, String value) {
        requestOptions.setHeader(key, value);
        return this;
    }

    public ApiCore<T> withFormData(Map<String, String> keyValue) {
        FormDataImpl formData = new FormDataImpl();
        for (Map.Entry<String, String> kv : keyValue.entrySet()) {
            formData.set(kv.getKey(), kv.getValue());
        }
        requestOptions.setForm(formData);
        return this;
    }

    public ApiCore<T> withBinary(String pathWithFileName) {
        FormDataImpl formData = new FormDataImpl();
        formData.set("file", Paths.get(System.getProperty("user.dir") + pathWithFileName));
        requestOptions.setMultipart(formData);
        return this;
    }

    public <B> ApiCore<T> withBody(B payload, String type) {
        requestOptions.setData(payload);
        requestOptions.setHeader("Content-type", type);
        return this;
    }

    public <B> B mapToObject(Class<B> t) {
        return JsonUtil.convertToObject(apiResponse.text(), t);
    }

    public <B> List<B> mapToList(Class<B> t) {
        return JsonUtil.convertToList(apiResponse.text(), t);
    }

    public <P, B> P mapToGenericList(Class<P> genericType, Class<B> parameterType) {
        return JsonUtil.getParameterizedObject(genericType, parameterType, apiResponse.text());
    }

    public ApiCore<T> printResponse() {
        log.debug(apiResponse.text());
        return this;
    }

    public ApiCore<T> isOk() {
        log.info("encountered status code : " + apiResponse.status());
        Assert.isTrue(apiResponse.ok(), "api response should be amongst :" + StatusCodes.codes() + " but found " + apiResponse.status());
        log.verificationSuccess(MessageFormat.format("[Status code : {0} encountered]", apiResponse.status()));
        return this;
    }

    public ApiCore<T> isNotOk() {
        Assert.isTrue(!apiResponse.ok(), "api response should not be amongst :" + StatusCodes.codes() + " but found " + apiResponse.status());
        log.verificationSuccess(MessageFormat.format("[Status code : {0} encountered]", apiResponse.status()));
        return this;
    }

    public boolean statusOk() {
        log.info("Response received : " + apiResponse.status());
        return StatusCodes.codes().contains(apiResponse.status());
    }

    public ApiCore<T> statusCodeMatches(int code) {
        Assert.isTrue(apiResponse.status() == code, "api response should be :" + code + " but found " + apiResponse.status());
        log.verificationSuccess(MessageFormat.format("[Status code : {0} encountered]", apiResponse.status()));
        return this;
    }

    private String encoded(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public ApiCore<T> bodyContains(String subString) {
        Assert.isTrue(apiResponse.text().contains(subString), "body should contains text :" + subString + " but found " + apiResponse.text());
        log.verificationSuccess(MessageFormat.format("[Body contains : \"{0}\" substring]", subString));
        return this;
    }

    public String getContent() {
        return apiResponse.text();
    }

    public byte[] getBody() {
        return apiResponse.body();
    }

    public APIResponse getResponse() {
        return apiResponse;
    }

    enum StatusCodes {
        HTTP_200(200), HTTP_201(201), HTTP_202(202), HTTP_203(203), HTTP_204(204);

        private final int httpCode;

        StatusCodes(int httpCode) {
            this.httpCode = httpCode;
        }

        public int code() {
            return httpCode;
        }

        public static List<Integer> codes() {
            return Arrays.stream(StatusCodes.values()).map(StatusCodes::code).toList();
        }
    }
}
