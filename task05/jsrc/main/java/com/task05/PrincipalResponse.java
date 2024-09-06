package com.task05;

import java.util.Map;

public class PrincipalResponse {
    private Integer statusCode;
    private Event event;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public static class Event {
        private String id;
        private Integer principalId;
        private String createdAt;
        private Map<String, String> body;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getPrincipalId() {
            return principalId;
        }

        public void setPrincipalId(Integer principalId) {
            this.principalId = principalId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public Map<String, String> getBody() {
            return body;
        }

        public void setBody(Map<String, String> body) {
            this.body = body;
        }
    }
}
