/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.function.Supplier;

public class ConcurrentModificationExceptionBuilder {
    private final Thesaurus thesaurus;

    private MessageSeed messageTitle;
    private Object[] messageTitleArgs;
    private MessageSeed messageBody;
    private Object[] messageBodyArgs;
    private Supplier<Long> version;
    private Object[] parentId;
    private Supplier<Long> parentVersion;

    ConcurrentModificationExceptionBuilder(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    ConcurrentModificationExceptionBuilder(Thesaurus thesaurus, String objectName) {
        this(thesaurus);
        this.messageTitleArgs = new Object[]{objectName};
        this.messageBodyArgs = this.messageTitleArgs;
    }

    /**
     * Provides an error message for user
     *
     * @param messageTitle
     * @param arguments
     * @return
     */
    public ConcurrentModificationExceptionBuilder withMessageTitle(MessageSeed messageTitle, Object... arguments) {
        this.messageTitle = messageTitle;
        this.messageTitleArgs = arguments;
        return this;
    }

    /**
     * Provides a detailed error explanation for user
     *
     * @param messageBody
     * @param arguments
     * @return the builder
     */
    public ConcurrentModificationExceptionBuilder withMessageBody(MessageSeed messageBody, Object... arguments) {
        this.messageBody = messageBody;
        this.messageBodyArgs = arguments;
        return this;
    }

    /**
     * @param actualObjectVersion supplier for current version of object, <code>null</code> means that object was removed
     * @return the builder
     */
    public ConcurrentModificationExceptionBuilder withActualVersion(Supplier<Long> actualObjectVersion) {
        this.version = actualObjectVersion;
        return this;
    }

    public ConcurrentModificationExceptionBuilder withActualParent(Supplier<Long> parentActualVersion, Object... parentId) {
        this.parentId = parentId;
        this.parentVersion = parentActualVersion;
        return this;
    }

    public ConcurrentModificationException build() {
        ConcurrentModificationException underConstruction = new ConcurrentModificationException(thesaurus, this.messageTitle, messageTitleArgs);
        underConstruction.setMessageBody(this.messageBody);
        underConstruction.setMessageBodyArgs(this.messageBodyArgs);
        underConstruction.setVersion(this.version);
        underConstruction.setParentId(this.parentId);
        underConstruction.setParentVersion(this.parentVersion);
        return underConstruction;
    }

    public Supplier<ConcurrentModificationException> supplier() {
        ConcurrentModificationException exception = build();
        return () -> exception;
    }
}
