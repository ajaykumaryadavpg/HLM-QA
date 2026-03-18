package com.tpg.methods;

public class Delete extends ApiCore<Delete> {

    private Delete(String endpoint) {
        super(endpoint);
    }

    @Override public Delete execute() {
        apiResponse = api.context()
            .delete(endpoint, requestOptions);
        log.info("Executing PUT on " + apiResponse.url());
        log.debug(apiResponse.text());
        return this;
    }

    public static Delete atUrl(String url) {
        return new Delete(url);
    }

}
