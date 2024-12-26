package example.gatling.sse;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(
                exchange.mutate()
                        .response(serverHttpResponseDecorator(exchange.getResponse()))
                        .build()
        );
    }

    private static ServerHttpResponseDecorator serverHttpResponseDecorator(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                log.debug("Joining Buffers...");
                return DataBufferUtils.join(body)
                        .flatMap(db -> {
                            var cached = cacheAndApply(db);
                            log.debug("Response body : {}", db.toString(StandardCharsets.UTF_8));
                            return getDelegate().writeWith(Mono.just(cached));
                        })
                        .doOnDiscard(DataBuffer.class, DataBufferUtils::release);
            }

            private DataBuffer cacheAndApply(DataBuffer originalDataBuffer) {
                var dataBuffer = originalDataBuffer.retainedSlice(0, originalDataBuffer.readableByteCount());
                DataBufferUtils.release(dataBuffer);
                return originalDataBuffer;
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }
        };
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
