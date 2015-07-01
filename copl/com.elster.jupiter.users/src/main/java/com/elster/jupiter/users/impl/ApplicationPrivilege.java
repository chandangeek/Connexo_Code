package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Created by Lucian on 7/1/2015.
 */
public class ApplicationPrivilege {
    private String privilegeName;
    private String applicationName;
    @SuppressWarnings("unused")
    private Instant createTime;

    // associations
    private Privilege privilege;
    private final DataModel dataModel;

    @Inject
    private ApplicationPrivilege(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    ApplicationPrivilege init(String applicationName , Privilege privilege) {
        this.privilegeName = privilege.getName();
        this.applicationName = applicationName;
        this.privilege = privilege;
        return this;
    }

    static ApplicationPrivilege from(DataModel dataModel, String applicationName, Privilege privilege) {
        return dataModel.getInstance(ApplicationPrivilege.class).init(applicationName, privilege);
    }

    Privilege getPrivilege() {
        if (privilege == null) {
            privilege = dataModel.mapper(Privilege.class).getExisting(privilegeName);
        }
        return privilege;
    }

    String getApplicationName() {
        return applicationName;
    }

    void persist() {
        dataModel.mapper(ApplicationPrivilege.class).persist(this);
    }

    public void delete() {
        dataModel.mapper(ApplicationPrivilege.class).remove(this);
    }
}
