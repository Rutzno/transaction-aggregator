package com.diarpy.transactionaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author Mack_TB
 * @since 13/04/2024
 * @version 1.0.4
 */

@EnableCaching
@SpringBootApplication
public class TransactionAggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionAggregatorApplication.class, args);
	}

}
