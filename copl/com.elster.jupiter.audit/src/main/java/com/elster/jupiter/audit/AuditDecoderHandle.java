/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface AuditDecoderHandle {

    String getDomain();

    List<String> getPrivileges();

    AuditDecoder getAuditDecoder(String reference);
}
