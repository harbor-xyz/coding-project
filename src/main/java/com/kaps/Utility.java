package com.kaps;

import java.util.UUID;

public class Utility {

    public static final String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
