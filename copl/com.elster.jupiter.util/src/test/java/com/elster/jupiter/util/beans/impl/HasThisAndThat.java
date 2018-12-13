/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.beans.impl;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-17 (11:43)
 */
public class HasThisAndThat implements HasThis, HasThat {
    @Override
    public String getThat() {
        return "That";
    }

    @Override
    public String getThis() {
        return "This";
    }

}