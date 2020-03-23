/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.custom.eventhandlers.CustomSAPDeviceEventHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    SOME_DEVICES_HAVE_NONE_OF_THE_SELECTED_READINGTYPES(1027, "dataexport.device.mismatch", "Some devices of device group {0} don''t contain the selected reading type(s) that have to be exported.", Level.INFO),
    EXPORT_PERIOD_COVERS_FUTURE(1029, "dataexport.exportwindow.overlapsfuture", "The export window {0} overlaps with the future. As a result the exported data is incomplete.", Level.WARNING),
    NO_DATA_TOEXPORT(1030, "dataexport.nodata", "There is no data to export.", Level.INFO),
    ITEM_DOES_NOT_HAVE_CREATED_DATA_FOR_EXPORT_WINDOW(1052, "sap.dataexport.item.created.nodata",
            "Item {0} doesn''t contain created data, profile id isn''t set or device isn''t registered in SAP for the selected export window.", Level.INFO),
    ITEM_DOES_NOT_HAVE_CHANGED_DATA_FOR_UPDATE_WINDOW(1053, "sap.dataexport.item.changed.nodata",
            "Item {0} doesn''t contain changed data, profile id isn''t set or device isn''t registered in SAP for the selected update window.", Level.INFO);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return CustomSAPDeviceEventHandler.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }
}

