package sse.simulation;

import io.gatling.http.action.sse.SseInboundMessage;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.asLongAs;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.sse;
import static sse.simulation.BasicEntities.httpProtocol;

public class SimulationWithoutProblem extends Simulation {
    ChainBuilder sseChainBuilder = sse("GET messages")
            .get("/v1/projects/stream/events")
            .toChainBuilder();

    ScenarioBuilder scn = scenario("SSE problem test")
            .exec(sseChainBuilder,
                    asLongAs("#{stop.isUndefined()}").on(
                            sse.processUnmatchedMessages((messages, session) -> messages.stream()
                                    .map(SseInboundMessage::message)
                                    .anyMatch(s -> s.contains("\\\"id\\\": \\\"5\\\""))
                                    ? session.set("stop", true) : session)),
                    sse("close").close()
            );

    {
        setUp(scn.injectOpen(
                        constantUsersPerSec(1)
                                .during(Duration.ofSeconds(1)))
                .protocols(httpProtocol));
    }
}
