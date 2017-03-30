/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.logging.LogLevel;

import java.util.EnumSet;
import java.util.StringTokenizer;

/**
 * Provides code reuse opportunities for {@link RequestType}s
 * that relate to logging.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:14)
 */
abstract class LoggingRequestType implements RequestType {

    @Override
    public boolean canParse (String name) {
        return name.equalsIgnoreCase(this.getLogLevelName());
    }

    protected abstract String getLogLevelName ();

    protected abstract LogLevel getLogLevel ();

    @Override
    public Request parse (String parameterString) throws UnknownCategoryParseException {
        EnumSet<Category> categories = this.parseCategories(parameterString);
        if (categories.isEmpty()) {
            categories = EnumSet.allOf(Category.class);
        }
        return new LoggingRequest(this.getLogLevel(), categories);
    }

    private EnumSet<Category> parseCategories (String commaSeparatedListOfCategories) throws UnknownCategoryParseException {
        EnumSet<Category> categories = EnumSet.noneOf(Category.class);
        StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfCategories, ",", false);
        while (tokenizer.hasMoreTokens()) {
            String eventCategoryName = tokenizer.nextToken();
            try {
                categories.add(Category.valueOfIgnoreCase(eventCategoryName));
            }
            catch (IllegalArgumentException e) {
                throw new UnknownCategoryParseException(eventCategoryName);
            }
        }
        return categories;
    }

}