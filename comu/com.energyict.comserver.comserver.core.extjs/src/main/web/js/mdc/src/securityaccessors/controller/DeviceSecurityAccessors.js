/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.controller.DeviceSecurityAccessors', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.securityaccessors.store.DeviceSecurityKeys',
        'Mdc.securityaccessors.store.DeviceSecurityCertificates'
    ],

    models: [
        'Mdc.securityaccessors.model.DeviceSecurityAccessor',
        'Mdc.securityaccessors.model.DeviceSecurityKey',
        'Mdc.securityaccessors.model.DeviceSecurityCertificate'
    ],

    views: [
        'Mdc.securityaccessors.view.DeviceSecurityAccessorsOverview',
        'Mdc.securityaccessors.view.EditDeviceKeyAttributes',
        'Mdc.securityaccessors.view.EditDeviceCertificateAttributes'
    ],

    refs: [
        {
            ref: 'keyPreviewForm',
            selector: '#mdc-device-accessors-key-preview device-security-accessor-preview-form form'
        },
        {
            ref: 'keyPreview',
            selector: '#mdc-device-accessors-key-preview'
        },
        {
            ref: 'certificatePreviewForm',
            selector: '#mdc-device-accessors-certificate-preview device-security-accessor-preview-form form'
        },
        {
            ref: 'certificatePreview',
            selector: '#mdc-device-accessors-certificate-preview'
        },
        {
            ref: 'tabPanel',
            selector: '#mdc-device-security-accessors-tab-panel'
        },
        {
            ref: 'activeKeyAttributesContainer',
            selector: '#mdc-device-accessors-key-preview #mdc-device-security-accessor-preview-active-attributes-container'
        },
        {
            ref: 'passiveKeyAttributesContainer',
            selector: '#mdc-device-accessors-key-preview #mdc-device-security-accessor-preview-passive-attributes-container'
        },
        {
            ref: 'activeCertificateAttributesContainer',
            selector: '#mdc-device-accessors-certificate-preview #mdc-device-security-accessor-preview-active-attributes-container'
        },
        {
            ref: 'passiveCertificateAttributesContainer',
            selector: '#mdc-device-accessors-certificate-preview #mdc-device-security-accessor-preview-passive-attributes-container'
        }
    ],

    deviceId: undefined,
    certificatesTabNeverSelectedYet: true,
    deviceKeyRecord: undefined,
    deviceCertificateRecord: undefined,

    init: function () {
        var me = this;

        me.control({
            'device-security-accessors-overview #mdc-device-accessors-keys-grid': {
                select: me.onKeyRecordSelected
            },
            'device-security-accessors-overview #mdc-device-accessors-certificates-grid': {
                select: me.onCertificateRecordSelected
            },
            'device-security-accessors-action-menu': {
                click: me.onMenuAction
            },
            '#mdc-device-security-accessors-tab-panel': {
                tabchange: me.onTabChange
            },
            '#mdc-device-key-attributes-edit-cancel-link': {
                click: me.navigateToKeysOverview
            },
            '#mdc-device-certificate-attributes-edit-cancel-link': {
                click: me.navigateToCertificatesOverview
            }
        });
    },

    navigateToKeysOverview: function () {
        location.href = '#/devices/' + encodeURIComponent(this.deviceId) + '/keys/';
    },

    navigateToCertificatesOverview: function () {
        location.href = '#/devices/' + encodeURIComponent(this.deviceId) + '/certificates/';
    },

    onTabChange: function(tabPanel, newActiveItem) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            url = router.getRoute().buildUrl(router.arguments, router.queryParams);

        if (newActiveItem.itemId === 'mdc-device-accessors-keys-tab') {
            url = url.replace('/certificates', '/keys');
        } else if (newActiveItem.itemId === 'mdc-device-accessors-certificates-tab') {
            url = url.replace('/keys', '/certificates');
        }
        Uni.util.History.setParsePath(false);
        Uni.util.History.suspendEventsForNextCall();
        window.location.replace(url);
    },

    showDeviceKeys: function(deviceId) {
        this.showDeviceSecurityAccessors(deviceId, 0);
    },

    showDeviceCertificates: function(deviceId) {
        this.showDeviceSecurityAccessors(deviceId, 1);
    },

    showDeviceSecurityAccessors: function(deviceId, activeTab) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.deviceId = deviceId;

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var overViewWidget = Ext.create('Mdc.securityaccessors.view.DeviceSecurityAccessorsOverview', {
                    device: device,
                    activeTab: activeTab
                });
                me.getApplication().fireEvent('changecontentevent', overViewWidget);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
            }
        });
    },

    onKeyRecordSelected: function (grid, record) {
        var me = this,
            actionsMenu = me.getKeyPreview().down('device-security-accessors-action-menu');

        me.getKeyPreviewForm().loadRecord(record);
        me.getKeyPreview().setTitle(Ext.htmlEncode(record.get('name')));
        if (actionsMenu) {
            actionsMenu.record = record;
        }

        me.getActiveKeyAttributesContainer().setVisible(record.propertiesStore.data.items.length > 0);
        me.getActiveKeyAttributesContainer().down('property-form').loadRecord(record);
        var tempPropertiesAvailable = record.temppropertiesStore.data.items.length > 0;
        me.getPassiveKeyAttributesContainer().setVisible(tempPropertiesAvailable);
        if (tempPropertiesAvailable) {
            me.getPassiveKeyAttributesContainer().down('property-form').initProperties(record.tempproperties());
        }
    },

    onCertificateRecordSelected: function (grid, record) {
        var me = this;

        if (me.certificatesTabNeverSelectedYet) {
            me.getTabPanel().on('tabchange', function() {
                me.certificatesTabNeverSelectedYet = false;
                me.previewCertificateRecord(record);
            }, me, {single: true});
        } else {
            me.previewCertificateRecord(record);
        }
    },

    previewCertificateRecord: function(record) {
        var me = this,
            actionsMenu = me.getKeyPreview().down('device-security-accessors-action-menu');

        me.getCertificatePreviewForm().loadRecord(record);
        me.getCertificatePreview().setTitle(Ext.htmlEncode(record.get('name')));
        if (actionsMenu) {
            actionsMenu.record = record;
        }
        me.getActiveCertificateAttributesContainer().setVisible(record.propertiesStore.data.items.length > 0);
        me.getActiveCertificateAttributesContainer().down('property-form').loadRecord(record);
        var tempPropertiesAvailable = record.temppropertiesStore.data.items.length > 0;
        me.getPassiveCertificateAttributesContainer().setVisible(tempPropertiesAvailable);
        if (tempPropertiesAvailable) {
            me.getPassiveCertificateAttributesContainer().down('property-form').initProperties(record.tempproperties());
        }
    },

    onMenuAction: function(menu, menuItem) {
        var me = this;

        switch (menuItem.action) {
            case 'editDeviceKey':
                me.navigateToEditKeyAttributes(menu.record);
                break;
            case 'editDeviceCertificate':
                me.navigateToEditCertificateAttributes(menu.record);
                break;
        }
    },

    navigateToEditKeyAttributes: function (deviceSecurityAccessorRecord) {
        this.deviceKeyRecord = deviceSecurityAccessorRecord;
        location.href = '#/devices/' + encodeURIComponent(this.deviceId) + '/keys/' + encodeURIComponent(deviceSecurityAccessorRecord.get('id')) + '/edit';
    },

    navigateToEditCertificateAttributes: function (deviceSecurityAccessorRecord) {
        this.deviceCertificateRecord = deviceSecurityAccessorRecord;
        location.href = '#/devices/' + encodeURIComponent(this.deviceId) + '/certificates/' + encodeURIComponent(deviceSecurityAccessorRecord.get('id')) + '/edit';
    },

    editDeviceKey: function(deviceId, accessorId) {
        var me = this;
        me.deviceId = deviceId;

        if (Ext.isEmpty(me.deviceKeyRecord)) {
            var keyModel = Ext.ModelManager.getModel('Mdc.securityaccessors.model.DeviceSecurityKey');
            keyModel.getProxy().setUrl(encodeURIComponent(deviceId));
            keyModel.load(accessorId, {
                success: function (keyRecord) {
                    me.deviceKeyRecord = keyRecord;
                    me.doEditDeviceKey(deviceId, accessorId);
                }
            });
        } else {
            me.doEditDeviceKey(deviceId, accessorId);
        }
    },

    doEditDeviceKey: function(deviceId, accessorId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget = Ext.create('Mdc.securityaccessors.view.EditDeviceKeyAttributes', {
                    device: device,
                    keyRecord: me.deviceKeyRecord
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('form').setTitle( Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", me.deviceKeyRecord.get('name')) );
                me.getApplication().fireEvent('deviceKeyLoaded', me.deviceKeyRecord);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
            }
        });
    },

    editDeviceCertificate: function(deviceId, accessorId) {
        var me = this;
        me.deviceId = deviceId;

        if (Ext.isEmpty(me.deviceCertificateRecord)) {
            var certificateModel = Ext.ModelManager.getModel('Mdc.securityaccessors.model.DeviceSecurityCertificate');
            certificateModel.getProxy().setUrl(encodeURIComponent(deviceId));
            certificateModel.load(accessorId, {
                success: function (certificateRecord) {
                    me.deviceCertificateRecord = certificateRecord;
                    me.doEditDeviceCertificate(deviceId, accessorId);
                }
            });
        } else {
            me.doEditDeviceCertificate(deviceId, accessorId);
        }
    },

    doEditDeviceCertificate: function(deviceId, accessorId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget = Ext.create('Mdc.securityaccessors.view.EditDeviceCertificateAttributes', {
                    device: device,
                    certificateRecord: me.certificateKeyRecord
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('form').setTitle( Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", me.deviceCertificateRecord.get('name')) );
                me.getApplication().fireEvent('deviceCertificateLoaded', me.deviceCertificateRecord);
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
            }
        });
    }

});