package com.debugaftercoffee;

import java.io.Serializable;
import java.util.List;

public class QueryResponse implements Serializable {
    public String message = null;
    public List<String> containers = null;

    public QueryResponse(String message, List<String> containers) {
        this.message = message;
        this.containers = containers;
    }
}