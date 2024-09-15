package com.task11.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task11.dto.AddTableRequest;
import com.task11.dto.AddTableResponse;
import com.task11.dto.TablesDBEntity;
import com.task11.handler.util.DynamoDBHelperTables;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

public class PostTablesHandler extends CognitoSupport implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    final ObjectMapper objectMapper = new ObjectMapper();

    public PostTablesHandler(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }

    private final String tableName = System.getenv("TABLES_TABLE_NAME");
    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        String requestBody = requestEvent.getBody();
        try {
            AddTableRequest addTableRequest = objectMapper.readValue(requestBody, AddTableRequest.class);
            String validationError = validateRequest(addTableRequest);

            if (validationError != null) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"message\": \"Bad Request: " + validationError + "\"}");
            }

            TablesDBEntity tablesDBEntity = convertToTablesDBEntity(addTableRequest);

            if (isTableExist(tablesDBEntity.getNumber())) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"message\": \"This table already exists.\"}");
            }


            putToDynamoDB(tablesDBEntity);

            AddTableResponse response = new AddTableResponse();
            response.setId(tablesDBEntity.getId());

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500);
        }

    }

    private TablesDBEntity convertToTablesDBEntity(AddTableRequest addTableRequest) {

        TablesDBEntity result = new TablesDBEntity();

        result.setId(addTableRequest.getId());
        result.setNumber(addTableRequest.getNumber());
        result.setPlaces(addTableRequest.getPlaces());
        result.setVip(addTableRequest.isVip());
        result.setMinOrder(addTableRequest.getMinOrder());

        return result;
    }

    private void putToDynamoDB(TablesDBEntity tablesDBEntity) {
        Table table = dynamoDB.getTable(tableName);
        Item item = new Item()
                .withPrimaryKey("id", String.valueOf(tablesDBEntity.getId()))
                .withInt("number", tablesDBEntity.getNumber())
                .withInt("places", tablesDBEntity.getPlaces())
                .withBoolean("isVip", tablesDBEntity.isVip())
                .withInt("minOrder", tablesDBEntity.getMinOrder());

        PutItemOutcome outcome = table.putItem(item);

        System.out.println("Table saved with outcome: " + outcome);
    }

    private String validateRequest(AddTableRequest request) {
        if (request.getNumber() < 0) {
            return "Table number must be greater than 0.";
        }
        if (request.getPlaces() < 0) {
            return "Table places must be greater than 0.";
        }
        return null;
    }

    private boolean isTableExist(int tableNumber) {
        return DynamoDBHelperTables.getFromDynamoDB(dynamoDB, tableName)
                .stream()
                .anyMatch(existingTable -> existingTable.getNumber() == tableNumber);
    }
}