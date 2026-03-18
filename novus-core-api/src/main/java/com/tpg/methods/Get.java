package com.tpg.methods;

public class Get extends ApiCore<Get> {
    private Get(String url) {
        super(url);
    }

    @Override
    public Get execute() {
        apiResponse = api.context()
            .get(endpoint, requestOptions);
        log.info("Executing GET on " + apiResponse.url());
        log.debug(apiResponse.text());
        return this;
    }

    public static Get atUrl(String url) {
        return new Get(url);
    }
}
