/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.time.Instant;

public interface JournaledRegisterReadingRecord extends ReadingRecord {
    @Override
    JournaledRegisterReadingRecord filter(ReadingType readingType);

    Instant getJournalTime();

    String getUserName();

    Channel getChannel();

}

