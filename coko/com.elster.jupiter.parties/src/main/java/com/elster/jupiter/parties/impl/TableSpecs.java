package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {
	PRT_PARTY {
		void describeTable(Table table) {
			table.map(PartyImpl.IMPLEMENTERS);
			table.setJournalTableName("PRT_PARTYJRNL");
			Column idColumn = table.addAutoIdColumn();
			table.addDiscriminatorColumn("PARTYTYPE", "char(1)");
			Column mRIDColumn = table.addColumn("MRID", "varchar2(80)", false, NOCONVERSION , "mRID");
			table.addColumn("NAME", "varchar2(80)", false, NOCONVERSION , "name");
			table.addColumn("ALIASNAME", "varchar2(80)", false, NOCONVERSION , "aliasName");
			table.addColumn("DESCRIPTION", "varchar2(256)", false, NOCONVERSION , "description");
			table.addColumn("EAEMAIL1", "varchar2(80)", false, NOCONVERSION , "electronicAddress.email1");
			table.addColumn("EAEMAIL2", "varchar2(80)", false, NOCONVERSION , "electronicAddress.email2");
			table.addColumn("EALAN", "varchar2(80)", false, NOCONVERSION , "electronicAddress.lan");
			table.addColumn("EAEMAC", "varchar2(80)", false, NOCONVERSION , "electronicAddress.mac");
			table.addColumn("EAPASSWORD", "varchar2(80)", false, NOCONVERSION , "electronicAddress.password");
			table.addColumn("EARADIO", "varchar2(80)", false, NOCONVERSION , "electronicAddress.radio");
			table.addColumn("EAUSERID", "varchar2(80)", false, NOCONVERSION , "electronicAddress.userID");
			table.addColumn("EAWEB", "varchar2(80)", false, NOCONVERSION , "electronicAddress.web");
			table.addColumn("PAPOBOX", "varchar2(80)", false, NOCONVERSION , "postalAddress.poBox");
			table.addColumn("PAPOSTALCODE", "varchar2(80)", false, NOCONVERSION , "postalAddress.postalCode");			
			table.addColumn("PASTREETADDRESSGENERAL", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.addressGeneral");
			table.addColumn("PASTREETBUILDINGNAME", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.buildingName");
			table.addColumn("PASTREETCODE", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.code");
			table.addColumn("PASTREETNAME", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.name");
			table.addColumn("PASTREETNUMBER", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.number");
			table.addColumn("PASTREETPREFIX", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.prefix");
			table.addColumn("PASTREETSUFFIX", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.suffix");
			table.addColumn("PASTREETSUITENUMBER", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.suiteNumber");
			table.addColumn("PASTREETTYPE", "varchar2(80)", false, NOCONVERSION , "postalAddress.streetDetail.type");
			table.addColumn("PASTREETWITHINTOWN", "char(1)", false, CHAR2BOOLEAN , "postalAddress.streetDetail.withinTownLimits");
			table.addColumn("PATOWNCODE", "varchar2(80)", false, NOCONVERSION , "postalAddress.townDetail.code");
			table.addColumn("PATOWNCOUNTRY", "varchar2(80)", false, NOCONVERSION , "postalAddress.townDetail.country");
			table.addColumn("PATOWNNAME", "varchar2(80)", false, NOCONVERSION , "postalAddress.townDetail.name");
			table.addColumn("PATOWNSECTION", "varchar2(80)", false, NOCONVERSION , "postalAddress.townDetail.section");
			table.addColumn("PATOWNSTATE", "varchar2(80)", false, NOCONVERSION , "postalAddress.townDetail.stateOrProvince");			
			table.addColumn("SASTATUSDATETIME", "number", false, NUMBER2UTCINSTANT , "streetAddress.status.dateTime");
			table.addColumn("SASTATUSREASON", "varchar2(80)", false, NOCONVERSION , "streetAddress.status.reason");
			table.addColumn("SASTATUSREMARK", "varchar2(80)", false, NOCONVERSION , "streetAddress.status.remark");
			table.addColumn("SASTATUSVALUE", "varchar2(80)", false, NOCONVERSION , "streetAddress.status.value");	
			table.addColumn("SASTREETADDRESSGENERAL", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.addressGeneral");
			table.addColumn("SASTREETBUILDINGNAME", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.buildingName");
			table.addColumn("SASTREETCODE", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.code");
			table.addColumn("SASTREETNAME", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.name");
			table.addColumn("SASTREETNUMBER", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.number");
			table.addColumn("SASTREETPREFIX", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.prefix");
			table.addColumn("SASTREETSUFFIX", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.suffix");
			table.addColumn("SASTREETSUITENUMBER", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.suiteNumber");
			table.addColumn("SASTREETTYPE", "varchar2(80)", false, NOCONVERSION , "streetAddress.streetDetail.type");
			table.addColumn("SASTREETWITHINTOWN", "char(1)", false, CHAR2BOOLEAN , "streetAddress.streetDetail.withinTownLimits");
			table.addColumn("SATOWNCODE", "varchar2(80)", false, NOCONVERSION , "streetAddress.townDetail.code");
			table.addColumn("SATOWNCOUNTRY", "varchar2(80)", false, NOCONVERSION , "streetAddress.townDetail.country");
			table.addColumn("SATOWNNAME", "varchar2(80)", false, NOCONVERSION , "streetAddress.townDetail.name");
			table.addColumn("SATOWNSECTION", "varchar2(80)", false, NOCONVERSION , "streetAddress.townDetail.section");
			table.addColumn("SATOWNSTATE", "varchar2(80)", false, NOCONVERSION , "streetAddress.townDetail.stateOrProvince");			
			table.addColumn("PHONE1AREA", "varchar2(80)", false, NOCONVERSION , "phone1.areaCode");
			table.addColumn("PHONE1CITY", "varchar2(80)", false, NOCONVERSION , "phone1.cityCode");
			table.addColumn("PHONE1COUNTRY", "varchar2(80)", false, NOCONVERSION , "phone1.countryCode");
			table.addColumn("PHONE1EXTENSION", "varchar2(80)", false, NOCONVERSION , "phone1.extension");
			table.addColumn("PHONE1LOCALNUMBER", "varchar2(80)", false, NOCONVERSION , "phone1.localNumber");
			table.addColumn("PHONE2AREA", "varchar2(80)", false, NOCONVERSION , "phone2.areaCode");
			table.addColumn("PHONE2CITY", "varchar2(80)", false, NOCONVERSION , "phone2.cityCode");
			table.addColumn("PHONE2COUNTRY", "varchar2(80)", false, NOCONVERSION , "phone2.countryCode");
			table.addColumn("PHONE2EXTENSION", "varchar2(80)", false, NOCONVERSION , "phone2.extension");
			table.addColumn("PHONE2LOCALNUMBER", "varchar2(80)", false, NOCONVERSION , "phone2.localNumber");
			table.addColumn("FIRSTNAME" , "varchar2(80)", false , NOCONVERSION , "firstName");
			table.addColumn("LASTNAME" , "varchar2(80)", false , NOCONVERSION , "lastName");
			table.addColumn("MIDDLENAME" , "varchar2(80)", false , NOCONVERSION , "middleName");
			table.addColumn("PREFIX" , "varchar2(80)", false , NOCONVERSION , "prefix");
			table.addColumn("SUFFIX" , "varchar2(80)", false , NOCONVERSION , "suffix");
			table.addColumn("SPECIALNEED" , "varchar2(80)", false , NOCONVERSION , "specialNeed");
			table.addAuditColumns();
			table.addPrimaryKeyConstraint("PTR_PK_PARTY", idColumn);
			table.addUniqueConstraint("PTR_U_PARTY", mRIDColumn);
		}
	},
	PRT_PARTYREP {
		void describeTable(Table table) {
			table.map(PartyRepresentationImpl.class);
			Column delegateColumn = table.column("DELEGATE").type("varchar2(256)").notNull().map("delegate").add();
			Column partyIdColumn = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");			
			table.addAuditColumns();
			table.primaryKey("PRT_PK_PARTYREP").on(delegateColumn , partyIdColumn , intervalColumns.get(0)).add();
			table.foreignKey("PRT_FKPARTYREP").on(partyIdColumn).references(PRT_PARTY.name()).onDelete(DeleteRule.CASCADE).
				map("party").reverseMap("representations").reverseMapOrder("delegate").add();
		}		
	},
	PRT_PARTYROLE {
		void describeTable(Table table) {
			table.map(PartyRoleImpl.class);
			Column mRIDColumn = table.column("MRID").type("varchar2(80)").notNull().map("mRID").add();
			table.column("COMPONENT").type("varchar2(3)").notNull().map("componentName").add();
			table.column("NAME").type("varchar2(80)").notNull().map("name").add();
			table.column("ALIASNAME").type("varchar2(80)").map("aliasName").add();
			table.column("DESCRIPTION").type("varchar2(256)").map("description").add();
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTYROLE").on(mRIDColumn).add();			
		}
	},
	PRT_PARTYINROLE {
		void describeTable(Table table) {
			table.map(PartyInRoleImpl.class);
			Column idColumn = table.addAutoIdColumn();
			Column partyIdColumn = table.column("PARTYID").number().notNull().conversion(NUMBER2LONG).add();
			Column roleMRIDColumn = table.column("PARTYROLEMRID").type("varchar2(80)").notNull().map("roleMRID").add();
			List<Column> intervalColumns = table.addIntervalColumns("interval");
			table.addAuditColumns();
			table.primaryKey("PTR_PK_PARTYINROLE").on(idColumn).add();
			table.unique("PTR_U_PARTYINROLE").on(partyIdColumn , roleMRIDColumn , intervalColumns.get(0)).add();
			table.foreignKey("PRT_FKPARTYINROLEPARTY").on(partyIdColumn).references(PRT_PARTY.name()).onDelete(DeleteRule.CASCADE).map("party").reverseMap("partyInRoles").add();
			table.foreignKey("PRT_FKPARTYINROLEROLE").on(roleMRIDColumn).references(PRT_PARTYROLE.name()).onDelete(DeleteRule.RESTRICT).map("role").add();
		}
	};
		
	public void addTo(DataModel component) {
		Table table = component.addTable(name());
		describeTable(table);
	}
	
	abstract void describeTable(Table table);
	
}