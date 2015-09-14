package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.util.Optional;

/**
 * Provides code reuse opportunities for entities in this bundle
 * that have a name that is unique across all entities of the same type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (13:38)
 */
@HasUniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_UNIQUE + "}")
public abstract class PersistentNamedObject<T> extends PersistentIdObject<T> {

    protected PersistentNamedObject(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(domainClass, dataModel, eventService, thesaurus);
    }

    protected abstract String getName ();

    public void setName(String name) {
        if (name != null) {
            this.doSetName(name.trim());
        }
        else {
            this.doSetName(null);
        }
    }

    protected abstract void doSetName(String name);

    protected boolean validateUniqueName() {
        return this.findOtherByName(this.getName()) == null;
    }

    private T findOtherByName(String name) {
        Optional<T> other = this.getDataMapper().getUnique("name", name);
        if (other.isPresent()) {
            PersistentIdObject otherPersistent = (PersistentIdObject) other.get();
            if (otherPersistent.getId() == this.getId()) {
                return null;
            }
            else {
                return other.get();
            }
        }
        else {
            return null;
        }
    }

}