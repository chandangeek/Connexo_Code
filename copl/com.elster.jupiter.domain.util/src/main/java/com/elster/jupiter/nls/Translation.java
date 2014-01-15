package com.elster.jupiter.nls;

import java.util.Locale;

public interface Translation {

    NlsKey getNlsKey();

    Locale getLocale();

    String getTranslation();
}
