package com.elster.us.protocolimplv2.mercury.minimax.utility;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class related to date conversions
 *
 * @author James Fox
 */
public final class DateFormatHelper {
    private static Map<String, SimpleDateFormat> formatters = new HashMap<String,SimpleDateFormat>();
    private static Map<String, SimpleDateFormat> eventFormatters = new HashMap<String,SimpleDateFormat>();

    private static SimpleDateFormat DEFAULT = new SimpleDateFormat(MMDDYY);
    private static SimpleDateFormat DEFAULT_EVENT = new SimpleDateFormat(MMDDYY_EVENT);

    private DateFormatHelper() {}

    static {
        // 0, 1 and 2 are defined in the protocol spec as corresponding
        // to these date formats
        formatters.put("0", new SimpleDateFormat(MMDDYY));
        formatters.put("1", new SimpleDateFormat(DDMMYY));
        formatters.put("2", new SimpleDateFormat(YYMMDD));

        eventFormatters.put("0", new SimpleDateFormat(MMDDYY_EVENT));
        eventFormatters.put("1", new SimpleDateFormat(DDMMYY_EVENT));
        eventFormatters.put("2", new SimpleDateFormat(YYMMDD_EVENI));
    }

    /**
     * Get the date formatter associated with the key
     * @param key this should be 0, 1 or 2 (from the spec)
     * @return the correct formatter if the key is valid, otherwise default of MM-dd-yy
     */
    public static SimpleDateFormat get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (formatters.containsKey(key)) {
            return formatters.get(key);
        }
        return DEFAULT;
    }

    /**
     * Get the date formatter associated with the key
     * @param key this should be 0, 1 or 2 (from the spec)
     * @return the correct formatter if the key is valid, otherwise default of MM-dd-yy
     */
    public static SimpleDateFormat getForEvent(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (eventFormatters.containsKey(key)) {
            return eventFormatters.get(key);
        }
        return DEFAULT_EVENT;
    }
}
