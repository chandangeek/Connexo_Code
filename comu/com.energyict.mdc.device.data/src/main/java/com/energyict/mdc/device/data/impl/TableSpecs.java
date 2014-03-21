package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethod;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethodImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.PluggableService;

import static com.elster.jupiter.orm.ColumnConversion.DATE2DATE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2UTCINSTANT;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum TableSpecs {

    MDCCONNECTIONMETHOD {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ConnectionMethod> table = dataModel.addTable(name(), ConnectionMethod.class);
            table.map(ConnectionMethodImpl.class);
            Column id = table.addAutoIdColumn();
            Column connectionTypePluggableClass = table.column("CONNECTIONTYPEPLUGGABLECLASS").number().conversion(NUMBER2LONG).map("pluggableClassId").notNull().add();
            table.column("NAME").varChar(255).map("name").add();
            Column comPortPool = table.column("COMPORTPOOL").number().notNull().add();
            table.primaryKey("PK_MDCCONNECTIONMETHOD").on(id).add();
            table.foreignKey("FK_MDCCONNMETHOD_CLASS").on(connectionTypePluggableClass).references(PluggableService.COMPONENTNAME, "EISPLUGGABLECLASS").map("pluggableClass").add();
            table.foreignKey("FK_MDCCONNTASKUSAGE_CPP").on(comPortPool).references(EngineModelService.COMPONENT_NAME, "MDCCOMPORTPOOL").map("comPortPool").add();
        }
    },

    MDCCONNECTIONTASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ConnectionTask> table = dataModel.addTable(name(), ConnectionTask.class);
            table.map(ConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            // Common columns
            // Todo: change to FK and reference once Device (JP-1122) is properly moved to the mdc.device.data bundle
            table.column("RTU").number().conversion(NUMBER2LONG).map("deviceId").add();
            Column connectionMethod = table.column("CONNECTIONMETHOD").number().add();
            table.column("MOD_DATE").type("DATE").map("modificationDate").add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2DATE).map("obsoleteDate").add();
            table.column("ISDEFAULT").number().conversion(NUMBER2BOOLEAN).map("isDefault").add();
            table.column("PAUSED").number().conversion(NUMBER2BOOLEAN).map("paused").add();
            table.column("LASTCOMMUNICATIONSTART").number().conversion(NUMBER2UTCINSTANT).map("lastCommunicationStart").add();
            table.column("LASTSUCCESSFULCOMMUNICATIONEND").conversion(NUMBER2UTCINSTANT).number().map("lastSuccessfulCommunicationEnd").add();
            Column comServer = table.column("COMSERVER").number().add();
            Column comPortPool = table.column("COMPORTPOOL").number().add();
            // Todo: change to FK and reference once PartialConnectionTask (JP-809) is properly moved to the mdc.device.config bundle
            table.column("PARTIALCONNECTIONTASK").number().conversion(NUMBER2LONG).map("partialConnectionTaskId").add();
            // Common columns for sheduled connection tasks
            table.column("CURRENTRETRYCOUNT").number().conversion(NUMBER2LONG).map("currentRetryCount").add();
            table.column("LASTEXECUTIONFAILED").number().conversion(NUMBER2BOOLEAN).map("lastExecutionFailed").add();
            // ScheduledConnectionTaskImpl columns
            table.column("COMWINDOWSTART").number().conversion(NUMBER2LONG).map("comWindow.start.millis").add();
            table.column("COMWINDOWEND").number().conversion(NUMBER2LONG).map("comWindow.end.millis").add();
            Column nextExecutionSpecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("NEXTEXECUTIONTIMESTAMP").number().conversion(NUMBER2UTCINSTANT).map("nextExecutionTimestamp").add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP").number().conversion(NUMBER2UTCINSTANT).map("plannedNextExecutionTimestamp").add();
            table.column("CONNECTIONSTRATEGY").number().conversion(NUMBER2ENUM).map("connectionStrategy").add();
            table.column("PRIORITY").number().conversion(NUMBER2INT).map("priority").add();
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2BOOLEAN).map("allowSimultaneousConnections").add();
            Column initiator = table.column("INITIATOR").number().add();
            // InboundConnectionTaskImpl columns: none at this moment
            // ConnectionInitiationTaskImpl columns: none at this moment
            table.primaryKey("PK_MDCCONNTASK").on(id).add();
            table.foreignKey("FK_MDCCONNTASK_METHOD").on(connectionMethod).references(MDCCONNECTIONMETHOD.name()).map("connectionMethod").add();
            table.foreignKey("FK_MDCCONNTASK_CPP").on(comPortPool).references(EngineModelService.COMPONENT_NAME, "MDCCOMPORTPOOL").map("comPortPool").add();
            table.foreignKey("FK_MDCCONNTASK_COMSERVER").on(comServer).references(EngineModelService.COMPONENT_NAME, "MDCCOMSERVER").map("comServer").add();
            table.foreignKey("FK_MDCCONNTASK_INITIATOR").on(initiator).references(MDCCONNECTIONTASK.name()).map("initiationTask").add();
            table.foreignKey("FK_MDCCONNTASK_NEXTEXEC").on(nextExecutionSpecs).references(DeviceConfigurationService.COMPONENTNAME, "MDCNEXTEXECUTIONSPEC").map("nextExecutionSpecs").add();
        }
    };

    abstract void addTo(DataModel component);

}