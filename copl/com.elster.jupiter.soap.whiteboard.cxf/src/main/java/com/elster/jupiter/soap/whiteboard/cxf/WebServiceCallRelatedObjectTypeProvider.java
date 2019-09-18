package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;

@ProviderType
public interface WebServiceCallRelatedObjectTypeProvider extends  TranslationKeyProvider {

    Map<String,TranslationKey> getTypes();
}
