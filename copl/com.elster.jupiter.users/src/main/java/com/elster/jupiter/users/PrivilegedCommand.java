package com.elster.jupiter.users;

import com.elster.jupiter.transaction.Transaction;

public interface PrivilegedCommand<T> extends Transaction<T> {
    String getPrivilegeName();
    String getAuditMessage();
    String getModule();
    String getAction();
}
