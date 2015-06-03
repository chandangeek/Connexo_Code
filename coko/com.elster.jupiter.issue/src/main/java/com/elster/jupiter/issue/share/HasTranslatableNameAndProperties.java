package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.nls.NlsKey;

@ConsumerType
public interface HasTranslatableNameAndProperties {
    
    String getDisplayName();

    NlsKey getNameNlsKey();
    
    String getNameDefaultFormat();
    
    String getDisplayName(String property);

    NlsKey getPropertyNlsKey(String property);
    
    String getPropertyDefaultFormat(String property);
    
}
