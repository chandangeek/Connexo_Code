/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Thesaurus;

import java.util.Locale;

public interface IThesaurus extends Thesaurus {

    String getComponent();

    Locale getLocale();

    void invalidate();

}
