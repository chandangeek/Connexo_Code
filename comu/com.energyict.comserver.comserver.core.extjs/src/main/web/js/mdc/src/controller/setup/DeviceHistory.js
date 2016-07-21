Ext.define('Mdc.controller.setup.DeviceHistory', {
    extend: 'Ext.app.Controller',

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
        'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'
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

    showDeviceHistory: function (mRID) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            router = me.getController('Uni.controller.history.Router'),
            view;

        deviceModel.load(mRID, {
            success: function (device) {
                view = Ext.widget('device-history-setup', {
                    router: me.getController('Uni.controller.history.Router'),
                    device: device,
                    activeTab: (!_.isEmpty(router.queryParams) && router.queryParams.activeTab == 'meterActivations') ? 2 : 0
                });
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', view);
                me.showDeviceLifeCycleHistory();
                me.showCustomAttributeSetsHistory(mRID);
            }
        });
    },

    showDeviceLifeCycleHistory: function () {
        var me = this,
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
        lifeCycleHistoryStore.getProxy().setUrl(me.getController('Uni.controller.history.Router').arguments);
        lifeCycleHistoryStore.load(function (records) {
            lifeCycleHistoryStore.add(records.reverse());

            firmwareHistoryStore.getProxy().setUrl(me.getController('Uni.controller.history.Router').arguments);
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

    showCustomAttributeSetsHistory: function (mRID) {
        var me = this,
            customAttributesStore = me.getStore('Mdc.customattributesonvaluesobjects.store.DeviceCustomAttributeSets');

        customAttributesStore.getProxy().setUrl(mRID);

        customAttributesStore.load(function () {
            me.getPage().loadCustomAttributeSets(this);
            me.showCustomAttributeSet(mRID);
        });
    },

    showCustomAttributeSet: function(mRID) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionsStore = me.getStore('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'),
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice'),
            queryParams = router.queryParams,
            component;

        attributeSetModel.getProxy().setUrl(mRID);
        if (queryParams.customAttributeSetId) {
            versionsStore.getProxy().setUrl(mRID, queryParams.customAttributeSetId);
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
            if (tabItem.itemId !== 'device-history-firmware-tab' && tabItem.itemId !== 'device-history-life-cycle-tab') {
                router.queryParams = {};
                if (tabItem.customAttributeSetId) {
                    router.queryParams.customAttributeSetId = tabItem.customAttributeSetId;
                }
                router.getRoute().forward(null, router.queryParams);
            }
        });
    }
});
