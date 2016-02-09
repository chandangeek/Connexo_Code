package com.elster.insight.usagepoint.data.impl.exceptions;

import com.elster.insight.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsagePointCustomPropertySetValuesManageException extends LocalizedException {

    private UsagePointCustomPropertySetValuesManageException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointCustomPropertySetValuesManageException noLinkedCustomPropertySetOnMetrologyConfiguration(Thesaurus thesaurus, Object... args){
        return new UsagePointCustomPropertySetValuesManageException(thesaurus,
                MessageSeeds.NO_LINKED_CUSTOM_PROPERTY_SET_ON_METROLOGY_CONFIGURATION, args);
    }

    public static UsagePointCustomPropertySetValuesManageException customPropertySetIsNotEditableByUser(Thesaurus thesaurus, Object... args){
        return new UsagePointCustomPropertySetValuesManageException(thesaurus,
                MessageSeeds.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, args);
    }

    public static UsagePointCustomPropertySetValuesManageException noLinkedMetrologyConfiguration(Thesaurus thesaurus, Object... args){
        return new UsagePointCustomPropertySetValuesManageException(thesaurus,
                MessageSeeds.NO_LINKED_METROLOGY_CONFIGURATION, args);
    }

    public static UsagePointCustomPropertySetValuesManageException noLinkedCustomPropertySetOnServiceCategory(Thesaurus thesaurus, Object... args){
        return new UsagePointCustomPropertySetValuesManageException(thesaurus,
                MessageSeeds.NO_LINKED_CUSTOM_PROPERTY_SET_ON_SERVICE_CATEGORY, args);
    }
}
