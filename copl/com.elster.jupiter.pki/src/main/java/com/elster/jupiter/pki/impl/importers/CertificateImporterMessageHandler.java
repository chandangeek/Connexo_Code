package com.elster.jupiter.pki.impl.importers;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.pki.impl.importers." + CertificateImporterMessageHandler.COMPONENT_NAME,
        service = MessageHandlerFactory.class,
        property = {
                "name=" + CertificateImporterMessageHandler.COMPONENT_NAME,
                "subscriber=" + CertificateImporterMessageHandler.SUBSCRIBER_NAME,
                "destination=" + CertificateImporterMessageHandler.DESTINATION_NAME},
        immediate = true)
public class CertificateImporterMessageHandler implements MessageHandlerFactory {
    public static final String COMPONENT_NAME = "PKI";

    public static final String DESTINATION_NAME = "CertificateFileImport";
    public static final String SUBSCRIBER_NAME = "CertificateFileImport";


    private volatile FileImportService fileImportService;

    public CertificateImporterMessageHandler() {
    }

    @Inject
    public CertificateImporterMessageHandler(FileImportService fileImportService) {
        this();
        setFileImportService(fileImportService);
    }

    @Reference
    public final void setFileImportService(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return fileImportService.createMessageHandler();
    }
}
