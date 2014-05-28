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
        'setup.communicationtask.CommunicationTaskEmptyList',
        'setup.communicationtask.CommunicationTaskDockedItems',
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
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'communicationTaskGridPanel', selector: '#communicationTaskGrid'},
        {ref: 'communicationTaskPreviewPanel', selector: '#communicationTaskPreview'},
        {ref: 'communicationTaskPreviewForm', selector: '#communicationTaskPreviewForm'},
        {ref: 'communicationTaskEditForm',selector: '#communicationTaskEditForm'},
        {ref: 'communicationTaskEditPanel',selector: '#communicationTaskEdit'},
        {ref: 'communicationTaskSetupPanel',selector: '#communicationTaskSetup'}
    ],

    init: function () {
        var me = this;
        me.control({
            '#communicationTaskGrid': {
                itemclick: me.communicationTaskGridItemClick
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

        me.addEvents('shownotification');

        me.on('shownotification', me.showNotification);
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

    showNotification: function(title, text, notificationType) {
        Ext.create('widget.uxNotification', {
            title: title,
            html: text,
            ui: notificationType
        }).show();
    },

    loadCommunicationTasksStore: function() {
        var me = this;

        me.getCommunicationTaskConfigsOfDeviceConfigurationStore().load({
            params: {
                sort: 'name'
            }
        });
    },

    communicationTaskGridItemClick: function(grid, record) {
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
            var numberOfCommunicationTasksContainer = Ext.ComponentQuery.query('communicationTaskSetup communicationTaskDockedItems container[name=CommunicationTasksCount]')[0],
                emptyMessage = Ext.ComponentQuery.query('communicationTaskSetup')[0].down('#CommunicationTaskEmptyList'),
                gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                communicationTasksCount = me.getCommunicationTaskConfigsOfDeviceConfigurationStore().getCount(),
                word = Uni.I18n.translatePlural('communicationtasks.commtasks', communicationTasksCount, 'MDC', 'communication tasks'),
                widget = Ext.widget('container', {
                    html: communicationTasksCount + ' ' + word
                });

            numberOfCommunicationTasksContainer.removeAll(true);
            numberOfCommunicationTasksContainer.add(widget);

            if (communicationTasksCount < 1) {
                grid.hide();
                emptyMessage.removeAll(true);
                emptyMessage.add(
                    {
                        xtype: 'communicationTaskEmptyList',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigurationId
                    }
                );
                me.getCommunicationTaskPreviewPanel().hide();
            } else {
                selectionModel.deselectAll(false);
                selectionModel.select(0);
                grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
            }
        }
    },

    showCommunicationTasks: function(deviceTypeId, deviceConfigurationId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        this.deviceConfigurationId = deviceConfigurationId;
        var widget = Ext.widget('communicationTaskSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId});

        me.getCommunicationTaskConfigsOfDeviceConfigurationStore().getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        var deviceTypeName = deviceType.get('name');
                        var deviceConfigName = deviceConfig.get('name');
                        me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                        me.overviewBreadCrumbs(deviceTypeId, deviceConfigurationId, deviceTypeName, deviceConfigName, null);
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
        me.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
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
                                                        protocolDialectsStore.add(Ext.create('Mdc.model.ProtocolDialect', {
                                                            id: -1,
                                                            name: Uni.I18n.translate('communicationtasks.form.selectProtocolDialectConfigurationProperties', 'MDC', 'Use the default protocol dialect')
                                                        }));
                                                        var communicationTask = Ext.create('Mdc.model.CommunicationTaskConfig', {
                                                            partialConnectionTask: {
                                                                id: -1
                                                            },
                                                            protocolDialectConfigurationProperties: {
                                                                id: -1
                                                            }
                                                        })
                                                        var deviceTypeName = deviceType.get('name');
                                                        var deviceConfigName = deviceConfig.get('name');
                                                        var title = Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task');
                                                        widget.down('#communicationTaskEditAddTitle').update('<h1>' + title  + '</h1>');
                                                        widget.setValues(communicationTask);
                                                        me.overviewBreadCrumbs(deviceTypeId, deviceConfigurationId, deviceTypeName, deviceConfigName, Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task'));
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
                var widget = Ext.widget('communicationTaskEdit', {
                    edit: true,
                    returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens(),
                    comTasksStore: comTasksStore,
                    securityPropertySetsStore: securityPropertySetsStore,
                    connectionMethodsStore: connectionMethodsStore,
                    protocolDialectsStore: protocolDialectsStore
                });
                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                widget.setLoading(true);
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                        model.getProxy().setExtraParam('deviceType', deviceTypeId);
                        model.load(deviceConfigurationId, {
                            success: function (deviceConfig) {
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
                                                var deviceTypeName = deviceType.get('name');
                                                var deviceConfigName = deviceConfig.get('name');
                                                widget.down('form').loadRecord(communicationTask);
                                                var title = Uni.I18n.translate('communicationtasks.edit', 'MDC', 'Edit communication task');
                                                widget.down('#communicationTaskEditAddTitle').update('<h1>' + title  + '</h1>');
                                                widget.setValues(communicationTask);
                                                me.overviewBreadCrumbs(deviceTypeId, deviceConfigurationId, deviceTypeName, deviceConfigName, Uni.I18n.translate('communicationtasks.edit', 'MDC', 'Edit communication task'));
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

        var confirmMessage = Ext.widget('messagebox', {
            buttons: [
                {
                    xtype: 'button',
                    text: 'Remove',
                    action: 'remove',
                    name: 'remove',
                    ui: 'delete',
                    handler: function () {
                        confirmMessage.close();
                        me.removeCommunicationTaskRecord({
                            communicationTaskToDelete: lastSelected,
                            me: me
                        });
                    }
                },
                {
                    xtype: 'button',
                    text: 'Cancel',
                    action: 'cancel',
                    name: 'cancel',
                    ui: 'link',
                    handler: function () {
                        confirmMessage.close();
                    }
                }
            ]
        }).show({
                msg: Uni.I18n.translate('communicationtasks.deleteCommunicationTask.message', 'MDC', 'On this device configuration it wont be possible anymore to execute this communication task'),
                title: Uni.I18n.translate('communicationtasks.deleteCommunicationTask.title', 'MDC', 'Remove') + ' ' + lastSelected.get('comTask').name + '?',
                icon: Ext.MessageBox.WARNING
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
                    me.fireEvent('shownotification', null, Uni.I18n.translate(messageKey, 'MDC', messageText), 'notification-success');
                    me.loadCommunicationTasksStore();
                },
                failure: function (result, request) {
                    me.fireEvent('shownotification', null, Uni.I18n.translate('communicationtasks.operation.failed', 'MDC', 'Operation failed'), 'notification-error');
                }
            });
        }
    },

    updateCommunicationTask: function(mode) {
        var me = this,
            form = me.getCommunicationTaskEditForm(),
            formErrorsPlaceHolder = Ext.ComponentQuery.query('#communicationTaskEditFormErrors')[0];
        if (form.isValid()) {
            formErrorsPlaceHolder.hide();
            me[mode + 'CommunicationTaskRecord'](form.getValues());
        } else {
            me.clearPreLoader();
            formErrorsPlaceHolder.hide();
            formErrorsPlaceHolder.removeAll();
            formErrorsPlaceHolder.add({
                html: Uni.I18n.translate('communicationtasks.form.errors', 'MDC', 'There are errors on this page that require your attention')
            });
            formErrorsPlaceHolder.show();
        }
    },

    editCommunicationTaskRecord: function(values) {
        var me = this,
            record = me.getCommunicationTaskEditForm().getRecord();
        me.updateRecord(record, values, {
            successMessage: Uni.I18n.translate('communicationtasks.updated', 'MDC', 'Communication task successfully updated')
        });
    },

    addCommunicationTaskRecord: function(values) {
        var me = this,
            record = Ext.create(Mdc.model.CommunicationTaskConfig);
        me.updateRecord(record, values, {
            successMessage: Uni.I18n.translate('communicationtasks.created', 'MDC', 'Communication task successfully created')
        });
    },

    removeCommunicationTaskRecord: function(cfg) {
        var me = cfg.me,
            communicationTaskToDelete = cfg.communicationTaskToDelete;

            me.setPreLoader(me.getCommunicationTaskSetupPanel(), Uni.I18n.translate('communicationtasks.removing', 'MDC', 'Removing communication task'));
            communicationTaskToDelete.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            communicationTaskToDelete.destroy({
                callback: function (record, operation) {
                    me.clearPreLoader();
                    if(operation.wasSuccessful()) {
                        location.href = '#/administration/devicetypes/'+me.deviceTypeId+'/deviceconfigurations/'+me.deviceConfigurationId+'/comtaskenablements';
                        me.fireEvent('shownotification', null, Uni.I18n.translate('communicationtasks.removed', 'MDC', 'Communication task successfully removed'), 'notification-success');
                        me.loadCommunicationTasksStore();
                    } else {
                        me.fireEvent('shownotification', null, Uni.I18n.translate('communicationtasks.operation.failed', 'MDC', 'Operation failed'), 'notification-error');
                    }

                }
            });
    },

    updateRecord: function(record, values, cfg) {
        var me = this;
        if (record) {
            me.setRecordValues(record, values);
            record.getProxy().extraParams = ({deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigurationId});
            record.save({
                success: function (record) {
                    location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements';
                    me.fireEvent('shownotification', null, cfg.successMessage, 'notification-success');
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getCommunicationTaskEditForm().getForm().markInvalid(json.errors);
                    }
                    me.fireEvent('shownotification', null, Uni.I18n.translate('communicationtasks.operation.failed', 'MDC', 'Operation failed'), 'notification-error');
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
        if(values.nextExecutionSpecsEnable == true) {
            record.set("nextExecutionSpecs", values.nextExecutionSpecs);
        }
        record.set("ignoreNextExecutionSpecsForInbound", values.ignoreNextExecutionSpecsForInbound);
    },

    overviewBreadCrumbs: function (deviceTypeId, deviceConfigId, deviceTypeName, deviceConfigName, action) {
        var me = this;

        var breadcrumbCommunicationTasks = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerConfig.communicationTasks', 'MDC', 'Communication tasks'),
            href: 'comtaskenablements'

        });

        var breadcrumbDeviceConfig = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceConfigName,
            href: deviceConfigId
        });

        var breadcrumbDeviceConfigs = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerConfig.deviceConfigs', 'MDC', 'Device configurations'),
            href: 'deviceconfigurations'
        });

        var breadcrumbDevicetype = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });

        var breadcrumbDeviceTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerConfig.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#/administration'
        });

        if (Ext.isEmpty(action)) {
            breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbDeviceConfigs).setChild(breadcrumbDeviceConfig).setChild(breadcrumbCommunicationTasks);
        } else {
            var breadaction = Ext.create('Uni.model.BreadcrumbItem', {
                text: action,
                href: ''
            });
            breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbDeviceConfigs).setChild(breadcrumbDeviceConfig).setChild(breadcrumbCommunicationTasks).setChild(breadaction);
        }

       me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
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
    }
});
