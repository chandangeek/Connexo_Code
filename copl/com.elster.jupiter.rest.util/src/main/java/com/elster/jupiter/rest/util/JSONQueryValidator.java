package com.elster.jupiter.rest.util;

import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.impl.MessageSeeds;
import com.elster.jupiter.util.PathVerification;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;
import java.util.Optional;

/**
 * Validate JSONQueryParameters
 * @author M R
 * @since 21-Sep-2020
 */

public class JSONQueryValidator {
    public static void validateJSONQueryParameters(JsonQueryParameters jsonQueryParameters) {
        String like = jsonQueryParameters.getLike();
        if(null != null) {
             if(!PathVerification.validateInputPattern(like, HasNotAllowedChars.Constant.SPECIAL_CHARS)){
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_INPUT_VALUE, "filter");
                }
                final Optional<Integer> limit = jsonQueryParameters.getLimit();
                if(null != limit && limit.isPresent() && limit.get() > 1 && limit.get() < 999) {
                    throw new LocalizedFieldValidationException(MessageSeeds.INVALID_INPUT_VALUE, "filter");
                }
                final List<Order> sortingColumns = jsonQueryParameters.getSortingColumns();
                for(Order item: sortingColumns) {
                    if(!PathVerification.validateInputPattern(item.getName(), HasNotAllowedChars.Constant.SPECIAL_CHARS)){
                        throw new LocalizedFieldValidationException(MessageSeeds.INVALID_INPUT_VALUE, "filter");
                    }
                }
        }
    }
}
