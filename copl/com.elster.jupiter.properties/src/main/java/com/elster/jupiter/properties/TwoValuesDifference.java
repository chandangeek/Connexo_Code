/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

/**
 * Created by dantonov on 28.03.2017.
 */
public abstract class TwoValuesDifference {

    public TwoValuesDifference(Type type) {
        this.type = type;
    }

    public Type type;

    public enum Type {
        absolute, percent
    }
}
