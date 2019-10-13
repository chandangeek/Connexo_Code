package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;

import aQute.bnd.annotation.ConsumerType;
import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

@ConsumerType
public interface WebServiceCallRelatedAttributesTypeProvider extends  TranslationKeyProvider {

    ImmutableMap<String,TranslationKey> getAttributeTranslations();
}
