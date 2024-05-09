package com.diarpy.transactionaggregator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AppController {

    @GetMapping("/aggregate")
    public String getPing() {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:8889/ping";
        return restTemplate.getForObject(apiUrl, String.class);
    }
}
