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
        'setup.device.DeviceOpenIssuesPanel',
        'setup.device.DeviceAdd'
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

    init: function () {
        this.control({
            'deviceAdd button[action=save]': {
                click: this.saveDevice
            },
            'deviceAdd button[action=cancel]': {
                click: this.back
            }
        });
    },

    back: function () {
        location.href = "#devices";
    },

    showDeviceDetailsView: function (mRID) {
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                var widget = Ext.widget('deviceSetup', {mRID: device.get('mRID')});
                me.getApplication().fireEvent('loadDevice', device);
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
    },

    showAddDevice: function () {
        var widget = Ext.widget('deviceAdd');
        widget.down('form').loadRecord(Ext.create('Mdc.model.Device'));
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    saveDevice: function (button) {
        var me = this;
        var form = button.up('form');

        form.updateRecord();
        form.getRecord().save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translatePlural('deviceAdd.added', record.get('mRID'), 'USM', 'Device \'{0}\' added.'));
                location.href = "#devices/" + record.get('mRID');
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    me.showErrorPanel(form);
                    form.getForm().markInvalid(json.errors);
                }
            }
        });
    },

    showErrorPanel: function(form) {
        var formErrorsPlaceHolder = form.down('#addDeviceFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add(
            {
                xtype: 'box',
                height: 22,
                width: 26,
                cls: 'x-uni-form-error-msg-icon'
            },
            {
                html: Uni.I18n.translate('addDevice.form.errors', 'MDC', 'There are errors on this page that require your attention.')
            });
        formErrorsPlaceHolder.show();
    }
});

