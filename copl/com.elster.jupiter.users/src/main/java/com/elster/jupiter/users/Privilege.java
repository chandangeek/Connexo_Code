package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

public interface Privilege extends HasName {
	String getName();

    void delete();
}
