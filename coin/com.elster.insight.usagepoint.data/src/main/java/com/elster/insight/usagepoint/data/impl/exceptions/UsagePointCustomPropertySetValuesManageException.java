package com.elster.insight.usagepoint.data.impl.exceptions;

import com.elster.insight.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointCustomPropertySetValuesManageException extends LocalizedException {

    private UsagePointCustomPropertySetValuesManageException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointCustomPropertySetValuesManageException customPropertySetIsNotEditableByUser(Thesaurus thesaurus, String cpsName){
        return new UsagePointCustomPropertySetValuesManageException(thesaurus,
                MessageSeeds.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, cpsName);
    }

    public static UsagePointCustomPropertySetValuesManageException noLinkedCustomPropertySet(Thesaurus thesaurus, String cpsName){
        return new UsagePointCustomPropertySetValuesManageException(thesaurus,
                MessageSeeds.NO_LINKED_CUSTOM_PROPERTY_SET_ON_USAGE_POINT, cpsName);
    }

    public static UsagePointCustomPropertySetValuesManageException badDomainType(Thesaurus thesaurus, String cpsName){
        return new UsagePointCustomPropertySetValuesManageException(thesaurus,
                MessageSeeds.CUSTOM_PROPERTY_SET_HAS_DIFFERENT_DOMAIN, cpsName);
    }
}
