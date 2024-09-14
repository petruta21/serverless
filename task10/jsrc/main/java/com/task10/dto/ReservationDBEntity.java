package com.task10.dto;

import java.util.Objects;

public class ReservationDBEntity {
    private String id;
    private Integer tableNumber; // number of the table
    private String clientName;
    private String phoneNumber;
    private String date;  // string in yyyy-MM-dd format
    private String slotTimeStart; // string in "HH:MM" format, like "13:00",
    private String slotTimeEnd;  // string in "HH:MM" format, like "15:00"

    public ReservationDBEntity() {
    }

    public ReservationDBEntity(Integer tableNumber, String clientName, String phoneNumber, String date, String slotTimeStart, String slotTimeEnd) {
        if (tableNumber == null || clientName == null || phoneNumber == null || date == null || slotTimeStart == null || slotTimeEnd == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
        this.tableNumber = tableNumber;
        this.clientName = clientName;
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.slotTimeStart = slotTimeStart;
        this.slotTimeEnd = slotTimeEnd;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDate() {
        return date;
    }

    public String getSlotTimeStart() {
        return slotTimeStart;
    }

    public String getSlotTimeEnd() {
        return slotTimeEnd;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSlotTimeStart(String slotTimeStart) {
        this.slotTimeStart = slotTimeStart;
    }

    public void setSlotTimeEnd(String slotTimeEnd) {
        this.slotTimeEnd = slotTimeEnd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationDBEntity that = (ReservationDBEntity) o;
        return Objects.equals(tableNumber, that.tableNumber) && Objects.equals(clientName, that.clientName) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(date, that.date) && Objects.equals(slotTimeStart, that.slotTimeStart) && Objects.equals(slotTimeEnd, that.slotTimeEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableNumber, clientName, phoneNumber, date, slotTimeStart, slotTimeEnd);
    }
}
