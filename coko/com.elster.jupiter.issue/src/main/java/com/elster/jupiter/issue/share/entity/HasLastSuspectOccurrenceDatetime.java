package com.elster.jupiter.issue.share.entity;

import java.time.Instant;

/**
 *
 */
public interface HasLastSuspectOccurrenceDatetime {

    void setLastSuspectOccurrenceDatetime(Instant lastSuspectOccurrenceDatetime);

    Instant getLastSuspectOccurrenceDatetime();

}
