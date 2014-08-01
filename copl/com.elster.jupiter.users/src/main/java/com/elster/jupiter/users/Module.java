package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

public interface Module extends HasName {
    String getName();
    String getDescription();
}
