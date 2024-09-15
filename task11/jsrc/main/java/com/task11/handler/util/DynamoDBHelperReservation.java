package com.task11.handler.util;

import com.amazonaws.services.dynamodbv2.document.*;
import com.task11.dto.GetReservationResponse;

import java.util.ArrayList;
import java.util.List;

public class DynamoDBHelperReservation {

    public static List<GetReservationResponse.Reservations> getFromDynamoDB(DynamoDB dynamoDB, String tableName) {
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

    private static GetReservationResponse.Reservations convertFromReservationDBEntity(Item item) {
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
