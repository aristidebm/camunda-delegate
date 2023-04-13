package org.com.camunda.example.delegate;

/*
* Stub of camunda execution engine and field injection mechanism.
 */
public class Main {
    public static void main(String[] args) {

        // inject fields to the delegate.
        Expression orderState = new Expression("acknowledged");
        Expression maxRetries = new Expression("3");
        BaseApi api = new BaseApi();
        api.setOrderState(orderState);
        api.setMaxRetries(maxRetries);

        // Execute the delegate.
        DelegateExecution execution = new DelegateExecution();
        try {
            api.execute(execution);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}