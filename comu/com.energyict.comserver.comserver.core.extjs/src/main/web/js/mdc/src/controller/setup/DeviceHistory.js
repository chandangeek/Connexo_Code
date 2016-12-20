Ext.define('Mdc.controller.setup.DeviceHistory', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.util.Common'
    ],

    views: [
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.devicehistory.Setup',
        'Mdc.view.setup.devicehistory.LifeCycle',
        'Mdc.view.setup.devicehistory.Firmware',
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnDevice'
    ],

    stores: [
        'Mdc.store.DeviceLifeCycleStatesHistory',
        'Mdc.store.DeviceFirmwareHistory',
        'Mdc.customattributesonvaluesobjects.store.DeviceCustomAttributeSets',
        'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice',
        'Mdc.store.device.MeterActivations'
    ],

    models: [
        'Mdc.model.DeviceLifeCycleStatesHistory',
        'Mdc.model.DeviceFirmwareHistory',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice',
        'Mdc.model.Device'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-history-setup'
        },
        {
            ref: 'tabPanel',
            selector: 'device-history-setup tabpanel'
        }
    ],

    showDeviceHistory: function (deviceId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            router = me.getController('Uni.controller.history.Router'),
            view;

        deviceModel.load(deviceId, {
            success: function (device) {
                view = Ext.widget('device-history-setup', {
                    router: me.getController('Uni.controller.history.Router'),
                    device: device,
                    controller: me,
                    activeTab: (!_.isEmpty(router.queryParams) && router.queryParams.activeTab == 'meterActivations') ? 2 : 0
                });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', view);
                me.showDeviceLifeCycleHistory();
                me.showCustomAttributeSetsHistory(deviceId);
            }
        });
    },

    showDeviceLifeCycleHistory: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routerArguments = Uni.util.Common.decodeURIArguments(router.arguments),
            lifeCycleTab = me.getPage().down('#device-history-life-cycle-tab'),
            lifeCyclePanel = Ext.widget('device-history-life-cycle-panel'),
            lifeCycleDataView = lifeCyclePanel.down('#life-cycle-data-view'),
            lifeCycleHistoryStore = me.getStore('Mdc.store.DeviceLifeCycleStatesHistory'),
            firmwareTab = me.getPage().down('#device-history-firmware-tab'),
            firmwareHistoryPanel = Ext.widget('device-history-firmware-panel'),
            firmwareHistoryStore = me.getStore('Mdc.store.DeviceFirmwareHistory');

        me.getPage().setLoading();
        lifeCycleTab.add(lifeCyclePanel);
        lifeCycleDataView.bindStore(lifeCycleHistoryStore);
        Ext.apply(lifeCycleHistoryStore.getProxy().extraParams, routerArguments);
        lifeCycleHistoryStore.load(function (records) {
            lifeCycleHistoryStore.add(records.reverse());

            Ext.apply(firmwareHistoryStore.getProxy().extraParams, routerArguments);
            firmwareHistoryStore.load(function() {
                if (firmwareHistoryStore.getTotalCount()===0) {
                    firmwareTab.add({
                        xtype: 'form',
                        items: {
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('general.noFirmwareHistory', 'MDC', 'No firmware history available')
                        }
                    });
                } else {
                    firmwareTab.add(firmwareHistoryPanel);
                }
                me.getPage().setLoading(false);
            });
        });
    },

    showCustomAttributeSetsHistory: function (deviceId) {
        var me = this,
            customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.DeviceCustomAttributeSets');

        customAttributesStore.getProxy().setExtraParam('deviceId', deviceId);

        customAttributesStore.load(function () {
            me.getPage().loadCustomAttributeSets(this);
            me.showCustomAttributeSet(deviceId);
        });
    },

    showMeterActivations: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = Ext.getStore('Mdc.store.device.MeterActivations');

        store.getProxy().setExtraParam('deviceId', decodeURIComponent(router.arguments.deviceId));
        store.load();
    },

    showCustomAttributeSet: function(deviceId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionsStore = me.getStore('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'),
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice'),
            queryParams = router.queryParams,
            component;

        attributeSetModel.getProxy().setExtraParam('deviceId', deviceId);
        if (queryParams.customAttributeSetId) {
            versionsStore.getProxy().setParams(deviceId, queryParams.customAttributeSetId);
            component = me.getTabPanel().down('#custom-attribute-set-' + queryParams.customAttributeSetId);
            component.add({
                xtype: 'device-history-custom-attribute-sets-versions'
            });
            me.getTabPanel().setActiveTab(component);
            attributeSetModel.load(queryParams.customAttributeSetId, {
                success: function (customattributeset) {
                    component.down('#custom-attribute-set-add-version-btn').setVisible(customattributeset.get('editable'));
                    component.down('#custom-attribute-set-versions-grid-action-column').setVisible(customattributeset.get('editable'));
                    component.down('#custom-attribute-set-add-version-btn-top').setVisible(customattributeset.get('editable'));
                }
            });
        }

        me.getTabPanel().on('tabchange', function(tabpanel, tabItem) {
            if (tabItem.itemId !== 'device-history-firmware-tab'
                && tabItem.itemId !== 'device-history-life-cycle-tab'
                && tabItem.itemId !== 'device-history-meter-activations-tab') {
                router.queryParams = {};
                if (tabItem.customAttributeSetId) {
                    router.queryParams.customAttributeSetId = tabItem.customAttributeSetId;
                }
                router.getRoute().forward(null, router.queryParams);
            }
        });
    }
});
