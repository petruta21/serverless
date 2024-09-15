package com.task11.handler;

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
import com.task11.dto.GetTableResponse;
import com.task11.dto.GetTablesResponse;
import com.task11.handler.util.DynamoDBHelperTables;
import org.json.JSONObject;

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
                Item existingTable = getFromDynamoDBWithTableId(tableId);
                if (existingTable == null) {
                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(400)
                            .withBody(new JSONObject().put("error", "table not found").toString());
                }
                GetTableResponse response = convertFromTableDBEntityWithTableId(existingTable);

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

    private Item getFromDynamoDBWithTableId(String tableId) {
        Table table = dynamoDB.getTable(tableName);
        PrimaryKey primaryKey = new PrimaryKey("id", tableId);
        return table.getItem(primaryKey);
    }

    private GetTableResponse convertFromTableDBEntityWithTableId(Item item) {
        GetTableResponse result = new GetTableResponse();
        result.setId(item.getInt("id"));
        result.setNumber(item.getInt("number"));
        result.setPlaces(item.getInt("places"));
        result.setVip(item.getBoolean("isVip"));
        result.setMinOrder(item.getInt("minOrder"));
        return result;
    }
}
