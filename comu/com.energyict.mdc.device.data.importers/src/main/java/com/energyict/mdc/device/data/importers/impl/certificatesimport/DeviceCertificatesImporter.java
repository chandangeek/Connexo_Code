package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.impl.MessageSeeds;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DeviceCertificatesImporter implements FileImporter {
    private final Thesaurus thesaurus;


    DeviceCertificatesImporter(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        String fileName = fileImportOccurrence.getFileName();
        String status = fileImportOccurrence.getStatusName();

        boolean fileExists = new File(fileName).exists();

        System.out.println(fileName + " " + status);

        try {
            ZipInputStream zis = new ZipInputStream(fileImportOccurrence.getContents());
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                System.out.println(zipEntry.getName() + " " + zipEntry.isDirectory());

//                deviceService.findDeviceByName(deviceName).ifPresent(device -> {

//            String fileName = zipEntry.getName();
//            File newFile = new File("c:/mytemp/unzipCertificates/" + fileName);
//            FileOutputStream fos = new FileOutputStream(newFile);
//            int len;
//            while ((len = zis.read(buffer)) > 0) {
//                fos.write(buffer, 0, len);
//            }
//            fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
//            new ExceptionLogFormatter(thesaurus, fileImportOccurrence.getLogger()).log(e);
//            throw new RuntimeException(thesaurus.getFormat(TranslationKeys.CERTIFICATE_IMPORT_FAILED).format());
            throw  new RuntimeException("invalid zip");
        }


        System.out.println("going loko");
//        try {
//            Calendars xmlContents = getXmlContents(fileImportOccurrence);
//            CalendarProcessor processor = new CalendarProcessor(calendarService, clock, thesaurus);
//            log(fileImportOccurrence, MessageSeeds.VALIDATION_OF_FILE_SUCCEEDED);
//
//            processor.addListener(new CalendarProcessor.ImportListener() {
//                @Override
//                public void created(String mrid) {
//                    logCreation(fileImportOccurrence);
//                }
//
//                @Override
//                public void updated(String mrid) {
//                    logUpdate(fileImportOccurrence);
//                }
//            });
//
//            processor.process(xmlContents);
//            markSuccess(fileImportOccurrence);
//        } catch (JAXBException e) {
//            Throwable toLog = (e.getLinkedException() != null) ? e.getLinkedException() : e;
//            String message = toLog.getLocalizedMessage();
//            if ("Content is not allowed in prolog.".equals(message)) {
//                throw new XmlValidationFailed(thesaurus, e);
//            } else {
//                throw new XmlValidationFailed(thesaurus, e, message);
//            }
//        } catch (ConstraintViolationException e) {
//            new ExceptionLogFormatter(thesaurus, fileImportOccurrence.getLogger()).log(e);
//            throw new RuntimeException(thesaurus.getFormat(TranslationKeys.CALENDAR_IMPORT_FAILED).format());
//        }
    }

//    Calendars getXmlContents(FileImportOccurrence fileImportOccurrence) throws JAXBException {
//        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCalendar.class);
//        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//        unmarshaller.setSchema(getSchema());
//        return unmarshall(unmarshaller, fileImportOccurrence.getContents());
//    }
//
//    private Calendars unmarshall(Unmarshaller unmarshaller, InputStream inputStream) throws JAXBException {
//        return (Calendars) unmarshaller.unmarshal(inputStream);
//    }

    private void logCreation(FileImportOccurrence fileImportOccurrence) {
//        log(fileImportOccurrence, MessageSeeds.CALENDAR_CREATED);
    }

    private void logUpdate(FileImportOccurrence fileImportOccurrence) {
//        log(fileImportOccurrence, MessageSeeds.CALENDAR_UPDATED);
    }

    private void log(FileImportOccurrence fileImportOccurrence, MessageSeeds messageSeeds) {
        messageSeeds.log(fileImportOccurrence.getLogger(), thesaurus);
    }

    private void markFailure(FileImportOccurrence fileImportOccurrence) {
//        fileImportOccurrence.markFailure(thesaurus.getFormat(TranslationKeys.CALENDAR_IMPORT_FAILED).format());
    }

    private void markSuccess(FileImportOccurrence fileImportOccurrence) {
//        fileImportOccurrence.markSuccess(
//                thesaurus.getFormat(TranslationKeys.CALENDAR_IMPORTED_SUCCESSFULLY).format());
    }


}
