/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import java.time.Clock;

/**
 * Created by igh on 18/04/2016.
 */
public interface ServerCalendarService extends CalendarService {

    DataModel getDataModel();

    Thesaurus getThesaurus();

    UserService getUserService();

    EventService getEventService();

    MessageService getMessageService();

    IdsService getIdsService();

    void createVault();

    Vault getVault();

    void createRecordSpec();

    RecordSpec getRecordSpec();

    Clock getClock();

}