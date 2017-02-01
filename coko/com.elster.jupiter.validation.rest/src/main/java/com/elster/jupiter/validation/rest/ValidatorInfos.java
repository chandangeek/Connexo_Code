/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.properties.rest.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Marked for deletion.
 * Pls use {@link com.elster.jupiter.rest.util.PagedInfoList} instead, e.g.
 * <code>PagedInfoList.fromCompleteList("validators", validatorInfoList, jsonQueryParameters)</code>.
 */
@Deprecated
@XmlRootElement
public class ValidatorInfos {

    public int total;
    public List<ValidatorInfo> validators = new ArrayList<ValidatorInfo>();

    public ValidatorInfos() {
    }

    ValidatorInfos(String implementation, String displayName, List<PropertyInfo> properties) {
        add(implementation, displayName, properties);
    }


    public ValidatorInfo add(String implementation, String displayName, List<PropertyInfo> properties) {
        ValidatorInfo result = new ValidatorInfo(implementation, displayName, properties);
        validators.add(result);
        total++;
        return result;
    }
}
