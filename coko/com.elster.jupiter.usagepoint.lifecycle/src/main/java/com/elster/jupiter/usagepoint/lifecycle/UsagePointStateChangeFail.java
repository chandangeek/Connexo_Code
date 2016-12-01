package com.elster.jupiter.usagepoint.lifecycle;

public interface UsagePointStateChangeFail {
    enum FailSource {
        ACTION,
        CHECK,;
    }

    FailSource getFailSource();

    String getKey();

    String getName();

    String getMessage();
}
