package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.cbo.Accumulation.DELTADELTA;
import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.*;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.VOLTAMPEREHOUR;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.cbo.TimeAttribute.*;


public class ReadingTypeGenerator {

	private static final TimeAttribute[] timeAttributes = {MINUTE1,MINUTE2,MINUTE3,MINUTE5,MINUTE10,MINUTE15,MINUTE20,MINUTE30,MINUTE60,HOUR24};
	private static final String[] timeAttributeNames = {"1m","2m","3m","5m","10m","15m","20m","30m","Hourly","Daily"};

    public ReadingTypeGenerator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private enum Root {
		FORWARDENERGY (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO,WATTHOUR),"Forward Energy"),
		REVERSEENERGY (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO,WATTHOUR),"Reverse Energy"),
		NETENERGY (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(NET).measure(ENERGY).in(KILO,WATTHOUR),"Net Energy"),
		LAGGINGREACTIVE(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(LAGGING).measure(ENERGY).in(KILO,VOLTAMPEREHOUR),"Lagging Reactive Energy"),
		LEADINGREACTIVE(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(LEADING).measure(ENERGY).in(KILO,VOLTAMPEREHOUR),"Leading Reactive Energy"),
		TOTALREACTIVE(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(TOTAL).measure(ENERGY).in(KILO,VOLTAMPEREHOUR),"Total Reactive Energy"),
		TOTALBYPHASEREACTIVE(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(TOTALBYPHASE).measure(ENERGY).in(KILO,VOLTAMPEREHOUR),"Total by Phase Reactive Energy"),
		Q1REACTIVE(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q1).measure(ENERGY).in(KILO,VOLTAMPEREHOUR),"Q1 Reactive Energy"),
		Q2REACTIVE(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q2).measure(ENERGY).in(KILO,VOLTAMPEREHOUR),"Q2 Reactive Energy"),
		Q3REACTIVE(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q3).measure(ENERGY).in(KILO,VOLTAMPEREHOUR),"Q3 Reactive Energy"),
		Q4REACTIVE(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q4).measure(ENERGY).in(KILO,VOLTAMPEREHOUR),"Q4 Reactive Energy");
	
		private final ReadingTypeCodeBuilder builder;
		private final String name;

		Root(ReadingTypeCodeBuilder builder , String name) {
			this.builder = builder;
			this.name = name;
		}
	}
	
	private final List<ReadingTypeImpl> readingTypes = new ArrayList<>();
    private final DataModel dataModel;
	
	static List<ReadingTypeImpl> generate(DataModel dataModel) {
		return new ReadingTypeGenerator(dataModel).readingTypes();
	}
	
	private List<ReadingTypeImpl> readingTypes() {
		for (Root root : Root.values()) {
			generate(root);
		}
		return readingTypes;
	}
	
	private void generate(Root root) {
		for (int i = 0 ; i < timeAttributes.length ; i++) {
			String code = root.builder.period(timeAttributes[i]).accumulate(DELTADELTA).code();
			String name = timeAttributeNames[i] + " " + root.name;		
			readingTypes.add(ReadingTypeImpl.from(dataModel, code, name));
			code = root.builder.period(timeAttributes[i]).accumulate(Accumulation.BULKQUANTITY).code();
			name = timeAttributeNames[i] + " " + root.name + " Cumulative index";
			readingTypes.add(ReadingTypeImpl.from(dataModel, code, name));
		}
		String code = root.builder.period(TimeAttribute.NOTAPPLICABLE).accumulate(Accumulation.BULKQUANTITY).code();
		String name = root.name + " Cumulative index";
		readingTypes.add(ReadingTypeImpl.from(dataModel, code, name));
	}
}
