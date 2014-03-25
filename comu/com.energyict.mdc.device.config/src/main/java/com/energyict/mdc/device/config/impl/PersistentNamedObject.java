package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.exceptions.DuplicateNameException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides code reuse opportunities for entities in this bundle
 * that have a name that is unique across all entities of the same type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-31 (13:38)
 */
public abstract class PersistentNamedObject<T> extends PersistentIdObject<T> {

    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private String name;

    protected PersistentNamedObject(Class<T> domainClass, DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(domainClass, dataModel, eventService, thesaurus);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name!=null){
            name = name.trim();
        }
        if (!is(name).equalTo(this.getName())) {
            this.validateUniqueName(name);
        }
        this.name = name;
    }

    protected void validateUniqueName(String name) {
        if (this.findOtherByName(name) != null) {
            throw this.duplicateNameException(this.getThesaurus(), name);
        }
    }

    protected abstract DuplicateNameException duplicateNameException(Thesaurus thesaurus, String name);

    private T findOtherByName(String name) {
        return this.getDataMapper().getUnique("name", name).orNull();
    }

}