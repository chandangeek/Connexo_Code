package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.HasId;

import java.util.Iterator;
import java.util.List;

/**
 * Provides code reuse opportunities for entities in this bundle
 * that are persistable and have a unique ID.
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/01/14
 * Time: 15:29
 */
public abstract class PersistentIdObject<T> {

    private long id;

    private final Class<T> domainClass;
    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;

    protected PersistentIdObject(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        this.domainClass = domainClass;
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    protected DataModel getDataModel() {
        return dataModel;
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
        if (this.getId() > 0) {
            this.post();
            this.notifyUpdated();
        }
        else {
            this.postNew();
            this.notifyCreated();
        }
    }

    public void delete() {
        this.validateDelete();
        this.doDelete();
        this.notifyDeleted();
    }

    private void notifyCreated() {
        this.getEventService().postEvent(this.createEventType().topic(), this);
    }

    protected EventService getEventService() {
        return this.eventService;
    }

    protected abstract CreateEventType createEventType();

    private void notifyUpdated() {
        this.getEventService().postEvent(this.updateEventType().topic(), this.toUpdateEventSource());
    }

    protected Object toUpdateEventSource() {
        return this;
    }

    protected abstract UpdateEventType updateEventType();

    private void notifyDeleted() {
        this.getEventService().postEvent(this.deleteEventType().topic(), this);
    }

    protected abstract DeleteEventType deleteEventType();

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

    void setId(long id){
        this.id = id;
    }

    /**
     * Checks if the ID of both the given HasId objects is the same.
     * This is not a replacement for the <code>equals</code> method!
     *
     * @param first  a HasId object
     * @param second another HasId object
     * @return true if both the ID's of the given HasId objects is the same
     */
    protected boolean isSameIdObject(HasId first, HasId second) {
        return first.getId() == second.getId();
    }

    /**
     * Checks whether the given list of HasId objects contains the given HasId object.
     * (the match is made using the {@link #isSameIdObject(com.energyict.mdc.common.HasId, com.energyict.mdc.common.HasId)} method
     *
     * @param hasIdList the list containing the HasId objects
     * @param idObject  the hasId object to check if it exists in the list
     * @return true if the idObject is an element of the list, false otherwise
     */
    protected boolean doesListContainIdObject(List<? extends HasId> hasIdList, HasId idObject) {
        for (HasId hasId : hasIdList) {
            if (isSameIdObject(hasId, idObject)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the given HasId object from the given HasId object List.
     *
     * @param hasIdList the list containing the HasId objects
     * @param idObject the hasId object to remove from the list
     */
    protected void removeFromHasIdList(List<? extends HasId> hasIdList, HasId idObject){
        Iterator<? extends HasId> hasIdIterator = hasIdList.iterator();
        while (hasIdIterator.hasNext()){
            HasId hasId = hasIdIterator.next();
            if (isSameIdObject(hasId, idObject)) {
                hasIdIterator.remove();
            }
        }
    }

}