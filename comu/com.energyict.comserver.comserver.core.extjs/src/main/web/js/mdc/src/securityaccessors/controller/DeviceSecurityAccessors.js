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
        'Mdc.securityaccessors.model.DeviceSecurityAccessor'
    ],

    views: [
        'Mdc.securityaccessors.view.DeviceSecurityAccessorsOverview'
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
            ref: 'keyCurrentAttributesContainer',
            selector: '#mdc-device-accessors-key-preview #mdc-device-security-accessor-preview-current-attributes-container'
        },
        {
            ref: 'keyTempAttributesContainer',
            selector: '#mdc-device-accessors-key-preview #mdc-device-security-accessor-preview-temp-attributes-container'
        },
        {
            ref: 'certificateCurrentAttributesContainer',
            selector: '#mdc-device-accessors-certificate-preview #mdc-device-security-accessor-preview-current-attributes-container'
        },
        {
            ref: 'certificateTempAttributesContainer',
            selector: '#mdc-device-accessors-certificate-preview #mdc-device-security-accessor-preview-temp-attributes-container'
        },
    ],

    deviceId: undefined,
    certificatesTabNeverSelectedYet: true,

    init: function () {
        var me = this;

        me.control({
            'device-security-accessors-overview #mdc-device-accessors-keys-grid': {
                select: me.onKeyRecordSelected
            },
            'device-security-accessors-overview #mdc-device-accessors-certificates-grid': {
                select: me.onCertificateRecordSelected
            }
        });
    },

    showDeviceSecurityAccessors: function(deviceId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        me.deviceId = deviceId;

        viewport.setLoading();
        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getApplication().fireEvent('changecontentevent', Ext.widget('device-security-accessors-overview', {
                    device: device
                }));
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

        me.getKeyCurrentAttributesContainer().setVisible(record.propertiesStore.data.items.length > 0);
        me.getKeyCurrentAttributesContainer().down('property-form').loadRecord(record);
        var tempPropertiesAvailable = record.temppropertiesStore.data.items.length > 0;
        me.getKeyTempAttributesContainer().setVisible(tempPropertiesAvailable);
        if (tempPropertiesAvailable) {
            me.getKeyTempAttributesContainer().down('property-form').initProperties(record.tempproperties());
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
        me.getCertificateCurrentAttributesContainer().setVisible(record.propertiesStore.data.items.length > 0);
        me.getCertificateCurrentAttributesContainer().down('property-form').loadRecord(record);
        var tempPropertiesAvailable = record.temppropertiesStore.data.items.length > 0;
        me.getCertificateTempAttributesContainer().setVisible(tempPropertiesAvailable);
        if (tempPropertiesAvailable) {
            me.getCertificateTempAttributesContainer().down('property-form').initProperties(record.tempproperties());
        }
    }

});