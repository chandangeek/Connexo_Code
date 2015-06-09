package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;

public enum TableSpecs {
    MTR_SERVICECATEGORY {
        @Override
        void addTo(DataModel dataModel) {
            Table<ServiceCategory> table = dataModel.addTable(name(), ServiceCategory.class);
            table.map(ServiceCategoryImpl.class);
            table.cache();
            table.setJournalTableName("MTR_SERVICECATEGORYJRNL");
            Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2ENUMPLUSONE).map("kind").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_SERVICECATEGORY").on(idColumn).add();
        }
    },
    MTR_SERVICELOCATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<ServiceLocation> table = dataModel.addTable(name(), ServiceLocation.class);
            table.map(ServiceLocationImpl.class);
            table.setJournalTableName("MTR_SERVICELOCATIONJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("DIRECTION").varChar(NAME_LENGTH).map("direction").add();
            table.column("EAEMAIL1").varChar(NAME_LENGTH).map("electronicAddress.email1").add();
            table.column("EAEMAIL2").varChar(NAME_LENGTH).map("electronicAddress.email2").add();
            table.column("EALAN").varChar(NAME_LENGTH).map("electronicAddress.lan").add();
            table.column("EAEMAC").varChar(NAME_LENGTH).map("electronicAddress.mac").add();
            table.column("EAPASSWORD").varChar(NAME_LENGTH).map("electronicAddress.password").add();
            table.column("EARADIO").varChar(NAME_LENGTH).map("electronicAddress.radio").add();
            table.column("EAUSERID").varChar(NAME_LENGTH).map("electronicAddress.userID").add();
            table.column("EAWEB").varChar(NAME_LENGTH).map("electronicAddress.web").add();
            table.column("GEOINFOREFERENCE").varChar(NAME_LENGTH).map("geoInfoReference").add();
            table.column("MASTATUSDATETIME").type("number").conversion(NUMBER2INSTANT).map("mainAddress.status.dateTime").add();
            table.column("MASTATUSREASON").varChar(NAME_LENGTH).map("mainAddress.status.reason").add();
            table.column("MASTATUSREMARK").varChar(NAME_LENGTH).map("mainAddress.status.remark").add();
            table.column("MASTATUSVALUE").varChar(NAME_LENGTH).map("mainAddress.status.value").add();
            table.column("MASTREETADDRESSGENERAL").varChar(NAME_LENGTH).map("mainAddress.streetDetail.addressGeneral").add();
            table.column("MASTREETBUILDINGNAME").varChar(NAME_LENGTH).map("mainAddress.streetDetail.buildingName").add();
            table.column("MASTREETCODE").varChar(NAME_LENGTH).map("mainAddress.streetDetail.code").add();
            table.column("MASTREETNAME").varChar(NAME_LENGTH).map("mainAddress.streetDetail.name").add();
            table.column("MASTREETNUMBER").varChar(NAME_LENGTH).map("mainAddress.streetDetail.number").add();
            table.column("MASTREETPREFIX").varChar(NAME_LENGTH).map("mainAddress.streetDetail.prefix").add();
            table.column("MASTREETSUFFIX").varChar(NAME_LENGTH).map("mainAddress.streetDetail.suffix").add();
            table.column("MASTREETSUITENUMBER").varChar(NAME_LENGTH).map("mainAddress.streetDetail.suiteNumber").add();
            table.column("MASTREETTYPE").varChar(NAME_LENGTH).map("mainAddress.streetDetail.type").add();
            table.column("MASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("mainAddress.streetDetail.withinTownLimits").add();
            table.column("MATOWNCODE").varChar(NAME_LENGTH).map("mainAddress.townDetail.code").add();
            table.column("MATOWNCOUNTRY").varChar(NAME_LENGTH).map("mainAddress.townDetail.country").add();
            table.column("MATOWNNAME").varChar(NAME_LENGTH).map("mainAddress.townDetail.name").add();
            table.column("MATOWNSECTION").varChar(NAME_LENGTH).map("mainAddress.townDetail.section").add();
            table.column("MATOWNSTATE").varChar(NAME_LENGTH).map("mainAddress.townDetail.stateOrProvince").add();
            table.column("PHONE1AREA").varChar(NAME_LENGTH).map("phone1.areaCode").add();
            table.column("PHONE1CITY").varChar(NAME_LENGTH).map("phone1.cityCode").add();
            table.column("PHONE1COUNTRY").varChar(NAME_LENGTH).map("phone1.countryCode").add();
            table.column("PHONE1EXTENSION").varChar(NAME_LENGTH).map("phone1.extension").add();
            table.column("PHONE1LOCALNUMBER").varChar(NAME_LENGTH).map("phone1.localNumber").add();
            table.column("PHONE2AREA").varChar(NAME_LENGTH).map("phone2.areaCode").add();
            table.column("PHONE2CITY").varChar(NAME_LENGTH).map("phone2.cityCode").add();
            table.column("PHONE2COUNTRY").varChar(NAME_LENGTH).map("phone2.countryCode").add();
            table.column("PHONE2EXTENSION").varChar(NAME_LENGTH).map("phone2.extension").add();
            table.column("PHONE2LOCALNUMBER").varChar(NAME_LENGTH).map("phone2.localNumber").add();
            table.column("SASTATUSDATETIME").type("number").conversion(NUMBER2INSTANT).map("secondaryAddress.status.dateTime").add();
            table.column("SASTATUSREASON").varChar(NAME_LENGTH).map("secondaryAddress.status.reason").add();
            table.column("SASTATUSREMARK").varChar(NAME_LENGTH).map("secondaryAddress.status.remark").add();
            table.column("SASTATUSVALUE").varChar(NAME_LENGTH).map("secondaryAddress.status.value").add();
            table.column("SASTREETADDRESSGENERAL").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.addressGeneral").add();
            table.column("SASTREETBUILDINGNAME").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.buildingName").add();
            table.column("SASTREETCODE").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.code").add();
            table.column("SASTREETNAME").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.name").add();
            table.column("SASTREETNUMBER").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.number").add();
            table.column("SASTREETPREFIX").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.prefix").add();
            table.column("SASTREETSUFFIX").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.suffix").add();
            table.column("SASTREETSUITENUMBER").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.suiteNumber").add();
            table.column("SASTREETTYPE").varChar(NAME_LENGTH).map("secondaryAddress.streetDetail.type").add();
            table.column("SASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("secondaryAddress.streetDetail.withinTownLimits").add();
            table.column("SATOWNCODE").varChar(NAME_LENGTH).map("secondaryAddress.townDetail.code").add();
            table.column("SATOWNCOUNTRY").varChar(NAME_LENGTH).map("secondaryAddress.townDetail.country").add();
            table.column("SATOWNNAME").varChar(NAME_LENGTH).map("secondaryAddress.townDetail.name").add();
            table.column("SATOWNSECTION").varChar(NAME_LENGTH).map("secondaryAddress.townDetail.section").add();
            table.column("SATOWNSTATE").varChar(NAME_LENGTH).map("secondaryAddress.townDetail.stateOrProvince").add();
            table.column("STATUSDATETIME").type("number").conversion(NUMBER2INSTANT).map("status.dateTime").add();
            table.column("STATUSREASON").varChar(NAME_LENGTH).map("status.reason").add();
            table.column("STATUSREMARK").varChar(NAME_LENGTH).map("status.remark").add();
            table.column("STATUSVALUE").varChar(NAME_LENGTH).map("status.value").add();
            table.column("SERVICELOCATIONTYPE").varChar(NAME_LENGTH).map("type").add();
            table.column("ACCESSMETHOD").varChar(NAME_LENGTH).map("accessMethod").add();
            table.column("NEEDSINSPECTION").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("needsInspection").add();
            table.column("SITEACCESSPROBLEM").varChar(NAME_LENGTH).map("siteAccessProblem").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_SERVICELOCATION").on(idColumn).add();
            table.unique("MTR_U_SERVICELOCATION").on(mRIDColumn).add();
        }
    },
    MTR_AMRSYSTEM {
        @Override
        void addTo(DataModel dataModel) {
            Table<AmrSystem> table = dataModel.addTable(name(), AmrSystem.class);
            table.map(AmrSystemImpl.class);
            Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2INT).map("id").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_AMRSYSTEM").on(idColumn).add();
            table.unique("MTR_U_AMRSYSTEM").on(nameColumn).add();
        }
    },
    MTR_USAGEPOINT {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePoint> table = dataModel.addTable(name(), UsagePoint.class);
            table.map(UsagePointImpl.class);
            table.setJournalTableName("MTR_USAGEPOINTJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            Column serviceKindColumn = table.column("SERVICEKIND").number().notNull().conversion(NUMBER2ENUMPLUSONE).add();
            Column serviceLocationIdColumn = table.column("SERVICELOCATIONID").number().conversion(NUMBER2LONGNULLZERO).add();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            //table.column("AMIBILLINGREADY").number().notNull().conversion(NUMBER2ENUM).map("amiBillingReady").add();
            //table.column("CHECKBILLING").bool().map("checkBilling").add();
            //table.column("CONNECTIONSTATE").number().notNull().conversion(NUMBER2ENUM).map("connectionState").add();
            //table.addQuantityColumns("ESTIMATEDLOAD", false, "estimatedLoad");
            //table.column("GROUNDED").bool().map("grounded").add();
            table.column("ISSDP").bool().map("isSdp").add();
            table.column("ISVIRTUAL").bool().map("isVirtual").add();
            //table.column("MINIMALUSAGEEXPECTED").bool().map("minimalUsageExpected").add();
            //table.addQuantityColumns("NOMINALVOLTAGE",false, "nominalServiceVoltage");
            table.column("OUTAGEREGION").varChar(NAME_LENGTH).map("outageRegion").add();
            //table.column("PHASECODE").type("varchar2(7)").conversion(CHAR2ENUM).map("phaseCode").add();
            //table.addQuantityColumns("RATEDCURRENT", false, "ratedCurrent");
            //table.addQuantityColumns("RATEDPOWER", false , "ratedPower");
            table.column("READCYCLE").varChar(NAME_LENGTH).map("readCycle").add();
            table.column("READROUTE").varChar(NAME_LENGTH).map("readRoute").add();
            //table.column("SERVICEDELIVERYREMARK").varChar(NAME_LENGTH).map("serviceDeliveryRemark").add();
            table.column("SERVICEPRIORITY").varChar(NAME_LENGTH).map("servicePriority").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_USAGEPOINT").on(idColumn).add();
            table.unique("MTR_U_USAGEPOINT").on(mRIDColumn).add();
            table.foreignKey("MTR_FK_USAGEPOINTSERVICECAT").on(serviceKindColumn).references(MTR_SERVICECATEGORY.name()).onDelete(RESTRICT).map("serviceCategory").add();
            table.foreignKey("MTR_FK_USAGEPOINTSERVICELOC").on(serviceLocationIdColumn).references(MTR_SERVICELOCATION.name()).onDelete(RESTRICT).map("serviceLocation").reverseMap("usagePoints").add();
        }
    },
    MTR_READINGTYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingType> table = dataModel.addTable(name(), ReadingType.class);
            table.map(ReadingTypeImpl.class);
            table.cache();
            Column mRidColumn = table.column("MRID").varChar(NAME_LENGTH).notNull().map("mRID").add();
            table.column("ALIASNAME").varChar(SHORT_DESCRIPTION_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_READINGTYPE").on(mRidColumn).add();
        }
    },
    MTR_ENDDEVICE {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDevice> table = dataModel.addTable(name(), EndDevice.class);
            table.map(EndDeviceImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("ENDDEVICETYPE", "char(1)");
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            Column amrSystemIdColumn = table.column("AMRSYSTEMID").type("number").notNull().conversion(NUMBER2INT).map("amrSystemId").add();
            Column amrIdColumn = table.column("AMRID").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("amrId").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("SERIALNUMBER").varChar(NAME_LENGTH).map("serialNumber").add();
            table.column("UTCNUMBER").varChar(NAME_LENGTH).map("utcNumber").add();
            table.column("EAEMAIL1").varChar(NAME_LENGTH).map("electronicAddress.email1").add();
            table.column("EAEMAIL2").varChar(NAME_LENGTH).map("electronicAddress.email2").add();
            table.column("EALAN").varChar(NAME_LENGTH).map("electronicAddress.lan").add();
            table.column("EAEMAC").varChar(NAME_LENGTH).map("electronicAddress.mac").add();
            table.column("EAPASSWORD").varChar(NAME_LENGTH).map("electronicAddress.password").add();
            table.column("EARADIO").varChar(NAME_LENGTH).map("electronicAddress.radio").add();
            table.column("EAUSERID").varChar(NAME_LENGTH).map("electronicAddress.userID").add();
            table.column("EAWEB").varChar(NAME_LENGTH).map("electronicAddress.web").add();
            table.column("MANUFACTUREDDATE").number().map("manufacturedDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("PURCHASEDDATE").number().map("purchasedDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("RECEIVEDDATE").number().map("receivedDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("INSTALLEDDATE").number().map("installedDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("REMOVEDDATE").number().map("removedDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("RETIREDDATE").number().map("retiredDate").conversion(ColumnConversion.NUMBER2INSTANT).add();
            Column stateMachine = table.column("FSM").number().add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_METER").on(idColumn).add();
            table.unique("MTR_U_METER").on(mRIDColumn).add();
            table.unique("MTR_U_METERAMR").on(amrSystemIdColumn, amrIdColumn).add();
            table.foreignKey("MTR_FK_METERAMRSYSTEM").references(MTR_AMRSYSTEM.name()).onDelete(RESTRICT).map("amrSystem").on(amrSystemIdColumn).add();
            table.foreignKey("MTR_FK_ENDDEVICE_FSM")
                    .on(stateMachine)
                    .references(FiniteStateMachineService.COMPONENT_NAME, "FSM_FINITE_STATE_MACHINE")
                    .map("stateMachine")
                    .add();
            table.index("MTR_IDX_ENDDEVICE_NAME").on(nameColumn).add();
        }
    },
    MTR_ENDDEVICESTATUS {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceLifeCycleStatus> table = dataModel.addTable(name(), EndDeviceLifeCycleStatus.class);
            table.map(EndDeviceLifeCycleStatusImpl.class);
            Column endDevice = table.column("ENDDEVICE").notNull().number().add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addAuditColumns();
            Column state = table.column("STATE").notNull().number().add();
            table.primaryKey("PK_MTR_ENDDEVICESTATUS").on(endDevice, intervalColumns.get(0)).add();
            table.foreignKey("FK_MTR_STATUS_ENDDEVICE").
                    on(endDevice).
                    references(MTR_ENDDEVICE.name()).
                    onDelete(CASCADE).
                    map("endDevice").
                    reverseMap("status").
                    composition().
                    add();
            table.foreignKey("FK_MTR_STATUS_STATE").
                    on(state).
                    references(FiniteStateMachineService.COMPONENT_NAME, "FSM_STATE").
                    onDelete(RESTRICT).
                    map("state").
                    add();
        }
    },
    MTR_METERACTIVATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<MeterActivation> table = dataModel.addTable(name(), MeterActivation.class);
            table.map(MeterActivationImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column usagePointIdColumn = table.column("USAGEPOINTID").type("number").conversion(NUMBER2LONGNULLZERO).add();
            Column meterIdColumn = table.column("METERID").type("number").conversion(NUMBER2LONGNULLZERO).add();
            table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.primaryKey("MTR_PK_METERACTIVATION").on(idColumn).add();
            table.foreignKey("MTR_FK_METERACTUSAGEPOINT").references(MTR_USAGEPOINT.name()).onDelete(RESTRICT).map("usagePoint").reverseMap("meterActivations").reverseMapOrder("interval.start").reverseMapCurrent("currentMeterActivation").on(usagePointIdColumn).add();
            table.foreignKey("MTR_FK_METERACTMETER").references(MTR_ENDDEVICE.name()).onDelete(RESTRICT).map("meter").reverseMap("meterActivations").reverseMapOrder("interval.start").reverseMapCurrent("currentMeterActivation").on(meterIdColumn).add();
        }
    },
    MTR_CHANNEL {
        @Override
        void addTo(DataModel dataModel) {
            Table<Channel> table = dataModel.addTable(name(), Channel.class);
            table.map(ChannelImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column meterActivationIdColumn = table.column("METERACTIVATIONID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column timeSeriesIdColumn = table.column("TIMESERIESID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column mainReadingTypeMRIDColumn = table.column("MAINREADINGTYPEMRID").varChar(NAME_LENGTH).notNull().add();
            Column bulkQuantityReadingTypeMRIDColumn = table.column("BULKQUANTITYREADINGTYPEMRID").varChar(NAME_LENGTH).add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_CHANNEL").on(idColumn).add();
            table.foreignKey("MTR_FK_CHANNELACTIVATION").references(MTR_METERACTIVATION.name()).onDelete(RESTRICT).map("meterActivation").reverseMap("channels", TimeSeries.class , ReadingTypeInChannel.class).on(meterActivationIdColumn).composition().add();
            table.foreignKey("MTR_FK_CHANNELMAINTYPE").references(MTR_READINGTYPE.name()).onDelete(RESTRICT).map("mainReadingType").on(mainReadingTypeMRIDColumn).add();
            table.foreignKey("MTR_FK_CHANNELBULQUANTITYTYPE").references(MTR_READINGTYPE.name()).onDelete(RESTRICT).map("bulkQuantityReadingType").on(bulkQuantityReadingTypeMRIDColumn).add();
            table.foreignKey("MTR_FK_CHANNELTIMESERIES").on(timeSeriesIdColumn).references("IDS", "IDS_TIMESERIES").onDelete(RESTRICT).map("timeSeries").add();
        }
    },
    MTR_READINGTYPEINCHANNEL {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeInChannel> table = dataModel.addTable(name(), ReadingTypeInChannel.class);
            table.map(ReadingTypeInChannel.class);
            Column channelIdColumn = table.column("CHANNNELID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
            Column readingTypeMRidColumn = table.column("READINGTYPEMRID").varChar(NAME_LENGTH).notNull().add();
            table.primaryKey("MTR_PK_READINGTYPEINCHANNEL").on(channelIdColumn, positionColumn).add();
            table.foreignKey("MTR_FK_READINGTYPEINCHANNEL1").on(channelIdColumn).references(MTR_CHANNEL.name()).
                    onDelete(CASCADE).map("channel").reverseMap("readingTypeInChannels").reverseMapOrder("position").add();
            table.foreignKey("MTR_FK_READINGTYPEINCHANNEL2").on(readingTypeMRidColumn).references(MTR_READINGTYPE.name()).
                    onDelete(RESTRICT).map("readingType").add();
        }
    },
    MTR_UPACCOUNTABILITY {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointAccountability> table = dataModel.addTable(name(), UsagePointAccountability.class);
            table.map(UsagePointAccountabilityImpl.class);
            table.setJournalTableName("MTR_UPACCOUNTABILITYJRNL");
            Column usagePointIdColumn = table.column("USAGEPOINTID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column partyIdColumn = table.column("PARTYID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column roleMRIDColumn = table.column("ROLEMRID").varChar(NAME_LENGTH).notNull().add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.primaryKey("MTR_PK_UPACCOUNTABILITY").on(usagePointIdColumn, partyIdColumn, roleMRIDColumn, intervalColumns.get(0)).add();
            table.foreignKey("MTR_FK_UPACCOUNTUP").on(usagePointIdColumn).references(MTR_USAGEPOINT.name()).onDelete(RESTRICT).map("usagePoint").reverseMap("accountabilities").composition().add();
            table.foreignKey("MTR_FK_UPACCOUNTPARTY").on(partyIdColumn).references("PRT", "PRT_PARTY").onDelete(RESTRICT).map("party").add();
            table.foreignKey("MTR_FK_UPACCOUNTPARTYROLE").on(roleMRIDColumn).references("PRT", "PRT_PARTYROLE").onDelete(RESTRICT).map("role").add();
        }
    },
    MTR_READINGQUALITY {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingQualityRecord> table = dataModel.addTable(name(), ReadingQualityRecord.class);
            table.map(ReadingQualityRecordImpl.class);
            table.setJournalTableName("MTR_READINGQUALITYJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column channelColumn = table.column("CHANNELID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column timestampColumn = table.column("READINGTIMESTAMP").type("number").notNull().conversion(NUMBER2INSTANT).map("readingTimestamp").add();
            Column typeColumn = table.column("TYPE").type("varchar(64)").notNull().map("typeCode").add();
            Column readingTypeColumn = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
            table.column("ACTUAL").bool().notNull().map("actual").add();
            table.addAuditColumns();
            table.column("COMMENTS").type("varchar(4000)").map("comment").add();
            table.primaryKey("MTR_PK_READINGQUALITY").on(idColumn).add();
            table.foreignKey("MTR_FK_RQ_CHANNEL").references(MTR_CHANNEL.name()).onDelete(DeleteRule.RESTRICT).map("channel").on(channelColumn).add();
            table.foreignKey("MTR_FK_RQ_READINGTYPE").references(MTR_READINGTYPE.name()).onDelete(DeleteRule.RESTRICT).map("readingType").on(readingTypeColumn).add();
            table.unique("MTR_U_READINGQUALITY").on(channelColumn, timestampColumn, typeColumn, readingTypeColumn).add();
        }
    },
    MTR_ENDDEVICEEVENTTYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceEventType> table = dataModel.addTable(name(), EndDeviceEventType.class);
            table.map(EndDeviceEventTypeImpl.class);
            table.cache();
            Column mRidColumn = table.column("MRID").varChar(NAME_LENGTH).notNull().map("mRID").add();
            table.column("ALIASNAME").varChar(SHORT_DESCRIPTION_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_ENDDEVICEEVENTTYPE").on(mRidColumn).add();
        }
    },
    MTR_ENDDEVICEEVENTRECORD {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceEventRecord> table = dataModel.addTable(name(), EndDeviceEventRecord.class);
            table.map(EndDeviceEventRecordImpl.class);
            Column endDeviceColumn = table.column("ENDDEVICEID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column eventTypeColumn = table.column("EVENTTYPE").varChar(NAME_LENGTH).notNull().add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME").type("number").notNull().conversion(NUMBER2INSTANT).map("createdDateTime").add();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("REASON").varChar(SHORT_DESCRIPTION_LENGTH).map("reason").add();
            table.column("SEVERITY").varChar(NAME_LENGTH).map("severity").add();
            table.column("ISSUERID").varChar(NAME_LENGTH).map("issuerID").add();
            table.column("ISSUERTRACKINGID").varChar(NAME_LENGTH).map("issuerTrackingID").add();
            table.column("STATUSDATETIME").type("number").conversion(NUMBER2INSTANT).map("status.dateTime").add();
            table.column("STATUSREASON").varChar(NAME_LENGTH).map("status.reason").add();
            table.column("STATUSREMARK").varChar(NAME_LENGTH).map("status.remark").add();
            table.column("STATUSVALUE").varChar(NAME_LENGTH).map("status.value").add();
            table.column("PROCESSINGFLAGS").type("number").map("processingFlags").conversion(NUMBER2LONG).add();
            table.column("LOGBOOKID").type("number").map("logBookId").conversion(NUMBER2LONG).add();
            table.column("LOGBOOKPOSITION").type("number").map("logBookPosition").conversion(NUMBER2INT).add();
            table.column("DEVICEEVENTTYPE").varChar(80).map("deviceEventType").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_ENDDEVICEEVENTRECORD").on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn).add();
            table.partitionOn(createdDateTimeColumn);
            table.foreignKey("MTR_FK_EVENT_ENDDEVICE").on(endDeviceColumn).references(MTR_ENDDEVICE.name()).onDelete(DeleteRule.CASCADE).map("endDevice").add();
            table.foreignKey("MTR_FK_EVENT_EVENTTYPE").on(eventTypeColumn).references(TableSpecs.MTR_ENDDEVICEEVENTTYPE.name()).onDelete(DeleteRule.RESTRICT).map("eventType").add();
        }
    },
    MTR_ENDDEVICEEVENTDETAIL {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceEventRecordImpl.EndDeviceEventDetailRecord> table = dataModel.addTable(name(), EndDeviceEventRecordImpl.EndDeviceEventDetailRecord.class);
            table.map(EndDeviceEventRecordImpl.EndDeviceEventDetailRecord.class);
            Column endDeviceColumn = table.column("ENDDEVICEID").type("number").notNull().map("endDeviceId").conversion(NUMBER2LONG).add();
            Column eventTypeColumn = table.column("EVENTTYPE").varChar(NAME_LENGTH).notNull().map("eventTypeCode").add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME").type("number").notNull().conversion(NUMBER2INSTANT).map("createdDateTime").add();
            Column keyColumn = table.column("KEY").varChar(NAME_LENGTH).notNull().map("key").add();
            table.column("DETAIL_VALUE").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("value").add();
            table.primaryKey("MTR_PK_ENDDEVICEEVENTDETAIL").on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn, keyColumn).add();
            table.foreignKey("MTR_FK_ENDDEVICEEVENT_DETAIL").on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn).references(MTR_ENDDEVICEEVENTRECORD.name())
                    .onDelete(DeleteRule.CASCADE).map("eventRecord").reverseMap("detailRecords").composition().refPartition().add();
        }
    },

    MTR_USAGEPOINTDETAIL {
        void addTo(DataModel dataModel) {
            Table<UsagePointDetail> table = dataModel.addTable(name(), UsagePointDetail.class);
            table.map(UsagePointDetailImpl.IMPLEMENTERS);
            table.setJournalTableName("MTR_USAGEPOINTDETAILJRNL");
            Column usagePointIdColumn = table.column("USAGEPOINTID").type("number").notNull().conversion(NUMBER2LONGNULLZERO).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");

            table.addDiscriminatorColumn("SERVICECATEGORY", "varchar2(1)");

            table.column("AMIBILLINGREADY").number().notNull().conversion(NUMBER2ENUM).map("amiBillingReady").add();
            table.column("CHECKBILLING").bool().map("checkBilling").add();
            table.column("CONNECTIONSTATE").number().notNull().conversion(NUMBER2ENUM).map("connectionState").add();
            table.column("MINIMALUSAGEEXPECTED").bool().map("minimalUsageExpected").add();
            table.column("SERVICEDELIVERYREMARK").varChar(NAME_LENGTH).map("serviceDeliveryRemark").add();

            table.column("GROUNDED").type("char(1)").conversion(CHAR2BOOLEAN).map("grounded").add();
            table.addQuantityColumns("NOMINALVOLTAGE", false, "nominalServiceVoltage");
            table.column("PHASECODE").type("varchar2(7)").conversion(CHAR2ENUM).map("phaseCode").add();
            table.addQuantityColumns("RATEDCURRENT", false, "ratedCurrent");
            table.addQuantityColumns("RATEDPOWER", false, "ratedPower");
            table.addQuantityColumns("ESTIMATEDLOAD", false, "estimatedLoad");

            table.addAuditColumns();
            table.primaryKey("MTR_PK_USAGEPOINTDETAIL").on(usagePointIdColumn, intervalColumns.get(0)).add();
            table.foreignKey("MTR_FK_USAGEPOINTDETAILUP").on(usagePointIdColumn).references(MTR_USAGEPOINT.name()).onDelete(RESTRICT).map("usagePoint").reverseMap("detail").composition().add();
        }
    };

    abstract void addTo(DataModel component);
}