package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.PropertyType;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;

@ProviderType
public interface PropertyFactory {

    PropertySpec getElement(String name, TranslationKey displayName, TranslationKey description);

    PropertyType getType();

}