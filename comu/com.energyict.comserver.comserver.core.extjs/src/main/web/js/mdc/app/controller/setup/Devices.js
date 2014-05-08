Ext.define('Mdc.controller.setup.Devices', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Mdc.model.Device'
    ],
    views: [
        'setup.device.DeviceSetup',
        'setup.device.DeviceMenu',
        'setup.device.DeviceGeneralInformationPanel'
    ],

    stores: [

    ],

    refs: [
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'deviceGeneralInformationForm', selector: '#deviceGeneralInformationForm'},
        {ref: 'deviceSetupTitle', selector: '#deviceSetupTitle'},
        {ref: 'deviceGeneralInformationDeviceTypeLink', selector: '#deviceGeneralInformationDeviceTypeLink'},
        {ref: 'deviceGeneralInformationDeviceConfigurationLink', selector: '#deviceGeneralInformationDeviceConfigurationLink'}
    ],


    showDeviceDetailsView: function (id) {
        var me = this;
        var widget = Ext.widget('deviceSetup', {deviceId: id});
        Ext.ModelManager.getModel('Mdc.model.Device').load(id, {
            success: function (device) {
                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                me.getDeviceSetupTitle().update('<h1>' + device.get('mRID') + '</h1>');
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().set({href: '#/setup/devicetypes/' + device.get('deviceTypeId')});
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().setHTML(device.get('deviceTypeName'));
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().set({href: '#/setup/devicetypes/' + device.get('deviceTypeId') + '/deviceconfigurations/' + device.get('deviceConfigurationId')});
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().setHTML(device.get('deviceConfigurationName'));
                me.overviewBreadCrumb(id, device.get('mRID'));
                me.getDeviceGeneralInformationForm().loadRecord(device);
            }
        });
    },


    overviewBreadCrumb: function (id, name) {
        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: name,
            href: id
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('device.devices', 'MDC', 'Devices'),
            href: 'devices'
        });
        breadcrumbChild.setChild(breadcrumbChild2);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbChild);
    }

});

