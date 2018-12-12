/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.collections;

import java.util.function.Consumer;

public interface ObserverContainer<T> {

    Subscription subscribe(T observer);

    void notify(Consumer<T> notification);
}
