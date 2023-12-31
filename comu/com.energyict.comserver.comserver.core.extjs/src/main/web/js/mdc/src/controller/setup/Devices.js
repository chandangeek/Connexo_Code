/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.Devices', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.DeviceAttribute',
        'Mdc.model.Device',
        'Cfg.privileges.Validation',
        'Mdc.widget.DeviceConfigurationField'
    ],
    views: [
        'Mdc.view.setup.device.DeviceSetup',
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.device.DeviceGeneralInformationPanel',
        'Mdc.view.setup.device.DeviceCommunicationTopologyPanel',
        'Mdc.view.setup.device.DeviceOpenIssuesPanel',
        'Mdc.view.setup.device.DeviceAdd',
        'Mdc.view.setup.device.DeviceAttributesForm',
        'Mdc.view.setup.device.DeviceZonesPanel'
    ],

    stores: [
        'AvailableDeviceTypes',
        'AvailableDeviceConfigurations',
        'MasterDeviceCandidates',
        'Mdc.store.DeviceTransitions'
    ],

    mixins: [
        'Mdc.util.DeviceDataValidationActivation'
    ],

    refs: [
        {ref: 'deviceGeneralInformationForm', selector: '#deviceGeneralInformationForm'},
        {ref: 'deviceCommunicationTopologyPanel', selector: '#devicecommicationtopologypanel'},
        {ref: 'deviceZonesPanel', selector: '#deviceZonesPanel'},
        {ref: 'dataLoggerSlavesPanel', selector: '#mdc-dataLoggerSlavesPanel'},
        {ref: 'deviceOpenIssuesPanel', selector: '#deviceopenissuespanel'},
        {ref: 'deviceDataValidationPanel', selector: '#deviceDataValidationPanel'},
        {ref: 'deviceSetup', selector: '#deviceSetup'},
        {ref: 'deviceSetupPanel', selector: '#deviceSetupPanel'},
        {ref: 'deviceGeneralInformationDeviceTypeLink', selector: '#deviceGeneralInformationDeviceTypeLink'},
        {ref: 'deviceGeneralInformationDeviceConfigurationLink', selector: '#deviceGeneralInformationDeviceConfigurationLink'},
        {ref: 'deviceGeneralInformationUsagePointLink', selector: '#deviceGeneralInformationUsagePointLink'},
        {ref: 'deviceIconImage', selector: '#device-information-panel-device-icon'},
        {ref: 'dataCollectionIssuesLink', selector: '#dataCollectionIssuesLink'},
        {ref: 'deviceValidationResultFieldLink', selector: '#lnk-validation-result'},
        {ref: 'validationFromDate', selector: '#validationFromDate'},
        {ref: 'deviceActionsMenu', selector: 'deviceSetup #deviceActionMenu'},
        {ref: 'addDevicePage', selector: 'deviceAdd'},
        {ref: 'deviceConnectionsList', selector: 'device-connections-list'},
        {ref: 'deviceCommunicationsList', selector: 'device-communications-list'},
        {ref: 'deviceGeneralInformationPanel', selector: 'deviceGeneralInformationPanel'}
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
                runNowWithPriority: this.communicationRunPrio,
                toggleActivation: this.communicationToggle
            },
            'deviceSetup #device-communications-panel #activate-all': {
                click: this.communicationActivateAll
            },
            'deviceSetup #device-communications-panel #deactivate-all': {
                click: this.communicationDeactivateAll
            },
            'deviceSetup #deviceSetupPanel #refresh-btn': {
                click: this.onRefreshAction
            }
        });
    },

    back: function () {
        location.href = "#/devices";
    },

    connectionRun: function (record) {
        var me = this,
            widget = this.getDeviceConnectionsList(),
            bodyDataForRequest =
                _.pick(record.getRecordData(), 'id', 'name', 'version', 'parent');

        widget.setLoading(true);
        record.run(function () {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('device.connection.run.now', 'MDC', 'Run succeeded'));
            record.set('nextExecution', new Date());
            me.updateDevice(me.doRefresh);
            widget.setLoading(false);
        }, bodyDataForRequest);
    },

    connectionToggle: function (record) {
        var me = this;
        var connectionMethod = record.get('connectionMethod');
        var widget = this.getDeviceConnectionsList();
        var previousConnectionData = Object.assign({}, record.data);
        var previousConnectionMethod = Object.assign({}, connectionMethod);

        connectionMethod.status = connectionMethod.status == 'active' ? 'inactive' : 'active';
        // COMU-705 : the save() failed due to the fact that for the dates numbers are expected (but strings were passed)
        record.data.startDateTime = Number(Ext.Date.format(record.data.startDateTime, 'time'));
        record.data.endDateTime = Number(Ext.Date.format(record.data.endDateTime, 'time'));
        record.data.nextExecution = Number(Ext.Date.format(record.data.nextExecution, 'time'));
        record.set('connectionMethod', connectionMethod);
        widget.setLoading(true);
        record.deactivate(function (recordData, success, request) {
            if (success) {
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('device.connection.toggle', 'MDC', 'Connection status changed to {0}',[connectionMethod.status])
                );
                me.updateDevice(me.doRefresh);
            }
            else{
                record.data = previousConnectionData;
                record.set('connectionMethod', previousConnectionMethod);
            }
            widget.setLoading(false);
        }, _.pick(record.getRecordData(), 'id', 'name', 'version', 'parent', 'connectionMethod'));
    },

    communicationToggle: function (record) {
        var me = this,
            status = !record.get('isOnHold'),
            widget = this.getDeviceCommunicationsList(),
            bodyDataForRequest = {
                device: _.pick(me.getDevice().getRecordData(), 'name', 'version', 'parent')
            };

        widget.setLoading(true);
        if (status) {
            record.deactivate(function () {
                me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.toggle.deactivate', 'MDC', 'Communication task deactivated'));
                me.updateDevice(me.doRefresh);
                widget.setLoading(false);
            }, bodyDataForRequest);
        } else {
            record.activate(function () {
                me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.toggle.activate', 'MDC', 'Communication task activated'));
                me.updateDevice(me.doRefresh);
                widget.setLoading(false);
            }, bodyDataForRequest);
        }
    },

    communicationActivateAll: function () {
        var me = this,
            widget = this.getDeviceCommunicationsList(),
            router = this.getController('Uni.controller.history.Router');

        widget.setLoading(true);
        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/ddr/devices/{deviceId}/comtasks/activate'.replace('{deviceId}', router.arguments.deviceId),
            isNotEdit: true,
            jsonData: {
                device: _.pick(me.getDevice().getRecordData(), 'name', 'version', 'parent')
            },
            success: function () {
                me.updateDevice(me.refreshCommunications);
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('device.communication.activateAll', 'MDC', 'Communication tasks activated')
                );
                widget.setLoading(false);
            }
        });
    },

    communicationDeactivateAll: function () {
        var me = this,
            widget = this.getDeviceCommunicationsList(),
            router = this.getController('Uni.controller.history.Router');

        widget.setLoading(true);
        Ext.Ajax.request({
            method: 'PUT',
            url: '/api/ddr/devices/{deviceId}/comtasks/deactivate'.replace('{deviceId}', router.arguments.deviceId),
            isNotEdit: true,
            jsonData: {
                device: _.pick(me.getDevice().getRecordData(), 'name', 'version', 'parent')
            },
            success: function () {
                me.updateDevice(me.refreshCommunications);
                me.getApplication().fireEvent('acknowledge',
                    Uni.I18n.translate('device.communication.deactivateAll', 'MDC', 'Communication tasks deactivated')
                );
                widget.setLoading(false);
            }
        });
    },

    communicationRun: function (record) {
        var me = this,
            widget = this.getDeviceCommunicationsList();

        widget.setLoading(true);
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.wait', 'MDC', 'Run succeeded')
            );
            record.set('plannedDate', new Date());
            me.updateDevice(me.doRefresh);
            widget.setLoading(false);
        }, {
            device: _.pick(me.getDevice().getRecordData(), 'name', 'version', 'parent')
        });
    },

    communicationRunNow: function (record) {
        var me = this,
            widget = this.getDeviceCommunicationsList();

        widget.setLoading(true);
        record.runNow(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.now', 'MDC', 'Run now succeeded')
            );
            record.set('plannedDate', new Date());
            me.updateDevice(me.doRefresh);
            widget.setLoading(false);
        }, {
            device: _.pick(me.getDevice().getRecordData(), 'name', 'version', 'parent')
        });
    },

    communicationRunPrio: function (record) {
        var me = this,
            widget = this.getDeviceCommunicationsList();

        widget.setLoading(true);
        record.runNowWithPriority(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.prio', 'MDC', 'Run with priority succeeded')
            );
            record.set('plannedDate', new Date());
            me.updateDevice(me.doRefresh);
            widget.setLoading(false);
        }, {
            device: _.pick(me.getDevice().getRecordData(), 'name', 'version', 'parent')
        });
    },

    hasSapCas: function(deviceId, callback){
        var me = this;
        var url = '/api/sap/devices/' + deviceId + '/hassapcas';
        Ext.Ajax.request({
            url: url,
            method: 'GET',
            success: function (response) {
                var hasSapCas = Ext.JSON.decode(response.responseText);
                callback(hasSapCas && hasSapCas.value);
            },
            failure: function(){
                callback(false);
            }
        });
    },

    showDeviceDetailsView: function (deviceId) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = this.getController('Uni.controller.history.Router'),
            attributesModel = Ext.ModelManager.getModel('Mdc.model.DeviceAttribute'),
            transitionsStore = Ext.StoreManager.get('Mdc.store.DeviceTransitions'),
            updateDeviceSummary = function (attributes) {
                Ext.suspendLayouts();
                me.getDeviceGeneralInformationForm().loadRecord(attributes);
                if(!Ext.isEmpty(attributes.get('deviceIcon'))) {
                    me.getDeviceIconImage().show();
                    me.getDeviceIconImage().setSrc('data:image;base64,' + attributes.get('deviceIcon'));
                } else {
                    me.getDeviceIconImage().hide();
                }
                me.getDeviceSetup().down('#deviceSetupPanel #last-updated-field')
                    .update(Uni.I18n.translate('general.lastRefreshedAt', 'MDC', 'Last refreshed at {0}', Uni.DateTime.formatTimeShort(new Date())));
                Ext.resumeLayouts(true);
            };

        viewport.setLoading();
        me.hasSapCas(deviceId, function(hasSapCas){
            transitionsStore.getProxy().setExtraParam('deviceId', deviceId);
            attributesModel.getProxy().setExtraParam('deviceId', deviceId);

            transitionsStore.load({
                callback: function () {
                    Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
                        success: function (device) {
                            me.getApplication().fireEvent('loadDevice', device);

                            var widget = Ext.widget('deviceSetup', {
                                router: router,
                                device: device,
                                actionsStore: transitionsStore,
                                hasSapCas: hasSapCas
                            });
                            var deviceLabelsStore = device.labels();
                            deviceLabelsStore.getProxy().setExtraParam('deviceId', deviceId);
                            deviceLabelsStore.load(function () {
                                widget.renderFlag(deviceLabelsStore);
                            });

                            if (me.getDeviceGeneralInformationPanel().rendered) {
                                attributesModel.load('attributes', { success: updateDeviceSummary });
                            } else {
                                me.getDeviceGeneralInformationPanel().on('afterrender', function () {
                                    attributesModel.load('attributes', { success: updateDeviceSummary });
                                }, me, {single:true});
                            }

                            me.getApplication().fireEvent('changecontentevent', widget);

                            me.doRefresh();

                            if (!Ext.isEmpty(me.getDeviceCommunicationTopologyPanel())) {
                                if (device.get('isDataLoggerSlave') || device.get('isMultiElementSlave')) {
                                    me.getDeviceCommunicationTopologyPanel().hide();
                                } else {
                                    me.getDeviceCommunicationTopologyPanel().setRecord(device);
                                }
                            }

                            if (!Ext.isEmpty(me.getDeviceZonesPanel())) {
                                if( device.get('zones').length == 0)
                                    me.getDeviceZonesPanel().hide();
                                else
                                    me.getDeviceZonesPanel().setRecord(device);
                            }

                            if (!Ext.isEmpty(me.getDataLoggerSlavesPanel())) {
                                var dataLoggerSlavesPanel = me.getDataLoggerSlavesPanel();
                                dataLoggerSlavesPanel.setDevice(device);
                                dataLoggerSlavesPanel.setSlaveStore(me.createDataLoggerSlavesStore(device));
                            }
                            if (!Ext.isEmpty(me.getDeviceOpenIssuesPanel())) {
                                me.getDeviceOpenIssuesPanel().setDataCollectionIssues(device);
                            }
                            if ((device.get('hasLoadProfiles') || device.get('hasLogBooks') || device.get('hasRegisters'))
                                && Cfg.privileges.Validation.canUpdateDeviceValidation()) {
                                me.updateDataValidationStatusSection(deviceId, widget, device);
                            } else {
                                !Ext.isEmpty(widget.down('device-data-validation-panel')) && widget.down('device-data-validation-panel').hide();
                            }
                            viewport.setLoading(false);

                        }
                    });
                }
            });
        });
    },

    createDataLoggerSlavesStore: function(device) {
        var me = this,
            counter,
            maxNumberOfStoreRecords = 5,
            store,
            slaves = [];

        // 1. Collect all datalogger slaves in an array
        Ext.Array.forEach(device.get('dataLoggerSlaveDevices'), function(slaveRecord){
            if (slaveRecord.id > 0) {
                slaves.push(slaveRecord);
            }
        }, me);
        // 2. Sort that array descending
        Ext.Array.sort(slaves, function(slave1, slave2) {
            return slave2.linkingTimeStamp - slave1.linkingTimeStamp;
        });
        // 3. If the array contains too much items, only keep the first <maxNumberOfStoreRecords>
        if (slaves.length > maxNumberOfStoreRecords) {
            slaves = Ext.Array.splice(slaves, maxNumberOfStoreRecords, slaves.length - maxNumberOfStoreRecords);
        }

        // 4. Create the store
        Ext.define('DataLoggerSlave', {
            extend: 'Ext.data.Model',
            fields: [
                {name: 'name', type: 'string'},
                {name: 'deviceTypeName', type: 'string'},
                {name: 'deviceConfigurationName', type: 'string'},
                {name: 'deviceTypePurpose', type: 'string'},
                {name: 'linkingTimeStamp', type: 'number'}
            ]
        });
        store = Ext.create('Ext.data.Store', {
            model: 'DataLoggerSlave',
            autoLoad: false
        });
        Ext.Array.forEach(slaves, function(slaveRecord){
            store.add({
                name: slaveRecord.name,
                deviceTypeName: slaveRecord.deviceTypeName,
                deviceConfigurationName: slaveRecord.deviceConfigurationName,
                deviceTypePurpose : slaveRecord.deviceTypePurpose,
                linkingTimeStamp: slaveRecord.linkingTimeStamp
            });
        }, me);
        return store;
    },

    doRefresh: function () {
        this.refreshConnections();
        this.refreshCommunications();
        this.refreshWhatsGoingOn();
    },

    onRefreshAction: function () {  // CXO-10446
        var me = this,
            router = this.getController('Uni.controller.history.Router');
        me.showDeviceDetailsView(router.arguments.deviceId);
    },

    refreshWhatsGoingOn: function(){
        var widget = this.getDeviceSetup();
        var whatsGoingOnWidget = widget.down('whatsgoingon');
        whatsGoingOnWidget.buildWidget();
    },

    refreshConnections: function () {
        var widget = this.getDeviceSetup(),
            device = widget.device,
            lastUpdateField = widget.down('#deviceSetupPanel #last-updated-field'),
            deviceConnectionsStore = device.connections(),
            connectionsList = widget.down('device-connections-list');

        if (connectionsList) {
            connectionsList.bindStore(deviceConnectionsStore);
            deviceConnectionsStore.getProxy().setExtraParam('deviceId', device.get('name'));
            lastUpdateField.update(Uni.I18n.translate('general.lastRefreshedAt', 'MDC', 'Last refreshed at {0}', Uni.DateTime.formatTimeShort(new Date())));
            deviceConnectionsStore.load(function (records) {
                if (!widget.isDestroyed) {
                    !!widget.down('#connectionslist') && widget.down('#connectionslist').setTitle(Uni.I18n.translate('device.connections.title', 'MDC', 'Connections ({0})', records.length));
                }
            });
        }
    },

    refreshCommunications: function () {
        var widget = this.getDeviceSetup();
        var device = widget.device;
        var lastUpdateField = widget.down('#deviceSetupPanel #last-updated-field');
        var deviceCommunicationsStore = device.communications();
        var communicationsList = widget.down('device-communications-list');

        if (communicationsList) {
            communicationsList.bindStore(deviceCommunicationsStore);
            deviceCommunicationsStore.getProxy().setExtraParam('deviceId', device.get('name'));
            lastUpdateField.update(Uni.I18n.translate('general.lastRefreshedAt', 'MDC', 'Last refreshed at {0}', Uni.DateTime.formatTimeShort(new Date())));
            deviceCommunicationsStore.load(function (records) {
                if (!widget.isDestroyed && !Ext.isEmpty(widget.down('#communicationslist'))) {
                    widget.down('#communicationslist').setTitle(
                        Uni.I18n.translate('device.communicationTasks.title', 'MDC', 'Communication tasks ({0})', records ? records.length : 0)
                    );
                }
            });
        }
    },

    showAddDevice: function () {
        var widget = Ext.widget('deviceAdd');
        widget.down('form').loadRecord(Ext.create('Mdc.model.Device'));
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();
        widget.down('#deviceConfiguration').getDeviceTypeStore().load(function () {
            widget.setLoading(false);
        });
    },

    saveDevice: function (button) {
        var me = this;
        var form = button.up('form');

        if (!form.getForm().isValid()) {
            me.showErrorPanel(form);
            return;
        }

        form.updateRecord();
        var deviceType = form.down('#deviceConfiguration').getDeviceType();
        var deviceConfig = form.down('#deviceConfiguration').getDeviceConfiguration();
        if (!deviceConfig){
            form.getRecord().set('deviceTypeId', null);
            form.getRecord().set('deviceConfigurationId', null);
        }else{
            form.getRecord().set('deviceTypeId', deviceType.get('id'));
            form.getRecord().set('deviceConfigurationId', deviceConfig.get('id'));
        }
        form.getRecord().set('shipmentDate', form.down('#deviceAddShipmentDate').getValue().getTime());
        me.getAddDevicePage().setLoading();
        form.getRecord().save({
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceAdd.added', 'MDC', 'Device added'));
                location.href = "#/devices/" + encodeURIComponent(record.get('name'));
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        var errorsToShow = [];
                        Ext.each(json.errors, function (item) {
                            if (item.id != 'deviceType') { // JP-6865 #hide device type error returned from backend
                                errorsToShow.push(item)
                            } else {
                                if (!deviceType) {
                                    errorsToShow.push(item);
                                }
                            }
                        });
                        me.showErrorPanel(form);
                        form.getForm().markInvalid(errorsToShow);
                    }
                }
            },
            callback: function () {
                me.getAddDevicePage().setLoading(false);
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
                html: Uni.I18n.translate('general.formErrors', 'MDC', 'There are errors on this page that require your attention.')
            });
        formErrorsPlaceHolder.show();
    },

    onActivate: function () {
        this.showActivationConfirmation(this.getDeviceSetupPanel());
    },

    onDeactivate: function () {
        this.showDeactivationConfirmation(this.getDeviceSetupPanel());
    },

    getDevice: function () {
        var me = this,
            page = me.getDeviceSetup(),
            device;

        if (page) {
            device = page.device;
        }

        return device
    },

    updateDevice: function (callback) {
        var me = this,
            page = me.getDeviceSetup();

        me.getModel('Mdc.model.Device').load(page.device.get('name'), {
            success: function (record) {
                if (page.rendered) {
                    page.device = record;
                    callback.call(me);
                }
            }
        });
    }
});

