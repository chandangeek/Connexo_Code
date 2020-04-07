/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.impl.MessageSeeds;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SensitiveExceptionInfo extends GenericExceptionInfo {

    @Inject
    public SensitiveExceptionInfo(Thesaurus thesaurus) {
        super(thesaurus);
    }

    public SensitiveExceptionInfo from(Exception exception){
        this.message = thesaurus.getSimpleFormat(MessageSeeds.INTERNAL_CONNEXO_ERROR).format();
        this.error = thesaurus.getSimpleFormat(MessageSeeds.PERSISTENCE_ERROR).format();
        this.errorCode = super.getErrorCode();
        return this;
    }

}
