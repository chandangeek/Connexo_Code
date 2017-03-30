/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.controller.CustomAttributeSetVersionsOnChannel', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsOnChannel',
        'Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionForm'
    ],

    requires: [
        'Mdc.model.Device',
        'Mdc.model.ChannelOfLoadProfilesOfDevice',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnChannel',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnChannel',
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'
    ],

    stores: [
        'Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnChannel',
        'Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'
    ],

    refs: [],

    editCustomAttributeVersion: function (deviceId, channelId, customAttributeSetId, versionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnChannel'),
            overlapStore = Ext.create('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionModel.getProxy().setParams(deviceId, channelId, customAttributeSetId);
        overlapStore.getProxy().setChannelEditUrl(deviceId, channelId, customAttributeSetId, versionId);

        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'channel',
            pageType: 'edit'
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        me.loadDeviceModel(deviceId);
        me.loadChannelModel(deviceId, channelId);
        me.loadAttributeSetModel(deviceId, channelId, customAttributeSetId);

        versionModel.load(versionId, {
            success: function (record) {
                Ext.suspendLayouts();
                me.getApplication().fireEvent('loadCustomAttributeSetVersionOnChannel', record);
                widget.down('form').setTitle(router.getRoute().getTitle());
                widget.loadRecord(record, record,  record.get('startTime'), record.get('endTime'));
                widget.setLoading(false);
                Ext.resumeLayouts(true);
            }
        });

    },

    addCustomAttributeVersion: function (deviceId, channelId, customAttributeSetId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnChannel'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnChannel'),
            versionPeriod = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'),
            overlapStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionModel.getProxy().setParams(deviceId, channelId, customAttributeSetId);
        versionPeriod.getProxy().setChannelUrl(deviceId, channelId, customAttributeSetId);
        overlapStore.getProxy().setChannelUrl(deviceId, channelId, customAttributeSetId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'channel',
            pageType: 'add'
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();

        me.loadDeviceModel(deviceId);
        me.loadChannelModel(deviceId, channelId);

        attributeSetModel.getProxy().setExtraParam('deviceId', deviceId);
        attributeSetModel.getProxy().setExtraParam('channelId', channelId);
        attributeSetModel.load(customAttributeSetId, {
            success: function (customattributeset) {
                versionPeriod.load('currentinterval', {
                    success: function (interval) {
                        Ext.suspendLayouts();
                        widget.loadRecord(customattributeset, Ext.create(versionModel), interval.get('start'), interval.get('end'));
                        me.getApplication().fireEvent('loadCustomAttributeSetOnChannelAdd', customattributeset);
                        me.getApplication().fireEvent('loadCustomAttributeSetOnChannel', customattributeset);
                        widget.down('form').setTitle(router.getRoute().getTitle());
                        widget.setLoading(false);
                        Ext.resumeLayouts(true);
                    }
                });
            }
        });


    },

    cloneCustomAttributeVersion: function (deviceId, channelId, customAttributeSetId, versionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            versionModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnChannel'),
            versionPeriod = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod'),
            overlapStore = me.getStore('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions'),
            widget;

        versionModel.getProxy().setParams(deviceId, channelId, customAttributeSetId);
        versionPeriod.getProxy().setChannelUrl(deviceId, channelId, customAttributeSetId);
        overlapStore.getProxy().setChannelUrl(deviceId, channelId, customAttributeSetId);
        widget = Ext.widget('custom-attribute-set-version-form', {
            router: router,
            overlapStore: overlapStore,
            versionModel: versionModel,
            type: 'channel',
            pageType: 'clone'
        });
        me.getApplication().fireEvent('changecontentevent', widget);

        widget.setLoading();
        me.loadDeviceModel(deviceId);
        me.loadChannelModel(deviceId, channelId);
        me.loadAttributeSetModel(deviceId, channelId, customAttributeSetId);

        versionModel.load(versionId, {
            success: function (record) {
                versionPeriod.load('currentinterval', {
                    success: function (interval) {
                        Ext.suspendLayouts();
                        me.getApplication().fireEvent('loadCustomAttributeSetVersionOnChannelClone', record);
                        widget.loadRecord(record, Ext.create(versionModel), interval.get('start'), interval.get('end'));
                        widget.down('form').setTitle(router.getRoute().getTitle());
                        widget.setLoading(false);
                        Ext.resumeLayouts(true);
                    }
                });
            }
        });
    },

    loadCustomAttributeVersions: function (deviceId, channelId, customAttributeSetId) {
        var me = this,
            versionsStore = me.getStore('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnChannel'),
            router = me.getController('Uni.controller.history.Router'),
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            widget;

        versionsStore.getProxy().setParams(deviceId, channelId, customAttributeSetId);
        contentPanel.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                widget = Ext.widget('channel-history-custom-attribute-sets-versions', {
                    device: device,
                    router: router
                });
                me.getApplication().fireEvent('changecontentevent', widget);

                me.loadChannelModel(deviceId, channelId);
                me.loadAttributeSetModel(deviceId, channelId, customAttributeSetId, widget);

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

    loadChannelModel: function (deviceId, channelId) {
        var me = this,
            channelModel = me.getModel('Mdc.model.ChannelOfLoadProfilesOfDevice');

        channelModel.getProxy().setExtraParam('deviceId', deviceId);
        channelModel.load(channelId, {
            success: function (channel) {
                me.getApplication().fireEvent('channelOfLoadProfileOfDeviceLoad', channel);
            }
        });
    },

    loadAttributeSetModel: function (deviceId, channelId, customAttributeSetId, widget) {
        var me = this,
            attributeSetModel = Ext.ModelManager.getModel('Mdc.customattributesonvaluesobjects.model.AttributeSetOnChannel'),
            router = me.getController('Uni.controller.history.Router');

        attributeSetModel.getProxy().setExtraParam('deviceId', deviceId);
        attributeSetModel.getProxy().setExtraParam('channelId', channelId);

        attributeSetModel.load(customAttributeSetId, {
            success: function (customattributeset) {
                me.getApplication().fireEvent('loadCustomAttributeSetOnChannel', customattributeset);
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