package com.diarpy.transactionaggregator;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Mack_TB
 * @since 13/04/2024
 * @version 1.0.4
 */

@Service
public class AppService {

    @Cacheable(cacheNames = "data", key = "#account")
    public ResponseEntity<List<Transaction>> fetchData(@RequestParam String account) {
        List<Transaction> transactions = new ArrayList<>();
        List<Transaction> transactions1, transactions2;;
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl1 = "http://localhost:8888/transactions?account="+ account;
        String apiUrl2 = "http://localhost:8889/transactions?account="+ account;

        // make the two request asynchronously so that waiting for a response doesn't
        // block the thread
//        ResponseEntity<List<Transaction>> response1 = fetchDataWithRetry(restTemplate, apiUrl1);
        CompletableFuture<ResponseEntity<List<Transaction>>> cf1 = CompletableFuture.supplyAsync(() -> fetchDataWithRetry(restTemplate, apiUrl1));
//        ResponseEntity<List<Transaction>> response2 = fetchDataWithRetry(restTemplate, apiUrl2);
        CompletableFuture<ResponseEntity<List<Transaction>>> cf2 = CompletableFuture.supplyAsync(() -> fetchDataWithRetry(restTemplate, apiUrl2));
        try {
            transactions1 = cf1.get().getBody();
            transactions2 = cf2.get().getBody();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

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
