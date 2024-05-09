package com.diarpy.transactionaggregator;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Mack_TB
 * @since 13/04/2024
 * @version 1.0.2
 */

@RestController
public class AppController {

    /*@GetMapping("/aggregate")
     public String getPing() {
         RestTemplate restTemplate = new RestTemplate();
         String apiUrl = "http://localhost:8889/ping";
         return restTemplate.getForObject(apiUrl, String.class);
     }*/
    @GetMapping("/aggregate")
    public List<Transaction> getPing(@RequestParam String account) {
        List<Transaction> transactions = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl1 = "http://localhost:8888/transactions?account="+account;
        String apiUrl2 = "http://localhost:8889/transactions?account="+account;
        ResponseEntity<List<Transaction>> response1 = restTemplate.exchange(apiUrl1, HttpMethod.GET, null, new ParameterizedTypeReference<List<Transaction>>() {});
        List<Transaction> transactions1 = response1.getBody();
        ResponseEntity<List<Transaction>> response2 = restTemplate.exchange(apiUrl2, HttpMethod.GET, null, new ParameterizedTypeReference<List<Transaction>>() {});
        List<Transaction> transactions2 = response2.getBody();
        if (transactions1 != null) {
            transactions.addAll(transactions1);
        }
        if (transactions2 != null) {
            transactions.addAll(transactions2);
        }
        transactions.sort(Comparator.comparing(Transaction::getTimestamp).reversed());
        return transactions;
    }
}
