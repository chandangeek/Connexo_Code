package com.energyict.mdc.common;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 * Date: 12/02/13
 * Time: 9:01
 */
public interface TranslatorProvider {

    AtomicReference<TranslatorProvider> instance = new AtomicReference<>();

    Translator getTranslator();

}
