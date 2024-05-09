package com.diarpy.transactionaggregator;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
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

    @GetMapping("/aggregate")
    public ResponseEntity<List<Transaction>> fetchData(@RequestParam String account) {
        List<Transaction> transactions = new ArrayList<>();
        List<Transaction> transactions1, transactions2;;
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl1 = "http://localhost:8888/transactions?account="+account;
        String apiUrl2 = "http://localhost:8889/transactions?account="+account;

        ResponseEntity<List<Transaction>> response1 = fetchDataWithRetry(restTemplate, apiUrl1);
        transactions1 = response1.getBody();
        ResponseEntity<List<Transaction>> response2 = fetchDataWithRetry(restTemplate, apiUrl2);
        transactions2 = response2.getBody();

        if (transactions1 != null) {
            transactions.addAll(transactions1);
        }
        if (transactions2 != null) {
            transactions.addAll(transactions2);
        }
        transactions.sort(Comparator.comparing(Transaction::getTimestamp).reversed()); // sort them by the transaction timestamp in the descending order
        return ResponseEntity.ok(transactions);
//        return transactions;
    }

    /**
     * A simple retry pattern:
     * 1. Send a request.
     * 2. Check if the response code is a server error.
     * 3. If yes, send another request until the total number of retries reaches 5.
     * 4. If no, return the received data.
     * @param restTemplate
     * @param url
     * @return
     */
    public ResponseEntity<List<Transaction>> fetchDataWithRetry(RestTemplate restTemplate, String url) {
        int retries = 0;
        ResponseEntity<List<Transaction>> response = null;
        do {
            try {
                response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Transaction>>() {});
                if (isServerError((HttpStatus) response.getStatusCode())) {
                    // Retry if it's a server error
                    System.out.println("Received server error: " + response.getStatusCode() + ". Retrying...");
                    retries++;
                } else {
                    // Return the received data if no server error
                    return response;
                }
            } catch (HttpServerErrorException e) {
                // Retry only if it's a 529 or 503 error
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                    System.out.println("E:Received server error: " + e.getStatusCode() + ". Retrying...");
                    retries++;
                }
            }
        } while (retries < 5);

        // Return the last response (even if it's an error)
        return response;
    }

    private boolean isServerError(HttpStatus statusCode) {
        return statusCode == HttpStatus.TOO_MANY_REQUESTS || statusCode == HttpStatus.SERVICE_UNAVAILABLE;
    }
}
