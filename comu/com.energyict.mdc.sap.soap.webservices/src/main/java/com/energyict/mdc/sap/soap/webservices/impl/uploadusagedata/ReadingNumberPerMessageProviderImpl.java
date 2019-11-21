package com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata;

import com.energyict.mdc.sap.soap.webservices.impl.ReadingNumberPerMessageProvider;

import com.google.common.base.Strings;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.uploadusagedata.ReadingNumberPerMessageProvider",
        service = {ReadingNumberPerMessageProvider.class},
        immediate = true,
        property = {"name=" + ReadingNumberPerMessageProviderImpl.NAME})
public class ReadingNumberPerMessageProviderImpl implements ReadingNumberPerMessageProvider{

    static final String NAME = "ReadingNumberProvider";

    private int numberOfReadingsPerMsg;
    private final int NUMBER_OF_READINGS_PER_MSG_DEFAULT = 500;
    private int MESSAGE_SIZE;
    private int MESSAGE_SIZE_DEFAULT = 512000;//500kB
    private int READING_SIZE; //bytes
    private final int READING_SIZE_DEFAULT = 327; //bytes
    private final String PROPERTY_READING_SIZE = "com.elster.jupiter.sap.timeseries.reading.size.in.bytes";
    private final String PROPERTY_MSG_SIZE = "com.elster.jupiter.sap.timeseries.msg.size.in.kbytes";
    private final int HEADER_SIZE = 1024; // bytes

    @Activate
    public void activate(BundleContext context) {
        initNumberOfReadingsPerMsg(context);
    }

    protected void initNumberOfReadingsPerMsg(BundleContext bundleContext){
        String msgSize = bundleContext.getProperty(PROPERTY_MSG_SIZE);
        String readingSize = bundleContext.getProperty(PROPERTY_READING_SIZE);
        if (!Strings.isNullOrEmpty(msgSize)) {
            MESSAGE_SIZE = Integer.valueOf(msgSize)*1024;
        }else{
            MESSAGE_SIZE = MESSAGE_SIZE_DEFAULT;
        }

        if (!Strings.isNullOrEmpty(readingSize)) {
            READING_SIZE = Integer.valueOf(readingSize);
        }else{
            READING_SIZE = READING_SIZE_DEFAULT;
        }

        numberOfReadingsPerMsg = (MESSAGE_SIZE - HEADER_SIZE)/READING_SIZE;

        if (numberOfReadingsPerMsg <= 0)
            numberOfReadingsPerMsg = NUMBER_OF_READINGS_PER_MSG_DEFAULT;
    }

    @Override
    public int getNumberOfReadingsPerMsg(){
        return numberOfReadingsPerMsg;
    }

}

