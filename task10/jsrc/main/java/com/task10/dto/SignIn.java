package com.task10.dto;

import org.json.JSONObject;

import java.util.Objects;

public class SignIn {

    private String nickName;
    private String password;


    public SignIn(String nickName, String password) {
        if (nickName == null || password == null) {
            throw new IllegalArgumentException("Missing or incomplete data.");
        }
    }

    public static SignIn fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String nickName = json.optString("nickName", null);
        String password = json.optString("password", null);

        return new SignIn(nickName, password);
    }

    public String getNickName() {
        return nickName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignIn signIn = (SignIn) o;
        return Objects.equals(nickName, signIn.nickName) && Objects.equals(password, signIn.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickName, password);
    }
}
