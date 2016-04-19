package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.Version;
import com.elster.jupiter.util.HasName;

public interface Upgrader extends HasName {

    Version getVersion();

    void migrate() throws Exception;
}
