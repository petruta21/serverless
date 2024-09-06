package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
        lambdaName = "api_handler",
        roleName = "api_handler-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "DYNAMODB_TABLE_NAME", value = "${target_table}")
})
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
    private final DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
    private final String tableName = System.getenv("DYNAMODB_TABLE_NAME"); //"cmtr-be3b68d2-Events";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        try {

            String eventId = UUID.randomUUID().toString();

            // Get current timestamp in ISO format
            String createdAt = Instant.now().toString();

            // Parse request body
            PrincipalRequest request = objectMapper.readValue(requestEvent.getBody(), PrincipalRequest.class);

            // Build the item to be inserted into DynamoDB
            Item eventItem = new Item()
                    .withPrimaryKey("id", eventId)
                    .withInt("principalId", request.getPrincipalId())
                    .withString("createdAt", createdAt)
                    .withMap("body", request.getContent());

            Table eventsTable = dynamoDB.getTable(tableName);

            // Save the event to the table
            eventsTable.putItem(eventItem);

            PrincipalResponse.Event event = new PrincipalResponse.Event();
            event.setId(eventId);
            event.setPrincipalId(request.getPrincipalId());
            event.setCreatedAt(createdAt);
            event.setBody(request.getContent());

            PrincipalResponse response = new PrincipalResponse();
            response.setStatusCode(201);
            response.setEvent(event);

            APIGatewayV2HTTPResponse gatewayResponse = new APIGatewayV2HTTPResponse();
            gatewayResponse.setStatusCode(201);
            gatewayResponse.setHeaders(responseHeaders);
            gatewayResponse.setBody(objectMapper.writeValueAsString(response));
            return gatewayResponse;
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            e.printStackTrace(); // this is fine, in the context of lambda execution all stdout is being forwarded to CW logs
            APIGatewayV2HTTPResponse gatewayResponse = new APIGatewayV2HTTPResponse();
            gatewayResponse.setStatusCode(500);
            return gatewayResponse;
        }
    }
}
