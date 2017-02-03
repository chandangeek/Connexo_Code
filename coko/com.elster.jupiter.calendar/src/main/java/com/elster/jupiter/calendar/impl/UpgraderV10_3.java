/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.perform;

class UpgraderV10_3 implements Upgrader {

    private final ServerCalendarService calendarService;

    @Inject
    UpgraderV10_3(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        try (
                Connection connection = this.calendarService.getDataModel().getConnection(true);
                Statement statement = connection.createStatement();
        ) {
            introduceEventSetWithExistingCalendarsSQL()
                    .forEach(perform(this::execute).on(statement));
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }

        dataModelUpgrader.upgrade(this.calendarService.getDataModel(), Version.version(10, 3));

        createNewCategories();

        this.calendarService.createVault();
        this.calendarService.createRecordSpec();
    }

    private void createNewCategories() {
        Stream
            .of(OutOfTheBoxCategory.WORKFORCE, OutOfTheBoxCategory.COMMANDS)
            .forEach(this::create);
    }

    private void create(OutOfTheBoxCategory outOfTheBoxCategory) {
        CategoryImpl category = this.calendarService.getDataModel().getInstance(CategoryImpl.class);
        category.init(outOfTheBoxCategory.getDefaultDisplayName());
        category.save();
    }

    private List<String> introduceEventSetWithExistingCalendarsSQL() {
        return Arrays.asList(
                "CREATE TABLE CAL_EVENTSET (\"ID\" NUMBER, \"NAME\" VARCHAR2(80 CHAR))",
                "CREATE UNIQUE INDEX CAL_PK_EVENTSET ON CAL_EVENTSET (\"ID\")",
                "ALTER TABLE CAL_EVENTSET ADD CONSTRAINT CAL_PK_EVENTSET PRIMARY KEY (\"ID\")",
                "ALTER TABLE CAL_EVENTSET MODIFY (\"NAME\" NOT NULL ENABLE)",
                "ALTER TABLE CAL_EVENTSET MODIFY (\"ID\" NOT NULL ENABLE)",
                "CREATE SEQUENCE  \"CAL_EVENTSETID\"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 1000 NOORDER  NOCYCLE",
                "ALTER TABLE CAL_EVENT ADD EVENTSET NUMBER",
                "INSERT INTO CAL_EVENTSET (ID, NAME) select CAL_EVENTSETID.nextVal, NAME from CAL_CALENDAR",
                "UPDATE CAL_EVENT e SET EVENTSET = (select s.id from CAL_EVENTSET s where s.name = (select c.name from CAL_CALENDAR c where c.id = e.CALENDAR))",
                "ALTER TABLE CAL_EVENT MODIFY EVENTSET NUMBER NOT NULL",
                "ALTER TABLE CAL_EVENT ADD CONSTRAINT CAL_EVENT_TO_SET FOREIGN KEY (EVENTSET) REFERENCES CAL_EVENTSET (ID)",
                "ALTER TABLE CAL_EVENT DROP COLUMN CALENDAR",
                "ALTER TABLE CAL_CALENDAR ADD EVENTSET NUMBER",
                "ALTER TABLE CAL_CALENDARJRNL ADD EVENTSET NUMBER",
                "UPDATE CAL_CALENDAR c SET EVENTSET = (select ID from CAL_EVENTSET s where s.name = c.name)",
                "ALTER TABLE CAL_CALENDAR ADD CONSTRAINT CAL_CAL_TO_EVENTSET FOREIGN KEY (EVENTSET) REFERENCES CAL_EVENTSET (ID)"
        );
    }
}
