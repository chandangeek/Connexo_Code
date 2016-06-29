package com.elster.jupiter.orm.impl;

import java.util.List;

interface Difference {

    String description();

    List<String> ddl();
}
