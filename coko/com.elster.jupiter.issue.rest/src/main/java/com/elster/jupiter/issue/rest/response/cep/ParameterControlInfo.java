package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.ParameterControl;

public class ParameterControlInfo {
    private String alias;

    public ParameterControlInfo(ParameterControl control) {
        if (control != null) {
            this.alias = control.getAlias();
        }
    }

    public String getAlias() {
        return alias;
    }
}
