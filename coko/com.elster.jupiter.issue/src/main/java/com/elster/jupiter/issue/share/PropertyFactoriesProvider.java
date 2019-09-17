package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.PropertyType;

@ProviderType
public interface PropertyFactoriesProvider {

    String getId();

    PropertyFactory getFactory(PropertyType type);

}