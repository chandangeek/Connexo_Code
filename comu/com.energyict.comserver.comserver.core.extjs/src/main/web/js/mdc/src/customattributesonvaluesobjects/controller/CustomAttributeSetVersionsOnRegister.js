/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnRegister', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnRegister',
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionForm'
    ],

    requires: [
        'Mdc.model.Device',
        'Mdc.model.Register',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnRegister',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnRegister',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'
    ],

    stores: [
        'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnRegister'
    ],

    refs: [],

    editCustomAttributeVersion: function (deviceId, registerId, customAttributeSetId, versionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnRegister'),
            overlapStore = Ext.create('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionModel.getProxy().setParams(deviceId, registerId, customAttributeSetId);
        overlapStore.getProxy().setRegisterEditUrl(deviceId, registerId, customAttributeSetId, versionId);

        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'register',
            pageType: 'edit'
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        me.loadDeviceModel(deviceId);
        me.loadRegisterModel(deviceId, registerId);
        me.loadAttributeSetModel(deviceId, registerId, customAttributeSetId);
        versionModel.load(versionId, {
            success: function (record) {
                Ext.suspendLayouts();
                me.getApplication().fireEvent('loadCustomAttributeSetVersionOnRegister', record);
                widget.down('form').setTitle(router.getRoute().getTitle());
                widget.loadRecord(record, record, record.get('startTime'), record.get('endTime'));
                widget.setLoading(false);
                Ext.resumeLayouts(true);
            }
        });

    },

    addCustomAttributeVersion: function (deviceId, registerId, customAttributeSetId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnRegister'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnRegister'),
            overlapStore = Ext.create('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            versionPeriod = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'),
            widget;

        versionModel.getProxy().setParams(deviceId, registerId, customAttributeSetId);
        attributeSetModel.getProxy().setParams(deviceId, registerId);
        versionPeriod.getProxy().setRegisterUrl(deviceId, registerId, customAttributeSetId);
        overlapStore.getProxy().setRegisterUrl(deviceId, registerId, customAttributeSetId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'register',
            pageType: 'add'
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        me.loadDeviceModel(deviceId);
        me.loadRegisterModel(deviceId, registerId);

        attributeSetModel.load(customAttributeSetId, {
            success: function (customattributeset) {
                versionPeriod.load('currentinterval', {
                    success: function (interval) {
                        Ext.suspendLayouts();
                        widget.loadRecord(customattributeset, Ext.create(versionModel), interval.get('start'), interval.get('end'));
                        me.getApplication().fireEvent('loadCustomAttributeSetOnRegisterAdd', customattributeset);
                        me.getApplication().fireEvent('loadCustomAttributeSetOnRegister', customattributeset);
                        widget.down('form').setTitle(router.getRoute().getTitle());
                        widget.setLoading(false);
                        Ext.resumeLayouts(true);
                    }
                });
            }
        });
    },

    cloneCustomAttributeVersion: function (deviceId, registerId, customAttributeSetId, versionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnRegister'),
            versionPeriod = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'),
            overlapStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionPeriod.getProxy().setRegisterUrl(deviceId, registerId, customAttributeSetId);
        overlapStore.getProxy().setRegisterUrl(deviceId, registerId, customAttributeSetId);
        versionModel.getProxy().setParams(deviceId, registerId, customAttributeSetId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'register',
            pageType: 'clone'
        });
        me.getApplication().fireEvent('changecontentevent', widget);

        widget.setLoading();
        me.loadDeviceModel(deviceId);
        me.loadRegisterModel(deviceId, registerId);
        me.loadAttributeSetModel(deviceId, registerId, customAttributeSetId);

        versionModel.load(versionId, {
            success: function (record) {
                versionPeriod.load('currentinterval', {
                    success: function (interval) {
                        Ext.suspendLayouts();
                        widget.loadRecord(record, Ext.create(versionModel), interval.get('start'), interval.get('end'));
                        me.getApplication().fireEvent('loadCustomAttributeSetVersionOnRegisterClone', record);
                        widget.down('form').setTitle(router.getRoute().getTitle());
                        widget.setLoading(false);
                        Ext.resumeLayouts(true);
                    }
                });
            }
        });
    },

    loadCustomAttributeVersions: function (deviceId, registerId, customAttributeSetId) {
        var me = this,
            versionsStore = me.getStore('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnRegister'),
            router = me.getController('Uni.controller.history.Router'),
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget;

        versionsStore.getProxy().setParams(deviceId, registerId, customAttributeSetId);
        contentPanel.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                widget = Ext.widget('register-history-custom-attribute-sets-versions', {
                    device: device,
                    router: router
                });
                me.getApplication().fireEvent('changecontentevent', widget);

                me.loadRegisterModel(deviceId, registerId);
                me.loadAttributeSetModel(deviceId, registerId, customAttributeSetId, widget);

            },
            callback: function () {
                contentPanel.setLoading(false);
            }
        });
    },

    loadDeviceModel: function (deviceId) {
        var me = this,
            deviceModel = Ext.ModelManager.getModel('Mdc.model.Device');

        deviceModel.load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
            }
        });

    },

    loadRegisterModel: function (deviceId, registerId) {
        var me = this,
            registerModel = Ext.ModelManager.getModel('Mdc.model.Register');

        registerModel.getProxy().setExtraParam('deviceId', deviceId);

        registerModel.load(registerId, {
            success: function (register) {
                me.getApplication().fireEvent('loadRegisterConfiguration', register);
            }
        });
    },

    loadAttributeSetModel: function (deviceId, registerId, customAttributeSetId, widget) {
        var me = this,
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnRegister'),
            router = me.getController('Uni.controller.history.Router');

        attributeSetModel.getProxy().setParams(deviceId, registerId);
        attributeSetModel.load(customAttributeSetId, {
            success: function (customattributeset) {
                me.getApplication().fireEvent('loadCustomAttributeSetOnRegister', customattributeset);
                if (!Ext.isEmpty(widget)) {
                    widget.down('custom-attribute-set-versions-setup').setTitle(router.getRoute().getTitle());
                    widget.down('#custom-attribute-set-add-version-btn').setVisible(customattributeset.get('editable'));
                    widget.down('#custom-attribute-set-versions-grid-action-column').setVisible(customattributeset.get('editable'));
                    widget.down('#custom-attribute-set-add-version-btn-top').setVisible(customattributeset.get('editable'));
                }
            }
        });
    }
});