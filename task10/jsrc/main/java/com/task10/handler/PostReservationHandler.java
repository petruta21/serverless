package com.task10.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.dto.*;
import com.task10.handler.util.DynamoDBHelper;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.UUID;

public class PostReservationHandler extends CognitoSupport implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    final ObjectMapper objectMapper = new ObjectMapper();


    public PostReservationHandler(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }

    private final String tableName = System.getenv("RESERVATIONS_TABLE_NAME");
    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private final String tableNameTables = System.getenv("TABLES_TABLE_NAME");


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        String requestBody = requestEvent.getBody();
        try {
            AddReservationRequest addReservationRequest = objectMapper.readValue(requestBody, AddReservationRequest.class);

            String validationError = validateRequest(addReservationRequest);

            if (validationError != null) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"message\": \"Bad Request: " + validationError + "\"}");
            }

            ReservationDBEntity reservationDBEntity = convertToReservationDBEntity(addReservationRequest);

          /*  if (isConflictingReservation(reservationDBEntity)) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"message\": \"This table is already reserved.\"}");
            }*/

          if(!isTableExist(reservationDBEntity.getTableNumber())){
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"message\": \"This table does not exist.\"}");
            }


            putToDynamoDB(reservationDBEntity);

            AddReservationResponse response = new AddReservationResponse();
            response.setReservationId(reservationDBEntity.getId());

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500);
        }

    }

    private ReservationDBEntity convertToReservationDBEntity(AddReservationRequest addReservationRequest) {
        String eventId = UUID.randomUUID().toString();
        ReservationDBEntity result = new ReservationDBEntity();

        result.setId(eventId);
        result.setTableNumber(addReservationRequest.getTableNumber());
        result.setClientName(addReservationRequest.getClientName());
        result.setPhoneNumber(addReservationRequest.getPhoneNumber());
        result.setDate(addReservationRequest.getDate());
        result.setSlotTimeStart(addReservationRequest.getSlotTimeStart());
        result.setSlotTimeEnd(addReservationRequest.getSlotTimeEnd());
        return result;
    }

    private void putToDynamoDB(ReservationDBEntity reservationDBEntity) {
        Table table = dynamoDB.getTable(tableName);

        Item item = new Item()
                .withPrimaryKey("id", reservationDBEntity.getId())
                .withInt("tableNumber", reservationDBEntity.getTableNumber())
                .withString("clientName", reservationDBEntity.getClientName())
                .withString("phoneNumber", reservationDBEntity.getPhoneNumber())
                .withString("date", reservationDBEntity.getDate())
                .withString("slotTimeStart", reservationDBEntity.getSlotTimeStart())
                .withString("slotTimeEnd", reservationDBEntity.getSlotTimeEnd());

        PutItemOutcome outcome = table.putItem(item);

        System.out.println("Reservation saved with outcome: " + outcome);
    }

    private String validateRequest(AddReservationRequest request) {
        return request.getTableNumber() <= 0 ? "Table number must be greater than 0."
                : request.getClientName().isEmpty() ? "Client name is a mandatory field."
                : request.getPhoneNumber().isEmpty() ? "Phone number is a mandatory field."
                : request.getDate().isEmpty() ? "Date is a mandatory field."
                : request.getSlotTimeStart().isEmpty() ? "Slot time start is a mandatory field."
                : request.getSlotTimeEnd().isEmpty() ? "Slot time end is a mandatory field."
                : null;
    }

    private boolean isTableExist(int tableNumber)
    {
        return DynamoDBHelper.getFromDynamoDB(dynamoDB, tableNameTables)
                .stream()
                .anyMatch(existingTable -> existingTable.getNumber() == tableNumber);
    }

}