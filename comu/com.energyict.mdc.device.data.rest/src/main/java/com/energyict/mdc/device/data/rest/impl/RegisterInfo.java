package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.data.Register;
import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegisterInfo extends RegisterConfigInfo {
    @JsonProperty("lastReading")
    public Long lastReading;

    public RegisterInfo() {}

    public RegisterInfo(Register register) {
        super(register.getRegisterSpec());
//        Optional<Date> lastReading = register.getLastReadingDate();
//        if(lastReading.isPresent()) {
//            this.lastReading = lastReading.get().getTime();
//        }
    }

    public static RegisterInfo from(Register register) {
        return new RegisterInfo(register);
    }

    public static List<RegisterInfo> fromList(List<Register> registerList) {
        List<RegisterInfo> registerInfos = new ArrayList<>(registerList.size());
        for(Register register : registerList) {
            registerInfos.add(RegisterInfo.from(register));
        }
        return registerInfos;
    }
}
