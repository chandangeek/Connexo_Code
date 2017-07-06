/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpgraderV10_3_1 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_3_1(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 3, 1));
        try (Connection connection = this.dataModel.getConnection(true);
             Statement statement = connection.createStatement()) {
            execute(statement, "alter table DQK_DATAQUALITYKPIMEMBER add name varchar2(80 char)");

            execute(statement, "update DQK_DATAQUALITYKPIMEMBER dqk set name = (select member.name from KPI_KPIMEMBER member where member.kpi=dqk.childkpi and member.position=1)");
            execute(statement, "update DQK_DATAQUALITYKPIMEMBER mem set " +
                    "usagepoint=to_number(substr(mem.name, instr(mem.name, '_') +1 , instr(mem.name, ':') - instr(mem.name, '_') - 1)), " +
                    "channelcontainer=to_number(substr(mem.name, instr(mem.name, ':') +1)) " +
                    "where exists (select * from DQK_DATAQUALITYKPI kpi where kpi.id=mem.dataqualitykpi and kpi.discriminator='UPDQ')");
            execute(statement, "update DQK_DATAQUALITYKPIMEMBER mem set " +
                    "device=to_number(substr(mem.name, instr(mem.name, '_') +1 )) " +
                    "where exists (select * from DQK_DATAQUALITYKPI kpi where kpi.id=mem.dataqualitykpi and kpi.discriminator='EDDQ')");
            execute(statement, "update KPI_KPIMEMBER set name=substr(name, 1, instr(name, '_')-1) where kpi in (select childkpi from DQK_DATAQUALITYKPIMEMBER)");
            execute(statement, "update KPI_KPI set keepzeros='N' where id in (select childkpi from DQK_DATAQUALITYKPIMEMBER)");
            execute(statement, "alter table DQK_DATAQUALITYKPIMEMBER drop column name");
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }

    }

}
