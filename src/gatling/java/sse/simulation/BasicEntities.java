package sse.simulation;

import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class BasicEntities {

    static final HttpProtocolBuilder httpProtocol =
            http.baseUrl("http://localhost:8080")
                    .check(status().is(200))
                    .sseUnmatchedInboundMessageBufferSize(200);
}
