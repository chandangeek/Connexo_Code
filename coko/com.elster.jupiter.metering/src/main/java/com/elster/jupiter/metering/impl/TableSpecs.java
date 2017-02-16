/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationMember;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.LocationTemplate.TemplateField;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointConfiguration;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointReadingTypeConfiguration;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.ReadingTypeTemplateAttribute;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.impl.config.AbstractNode;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyConfigurationOnUsagePointImpl;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyContractOnUsagePoint;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyContractOnUsagePointImpl;
import com.elster.jupiter.metering.impl.config.FormulaImpl;
import com.elster.jupiter.metering.impl.config.MeterRoleImpl;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationCustomPropertySetUsage;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationCustomPropertySetUsageImpl;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationImpl;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationMeterRoleUsageImpl;
import com.elster.jupiter.metering.impl.config.MetrologyContractChannelsContainerImpl;
import com.elster.jupiter.metering.impl.config.MetrologyContractImpl;
import com.elster.jupiter.metering.impl.config.MetrologyContractReadingTypeDeliverableUsage;
import com.elster.jupiter.metering.impl.config.MetrologyPurposeImpl;
import com.elster.jupiter.metering.impl.config.PartiallySpecifiedReadingTypeAttributeValueImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeRequirementMeterRoleUsage;
import com.elster.jupiter.metering.impl.config.ReadingTypeTemplateAttributeImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeTemplateAttributeValueImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeTemplateImpl;
import com.elster.jupiter.metering.impl.config.ServiceCategoryMeterRoleUsage;
import com.elster.jupiter.metering.impl.config.UsagePointRequirementImpl;
import com.elster.jupiter.metering.impl.config.UsagePointRequirementValue;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;

