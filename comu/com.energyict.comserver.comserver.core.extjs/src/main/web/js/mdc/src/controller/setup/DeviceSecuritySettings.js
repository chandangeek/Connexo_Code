/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceSecuritySettings', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,

    views: [
        'setup.devicesecuritysettings.DeviceSecuritySettingSetup',
        'setup.devicesecuritysettings.DeviceSecuritySettingGrid',
        'setup.devicesecuritysettings.DeviceSecuritySettingPreview',
    ],

    stores: [
        'Mdc.store.SecuritySettingsOfDevice'
    ],

    refs: [
        {ref: 'deviceSecuritySettingGrid', selector: '#devicesecuritysettinggrid'},
        {ref: 'deviceSecuritySettingPreview', selector: '#deviceSecuritySettingPreview'},
        {ref: 'deviceSecuritySettingPreviewForm', selector: '#deviceSecuritySettingPreviewForm'},
        {ref: 'deviceSecuritySettingComboBox', selector: '#deviceSecuritySettingComboBox'},
        {ref: 'deviceSecuritySettingPreviewDetailsTitle', selector: '#deviceSecuritySettingPreviewDetailsTitle'},
        {ref: 'restoreAllButton', selector: '#restoreAllButton'},
        {ref: 'addEditButton', selector: '#addEditButton'},
        {ref: 'showValueDeviceSecuritySetting', selector: '#showValueDeviceSecuritySetting'},
        {ref: 'hideValueDeviceSecuritySetting', selector: '#hideValueDeviceSecuritySetting'}
    ],

    init: function () {
        this.control({
            '#devicesecuritysettinggrid': {
                selectionchange: this.previewDeviceSecuritySetting
            }
        });
    },

    showDeviceSecuritySettings: function (deviceId) {
        var me = this,
        viewport = Ext.ComponentQuery.query('viewport')[0];

        this.deviceId = deviceId;

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                me.getStore('Mdc.store.SecuritySettingsOfDevice').getProxy().setExtraParam('deviceId', deviceId);
                me.getApplication().fireEvent('changecontentevent', Ext.widget('deviceSecuritySettingSetup', {
                    device: device
                }));
                me.getApplication().fireEvent('loadDevice', device);
                viewport.setLoading(false);
            }
        });
    },

    previewDeviceSecuritySetting: function () {
        var me = this;
        var deviceSecuritySetting = me.getDeviceSecuritySettingGrid().getSelectionModel().getSelection();
        me.getDeviceSecuritySettingPreview().down('property-form').remove();
        if (deviceSecuritySetting.length == 1) {
            var deviceSecuritySettingName = deviceSecuritySetting[0].get('name');
            me.getDeviceSecuritySettingPreview().getLayout().setActiveItem(1);
            me.getDeviceSecuritySettingPreview().setTitle(Ext.String.htmlEncode(deviceSecuritySettingName));
            me.getDeviceSecuritySettingPreviewForm().loadRecord(deviceSecuritySetting[0]);
            me.getDeviceSecuritySettingPreview().down('property-form').readOnly = true;
            me.getDeviceSecuritySettingPreview().down('property-form').loadRecord(deviceSecuritySetting[0]);
            me.getDeviceSecuritySettingPreviewDetailsTitle().setVisible(deviceSecuritySetting[0].propertiesStore.data.items.length > 0);
        } else {
            me.getDeviceSecuritySettingPreview().getLayout().setActiveItem(0);
        }
    }
})
;