package com.task10.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetTableResponseWithTableId {
    private Table table;

    public static class Table {
        private Integer id;
        private Integer number; //number of the table
        private Integer places; //amount of people to sit at the table
        private boolean isVip;  // is the table in the VIP hall
        private Integer minOrder;  //optional, table deposit required to book it

        public Table() {
        }

        public Table(Integer id, Integer number, Integer places, boolean isVip, Integer minOrder) {
            if (id == null || number == null || places == null) {
                throw new IllegalArgumentException("Missing or incomplete data.");
            }
            this.id = id;
            this.number = number;
            this.places = places;
            this.isVip = isVip;
            this.minOrder = minOrder;

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

    }
}
