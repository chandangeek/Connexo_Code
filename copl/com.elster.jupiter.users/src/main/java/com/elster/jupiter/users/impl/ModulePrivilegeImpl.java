package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.Privilege;

/**
 * Created by Lucian on 6/29/2015.
 */
public class ModulePrivilegeImpl implements Privilege {

    String name;

    ModulePrivilegeImpl(){

    }

    public static ModulePrivilegeImpl from(String privilegeName){
        return new ModulePrivilegeImpl().init(privilegeName);
    }

    private ModulePrivilegeImpl init(String privilegeName){
        this.name = privilegeName;
        return this;
    }
    @Override
    public void delete() {

    }

    @Override
    public String getName() {
        return name;
    }
}
