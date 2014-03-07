package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.issue.mapping", service = IssueMappingService.class)
public class IssueMappingServiceImpl implements IssueMappingService {
    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile MeteringService meteringService; // Required for correct DataModel init order
    private volatile UserService userService;

    public IssueMappingServiceImpl(){}

    @Inject
    public IssueMappingServiceImpl(MeteringService meteringService, UserService userService, OrmService ormService){
        setMeteringService(meteringService);
        setUserService(userService);
        setOrmService(ormService);

        activate();
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel(IssueService.COMPONENT_NAME, "Issue Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MeteringService.class).toInstance(meteringService);
                bind(UserService.class).toInstance(userService);
                bind(OrmService.class).toInstance(ormService);
            }
        });
    }
}
