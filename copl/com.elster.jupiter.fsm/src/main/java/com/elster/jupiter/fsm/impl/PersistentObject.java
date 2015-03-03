package com.elster.jupiter.fsm.impl;

import java.time.Instant;

/**
 * Provides structural code reuse opportunities for
 * objects that have persistent state.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:22)
 */
public abstract class PersistentObject {

    private long id;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    protected long getId() {
        return id;
    }

    protected String getUserName() {
        return userName;
    }

    protected long getVersion() {
        return version;
    }

    protected Instant getCreationTimestamp() {
        return createTime;
    }

    protected void setCreationTimestamp(Instant createTime) {
        this.createTime = createTime;
    }

    protected Instant getModifiedTimestamp() {
        return modTime;
    }

    protected void setModifiedTimestamp(Instant modTime) {
        this.modTime = modTime;
    }

}