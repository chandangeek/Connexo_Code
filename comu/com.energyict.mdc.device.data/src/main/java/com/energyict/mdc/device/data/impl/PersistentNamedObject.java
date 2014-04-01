package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.DuplicateNameException;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides code reuse opportunities for entities in this bundle
 * that have a name that is unique across all entities of the same type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:40)
 */
public abstract class PersistentNamedObject<D> extends PersistentIdObject<D> {

    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private String name;

    protected PersistentNamedObject(Class<D> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(domainClass, dataModel, eventService, thesaurus);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.nameMustBeUnique() && !is(name).equalTo(this.getName())) {
            this.validateUniqueName(name);
        }
        this.name = name;
    }

    protected boolean nameMustBeUnique () {
        return true;
    }

    protected void validateUniqueName(String name) {
        if (this.findOtherByName(name) != null) {
            throw this.duplicateNameException(this.getThesaurus(), name);
        }
    }

    protected abstract DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name);

    private D findOtherByName(String name) {
        return this.getDataMapper().getUnique("name", name).orNull();
    }

}