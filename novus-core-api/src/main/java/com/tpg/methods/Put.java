package com.tpg.methods;

public class Put extends ApiCore<Put> {
    private Put(String endpoint) {
        super(endpoint);
    }

    @Override public Put execute() {
        apiResponse = api.context()
            .put(endpoint, requestOptions);
        log.info("Executing PUT on " + apiResponse.url());
        log.debug(apiResponse.text());
        return this;
    }

    public static Put atUrl(String url) {
        return new Put(url);
    }
}
