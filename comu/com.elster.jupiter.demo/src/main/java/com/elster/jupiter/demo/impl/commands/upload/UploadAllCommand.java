package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class UploadAllCommand {
    protected static final String START_DATE_FORMAT = "yyyy-MM-ddTHH:mm:00Z";

    private final Provider<UploadIntervalChannelDataCommand> uploadIntervalChannelDataCommandProvider;
    private final Provider<UploadNonIntervalChannelDataCommand> uploadNonIntervalChannelDataCommandProvider;
    private final Provider<UploadRegisterDataCommand> uploadRegisterDataCommandProvider;

    private Instant start;

    @Inject
    public UploadAllCommand(Provider<UploadIntervalChannelDataCommand> uploadIntervalChannelDataCommandProvider, Provider<UploadNonIntervalChannelDataCommand> uploadNonIntervalChannelDataCommandProvider, Provider<UploadRegisterDataCommand> uploadRegisterDataCommandProvider) {
        this.uploadIntervalChannelDataCommandProvider = uploadIntervalChannelDataCommandProvider;
        this.uploadNonIntervalChannelDataCommandProvider = uploadNonIntervalChannelDataCommandProvider;
        this.uploadRegisterDataCommandProvider = uploadRegisterDataCommandProvider;
    }

    public void setStartDate(String date){
        try {
            this.start = ZonedDateTime.ofInstant(Instant.parse(date), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            throw new UnableToCreate("Unable to parse start time. Please use the following format: " + START_DATE_FORMAT);
        }
    }

    public void run(){
        if (this.start == null){
            throw new UnableToCreate("You must set start date for data");
        }

        uploadData(Constants.Device.MOCKED_REALISTIC_DEVICE + Constants.Device.MOCKED_REALISTIC_SERIAL_NUMBER, "realisticChannelData - Interval.csv", uploadIntervalChannelDataCommandProvider.get());
        uploadData(Constants.Device.MOCKED_REALISTIC_DEVICE + Constants.Device.MOCKED_REALISTIC_SERIAL_NUMBER, "realisticChannelData - Daily.csv", uploadNonIntervalChannelDataCommandProvider.get());
        uploadData(Constants.Device.MOCKED_REALISTIC_DEVICE + Constants.Device.MOCKED_REALISTIC_SERIAL_NUMBER, "realisticChannelData - Monthly.csv", uploadNonIntervalChannelDataCommandProvider.get());
        uploadData(Constants.Device.MOCKED_REALISTIC_DEVICE + Constants.Device.MOCKED_REALISTIC_SERIAL_NUMBER, "realisticRegisterData.csv", uploadRegisterDataCommandProvider.get());

        uploadData(Constants.Device.MOCKED_VALIDATION_DEVICE + Constants.Device.MOCKED_VALIDATION_SERIAL_NUMBER, "realisticChannelData - Interval - Validation.csv", uploadIntervalChannelDataCommandProvider.get());
        uploadData(Constants.Device.MOCKED_VALIDATION_DEVICE + Constants.Device.MOCKED_VALIDATION_SERIAL_NUMBER, "realisticChannelData - Daily - Validation.csv", uploadNonIntervalChannelDataCommandProvider.get());
        uploadData(Constants.Device.MOCKED_VALIDATION_DEVICE + Constants.Device.MOCKED_VALIDATION_SERIAL_NUMBER, "realisticChannelData - Monthly.csv", uploadNonIntervalChannelDataCommandProvider.get());
        uploadData(Constants.Device.MOCKED_VALIDATION_DEVICE + Constants.Device.MOCKED_VALIDATION_SERIAL_NUMBER, "realisticRegisterData - Validation.csv", uploadRegisterDataCommandProvider.get());
    }

    private void uploadData(String meter, String source, ReadDataFromFileCommand command) {
        command.setStartDate(start);
        command.setMeter(meter);
        command.setSource(getResourceAsStream(source));
        command.run();
    }

    private InputStream getResourceAsStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }
}
