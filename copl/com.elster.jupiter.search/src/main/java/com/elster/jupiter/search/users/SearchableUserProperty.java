package com.elster.jupiter.search.users;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.util.conditions.Condition;

public interface SearchableUserProperty extends SearchableProperty {

    Condition toCondition(Condition specification);

}