import com.google.common.collect.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.CHAR2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUMPLUSONE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INTWRAPPER;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONGNULLZERO;
import static com.elster.jupiter.orm.ColumnConversion.SDOGEOMETRY2SPATIALGEOOBJ;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;


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
            table.column("ACTIVE").type("char(1)").conversion(CHAR2BOOLEAN).map("active").notNull().since(version(10, 2)).installValue("'Y'").add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_SERVICECATEGORY").on(idColumn).add();
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
            table.column("MASTATUSDATETIME")
                    .number()
                    .conversion(NUMBER2INSTANT)
                    .map("mainAddress.status.dateTime")
                    .add();
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
            table.column("SASTATUSDATETIME")
                    .number()
                    .conversion(NUMBER2INSTANT)
                    .map("secondaryAddress.status.dateTime")
                    .add();
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
            table.column("STATUSDATETIME").number().conversion(NUMBER2INSTANT).map("status.dateTime").add();
            table.column("STATUSREASON").varChar(NAME_LENGTH).map("status.reason").add();
            table.column("STATUSREMARK").varChar(NAME_LENGTH).map("status.remark").add();
            table.column("STATUSVALUE").varChar(NAME_LENGTH).map("status.value").add();
            table.column("SERVICELOCATIONTYPE").varChar(NAME_LENGTH).map("type").add();
            table.column("ACCESSMETHOD").varChar(NAME_LENGTH).map("accessMethod").add();
            table.column("NEEDSINSPECTION").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("needsInspection").add();
            table.column("SITEACCESSPROBLEM").varChar(NAME_LENGTH).map("siteAccessProblem").add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_SERVICELOCATION").on(idColumn).add();
            table.unique("MTR_U_SERVICELOCATION").on(mRIDColumn).add();
        }
    },

    MTR_LOCATION_TEMPLATE {
        @Override
        void addTo(DataModel dataModel) {
            Table<LocationTemplate> table = dataModel.addTable(name(), LocationTemplate.class);
            table.since(version(10, 2));
            table.map(LocationTemplateImpl.class);
            table.setJournalTableName("MTR_LOCATIONTEMPLATEJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column templateColumn = table.column("LOCATIONTEMPLATE")
                    .varChar(Table.SHORT_DESCRIPTION_LENGTH)
                    .map("templateFields")
                    .add();
            table.column("MANDATORYFIELDS").varChar(Table.SHORT_DESCRIPTION_LENGTH).map("mandatoryFields").add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_LOCATIONTEMPLATE").on(idColumn).add();
            table.unique("MTR_U_LOCATIONTEMPLATE").on(templateColumn).add();
        }
    },

    MTR_LOCATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<Location> table = dataModel.addTable(name(), Location.class);
            table.since(version(10, 2));
            table.map(LocationImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("PK_MTR_LOCATION").on(idColumn).add();
        }
    },

    MTR_LOCATIONMEMBER {
        @Override
        void addTo(DataModel dataModel) {
            Table<LocationMember> table = dataModel.addTable(name(), LocationMember.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_LOCATIONMEMBER_JRNL");
            table.map(LocationMemberImpl.class);
            TableBuilder.buildLocationMemberTable(table, MeteringServiceImpl.getLocationTemplateMembers());
            table.addAuditColumns();
        }
    },
    MTR_AMRSYSTEM {
        @Override
        void addTo(DataModel dataModel) {
            Table<AmrSystem> table = dataModel.addTable(name(), AmrSystem.class);
            table.map(AmrSystemImpl.class);
            table.cache();
            Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2INT).map("id").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_AMRSYSTEM").on(idColumn).add();
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
            Column mRIDColumn_10_2 = table.column("MRID").varChar(NAME_LENGTH).upTo(version(10, 2, 1)).add();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).notNull().map("mRID").since(version(10, 2, 1)).previously(mRIDColumn_10_2).add();
            Column serviceKindColumn = table.column("SERVICEKIND").number().notNull().conversion(NUMBER2ENUMPLUSONE).add();
            Column serviceLocationIdColumn = table.column("SERVICELOCATIONID").number().conversion(NUMBER2LONGNULLZERO).add();
            Column nameColumn_10_2 = table.column("NAME").varChar(NAME_LENGTH).upTo(version(10, 2, 1)).add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").since(version(10, 2, 1)).previously(nameColumn_10_2).add();
            table.column("SERVICELOCATIONSTRING").varChar(SHORT_DESCRIPTION_LENGTH).map("serviceLocationString").since(version(10, 2)).add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("ISSDP").bool().map("isSdp").add();
            table.column("ISVIRTUAL").bool().map("isVirtual").add();
            table.column("OUTAGEREGION").varChar(NAME_LENGTH).map("outageRegion").add();
            table.column("READCYCLE").varChar(NAME_LENGTH).map("readCycle").upTo(version(10, 2)).add();
            table.column("READROUTE").varChar(NAME_LENGTH).map("readRoute").add();
            table.column("SERVICEPRIORITY").varChar(NAME_LENGTH).map("servicePriority").add();
            table.column("SERVICEDELIVERYREMARK").varChar(SHORT_DESCRIPTION_LENGTH).map("serviceDeliveryRemark").since(version(10, 2)).add();
            table.column("INSTALLATIONTIME")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map("installationTime")
                    .since(version(10, 2))
                    .installValue("0")
                    .add();
            Column locationIdColumn = table.column("LOCATIONID")
                    .number()
                    .conversion(NUMBER2LONGNULLZERO)
                    .map("location")
                    .since(version(10, 2))
                    .add();
            table.column("GEOCOORDINATES").sdoGeometry().conversion(SDOGEOMETRY2SPATIALGEOOBJ).map("spatialCoordinates").since(version(10, 2)).add();
            Column obsoleteTime = table.column("OBSOLETETIME").number().map("obsoleteTime").conversion(ColumnConversion.NUMBER2INSTANT).since(version(10, 3)).add();
            table.addAuditColumns();

            table.primaryKey("PK_MTR_USAGEPOINT").on(idColumn).add();
            table.unique("MTR_U_USAGEPOINT").on(mRIDColumn).add();
            table.unique("MTR_U_USAGEPOINTNAME").on(nameColumn).during(Range.closedOpen(version(10, 2, 1), version(10, 3))).add();
            table.unique("MTR_U_USAGEPOINTNAME").on(nameColumn, obsoleteTime).since(version(10, 3)).add();
            table.foreignKey("FK_MTR_USAGEPOINTSERVICECAT")
                    .on(serviceKindColumn)
                    .references(ServiceCategory.class)
                    .onDelete(RESTRICT)
                    .map("serviceCategory")
                    .add();
            table.foreignKey("FK_MTR_USAGEPOINTSERVICELOC")
                    .on(serviceLocationIdColumn)
                    .references(ServiceLocation.class)
                    .onDelete(RESTRICT)
                    .map("serviceLocation")
                    .reverseMap("usagePoints")
                    .add();
            table.foreignKey("FK_MTR_USAGEPOINTLOCATION")
                    .on(locationIdColumn)
                    .references(Location.class)
                    .onDelete(RESTRICT)
                    .map("upLocation", LocationMember.class)
                    .since(version(10, 2))
                    .add();
        }
    },
    MTR_READINGTYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<IReadingType> table = dataModel.addTable(name(), IReadingType.class);
            table.map(ReadingTypeImpl.class).alsoReferredToAs(ReadingType.class);
            table.setJournalTableName("MTR_READINGTYPE_JNRL").since(version(10, 2));
            table.cache();
            Column mRidColumn = table.column("MRID").varChar(NAME_LENGTH).notNull().map("mRID").add();
            table.column("ALIASNAME").varChar(SHORT_DESCRIPTION_LENGTH).map("aliasName").add();
            table.column("FULLALIASNAME").varChar(SHORT_DESCRIPTION_LENGTH).map("fullAliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("ACTIVE").type("char(1)").conversion(CHAR2BOOLEAN).map("active").add();
            Column equidistantColumn = table.column("EQUIDISTANT").bool().map("equidistant").add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_READINGTYPE").on(mRidColumn).add();
            table.index("MTR_READINGTYPE_EQUIDISTANT").on(equidistantColumn).add();
        }
    },
    MTR_ENDDEVICE {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDevice> table = dataModel.addTable(name(), EndDevice.class);
            table.map(EndDeviceImpl.IMPLEMENTERS);
            table.setJournalTableName("MTR_ENDDEVICEJRNL").since(version(10, 2));
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("ENDDEVICETYPE", "char(1)");
            Column mRIDColumn_10_2 = table.column("MRID").varChar(NAME_LENGTH).upTo(version(10, 2, 1)).add();
            Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).notNull().map("mRID").since(version(10, 2, 1)).previously(mRIDColumn_10_2).add();
            Column amrSystemIdColumn = table.column("AMRSYSTEMID").number().notNull().conversion(NUMBER2INT).map("amrSystemId").add();
            Column amrIdColumn = table.column("AMRID").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("amrId").add();
            Column nameColumn_10_2 = table.column("NAME").varChar(NAME_LENGTH).upTo(version(10, 2, 1)).add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").since(version(10, 2, 1)).previously(nameColumn_10_2).add();
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
            Column obsoleteTime = table.column("OBSOLETETIME").number().map("obsoleteTime").conversion(ColumnConversion.NUMBER2INSTANT).add();
            Column stateMachine = table.column("FSM").number().conversion(ColumnConversion.NUMBER2LONG).add();
            Column locationIdColumn = table.column("LOCATIONID").number().conversion(NUMBER2LONGNULLZERO).since(version(10, 2)).add();
            table.column("GEOCOORDINATES").sdoGeometry().conversion(SDOGEOMETRY2SPATIALGEOOBJ).map("spatialCoordinates").since(version(10, 2)).add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_METER").on(idColumn).add();
            table.foreignKey("FK_MTR_METERAMRSYSTEM")
                    .references(MTR_AMRSYSTEM.name())
                    .onDelete(RESTRICT)
                    .map("amrSystem")
                    .on(amrSystemIdColumn)
                    .add();
            table.foreignKey("FK_MTR_ENDDEVICE_FSM")
                    .on(stateMachine)
                    .references(FiniteStateMachine.class)
                    .map("stateMachine")
                    .add();
            table.foreignKey("FK_MTR_ENDDEVICELOCATION")
                    .on(locationIdColumn)
                    .references(Location.class)
                    .onDelete(RESTRICT)
                    .map("location", LocationMember.class)
                    .since(version(10, 2))
                    .add();
            table.unique("MTR_U_METER").on(mRIDColumn_10_2, obsoleteTime).upTo(version(10, 2, 1)).add();
            table.unique("MTR_U_METER").on(mRIDColumn).since(version(10, 2, 1)).add();
            table.unique("MTR_U_METERAMR").on(amrSystemIdColumn, amrIdColumn).add();
            table.index("MTR_IDX_ENDDEVICE_NAME").on(nameColumn).add();
            table.unique("UK_MTR_ENDDEVICE_NAME").on(nameColumn, obsoleteTime).since(version(10, 2, 1)).add();
        }
    },
    MTR_ENDDEVICESTATUS {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceLifeCycleStatus> table = dataModel.addTable(name(), EndDeviceLifeCycleStatus.class);
            table.map(EndDeviceLifeCycleStatusImpl.class);
            Column endDevice = table.column("ENDDEVICE").notNull().number().conversion(ColumnConversion.NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addAuditColumns();
            Column state = table.column("STATE").notNull().number().add();
            table.primaryKey("PK_MTR_ENDDEVICESTATUS").on(endDevice, intervalColumns.get(0)).add();
            table.foreignKey("FK_MTR_STATUS_ENDDEVICE")
                    .on(endDevice)
                    .references(EndDevice.class)
                    .onDelete(CASCADE)
                    .map("endDevice")
                    .reverseMap("status")
                    .composition()
                    .add();
            table.foreignKey("FK_MTR_STATUS_STATE")
                    .on(state)
                    .references(State.class)
                    .onDelete(RESTRICT)
                    .map("state")
                    .add();
        }
    },
    MTR_METERROLE {
        @Override
        void addTo(DataModel dataModel) {
            Table<MeterRole> table = dataModel.addTable(name(), MeterRole.class);
            table.map(MeterRoleImpl.class);
            table.setJournalTableName("MTR_METERROLE_JRNL").since(version(10, 2));
            Column nameColumn = table.column(MeterRoleImpl.Fields.KEY.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(MeterRoleImpl.Fields.KEY.fieldName())
                    .add();

            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.primaryKey("PK_MTR_METERROLE").on(nameColumn).add();
        }
    },
    MTR_METERACTIVATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<MeterActivation> table = dataModel.addTable(name(), MeterActivation.class);
            table.map(MeterActivationImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column usagePointIdColumn = table.column("USAGEPOINTID")
                    .number()
                    .conversion(NUMBER2LONGNULLZERO)
                    .add();
            Column meterIdColumn = table.column("METERID")
                    .number()
                    .conversion(NUMBER2LONGNULLZERO)
                    .add();
            Column meterRoleIdColumn = table.column("METERROLE")
                    .varChar(Table.NAME_LENGTH)
                    .since(version(10, 2))
                    .add();
            table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.primaryKey("PK_MTR_METERACTIVATION").on(idColumn).add();
            table.foreignKey("FK_MTR_METERACTUSAGEPOINT")
                    .references(UsagePoint.class)
                    .onDelete(RESTRICT)
                    .map("usagePoint")
                    .reverseMap("meterActivations", EndDevice.class)
                    .reverseMapOrder("interval.start")
                    .reverseMapCurrent("currentMeterActivation")
                    .on(usagePointIdColumn)
                    .add();
            table.foreignKey("FK_MTR_METERACTMETER")
                    .references(EndDevice.class)
                    .onDelete(RESTRICT)
                    .map("meter")
                    .reverseMap("meterActivations", UsagePoint.class)
                    .reverseMapOrder("interval.start")
                    .reverseMapCurrent("currentMeterActivation")
                    .on(meterIdColumn)
                    .add();
            table.foreignKey("FK_MTR_METER_ACT_2_ROLE")
                    .references(MeterRole.class)
                    .onDelete(RESTRICT)
                    .map("meterRole")
                    .on(meterRoleIdColumn)
                    .add();
        }
    },
    MTR_UPACCOUNTABILITY {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointAccountability> table = dataModel.addTable(name(), UsagePointAccountability.class);
            table.map(UsagePointAccountabilityImpl.class);
            table.setJournalTableName("MTR_UPACCOUNTABILITYJRNL");
            Column usagePointIdColumn = table.column("USAGEPOINTID")
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONG)
                    .add();
            Column partyIdColumn = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
            Column roleMRIDColumn = table.column("ROLEMRID").varChar(NAME_LENGTH).notNull().add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.primaryKey("PK_MTR_UPACCOUNTABILITY")
                    .on(usagePointIdColumn, partyIdColumn, roleMRIDColumn, intervalColumns.get(0))
                    .add();
            table.foreignKey("FK_MTR_UPACCOUNTUP")
                    .on(usagePointIdColumn)
                    .references(MTR_USAGEPOINT.name())
                    .onDelete(RESTRICT).map("usagePoint")
                    .reverseMap("accountabilities")
                    .composition()
                    .add();
            table.foreignKey("FK_MTR_UPACCOUNTPARTY")
                    .on(partyIdColumn)
                    .references(Party.class)
                    .onDelete(RESTRICT)
                    .map("party")
                    .add();
            table.foreignKey("FK_MTR_UPACCOUNTPARTYROLE")
                    .on(roleMRIDColumn)
                    .references(PartyRole.class)
                    .onDelete(RESTRICT)
                    .map("role")
                    .add();
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
            table.primaryKey("PK_MTR_ENDDEVICEEVENTTYPE").on(mRidColumn).add();
        }
    },
    MTR_ENDDEVICEEVENTRECORD {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceEventRecord> table = dataModel.addTable(name(), EndDeviceEventRecord.class);
            table.map(EndDeviceEventRecordImpl.class);
            Column endDeviceColumn = table.column("ENDDEVICEID").number().notNull().conversion(NUMBER2LONG).add();
            Column eventTypeColumn = table.column("EVENTTYPE").varChar(NAME_LENGTH).notNull().add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME")
                    .number()
                    .notNull()
                    .conversion(NUMBER2INSTANT)
                    .map("createdDateTime")
                    .add();
            table.column("NAME").varChar(NAME_LENGTH).map("name").add();
            table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
            table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.column("REASON").varChar(SHORT_DESCRIPTION_LENGTH).map("reason").add();
            table.column("SEVERITY").varChar(NAME_LENGTH).map("severity").add();
            table.column("ISSUERID").varChar(NAME_LENGTH).map("issuerID").add();
            table.column("ISSUERTRACKINGID").varChar(NAME_LENGTH).map("issuerTrackingID").add();
            table.column("STATUSDATETIME").number().conversion(NUMBER2INSTANT).map("status.dateTime").add();
            table.column("STATUSREASON").varChar(NAME_LENGTH).map("status.reason").add();
            table.column("STATUSREMARK").varChar(NAME_LENGTH).map("status.remark").add();
            table.column("STATUSVALUE").varChar(NAME_LENGTH).map("status.value").add();
            table.column("PROCESSINGFLAGS").number().map("processingFlags").conversion(NUMBER2LONG).add();
            table.column("LOGBOOKID").number().map("logBookId").conversion(NUMBER2LONG).add();
            table.column("LOGBOOKPOSITION").number().map("logBookPosition").conversion(NUMBER2INT).add();
            table.column("DEVICEEVENTTYPE").varChar(80).map("deviceEventType").add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_ENDDEVICEEVENTRECORD")
                    .on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn)
                    .add();
            table.partitionOn(createdDateTimeColumn);
            table.foreignKey("FK_MTR_EVENT_ENDDEVICE")
                    .on(endDeviceColumn)
                    .references(EndDevice.class)
                    .onDelete(DeleteRule.CASCADE)
                    .map("endDevice")
                    .add();
            table.foreignKey("FK_MTR_EVENT_EVENTTYPE")
                    .on(eventTypeColumn)
                    .references(EndDeviceEventType.class)
                    .onDelete(DeleteRule.RESTRICT)
                    .map("eventType")
                    .add();
        }
    },
    MTR_ENDDEVICEEVENTDETAIL {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceEventRecordImpl.EndDeviceEventDetailRecord> table = dataModel.addTable(name(), EndDeviceEventRecordImpl.EndDeviceEventDetailRecord.class);
            table.map(EndDeviceEventRecordImpl.EndDeviceEventDetailRecord.class);
            Column endDeviceColumn = table.column("ENDDEVICEID")
                    .number()
                    .notNull()
                    .map("endDeviceId")
                    .conversion(NUMBER2LONG)
                    .add();
            Column eventTypeColumn = table.column("EVENTTYPE")
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map("eventTypeCode")
                    .add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME")
                    .number()
                    .notNull()
                    .conversion(NUMBER2INSTANT)
                    .map("createdDateTime")
                    .add();
            Column keyColumn = table.column("KEY").varChar(NAME_LENGTH).notNull().map("key").add();
            table.column("DETAIL_VALUE").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("value").add();
            table.primaryKey("PK_MTR_ENDDEVICEEVENTDETAIL")
                    .on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn, keyColumn)
                    .add();
            table.foreignKey("FK_MTR_ENDDEVICEEVENT_DETAIL")
                    .on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn)
                    .references(EndDeviceEventRecord.class)
                    .onDelete(DeleteRule.CASCADE)
                    .map("eventRecord")
                    .reverseMap("detailRecords")
                    .composition()
                    .refPartition()
                    .add();
        }
    },
    MTR_USAGEPOINTDETAIL {
        void addTo(DataModel dataModel) {
            Table<UsagePointDetail> table = dataModel.addTable(name(), UsagePointDetail.class);
            table.map(UsagePointDetailImpl.IMPLEMENTERS);
            table.setJournalTableName("MTR_USAGEPOINTDETAILJRNL");
            Column usagePointIdColumn = table.column("USAGEPOINTID")
                    .number()
                    .notNull()
                    .conversion(NUMBER2LONGNULLZERO)
                    .add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");

            table.addDiscriminatorColumn("SERVICECATEGORY", "varchar2(1 char)");

            table.column("AMIBILLINGREADY").number().notNull().conversion(NUMBER2ENUM).map("amiBillingReady").upTo(version(10, 2)).add();
            table.column("CHECKBILLING").bool().map("checkBilling").upTo(version(10, 2)).add();
            table.column("CONNECTIONSTATE").number().notNull().conversion(NUMBER2ENUM).map("connectionState").upTo(version(10, 2)).add();
            table.column("MINIMALUSAGEEXPECTED").bool().map("minimalUsageExpected").upTo(version(10, 2)).add();
            table.column("SERVICEDELIVERYREMARK").varChar(NAME_LENGTH).map("serviceDeliveryRemark").upTo(version(10, 2)).add();

            Column groundedColumn1 = table.column("GROUNDED").type("char(1)").conversion(CHAR2BOOLEAN).map("grounded").upTo(version(10, 2)).add();
            table.column("GROUNDED").varChar(7).conversion(CHAR2ENUM).map("grounded").since(version(10, 2)).previously(groundedColumn1).installValue("'Y'").add();
            table.addQuantityColumns("NOMINALVOLTAGE", false, "nominalServiceVoltage", Range.atLeast(version(10, 2)));
            table.column("PHASECODE").varChar(7).conversion(CHAR2ENUM).map("phaseCode").add();
            table.addQuantityColumns("PHYSICALCAPACITY", false, "physicalCapacity", Range.atLeast(version(10, 2)));
            table.addQuantityColumns("RATEDCURRENT", false, "ratedCurrent");
            table.addQuantityColumns("RATEDPOWER", false, "ratedPower");
            table.addQuantityColumns("ESTIMATEDLOAD", false, "estimatedLoad");
            table.column("LIMITER").varChar(7).conversion(CHAR2ENUM).map("limiter").since(version(10, 2)).add();
            table.column("LOADLIMITERTYPE").varChar(NAME_LENGTH).map("loadLimiterType").since(version(10, 2)).add();
            table.addQuantityColumns("LOADLIMIT", false, "loadLimit", Range.atLeast(version(10, 2)));
            table.addQuantityColumns("PRESSURE", false, "pressure", Range.atLeast(version(10, 2)));
            table.column("INTERRUPTIBLE").varChar(7).conversion(CHAR2ENUM).map("interruptible").since(version(10, 2)).add();
            table.column("COLLAR").varChar(7).conversion(CHAR2ENUM).map("collar").since(version(10, 2)).add();
            table.column("BYPASS").varChar(7).conversion(CHAR2ENUM).map("bypass").since(version(10, 2)).add();
            table.column("BYPASSSTATUS").varChar(7).conversion(CHAR2ENUM).map("bypassStatus").since(version(10, 2)).add();
            table.column("VALVE").varChar(7).conversion(CHAR2ENUM).map("valve").since(version(10, 2)).add();
            table.column("CAPPED").varChar(7).conversion(CHAR2ENUM).map("capped").since(version(10, 2)).add();
            table.column("CLAMPED").varChar(7).conversion(CHAR2ENUM).map("clamped").since(version(10, 2)).add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_USAGEPOINTDETAIL").on(usagePointIdColumn, intervalColumns.get(0)).add();
            table.foreignKey("FK_MTR_USAGEPOINTDETAILUP")
                    .on(usagePointIdColumn)
                    .references(UsagePoint.class)
                    .onDelete(RESTRICT)
                    .map("usagePoint")
                    .reverseMap("detail")
                    .composition()
                    .add();
        }
    },
    MTR_USAGEPOINTSTATE {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointConnectionState> table = dataModel.addTable(name(), UsagePointConnectionState.class);
            table.map(UsagePointConnectionStateImpl.class);
            Column usagePoint = table.column("USAGEPOINT").notNull().number().conversion(ColumnConversion.NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.column("CONNECTIONSTATE").varChar(30).conversion(CHAR2ENUM).map("connectionState").since(version(10, 2)).add();
            table.primaryKey("PK_MTR_USAGEPOINTSTATE").on(usagePoint, intervalColumns.get(0)).add();
            table.foreignKey("FK_MTR_USAGEPOINTSTATE")
                    .on(usagePoint)
                    .references(UsagePoint.class)
                    .onDelete(CASCADE)
                    .map("usagePoint")
                    .reverseMap("connectionState")
                    .composition()
                    .add();
        }
    },
    MTR_METROLOGYCONFIG {
        void addTo(DataModel dataModel) {
            Table<MetrologyConfiguration> table = dataModel.addTable(name(), MetrologyConfiguration.class);
            table.map(MetrologyConfigurationImpl.IMPLEMENTERS);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_METROLOGYCONFIG_JRNL");
            table.cache();
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("CONFIG_TYPE", "char(1)");
            Column name = table.column(MetrologyConfigurationImpl.Fields.NAME.name())
                    .varChar()
                    .notNull()
                    .map(MetrologyConfigurationImpl.Fields.NAME.fieldName())
                    .add();
            table.column(MetrologyConfigurationImpl.Fields.DESCRIPTION.name())
                    .varChar()
                    .map(MetrologyConfigurationImpl.Fields.DESCRIPTION.fieldName())
                    .add();
            table.column(MetrologyConfigurationImpl.Fields.STATUS.name())
                    .number()
                    .conversion(NUMBER2ENUM)
                    .map(MetrologyConfigurationImpl.Fields.STATUS.fieldName())
                    .notNull()
                    .add();
            Column serviceCategoryColumn = table.column(MetrologyConfigurationImpl.Fields.SERVICECATEGORY.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2ENUMPLUSONE)
                    .add();
            Column obsoleteTime = table.column(MetrologyConfigurationImpl.Fields.OBSOLETETIME.name())
                    .number()
                    .map(MetrologyConfigurationImpl.Fields.OBSOLETETIME.fieldName())
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .since(version(10, 3))
                    .add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_METROLOGYCONFIG").on(id).add();
            table.foreignKey("FK_MTR_METROLOGYCONFIG2SERVCAT")
                    .references(MTR_SERVICECATEGORY.name())
                    .on(serviceCategoryColumn)
                    .map(MetrologyConfigurationImpl.Fields.SERVICECATEGORY.fieldName())
                    .add();
            table.unique("UK_MTR_METROLOGYCONFIGURATION").on(name).upTo(version(10, 3)).add();
            table.unique("UK_MTR_METROLOGYCONFIGURATION").on(name, obsoleteTime).since(version(10, 3)).add();
        }
    },
    MTR_M_CONFIG_CPS_USAGES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<MetrologyConfigurationCustomPropertySetUsage> table = dataModel.addTable(name(), MetrologyConfigurationCustomPropertySetUsage.class);
            table.since(version(10, 2));
            table.map(MetrologyConfigurationCustomPropertySetUsageImpl.class);
            table.setJournalTableName("MTR_M_CONFIG_CPS_USAGES_JRNL");
            Column metrologyConfig = table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.METROLOGY_CONFIG.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column customPropertySet = table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.CUSTOM_PROPERTY_SET.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            table.column(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2INT)
                    .map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.fieldName())
                    .add();
            table.addAuditColumns();
            table.primaryKey("PK_M_CONFIG_CPS_USAGE").on(metrologyConfig, customPropertySet).add();
            table.foreignKey("FK_MCPS_USAGE_TO_CONFIG")
                    .references(MTR_METROLOGYCONFIG.name())
                    .on(metrologyConfig)
                    .map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.METROLOGY_CONFIG.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.CUSTOM_PROPERTY_SETS.fieldName())
                    .reverseMapOrder(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.POSITION.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_MCAS_USAGE_TO_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .map(MetrologyConfigurationCustomPropertySetUsageImpl.Fields.CUSTOM_PROPERTY_SET.fieldName())
                    .add();
        }
    },
    MTR_USAGEPOINTMTRCONFIG {
        void addTo(DataModel dataModel) {
            Table<EffectiveMetrologyConfigurationOnUsagePoint> table = dataModel.addTable(name(), EffectiveMetrologyConfigurationOnUsagePoint.class);
            table.map(EffectiveMetrologyConfigurationOnUsagePointImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_USAGEPOINTMTRCONFIG_JRNL");
            Column id = table.addAutoIdColumn();
            Column usagePoint = table.column("USAGEPOINT").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column metrologyConfiguration = table.column("METROLOGYCONFIG").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
            table.addIntervalColumns("interval");
            table.addAuditColumns();
            table.primaryKey("PK_MTR_UPMTRCONFIG").on(id).add();
            table.foreignKey("FK_MTR_UPMTRCONFIG_UP")
                    .on(usagePoint)
                    .references(UsagePoint.class)
                    .map("usagePoint")
                    .reverseMap("metrologyConfigurations")
                    .composition()
                    .add();
            table.foreignKey("FK_MTR_UPMTRCONFIG_MC")
                    .on(metrologyConfiguration)
                    .references(UsagePointMetrologyConfiguration.class)
                    .map("metrologyConfiguration")
                    .add();
        }
    },
    MTR_MULTIPLIERTYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<MultiplierType> table = dataModel.addTable(name(), MultiplierType.class);
            table.map(MultiplierTypeImpl.class);

            table.cache();
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map("name").add();
            Column nameIsKey = table.column("NAMEISKEY").bool().notNull().map("nameIsKey").installValue("'Y'").since(version(10, 2)).add();
            table.primaryKey("PK_MTR_MULTIPLIERTYPE").on(id).add();
            table.unique("UK_MTR_MULTTYPE_NAME").on(name, nameIsKey).add();
        }
    },
    MTR_MULTIPLIERVALUE {
        @Override
        void addTo(DataModel dataModel) {
            Table<MultiplierValue> table = dataModel.addTable(name(), MultiplierValue.class);
            table.map(MultiplierValueImpl.class);
            table.setJournalTableName("MTR_MULTIPLIERVALUE_JRNL").since(version(10, 2));

            Column meterActivationIdColumn = table.column("METERACTIVATIONID").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column typeColumn = table.column("MULTIPLIERTYPE").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.column("VALUE").number().map("value").notNull().add();

            table.addAuditColumns().forEach(column -> column.since(version(10, 2)));
            table.primaryKey("PK_MTR_MULTIPLIERVALUE").on(meterActivationIdColumn, typeColumn).add();
            table.foreignKey("FK_MTR_MULTIPLIERVALUE_MA")
                    .on(meterActivationIdColumn)
                    .references(MeterActivation.class)
                    .map("meterActivation")
                    .composition()
                    .reverseMap("multipliers")
                    .add();
            table.foreignKey("FK_MTR_MULTIPLIERVALUE_TP")
                    .on(typeColumn)
                    .references(MultiplierType.class)
                    .map("type")
                    .add();
        }
    },
    MTR_METER_CONFIG {
        @Override
        void addTo(DataModel dataModel) {
            Table<MeterConfiguration> table = dataModel.addTable(name(), MeterConfiguration.class);
            table.map(MeterConfigurationImpl.class);

            table.setJournalTableName(name() + Constants.JOURNAL_TABLE_SUFFIX).upTo(version(10, 2));
            Column idColumn = table.addAutoIdColumn();
            Column meterIdColumn = table.column("METERID").number().conversion(NUMBER2LONG).add();
            table.addIntervalColumns("interval");
            table.addAuditColumns();

            table.primaryKey("PK_MTR_METER_CONFIG").on(idColumn).add();
            table.foreignKey("FK_MTR_METER_CONFIG")
                    .references(EndDevice.class)
                    .composition()
                    .onDelete(RESTRICT)
                    .map("meter")
                    .reverseMap("meterConfigurations")
                    .reverseMapOrder("interval.start")
                    .on(meterIdColumn).add();

        }
    },
    MTR_RT_METER_CONFIG {
        @Override
        void addTo(DataModel dataModel) {
            Table<MeterReadingTypeConfiguration> table = dataModel.addTable(name(), MeterReadingTypeConfiguration.class);
            table.map(MeterReadingTypeConfigurationImpl.class);

            table.setJournalTableName(name() + Constants.JOURNAL_TABLE_SUFFIX);
            Column meterConfig = table.column("METER_CONFIG").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column measured = table.column("MEASURED").varChar(NAME_LENGTH).notNull().add();
            Column calculated = table.column("CALCULATED").varChar(NAME_LENGTH).add();
            Column multiplierType = table.column("MULTIPLIERTYPE").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.addAuditColumns();
            table.column("OVERFLOW").number().map("overflowValue").add();
            table.column("FRACTIONDIGITS").number().conversion(ColumnConversion.NUMBER2INTWRAPPER).map("numberOfFractionDigits").add();

            table.primaryKey("PK_MTR_RT_METER_CONFIG").on(meterConfig, measured).add();
            table.foreignKey("FK_MTR_RTMC_METER_CONFIG")
                    .references(MeterConfiguration.class)
                    .on(meterConfig)
                    .composition()
                    .map("meterConfiguration")
                    .reverseMap("readingTypeConfigs")
                    .add();
            table.foreignKey("FK_MTR_RTMC_MEASUREDRT")
                    .references(ReadingType.class)
                    .on(measured)
                    .map("measured")
                    .add();
            table.foreignKey("FK_MTR_RTMC_CALCULATEDRT")
                    .references(ReadingType.class)
                    .on(calculated)
                    .map("calculated")
                    .add();
            table.foreignKey("FK_MTR_RTMC_MULTTP")
                    .references(MultiplierType.class)
                    .on(multiplierType)
                    .map("multiplierType")
                    .add();
        }
    },
    MTR_UP_CONFIG {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointConfiguration> table = dataModel.addTable(name(), UsagePointConfiguration.class);
            table.map(UsagePointConfigurationImpl.class);

            table.setJournalTableName(name() + Constants.JOURNAL_TABLE_SUFFIX).upTo(version(10, 2));
            Column idColumn = table.addAutoIdColumn();
            Column usagePointIdColumn = table.column("USAGEPOINTID").number().conversion(NUMBER2LONG).add();
            table.addIntervalColumns("interval");
            table.addAuditColumns();

            table.primaryKey("PK_MTR_UP_CONFIG").on(idColumn).add();
            table.foreignKey("FK_MTR_UP_CONFIG")
                    .references(UsagePoint.class)
                    .composition()
                    .onDelete(RESTRICT)
                    .map("usagePoint")
                    .reverseMap("usagePointConfigurations")
                    .reverseMapOrder("interval.start")
                    .on(usagePointIdColumn).add();

        }
    },
    MTR_RT_UP_CONFIG {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointReadingTypeConfiguration> table = dataModel.addTable(name(), UsagePointReadingTypeConfiguration.class);
            table.map(UsagePointReadingTypeConfigurationImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_RT_UP_CONFIG_JRNL");

            table.setJournalTableName(name() + Constants.JOURNAL_TABLE_SUFFIX);
            Column usagePointConfig = table.column("USAGEPOINT_CONFIG").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            Column measured = table.column("MEASURED").varChar(NAME_LENGTH).notNull().add();
            Column calculated = table.column("CALCULATED").varChar(NAME_LENGTH).add();
            Column multiplierType = table.column("MULTIPLIERTYPE").number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.addAuditColumns();

            table.primaryKey("PK_MTR_RT_UP_CONFIG").on(usagePointConfig, measured).add();
            table.foreignKey("FK_MTR_RTUPC_UP_CONFIG")
                    .references(UsagePointConfiguration.class)
                    .on(usagePointConfig)
                    .composition()
                    .map("usagePointConfiguration")
                    .reverseMap("readingTypeConfigs")
                    .add();
            table.foreignKey("FK_MTR_RTUPC_MEASUREDRT")
                    .references(ReadingType.class)
                    .on(measured)
                    .map("measured")
                    .add();
            table.foreignKey("FK_MTR_RTUPC_CALCULATEDRT")
                    .references(ReadingType.class)
                    .on(calculated)
                    .map("calculated")
                    .add();
            table.foreignKey("FK_MTR_RTUPC_MULTTP")
                    .references(MultiplierType.class)
                    .on(multiplierType)
                    .map("multiplierType")
                    .add();
        }
    },

    MTR_SERVICECATEGORY_CPS_USAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ServiceCategoryCustomPropertySetUsage> table = dataModel.addTable(name(), ServiceCategoryCustomPropertySetUsage.class);
            table.since(version(10, 2));
            table.map(ServiceCategoryCustomPropertySetUsage.class);
            table.setJournalTableName("MTR_SERVCAT_CPS_USAGE_JRNL");
            Column serviceCategory = table.column("SERVICECATEGORY")
                    .number()
                    .notNull()
                    .conversion(NUMBER2ENUMPLUSONE)
                    .add();
            Column customPropertySet = table.column("CUSTOMPROPERTYSET").number().conversion(ColumnConversion.NUMBER2LONG).notNull().add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_CPSSERVICECATUSAGE").on(serviceCategory, customPropertySet).add();
            table.column(ServiceCategoryCustomPropertySetUsage.Fields.POSITION.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2INT)
                    .map(ServiceCategoryCustomPropertySetUsage.Fields.POSITION.fieldName())
                    .add();
            table.foreignKey("FK_MTR_CPSSERVICECATEGORY")
                    .references(MTR_SERVICECATEGORY.name())
                    .on(serviceCategory)
                    .map(ServiceCategoryCustomPropertySetUsage.Fields.SERVICECATEGORY.fieldName())
                    .reverseMap(ServiceCategoryImpl.Fields.CUSTOMPROPERTYSETUSAGE.fieldName())
                    .reverseMapOrder(ServiceCategoryCustomPropertySetUsage.Fields.POSITION.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_MTR_CPSSERVICECATEGORY_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .map(ServiceCategoryCustomPropertySetUsage.Fields.CUSTOMPROPERTYSET.fieldName())
                    .add();
        }
    },
    MTR_FORMULA_NODE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ExpressionNode> table = dataModel.addTable(name(), ExpressionNode.class);
            table.map(AbstractNode.IMPLEMENTERS);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_FORMULA_NODE_JRNL");
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("NODETYPE", "char(3)");

            // parent node
            Column parentColumn = table.column("PARENTID").number().conversion(NUMBER2LONG).add();
            table.column("ARGUMENTINDEX").number().notNull().map("argumentIndex").conversion(NUMBER2INT).add();

            //OperationNodeImpl operator value
            table.column("OPERATOR").number().conversion(ColumnConversion.NUMBER2ENUM).map("operator").add();

            //FunctionCallNodeImpl function value
            table.column("FUNCTION").number().conversion(ColumnConversion.NUMBER2ENUM).map("function").add();

            //FunctionCallNodeImpl function value
            table.column("AGGLEVEL").number().conversion(ColumnConversion.NUMBER2ENUM).map("aggregationLevel").add();

            //ConstantNodeImpl constantValue
            table.column("CONSTANTVALUE").number().map("constantValue").add();

            // ReadingTypeDeliverableNodeImpl readingTypeDeliverable value
            table.column("READINGTYPE_DELIVERABLE").number().conversion(ColumnConversion.NUMBER2LONG).add();

            // ReadingTypeRequirementNodeImpl readingTypeRequirement value
            table.column("READINGTYPE_REQUIREMENT").number().conversion(ColumnConversion.NUMBER2LONG).add();

            // CustomPropertyNodeImpl
            table.column("PROPERTY_SPEC_NAME").map("propertySpecName").varChar().add();
            Column customPropertySet = table.column("CUSTOM_PROPERTY_SET").number().conversion(ColumnConversion.NUMBER2LONG).add();

            table.addAuditColumns();

            table.primaryKey("PK_MTR_FORMULA_NODE").on(idColumn).add();
            table.foreignKey("MTR_FORMULANODE_CPS")
                    .references(RegisteredCustomPropertySet.class)
                    .on(customPropertySet)
                    .map("customPropertySet")
                    .add();
            table.foreignKey("MTR_VALIDCHILD")
                    .references(MTR_FORMULA_NODE.name())
                    .on(parentColumn)
                    .map("parent")
                    .reverseMap("children")
                    .reverseMapOrder("argumentIndex")
                    .add();
        }
    },
    MTR_FORMULA {
        @Override
        void addTo(DataModel dataModel) {
            Table<Formula> table = dataModel.addTable(name(), Formula.class);
            table.map(FormulaImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_FORMULA_JRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("FORMULA_MODE").number().conversion(ColumnConversion.NUMBER2ENUM).map("mode").add();
            Column expressionNodeColumn = table.column("EXPRESSION_NODE_ID").number().conversion(NUMBER2LONG).add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_FORMULA").on(idColumn).add();
            table.foreignKey("MTR_VALIDNODE").references(MTR_FORMULA_NODE.name()).on(expressionNodeColumn)
                    .map("expressionNode").add();
        }
    },
    MTR_SERVICECAT_METERROLE_USAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ServiceCategoryMeterRoleUsage> table = dataModel.addTable(name(), ServiceCategoryMeterRoleUsage.class);
            table.map(ServiceCategoryMeterRoleUsage.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_SERVCAT_MTRROLE_USAGE_JRNL");

            Column serviceCategory = table.column(ServiceCategoryMeterRoleUsage.Fields.SERVICECATEGORY.name()).number().notNull().conversion(NUMBER2ENUMPLUSONE).add();
            Column meterRole = table.column(ServiceCategoryMeterRoleUsage.Fields.METERROLE.name()).varChar(NAME_LENGTH).notNull().add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_SERVCATMTRROLEUSE").on(serviceCategory, meterRole).add();
            table.foreignKey("FK_MTR_SERVCATMETERROLE2CAT")
                    .references(MTR_SERVICECATEGORY.name())
                    .on(serviceCategory)
                    .map(ServiceCategoryMeterRoleUsage.Fields.SERVICECATEGORY.fieldName())
                    .reverseMap(ServiceCategoryImpl.Fields.METERROLEUSAGE.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_MTR_SERVCATMETERROLE2ROLE")
                    .references(MeterRole.class)
                    .on(meterRole)
                    .map(ServiceCategoryMeterRoleUsage.Fields.METERROLE.fieldName())
                    .add();
        }
    },
    MTR_M_CONFIG_ROLE_USAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<MetrologyConfigurationMeterRoleUsageImpl> table = dataModel.addTable(name(), MetrologyConfigurationMeterRoleUsageImpl.class);
            table.map(MetrologyConfigurationMeterRoleUsageImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_M_CONFIG_ROLE_USAGE_JRNL");

            Column metrologyConfigColumn = table.column(MetrologyConfigurationMeterRoleUsageImpl.Fields.METROLOGY_CONFIGURATION.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column meterRoleColumn = table.column(MetrologyConfigurationMeterRoleUsageImpl.Fields.METER_ROLE.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_CONF_ROLE_USAGE").on(metrologyConfigColumn, meterRoleColumn).add();
            table.foreignKey("FK_USAGE_MCMR_TO_CONFIG")
                    .references(UsagePointMetrologyConfiguration.class)
                    .on(metrologyConfigColumn)
                    .map(MetrologyConfigurationMeterRoleUsageImpl.Fields.METROLOGY_CONFIGURATION.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.METER_ROLES.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_USAGE_MCMR_TO_ROLE")
                    .references(MeterRole.class)
                    .on(meterRoleColumn)
                    .map(MetrologyConfigurationMeterRoleUsageImpl.Fields.METER_ROLE.fieldName())
                    .add();
        }
    },
    MTR_RT_TEMPLATE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeTemplate> table = dataModel.addTable(name(), ReadingTypeTemplate.class);
            table.map(ReadingTypeTemplateImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_RT_TEMPLATE_JRNL");
            table.cache();

            Column idColumn = table.addAutoIdColumn();
            table.column(ReadingTypeTemplateImpl.Fields.NAME.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(ReadingTypeTemplateImpl.Fields.NAME.fieldName())
                    .add();
            table.column(ReadingTypeTemplateImpl.Fields.DEFAULT_TEMPLATE.name())
                    .number()
                    .conversion(NUMBER2ENUM)
                    .map(ReadingTypeTemplateImpl.Fields.DEFAULT_TEMPLATE.fieldName())
                    .add();
            table.column(ReadingTypeTemplateImpl.Fields.EQUIDISTANT.name())
                    .varChar(NAME_LENGTH)
                    .conversion(CHAR2ENUM)
                    .since(version(10,3))
                    .map(ReadingTypeTemplateImpl.Fields.EQUIDISTANT.fieldName())
                    .add();
            table.addAuditColumns();

            table.primaryKey("PK_MTR_RT_TEMPLATE").on(idColumn).add();
        }
    },
    MTR_RT_TEMPLATE_ATTR {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeTemplateAttribute> table = dataModel.addTable(name(), ReadingTypeTemplateAttribute.class);
            table.map(ReadingTypeTemplateAttributeImpl.class);
            table.since(version(10 ,2));
            table.setJournalTableName("MTR_RT_TEMPLATE_ATTR_JNRL").since(version(10, 2));

            Column idColumn = table.addAutoIdColumn();
            Column templateColumn = table
                    .column(ReadingTypeTemplateAttributeImpl.Fields.TEMPLATE.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column nameColumn = table.column(ReadingTypeTemplateAttributeImpl.Fields.NAME.name())
                    .number()
                    .conversion(NUMBER2ENUM)
                    .notNull()
                    .map(ReadingTypeTemplateAttributeImpl.Fields.NAME.fieldName())
                    .add();
            table.column(ReadingTypeTemplateAttributeImpl.Fields.CODE.name())
                    .number()
                    .conversion(NUMBER2INTWRAPPER)
                    .map(ReadingTypeTemplateAttributeImpl.Fields.CODE.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_RT_TEMPLATE_ATTR").on(idColumn).add();
            table.unique("UK_MTR_RT_TEMPLATE_ATTR").on(templateColumn, nameColumn).add();
            table.foreignKey("FK_TEMPLATE_ATTR_TO_TEMPLATE")
                    .references(ReadingTypeTemplate.class)
                    .on(templateColumn)
                    .map(ReadingTypeTemplateAttributeImpl.Fields.TEMPLATE.fieldName())
                    .reverseMap(ReadingTypeTemplateImpl.Fields.ATTRIBUTES.fieldName())
                    .composition()
                    .add();
        }
    },
    MTR_RT_TEMPLATE_ATTR_VALUE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeTemplateAttributeValueImpl> table = dataModel.addTable(name(), ReadingTypeTemplateAttributeValueImpl.class);
            table.map(ReadingTypeTemplateAttributeValueImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_RT_TEMPL_ATTR_VALUE_JRNL");

            Column attrColumn = table
                    .column(ReadingTypeTemplateAttributeValueImpl.Fields.ATTR.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column valueColumn = table.column(ReadingTypeTemplateAttributeValueImpl.Fields.CODE.name())
                    .number()
                    .conversion(NUMBER2INT)
                    .notNull()
                    .map(ReadingTypeTemplateAttributeValueImpl.Fields.CODE.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_RT_TPL_ATTR_VALUE").on(attrColumn, valueColumn).add();
            table.foreignKey("FK_RT_TPL_ATTR_VALUE_TO_ATTR")
                    .references(ReadingTypeTemplateAttribute.class)
                    .on(attrColumn)
                    .map(ReadingTypeTemplateAttributeValueImpl.Fields.ATTR.fieldName())
                    .reverseMap(ReadingTypeTemplateAttributeImpl.Fields.POSSIBLE_VALUES.fieldName())
                    .composition()
                    .add();
        }
    },
    MTR_RT_REQUIREMENT {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeRequirement> table = dataModel.addTable(name(), ReadingTypeRequirement.class);
            table.map(ReadingTypeRequirementImpl.IMPLEMENTERS);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_RT_REQUIREMENT_JNRL");

            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("REQTYPE", "char(3)");
            Column nameColumn = table.column(ReadingTypeRequirementImpl.Fields.NAME.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(ReadingTypeRequirementImpl.Fields.NAME.fieldName())
                    .add();
            Column metrologyConfigColumn = table
                    .column(ReadingTypeRequirementImpl.Fields.METROLOGY_CONFIGURATION.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column templateColumn = table
                    .column(ReadingTypeRequirementImpl.Fields.TEMPLATE.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .map(ReadingTypeRequirementImpl.Fields.TEMPLATE.fieldName())
                    .add();
            Column readingTypeColumn = table
                    .column(ReadingTypeRequirementImpl.Fields.READING_TYPE.name())
                    .varChar(NAME_LENGTH)
                    .add();
            table.addAuditColumns();

            table.primaryKey("PK_MTR_RT_REQUIREMENT").on(idColumn).add();
            table.unique("UK_MTR_RT_REQUIREMENT_NAME").on(nameColumn, metrologyConfigColumn).add();
            table.foreignKey("FK_RT_REQUIREMENT_TO_M_CONFIG")
                    .references(MetrologyConfiguration.class)
                    .on(metrologyConfigColumn)
                    .map(ReadingTypeRequirementImpl.Fields.METROLOGY_CONFIGURATION.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.RT_REQUIREMENTS.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_RT_REQUIREMENT_TO_TPL")
                    .references(ReadingTypeTemplate.class)
                    .on(templateColumn)
                    .map("readingTypeTemplate")
                    .add();
            table.foreignKey("FK_RT_REQUIREMENT_TO_RT")
                    .references(ReadingType.class)
                    .on(readingTypeColumn)
                    .map(ReadingTypeRequirementImpl.Fields.READING_TYPE.fieldName())
                    .add();
        }
    },
    MTR_RT_REQUIREMENT_ATTR_VALUE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PartiallySpecifiedReadingTypeAttributeValueImpl> table = dataModel.addTable(name(), PartiallySpecifiedReadingTypeAttributeValueImpl.class);
            table.map(PartiallySpecifiedReadingTypeAttributeValueImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_RT_REQ_ATTR_VALUE_JRNL");

            Column requirementColumn = table
                    .column(PartiallySpecifiedReadingTypeAttributeValueImpl.Fields.READING_TYPE_REQUIREMENT.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column nameColumn = table.column(PartiallySpecifiedReadingTypeAttributeValueImpl.Fields.ATTRIBUTE_NAME.name())
                    .number()
                    .conversion(NUMBER2ENUM)
                    .notNull()
                    .map(PartiallySpecifiedReadingTypeAttributeValueImpl.Fields.ATTRIBUTE_NAME.fieldName())
                    .add();
            table.column(PartiallySpecifiedReadingTypeAttributeValueImpl.Fields.CODE.name())
                    .number()
                    .conversion(NUMBER2INT)
                    .notNull()
                    .map(PartiallySpecifiedReadingTypeAttributeValueImpl.Fields.CODE.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_RT_REQ_ATTR_VALUE").on(requirementColumn, nameColumn).add();
            table.foreignKey("FK_RT_REQ_ATTR_VALUE_TO_RT_REQ")
                    .references(ReadingTypeRequirement.class)
                    .on(requirementColumn)
                    .map(PartiallySpecifiedReadingTypeAttributeValueImpl.Fields.READING_TYPE_REQUIREMENT.fieldName())
                    .reverseMap(ReadingTypeRequirementImpl.Fields.ATTRIBUTES.fieldName())
                    .composition()
                    .add();
        }
    },
    MTR_REQUIREMENT_2_METER_ROLE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ReadingTypeRequirementMeterRoleUsage> table = dataModel.addTable(name(), ReadingTypeRequirementMeterRoleUsage.class);
            table.map(ReadingTypeRequirementMeterRoleUsage.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_REQ_2_METER_ROLE_JRNL");

            Column meterRoleColumn = table.column(ReadingTypeRequirementMeterRoleUsage.Fields.METER_ROLE.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .add();
            Column requirementColumn = table
                    .column(ReadingTypeRequirementMeterRoleUsage.Fields.READING_TYPE_REQUIREMENT.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column metrologyConfigColumn = table.column(ReadingTypeRequirementMeterRoleUsage.Fields.METROLOGY_CONFIGURATION.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add(); // we need this column for a reverse reference map

            table.addAuditColumns();
            table.primaryKey("PK_MTR_REQUIREMENT_2_ROLE").on(meterRoleColumn, requirementColumn).add();
            table.foreignKey("FK_REQ2ROLE_TO_CONFIG")
                    .references(UsagePointMetrologyConfiguration.class)
                    .on(metrologyConfigColumn)
                    .map(ReadingTypeRequirementMeterRoleUsage.Fields.METROLOGY_CONFIGURATION.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.REQUIREMENT_TO_ROLE_REFERENCES.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_REQ2ROLE_TO_ROLE")
                    .references(MeterRole.class)
                    .on(meterRoleColumn)
                    .map(ReadingTypeRequirementMeterRoleUsage.Fields.METER_ROLE.fieldName())
                    .add();
            table.foreignKey("FK_REQ2ROLE_TO_REQ")
                    .references(ReadingTypeRequirement.class)
                    .on(requirementColumn)
                    .map(ReadingTypeRequirementMeterRoleUsage.Fields.READING_TYPE_REQUIREMENT.fieldName())
                    .add();
        }
    },
    MTR_METROLOGY_PURPOSE {
        @Override
        void addTo(DataModel dataModel) {
            Table<MetrologyPurpose> table = dataModel.addTable(name(), MetrologyPurpose.class);
            table.map(MetrologyPurposeImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_METROLOGY_PURPOSE_JRNL");

            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column(MetrologyPurposeImpl.Fields.NAME.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(MetrologyPurposeImpl.Fields.NAME.fieldName())
                    .add();
            table.column(MetrologyPurposeImpl.Fields.DESCRIPTION.name())
                    .varChar(DESCRIPTION_LENGTH)
                    .map(MetrologyPurposeImpl.Fields.DESCRIPTION.fieldName())
                    .add();
            table.column(MetrologyPurposeImpl.Fields.DEFAULT_PURPOSE.name())
                    .number()
                    .conversion(NUMBER2ENUM)
                    .map(MetrologyPurposeImpl.Fields.DEFAULT_PURPOSE.fieldName())
                    .add();
            table.column(MetrologyPurposeImpl.Fields.TRANSLATABLE.name())
                    .bool()
                    .conversion(CHAR2BOOLEAN)
                    .map(MetrologyPurposeImpl.Fields.TRANSLATABLE.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_METROLOGY_PURPOSE").on(idColumn).add();
            table.unique("UK_MTR_METROLOGY_PURPOSE_NAME").on(nameColumn).add();
        }
    },
    MTR_METROLOGY_CONTRACT {
        @Override
        void addTo(DataModel dataModel) {
            Table<MetrologyContract> table = dataModel.addTable(name(), MetrologyContract.class);
            table.map(MetrologyContractImpl.class);
            table.setJournalTableName("MTR_METROLOGY_CONTRACT_JRNL").since(version(10, 2));

            Column idColumn = table.addAutoIdColumn();
            Column metrologyConfigColumn = table
                    .column(MetrologyContractImpl.Fields.METROLOGY_CONFIG.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column metrologyPurposeColumn = table
                    .column(MetrologyContractImpl.Fields.METROLOGY_PURPOSE.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            table.column(MetrologyContractImpl.Fields.MANDATORY.name())
                    .bool()
                    .notNull()
                    .map(MetrologyContractImpl.Fields.MANDATORY.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_METROLOGY_CONTRACT").on(idColumn).add();
            table.unique("UK_MTR_METROLOGY_CONTRACT").on(metrologyConfigColumn, metrologyPurposeColumn).add();
            table.foreignKey("MTR_CONTRACT_TO_M_CONFIG")
                    .references(MetrologyConfiguration.class)
                    .on(metrologyConfigColumn)
                    .map(MetrologyContractImpl.Fields.METROLOGY_CONFIG.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.METROLOGY_CONTRACTS.fieldName())
                    .composition()
                    .add();
            table.foreignKey("MTR_CONTRACT_TO_M_PURPOSE")
                    .references(MetrologyPurpose.class)
                    .on(metrologyPurposeColumn)
                    .map(MetrologyContractImpl.Fields.METROLOGY_PURPOSE.fieldName())
                    .add();
        }
    },
    MTR_RT_DELIVERABLE {
        @Override
        void addTo(DataModel dataModel) {
            Table<ReadingTypeDeliverable> table = dataModel.addTable(name(), ReadingTypeDeliverable.class);
            table.map(ReadingTypeDeliverableImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_RT_DELIVERABLE_JNRL");

            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column(ReadingTypeDeliverableImpl.Fields.NAME.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .map(ReadingTypeDeliverableImpl.Fields.NAME.fieldName())
                    .add();
            Column metrologyConfigColumn = table
                    .column(ReadingTypeDeliverableImpl.Fields.METROLOGY_CONFIGURATION.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column readingTypeColumn = table
                    .column(ReadingTypeDeliverableImpl.Fields.READING_TYPE.name())
                    .varChar(NAME_LENGTH)
                    .notNull()
                    .add();
            Column formulaColumn = table
                    .column(ReadingTypeDeliverableImpl.Fields.FORMULA.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            table.column(ReadingTypeDeliverableImpl.Fields.DELIVERABLE_TYPE.name())
                    .map(ReadingTypeDeliverableImpl.Fields.DELIVERABLE_TYPE.fieldName())
                    .number()
                    .since(version(10, 3))
                    .installValue("2")
                    .notNull()
                    .conversion(NUMBER2ENUMPLUSONE).add();
            table.addAuditColumns();

            table.primaryKey("PK_MTR_DELIVERABLE").on(idColumn).add();
            table.unique("UK_MTR_DELIVERABLE_NAME").on(nameColumn, metrologyConfigColumn).add();
            table.foreignKey("MTR_DELIVERABLE_TO_CONFIG")
                    .references(MetrologyConfiguration.class)
                    .on(metrologyConfigColumn)
                    .map(ReadingTypeDeliverableImpl.Fields.METROLOGY_CONFIGURATION.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.DELIVERABLES.fieldName())
                    .composition()
                    .add();
            table.foreignKey("MTR_DELIVERABLE_TO_RT")
                    .references(ReadingType.class)
                    .on(readingTypeColumn)
                    .map(ReadingTypeDeliverableImpl.Fields.READING_TYPE.fieldName())
                    .add();
            table.foreignKey("MTR_DELIVERABLE_TO_FORMULA")
                    .references(Formula.class)
                    .on(formulaColumn)
                    .map(ReadingTypeDeliverableImpl.Fields.FORMULA.fieldName())
                    .add();
        }
    },
    MTR_CONTRACT_TO_DELIVERABLE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<MetrologyContractReadingTypeDeliverableUsage> table = dataModel.addTable(name(), MetrologyContractReadingTypeDeliverableUsage.class);
            table.map(MetrologyContractReadingTypeDeliverableUsage.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_CONTRACT2DELIVERABLE_JRNL");

            Column metrologyContractColumn = table.column(MetrologyContractReadingTypeDeliverableUsage.Fields.METROLOGY_CONTRACT.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column deliverableColumn = table.column(MetrologyContractReadingTypeDeliverableUsage.Fields.DELIVERABLE.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            table.addAuditColumns();

            table.primaryKey("PK_MTR_CONTRCT_DELIVRBLE").on(metrologyContractColumn, deliverableColumn).add();
            table.foreignKey("FK_CONTR_DELIVER_TO_CONTR")
                    .references(MetrologyContract.class)
                    .on(metrologyContractColumn)
                    .map(MetrologyContractReadingTypeDeliverableUsage.Fields.METROLOGY_CONTRACT.fieldName())
                    .reverseMap(MetrologyContractImpl.Fields.DELIVERABLES.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_CONTR_DELIVER_TO_DELIVER")
                    .references(ReadingTypeDeliverable.class)
                    .on(deliverableColumn)
                    .map(MetrologyContractReadingTypeDeliverableUsage.Fields.DELIVERABLE.fieldName())
                    .add();
        }
    },
    MTR_UP_REQUIREMENT {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointRequirement> table = dataModel.addTable(name(), UsagePointRequirement.class);
            table.map(UsagePointRequirementImpl.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_UP_REQUIREMENT_JRNL");

            Column metrologyConfigurationColumn = table.column(UsagePointRequirementImpl.Fields.METROLOGY_CONFIGURATION.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column searchablePropertyColumn = table.column(UsagePointRequirementImpl.Fields.SEARCHABLE_PROPERTY.name())
                    .varChar(Table.SHORT_DESCRIPTION_LENGTH)
                    .notNull()
                    .map(UsagePointRequirementImpl.Fields.SEARCHABLE_PROPERTY.fieldName())
                    .add();
            table.column(UsagePointRequirementImpl.Fields.OPERATOR.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2ENUM)
                    .map(UsagePointRequirementImpl.Fields.OPERATOR.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_UP_REQUIREMENT").on(metrologyConfigurationColumn, searchablePropertyColumn).add();
            table.foreignKey("MTR_UP_REQUIREMENT_2_CONFIG")
                    .on(metrologyConfigurationColumn)
                    .references(UsagePointMetrologyConfiguration.class)
                    .map(UsagePointRequirementImpl.Fields.METROLOGY_CONFIGURATION.fieldName())
                    .reverseMap(MetrologyConfigurationImpl.Fields.USAGE_POINT_REQUIREMENTS.fieldName())
                    .composition()
                    .add();
        }
    },
    MTR_UP_REQUIREMENT_VALUE {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointRequirementValue> table = dataModel.addTable(name(), UsagePointRequirementValue.class);
            table.map(UsagePointRequirementValue.class);
            table.since(version(10, 2));
            table.setJournalTableName("MTR_UP_REQUIREMENT_VALUE_JRNL");
            Column metrologyConfigurationColumn = table.column(UsagePointRequirementImpl.Fields.METROLOGY_CONFIGURATION.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column searchablePropertyColumn = table.column(UsagePointRequirementImpl.Fields.SEARCHABLE_PROPERTY.name())
                    .varChar(Table.SHORT_DESCRIPTION_LENGTH)
                    .notNull()
                    .add();
            Column positionColumn = table.column(UsagePointRequirementValue.Fields.POSITION.name())
                    .number()
                    .notNull()
                    .conversion(NUMBER2INT)
                    .map(UsagePointRequirementValue.Fields.POSITION.fieldName())
                    .add();
            table.column(UsagePointRequirementValue.Fields.VALUE.name())
                    .varChar(Table.SHORT_DESCRIPTION_LENGTH)
                    .notNull()
                    .map(UsagePointRequirementValue.Fields.VALUE.fieldName())
                    .add();

            table.addAuditColumns();
            table.primaryKey("PK_MTR_UP_REQUIREMENT_VAL").on(metrologyConfigurationColumn, searchablePropertyColumn, positionColumn).add();
            table.foreignKey("MTR_UP_REQ_VALUE_2_REQ")
                    .on(metrologyConfigurationColumn, searchablePropertyColumn)
                    .references(UsagePointRequirement.class)
                    .map(UsagePointRequirementValue.Fields.USAGE_POINT_REQUIREMENT.fieldName())
                    .reverseMap(UsagePointRequirementImpl.Fields.CONDITION_VALUES.fieldName())
                    .reverseMapOrder(UsagePointRequirementValue.Fields.POSITION.fieldName())
                    .composition()
                    .add();
        }
    },
    MTR_EFFECTIVE_CONTRACT {
        @Override
        void addTo(DataModel dataModel) {
            Table<EffectiveMetrologyContractOnUsagePoint> table = dataModel.addTable(name(), EffectiveMetrologyContractOnUsagePoint.class);
            table.map(EffectiveMetrologyContractOnUsagePointImpl.class);
            table.since(version(10, 2));

            Column idColumn = table.addAutoIdColumn();
            List<Column> intervalColumns = table.addIntervalColumns(EffectiveMetrologyContractOnUsagePointImpl.Fields.INTERVAL.fieldName());
            Column effectiveConfColumn = table.column(EffectiveMetrologyContractOnUsagePointImpl.Fields.EFFECTIVE_CONF.name()).number().conversion(ColumnConversion.NUMBER2LONG).add();
            Column metrologyContractColumn = table.column(EffectiveMetrologyContractOnUsagePointImpl.Fields.METROLOGY_CONTRACT.name()).number().conversion(ColumnConversion.NUMBER2LONG).add();

            table.primaryKey("PK_MTR_EFFECTIVE_CONTRACT").on(idColumn).add();
            table.foreignKey("MTR_EF_CONTRACT_2_EF_CONF")
                    .on(effectiveConfColumn)
                    .references(EffectiveMetrologyConfigurationOnUsagePoint.class)
                    .map(EffectiveMetrologyContractOnUsagePointImpl.Fields.EFFECTIVE_CONF.fieldName())
                    .reverseMap("effectiveContracts")
                    .composition()
                    .add();
            table.foreignKey("MTR_EF_CONTRACT_2_CONTRACT")
                    .on(metrologyContractColumn)
                    .references(MetrologyContract.class)
                    .map(EffectiveMetrologyContractOnUsagePointImpl.Fields.METROLOGY_CONTRACT.fieldName())
                    .add();
        }
    },
    MTR_CHANNEL_CONTAINER {
        @Override
        void addTo(DataModel dataModel) {
            Table<ChannelsContainer> table = dataModel.addTable(name(), ChannelsContainer.class);
            table.since(version(10, 2));

            Map<String, Class<? extends ChannelsContainer>> implementers = new HashMap<>();
            implementers.put("MeterActivation", MeterActivationChannelsContainerImpl.class);
            implementers.put("MetrologyContract", MetrologyContractChannelsContainerImpl.class);
            table.map(implementers);

            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("CONTAINER_TYPE", "varchar2(80 char)");
            Column meterActivationColumn = table.column("METER_ACTIVATION").number().conversion(ColumnConversion.NUMBER2LONG).add();
            Column effectiveMetrologyContractColumn = table.column("EFFECTIVE_CONTRACT")
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .upTo(version(10, 3))
                    .add();

            table.addAuditColumns();

            table.primaryKey("PK_MTR_CONTRACT_CHANNEL").on(idColumn).add();
            table.unique("UK_MTR_CH_CONTAINER_MA").on(meterActivationColumn).add();
            table.foreignKey("MTR_CH_CONTAINER_2_MA")
                    .on(meterActivationColumn)
                    .references(MeterActivation.class)
                    .map("meterActivation")
                    .reverseMap("channelsContainer")
                    .add();
            table.unique("MTR_CH_CONTAINER_EF_CONTR_UK")
                    .on(effectiveMetrologyContractColumn)
                    .upTo(version(10, 3))
                    .add();
            table.foreignKey("MTR_CH_CONTAINER_2_EF_CONTR")
                    .upTo(version(10, 3))
                    .on(effectiveMetrologyContractColumn)
                    .references(EffectiveMetrologyContractOnUsagePoint.class)
                    .map(MetrologyContractChannelsContainerImpl.Fields.EFFECTIVE_CONTRACT.fieldName())
                    .reverseMap(EffectiveMetrologyContractOnUsagePointImpl.Fields.CHANNELS_CONTAINER.fieldName())
                    .composition()
                    .add();
        }
    },
    ADD_MTR_EFFECTIVE_CONTRACT_CHANNEL_CONTAINER {
        @Override
        void addTo(DataModel dataModel) {
            Table<?> table = dataModel.getTable(MTR_EFFECTIVE_CONTRACT.name());
            Column channelContainerColumn = table.column(EffectiveMetrologyContractOnUsagePointImpl.Fields.CHANNELS_CONTAINER
                    .name()).number().conversion(ColumnConversion.NUMBER2LONG).since(version(10, 3)).add();
            table.foreignKey("MTR_EF_CONTR_2_CH_CONTAINER")
                    .since(version(10, 3))
                    .on(channelContainerColumn)
                    .references(MTR_CHANNEL_CONTAINER.name())
                    .map(EffectiveMetrologyContractOnUsagePointImpl.Fields.CHANNELS_CONTAINER.fieldName())
                    .reverseMap(MetrologyContractChannelsContainerImpl.Fields.EFFECTIVE_CONTRACT.fieldName())
                    .add();
        }
    },
    MTR_CHANNEL {
        @Override
        void addTo(DataModel dataModel) {
            Table<Channel> table = dataModel.addTable(name(), Channel.class);
            table.map(ChannelImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column meterActivationIdColumn = table.column("METERACTIVATIONID").type("number").notNull().conversion(NUMBER2LONG).upTo(version(10, 2)).add();
            Column channelsContainerId = table.column("CHANNEL_CONTAINER").type("number").conversion(NUMBER2LONG).previously(meterActivationIdColumn).since(Version.version(10, 2)).add();
            Column timeSeriesIdColumn = table.column("TIMESERIESID").type("number").notNull().conversion(NUMBER2LONG).add();
            Column mainReadingTypeMRIDColumn = table.column("MAINREADINGTYPEMRID").varChar(NAME_LENGTH).notNull().add();
            table.column("MAINDERIVATIONRULE").number().conversion(ColumnConversion.NUMBER2ENUM).map("mainDerivationRule").notNull().add();
            Column bulkQuantityReadingTypeMRIDColumn = table.column("BULKQUANTITYREADINGTYPEMRID").varChar(NAME_LENGTH).add();
            table.column("BULKDERIVATIONRULE").number().conversion(ColumnConversion.NUMBER2ENUM).map("bulkDerivationRule").add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_CHANNEL").on(idColumn).add();
            table.foreignKey("FK_MTR_CHANNELACTIVATION")
                    .references(MeterActivation.class)
                    .on(meterActivationIdColumn)
                    .onDelete(RESTRICT)
                    .upTo(version(10, 2))
                    .map("meterActivation;")
                    .add();
            table.foreignKey("FK_MTR_CHANNEL_CONTAINER")
                    .references(ChannelsContainer.class)
                    .onDelete(RESTRICT)
                    .map("channelsContainer")
                    .reverseMap("channels", TimeSeries.class, ReadingTypeInChannel.class)
                    .on(channelsContainerId)
                    .composition()
                    .since(version(10, 2))
                    .add();
            table.foreignKey("FK_MTR_CHANNELMAINTYPE")
                    .references(ReadingType.class)
                    .onDelete(RESTRICT)
                    .map("mainReadingType")
                    .on(mainReadingTypeMRIDColumn)
                    .add();
            table.foreignKey("FK_MTR_CHANNELBULQUANTITYTYPE")
                    .references(ReadingType.class)
                    .onDelete(RESTRICT)
                    .map("bulkQuantityReadingType")
                    .on(bulkQuantityReadingTypeMRIDColumn)
                    .add();
            table.foreignKey("FK_MTR_CHANNELTIMESERIES")
                    .on(timeSeriesIdColumn)
                    .references(TimeSeries.class)
                    .onDelete(RESTRICT)
                    .map("timeSeries")
                    .add();
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
            table.column("DERIVATIONRULE").number().conversion(ColumnConversion.NUMBER2ENUM).map("derivationRule").notNull().add();
            table.primaryKey("PK_MTR_READINGTYPEINCHANNEL").on(channelIdColumn, positionColumn).add();
            table.foreignKey("FK_MTR_READINGTYPEINCHANNEL1")
                    .on(channelIdColumn)
                    .references(Channel.class)
                    .composition()
                    .onDelete(CASCADE)
                    .map("channel")
                    .reverseMap("readingTypeInChannels")
                    .reverseMapOrder("position")
                    .add();
            table.foreignKey("FK_MTR_READINGTYPEINCHANNEL2")
                    .on(readingTypeMRidColumn)
                    .references(ReadingType.class)
                    .onDelete(RESTRICT)
                    .map("readingType")
                    .add();
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
            Column typeColumn = table.column("TYPE").varChar(64).notNull().map("typeCode").add();
            Column readingTypeColumn = table.column("READINGTYPE").varChar(NAME_LENGTH).notNull().add();
            Column actual = table.column("ACTUAL").bool().notNull().map("actual").add();
            table.addAuditColumns();
            table.column("COMMENTS").varChar(4000).map("comment").add();
            table.primaryKey("PK_MTR_READINGQUALITY").on(idColumn).add();
            table.foreignKey("FK_MTR_RQ_CHANNEL")
                    .references(Channel.class)
                    .onDelete(DeleteRule.RESTRICT)
                    .map("channel")
                    .on(channelColumn)
                    .add();
            table.foreignKey("FK_MTR_RQ_READINGTYPE")
                    .references(ReadingType.class)
                    .onDelete(DeleteRule.RESTRICT)
                    .map("readingType")
                    .on(readingTypeColumn)
                    .add();
            table.unique("MTR_U_READINGQUALITY")
                    .on(channelColumn, timestampColumn, typeColumn, readingTypeColumn)
                    .add();
            table
                    .index("MTR_READINGQUALITY_VAL_OVERVW")
                    .on(channelColumn, typeColumn, actual)
                    .add();
        }
    },
    ADD_IN_OUT_DEPENDENCIES_TO_FORMULA_NODE {
        @Override
        void addTo(DataModel dataModel) {
            Table<?> table = dataModel.getTable(MTR_FORMULA_NODE.name());
            // ReadingTypeDeliverableNodeImpl readingTypeDeliverable value
            Column readingTypeDeliverableColumn = table.getColumn("READINGTYPE_DELIVERABLE").get();
            // ReadingTypeRequirementNodeImpl readingTypeRequirement value
            Column readingTypeRequirementColumn = table.getColumn("READINGTYPE_REQUIREMENT").get();
            table.foreignKey("MTR_FORMULA_TO_DELIVERABLE")
                    .references(MTR_RT_DELIVERABLE.name())
                    .on(readingTypeDeliverableColumn)
                    .map("readingTypeDeliverable")
                    .add();
            table.foreignKey("MTR_FORMULA_TO_RT_REQ")
                    .references(MTR_RT_REQUIREMENT.name())
                    .on(readingTypeRequirementColumn)
                    .map("readingTypeRequirement")
                    .add();
        }
    },

    MTR_ENDDEVICECONTROLTYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<EndDeviceControlType> table = dataModel.addTable(name(), EndDeviceControlType.class);
            table.map(EndDeviceControlTypeImpl.class);
            table.cache();
            Column mRidColumn = table.column("MRID").varChar(NAME_LENGTH).notNull().map("mRID").add();
            table.column("ALIASNAME").varChar(SHORT_DESCRIPTION_LENGTH).map("aliasName").add();
            table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
            table.addAuditColumns();
            table.primaryKey("PK_MTR_ENDDEVICECONTROLTYPE").on(mRidColumn).add();
        }
    },

    MTR_GASDAYOPTIONS {
        @Override
        void addTo(DataModel dataModel) {
            Table<GasDayOptions> table = dataModel.addTable(name(), GasDayOptions.class);
            table.since(version(10, 2));
            table.map(GasDayOptionsImpl.class);
            table.cache();
            Column id = this.addColum("ID", "id", table);
            this.addColum("MONTH", "month", table);
            this.addColum("DAY", "day", table);
            this.addColum("HOUR", "hour", table);
            table.primaryKey("MTR_PK_GASDAYOPTIONS").on(id).add();
        }

        private Column addColum(String columnName, String fieldName, Table table) {
            return table.column(columnName)
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(fieldName)
                    .add();
        }
    },
    MTR_UPL_STATE {
        @Override
        void addTo(DataModel dataModel) {
            Table<UsagePointStateTemporalImpl> table = dataModel.addTable(name(), UsagePointStateTemporalImpl.class);
            table.map(UsagePointStateTemporalImpl.class);
            table.since(version(10, 3));
            Column usagePoint = table.column("USAGE_POINT").notNull().number().conversion(ColumnConversion.NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column state = table.column("UPL_STATE").notNull().number().conversion(ColumnConversion.NUMBER2LONG).add();
            table.addAuditColumns();
            table.primaryKey("MTR_UPL_STATE_PK").on(usagePoint, intervalColumns.get(0)).add();
            table.foreignKey("MTR_UPL_STATE_2_UP_FK")
                    .on(usagePoint)
                    .references(UsagePoint.class)
                    .onDelete(CASCADE)
                    .map("usagePoint")
                    .reverseMap("state")
                    .composition()
                    .add();
            table.foreignKey("FK_UPL_STATE_2_STATE")
                    .on(state)
                    .references(UsagePointState.class)
                    .onDelete(RESTRICT)
                    .map("state")
                    .add();
        }
    },;

    abstract void addTo(DataModel dataModel);

    private static class Constants {
        static final String JOURNAL_TABLE_SUFFIX = "JRNL";
    }

    private static class TableBuilder {
        static void buildLocationMemberTable(Table<?> table, List<TemplateField> templateMembers) {

            Column locationIdColumn = table.column("LOCATIONID")
                    .number()
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .add();
            Column localeColumn = table.column("LOCALE").varChar(Table.NAME_LENGTH).notNull().map("locale").add();

            if (templateMembers != null && !templateMembers.isEmpty()) {
                templateMembers.stream().filter(f -> !"locale".equalsIgnoreCase(f.getName())).forEach(column -> {
                            if (column.isMandatory()) {
                                table.column(column.getName().toUpperCase())
                                        .varChar(Table.NAME_LENGTH)
                                        .notNull()
                                        .map(column.getName())
                                        .add();
                                table.column("UPPER" + column.getName().toUpperCase())
                                        .varChar(NAME_LENGTH)
                                        .notNull()
                                        .as("UPPER(" + column.getName().toUpperCase() + ")")
                                        .alias("upper" + column.getName().substring(0, 1).toUpperCase() + column.getName()
                                                .substring(1))
                                        .add();
                            } else {
                                table.column(column.getName().toUpperCase())
                                        .varChar(Table.NAME_LENGTH)
                                        .map(column.getName())
                                        .add();
                                table.column("UPPER" + column.getName().toUpperCase())
                                        .varChar(NAME_LENGTH)
                                        .as("UPPER(" + column.getName().toUpperCase() + ")")
                                        .alias("upper" + column.getName().substring(0, 1).toUpperCase() + column.getName()
                                                .substring(1))
                                        .add();
                            }
                        }
                );

            }
            table.column("DEFAULTLOCATION").bool().map("defaultLocation").add();
            table.primaryKey("PK_MTR_LOCATION_MEMBER").on(locationIdColumn, localeColumn).add();
            table.foreignKey("FK_MTR_LOCATION_MEMBER").on(locationIdColumn)
                    .references(MTR_LOCATION.name())
                    .onDelete(RESTRICT)
                    .map("location")
                    .reverseMap("members")
                    .add();

        }
    }
}
