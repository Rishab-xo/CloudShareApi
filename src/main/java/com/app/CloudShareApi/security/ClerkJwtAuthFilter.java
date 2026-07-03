package com.app.CloudShareApi.security;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ClerkJwtAuthFilter extends OncePerRequestFilter {

    @Value("${clerk.issuer}")
    private String clerkIssuer;

    private final ClerkJwksProvider jwksProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().contains("/webhooks")){
            filterChain.doFilter(request, response);
        }
        String authHeader = request.getHeader("Authoriszation");

        if (authHeader == null || !authHeader.startsWith("Bearer") ){
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Autherization header missing/invalid");
            return;
        }

        try {
            String token = authHeader.substring(7);
            String[] chunks = token.split("\\.");
            if (chunks.length < 3) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid Jwt Token format");
                return;
            }
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode headerNode = mapper.readTree(headerJson);

            if (!headerNode.has("kid")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token header is missi");
                return;
            }

            String kid = headerNode.get("kid").asText();

            PublicKey publicKey = jwksProvider.getPublicKey(kid);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        filterChain.doFilter(request, response);
    }
}
