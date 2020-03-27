package com.elster.jupiter.systemproperties;


import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.systemproperties.properties.CacheIsEnabledSystemPropertySpec;
import com.elster.jupiter.systemproperties.properties.EvictionTimeSystemPropertySpec;

import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Component(name = "com.elster.jupiter.systemproperties.service",
        service = {SystemPropertyService.class},
        property = "name=" + SystemPropertiesServiceImpl.COMPONENT_NAME,
        immediate = true)
public class SystemPropertiesServiceImpl implements SystemPropertyService{

    public static final String COMPONENT_NAME = "SYP";

    private volatile Thesaurus thesaurus;
    private volatile OrmService ormService;
    private volatile DataModel dataModel;
    private Map<String, SystemPropertySpec> specs = new HashMap<>();
    private Map<String , String> props = new HashMap<>();

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.SERVICE)
                .join(nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN));
    }

    @Activate
    public void activate() {
        System.out.println("ACTIVATE SYSTEM PROPERTY SERVICE!!!!!");
        dataModel = ormService.newDataModel(COMPONENT_NAME, "System Property Name");

        for (
            SystemPropsTableSpecs spec : SystemPropsTableSpecs.values()) {
            System.out.println("ADD");
            spec.addTo(dataModel);
        }
        System.out.println("REGISTER!!!!");
        dataModel.register(getModule());
        //dataModel.register();
        initSystemPropertySpecs();
        intitSystemProperties();
        startPropertyReading();
        System.out.println("ACTIVATION IS DONE!!!!!!!!!!!!");
    }

    private void initSystemPropertySpecs(){
        specs.put(CacheIsEnabledSystemPropertySpec.PROPERTY_NAME, new CacheIsEnabledSystemPropertySpec(ormService));
        specs.put(EvictionTimeSystemPropertySpec.PROPERTY_NAME, new EvictionTimeSystemPropertySpec(ormService));
    }

    private void intitSystemProperties(){
        props = getAllSystemProperties().stream().collect(
                Collectors.toMap(x -> x.getName(), x -> x.getValue()));
    }

    public Optional<SystemPropertySpec>getPropertySpec(String key){
        SystemPropertySpec spec = specs.get(key);
        return spec == null ? Optional.empty(): Optional.of(spec);
    }

    public List<SystemProperty> getAllSystemProperties(){
        List<SystemProperty> lst = dataModel.mapper(SystemProperty.class).find();
        return lst;
    };
    //SystemProperty getSystemPropertiesById();//Needed?
    public Optional<SystemProperty> getSystemPropertiesByName(String name){

        Optional<SystemProperty> property = dataModel.mapper(SystemProperty.class)
                .getUnique("propertyName", name);

        return property;
    };

    public DataModel getDataModel(){
        return dataModel;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        };
    }

    private void startPropertyReading(){
        System.out.println("START PROPERTY READING !!!!!!!!!!");
        Thread thread = new Thread(new ReadSystemPropertiesFromDB());
        thread.start();
    }

    private class ReadSystemPropertiesFromDB implements Runnable{

        @Override
        public void run(){
            while(true) {
                try {
                    Thread.sleep(20000);
                    readAndCheckPropertiesByTimeout();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        private void readAndCheckPropertiesByTimeout(){
            List<SystemProperty> newprops = getAllSystemProperties();
            for(SystemProperty prop : newprops){
                System.out.println("PROP NAME = "+prop.getName()+ " PROP VALUE ="+prop.getValue());
                if (!props.get(prop.getName()).equals(prop.getValue())){
                    System.out.println("PROPERTY WAS CHANGED");
                    specs.get(prop.getName()).actionOnChange(prop);
                    props.put(prop.getName() ,prop.getValue());//replace with new value
                }
            }

        }
    }

}


