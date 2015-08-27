package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class EstimatorInfos {

    public int total;
    public List<EstimationInfo> estimators = new ArrayList<EstimationInfo>();

    EstimatorInfos() {
    }

    EstimatorInfos(String implementation, String displayName, List<PropertyInfo> properties) {
        add(implementation, displayName, properties);
    }


    EstimationInfo add(String implementation, String displayName, List<PropertyInfo> properties) {
        EstimationInfo result = new EstimationInfo(implementation, displayName, properties);
        estimators.add(result);
        total++;
        return result;
    }
}
