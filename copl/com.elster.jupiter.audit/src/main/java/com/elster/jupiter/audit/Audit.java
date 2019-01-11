package com.elster.jupiter.audit;

import java.time.Instant;
import java.util.List;

public interface Audit {

    Long getId();

    String getOperation();

    Instant getChangedOn();

    String getCategory();

    String getSubCategory();

    String getUser();

    List<AuditLog> getLogs();

    String getName();

}
