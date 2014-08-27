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
        'ProtocolDialectsOfDeviceConfiguration'
    ],

    refs: [
        {ref: 'communicationTaskGridPanel', selector: '#communicationTaskGrid'},
        {ref: 'communicationTaskPreviewPanel', selector: '#communicationTaskPreview'},
        {ref: 'communicationTaskPreviewForm', selector: '#communicationTaskPreviewForm'},
        {ref: 'communicationTaskEditForm',selector: '#communicationTaskEditForm'},
        {ref: 'communicationTaskEditPanel',selector: '#communicationTaskEdit'},
        {ref: 'communicationTaskSetupPanel',selector: '#communicationTaskSetup'},
        {ref: 'scheduleField',selector: '#communicationTaskEditForm #scheduleField #scheduleFieldItem'}
    ],

    init: function () {
        var me = this;
        me.control({
            '#communicationTaskGrid': {
                select: me.loadCommunicationTaskDetail
            },
            '#communicationTaskSetup': {
                afterrender: me.loadCommunicationTasksStore
            },
            '#addEditButton[action=addCommunicationTaskAction]':{
                click: me.addCommunicationTask
            },
            '#addEditButton[action=editCommunicationTaskAction]':{
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

        me.listen({
            store: {
                '#CommunicationTaskConfigsOfDeviceConfiguration': {
                    load: me.checkCommunicationTasksCount
                }
            }
        });
    },

    setupMenuItems: function(record) {
        var me = this,
            suspended = record.get('suspended');

        if(!Ext.isEmpty(suspended)) {
            var textKey = ((suspended == true) ? 'communicationtasks.activate' : 'communicationtasks.deactivate'),
                text = ((suspended == true) ? 'Activate' : 'Deactivate'),
                menuItems = Ext.ComponentQuery.query('menu menuitem[action=activatecommunicationtask]');

            if(!Ext.isEmpty(menuItems)) {
                Ext.Array.each(menuItems, function(item) {
                    item.setText(Uni.I18n.translate(textKey, 'MDC', text));
                });
            }
        }
    },

    enableScheduleFieldItemChanged: function(item, newValue, oldValue, eOpts) {
        var me = this;

        if(newValue === false) {
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

    loadCommunicationTasksStore: function() {
        var me = this;

        me.getCommunicationTaskConfigsOfDeviceConfigurationStore().load({
            params: {
                sort: 'name'
            }
        });
    },

    loadCommunicationTaskDetail: function(rowmodel, record, index) {
        var me = this;
        me.previewConnectionTask(record);
        me.setupMenuItems(record);
    },

    previewConnectionTask: function (record) {
        var me = this,
            form = me.getCommunicationTaskPreviewForm();
        me.setPreLoader(form, Uni.I18n.translate('communicationtasks.loading', 'MDC', 'Loading...'));
        me.getCommunicationTaskPreviewPanel().setTitle(record.getData().comTask.name);
        form.loadRecord(record);
        me.clearPreLoader();
    },

    checkCommunicationTasksCount : function() {
        var me = this,
            grid = me.getCommunicationTaskGridPanel();
        if (!Ext.isEmpty(grid)) {
            var numberOfCommunicationTasksContainer = Ext.ComponentQuery.query('#communicationTaskGrid #communicationTasksCount')[0],
                gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                communicationTasksCount = me.getCommunicationTaskConfigsOfDeviceConfigurationStore().getCount(),
                word = Uni.I18n.translatePlural('communicationtasks.commtasks', communicationTasksCount, 'MDC', 'communication tasks'),
                widget = Ext.widget('container', {
                    html: communicationTasksCount + ' ' + word
                });

            numberOfCommunicationTasksContainer.removeAll(true);
            numberOfCommunicationTasksContainer.add(widget);

            if (communicationTasksCount > 0) {
                selectionModel.deselectAll(false);
                selectionModel.select(0);
            }
        }
    },

    showCommunicationTasks: function(deviceTypeId, deviceConfigurationId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigurationId;
        var widget = Ext.widget('communicationTaskSetup', {deviceTypeId: deviceTypeId, deviceConfigurationId: deviceConfigurationId});

        me.getCommunicationTaskConfigsOfDeviceConfigurationStore().getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    showAddCommunicationTaskView: function(deviceTypeId, deviceConfigurationId) {
        var me = this,
            comTasksStore = me.getComTasksStore(),
            securityPropertySetsStore = me.getSecuritySettingsOfDeviceConfigurationStore(),
            connectionMethodsStore = me.getConnectionMethodsOfDeviceConfigurationStore(),
            protocolDialectsStore = me.getProtocolDialectsOfDeviceConfigurationStore();
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        var widget = Ext.widget('communicationTaskEdit', {
            edit: false,
            returnLink: '#/administration/devicetypes/'+me.deviceTypeId+'/deviceconfigurations/'+me.deviceConfigurationId+'/comtaskenablements',
            comTasksStore: comTasksStore,
            securityPropertySetsStore: securityPropertySetsStore,
            connectionMethodsStore: connectionMethodsStore,
            protocolDialectsStore: protocolDialectsStore
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
                        comTasksStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId, available: true});
                        comTasksStore.load({
                            callback: function(){
                                securityPropertySetsStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
                                securityPropertySetsStore.load({
                                    callback: function(){
                                        connectionMethodsStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
                                        connectionMethodsStore.load({
                                            callback: function() {
                                                connectionMethodsStore.add(Ext.create('Mdc.model.ConnectionMethod', {
                                                    id: -1,
                                                    name: Uni.I18n.translate('communicationtasks.form.selectPartialConnectionTask', 'MDC', 'Use the default connection method')
                                                }));
                                                protocolDialectsStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
                                                protocolDialectsStore.load({
                                                    callback: function() {
//                                                        protocolDialectsStore.add(Ext.create('Mdc.model.ProtocolDialect', {
//                                                            id: -1,
//                                                            name: Uni.I18n.translate('communicationtasks.form.selectProtocolDialectConfigurationProperties', 'MDC', 'Use the default protocol dialect')
//                                                        }));
//                                                        var communicationTask = Ext.create('Mdc.model.CommunicationTaskConfig', {
//                                                            partialConnectionTask: {
//                                                                id: -1
//                                                            },
//                                                            protocolDialectConfigurationProperties: {
//                                                                id: -1
//                                                            }
//                                                        })
                                                        var title = Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task');
                                                        widget.down('#communicationTaskEditForm').setTitle(title);
//                                                        widget.setValues(communicationTask);
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

    editCommunicationTaskHistory: function() {
        var me = this,
            grid = me.getCommunicationTaskGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements/' + lastSelected.getData().id + '/edit';
    },

    showEditCommunicationTaskView: function(deviceTypeId, deviceConfigurationId, comTaskEnablementId) {
        var me = this,
            comTasksStore = me.getComTasksStore(),
            securityPropertySetsStore = me.getSecuritySettingsOfDeviceConfigurationStore(),
            connectionMethodsStore = me.getConnectionMethodsOfDeviceConfigurationStore(),
            protocolDialectsStore = me.getProtocolDialectsOfDeviceConfigurationStore(),
            model = Ext.ModelManager.getModel('Mdc.model.CommunicationTaskConfig');
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;

        model.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        model.load(comTaskEnablementId, {
            success: function (communicationTask) {
                me.getApplication().fireEvent('loadCommunicationTaskModel', communicationTask);
                var widget = Ext.widget('communicationTaskEdit', {
                    edit: true,
                    returnLink: '#/administration/devicetypes/'+me.deviceTypeId+'/deviceconfigurations/'+me.deviceConfigurationId+'/comtaskenablements',
                    comTasksStore: comTasksStore,
                    securityPropertySetsStore: securityPropertySetsStore,
                    connectionMethodsStore: connectionMethodsStore,
                    protocolDialectsStore: protocolDialectsStore
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
                                securityPropertySetsStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
                                securityPropertySetsStore.load({
                                    callback: function(){
                                        connectionMethodsStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
                                        connectionMethodsStore.load({
                                            callback: function() {
                                                connectionMethodsStore.add(Ext.create('Mdc.model.ConnectionMethod', {
                                                    id: -1,
                                                    name: Uni.I18n.translate('communicationtasks.form.selectPartialConnectionTask', 'MDC', 'Use the default connection method')
                                                }));
                                                widget.down('form').loadRecord(communicationTask);
                                                var comTaskName = '';
                                                if(!Ext.isEmpty(communicationTask.get('comTask'))) {
                                                    comTaskName = communicationTask.get('comTask').name;
                                                }
                                                var title = Uni.I18n.translate('communicationtasks.edit', 'MDC', 'Edit') + ' ' + comTaskName;
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
    },

    addCommunicationTask: function() {
        var me = this;
        me.setPreLoader(me.getCommunicationTaskEditPanel(), Uni.I18n.translate('communicationtasks.creating', 'MDC', 'Creating communication task'));
        me.updateCommunicationTask('add');
    },

    editCommunicationTask: function() {
        var me = this;
        me.setPreLoader(me.getCommunicationTaskEditPanel(), Uni.I18n.translate('communicationtasks.updating', 'MDC', 'Updating communication task'));
        me.updateCommunicationTask('edit');
    },

    removeCommunicationTask: function() {
        var me = this,
            grid = me.getCommunicationTaskGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('communicationtasks.deleteCommunicationTask.message', 'MDC', 'On this device configuration it wont be possible anymore to execute this communication task'),
            title: Uni.I18n.translate('communicationtasks.deleteCommunicationTask.title', 'MDC', 'Remove') + ' ' + lastSelected.get('comTask').name + '?',
            config: {
                communicationTaskToDelete: lastSelected,
                me: me
            },
            fn: me.removeCommunicationTaskRecord
        });
    },

    activateCommunicationTask: function() {
        var me = this,
            grid = me.getCommunicationTaskGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            suspended = lastSelected.get('suspended');

        if(!Ext.isEmpty(suspended)) {
            var action = ((suspended == true) ? 'activate' : 'deactivate');

            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements/' + lastSelected.getData().id + '/' + action,
                method: 'PUT',
                success: function () {
                    var messageKey = ((suspended == true) ? 'communicationtasks.activated' : 'communicationtasks.deactivated');
                    var messageText = ((suspended == true) ? 'Communication task successfully activated' : 'Communication task successfully deactivated');
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate(messageKey, 'MDC', messageText));
                    me.loadCommunicationTasksStore();
                },
                failure: function (response, request) {
                    if(response.status == 400) {
                        var errorText = Uni.I18n.translate('communicationtasks.error.unknown', 'MDC', 'Unknown error occurred');
                        if(!Ext.isEmpty(response.statusText)) {
                            errorText = response.statusText;
                        }
                        if(!Ext.isEmpty(response.responseText)) {
                            var json = Ext.decode(response.responseText, true);

                            if (json && json.error) {
                                errorText = json.error;
                            }
                        }

                        var titleKey = ((suspended == true) ? 'communicationtasks.activate.operation.failed' : 'communicationtasks.deactivate.operation.failed'),
                            titleValue = ((suspended == true) ? 'Activate operation failed' : 'Deactivate operation failed');


                        me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate(titleKey, 'MDC', titleValue), errorText);
                    }
                }
            });
        }
    },

    updateCommunicationTask: function(operation) {
        var me = this,
            form = me.getCommunicationTaskEditForm();
        if (form.isValid()) {
            me.hideErrorPanel();
            me[operation + 'CommunicationTaskRecord'](form.getValues(), {operation : operation});
        } else {
            me.clearPreLoader();
            me.showErrorPanel();
        }
    },

    editCommunicationTaskRecord: function(values, cfg) {
        var me = this,
            record = me.getCommunicationTaskEditForm().getRecord();
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('communicationtasks.updated', 'MDC', 'Communication task saved')
        }, cfg));
    },

    addCommunicationTaskRecord: function(values, cfg) {
        var me = this,
            record = Ext.create(Mdc.model.CommunicationTaskConfig);
        me.updateRecord(record, values, Ext.apply({
            successMessage: Uni.I18n.translate('communicationtasks.created', 'MDC', 'Communication task saved')
        }, cfg));
    },

    removeCommunicationTaskRecord: function(btn, text, cfg) {
        if (btn === 'confirm') {
            var me = cfg.config.me,
                communicationTaskToDelete = cfg.config.communicationTaskToDelete;
                communicationTaskToDelete.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
                communicationTaskToDelete.destroy({
                    callback: function (record, operation) {
                        if(operation.wasSuccessful()) {
                            location.href = '#/administration/devicetypes/'+me.deviceTypeId+'/deviceconfigurations/'+me.deviceConfigurationId+'/comtaskenablements';
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('communicationtasks.removed', 'MDC', 'Communication task successfully removed'));
                            me.loadCommunicationTasksStore();
                        } else {
                            if(operation.response.status == 400) {
                                var errorText = Uni.I18n.translate('communicationtasks.error.unknown', 'MDC', 'Unknown error occurred');
                                if(!Ext.isEmpty(operation.response.statusText)) {
                                    errorText = operation.response.statusText;
                                }
                                if(!Ext.isEmpty(operation.response.responseText)) {
                                    var json = Ext.decode(operation.response.responseText, true);
                                    if (json && json.error) {
                                        errorText = json.error;
                                    }
                                }

                                me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('communicationtasks.remove.operation.failed', 'MDC', 'Remove operation failed'), errorText);
                            }
                        }

                    }
                });
        }
    },

    updateRecord: function(record, values, cfg) {
        var me = this;
        if (record) {
            me.setRecordValues(record, values);
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            record.save({
                success: function (record) {
                    location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements';
                    me.getApplication().fireEvent('acknowledge', cfg.successMessage);
                },
                failure: function (record, operation) {
                    if(operation.response.status == 400) {
                        var errorText = Uni.I18n.translate('communicationtasks.error.unknown', 'MDC', 'Unknown error occurred');
                        if(!Ext.isEmpty(operation.response.statusText)) {
                            errorText = operation.response.statusText;
                        }
                        if(!Ext.isEmpty(operation.response.responseText)) {
                            var json = Ext.decode(operation.response.responseText, true);
                            if (json && json.errors) {
                                me.showErrorPanel();
                                me.getCommunicationTaskEditForm().getForm().markInvalid(json.errors);
                                return;
                            }
                            if (json && json.error) {
                                errorText = json.error;
                            }
                        }

                        var titleKey = ((cfg.operation == 'add') ? 'communicationtasks.add.operation.failed' : 'communicationtasks.edit.operation.failed'),
                            titleValue = ((cfg.operation == 'add') ? 'Add operation failed' : 'Update operation failed');

                        me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate(titleKey, 'MDC', titleValue), errorText);
                    }
                },
                callback: function() {
                    me.clearPreLoader();
                }
            });
        }
    },

    setRecordValues: function(record, values) {
        record.set("comTask", {id: values.comTaskId});
        record.set("securityPropertySet", {id: values.securityPropertySetId});
        if(!Ext.isEmpty(values.partialConnectionTaskId)) {
            record.set("partialConnectionTask", {id: values.partialConnectionTaskId});
        }
        if(!Ext.isEmpty(values.protocolDialectConfigurationPropertiesId)) {
            record.set("protocolDialectConfigurationProperties", {id: values.protocolDialectConfigurationPropertiesId});
        }
        record.set("priority", values.priority);
        record.set("ignoreNextExecutionSpecsForInbound", values.ignoreNextExecutionSpecsForInbound);
    },

    setPreLoader: function(target, message) {
        var me = this;
        me.preloader = Ext.create('Ext.LoadMask', {
            msg: message,
            target: target
        });
        me.preloader.show();
    },

    clearPreLoader: function() {
        var me = this;
        if(!Ext.isEmpty(me.preloader)) {
            me.preloader.destroy();
            me.preloader = null;
        }
    },

    showErrorPanel: function() {
        var me = this,
            formErrorsPlaceHolder = me.getCommunicationTaskEditForm().down('#communicationTaskEditFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
        formErrorsPlaceHolder.add({
            html: Uni.I18n.translate('communicationtasks.form.errors', 'MDC', 'There are errors on this page that require your attention')
        });
        formErrorsPlaceHolder.show();
    },

    hideErrorPanel: function() {
        var me = this,
            formErrorsPlaceHolder = me.getCommunicationTaskEditForm().down('#communicationTaskEditFormErrors');

        formErrorsPlaceHolder.hide();
        formErrorsPlaceHolder.removeAll();
    }
});
