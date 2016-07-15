package com.elster.jupiter.orm;

import java.util.List;

public interface Difference {

    String description();

    List<String> ddl();
}
