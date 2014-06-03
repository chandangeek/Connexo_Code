Ext.define('Mdc.controller.setup.Devices', {
    extend: 'Ext.app.Controller',

    requires: [
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
        {ref: 'deviceGeneralInformationForm', selector: '#deviceGeneralInformationForm'},
        {ref: 'deviceCommunicationTopologyForm', selector: '#deviceCommunicationTopologyForm'},
        {ref: 'deviceCommunicationtopologyMasterLink', selector: '#deviceCommunicationtopologyMasterLink'},
        {ref: 'deviceOpenIssuesForm', selector: '#deviceOpenIssuesForm'},
        {ref: 'deviceSetupPanel', selector: '#deviceSetupPanel'},
        {ref: 'deviceGeneralInformationDeviceTypeLink', selector: '#deviceGeneralInformationDeviceTypeLink'},
        {ref: 'deviceGeneralInformationDeviceConfigurationLink', selector: '#deviceGeneralInformationDeviceConfigurationLink'},
        {ref: 'dataCollectionIssuesLink', selector: '#dataCollectionIssuesLink'}
    ],


    showDeviceDetailsView: function (id) {
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.Device').load(id, {
            success: function (device) {
                var widget = Ext.widget('deviceSetup', {deviceId: id, mRID: device.get('mRID')});
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getDeviceSetupPanel().setTitle(device.get('mRID'));
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().set({href: '#/administration/devicetypes/' + device.get('deviceTypeId')});
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().setHTML(device.get('deviceTypeName'));
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().set({href: '#/administration/devicetypes/' + device.get('deviceTypeId') + '/deviceconfigurations/' + device.get('deviceConfigurationId')});
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().setHTML(device.get('deviceConfigurationName'));
                me.getDeviceCommunicationtopologyMasterLink().getEl().set({href: '#/devices/' + device.get('masterDevicemRID')});
                me.getDeviceCommunicationtopologyMasterLink().getEl().setHTML(device.get('masterDevicemRID'));
                device.slaveDevicesStore.data.items.forEach(function (slaveDevice) {
                    widget.addSlaveDevice(slaveDevice.get('mRID'));
                });
                me.getDeviceOpenIssuesForm().getForm().setValues({
                    issues: device.get('nbrOfDataCollectionIssues')
                });
                me.getDeviceGeneralInformationForm().loadRecord(device);
                me.getDeviceCommunicationTopologyForm().loadRecord(device);
                me.getDeviceOpenIssuesForm().loadRecord(device);
            }
        });
    }
});

