/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.EventType;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.RelativePeriodCategoryUsage;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Unique(fields = "name", groups = Save.Create.class, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
final class RelativePeriodCategoryImpl extends EntityImpl implements RelativePeriodCategory {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;

    private List<RelativePeriodCategoryUsage> relativePeriodCategoryUsages = new ArrayList<>();

    private final Thesaurus thesaurus;

    @Inject
    RelativePeriodCategoryImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(dataModel, eventService);
        this.thesaurus = thesaurus;
    }

    static RelativePeriodCategoryImpl from(DataModel dataModel, String name) {
        return dataModel.getInstance(RelativePeriodCategoryImpl.class).initialize(name);
    }

    RelativePeriodCategoryImpl initialize(String name) {
        this.setName(name);
        return this;
    }

    void setName(String name) {
        this.name = name.trim();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(new SimpleTranslationKey(this.name, this.name)).format();
    }

    @Override
    EventType created() {
        return EventType.RELATIVE_PERIOD_CATEGORY_CREATED;
    }

    @Override
    EventType updated() {
        return EventType.RELATIVE_PERIOD_CATEGORY_UPDATED;
    }

    @Override
    EventType deleted() {
        return EventType.RELATIVE_PERIOD_CATEGORY_DELETED;
    }
}
