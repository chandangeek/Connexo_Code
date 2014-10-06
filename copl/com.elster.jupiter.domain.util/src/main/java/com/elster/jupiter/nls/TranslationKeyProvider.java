package com.elster.jupiter.nls;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 6/10/2014
 * Time: 10:48
 */
public interface TranslationKeyProvider {
    String getComponentName();

    Layer getLayer();

    List<TranslationKey> getKeys();
}
