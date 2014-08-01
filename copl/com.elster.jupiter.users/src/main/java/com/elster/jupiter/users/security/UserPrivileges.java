package com.elster.jupiter.users.security;

import java.util.HashMap;

public enum UserPrivileges {
    UPDATE(100, "privilege.update"),
    VIEW(101, "privilege.view");

    private long id;
    private String value;

    private UserPrivileges(long id, String value){
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
        for(UserPrivileges privilege : UserPrivileges.values()){
            list.put(privilege.getId(), privilege.getValue());
        }

        return list;
    }
}
