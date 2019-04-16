/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.CommunicationTasks', {
    extend: 'Ext.app.Controller',
    deviceTypeId: null,
    deviceConfigurationId: null,

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'setup.communicationtask.CommunicationTaskSetup',
        'setup.communicationtask.CommunicationTaskPreview',
        'setup.communicationtask.CommunicationTaskGrid',
        'setup.communicationtask.CommunicationTaskEdit'
    ],

    stores: [
        'CommunicationTaskConfigsOfDeviceConfiguration',
        'ComTasks',
        'SecuritySettingsOfDeviceConfiguration',
        'ConnectionMethodsOfDeviceConfiguration',
        'ConnectionMethodsOfDeviceConfigurationCombo',
        'ConnectionFunctions'
    ],

    refs: [
        {ref: 'communicationTaskGridPanel', selector: 'communicationTaskSetup communicationTaskGrid'},
        {ref: 'communicationTaskPreviewPanel', selector: 'communicationTaskSetup communicationTaskPreview'},
        {ref: 'communicationTaskPreviewForm', selector: 'communicationTaskSetup #communicationTaskPreviewForm'},
        {ref: 'communicationTaskEditForm', selector: '#communicationTaskEditForm'},
        {ref: 'communicationTaskEditPanel', selector: '#communicationTaskEdit'},
        {ref: 'communicationTaskSetupPanel', selector: '#communicationTaskSetup'},
        {ref: 'scheduleField', selector: '#communicationTaskEditForm #scheduleField #scheduleFieldItem'}
    ],

    init: function () {
        var me = this;
        me.control({
            'communicationTaskSetup communicationTaskGrid': {
                select: me.loadCommunicationTaskDetail
            },
            '#communicationTaskSetup': {
                afterrender: me.loadCommunicationTasksStore
            },
            '#addEditButton[action=addCommunicationTaskAction]': {
                click: me.addCommunicationTask
            },
            '#addEditButton[action=editCommunicationTaskAction]': {
                click: me.editCommunicationTask
            },
            '#communicationTaskEditForm #scheduleField #enableScheduleFieldItem': {
                change: me.enableScheduleFieldItemChanged
            },
            'menu menuitem[action=editcommunicationtask]': {
                click: me.editCommunicationTaskHistory
            },
            'menu menuitem[action=removecommunicationtask]': {
                click: me.removeCommunicationTask
            },
            'menu menuitem[action=activatecommunicationtask]': {
                click: me.activateCommunicationTask
            }
        });
    },

    setupMenuItems: function (record) {
        var me = this,
            suspended = record.get('suspended');

        if (!Ext.isEmpty(suspended)) {
            var textKey = ((suspended == true) ? 'general.activate' : 'general.deactivate'),
                text = ((suspended == true) ? 'Activate' : 'Deactivate'),
                menuItems = Ext.ComponentQuery.query('menu menuitem[action=activatecommunicationtask]');

            if (!Ext.isEmpty(menuItems)) {
                Ext.Array.each(menuItems, function (item) {
                    item.setText(Uni.I18n.translate(textKey, 'MDC', text));
                });
            }
        }
    },

    enableScheduleFieldItemChanged: function (item, newValue, oldValue, eOpts) {
        var me = this;

        if (newValue === false) {
            me.getScheduleField().clearOnlyOffsetValues();
            me.getScheduleField().setValue({
                every: {
                    count: 15,
                    timeUnit: 'minutes'
                },
                offset: {
                    count: 0,
                    timeUnit: 'seconds'
                }
            });
            me.getScheduleField().disable();
        } else {
            me.getScheduleField().enable();
        }
    },

    loadCommunicationTasksStore: function () {
        var me = this;

        me.getCommunicationTaskConfigsOfDeviceConfigurationStore().load({
            params: {
                sort: 'name'
            }
        });
    },

    loadCommunicationTaskDetail: function (rowmodel, record, index) {
        var me = this;
        me.previewConnectionTask(record);
        me.setupMenuItems(record);
    },

    previewConnectionTask: function (record) {
        var me = this,
            form = me.getCommunicationTaskPreviewForm();
        form.setLoading(Uni.I18n.translate('general.loading', 'MDC', 'Loading...'));
        me.getCommunicationTaskPreviewPanel().setTitle(Ext.String.htmlEncode(record.getData().comTask.name));
        form.loadRecord(record);
        form.setLoading(false);
    },

    showCommunicationTasks: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            widget,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            proxy = me.getCommunicationTaskConfigsOfDeviceConfigurationStore().getProxy();

        if (mainView) mainView.setLoading(Uni.I18n.translate('general.loading', 'MDC', 'Loading...'));
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;

        proxy.setExtraParam("deviceType", deviceTypeId);
        proxy.setExtraParam("deviceConfig", deviceConfigurationId);

        me.getCommunicationTaskConfigsOfDeviceConfigurationStore().load({
            callback: function () {
                widget = Ext.widget('communicationTaskSetup', {
                    deviceTypeId: deviceTypeId,
                    deviceConfigurationId: deviceConfigurationId
                });
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        model.getProxy().setExtraParam('deviceType', deviceTypeId);
                        model.load(deviceConfigurationId, {
                            success: function (deviceConfig) {
                                if (mainView) mainView.setLoading(false);
                                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                                widget.down('#stepsMenu').setHeader(deviceConfig.get('name'));
                                me.getApplication().fireEvent('changecontentevent', widget);
                            }
                        });
                    }
                });
            }
        });

    },

    showAddCommunicationTaskView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            comTasksStore = me.getComTasksStore(),
            securityPropertySetsStore = me.getSecuritySettingsOfDeviceConfigurationStore(),
            connectionMethodsStore = me.getConnectionMethodsOfDeviceConfigurationComboStore(),
            connectionFunctionsStore = me.getConnectionFunctionsStore(),
            defaultConnectionMethod;

        defaultConnectionMethod = Ext.create('Mdc.model.ConnectionMethod', {
            id: 0,
            name: Uni.I18n.translate('communicationtasks.form.selectDefaultPartialConnectionTask', 'MDC', 'Use the default connection method')
        });

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        var widget = Ext.widget('communicationTaskEdit', {
            edit: false,
            returnLink: '#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(me.deviceConfigurationId) + '/comtaskenablements',
            comTasksStore: comTasksStore,
            securityPropertySetsStore: securityPropertySetsStore,
            connectionMethodsStore: connectionMethodsStore,
            connectionFunctionsStore: connectionFunctionsStore
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        comTasksStore.getProxy().extraParams = ({
                            deviceType: deviceTypeId,
                            deviceConfig: deviceConfigurationId,
                            available: true
                        });
                        comTasksStore.getProxy().pageParam = false;
                        comTasksStore.getProxy().limitParam = false;
                        comTasksStore.getProxy().startParam = false;
                        comTasksStore.load({
                            callback: function () {
                                connectionMethodsStore.getProxy().setUrl(deviceTypeId, deviceConfigurationId);
                                connectionMethodsStore.load({
                                    callback: function () {
                                        connectionFunctionsStore.getProxy().extraParams = ({deviceType: deviceTypeId});
                                        connectionFunctionsStore.getProxy().setExtraParam('connectionFunctionType', 1); // 1 = the consumable connection functions
                                        connectionFunctionsStore.load({
                                            callback: function (records) {
                                                securityPropertySetsStore.getProxy().extraParams = ({
                                                    deviceType: deviceTypeId,
                                                    deviceConfig: deviceConfigurationId
                                                });
                                                me.addSpecialConnectionMethodsToConnectionMethodsStore(connectionMethodsStore, defaultConnectionMethod, records);
                                                securityPropertySetsStore.load({
                                                    callback: function () {
                                                        var title = Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task configuration');
                                                        widget.down('#communicationTaskEditForm').setTitle(title);
                                                        widget.down('#partialConnectionTaskComboBox').setValue(0);
                                                        widget.setLoading(false);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    },

    addSpecialConnectionMethodsToConnectionMethodsStore: function (connectionMethodsStore, defaultConnectionMethod, records) {
        connectionMethodsStore.add(defaultConnectionMethod);
        for (var i = 0; i < records.length; i++) {
            connectionMethodsStore.add(Ext.create('Mdc.model.ConnectionMethod', {
                    id: -records[i].getData()['id'], // Negate the connection function id and use it as special connection method id
                    name: Uni.I18n.translate(
                        'communicationtasks.form.selectPartialConnectionTaskBasedOnConnectionFunction',
                        'MDC',
                        'Connection method with \'{0}\' function',
                        records[i].getData()['localizedValue']
                    )
                })
            );
        }
    },

    editCommunicationTaskHistory: function () {
        var me = this,
            grid = me.getCommunicationTaskGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        location.href = '#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(me.deviceConfigurationId) + '/comtaskenablements/' + encodeURIComponent(lastSelected.getData().id) + '/edit';
    },

    showEditCommunicationTaskView: function (deviceTypeId, deviceConfigurationId, comTaskEnablementId) {
        var me = this,
            comTasksStore = me.getComTasksStore(),
            securityPropertySetsStore = me.getSecuritySettingsOfDeviceConfigurationStore(),
            connectionMethodsStore = me.getConnectionMethodsOfDeviceConfigurationComboStore(),
            connectionFunctionsStore = me.getConnectionFunctionsStore(),
            model = Ext.ModelManager.getModel('Mdc.model.CommunicationTaskConfig'),
            defaultConnectionMethod;
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;

        defaultConnectionMethod = Ext.create('Mdc.model.ConnectionMethod', {
            id: 0,
            name: Uni.I18n.translate('communicationtasks.form.selectDefaultPartialConnectionTask', 'MDC', 'Use the default connection method')
        });

        model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        model.load(comTaskEnablementId, {
            success: function (communicationTask) {
                me.getApplication().fireEvent('loadCommunicationTaskModel', communicationTask);
                var widget = Ext.widget('communicationTaskEdit', {
                    edit: true,
                    returnLink: '#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(me.deviceConfigurationId) + '/comtaskenablements',
                    comTasksStore: comTasksStore,
                    securityPropertySetsStore: securityPropertySetsStore,
                    connectionMethodsStore: connectionMethodsStore,
                    connectionFunctionsStore: connectionFunctionsStore
                });
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.setLoading(true);
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        model.getProxy().setExtraParam('deviceType', deviceTypeId);
                        model.load(deviceConfigurationId, {
                            success: function (deviceConfig) {
                                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                                securityPropertySetsStore.getProxy().extraParams = ({
                                    deviceType: deviceTypeId,
                                    deviceConfig: deviceConfigurationId
                                });
                                securityPropertySetsStore.load({
                                    callback: function () {
                                        connectionMethodsStore.getProxy().extraParams = ({
                                            deviceType: deviceTypeId,
                                            deviceConfig: deviceConfigurationId
                                        });
                                        connectionMethodsStore.load({
                                            callback: function () {
                                                connectionFunctionsStore.getProxy().extraParams = ({deviceType: deviceTypeId});
                                                connectionFunctionsStore.getProxy().setExtraParam('connectionFunctionType', 1); // 1 = the consumable connection functions
                                                connectionFunctionsStore.load({
                                                    callback: function (records) {
                                                        me.addSpecialConnectionMethodsToConnectionMethodsStore(connectionMethodsStore, defaultConnectionMethod, records);
                                                        widget.down('form').loadRecord(communicationTask);
                                                        var comTaskName = '';
                                                        if (!Ext.isEmpty(communicationTask.get('comTask'))) {
                                                            comTaskName = communicationTask.get('comTask').name;
                                                        }
                                                        var title = Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [comTaskName]);
                                                        widget.down('#communicationTaskEditForm').setTitle(title);
                                                        widget.setValues(communicationTask);
                                                        widget.setLoading(false);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    },

    addCommunicationTask: function () {
        var me = this;
        me.setPreLoader(me.getCommunicationTaskEditPanel(), Uni.I18n.translate('communicationtasks.creating', 'MDC', 'Creating communication task'));
        me.updateCommunicationTask('add');
    },

    editCommunicationTask: function () {
        var me = this;
        me.setPreLoader(me.getCommunicationTaskEditPanel(), Uni.I18n.translate('communicationtasks.updating', 'MDC', 'Updating communication task'));
        me.updateCommunicationTask('edit');
    },

    removeCommunicationTask: function () {
        var me = this,
            grid = me.getCommunicationTaskGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('communicationtasks.deleteCommunicationTask.message', 'MDC', "For this device configuration it won't be possible anymore to execute the corresponding communication task"),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", [lastSelected.get('comTask').name]),
            config: {
                communicationTaskToDelete: lastSelected,
                me: me
            },
            fn: me.removeCommunicationTaskRecord
        });
    },

    activateCommunicationTask: function () {
        var me = this,
            grid = me.getCommunicationTaskGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            suspended = lastSelected.get('suspended'),
            acknowledgeMessage;

        if (!Ext.isEmpty(suspended)) {
            var action = ((suspended == true) ? 'activate' : 'deactivate');

            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements/' + lastSelected.getData().id + '/' + action,
                method: 'PUT',
                isNotEdit: true,
                jsonData: lastSelected.getRecordData(),
                success: function () {
                    if (suspended == true) {
                        acknowledgeMessage = Uni.I18n.translate('communicationtasks.activated', 'MDC', 'Communication task configuration activated');
                    } else {
                        acknowledgeMessage = Uni.I18n.translate('communicationtasks.deactivated', 'MDC', 'Communication task configuration deactivated');
                    }
                    me.getApplication().fireEvent('acknowledge', acknowledgeMessage);
                    me.loadCommunicationTasksStore();
                },
                failure: function (response, request) {
                    if (response.status == 400) {
                        var errorText = Uni.I18n.translate('general.error.unknown', 'MDC', 'Unknown error occurred'),
                            errorCode = '';
                        if (!Ext.isEmpty(response.statusText)) {
                            errorText = response.statusText;
                        }
                        if (!Ext.isEmpty(response.responseText)) {
                            var json = Ext.decode(response.responseText, true);

                            if (json && json.error) {
                                errorText = json.error;
                            }
                            if (json && json.errorCode) {
                                errorCode = json.errorCode;
                            }
                        }

                        var title = Uni.I18n.translate("communicationtasks.activation.failure.title", 'MDC', 'Couldn\'t perform your action'),
                            msgKey = ((suspended == true) ? 'communicationtasks.activate.operation.failed' : 'communicationtasks.deactivate.operation.failed'),
                            msgValue = ((suspended == true) ? 'Activate operation failed' : 'Deactivate operation failed');

                        me.getApplication().getController('Uni.controller.Error').showError(title, Uni.I18n.translate(msgKey, 'MDC', msgValue) + "." + errorText, errorCode);
                    }
                }
            });
        }
    },

    updateCommunicationTask: function (operation) {
        var me = this,
            form = me.getCommunicationTaskEditForm();
        if (!me.getCommunicationTaskEditForm().down('#communicationTaskEditFormErrors').isHidden()) {
            me.hideErrorPanel();
            me.getCommunicationTaskEditForm().down('#comTaskComboBox').clearInvalid();
            me.getCommunicationTaskEditForm().down('#securityPropertySetComboBox').clearInvalid();
        }
        me[operation + 'CommunicationTaskRecord'](form.getValues(), {operation: operation});
    },

    editCommunicationTaskRecord: function (values, cfg) {
        var me = this,
            record = me.getCommunicationTaskEditForm().getRecord();
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('comtask.saved', 'MDC', 'Communication task saved')
        }, cfg));
    },

    addCommunicationTaskRecord: function (values, cfg) {
        var me = this,
            record = Ext.create(Mdc.model.CommunicationTaskConfig);
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('communicationtasks.created', 'MDC', 'Communication task added')
        }, cfg));
    },

    removeCommunicationTaskRecord: function (btn, text, cfg) {
        if (btn === 'confirm') {
            var me = cfg.config.me,
                communicationTaskToDelete = cfg.config.communicationTaskToDelete;
            communicationTaskToDelete.getProxy().extraParams = ({
                deviceType: me.deviceTypeId,
                deviceConfig: me.deviceConfigurationId
            });
            communicationTaskToDelete.destroy({
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute().forward();
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationtasks.removed', 'MDC', 'Communication task removed'));
                }
            });
        }
    },

    updateRecord: function (record, values, cfg) {
        var me = this,
            backUrl = me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/deviceconfigurations/view/comtaskenablements').buildUrl();

        if (record) {
            me.setRecordValues(record, values);
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            record.save({
                backUrl: backUrl,
                success: function (record) {
                    location.href = backUrl;
                    me.getApplication().fireEvent('acknowledge', cfg.successMessage);
                },
                failure: function (record, operation) {
                    if (operation.response.status == 400) {
                        var errorText = Uni.I18n.translate('general.error.unknown', 'MDC', 'Unknown error occurred'),
                            errorCode;
                        if (!Ext.isEmpty(operation.response.statusText)) {
                            errorText = operation.response.statusText;
                        }
                        if (!Ext.isEmpty(operation.response.responseText)) {
                            var json = Ext.decode(operation.response.responseText, true);
                            if (json && json.errors) {
                                me.showErrorPanel();
                                me.getCommunicationTaskEditForm().getForm().markInvalid(json.errors);
                                Ext.Array.each(json.errors, function (item) {
                                    item.id.indexOf('comTask') !== -1 && me.getCommunicationTaskEditForm().down('#comTaskComboBox').markInvalid(item.msg);
                                    item.id.indexOf('securityPropertySet') !== -1 && me.getCommunicationTaskEditForm().down('#securityPropertySetComboBox').markInvalid(item.msg);
                                });
                                return;
                            }
                            if (json && json.error) {
                                errorText = json.error;
                            }
                            if (json && json.errorCode) {
                                errorCode = json.errorCode;
                            }
                        }
                        var title = Uni.I18n.translate("communicationtasks.edit.failure.title", 'MDC', 'Couldn\'t perform your action'),
                            msgKey = ((cfg.operation == 'add') ? 'communicationtasks.add.operation.failed' : 'general.edit.operation.failed'),
                            msgValue = ((cfg.operation == 'add') ? 'Add operation failed' : 'Update operation failed');

                        me.getApplication().getController('Uni.controller.Error').showError(title, Uni.I18n.translate(msgKey, 'MDC', msgValue) + "." + errorText, errorCode);
                    }
                },
                callback: function () {
                    me.clearPreLoader();
                }
            });
        }
    },

    setRecordValues: function (record, values) {
        record.set("comTask", {
            id: values.comTaskId,
            name: Ext.isEmpty(values.comTaskName) ? undefined : values.comTaskName
        });
        record.set("securityPropertySet", {id: values.securityPropertySetId});
        if (!Ext.isEmpty(values.partialConnectionTaskId)) {
            if (values.partialConnectionTaskId >= 0) {
                record.set("partialConnectionTask", {id: values.partialConnectionTaskId});
                record.set('connectionFunctionInfo', undefined);
            } else { // If the partialConnectionTaskId is below zero, then these ids should actually be considered as connection function ids.
                record.set("connectionFunctionInfo", {id: -values.partialConnectionTaskId}); // Negate again to convert to actual connection function id
                record.set("partialConnectionTask", undefined);
            }
        }

        record.set("priority", values.priority);
        record.set("ignoreNextExecutionSpecsForInbound", values.ignoreNextExecutionSpecsForInbound);
        record.set("maxNumberOfTries", values.maxNumberOfTries);
    },

    setPreLoader: function (target, message) {
        var me = this;
        me.preloader = Ext.create('Ext.LoadMask', {
            msg: message,
            target: target
        });
        me.preloader.show();
    },

    clearPreLoader: function () {
        var me = this;
        if (!Ext.isEmpty(me.preloader)) {
            me.preloader.destroy();
            me.preloader = null;
        }
    },

    showErrorPanel: function () {
        this.getCommunicationTaskEditForm().down('#communicationTaskEditFormErrors').show();
    },

    hideErrorPanel: function () {
        this.getCommunicationTaskEditForm().down('#communicationTaskEditFormErrors').hide();
    }
});
