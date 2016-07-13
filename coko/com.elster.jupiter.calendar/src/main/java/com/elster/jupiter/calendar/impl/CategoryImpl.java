package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.MessageSeeds;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;

import javax.inject.Inject;
import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link com.elster.jupiter.calendar.Category} interface.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-04-15
 */
@UniqueCategoryName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_CATEGORY_NAME + "}")
class CategoryImpl implements Category {

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

    public CategoryImpl init(String name) {
        this.name = name;
        return this;
    }

    @Inject
    CategoryImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void save() {
        Save.CREATE.save(calendarService.getDataModel(), this, Save.Create.class);
    }

}