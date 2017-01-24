Ext.define('Mdc.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnDevice', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionForm'
    ],

    requires: [
        'Mdc.model.Device',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnDevice',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'
    ],

    stores: [
        'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice'
    ],

    refs: [],

    editCustomAttributeVersion: function (deviceId, customAttributeSetId, versionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnDevice'),
            overlapStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionModel.getProxy().setParams(deviceId, customAttributeSetId);
        overlapStore.getProxy().setDeviceEditUrl(deviceId, customAttributeSetId, versionId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'device',
            pageType: 'edit'
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        me.loadDeviceModel(deviceId);
        me.loadAttributeSetModel(deviceId, customAttributeSetId);

        versionModel.load(versionId, {
            success: function(record) {
                Ext.suspendLayouts();
                me.getApplication().fireEvent('loadCustomAttributeSetVersionOnDevice', record);
                widget.down('form').setTitle(router.getRoute().getTitle());
                widget.loadRecord(record, record, record.get('startTime'), record.get('endTime'));
                widget.setLoading(false);
                Ext.resumeLayouts(true);
            }
        });

    },

    addCustomAttributeVersion: function (deviceId, customAttributeSetId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnDevice'),
            versionPeriod = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'),
            overlapStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionModel.getProxy().setParams(deviceId, customAttributeSetId);
        versionPeriod.getProxy().setDeviceUrl(deviceId, customAttributeSetId);
        overlapStore.getProxy().setDeviceUrl(deviceId, customAttributeSetId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'device',
            pageType: 'add'
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        me.loadDeviceModel(deviceId);

        attributeSetModel.getProxy().setExtraParam('deviceId', deviceId);
        attributeSetModel.getProxy().setExtraParam('default', true);
        attributeSetModel.load(customAttributeSetId, {
            success: function(customattributeset) {
                versionPeriod.load('currentinterval', {
                    success: function(interval){
                        Ext.suspendLayouts();
                        widget.loadRecord(customattributeset, Ext.create(versionModel), interval.get('start'), interval.get('end'));
                        me.getApplication().fireEvent('loadCustomAttributeSetOnDeviceAdd', customattributeset);
                        me.getApplication().fireEvent('loadCustomAttributeSetOnDevice', customattributeset);
                        widget.down('form').setTitle(router.getRoute().getTitle());
                        widget.setLoading(false);
                        Ext.resumeLayouts(true);
                    }
                });
            }
        });
    },

    cloneCustomAttributeVersion: function (deviceId, customAttributeSetId, versionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnDevice'),
            versionPeriod = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'),
            overlapStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionModel.getProxy().setParams(deviceId, customAttributeSetId);
        versionPeriod.getProxy().setDeviceUrl(deviceId, customAttributeSetId);
        overlapStore.getProxy().setDeviceUrl(deviceId, customAttributeSetId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'device',
            pageType: 'clone'
        });
        me.getApplication().fireEvent('changecontentevent', widget);

        widget.setLoading();
        me.loadDeviceModel(deviceId);
        me.loadAttributeSetModel(deviceId, customAttributeSetId);

        versionModel.load(versionId, {
            success: function(record) {
                versionPeriod.load('currentinterval', {
                    success: function(interval){
                        Ext.suspendLayouts();
                        me.getApplication().fireEvent('loadCustomAttributeSetVersionOnDeviceClone', record);
                        widget.down('form').setTitle(router.getRoute().getTitle());
                        widget.loadRecord(record, Ext.create(versionModel), interval.get('start'), interval.get('end'));
                        widget.setLoading(false);
                        Ext.resumeLayouts(true);
                    }
                });

            }
        });
    },

    loadDeviceModel: function (deviceId) {
        var me = this,
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');

        deviceModel.load(deviceId, {
            success: function(device) {
                me.getApplication().fireEvent('loadDevice', device);
            }
        });

    },

    loadAttributeSetModel: function (deviceId, registerId, customAttributeSetId) {
        var me = this,
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice');

        attributeSetModel.getProxy().setExtraParam('deviceId', deviceId);
        attributeSetModel.getProxy().setExtraParam('registerId', registerId);
        attributeSetModel.load(customAttributeSetId, {
            success: function(customattributeset) {
                me.getApplication().fireEvent('loadCustomAttributeSetOnDevice', customattributeset);
            }
        });
    }
});