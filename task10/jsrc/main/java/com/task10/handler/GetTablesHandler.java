package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.dto.GetTableResponse;
import com.task10.dto.GetTablesResponse;
import com.task10.handler.util.DynamoDBHelperTables;

import java.util.List;

public class GetTablesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    final ObjectMapper objectMapper = new ObjectMapper();

    private final String tableName = System.getenv("TABLES_TABLE_NAME");
    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {
            String tableId = requestEvent.getPathParameters() != null ? requestEvent.getPathParameters().get("tableId") : null;

            if (tableId == null || tableId.isEmpty()) {
                List<GetTablesResponse.Table> tables = DynamoDBHelperTables.getFromDynamoDB(dynamoDB, tableName);

                GetTablesResponse response = new GetTablesResponse();
                response.setTables(tables);

                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withBody(objectMapper.writeValueAsString(response));
            } else {
                GetTableResponse.Table tableResult = getFromDynamoDBWithTableId(tableId);
                GetTableResponse response = new GetTableResponse();
                response.setTable(tableResult);

                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withBody(objectMapper.writeValueAsString(response));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500);
        }
    }

    private GetTableResponse.Table getFromDynamoDBWithTableId(String tableId) {
        Table table = dynamoDB.getTable(tableName);
        PrimaryKey primaryKey = new PrimaryKey("id", tableId);
        Item item = table.getItem(primaryKey);
        return convertFromTableDBEntityWithTableId(item);
    }

    private GetTableResponse.Table convertFromTableDBEntityWithTableId(Item item) {
        GetTableResponse.Table result = new GetTableResponse.Table();
        result.setId(item.getInt("id"));
        result.setNumber(item.getInt("number"));
        result.setPlaces(item.getInt("places"));
        result.setVip(item.getBoolean("isVip"));
        result.setMinOrder(item.getInt("minOrder"));
        return result;
    }
}
