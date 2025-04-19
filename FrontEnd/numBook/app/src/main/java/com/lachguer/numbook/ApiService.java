package com.lachguer.numbook;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class ApiService {
    private static final String BASE_URL = "http://10.0.2.2:8080/api";
    private final Context context;
    private String currentUserId;

    public ApiService(Context context) {
        this.context = context;
    }

    public void createUser(String imei, String phoneNumber, VolleyCallback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("imei", imei);
            jsonBody.put("number", phoneNumber);
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                BASE_URL + "/users",
                response -> handleUserResponse(response, callback),
                error -> handleError(error, callback)
        ) {
            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    private void handleUserResponse(String response, VolleyCallback callback) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            currentUserId = String.valueOf(jsonResponse.getLong("id"));
            callback.onSuccess(response);
        } catch (JSONException e) {
            callback.onError("Invalid server response");
        }
    }

    public void addContact(String name, String phoneNumber, VolleyCallback callback) {
        if (currentUserId == null) {
            callback.onError("User not initialized");
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("number", phoneNumber.replaceAll("[^0-9]", ""));
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                BASE_URL + "/contacts?userId=" + currentUserId,
                response -> callback.onSuccess(response),
                error -> handleError(error, callback)
        ) {
            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    private void handleError(VolleyError error, VolleyCallback callback) {
        String errorMsg = error.getMessage();
        if (error.networkResponse != null) {
            errorMsg = "Status: " + error.networkResponse.statusCode;
        }
        callback.onError(errorMsg);
    }

    public interface VolleyCallback {
        void onSuccess(String result);
        void onError(String error);
    }
}