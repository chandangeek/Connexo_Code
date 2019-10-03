/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    SUSPECT_INTERVAL(1022, "dataexport.item.suspectinterval", "The interval(s) starting from {0} till {1} with suspect/not validated data are not exported for item {2}", Level.INFO),
    SUSPECT_WINDOW(1023, "dataexport.item.suspectwindow", "The export window starting from {0} till {1} with suspect/not validated data is not exported for item {2}", Level.INFO),
    SOME_DEVICES_HAVE_NONE_OF_THE_SELECTED_READINGTYPES(1027, "dataexport.device.mismatch", "Some devices of device group {0} don''t contain the selected reading type(s) that have to be exported.", Level.WARNING),
    ITEM_DOES_NOT_HAVE_DATA_FOR_EXPORT_WINDOW(1028, "dataexport.item.nodata", "Item {0} doesn''t contain data for the selected export window.", Level.WARNING),
    EXPORT_PERIOD_COVERS_FUTURE(1029, "dataexport.exportwindow.overlapsfuture", "The export window {0} overlaps with the future. As a result the exported data is incomplete.", Level.WARNING),
    NO_DATA_TOEXPORT(1030, "dataexport.nodata", "There is no data to export.", Level.INFO);

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
        return DataExportService.COMPONENTNAME;
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

