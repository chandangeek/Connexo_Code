package com.elster.jupiter.transaction;

public interface TransactionProperties {

    void setProperty(String name, Object value);

    void removeProperty(String name);

    Object getProperty(String name);
}
