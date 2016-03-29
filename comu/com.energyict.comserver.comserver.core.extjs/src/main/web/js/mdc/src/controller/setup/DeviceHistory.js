Ext.define('Mdc.controller.setup.DeviceHistory', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicehistory.Setup',
        'Mdc.view.setup.devicehistory.LifeCycle',
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnDevice'
    ],

    stores: [
        'Mdc.store.DeviceLifeCycleStatesHistory',
        'Mdc.customattributesonvaluesobjects.store.DeviceCustomAttributeSets',
        'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'
    ],

    models: [
        'Mdc.model.DeviceLifeCycleStatesHistory',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice',
        'Mdc.model.Device'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'device-history-setup'
        }
    ],

    showDeviceHistory: function (mRID) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            view;

        deviceModel.load(mRID, {
            success: function (device) {
                view = Ext.widget('device-history-setup', {
                    router: me.getController('Uni.controller.history.Router'),
                    device: device
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
            historyPanel = me.getPage().down('#device-history-life-cycle-tab'),
            lifeCyclePanel = Ext.widget('device-history-life-cycle-panel'),
            lifeCycleDataView = lifeCyclePanel.down('#life-cycle-data-view'),
            store = me.getStore('Mdc.store.DeviceLifeCycleStatesHistory');

        me.getPage().setLoading();
        historyPanel.add(lifeCyclePanel);
        lifeCycleDataView.bindStore(store);
        store.getProxy().setUrl(me.getController('Uni.controller.history.Router').arguments);
        store.load(function (records) {
            store.add(records.reverse());
            me.getPage().setLoading(false);
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
            tabpanel = me.getPage().down('tabpanel'),
            versionsStore = me.getStore('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'),
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice'),
            queryParams = router.queryParams,
            component;

        attributeSetModel.getProxy().setUrl(mRID);
        if (queryParams.customAttributeSetId) {
            versionsStore.getProxy().setUrl(mRID, queryParams.customAttributeSetId);
            component = tabpanel.down('#custom-attribute-set-' + queryParams.customAttributeSetId);
            component.add({
                xtype: 'device-history-custom-attribute-sets-versions'
            });
            tabpanel.setActiveTab(component);
            attributeSetModel.load(queryParams.customAttributeSetId, {
                success: function (customattributeset) {
                    component.down('#custom-attribute-set-add-version-btn').setVisible(customattributeset.get('editable'));
                    component.down('#custom-attribute-set-versions-grid-action-column').setVisible(customattributeset.get('editable'));
                    component.down('#custom-attribute-set-add-version-btn-top').setVisible(customattributeset.get('editable'));
                }
            });
        }
        tabpanel.on('tabchange', function(tabpanel, tabItem) {
            router.queryParams = {};
            if (tabItem.customAttributeSetId) {
                router.queryParams.customAttributeSetId = tabItem.customAttributeSetId;
            }
            router.getRoute().forward(null, router.queryParams);
        });
    }
});
