package org.delcom.app.configs;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);

        Map<String, Object> attributes = this.errorAttributes.getErrorAttributes(
            webRequest,
            ErrorAttributeOptions.defaults()
        );

        Integer status = (Integer) attributes.getOrDefault("status", 500);
        String path = (String) attributes.getOrDefault("path", "unknown");
        String error = (String) attributes.getOrDefault("error", "Unknown Error");
        String message = (String) attributes.getOrDefault("message", "Terjadi kesalahan");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", (status == 500) ? "error" : "fail");
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);

        return new ResponseEntity<>(body, HttpStatus.valueOf(status));
    }
}