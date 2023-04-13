package org.com.camunda.example.delegate;

import java.util.logging.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class BaseApi {

    private static final Logger LOGGER = Logger.getLogger(BaseApi.class.getName());
    private Expression orderState;
    private Expression maxRetries;

    public void execute(DelegateExecution execution) throws Exception {
        String orderStateValue = (String) orderState.getValue(execution);
        String maxRetriesValue = (String) maxRetries.getValue(execution);
//        String orderId = (String) execution.getVariable("orderId");
        String orderId = "myId";
        String uri = getHost() + "/api/v1/orders/" + orderId + "/state";
        String json = "{\"orderState\" : " + orderStateValue + "}";

        int maxRetriesInt = Integer.parseInt(maxRetriesValue);
        int count = 0;

        while (true) {
            try {
                HttpResponse<String> response = postJson(uri, json);
                if (response.statusCode() >= 400) {
                    String msg = "Cannot update the state of the order with orderId: " + orderId + "\n"
                            + "Response code: " + response.statusCode() + "\n"
                            + "Response body: " + response.body() + "\n"
                            + "Response headers: " + response.headers() + "\n"
                            + "Request path: " + uri;
                    throw new Exception(msg);
                }
            } catch (Exception exp) {
                if (count >= maxRetriesInt) {
                    throw exp;
                } else {
                    count +=1 ;
                    // exponential backoff with base 2.
                    Thread.sleep((long) Math.pow(2, count) * 1000);
                    LOGGER.warning("Retrying request on path " + uri);
                    continue;
                }
            }
            break;
        }
        LOGGER.info("The state of the order with orderId: " + orderId + " is updated to " + orderStateValue + "." + "\n");
    }

    /*
     The setter is required to ease field injection
     as mentioned here https://docs.camunda.org/manual/7.18/user-guide/process-engine/delegation-code/#field-injection
    */
    public void setOrderState(Expression orderState) {
        this.orderState = orderState;
    }

    public void setMaxRetries(Expression maxRetries) {
        this.maxRetries = maxRetries;
    }

    private HttpResponse<String> postJson(String uri, String json) throws Exception {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .version(Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();
        return client.send(request, BodyHandlers.ofString());
    }

    private String getHost() {
        String host = System.getenv("API_INTERNAL_URL");
        host = host != null ? host : "http://127.0.0.1:3001/";
        host = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
        return host;
    }
}