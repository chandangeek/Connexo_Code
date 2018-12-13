/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim.impl;

import com.elster.jupiter.metering.cim.OutputStreamClosure;
import com.elster.jupiter.metering.cim.OutputStreamProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CompositeOutputStreamProvider implements OutputStreamProvider {

    private final List<OutputStreamProvider> providers;

    public CompositeOutputStreamProvider(List<OutputStreamProvider> providers) {
        this.providers = new CopyOnWriteArrayList<>(providers);
    }

    public void addProvider(OutputStreamProvider provider) {
        this.providers.add(provider);
    }

    public void removeProvider(OutputStreamProvider provider) {
        this.providers.remove(provider);
    }

    @Override
    public void writeTo(OutputStreamClosure outputStreamClosure) {
        for (OutputStreamProvider provider : providers) {
            provider.writeTo(outputStreamClosure);
        }
    }
}
