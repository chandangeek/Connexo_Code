/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

abstract class PersistentIdObject<T> {

    private long id;

    protected final Class<T> domainClass;
    protected final DataModel dataModel;
    protected final EventService eventService;
    protected final Thesaurus thesaurus;

    protected PersistentIdObject(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.domainClass = domainClass;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    protected DataMapper<T> getDataMapper() {
        return this.dataModel.mapper(this.domainClass);
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected <U> DataMapper<U> mapper(Class<U> api) {
        return this.dataModel.mapper(api);
    }

    public void save () {
        if (this.id > 0) {
            this.post();
        }
        else {
            this.postNew();
        }
    }

    public void delete() {
        this.validateDelete();
        this.doDelete();
    }

    protected EventService getEventService() {
        return this.eventService;
    }

    /**
     * Saves this object for the first time.
     */
    protected void postNew() {
        Save.CREATE.save(this.dataModel, this, Save.Create.class);
    }

    /**
     * Updates the changes made to this object.
     */
    protected void post() {
        Save.UPDATE.save(this.dataModel, this, Save.Update.class);
    }


    /**
     * Deletes this object using the mapper.
     */
    protected abstract void doDelete();

    /**
     * Validates that this object can safely be deleted
     * and throws a {@link com.elster.jupiter.nls.LocalizedException} if that is not the case.
     */
    protected abstract void validateDelete();

    public long getId() {
        return id;
    }

    public void setId(long id){
        this.id = id;
    }
}
