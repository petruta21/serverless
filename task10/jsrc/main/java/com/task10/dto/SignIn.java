package com.task10.dto;

import org.json.JSONObject;

import java.util.Objects;

public class SignIn {

    private String email;
    private String password;


    public SignIn(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
        this.email = email;
        this.password = password;
    }

    public static SignIn fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String email = json.optString("email", null);
        String password = json.optString("password", null);

        return new SignIn(email, password);
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignIn signIn = (SignIn) o;
        return Objects.equals(email, signIn.email) && Objects.equals(password, signIn.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }
}
