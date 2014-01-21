package com.elster.jupiter.nls;

import java.util.Locale;

public interface NlsMessageFormat {

    String format(Object... args);

    String format(Locale locale, Object... args);
}
