/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import java.util.Locale;

public interface Translation {

    NlsKey getNlsKey();

    Locale getLocale();

    String getTranslation();
}
