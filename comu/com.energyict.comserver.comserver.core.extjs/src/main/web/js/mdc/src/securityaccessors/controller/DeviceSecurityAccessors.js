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
        }
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
        var me = this;
        me.getKeyPreviewForm().loadRecord(record);
        me.getKeyPreview().setTitle(Ext.htmlEncode(record.get('name')));
        //if (me.getPreview().down('security-accessors-action-menu')) {
        //    me.getPreview().down('security-accessors-action-menu').record = record;
        //}
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
        var me = this;
        me.getCertificatePreviewForm().loadRecord(record);
        me.getCertificatePreview().setTitle(Ext.htmlEncode(record.get('name')));
        //if (me.getPreview().down('security-accessors-action-menu')) {
        //    me.getPreview().down('security-accessors-action-menu').record = record;
        //}
    }

});