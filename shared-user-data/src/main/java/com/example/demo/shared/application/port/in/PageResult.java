package com.example.demo.shared.application.port.in;

import java.util.List;
import java.io.Serializable;

public record PageResult<T>(
        List<T> content,
        Page currentPage,
        Page nextPage
) implements Serializable {
    public boolean hasNext() {
        return nextPage != null;
    }
}

