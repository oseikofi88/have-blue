package works.wima.Routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class HealthCheckResponse {

    @Value("${ENVIRONMENT}")
    private String ENVIRONMENT;

    public HashMap<String, Object> returnHealthStatus() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("email", "wima88.ok@gmail.com ");
        response.put("version", "1.0.0"); // TODO: fetch this from the properties file
        response.put("status", "OK");
        return response;
    }

}
