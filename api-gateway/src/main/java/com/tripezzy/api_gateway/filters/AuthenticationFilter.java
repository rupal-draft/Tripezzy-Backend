package com.tripezzy.api_gateway.filters;

import com.tripezzy.api_gateway.config.PublicEndpointConfig;
import com.tripezzy.api_gateway.exceptions.TokenValidationException;
import com.tripezzy.api_gateway.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {


    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final JwtService jwtService;
    private final PublicEndpointConfig publicEndpointConfig;


    public AuthenticationFilter(JwtService jwtService, PublicEndpointConfig publicEndpointConfig) {
        super(Config.class);
        this.jwtService = jwtService;
        this.publicEndpointConfig = publicEndpointConfig;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.info("Incoming request: {}", exchange
                    .getRequest());

            final String path = exchange
                    .getRequest()
                    .getURI()
                    .getPath();

            if (publicEndpointConfig.isPublicEndpoint(path)) {
                log.info("Skipping authentication for public endpoint: {}", path);
                return chain.filter(exchange);
            }

            final String tokenHeader = exchange
                    .getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            if(tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                log.error("Authorization header is missing or invalid");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange
                        .getResponse()
                        .setComplete();
            }

            final String token = tokenHeader.split("Bearer ")[1];

            try {
                Map<String, String> userDetails = jwtService.getUserDetails(token);

                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(req -> req
                                .header("X-User-Id", userDetails.get("id"))
                                .header("X-User-Role", userDetails.get("role")))
                        .build();

                return chain.filter(mutatedExchange);
            } catch (JwtException e) {
                log.error("Token validation failed: {}", e.getMessage());
                throw new TokenValidationException("Token validation failed: " + e.getMessage());
            }
        };
    }

    public static class Config{
    }


}
