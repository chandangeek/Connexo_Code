package com.elster.jupiter.datavault.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import javax.validation.constraints.Size;

/**
 * Serves as the root for all persistent keys stores that have a name.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-20 (11:54)
 */
@Unique(fields="name", groups = {Save.Create.class, Save.Update.class})
abstract class NamedPersistentKeyStoreImpl extends PersistentKeyStoreImpl {
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;

    protected NamedPersistentKeyStoreImpl(DataModel dataModel, ExceptionFactory exceptionFactory) {
        super(dataModel, exceptionFactory);
    }

    @Override
    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }
}