package com.elster.jupiter.users.security;

import java.util.HashMap;

public enum DomainPrivileges {
    CREATE(120, "privilege.create"),
    UPDATE(121, "privilege.update"),
    VIEW(122, "privilege.view");

    private long id;
    private String value;

    private DomainPrivileges(long id, String value){
        this.id = id;
        this.value = value;
    }

    public long getId(){
        return this.id;
    }

    public String getValue() {
        return value;
    }

    public static HashMap<Long, String> getValues(){
        HashMap<Long, String> list = new HashMap();
        for(DomainPrivileges privilege : DomainPrivileges.values()){
            list.put(privilege.getId(), privilege.getValue());
        }

        return list;
    }
}
