package org.example.gateway.infrastructure;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

//@Component
//public class RateLimitErrorAttributes extends DefaultErrorAttributes {
//
//    //test
//    @Override
//    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
//        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
//
//        Throwable error = getError(request);
//
//        if (error instanceof ResponseStatusException statusException
//                && statusException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
//
//            errorAttributes.put("status", 429);
//            errorAttributes.put("error", "Too Many Requests");
//            errorAttributes.put("message", "Posielate príliš veľa požiadaviek. Skúste to prosím neskôr.");
//        }
//
//        return errorAttributes;
//    }
//}