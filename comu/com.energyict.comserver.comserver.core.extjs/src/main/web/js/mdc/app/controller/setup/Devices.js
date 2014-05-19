Ext.define('Mdc.controller.setup.Devices', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Mdc.model.Device'
    ],
    views: [
        'setup.device.DeviceSetup',
        'setup.device.DeviceMenu',
        'setup.device.DeviceGeneralInformationPanel',
        'setup.device.DeviceCommunicationTopologyPanel',
        'setup.device.DeviceOpenIssuesPanel'
    ],

    stores: [

    ],

    refs: [
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'deviceGeneralInformationForm', selector: '#deviceGeneralInformationForm'},
        {ref: 'deviceCommunicationTopologyForm', selector: '#deviceCommunicationTopologyForm'},
        {ref: 'deviceCommunicationtopologyMasterLink', selector: '#deviceCommunicationtopologyMasterLink'},
        {ref: 'deviceOpenIssuesForm', selector: '#deviceOpenIssuesForm'},
        {ref: 'deviceSetupTitle', selector: '#deviceSetupTitle'},
        {ref: 'deviceGeneralInformationDeviceTypeLink', selector: '#deviceGeneralInformationDeviceTypeLink'},
        {ref: 'deviceGeneralInformationDeviceConfigurationLink', selector: '#deviceGeneralInformationDeviceConfigurationLink'},
        {ref: 'dataCollectionIssuesLink', selector: '#dataCollectionIssuesLink'}
    ],


    showDeviceDetailsView: function (id) {
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.Device').load(id, {
            success: function (device) {
                var widget = Ext.widget('deviceSetup', {deviceId: id, mRID: device.get('mRID')});
                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                me.getDeviceSetupTitle().update('<h1>' + device.get('mRID') + '</h1>');
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().set({href: '#/administration/devicetypes/' + device.get('deviceTypeId')});
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().setHTML(device.get('deviceTypeName'));
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().set({href: '#/administration/devicetypes/' + device.get('deviceTypeId') + '/deviceconfigurations/' + device.get('deviceConfigurationId')});
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().setHTML(device.get('deviceConfigurationName'));
                me.getDeviceCommunicationtopologyMasterLink().getEl().set({href: '#/administration/devices/' + device.get('masterDeviceId')});
                me.getDeviceCommunicationtopologyMasterLink().getEl().setHTML(device.get('masterDevicemRID'));
                device.slaveDevicesStore.data.items.forEach(function (slaveDevice) {
                    widget.addSlaveDevice(slaveDevice.get('mRID'), slaveDevice.get('id'));
                });
                me.getDataCollectionIssuesLink().getEl().setHTML(device.get('nbrOfDataCollectionIssues') + ' ' + Uni.I18n.translatePlural('deviceOpenIssues.dataCollectionIssues', device.get('nbrOfDataCollectionIssues'), 'MDC', 'data collection issue(s)'));
                me.overviewBreadCrumb(id, device.get('mRID'));
                me.getDeviceGeneralInformationForm().loadRecord(device);
                me.getDeviceCommunicationTopologyForm().loadRecord(device);
                me.getDeviceOpenIssuesForm().loadRecord(device);
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

