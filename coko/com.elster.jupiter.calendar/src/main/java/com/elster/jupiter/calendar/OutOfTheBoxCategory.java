/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

/**
 * Defines the names of the {@link Category Categories}
 * that have been created out of the box by the Connxo installer.
 * As an example, you can find the 'time of use' category with the following code:
 * <code>
 * <pre>
 * CalendarService service = ...;
 * service.findCategoryByName(OutOfTheBoxCategory.TOU.name());
 * </pre>
 * </code>
 * Checking if a certain Category is the 'time of use' category
 * can be done with the following code:
 * <code>
 * <pre>
 * Category category = ...;
 * category.getName().equals(OutOfTheBoxCategory.TOU.name());
 * </pre>
 * </code>
 */
public enum OutOfTheBoxCategory {

    TOU, WORKFORCE, COMMANDS;

}