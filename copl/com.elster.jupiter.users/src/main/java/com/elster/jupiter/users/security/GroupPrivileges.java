package com.elster.jupiter.users.security;

import java.util.HashMap;

public enum GroupPrivileges {
    CREATE(110, "privilege.create"),
    UPDATE(111, "privilege.update"),
    VIEW(112, "privilege.view");

    private long id;
    private String value;

    private GroupPrivileges(long id, String value){
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
        for(GroupPrivileges privilege : GroupPrivileges.values()){
            list.put(privilege.getId(), privilege.getValue());
        }

        return list;
    }
}
