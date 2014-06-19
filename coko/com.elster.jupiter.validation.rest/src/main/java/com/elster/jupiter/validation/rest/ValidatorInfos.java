package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.rest.ValidatorInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ValidatorInfos {

    public int total;
    public List<ValidatorInfo> validators = new ArrayList<ValidatorInfo>();

    ValidatorInfos() {
    }

    ValidatorInfos(String implementation, String displayName) {
        add(implementation, displayName);
    }


    ValidatorInfo add(String implementation, String displayName) {
        ValidatorInfo result = new ValidatorInfo(implementation, displayName);
        validators.add(result);
        total++;
        return result;
    }
}
