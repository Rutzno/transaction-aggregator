package com.diarpy.transactionaggregator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Mack_TB
 * @since 13/04/2024
 * @version 1.0.4
 */

@RestController
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/aggregate")
    public ResponseEntity<List<Transaction>> fetchData(@RequestParam String account) {
        return appService.fetchData(account);
    }
}
