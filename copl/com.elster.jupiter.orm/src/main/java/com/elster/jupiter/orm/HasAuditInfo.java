package com.elster.jupiter.orm;

import java.time.Instant;

public interface HasAuditInfo {

    public long getVersion();

    public Instant getCreateTime();

    public Instant getModTime();

    public String getUserName();

}
