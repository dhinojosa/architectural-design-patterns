package com.xyzcorp.resilience4j;

import java.util.Objects;
import java.util.StringJoiner;

public class MyHttpResponse {
    private final int status;
    private final String message;

    public MyHttpResponse(int status, String message) {
        Objects.requireNonNull(message, "Message cannot be null");
        this.status = status;
        this.message = message;
    }

    public int status() {
        return status;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyHttpResponse that = (MyHttpResponse) o;
        return status == that.status && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MyHttpResponse.class.getSimpleName() +
            "[", "]")
            .add("status=" + status)
            .add("message='" + message + "'")
            .toString();
    }
}
