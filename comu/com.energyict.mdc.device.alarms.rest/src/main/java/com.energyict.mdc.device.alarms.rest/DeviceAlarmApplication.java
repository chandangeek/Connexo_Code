package com.energyict.mdc.device.alarms.rest;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.rest.i18n.DeviceAlarmTranslationKeys;
import com.energyict.mdc.device.alarms.rest.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.rest.resource.DeviceAlarmResource;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmInfoFactory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component(name = "com.energyict.mdc.device.com.energyict.mdc.device.alarms.rest", service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/dal", "app=MDC", "name=" + DeviceAlarmApplication.DEVICE_ALARMS_REST_COMPONENT})
public class DeviceAlarmApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {

    public static final String APP_KEY = "MDC";
    public static final String DEVICE_ALARMS_REST_COMPONENT = "DAR";

    private volatile TransactionService transactionService;
    private volatile DeviceAlarmService deviceAlarmService;
    private volatile DeviceService deviceService;
    private volatile LogBookService logBookService;
    private volatile IssueService issueService;

    public DeviceAlarmApplication(){

    }

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                DeviceAlarmResource.class);
    }

    @Override
    public String getComponentName() {
        return DEVICE_ALARMS_REST_COMPONENT;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(DeviceAlarmTranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setLogBookService(LogBookService logBookService){
        this.logBookService = logBookService;
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setIssueService(IssueService issueService){
        this.issueService = issueService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(transactionService).to(TransactionService.class);
            bind(deviceAlarmService).to(DeviceAlarmService.class);
            bind(deviceService).to(DeviceService.class);
            bind(DeviceAlarmInfoFactory.class).to(DeviceAlarmInfoFactory.class);
            bind(logBookService).to(LogBookService.class);
            bind(issueService).to(IssueService.class);
        }
    }
}
