package com.energyict.mdc.engine.offline.core;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.offline.NlsServiceTanslator;
import com.energyict.mdc.engine.offline.UserEnvironment;

/**
 * Copyrights EnergyICT
 * Date: 12/02/13
 * Time: 9:05
 */
public class DefaultTranslatorProvider implements TranslatorProvider {

    private Translator translator;

    public DefaultTranslatorProvider(Thesaurus thesaurus) {
        this.translator = new NlsServiceTanslator(thesaurus);
    }

    @Override
    public Translator getTranslator() {
        return this.translator;
    }
}
