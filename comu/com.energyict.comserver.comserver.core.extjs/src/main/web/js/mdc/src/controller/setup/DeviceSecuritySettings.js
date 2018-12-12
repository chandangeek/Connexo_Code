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
        'setup.devicesecuritysettings.DeviceSecuritySettingPreview'
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
        var me = this,
            deviceSecuritySetting = me.getDeviceSecuritySettingGrid().getSelectionModel().getSelection(),
            propertyForm = me.getDeviceSecuritySettingPreview().down('property-form');

        me.getDeviceSecuritySettingPreview().on('afterlayout', function() {
            propertyForm.showValues();
        }, me, {single:true});
        Ext.suspendLayouts();
        propertyForm.remove();
        if (deviceSecuritySetting.length === 1) {
            propertyForm.readOnly = true;
            propertyForm.loadRecord(deviceSecuritySetting[0]);
            me.getDeviceSecuritySettingPreview().setTitle(Ext.String.htmlEncode(deviceSecuritySetting[0].get('name')));
            me.getDeviceSecuritySettingPreviewDetailsTitle().setVisible(deviceSecuritySetting[0].propertiesStore.data.items.length > 0);
            me.getDeviceSecuritySettingPreview().getLayout().setActiveItem(1);
            me.getDeviceSecuritySettingPreview().loadRecord(deviceSecuritySetting[0]);
        } else {
            me.getDeviceSecuritySettingPreview().getLayout().setActiveItem(0);
        }
        Ext.resumeLayouts(true);
    }
});