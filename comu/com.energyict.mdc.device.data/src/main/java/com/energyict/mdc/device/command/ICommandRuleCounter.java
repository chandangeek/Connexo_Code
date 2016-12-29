package com.energyict.mdc.device.command;

import java.time.Instant;

/**
 * Created by TVN on 29/12/2016.
 */
public interface ICommandRuleCounter {

    Instant getTo();

    Instant getFrom();

    long getCount();

    CounterType getCounterType();


    enum CounterType {
        DAY,
        WEEK,
        MONTH
    }
}
