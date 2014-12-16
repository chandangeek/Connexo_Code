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
        'AvailableDeviceTypes',
        'AvailableDeviceConfigurations'
    ],

    mixins: [
        'Mdc.util.DeviceDataValidationActivation'
    ],

    refs: [
        {ref: 'deviceGeneralInformationForm', selector: '#deviceGeneralInformationForm'},
        {ref: 'deviceCommunicationTopologyPanel', selector: '#devicecommicationtopologypanel'},
        {ref: 'deviceOpenIssuesPanel', selector: '#deviceopenissuespanel'},
        {ref: 'deviceSetupPanel', selector: '#deviceSetupPanel'},
        {ref: 'deviceGeneralInformationDeviceTypeLink', selector: '#deviceGeneralInformationDeviceTypeLink'},
        {ref: 'deviceGeneralInformationDeviceConfigurationLink', selector: '#deviceGeneralInformationDeviceConfigurationLink'},
        {ref: 'dataCollectionIssuesLink', selector: '#dataCollectionIssuesLink'},
        {ref: 'validationFromDate', selector: '#validationFromDate'}
    ],

    init: function () {
        this.control({
            'deviceAdd button[action=save]': {
                click: this.saveDevice
            },
            'deviceAdd button[action=cancel]': {
                click: this.back
            },
            '#validationFromDate': {
                change: this.onValidationFromDateChange
            },
            'deviceSetup #activate': {
                click: this.onActivate
            },
            'deviceSetup #deactivate': {
                click: this.onDeactivate
            },
            'deviceSetup device-connections-list uni-actioncolumn': {
                run: this.connectionRun
            }
        });
    },

    back: function () {
        location.href = "#/devices";
    },

    connectionRun: function (record) {
        var me = this;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.connection.run.success', 'MDC', 'Connection will run immediately')
            );
            record.set('nextExecution', new Date());
        });
    },

    showDeviceDetailsView: function (mRID) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = this.getController('Uni.controller.history.Router');

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);

                var widget = Ext.widget('deviceSetup', {router: router, device: device});
                var deviceLabelsStore = device.labels();
                deviceLabelsStore.getProxy().setUrl(mRID);
                deviceLabelsStore.load(function() {
                    widget.renderFlag(deviceLabelsStore);
                });

                var deviceConnectionsStore = device.connections();
                deviceConnectionsStore.getProxy().setUrl(mRID);
                deviceConnectionsStore.load(function() {
                    widget.down('device-connections-list').reconfigure(deviceConnectionsStore);
                });

                var deviceCommunicationsStore = device.communications();
                deviceCommunicationsStore.getProxy().setUrl(mRID);
                deviceCommunicationsStore.load(function() {
                    widget.down('device-communications-list').reconfigure(deviceCommunicationsStore);
                });

                me.getApplication().fireEvent('changecontentevent', widget);
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().set({href: '#/administration/devicetypes/' + device.get('deviceTypeId')});
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().setHTML(device.get('deviceTypeName'));
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().set({href: '#/administration/devicetypes/' + device.get('deviceTypeId') + '/deviceconfigurations/' + device.get('deviceConfigurationId')});
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().setHTML(device.get('deviceConfigurationName')),
                me.getDeviceCommunicationTopologyPanel().setRecord(device);
                me.getDeviceOpenIssuesPanel().setDataCollectionIssues(device.get('nbrOfDataCollectionIssues'));
                me.getDeviceGeneralInformationForm().loadRecord(device);

                if ((device.get('hasLoadProfiles') || device.get('hasLogBooks') || device.get('hasRegisters'))
                    && (Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration']))) {
                    me.updateDataValidationStatusSection(mRID, widget);
                } else {
                    widget.down('device-data-validation-panel').hide();
                }
                viewport.setLoading(false);

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

        form.getForm().isValid();
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

    showErrorPanel: function (form) {
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
    },

    onActivate: function () {
        this.showActivationConfirmation(this.getDeviceSetupPanel());
    },

    onDeactivate: function () {
        this.showDeactivationConfirmation(this.getDeviceSetupPanel());
    }
});

