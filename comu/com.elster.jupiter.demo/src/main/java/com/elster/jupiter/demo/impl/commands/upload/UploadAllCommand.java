package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class UploadAllCommand {
    private final Provider<AddIntervalChannelReadingsCommand> uploadIntervalChannelDataCommandProvider;
    private final Provider<AddNoneIntervalChannelReadingsCommand> uploadNonIntervalChannelDataCommandProvider;
    private final Provider<AddRegisterReadingsCommand> uploadRegisterDataCommandProvider;

    private Instant start;

    @Inject
    public UploadAllCommand(Provider<AddIntervalChannelReadingsCommand> uploadIntervalChannelDataCommandProvider, Provider<AddNoneIntervalChannelReadingsCommand> uploadNonIntervalChannelDataCommandProvider, Provider<AddRegisterReadingsCommand> uploadRegisterDataCommandProvider) {
        this.uploadIntervalChannelDataCommandProvider = uploadIntervalChannelDataCommandProvider;
        this.uploadNonIntervalChannelDataCommandProvider = uploadNonIntervalChannelDataCommandProvider;
        this.uploadRegisterDataCommandProvider = uploadRegisterDataCommandProvider;
    }

    public void setStartDate(Instant date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date, ZoneId.systemDefault());
        if (zonedDateTime.getDayOfMonth() != 1) {
            throw new UnableToCreate("Please specify the first day of month as a start date");
        }
        this.start = date;
    }

    public void run() {
        if (this.start == null) {
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
