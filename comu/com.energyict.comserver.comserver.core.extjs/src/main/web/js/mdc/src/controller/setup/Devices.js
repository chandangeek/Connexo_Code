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
                run: this.connectionRun,
                toggleActivation: this.connectionToggle
            },
            'deviceSetup device-communications-list uni-actioncolumn': {
                run: this.communicationRun,
                runNow: this.communicationRunNow,
                toggleActivation: this.communicationToggle
            },
            'deviceSetup #device-communications-panel #activate-all': {
                click: this.communicationActivateAll
            },
            'deviceSetup #device-communications-panel #deactivate-all': {
                click: this.communicationDeactivateAll
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
                Uni.I18n.translate('device.connection.run.now', 'MDC', 'Connection will run immediately')
            );
            record.set('nextExecution', new Date());
        });
    },

    connectionToggle: function (record) {
        var me = this;
        var connectionMethod = record.get('connectionMethod');
        connectionMethod.status = connectionMethod.status == 'active' ? 'inactive' : 'active';
        record.set('connectionMethod', connectionMethod);
        record.save({
            callback: function (record, operation, success) {
                if (success) {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('device.connection.toggle.' + connectionMethod.status, 'MDC', 'Connection status changed to: ' + connectionMethod.status)
                    );
                }
            }
        });
    },

    communicationToggle: function (record) {
        var me = this;
        var status = !record.get('isOnHold');
        record.set('isOnHold', status);
        record.save({
            callback: function (record, operation, success) {
                if (success) {
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translate('device.communication.toggle.' + status, 'MDC', 'Communication status changed to: ' + status)
                    );
                }
            }
        });
    },

    communicationActivateAll: function() {
        var me = this,
            router = this.getController('Uni.controller.history.Router');

        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/ddr/devices/{mRID}/communications/activate'.replace('{mRID}', router.arguments.mRID),
            success: function() {
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('device.communication.activateAll', 'MDC', 'Communication tasks activated')
                );
            }
        });
    },

    communicationDeactivateAll: function() {
        var me = this,
            router = this.getController('Uni.controller.history.Router');

        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/ddr/devices/{mRID}/communications/deactivate'.replace('{mRID}', router.arguments.mRID),
            success: function() {
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('device.communication.deactivateAll', 'MDC', 'Communication tasks deactivated')
                );
            }
        });
    },

    communicationRun: function (record) {
        var me = this;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.wait', 'MDC', 'Communication task will wait')
            );
            record.set('plannedDate', new Date());
        });
    },

    communicationRunNow: function (record) {
        var me = this;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.now', 'MDC', 'Communication task will run immediately')
            );
            record.set('plannedDate', new Date());
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
                    widget.down('#device-connections-panel').bindStore(deviceConnectionsStore);
                });

                var deviceCommunicationsStore = device.communications();
                deviceCommunicationsStore.getProxy().setUrl(mRID);
                deviceCommunicationsStore.load(function() {
                    widget.down('#device-communications-panel').bindStore(deviceCommunicationsStore);
                });

                me.getApplication().fireEvent('changecontentevent', widget);
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().set({href: '#/administration/devicetypes/' + device.get('deviceTypeId')});
                me.getDeviceGeneralInformationDeviceTypeLink().getEl().setHTML(device.get('deviceTypeName'));
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().set({href: '#/administration/devicetypes/' + device.get('deviceTypeId') + '/deviceconfigurations/' + device.get('deviceConfigurationId')});
                me.getDeviceGeneralInformationDeviceConfigurationLink().getEl().setHTML(device.get('deviceConfigurationName'));
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

