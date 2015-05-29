package com.elster.jupiter.issue.share;

import com.elster.jupiter.nls.NlsKey;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HasTranslatableNameAndProperties {
    
    String getDisplayName();
    
    String getDisplayName(String propertyName);

    NlsKey getNameNlsKey();
    
    String getNameDefaultFormat();

    NlsKey getPropertyNlsKey(String property);
    
    String getPropertyDefaultFormat(String property);
    
}
