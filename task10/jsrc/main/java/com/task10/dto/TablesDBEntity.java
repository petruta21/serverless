package com.task10.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONObject;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TablesDBEntity {
    private Integer id;
    private Integer number; //number of the table
    private Integer places; //amount of people to sit at the table
    private boolean isVip;  // is the table in the VIP hall
    private Integer minOrder;  //optional, table deposit required to book it

    public TablesDBEntity() {
    }

    public TablesDBEntity(Integer id, Integer number, Integer places, boolean isVip, Integer minOrder) {
        if (id == null || number == null || places == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
    }

//    public static TablesDBEntity fromJson(String jsonString) {
//        JSONObject json = new JSONObject(jsonString);
//        Integer id = Integer.valueOf(json.optString("id", null));
//        Integer number = Integer.valueOf(json.optString("number", null));
//        Integer places = Integer.valueOf(json.optString("places", null));
//        boolean isVip = Boolean.parseBoolean(json.optString("isVip", null));
//        Integer minOrder = Integer.valueOf(json.optString("minOrder", null));
//
//        return new TablesDBEntity(id, number, places, isVip, minOrder);
//    }

    public int getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public int getPlaces() {
        return places;
    }

    public boolean isVip() {
        return isVip;
    }

    public int getMinOrder() {
        return minOrder;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setPlaces(Integer places) {
        this.places = places;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public void setMinOrder(Integer minOrder) {
        this.minOrder = minOrder;
    }
}
