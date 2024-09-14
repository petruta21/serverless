/*
 * Copyright 2024 EPAM Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.task10.handler;

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
import com.task10.dto.AddTableRequest;
import com.task10.dto.AddTableResponse;
import com.task10.dto.TablesDBEntity;
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
            TablesDBEntity tablesDBEntity = convertToTablesDBEntity(addTableRequest);
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

}