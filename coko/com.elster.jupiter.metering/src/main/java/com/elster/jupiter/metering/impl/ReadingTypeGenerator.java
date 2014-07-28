package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.cbo.Accumulation.DELTADELTA;
import static com.elster.jupiter.cbo.Commodity.ELECTRICITY_SECONDARY_METERED;
import static com.elster.jupiter.cbo.FlowDirection.*;
import static com.elster.jupiter.cbo.MeasurementKind.ENERGY;
import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.cbo.TimeAttribute.*;


public final class ReadingTypeGenerator {

    private static final Logger LOGGER = Logger.getLogger(ReadingTypeGenerator.class.getName());

    private static final TimeAttribute[] timeAttributes = {MINUTE1,MINUTE2,MINUTE3,MINUTE5,MINUTE10,MINUTE15,MINUTE20,MINUTE30,MINUTE60,HOUR24};
    private static final MacroPeriod[] macroPeriods = {MacroPeriod.DAILY, MacroPeriod.MONTHLY};

	private static final String[] timeAttributeNames = {"1m","2m","3m","5m","10m","15m","20m","30m","Hourly","Daily"};
    private static final String[] macroPeriodsNames = {"Daily Period", "Monthly Period"};

    private ReadingTypeGenerator(MeteringServiceImpl meteringService) {
        this.meteringService = meteringService;
    }

    private enum Root {
		FORWARDENERGYKWH (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(KILO,WATTHOUR),"Forward Energy"),
		FORWARDENERGYWH (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(FORWARD).measure(ENERGY).in(WATTHOUR),"Forward Energy"),
		REVERSEENERGYKWH (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(KILO,WATTHOUR),"Reverse Energy"),
		REVERSEENERGYWH (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(REVERSE).measure(ENERGY).in(WATTHOUR),"Reverse Energy"),
		NETENERGYKWH (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(NET).measure(ENERGY).in(KILO,WATTHOUR),"Net Energy"),
		NETENERGYWH (ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(NET).measure(ENERGY).in(WATTHOUR),"Net Energy"),
		TOTALREACTIVEKVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(TOTAL).measure(ENERGY).in(KILO,VOLTAMPEREREACTIVEHOUR),"Total Reactive Energy"),
		TOTALREACTIVEVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(TOTAL).measure(ENERGY).in(VOLTAMPEREREACTIVEHOUR),"Total Reactive Energy"),
		TOTALBYPHASEREACTIVEKVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(TOTALBYPHASE).measure(ENERGY).in(KILO,VOLTAMPEREREACTIVEHOUR),"Total by Phase Reactive Energy"),
		TOTALBYPHASEREACTIVEVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(TOTALBYPHASE).measure(ENERGY).in(VOLTAMPEREREACTIVEHOUR),"Total by Phase Reactive Energy"),
		Q1REACTIVEKVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q1).measure(ENERGY).in(KILO,VOLTAMPEREREACTIVEHOUR),"Q1 Reactive Energy"),
		Q1REACTIVEVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q1).measure(ENERGY).in(VOLTAMPEREREACTIVEHOUR),"Q1 Reactive Energy"),
		Q2REACTIVEKVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q2).measure(ENERGY).in(KILO,VOLTAMPEREREACTIVEHOUR),"Q2 Reactive Energy"),
		Q2REACTIVEVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q2).measure(ENERGY).in(VOLTAMPEREREACTIVEHOUR),"Q2 Reactive Energy"),
		Q3REACTIVEKVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q3).measure(ENERGY).in(KILO,VOLTAMPEREREACTIVEHOUR),"Q3 Reactive Energy"),
		Q3REACTIVEVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q3).measure(ENERGY).in(VOLTAMPEREREACTIVEHOUR),"Q3 Reactive Energy"),
		Q4REACTIVEKVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q4).measure(ENERGY).in(KILO,VOLTAMPEREREACTIVEHOUR),"Q4 Reactive Energy"),
		Q4REACTIVEVARH(ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).flow(Q4).measure(ENERGY).in(VOLTAMPEREREACTIVEHOUR),"Q4 Reactive Energy");

		private final ReadingTypeCodeBuilder builder;
		private final String name;

		Root(ReadingTypeCodeBuilder builder , String name) {
			this.builder = builder;
			this.name = name;
		}
	}
	
	private final List<ReadingTypeImpl> readingTypes = new ArrayList<>();
    private final MeteringServiceImpl meteringService;
	
	static List<ReadingTypeImpl> generate(MeteringServiceImpl meteringService) {
		return new ReadingTypeGenerator(meteringService).readingTypes();
	}
	
	private List<ReadingTypeImpl> readingTypes() {
		for (Root root : Root.values()) {
			generate(root);
		}
		return readingTypes;
	}
	
	private void generate(Root root) {
        addTheTimeAttributeRelatedReadingTypes(root);
        addTheMacroPeriodRelatedReadingTypes(root);
        String code = root.builder.period(TimeAttribute.NOTAPPLICABLE).period(MacroPeriod.NOTAPPLICABLE).accumulate(Accumulation.BULKQUANTITY).code();
        String name = root.name + " Cumulative index";
        try {
            readingTypes.add(meteringService.createReadingType(code, name));
        } catch (UnderlyingSQLFailedException e) {
            LOGGER.log(Level.FINE, "Error creating readingtype : " + code + " - " + e.getMessage(), e);
        }
    }

    private void addTheMacroPeriodRelatedReadingTypes(Root root) {
        for (int i = 0; i < macroPeriods.length; i++) {
            root.builder.period(TimeAttribute.NOTAPPLICABLE);
            String code = root.builder.period(macroPeriods[i]).accumulate(DELTADELTA).code();
            try {
                String name = macroPeriodsNames[i] + " " + root.name;
                readingTypes.add(meteringService.createReadingType(code, name));
                code = root.builder.period(macroPeriods[i]).accumulate(Accumulation.BULKQUANTITY).code();
                name = macroPeriodsNames[i] + " " + root.name + " Cumulative index";
                readingTypes.add(meteringService.createReadingType(code, name));
            } catch (UnderlyingSQLFailedException e) {
                LOGGER.log(Level.FINE, "Error creating readingtype : " + code + " - " + e.getMessage(), e);
            }
        }
    }

    private void addTheTimeAttributeRelatedReadingTypes(Root root) {
        for (int i = 0; i < timeAttributes.length; i++) {
            root.builder.period(MacroPeriod.NOTAPPLICABLE);
            String code = root.builder.period(timeAttributes[i]).accumulate(DELTADELTA).code();
            try {
                String name = timeAttributeNames[i] + " " + root.name;
                readingTypes.add(meteringService.createReadingType(code, name));
                code = root.builder.period(timeAttributes[i]).accumulate(Accumulation.BULKQUANTITY).code();
                name = timeAttributeNames[i] + " " + root.name + " Cumulative index";
                readingTypes.add(meteringService.createReadingType(code, name));
            } catch (UnderlyingSQLFailedException e) {
                LOGGER.log(Level.FINE, "Error creating readingtype : " + code + " - " + e.getMessage(), e);
            }
        }
    }
}
