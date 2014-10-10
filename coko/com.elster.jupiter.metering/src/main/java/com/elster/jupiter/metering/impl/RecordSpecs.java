package com.elster.jupiter.metering.impl;

import static com.elster.jupiter.ids.FieldType.INSTANT;
import static com.elster.jupiter.ids.FieldType.LONGINTEGER;
import static com.elster.jupiter.ids.FieldType.NUMBER;
import static com.elster.jupiter.ids.FieldType.TEXT;

import com.elster.jupiter.ids.FieldDerivationRule;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.Interval;

public enum RecordSpecs {
	SINGLEINTERVAL("Single Interval Data",true) {
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {			
			recordSpec.addFieldSpec("Value", NUMBER);
		}
		
		@Override
		Object[] toArray(BaseReading reading, ProcessStatus status) {
			Object[] result = new Object[3];
			result[0] = status.getBits();
			if (reading instanceof IntervalReading) {
				result[1] = ((IntervalReading) reading).getProfileStatus().getBits();
			} else {
				result[1] = 0L;
			}
			result[2] = reading.getValue();
			return result;
		}
	},
	BULKQUANTITYINTERVAL("Bulk Quantity Interval Data",true) {  
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {
			recordSpec.addDerivedFieldSpec("Value", "Bulk", NUMBER, FieldDerivationRule.DELTAFROMPREVIOUS);
		}
		
		@Override
		Object[] toArray(BaseReading reading, ProcessStatus status) {
			Object[] result = new Object[4];
			result[0] = status.getBits();
			if (reading instanceof IntervalReading) {
				result[1] = ((IntervalReading) reading).getProfileStatus().getBits();
			} else {
				result[1] = 0L;
			}
			result[status.get(ProcessStatus.Flag.EDITED) ? 2 : 3] = reading.getValue();
			return result;
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
		
		@Override
		Object[] toArray(BaseReading reading, ProcessStatus status) {
			throw new UnsupportedOperationException();
		}
	},
	BASEREGISTER("Base Register",false) {
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {
			recordSpec.addFieldSpec("Value", NUMBER);
			recordSpec.addFieldSpec("Text", TEXT);
		}
		
		@Override
		Object[] toArray(BaseReading reading, ProcessStatus status) {
			Object[] result = new Object[3];
			result[0] = status.getBits();
			result[1] = reading.getValue();
			result[2] = ((Reading) reading).getText();
			return result;
		}
	},
	BILLINGPERIOD("Billing Period Register",false) {
		@Override
		void addFieldSpecs(RecordSpec recordSpec) {
			 recordSpec.addFieldSpec("Value", NUMBER);
			 recordSpec.addFieldSpec("Text", TEXT);
			 recordSpec.addFieldSpec("From Time", INSTANT);
			 recordSpec.addFieldSpec("To Time", INSTANT);
		}
		
		@Override
		Object[] toArray(BaseReading reading, ProcessStatus status) {
			Object[] result = new Object[5];
			result[0] = status.getBits();
			result[1] = reading.getValue();
			result[2] = ((Reading) reading).getText();
			Interval interval = reading.getTimePeriod();
			if (interval != null) {
				result[3] = interval.getStart();
				result[4] = interval.getEnd();
			}
			return result;
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
		recordSpec.addFieldSpec("ProcesStatus", LONGINTEGER);
		if (interval) {
			recordSpec.addFieldSpec("ProfileStatus", LONGINTEGER);
		}
		addFieldSpecs(recordSpec);
		recordSpec.persist();
		return recordSpec;
	}
	  
	abstract void addFieldSpecs(RecordSpec recordSpec);
	
	abstract Object[] toArray(BaseReading reading, ProcessStatus status);
	
	final RecordSpec get(IdsService idsService) {
		return idsService.getRecordSpec(MeteringService.COMPONENTNAME, ordinal()+1).get();
	}
}
