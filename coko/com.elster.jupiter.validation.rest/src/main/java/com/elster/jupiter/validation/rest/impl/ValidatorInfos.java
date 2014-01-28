package com.elster.jupiter.validation.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ValidatorInfos {

    public int total;
    public List<ValidatorInfo> validators = new ArrayList<ValidatorInfo>();

    ValidatorInfos() {
    }

    ValidatorInfos(String implementation) {
        add(implementation);
    }


    ValidatorInfo add(String implementation) {
        ValidatorInfo result = new ValidatorInfo(implementation);
        validators.add(result);
        total++;
        return result;
    }
}
