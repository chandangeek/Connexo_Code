package com.elster.jupiter.nls;

import aQute.bnd.annotation.ProviderType;

import javax.validation.ConstraintViolation;
import java.util.Locale;
import java.util.function.Function;

@ProviderType
public interface NlsService {

    String COMPONENTNAME = "NLS";

    Thesaurus getThesaurus(String componentName, Layer layer);

    PrivilegeThesaurus getPrivilegeThesaurus();

    TranslationBuilder translate(NlsKey key);

    String interpolate(ConstraintViolation<?> violation);

    /**
     * Copies the specified {@link NlsKey} into this Thesaurus,
     * mapping the key the result of the mapping function.
     *
     * @param key The NlsKey
     * @param targetComponent The target component
     * @param targetLayer the target layer
     * @param keyMapper The key mapping function
     */
    void copy(NlsKey key, String targetComponent, Layer targetLayer, Function<String, String> keyMapper);

    @ProviderType
    interface TranslationBuilder {
        TranslationBuilder to(Locale locale, String translation);

        void add();
    }
}
