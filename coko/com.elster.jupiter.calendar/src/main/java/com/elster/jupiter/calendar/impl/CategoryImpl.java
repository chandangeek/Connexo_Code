/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Provides an implementation for the {@link com.elster.jupiter.calendar.Category} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-15
 */
final class CategoryImpl implements Category {

    public enum Fields {
        ID("id"),
        NAME("name");

        private String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    @NotEmpty(message = "{" + MessageSeeds.Constants.REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Constants.CATEGORY_NAME_FIELD_TOO_LONG + "}")
    private String name;

    private final ServerCalendarService calendarService;
    private final Thesaurus thesaurus;

    public CategoryImpl init(OutOfTheBoxCategory category) {
        this.name = category.name();
        return this;
    }

    @Inject
    CategoryImpl(ServerCalendarService calendarService, Thesaurus thesaurus) {
        this.calendarService = calendarService;
        this.thesaurus = thesaurus;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void save() {
        Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
    }

    @Override
    public String getDisplayName() {
        try {
            switch (OutOfTheBoxCategory.valueOf(this.name)) {
                case TOU: {
                    return thesaurus.getFormat(TranslationKeys.CALENDAR_CATEGORY_TOU).format();
                }
                case WORKFORCE: {
                    return thesaurus.getFormat(TranslationKeys.CALENDAR_CATEGORY_WORKFORCE).format();
                }
                case COMMANDS: {
                    return thesaurus.getFormat(TranslationKeys.CALENDAR_CATEGORY_COMMANDS).format();
                }
                default: {
                    return this.getName();
                }
            }
        } catch (IllegalArgumentException e) {
            /* Code must be running on database that was initialized with previous
             * version of the Installer or upgrader that used the default translation as the name.
             * be leniant for now and return that translated name. */
            return this.name;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CategoryImpl category = (CategoryImpl) o;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}