package com.tpg.methods;

public class Post extends ApiCore<Post> {
    private Post(String endpoint) {
        super(endpoint);
    }

    public Post execute() {
        apiResponse = api.context()
            .post(endpoint, requestOptions);
        log.info("Executing POST on " + apiResponse.url());
        log.debug(apiResponse.text());
        return this;
    }

    public static Post atUrl(String url) {
        return new Post(url);
    }
}
