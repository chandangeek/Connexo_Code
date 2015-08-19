package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class RegisterGroupBuilder extends NamedBuilder<RegisterGroup, RegisterGroupBuilder> {
    private final MasterDataService masterDataService;

    private List<RegisterType> registerTypes;

    public RegisterGroupBuilder withRegisterTypes(List<RegisterType> registerTypes){
        this.registerTypes = registerTypes;
        return this;
    }

    @Inject
    public RegisterGroupBuilder(MasterDataService masterDataService) {
        super(RegisterGroupBuilder.class);
        this.masterDataService = masterDataService;
    }

    @Override
    public Optional<RegisterGroup> find(){
        return masterDataService.findAllRegisterGroups().stream().filter(rg -> rg.getName().equals(getName())).findFirst();
    }

    @Override
    public RegisterGroup create(){
        Log.write(this);
        RegisterGroup registerGroup = masterDataService.newRegisterGroup(getName());
        for (RegisterType registerType : registerTypes) {
            registerGroup.addRegisterType(registerType);
        }
        registerGroup.save();
        return registerGroup;
    }
}
