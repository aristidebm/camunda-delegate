package org.com.camunda.example.delegate;

public class OrderStateUpdateException extends Exception {
    public OrderStateUpdateException(String errorMessage) {
        super(errorMessage);
    }
}
