package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.dto.GetReservationResponse;

import java.util.ArrayList;
import java.util.List;

public class GetReservationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    final ObjectMapper objectMapper = new ObjectMapper();

    private final String tableName = System.getenv("RESERVATIONS_TABLE_NAME");
    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        try {

            List<GetReservationResponse.Reservations> reservations = getFromDynamoDB();

            GetReservationResponse response = new GetReservationResponse();
            response.setReservations(reservations);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500);
        }
    }

    private List<GetReservationResponse.Reservations> getFromDynamoDB() {
        List<GetReservationResponse.Reservations> reservations = new ArrayList<>();
        Table table = dynamoDB.getTable(tableName);

        try {
            ItemCollection<ScanOutcome> items = table.scan();
            for (Item item : items) {
                GetReservationResponse.Reservations reservation = convertFromReservationDBEntity(item);
                reservations.add(reservation);
            }
        } catch (Exception e) {
            System.err.println("Unable to scan the table:");
            e.printStackTrace();
        }

        return reservations;

    }

    private GetReservationResponse.Reservations convertFromReservationDBEntity(Item item) {
        GetReservationResponse.Reservations result = new GetReservationResponse.Reservations();

        result.setTableNumber(item.getInt("tableNumber"));
        result.setClientName(item.getString("clientName"));
        result.setPhoneNumber(item.getString("phoneNumber"));
        result.setDate(item.getString("date"));
        result.setSlotTimeStart(item.getString("slotTimeStart"));
        result.setSlotTimeEnd(item.getString("slotTimeEnd"));
        return result;
    }

}
