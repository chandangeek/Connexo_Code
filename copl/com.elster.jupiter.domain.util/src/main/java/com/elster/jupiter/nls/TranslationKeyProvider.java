/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import java.util.List;

public interface TranslationKeyProvider {
    String getComponentName();

    Layer getLayer();

    List<TranslationKey> getKeys();
}
