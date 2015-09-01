package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.yellowfin.groups.AdHocDeviceGroup;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(
        name = "com.elster.jupiter.yellowfin.groups",
        service = {YellowfinGroupsService.class, InstallService.class, TranslationKeyProvider.class},
        property = "name=" + YellowfinGroupsService.COMPONENTNAME,
        immediate = true)
public class YellowfinGroupsServiceImpl implements YellowfinGroupsService, InstallService, TranslationKeyProvider {

    private static final String YELLOWFIN_LAST_DAYS = "com.elster.jupiter.yellowfin.groups.last";
    private static final int DEFAULT_LAST = 2;

    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MessageService messageService;
    private volatile TaskService taskService;
    private volatile Thesaurus thesaurus;

    private int lastDays;

    public YellowfinGroupsServiceImpl() {
        lastDays = DEFAULT_LAST;
    }

    @Inject
    public YellowfinGroupsServiceImpl(OrmService ormService, MeteringGroupsService meteringGroupsService, MessageService messageService, TaskService taskService, NlsService nlsService) {
        setOrmService(ormService);
        setNlsService(nlsService);
        setMeteringGroupsService(meteringGroupsService);
        setMessageService(messageService);
        setTaskService(taskService);
        activate(null);
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public void install() {
        dataModel.install(true, true);
        new Installer(dataModel, messageService, taskService).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME, MeteringGroupsService.COMPONENTNAME, MessageService.COMPONENTNAME, TaskService.COMPONENTNAME);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(YellowfinGroupsService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public Thesaurus thesaurus() {
        return thesaurus;
    }

    @Activate
    public void activate(BundleContext context) {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(YellowfinGroupsService.class).toInstance(YellowfinGroupsServiceImpl.this);
                    bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                    bind(DataModel.class).toInstance(dataModel);
                }
            });

            try {
                String readLastDays;
                if (context != null) {
                    readLastDays = context.getProperty(YELLOWFIN_LAST_DAYS);

                    if (readLastDays != null) {
                        lastDays = Integer.parseInt(readLastDays);
                    }
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public void deactivate() {
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "Yellowfin Metering Groups");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        this.ormService = ormService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Optional<DynamicDeviceGroupImpl> cacheDynamicDeviceGroup(String groupName) {
        Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroupByName(groupName);
        if(found.isPresent() && found.get().isDynamic()){
            QueryEndDeviceGroup group = (QueryEndDeviceGroup)found.get();

            DynamicDeviceGroupImpl cachedDeviceGroup = DynamicDeviceGroupImpl.from(dataModel, group.getId(), group.getMembers(Instant.now()));
            cachedDeviceGroup.save();
            return Optional.of(cachedDeviceGroup);
        }

        return Optional.empty();
    }

    @Override
    public Optional<AdHocDeviceGroup> cacheAdHocDeviceGroup(List<Long> devices){
        AdHocDeviceGroupImpl cachedDeviceGroup = AdHocDeviceGroupImpl.from(dataModel, 0, devices);
        cachedDeviceGroup.save();
        return Optional.of(cachedDeviceGroup);
    }

    @Override
    public void purgeAdHocSearch(){
        AdHocDeviceGroupImpl cachedDeviceGroup = AdHocDeviceGroupImpl.from(dataModel);
        cachedDeviceGroup.purgeAdHocSearch(lastDays);
    }

    @Override
    public String getComponentName() {
        return YellowfinGroupsService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(new SimpleTranslationKey(YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST, YellowfinGroupsService.ADHOC_SEARCH_LIFE_CYCLE_QUEUE_DEST_DISPLAYNAME));
    }
}
