package com.elster.jupiter.orm;

import com.elster.jupiter.util.Registration;

import java.util.List;

public interface DataModelDifferencesLister {

    List<Difference> findDifferences();

    Registration register(DifferencesListener listener);

}
