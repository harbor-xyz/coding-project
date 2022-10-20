package com.harbor.calendly.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Response {
    private boolean success;
    private Object data;

    public Response(boolean success, Object data) {
        this.data = data;
        this.success = success;
    }
}
