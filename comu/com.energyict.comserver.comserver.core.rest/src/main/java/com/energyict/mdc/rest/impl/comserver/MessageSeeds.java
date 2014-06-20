package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.rest.impl.MdcApplication;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    MILLISECONDS(1, "milliseconds", "Milliseconds", Layer.UI),
    SECONDS(2, "seconds", "Seconds", Layer.UI),
    MINUTES(3, "minutes", "Minutes", Layer.UI),
    HOURS(4, "hours", "Hours", Layer.UI),
    DAYS(5, "days", "Days", Layer.UI),
    WEEKS(6, "weeks", "Weeks", Layer.UI),
    MONTHS(7, "months", "Months", Layer.UI),
    YEARS(8, "years", "Years", Layer.UI)
    ;

    private final int number;
    private final String key;
    private final String format;
    private final Layer layer;

    private MessageSeeds(int number, String key, String format) {
        this(number, key, format, Layer.REST);
    }

    private MessageSeeds(int number, String key, String format, Layer layer) {
        this.number = number;
        this.key = key;
        this.format = format;
        this.layer = layer;
    }

    @Override
    public String getModule() {
        return MdcApplication.COMPONENT_NAME;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public Layer getLayer() {
        return layer;
    }
}
