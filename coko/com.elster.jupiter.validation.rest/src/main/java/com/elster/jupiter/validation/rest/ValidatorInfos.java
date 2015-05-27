package com.elster.jupiter.validation.rest;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

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
