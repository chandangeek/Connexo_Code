package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.LocationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Optional;

@Component(name = "com.elster.jupiter.location",
        service = {LocationService.class},
        property = "name=" + LocationService.COMPONENT_NAME)
public class LocationServiceImpl implements LocationService {

    private volatile DataModel dataModel;

    public LocationServiceImpl(){}

    @Inject
    public LocationServiceImpl(OrmService ormService) {
        this.setOrmService(ormService);
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        //dataModel = ormService.newDataModel(MeteringService.COMPONENTNAME, "Location");
        ormService.getDataModel("MTR").ifPresent(found ->
            this.dataModel = found);
    }

    @Activate
    public final void activate() {
        System.out.println("LocationService bundle activating");
       /* dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                //bind(LocationServiceImpl.class).toInstance(LocationServiceImpl.this);
                bind(LocationService.class).toInstance(LocationServiceImpl.this);
                bind(DataModel.class).toInstance(dataModel);
            }
        }); */
    }

    @Override
    public Optional<Location> findLocationById(long id) {
        return dataModel.mapper(Location.class).getOptional(id);
    }
}
