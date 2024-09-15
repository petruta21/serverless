package com.task10.handler.util;

import com.amazonaws.services.dynamodbv2.document.*;
import com.task10.dto.GetTablesResponse;

import java.util.ArrayList;
import java.util.List;

public class DynamoDBHelperTables {

    public static List<GetTablesResponse.Table> getFromDynamoDB(DynamoDB dynamoDB, String tableName) {
        List<GetTablesResponse.Table> tableList = new ArrayList<>();
        Table table = dynamoDB.getTable(tableName);

        try {
            ItemCollection<ScanOutcome> items = table.scan();
            for (Item item : items) {
                GetTablesResponse.Table tableRes = convertFromTableDBEntity(item);
                tableList.add(tableRes);
            }
        } catch (Exception e) {
            System.err.println("Unable to scan the table");
            e.printStackTrace();
        }

        return tableList;

    }

    private static GetTablesResponse.Table convertFromTableDBEntity(Item item) {
        GetTablesResponse.Table result = new GetTablesResponse.Table();

        result.setId(item.getInt("id"));
        result.setNumber(item.getInt("number"));
        result.setPlaces(item.getInt("places"));
        result.setVip(item.getBoolean("isVip"));
        result.setMinOrder(item.getInt("minOrder"));
        return result;
    }
}
