package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.google.common.base.Strings;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.ReadingNumberPerMessageProvider",
        service = {ReadingNumberPerMessageProvider.class},
        immediate = true,
        property = {"name=" + ReadingNumberPerMessageProviderImpl.NAME})
public class ReadingNumberPerMessageProviderImpl implements ReadingNumberPerMessageProvider {
    static final String NAME = "ReadingNumberProvider";
    private static final int NUMBER_OF_READINGS_PER_MSG_DEFAULT = 500;
    private static final int MESSAGE_SIZE_DEFAULT = 512000; //500kB
    private static final int READING_SIZE_DEFAULT = 327; //bytes
    private static final int HEADER_SIZE = 1024; // bytes
    private static final String PROPERTY_READING_SIZE = "com.elster.jupiter.sap.timeseries.reading.size.in.bytes";
    private static final String PROPERTY_MSG_SIZE = "com.elster.jupiter.sap.timeseries.msg.size.in.kbytes";

    private int numberOfReadingsPerMsg;

    @Activate
    public void activate(BundleContext context) {
        String messageSizeString = context.getProperty(PROPERTY_MSG_SIZE);
        String readingSizeString = context.getProperty(PROPERTY_READING_SIZE);
        int messageSize;
        if (!Strings.isNullOrEmpty(messageSizeString)) {
            messageSize = Integer.valueOf(messageSizeString) * 1024;
        } else {
            messageSize = MESSAGE_SIZE_DEFAULT;
        }

        int readingSize;
        if (!Strings.isNullOrEmpty(readingSizeString)) {
            readingSize = Integer.valueOf(readingSizeString);
        } else {
            readingSize = READING_SIZE_DEFAULT;
        }

        numberOfReadingsPerMsg = (messageSize - HEADER_SIZE) / readingSize;

        if (numberOfReadingsPerMsg <= 0) {
            numberOfReadingsPerMsg = NUMBER_OF_READINGS_PER_MSG_DEFAULT;
        }
    }

    @Override
    public int getNumberOfReadingsPerMsg() {
        return numberOfReadingsPerMsg;
    }
}
