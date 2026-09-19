package com.example.demo.userServicePerformance;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class SharedTestData {

    private SharedTestData() {
    }

    public static final AtomicReference<List<String>> USER_IDS =
            new AtomicReference<>();
}