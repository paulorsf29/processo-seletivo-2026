package com.example.Ecomerce.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Stand-in for a real payment gateway: approves a fixed percentage of attempts at random. */
@Component
public class RandomPaymentGatewaySimulator implements PaymentGatewaySimulator {

    private static final int APPROVAL_PERCENTAGE = 80;

    @Override
    public boolean approve() {
        return ThreadLocalRandom.current().nextInt(100) < APPROVAL_PERCENTAGE;
    }
}
