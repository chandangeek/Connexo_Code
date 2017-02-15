/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static com.elster.jupiter.util.Checks.is;

public abstract class ReadDataFromFileCommand {
    protected static final String START_DATE_FORMAT = "yyyy-MM-dd";

    private final MeteringService meteringService;

    private Instant start;
    private InputStream source;
    private Meter meter;

    private List<ReadingType> readingTypes;

    public ReadDataFromFileCommand(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public void setStartDate(String date){
        try {
            this.start = ZonedDateTime.ofInstant(Instant.parse(date + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            throw new UnableToCreate("Unable to parse start time. Please use the following format: " + START_DATE_FORMAT);
        }
    }

    public void setStartDate(Instant date){
        this.start = date;
    }

    public void setSource(String pathToFile){
        try {
            setSource(new FileInputStream(pathToFile));
        } catch (FileNotFoundException e) {
            throw new UnableToCreate("Unable to find the specified file");
        }
    }

    public void setSource(InputStream source){
        this.source = source;
    }

    public void setMeter(String name) {
        Optional<Meter> meterRef = meteringService.findMeterByName(name);
        if (!meterRef.isPresent()) {
            throw new UnableToCreate("Unable to find meter with name " + name);
        }
        this.meter = meterRef.get();
    }

    public void setMeter(Meter meter){
        this.meter = meter;
    }

    protected void checkBeforeRun(){
        if (this.start == null){
            throw new UnableToCreate("Please specify the start time");
        }
        if (this.source == null){
            throw new UnableToCreate("Please specify the source for import");
        }
        if (this.meter == null){
            throw new UnableToCreate("Please specify the target device");
        }
    }

    public void run(){
        checkBeforeRun();
        beforeParse();
        Scanner scanner = new Scanner(this.source);
        String header = scanner.nextLine();
        parseHeader(header);
        while (scanner.hasNextLine()){
            parseRecord(scanner.nextLine());
        }
        scanner.close();
        afterParse();
    }

    protected void beforeParse(){
        // do nothing by default
    }

    protected void parseHeader(String header){
        String[] columns = header.split(";");
        if (columns.length < 2) {
            throw new UnableToCreate("You source for import has incorrect header format");
        }
        this.readingTypes = new ArrayList<>(columns.length - 1);
        for (int i = 1; i < columns.length; i++){
            Optional<ReadingType> readingTypeRef = meteringService.getReadingType(columns[i]);
            if (readingTypeRef.isPresent()){
                readingTypes.add(readingTypeRef.get());
            } else {
                throw new UnableToCreate("Unable to find the reading type with mrid = " + columns[i]);
            }
        }
        validateReadingTypes();
    }

    protected void validateReadingTypes(){
        if (readingTypes == null){
            throw new UnableToCreate("Internal error, no reading types");
        }
        TimeAttribute defaultMeasurementPeriod = null;
        for (ReadingType readingType : readingTypes) {
            TimeAttribute measuringPeriod = readingType.getMeasuringPeriod();
            if (defaultMeasurementPeriod != null && measuringPeriod != defaultMeasurementPeriod){
                throw new UnableToCreate("All reading types in file must have the same time attribute");
            }
            defaultMeasurementPeriod = measuringPeriod;
        }
    }

    protected void parseRecord(String record){
        String[] columns = record.split(";");
        String controlValue = columns[0];
        try {
            for (int i = 1; i < columns.length && i <= this.readingTypes.size() ; i++) {
                String stringValue = columns[i].replace(",", ".").replace(" ", "");
                if (!is(stringValue).emptyOrOnlyWhiteSpace()) {
                    double doubleValue = Double.valueOf(stringValue);
                    saveRecord(this.readingTypes.get(i-1), controlValue, doubleValue);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    protected abstract void saveRecord(ReadingType readingType, String controlValue, Double value);

    protected void afterParse(){
        // do nothing by default
    }

    protected List<ReadingType> getReadingTypes(){
        return this.readingTypes;
    }

    protected Instant getStart() {
        return start;
    }

    protected Meter getMeter() {
        return meter;
    }
}
