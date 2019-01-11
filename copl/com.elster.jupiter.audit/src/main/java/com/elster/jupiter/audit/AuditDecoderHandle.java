package com.elster.jupiter.audit;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface AuditDecoderHandle {

    String getIdentifier();

    AuditDecoder getAuditDecoder(String reference);
}
