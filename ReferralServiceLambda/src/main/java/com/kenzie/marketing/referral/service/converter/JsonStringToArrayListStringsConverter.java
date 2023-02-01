package com.kenzie.marketing.referral.service.converter;


import com.kenzie.marketing.referral.service.exceptions.InvalidDataException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class JsonStringToArrayListStringsConverter {

    public ArrayList<String> convert(String body) {
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            ArrayList<String> list = gson.fromJson(body, new TypeToken<ArrayList<String>>() {}.getType());
            return list;
        } catch (Exception e) {
            throw new InvalidDataException("Body \"" + body + "\" could not be deserialized into an ArrayList<String>");
        }
    }
}
