package com.function.Handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.function.Models.Order;
import com.github.javafaker.Faker;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.Cardinality;
import com.microsoft.azure.functions.annotation.EventHubOutput;
import com.microsoft.azure.functions.annotation.EventHubTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;
import java.util.UUID;

/**
 * Azure Functions with HTTP Trigger.
 */
public class OrderHandler {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    // @FunctionName("HttpExample")
    // public HttpResponseMessage run(
    //         @HttpTrigger(
    //             name = "req",
    //             methods = {HttpMethod.GET, HttpMethod.POST},
    //             authLevel = AuthorizationLevel.ANONYMOUS)
    //             HttpRequestMessage<Optional<String>> request,
    //         final ExecutionContext context) {
    //     context.getLogger().info("Java HTTP trigger processed a request.");

    //     // Parse query parameter
    //     final String query = request.getQueryParameters().get("name");
    //     final String name = request.getBody().orElse(query);

    //     if (name == null) {
    //         return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
    //     } else {
    //         return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
    //     }
    // }

    @FunctionName("CreateOrders")
    public HttpResponseMessage createOrders(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "orders") HttpRequestMessage<Optional<String>> request,
            @EventHubOutput(
                name = "eventOutput", 
                eventHubName = "%XmlEventHub%", 
                connection = "XmlEventHubConnectionString") OutputBinding<String[]> eventOutput,
            final ExecutionContext context) {
        try {
            Faker faker = new Faker();
            String[] orders = new String[10];
            XmlMapper xmlMapper = new XmlMapper();

            for (int i = 0; i < 10; i++) {
                Order order = new Order();
                order.id = UUID.randomUUID();
                order.customerName = faker.name().fullName();
                order.productName = faker.commerce().productName();
                order.quantity = faker.number().numberBetween(1, 10);
                order.price = faker.number().randomDouble(2, 1, 100);

                orders[i] = xmlMapper.writeValueAsString(order);
                ;
            }

            eventOutput.setValue(orders);

            context.getLogger().info("Sent 10 orders.");
            return request.createResponseBuilder(HttpStatus.OK).body(orders).build();
        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        }
    }

    @FunctionName("ProcessOrders")
    public void processOrders(
        @EventHubTrigger(
            name = "eventInput", 
            eventHubName = "%XmlEventHub%", 
            connection = "XmlEventHubConnectionString",
            dataType = "string",
            cardinality = Cardinality.MANY) String[] xmlOrders,
        @EventHubOutput(
            name = "eventOutput", 
            eventHubName = "%JsonEventHub%", 
            connection = "JsonEventHubConnectionString") OutputBinding<String[]> eventOutput,
        final ExecutionContext context) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            ObjectMapper jsonMapper = new ObjectMapper();
            String[] jsonOrders = new String[xmlOrders.length];

            for (int i = 0; i < xmlOrders.length; i++) {
                Order order = xmlMapper.readValue(xmlOrders[i], Order.class);
                jsonOrders[i] = jsonMapper.writeValueAsString(order);
            }

            eventOutput.setValue(jsonOrders);

            context.getLogger().info("Processed " + xmlOrders.length + " orders.");
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
        }
    }
}
