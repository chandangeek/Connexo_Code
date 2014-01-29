package com.elster.jupiter.metering.cim;

import com.elster.jupiter.util.time.Clock;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FileOutputStreamProvider implements OutputStreamProvider {

    private final String path;
    private final String extension;
    private final Clock clock;
    private int count = 0;
    private LocalDate localDate;
    private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("yyyyMMdd");
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("000000");

    public FileOutputStreamProvider(Clock clock, String path, String extension) {
        this.path = path;
        this.clock = clock;
        this.extension = extension;
        localDate = currentLocalDate();
    }

    private LocalDate currentLocalDate() {
        return new LocalDate(clock.now(), DateTimeZone.forTimeZone(clock.getTimeZone()));
    }

    @Override
    public void writeTo(OutputStreamClosure outputStreamClosure) {
        try {
            File file = createNextFile();
            try (FileOutputStream out = new FileOutputStream(file)) {
                outputStreamClosure.using(out);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized File createNextFile() throws IOException {
        if (localDate.equals(currentLocalDate())) {
            count = 0;
            localDate = currentLocalDate();
        }
        File file = null;
        while(file == null || file.exists()) {
            file = new File(path + FORMAT.print(localDate) + NUMBER_FORMAT.format(++count) + '.' + extension);
        }
        boolean success = file.createNewFile();
        if (!success) {
            throw new RuntimeException("Could not create file " + file.getAbsolutePath());
        }
        return file;
    }
}
