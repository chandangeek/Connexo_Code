package com.elster.jupiter.metering.cim;

public interface CimMessageHandlerFactory {

    void addOutputStreamProvider(OutputStreamProvider provider);

    void removeOutputStreamProvider(OutputStreamProvider provider);
}
