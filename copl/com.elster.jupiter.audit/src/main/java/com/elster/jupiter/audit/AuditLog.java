package com.elster.jupiter.audit;

import java.util.List;

public interface AuditLog {

    String getName();

    List<AuditLogChanges> getAuditLogChanges();

}
