package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

public interface Group extends HasName {

    long getId();

    boolean hasPrivilege(String privilegeName);

    void grant(String privilegeName);

    long getVersion();

    void save();

    void delete();
}
