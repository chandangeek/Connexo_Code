package com.elster.jupiter.cbo;

import com.elster.jupiter.util.HasName;

public interface IdentifiedObject extends HasName {

    String getAliasName();
    String getDescription();
    String getMRID();

}
