package com.task10.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
