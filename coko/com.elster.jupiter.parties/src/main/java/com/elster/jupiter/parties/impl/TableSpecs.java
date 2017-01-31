/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
	PRT_PARTY () {
		void addTo(DataModel dataModel) {
			Table<Party> table = dataModel.addTable(name(), Party.class);
			table.map(PartyImpl.IMPLEMENTERS);
			table.setJournalTableName("PRT_PARTYJRNL");
			Column idColumn = table.addAutoIdColumn();
			table.addDiscriminatorColumn("PARTYTYPE", "char(1)");
			Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).map("mRID").add();
			Column nameColumn = table.column("NAME").varChar().map("name").add();
			table.column("ALIASNAME").varChar().map("aliasName").add();
			table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
			table.column("EAEMAIL1").varChar(NAME_LENGTH).map("electronicAddress.email1").add();
			table.column("EAEMAIL2").varChar(NAME_LENGTH).map("electronicAddress.email2").add();
			table.column("EALAN").varChar(NAME_LENGTH).map("electronicAddress.lan").add();
			table.column("EAEMAC").varChar(NAME_LENGTH).map("electronicAddress.mac").add();
			table.column("EAPASSWORD").varChar(NAME_LENGTH).map("electronicAddress.password").add();
			table.column("EARADIO").varChar(NAME_LENGTH).map("electronicAddress.radio").add();
			table.column("EAUSERID").varChar(NAME_LENGTH).map("electronicAddress.userID").add();
			table.column("EAWEB").varChar(NAME_LENGTH).map("electronicAddress.web").add();
			table.column("PAPOBOX").varChar(NAME_LENGTH).map("postalAddress.poBox").add();
			table.column("PAPOSTALCODE").varChar(NAME_LENGTH).map("postalAddress.postalCode").add();
			table.column("PASTREETADDRESSGENERAL").varChar(NAME_LENGTH).map("postalAddress.streetDetail.addressGeneral").add();
			table.column("PASTREETBUILDINGNAME").varChar(NAME_LENGTH).map("postalAddress.streetDetail.buildingName").add();
			table.column("PASTREETCODE").varChar(NAME_LENGTH).map("postalAddress.streetDetail.code").add();
			table.column("PASTREETNAME").varChar(NAME_LENGTH).map("postalAddress.streetDetail.name").add();
			table.column("PASTREETNUMBER").varChar(NAME_LENGTH).map("postalAddress.streetDetail.number").add();
			table.column("PASTREETPREFIX").varChar(NAME_LENGTH).map("postalAddress.streetDetail.prefix").add();
			table.column("PASTREETSUFFIX").varChar(NAME_LENGTH).map("postalAddress.streetDetail.suffix").add();
			table.column("PASTREETSUITENUMBER").varChar(NAME_LENGTH).map("postalAddress.streetDetail.suiteNumber").add();
			table.column("PASTREETTYPE").varChar(NAME_LENGTH).map("postalAddress.streetDetail.type").add();
			table.column("PASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("postalAddress.streetDetail.withinTownLimits").add();
			table.column("PATOWNCODE").varChar(NAME_LENGTH).map("postalAddress.townDetail.code").add();
			table.column("PATOWNCOUNTRY").varChar(NAME_LENGTH).map("postalAddress.townDetail.country").add();
			table.column("PATOWNNAME").varChar(NAME_LENGTH).map("postalAddress.townDetail.name").add();
			table.column("PATOWNSECTION").varChar(NAME_LENGTH).map("postalAddress.townDetail.section").add();
			table.column("PATOWNSTATE").varChar(NAME_LENGTH).map("postalAddress.townDetail.stateOrProvince").add();
			table.column("SASTATUSDATETIME").number().conversion(NUMBER2INSTANT).map("streetAddress.status.dateTime").add();
			table.column("SASTATUSREASON").varChar(NAME_LENGTH).map("streetAddress.status.reason").add();
			table.column("SASTATUSREMARK").varChar(NAME_LENGTH).map("streetAddress.status.remark").add();
			table.column("SASTATUSVALUE").varChar(NAME_LENGTH).map("streetAddress.status.value").add();
			table.column("SASTREETADDRESSGENERAL").varChar(NAME_LENGTH).map("streetAddress.streetDetail.addressGeneral").add();
			table.column("SASTREETBUILDINGNAME").varChar(NAME_LENGTH).map("streetAddress.streetDetail.buildingName").add();
			table.column("SASTREETCODE").varChar(NAME_LENGTH).map("streetAddress.streetDetail.code").add();
			table.column("SASTREETNAME").varChar(NAME_LENGTH).map("streetAddress.streetDetail.name").add();
			table.column("SASTREETNUMBER").varChar(NAME_LENGTH).map("streetAddress.streetDetail.number").add();
			table.column("SASTREETPREFIX").varChar(NAME_LENGTH).map("streetAddress.streetDetail.prefix").add();
			table.column("SASTREETSUFFIX").varChar(NAME_LENGTH).map("streetAddress.streetDetail.suffix").add();
			table.column("SASTREETSUITENUMBER").varChar(NAME_LENGTH).map("streetAddress.streetDetail.suiteNumber").add();
			table.column("SASTREETTYPE").varChar(NAME_LENGTH).map("streetAddress.streetDetail.type").add();
			table.column("SASTREETWITHINTOWN").type("char(1)").conversion(CHAR2BOOLEAN).map("streetAddress.streetDetail.withinTownLimits").add();
			table.column("SATOWNCODE").varChar(NAME_LENGTH).map("streetAddress.townDetail.code").add();
			table.column("SATOWNCOUNTRY").varChar(NAME_LENGTH).map("streetAddress.townDetail.country").add();
			table.column("SATOWNNAME").varChar(NAME_LENGTH).map("streetAddress.townDetail.name").add();
			table.column("SATOWNSECTION").varChar(NAME_LENGTH).map("streetAddress.townDetail.section").add();
			table.column("SATOWNSTATE").varChar(NAME_LENGTH).map("streetAddress.townDetail.stateOrProvince").add();
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
			table.column("FIRSTNAME").varChar(NAME_LENGTH).map("firstName").add();
			table.column("LASTNAME").varChar(NAME_LENGTH).map("lastName").add();
			table.column("MIDDLENAME").varChar(NAME_LENGTH).map("middleName").add();
			table.column("PREFIX").varChar(NAME_LENGTH).map("prefix").add();
			table.column("SUFFIX").varChar(NAME_LENGTH).map("suffix").add();
			table.column("SPECIALNEED").varChar(NAME_LENGTH).map("specialNeed").add();
			table.column("UPPERNAME").varChar(NAME_LENGTH).as("UPPER(NAME)").alias("upperName").add();
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTY").on(idColumn).add();
			table.unique("PTR_U_PARTY").on(mRIDColumn).add();
			table.unique("PTR_UQ_PARTY_NAME").on(nameColumn).add();
		}
	},
	PRT_PARTYREP {
		void addTo(DataModel dataModel) {
			Table<PartyRepresentation> table = dataModel.addTable(name(), PartyRepresentation.class);
			table.map(PartyRepresentationImpl.class);
			Column delegateColumn = table.column("DELEGATE").varChar(SHORT_DESCRIPTION_LENGTH).notNull().map("delegate").add();
			Column partyIdColumn = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.setJournalTableName("PRT_PARTYREPJRNL").since(version(10, 2));
			table.addAuditColumns();
			table.primaryKey("PRT_PK_PARTYREP").on(delegateColumn , partyIdColumn , intervalColumns.get(0)).add();
			table
                .foreignKey("PRT_FKPARTYREP")
                .on(partyIdColumn)
                .references(PRT_PARTY.name())
                .map("party")
                .reverseMap("representations")
                .reverseMapOrder("delegate")
                .composition()
                .add();
		}
	},
	PRT_PARTYROLE {
		void addTo(DataModel dataModel) {
			Table<PartyRole> table = dataModel.addTable(name(), PartyRole.class);
			table.map(PartyRoleImpl.class);
			table.cache();
			Column mRIDColumn = table.column("MRID").varChar(NAME_LENGTH).notNull().map("mRID").add();
			table.column("COMPONENT").varChar(3).notNull().map("componentName").add();
			table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
			table.column("ALIASNAME").varChar(NAME_LENGTH).map("aliasName").add();
			table.column("DESCRIPTION").varChar(SHORT_DESCRIPTION_LENGTH).map("description").add();
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTYROLE").on(mRIDColumn).add();
		}
	},
	PRT_PARTYINROLE {
		void addTo(DataModel dataModel) {
			Table<PartyInRole> table = dataModel.addTable(name(), PartyInRole.class);
			table.map(PartyInRoleImpl.class);
			Column id = table.addAutoIdColumn();
			Column party = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
			Column roleMRID = table.column("PARTYROLEMRID").varChar(NAME_LENGTH).notNull().add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");
            table.setJournalTableName("PRT_PARTYINROLEJRNL").since(version(10, 2));
			table.addAuditColumns();
			table.column("UPPERROLEMRID").varChar(NAME_LENGTH).as("upper(PARTYROLEMRID)").alias("upperPartyRoleMRID").add();
			table.primaryKey("PTR_PK_PARTYINROLE").on(id).add();
			table.unique("PTR_U_PARTYINROLE").on(party , roleMRID , intervalColumns.get(0)).add();
			table
                .foreignKey("PRT_FKPARTYINROLEPARTY")
                .on(party)
                .references(PRT_PARTY.name())
                .map("party")
                .reverseMap("partyInRoles")
                .composition()
                .add();
			table
                .foreignKey("PRT_FKPARTYINROLEROLE")
                .on(roleMRID)
                .references(PRT_PARTYROLE.name())
                .onDelete(DeleteRule.RESTRICT)
                .map("role")
                .add();
		}
	};

	abstract void addTo(DataModel component);


}