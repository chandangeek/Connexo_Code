package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.impl.InstallerV10_3Impl;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private static final Version VERSION = version(10, 3);
    private final DataModel dataModel;
    private final EventService eventService;
    private final ServerMeteringService meteringService;
    private final TimeService timeService;
    private final UserService userService;
    private final InstallerV10_3Impl installerV10_3;

    @Inject
    public UpgraderV10_3(DataModel dataModel,
                         ServerMeteringService meteringService,
                         TimeService timeService,
                         EventService eventService,
                         UserService userService,
                         InstallerV10_3Impl installerV10_3) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.timeService = timeService;
        this.userService = userService;
        this.installerV10_3 = installerV10_3;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        installNewEventTypes();
        GasDayRelativePeriodCreator.createAll(this.meteringService, this.timeService);
        userService.addModulePrivileges(installerV10_3);
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeEffectiveContracts(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.METROLOGY_CONTRACT_DELETED)
                .forEach(eventType -> eventType.install(eventService));
    }

    private void upgradeEffectiveContracts(Connection connection) {
        String[] sqlStatements = { "update MTR_EFFECTIVE_CONTRACT set CHANNELS_CONTAINER = (select MTR_CHANNEL_CONTAINER.ID from MTR_CHANNEL_CONTAINER where MTR_CHANNEL_CONTAINER.EFFECTIVE_CONTRACT = MTR_EFFECTIVE_CONTRACT.ID)"};
        for (String sqlStatement : sqlStatements) {
            try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }
}
