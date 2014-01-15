package com.elster.jupiter.metering.impl;

import static com.elster.jupiter.ids.FieldType.DATE;
import static com.elster.jupiter.ids.FieldType.LONGINTEGER;
import static com.elster.jupiter.ids.FieldType.NUMBER;

import com.elster.jupiter.ids.FieldDerivationRule;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.metering.MeteringService;

public enum RecordSpecs {
	SINGLEINTERVAL("Single Interval Data",true) {
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {			
			recordSpec.addFieldSpec("Value", NUMBER);
		}
	},
	CUMULATIVEINTERVAL("Cumulative Interval Data",true) {  
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {
			recordSpec.addDerivedFieldSpec("Value", "Cumulative", NUMBER, FieldDerivationRule.DELTAFROMPREVIOUS);
		}
	},
	MULTIINTERVAL("Multi Interval Data",true) {
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {
			recordSpec.addFieldSpec("Value1", NUMBER);
			recordSpec.addFieldSpec("Value2", NUMBER);
			recordSpec.addFieldSpec("Value3", NUMBER);
			recordSpec.addFieldSpec("Value4", NUMBER);
			recordSpec.addFieldSpec("Value5", NUMBER);
			recordSpec.addFieldSpec("Value6", NUMBER);
		}
	},
	BASEREGISTER("Base Register",false) {
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {
			recordSpec.addFieldSpec("Value", NUMBER);
		}
	},
	BILLINGPERIOD("Billing Period Register",false) {
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {
			 recordSpec.addFieldSpec("Value", NUMBER);
			 recordSpec.addFieldSpec("From Time", DATE);
		}
	},
	DEMANDREGISTER("Demand Register",false) {
		void addFieldSpecs(RecordSpec recordSpec) {
			recordSpec.addFieldSpec("Value", NUMBER);
			recordSpec.addFieldSpec("From Time", DATE);
			recordSpec.addFieldSpec("Event Time", DATE);
		}
	};
	  
	private final String specName;
	private final boolean interval;
	  
	RecordSpecs(String specName,boolean interval) {
		this.specName = specName;
		this.interval = interval;
	}
	  
	static void createAll(IdsService idsService) {
		for (RecordSpecs each : values()) {
			each.create(idsService);
		}
	}

	private RecordSpec create(IdsService idsService) {
		RecordSpec recordSpec = idsService.newRecordSpec(MeteringService.COMPONENTNAME, ordinal() + 1 , specName);
		recordSpec.addFieldSpec("ProcessingFlags", LONGINTEGER);
		if (interval) {
			recordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
		}
		addFieldSpecs(recordSpec);
		recordSpec.persist();
		return recordSpec;
	}
	  
	abstract void addFieldSpecs(RecordSpec recordSpec);
	
	final RecordSpec get(IdsService idsService) {
		return idsService.getRecordSpec(MeteringService.COMPONENTNAME, ordinal()+1).get();
	}
}
