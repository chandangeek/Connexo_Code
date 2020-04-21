package com.elster.jupiter.systemproperties;


import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.systemproperties.properties.CacheIsEnabledSystemPropertySpec;
import com.elster.jupiter.systemproperties.properties.EvictionTimeSystemPropertySpec;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;


@Component(name = "com.elster.jupiter.systemproperties.service",
        service = {SystemPropertyService.class},
        property = "name=" + SystemPropertiesServiceImpl.COMPONENT_NAME,
        immediate = true)
public class SystemPropertiesServiceImpl implements SystemPropertyService , TranslationKeyProvider {

    public static final String COMPONENT_NAME = "SYP";

    private volatile Thesaurus thesaurus;
    private volatile OrmService ormService;
    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile UserService userService;
    private Map<String, SystemPropertySpec> specs = new HashMap<>();
    private Map<String , String> props = new HashMap<>();


    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService= upgradeService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.SERVICE)
                .join(nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Activate
    public void activate() {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "System Property Name");

        for (SystemPropsTableSpecs spec : SystemPropsTableSpecs.values()) {
            spec.addTo(dataModel);
        }

        dataModel.register(getModule());
        upgradeService.register(identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
        initSystemPropertySpecs();
        intitSystemProperties();
        startPropertyReading();
    }

    private void initSystemPropertySpecs(){
        specs.put(CacheIsEnabledSystemPropertySpec.PROPERTY_KEY, new CacheIsEnabledSystemPropertySpec(ormService));
        specs.put(EvictionTimeSystemPropertySpec.PROPERTY_KEY, new EvictionTimeSystemPropertySpec(ormService));
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
                bind(UserService.class).toInstance(userService);
            }
        };
    }

    private void startPropertyReading(){
        Thread thread = new Thread(new ReadSystemPropertiesFromDB());
        thread.start();
    }



    private synchronized void readAndCheckPropertiesByTimeout(){
        List<SystemProperty> newprops = getAllSystemProperties();
        for(SystemProperty prop : newprops){
            if (!props.get(prop.getName()).equals(prop.getValue())){
                specs.get(prop.getName()).actionOnChange(prop);
                props.put(prop.getName() ,prop.getValue());//replace old value with new value
            }
        }

    }

    @Override
    public synchronized void actionOnPropertyChange(SystemProperty systemProperty, SystemPropertySpec spec){
        systemProperty.update();
        props.put(systemProperty.getName(), systemProperty.getValue());
        spec.actionOnChange(systemProperty);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }


    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Stream.of(Privileges.values()).forEach(keys::add);
        //Stream.of(TranslationKeys.values()).forEach(keys::add);
        return keys;
    }

    /*Separate thread is used to read system properties from DB by timeout.
    * This mechanism needed to synchronize different instances of connecxo.
    * Message queue can be used only on instances that have configured Application Server.
    * That is why such mechanism was added. But it should be improved. message queue independent from
    * Application server should be introduced. */
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
    }

}


