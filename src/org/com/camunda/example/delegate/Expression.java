package org.com.camunda.example.delegate;

/*
*  Stub of camunda Expression.
* */
public class Expression {
    private final String value;

    public Expression(String value) {
        this.value = value;
    }

    public String getValue(DelegateExecution execution) {
        return value;
    }
}
