package com.energyict.mdc.sap.soap.webservices.impl;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ReadingNumberPerMessageProvider {
    int getNumberOfReadingsPerMsg();
}
