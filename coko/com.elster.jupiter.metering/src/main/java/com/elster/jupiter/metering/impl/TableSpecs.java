package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointGroup;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.DeleteRule.RESTRICT;

public enum TableSpecs {
	MTR_SERVICECATEGORY(ServiceCategory.class) {
		void describeTable(Table table) {
            table.map(ServiceCategoryImpl.class);
			table.setJournalTableName("MTR_SERVICECATEGORYJRNL");
			Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2ENUMPLUSONE).map("kind").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.addAuditColumns();			
			table.primaryKey("MTR_PK_SERVICECATEGORY").on(idColumn).add();
		}
	},
	MTR_SERVICELOCATION(ServiceLocation.class) {
		void describeTable(Table table) {
            table.map(ServiceLocationImpl.class);
			table.setJournalTableName("MTR_SERVICELOCATIONJRNL");
			Column idColumn = table.addAutoIdColumn();
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
			table.column("NAME").type("varchar2(80)").map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.column("DIRECTION").type("varchar2(80)").map("direction").add();
			table.column("EAEMAIL1").type("varchar2(80)").map("electronicAddress.email1").add();
			table.column("EAEMAIL2").type("varchar2(80)").map("electronicAddress.email2").add();
			table.column("EALAN").type("varchar2(80)").map("electronicAddress.lan").add();
			table.column("EAEMAC").type("varchar2(80)").map("electronicAddress.mac").add();
			table.column("EAPASSWORD").type("varchar2(80)").map("electronicAddress.password").add();
			table.column("EARADIO").type("varchar2(80)").map("electronicAddress.radio").add();
			table.column("EAUSERID").type("varchar2(80)").map("electronicAddress.userID").add();
			table.column("EAWEB").type("varchar2(80)").map("electronicAddress.web").add();
			table.column("GEOINFOREFERENCE").type("varchar2(80)").map("geoInfoReference").add();
			table.column("MASTATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("mainAddress.status.dateTime").add();
			table.column("MASTATUSREASON").type("varchar2(80)").map("mainAddress.status.reason").add();
			table.column("MASTATUSREMARK").type("varchar2(80)").map("mainAddress.status.remark").add();
			table.column("MASTATUSVALUE").type("varchar2(80)").map("mainAddress.status.value").add();
			table.column("MASTREETADDRESSGENERAL").type("varchar2(80)").map("mainAddress.streetDetail.addressGeneral").add();
			table.column("MASTREETBUILDINGNAME").type("varchar2(80)").map("mainAddress.streetDetail.buildingName").add();
			table.column("MASTREETCODE").type("varchar2(80)").map("mainAddress.streetDetail.code").add();
			table.column("MASTREETNAME").type("varchar2(80)").map("mainAddress.streetDetail.name").add();
			table.column("MASTREETNUMBER").type("varchar2(80)").map("mainAddress.streetDetail.number").add();
			table.column("MASTREETPREFIX").type("varchar2(80)").map("mainAddress.streetDetail.prefix").add();
			table.column("MASTREETSUFFIX").type("varchar2(80)").map("mainAddress.streetDetail.suffix").add();
			table.column("MASTREETSUITENUMBER").type("varchar2(80)").map("mainAddress.streetDetail.suiteNumber").add();
			table.column("MASTREETTYPE").type("varchar2(80)").map("mainAddress.streetDetail.type").add();
			table.column("MASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("mainAddress.streetDetail.withinTownLimits").add();
			table.column("MATOWNCODE").type("varchar2(80)").map("mainAddress.townDetail.code").add();
			table.column("MATOWNCOUNTRY").type("varchar2(80)").map("mainAddress.townDetail.country").add();
			table.column("MATOWNNAME").type("varchar2(80)").map("mainAddress.townDetail.name").add();
			table.column("MATOWNSECTION").type("varchar2(80)").map("mainAddress.townDetail.section").add();
			table.column("MATOWNSTATE").type("varchar2(80)").map("mainAddress.townDetail.stateOrProvince").add();
			table.column("PHONE1AREA").type("varchar2(80)").map("phone1.areaCode").add();
			table.column("PHONE1CITY").type("varchar2(80)").map("phone1.cityCode").add();
			table.column("PHONE1COUNTRY").type("varchar2(80)").map("phone1.countryCode").add();
			table.column("PHONE1EXTENSION").type("varchar2(80)").map("phone1.extension").add();
			table.column("PHONE1LOCALNUMBER").type("varchar2(80)").map("phone1.localNumber").add();
			table.column("PHONE2AREA").type("varchar2(80)").map("phone2.areaCode").add();
			table.column("PHONE2CITY").type("varchar2(80)").map("phone2.cityCode").add();
			table.column("PHONE2COUNTRY").type("varchar2(80)").map("phone2.countryCode").add();
			table.column("PHONE2EXTENSION").type("varchar2(80)").map("phone2.extension").add();
			table.column("PHONE2LOCALNUMBER").type("varchar2(80)").map("phone2.localNumber").add();
			table.column("SASTATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("secondaryAddress.status.dateTime").add();
			table.column("SASTATUSREASON").type("varchar2(80)").map("secondaryAddress.status.reason").add();
			table.column("SASTATUSREMARK").type("varchar2(80)").map("secondaryAddress.status.remark").add();
			table.column("SASTATUSVALUE").type("varchar2(80)").map("secondaryAddress.status.value").add();
			table.column("SASTREETADDRESSGENERAL").type("varchar2(80)").map("secondaryAddress.streetDetail.addressGeneral").add();
			table.column("SASTREETBUILDINGNAME").type("varchar2(80)").map("secondaryAddress.streetDetail.buildingName").add();
			table.column("SASTREETCODE").type("varchar2(80)").map("secondaryAddress.streetDetail.code").add();
			table.column("SASTREETNAME").type("varchar2(80)").map("secondaryAddress.streetDetail.name").add();
			table.column("SASTREETNUMBER").type("varchar2(80)").map("secondaryAddress.streetDetail.number").add();
			table.column("SASTREETPREFIX").type("varchar2(80)").map("secondaryAddress.streetDetail.prefix").add();
			table.column("SASTREETSUFFIX").type("varchar2(80)").map("secondaryAddress.streetDetail.suffix").add();
			table.column("SASTREETSUITENUMBER").type("varchar2(80)").map("secondaryAddress.streetDetail.suiteNumber").add();
			table.column("SASTREETTYPE").type("varchar2(80)").map("secondaryAddress.streetDetail.type").add();
			table.column("SASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("secondaryAddress.streetDetail.withinTownLimits").add();
			table.column("SATOWNCODE").type("varchar2(80)").map("secondaryAddress.townDetail.code").add();
			table.column("SATOWNCOUNTRY").type("varchar2(80)").map("secondaryAddress.townDetail.country").add();
			table.column("SATOWNNAME").type("varchar2(80)").map("secondaryAddress.townDetail.name").add();
			table.column("SATOWNSECTION").type("varchar2(80)").map("secondaryAddress.townDetail.section").add();
			table.column("SATOWNSTATE").type("varchar2(80)").map("secondaryAddress.townDetail.stateOrProvince").add();
			table.column("STATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("status.dateTime").add();
			table.column("STATUSREASON").type("varchar2(80)").map("status.reason").add();
			table.column("STATUSREMARK").type("varchar2(80)").map("status.remark").add();
			table.column("STATUSVALUE").type("varchar2(80)").map("status.value").add();
			table.column("SERVICELOCATIONTYPE").type("varchar2(80)").map("type").add();
			table.column("ACCESSMETHOD").type("varchar2(80)").map("accessMethod").add();
			table.column("NEEDSINSPECTION").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("needsInspection").add();
			table.column("SITEACCESSPROBLEM").type("varchar2(80)").map("siteAccessProblem").add();
			table.addAuditColumns();
			table.primaryKey("MTR_PK_SERVICELOCATION").on(idColumn).add();
			table.unique("MTR_U_SERVICELOCATION").on(mRIDColumn).add();
		}
	},
	MTR_AMRSYSTEM(AmrSystem.class) {
		void describeTable(Table table) {
            table.map(AmrSystemImpl.class);
			Column idColumn = table.column("ID").number().notNull().conversion(NUMBER2INT).map("id").add();
			Column nameColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
			table.addAuditColumns();
			table.primaryKey("MTR_PK_AMRSYSTEM").on(idColumn).add();
			table.unique("MTR_U_AMRSYSTEM").on(nameColumn).add();
		}
	},
	MTR_USAGEPOINT(UsagePoint.class) {
		void describeTable(Table table) {
            table.map(UsagePointImpl.class);
			table.setJournalTableName("MTR_USAGEPOINTJRNL");
			Column idColumn = table.addAutoIdColumn();
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
			Column serviceKindColumn = table.column("SERVICEKIND").number().notNull().conversion(NUMBER2ENUMPLUSONE).map("serviceKind").add();
			Column serviceLocationIdColumn = table.column("SERVICELOCATIONID").number().conversion(NUMBER2LONGNULLZERO).map("serviceLocationId").add();
			table.column("NAME").type("varchar2(80)").map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.column("AMIBILLINGREADY").number().notNull().conversion(NUMBER2ENUM).map("amiBillingReady").add();
			table.column("CHECKBILLING").bool().map("checkBilling").add();
			table.column("CONNECTIONSTATE").number().notNull().conversion(NUMBER2ENUM).map("connectionState").add();
			table.addQuantityColumns("ESTIMATEDLOAD", false, "estimatedLoad");
			table.column("GROUNDED").bool().map("grounded").add();
			table.column("ISSDP").bool().map("isSdp").add();
			table.column("ISVIRTUAL").bool().map("isVirtual").add();
			table.column("MINIMALUSAGEEXPECTED").bool().map("minimalUsageExpected").add();
			table.addQuantityColumns("NOMINALVOLTAGE",false, "nominalServiceVoltage");
			table.column("OUTAGEREGION").type("varchar2(80)").map("outageRegion").add();
			table.column("PHASECODE").type("varchar2(7)").conversion(CHAR2ENUM).map("phaseCode").add();
			table.addQuantityColumns("RATEDCURRENT", false, "ratedCurrent");
			table.addQuantityColumns("RATEDPOWER", false , "ratedPower");
			table.column("READCYCLE").type("varchar2(80)").map("readCycle").add();
			table.column("READROUTE").type("varchar2(80)").map("readRoute").add();
			table.column("SERVICEDELIVERYREMARK").type("varchar2(80)").map("serviceDeliveryRemark").add();
			table.column("SERVICEPRIORITY").type("varchar2(80)").map("servicePriority").add();
			table.addAuditColumns();
			table.primaryKey("MTR_PK_USAGEPOINT").on(idColumn).add();
			table.unique("MTR_U_USAGEPOINT").on(mRIDColumn).add();
			table.foreignKey("MTR_FK_USAGEPOINTSERVICECAT").on(serviceKindColumn).references(MTR_SERVICECATEGORY.name()).onDelete(RESTRICT).map("serviceCategory").add();
			table.foreignKey("MTR_FK_USAGEPOINTSERVICELOC").on(serviceLocationIdColumn).references(MTR_SERVICELOCATION.name()).onDelete(RESTRICT).map("serviceLocation").reverseMap("usagePoints").add();
		}
	},
	MTR_READINGTYPE(ReadingType.class) {
		void describeTable(Table table) {
            table.map(ReadingTypeImpl.class);
			Column mRidColumn = table.column("MRID").type("varchar2(80)").notNull().map("mRID").add();
			table.column("ALIASNAME").type("varchar2(256)").map("aliasName").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.addAuditColumns();
			table.primaryKey("MTR_PK_READINGTYPE").on(mRidColumn).add();
		}
	},
	MTR_ENDDEVICE(EndDevice.class) {
		void describeTable(Table table) {
            table.map(EndDeviceImpl.IMPLEMENTERS);
			Column idColumn = table.addAutoIdColumn();
			table.addDiscriminatorColumn("ENDDEVICETYPE", "char(1)");
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
			Column amrSystemIdColumn = table.column("AMRSYSTEMID").type("number").notNull().conversion(NUMBER2INT).map("amrSystemId").add();
			Column amrIdColumn = table.column("AMRID").type("varchar2(256)").notNull().map("amrId").add();
			table.column("NAME").type("varchar2(80)").map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.column("SERIALNUMBER").type("varchar2(80)").map("serialNumber").add();
			table.column("UTCNUMBER").type("varchar2(80)").map("utcNumber").add();
			table.column("EAEMAIL1").type("varchar2(80)").map("electronicAddress.email1").add();
			table.column("EAEMAIL2").type("varchar2(80)").map("electronicAddress.email2").add();
			table.column("EALAN").type("varchar2(80)").map("electronicAddress.lan").add();
			table.column("EAEMAC").type("varchar2(80)").map("electronicAddress.mac").add();
			table.column("EAPASSWORD").type("varchar2(80)").map("electronicAddress.password").add();
			table.column("EARADIO").type("varchar2(80)").map("electronicAddress.radio").add();
			table.column("EAUSERID").type("varchar2(80)").map("electronicAddress.userID").add();
			table.column("EAWEB").type("varchar2(80)").map("electronicAddress.web").add();
			table.addAuditColumns();
			table.primaryKey("MTR_PK_METER").on(idColumn).add();
			table.unique("MTR_U_METER").on(mRIDColumn).add();
			table.unique("MTR_U_METERAMR").on(amrSystemIdColumn, amrIdColumn).add();
			table.foreignKey("MTR_FK_METERAMRSYSTEM").references(MTR_AMRSYSTEM.name()).onDelete(RESTRICT).map("amrSystem").on(amrSystemIdColumn).add();
		}
	},	
	MTR_METERACTIVATION(MeterActivation.class) {
		void describeTable(Table table) {
            table.map(MeterActivationImpl.class);
			Column idColumn = table.addAutoIdColumn();
			Column usagePointIdColumn = table.column("USAGEPOINTID").type("number").conversion(NUMBER2LONGNULLZERO).map("usagePointId").add();
			Column meterIdColumn = table.column("METERID").type("number").conversion(NUMBER2LONGNULLZERO).map("meterId").add();
			table.addIntervalColumns("interval");
			table.addAuditColumns();
			table.primaryKey("MTR_PK_METERACTIVATION").on(idColumn).add();
			table.foreignKey("MTR_FK_METERACTUSAGEPOINT").references(MTR_USAGEPOINT.name()).onDelete(RESTRICT).map("usagePoint").reverseMap("meterActivations").reverseMapOrder("interval.start").reverseMapCurrent("currentMeterActivation").on(usagePointIdColumn).add();
			table.foreignKey("MTR_FK_METERACTMETER").references(MTR_ENDDEVICE.name()).onDelete(RESTRICT).map("meter").reverseMap("meterActivations").reverseMapOrder("interval.start").reverseMapCurrent("currentMeterActivation").on(meterIdColumn).add();
		}
	},
	MTR_CHANNEL(Channel.class) {
		void describeTable(Table table) {
            table.map(ChannelImpl.class);
			Column idColumn = table.addAutoIdColumn();
			Column meterActivationIdColumn = table.column("METERACTIVATIONID").type("number").notNull().conversion(NUMBER2LONG).add();
			Column timeSeriesIdColumn = table.column("TIMESERIESID").type("number").notNull().conversion(NUMBER2LONG).map("timeSeriesId").add();
			Column mainReadingTypeMRIDColumn = table.column("MAINREADINGTYPEMRID").type("varchar2(80)").notNull().map("mainReadingTypeMRID").add();
			Column cumulativeReadingTypeMRIDColumn = table.column("CUMULATIVEREADINGTYPEMRID").type("varchar2(80)").map("cumulativeReadingTypeMRID").add();
			table.addAuditColumns();
			table.primaryKey("MTR_PK_CHANNEL").on(idColumn).add();
			table.foreignKey("MTR_FK_CHANNELACTIVATION").references(MTR_METERACTIVATION.name()).onDelete(RESTRICT).map("meterActivation").reverseMap("channels").on(meterActivationIdColumn).composition().add();
			table.foreignKey("MTR_FK_CHANNELMAINTYPE").references(MTR_READINGTYPE.name()).onDelete(RESTRICT).map("mainReadingType").on(mainReadingTypeMRIDColumn).add();
			table.foreignKey("MTR_FK_CHANNELCUMULATIVETYPE").references(MTR_READINGTYPE.name()).onDelete(RESTRICT).map("cumulativeReadingType").on(cumulativeReadingTypeMRIDColumn).add();
			table.foreignKey("MTR_FK_CHANNELTIMESERIES").on(timeSeriesIdColumn).references("IDS","IDS_TIMESERIES").onDelete(RESTRICT).map("timeSeries").add();
		}
	},		
	MTR_READINGTYPEINCHANNEL(ReadingTypeInChannel.class) {
		void describeTable(Table table) {
            table.map(ReadingTypeInChannel.class);
			Column channelIdColumn = table.column("CHANNNELID").type("number").notNull().conversion(NUMBER2LONG).add();
			Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
			Column readingTypeMRidColumn = table.column("READINGTYPEMRID").type("varchar2(80)").notNull().map("readingTypeMRID").add();
			table.primaryKey("MTR_PK_READINGTYPEINCHANNEL").on(channelIdColumn , positionColumn).add();
			table.foreignKey("MTR_FK_READINGTYPEINCHANNEL1").references(MTR_CHANNEL.name()).onDelete(CASCADE).map("channel").on(channelIdColumn).add();
            table.foreignKey("MTR_FK_READINGTYPEINCHANNEL2").references(MTR_READINGTYPE.name()).onDelete(RESTRICT).map("readingType").on(readingTypeMRidColumn).add();
		}
	},
	MTR_UPACCOUNTABILITY(UsagePointAccountability.class) {
		void describeTable(Table table) {
            table.map(UsagePointAccountabilityImpl.class);
			table.setJournalTableName("MTR_UPACCOUNTABILITYJRNL");
			Column usagePointIdColumn = table.column("USAGEPOINTID").type("number").notNull().conversion(NUMBER2LONG).map("usagePointId").add();
			Column partyIdColumn = table.column("PARTYID").type("number").notNull().conversion(NUMBER2LONG).map("partyId").add();
			Column roleMRIDColumn = table.column("ROLEMRID").type("varchar2(80)").notNull().map("roleMRID").add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");
			table.addAuditColumns();
			table.primaryKey("MTR_PK_UPACCOUNTABILITY").on(usagePointIdColumn , partyIdColumn , roleMRIDColumn , intervalColumns.get(0)).add();
			table.foreignKey("MTR_FK_UPACCOUNTUP").references(MTR_USAGEPOINT.name()).onDelete(CASCADE).map("usagePoint").reverseMap("accountabilities").on(usagePointIdColumn).add();
			table.foreignKey("MTR_FK_UPACCOUNTPARTY").on(partyIdColumn).references("PRT", "PRT_PARTY").onDelete(RESTRICT).map("party").add();
			table.foreignKey("MTR_FK_UPACCOUNTPARTYROLE").on(roleMRIDColumn).references("PRT", "PRT_PARTYROLE").onDelete(RESTRICT).map("role").add();
 		}
	},
    MTR_UP_GROUP(UsagePointGroup.class) {
        @Override
        void describeTable(Table table) {
            table.map(AbstractUsagePointGroup.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").map("name").add();
            Column mRIDColumn = table.column("MRID").type("varchar2(80)").map("mRID").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
            table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
            table.addDiscriminatorColumn("GROUPTYPE", "char(3)");
            table.addAuditColumns();
            table.primaryKey("MTR_PK_ENUM_UP_GROUP").on(idColumn).add();
            table.unique("MTR_U_ENUM_UP_GROUP").on(mRIDColumn).add();
        }
    },
    MTR_ENUM_UP_IN_GROUP(EnumeratedUsagePointGroup.Entry.class) {
        @Override
        void describeTable(Table table) {
            table.map(EnumeratedUsagePointGroupImpl.EntryImpl.class);
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column usagePointColumn = table.column("USAGEPOINT_ID").type("number").notNull().conversion(NUMBER2LONG).map("usagePointId").add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.primaryKey("MTR_PK_ENUM_UP_GROUP_ENTRY").on(groupColumn, usagePointColumn, intervalColumns.get(0)).add();
            table.foreignKey("MTR_FK_UPGE_UPG").references(MTR_UP_GROUP.name()).onDelete(DeleteRule.CASCADE).map("usagePointGroup").on(groupColumn).add();
            table.foreignKey("MTR_FK_UPGE_UP").references(MTR_USAGEPOINT.name()).onDelete(DeleteRule.RESTRICT).map("usagePoint").on(usagePointColumn).add();
        }
    },
    MTR_QUERY_UP_GROUP_OP(QueryBuilderOperation.class) {
        @Override
        void describeTable(Table table) {
            table.map(AbstractQueryBuilderOperation.IMPLEMENTERS);
            Column groupColumn = table.column("GROUP_ID").type("number").notNull().conversion(NUMBER2LONG).map("groupId").add();
            Column positionColumn = table.column("POSITION").type("number").notNull().conversion(NUMBER2INT).map("position").add();
            table.addDiscriminatorColumn("OPERATORTYPE", "char(3)");
            table.column("OPERATOR").type("VARCHAR2(80)").map("operator").add();
            table.column("FIELDNAME").type("VARCHAR2(80)").map("fieldName").add();
            table.column("BINDVALUES").type("VARCHAR2(256)").conversion(CHAR2JSON).map("values").add();

            table.primaryKey("MTR_PK_QUPGOP").on(groupColumn, positionColumn).add();
            table.foreignKey("MTR_FK_QUPG_QUPGOP").references(MTR_UP_GROUP.name()).onDelete(DeleteRule.CASCADE).map("usagePointGroup").reverseMap("operations").reverseMapOrder("position").on(groupColumn).add();

        }
    },
    MTR_READINGQUALITY(ReadingQuality.class) {
        @Override
        void describeTable(Table table) {
            table.map(ReadingQualityImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column channelColumn = table.column("CHANNELID").type("number").notNull().conversion(NUMBER2LONG).map("channelId").add();
            Column timestampColumn = table.column("READINGTIMESTAMP").type("number").notNull().conversion(NUMBER2UTCINSTANT).map("readingTimestamp").add();
            Column typeColumn = table.column("TYPE").type("varchar(64)").notNull().map("typeCode").add();
            table.addAuditColumns();
            table.column("COMMENTS").type("varchar(4000)").map("comment").add();
            table.primaryKey("MTR_PK_READINGQUALITY").on(idColumn).add();
            table.foreignKey("MTR_FK_RQ_CHANNEL").references(MTR_CHANNEL.name()).onDelete(DeleteRule.CASCADE).map("channel").on(channelColumn).add();
            table.unique("MTR_U_READINGQUALITY").on(channelColumn, timestampColumn, typeColumn).add();
        }
    },
    MTR_ENDDEVICEEVENTTYPE(EndDeviceEventType.class) {
        @Override
        void describeTable(Table table) {
            table.map(EndDeviceEventTypeImpl.class);
            Column mRidColumn = table.column("MRID").type("varchar2(80)").notNull().map("mRID").add();
            table.column("ALIASNAME").type("varchar2(256)").map("aliasName").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_ENDDEVICEEVENTTYPE").on(mRidColumn).add();
        }
    }, MTR_ENDDEVICEEVENTRECORD(EndDeviceEventRecord.class) {
        @Override
        void describeTable(Table table) {
            table.map(EndDeviceEventRecordImpl.class);
            table.column("NAME").type("varchar2(80)").map("name").add();
            table.column("MRID").type("varchar2(80)").map("mRID").add();
            table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
            table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
            table.column("REASON").type("varchar2(256)").map("reason").add();
            table.column("SEVERITY").type("varchar2(80)").map("severity").add();
            Column eventTypeColumn = table.column("EVENTTYPE").type("varchar2(80)").notNull().map("eventTypeCode").add();
            table.column("ISSUERID").type("varchar2(80)").map("issuerID").add();
            table.column("ISSUERTRACKINGID").type("varchar2(80)").map("issuerTrackingID").add();
            table.column("STATUSDATETIME").type("number").conversion(NUMBER2UTCINSTANT).map("status.dateTime").add();
            table.column("STATUSREASON").type("varchar2(80)").map("status.reason").add();
            table.column("STATUSREMARK").type("varchar2(80)").map("status.remark").add();
            table.column("STATUSVALUE").type("varchar2(80)").map("status.value").add();
            table.column("PROCESSINGFLAGS").type("number").map("processingFlags").conversion(NUMBER2LONG).add();
            Column endDeviceColumn = table.column("ENDDEVICEID").type("number").notNull().map("endDeviceId").conversion(NUMBER2LONG).add();
            table.column("LOGBOOKID").type("number").map("logBookId").conversion(NUMBER2INT).add();
            table.column("LOGBOOKPOSITION").type("number").map("logBookPosition").conversion(NUMBER2INT).add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME").type("number").notNull().conversion(NUMBER2UTCINSTANT).map("createdDateTime").add();
            table.addAuditColumns();
            table.primaryKey("MTR_PK_ENDDEVICEEVENTRECORD").on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn).add();
            table.foreignKey("MTR_FK_EVENT_ENDDEVICE").references(MTR_ENDDEVICE.name()).onDelete(DeleteRule.CASCADE).map("endDevice").on(endDeviceColumn).add();
        }
    },
    MTR_ENDDEVICEEVENTDETAIL(EndDeviceEventRecordImpl.EndDeviceEventDetailRecord.class) {
        @Override
        void describeTable(Table table) {
            table.map(EndDeviceEventRecordImpl.EndDeviceEventDetailRecord.class);
            Column eventTypeColumn = table.column("EVENTTYPE").type("varchar2(80)").notNull().map("eventTypeCode").add();
            Column endDeviceColumn = table.column("ENDDEVICEID").type("number").notNull().map("endDeviceId").conversion(NUMBER2LONG).add();
            Column createdDateTimeColumn = table.column("CREATEDDATETIME").type("number").notNull().conversion(NUMBER2UTCINSTANT).map("createdDateTime").add();
            Column keyColumn = table.column("KEY").type("varchar2(80)").notNull().map("key").add();
            table.column("DETAIL_VALUE").type("varchar2(256)").notNull().map("value").add();
            table.primaryKey("MTR_PK_ENDDEVICEEVENTDETAIL").on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn, keyColumn).add();
            table.foreignKey("MTR_FK_ENDDEVICEEVENT_DETAIL").on(endDeviceColumn, eventTypeColumn, createdDateTimeColumn).references(MTR_ENDDEVICEEVENTRECORD.name())
                    .onDelete(DeleteRule.CASCADE).map("eventRecord").reverseMap("detailRecords").composition().add();
        }
    };

    private final Class<?> api;

    TableSpecs(Class<?> api) {
        this.api = api;
    }

    public void addTo(DataModel component) {
		Table table = component.addTable(name(), api);
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}