package com.energyict.protocols.impl.channels.ip.socket.dsmr;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.protocol.ConnectionProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.naming.CustomPropertySetComponentName;
import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class OutboundTcpIpWithWakeupConnectionPropertiesPersistenceSupport
        implements PersistenceSupport<ConnectionProvider, OutboundTcpIpWithWakeupConnectionProperties> {

    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P40.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_IP_OUT_WKUP_CT";
    }

    /** Because of frequent changes of IP on each wakeup, the journal table will grow uncontrolled.
     * So we'll disable journal and versioning for this table, unless another signaling method is implemented.
     */
    @Override
    public String journalTableName() {
        return null;
    }

    @Override
    public String domainFieldName() {
        return OutboundTcpIpWithWakeupConnectionProperties.Fields.CONNECTION_PROVIDER.javaName();
    }

    @Override
    public String domainColumnName() {
        return OutboundTcpIpWithWakeupConnectionProperties.Fields.CONNECTION_PROVIDER.databaseName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_IP_OUT_WK_CT";
    }

    @Override
    public Class<OutboundTcpIpWithWakeupConnectionProperties> persistenceClass() {
        return OutboundTcpIpWithWakeupConnectionProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        // None of the custom properties are part of the primary key
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        table
                .column(OutboundTcpIpWithWakeupConnectionProperties.Fields.HOST.databaseName())
                .varChar()
                .map(OutboundTcpIpWithWakeupConnectionProperties.Fields.HOST.javaName())
                .add();

        table
                .column(OutboundTcpIpWithWakeupConnectionProperties.Fields.PORT_NUMBER.databaseName())
                .number()
                .map(OutboundTcpIpWithWakeupConnectionProperties.Fields.PORT_NUMBER.javaName())
                .add();

        table
                .column(OutboundTcpIpWithWakeupConnectionProperties.Fields.POOL_RETRIES.databaseName())
                .number()
                .map(OutboundTcpIpWithWakeupConnectionProperties.Fields.POOL_RETRIES.javaName())
                .add();

        Stream
                .of(
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.ENDPOINT_ADDRESS,
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.SOAP_ACTION,
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.SOURCE_ID,
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.TRIGGER_TYPE,
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.WS_USER_ID,
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.WS_USER_PASS
                )
                .forEach(field -> this.addOptionalStringColumnTo(table, field));

        Stream
                .of(
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.CONNECTION_TIMEOUT,
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.WS_CONNECT_TIME_OUT,
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.WS_REQUEST_TIME_OUT,
                        OutboundTcpIpWithWakeupConnectionProperties.Fields.WAITING_TIME
                )
                .forEach(field -> this.addDurationColumnTo(table, field));

    }

    private void addDurationColumnTo(Table table, OutboundTcpIpWithWakeupConnectionProperties.Fields fieldName) {
        table
                .column(fieldName.databaseName()+"VALUE")
                .number()
                .conversion(ColumnConversion.NUMBER2INT)
                .map(fieldName.javaName() + ".count")
                .add();
        table
                .column(fieldName.databaseName()+"UNIT")
                .number()
                .conversion(ColumnConversion.NUMBER2INT)
                .map(fieldName.javaName() + ".timeUnitCode")
                .add();
    }

    private void addOptionalStringColumnTo(Table table, OutboundTcpIpWithWakeupConnectionProperties.Fields fieldName) {
        table
                .column(fieldName.databaseName())
                .varChar()
                .map(fieldName.javaName())
                .add();
    }

}