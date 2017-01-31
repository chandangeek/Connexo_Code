/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.device.data.Register;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class RegisterInfo  {

    public int id;
    public String name;

    public RegisterInfo() {
    }

    public RegisterInfo(Map<String, Object> map) {
        this.id = (int) map.get("id");
        this.name = (String) map.get("name");
    }

    public RegisterInfo(Register register) {
        id = (int)register.getRegisterSpecId();
        name = register.getRegisterSpec().getRegisterType().getReadingType().getAliasName();
    }

}
