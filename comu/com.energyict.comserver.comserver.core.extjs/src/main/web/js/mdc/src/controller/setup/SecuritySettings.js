Ext.define('Mdc.controller.setup.SecuritySettings', {
    extend: 'Ext.app.Controller',
    views: [
        'setup.securitysettings.SecuritySettingSetup',
        'setup.securitysettings.SecuritySettingGrid',
        'setup.securitysettings.SecuritySettingPreview',
        'setup.securitysettings.SecuritySettingFiltering',
        'setup.securitysettings.SecuritySettingSorting',
        'setup.securitysettings.SecuritySettingSideFilter',
        'setup.securitysettings.SecuritySettingForm',
        'setup.executionlevels.AddExecutionLevels'
    ],

    stores: [
        'SecuritySettingsOfDeviceConfiguration',
        'AuthenticationLevels',
        'EncryptionLevels',
        'AvailableExecLevelsForSecSettingsOfDevConfig'
    ],

    refs: [
        {ref: 'formPanel', selector: 'securitySettingForm'},
        {ref: 'securityGridPanel', selector: 'securitySettingGrid'},
        {ref: 'executionLevelGridAddLink', selector: '#execution-level-grid-add-link'},
        {ref: 'addExecutionLevelPanel', selector: '#addExecutionLevelPanel'},
        {ref: 'executionLevelAddGrid', selector: '#execution-level-add-grid'},
        {ref: 'addExecutionLevels', selector: 'add-execution-levels'},
        {ref: 'executionLevelGridPanel', selector: '#execution-level-grid'},
        {ref: 'executionLevelAddLink', selector: '#execution-level-grid #createExecutionLevel'},
        {ref: 'executionLevelsForSecuritySettingPreview', selector: '#execution-level-grid-title'}
    ],
    config: {
        deviceTypeName: null,
        deviceConfigId: null
    },

    secId: -1,

    init: function () {
        me = this;
        // intercepted Functions for initializing the breadcrumb
        Ext.Function.createInterceptor('showSecuritySettingsCreateView', me.prepareBreadcrumb);
        Ext.Function.createInterceptor('showSecuritySettingsEditView', me.prepareBreadcrumb);
        this.control({
            'securitySettingSetup securitySettingGrid': {
                select: this.loadGridItemDetail
            },
            'securitySettingSetup': {
                afterrender: this.loadStore
            },
            'securitySettingForm button[name=securityaction]': {
                click: this.onSubmit
            },
            'menu menuitem[action=editsecuritysetting]': {
                click: this.editRecord
            },
            'menu menuitem[action=deletesecuritysetting]': {
                click: this.removeSecuritySetting
            },
            'add-execution-levels grid': {
                selectionchange: this.hideExecutionLevelsErrorPanel
            },
            'add-execution-levels button[action=add]': {
                click: this.addExecutionLevels
            },
            '#execution-level-grid actioncolumn': {
                deleteExecutionLevel: this.removeExecutionLevel
            },
            '#execution-level-grid button[action=createExecutionLevel]': {
                click: this.createAddExecutionLevelHistory
            }

        });

        this.listen({
            store: {
                '#Mdc.store.SecuritySettingsOfDeviceConfiguration': {
                    load: this.selectFirstIfPossible
                }
            }
        });

        this.store = this.getStore('Mdc.store.SecuritySettingsOfDeviceConfiguration');
    },

    createAddExecutionLevelHistory: function () {
        var grid = this.getSecurityGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings/' + lastSelected.getData().id + '/privileges/add';
    },

    editRecord: function () {
        var grid = this.getSecurityGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        window.location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings/' + lastSelected.getData().id + '/edit';
    },

    prepareBreadcrumb: function(deviceTypeId, deviceConfigurationId, securitySettingId){
        var me = this;
        console.log("Preparing breadcrumb");
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.setDeviceTypeName(deviceType.get('name'));
                        me.setDeviceConfigName(deviceConfig.get('name'));
                        return true;
                    }
                });
            },
        });
        return false;
    },

    removeSecuritySetting: function () {
        var me = this,
            grid = me.getSecurityGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: "This security setting configuration will no longer be available",
            title: "Remove " + ' \'' + lastSelected.getData().name + '\'?',
            config: {
                securitySettingToDelete: lastSelected,
                me: me
            },
            fn: me.removeSecuritySettingRecord
        });
    },

    removeSecuritySettingRecord: function (btn, text, cfg) {
        if (btn === 'confirm') {
            var me = cfg.config.me,
                securitySettingToDelete = cfg.config.securitySettingToDelete;

            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securityproperties/' + securitySettingToDelete.getData().id,
                method: 'DELETE',
                waitMsg: 'Removing...',
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicesecuritysetting.saveSuccess.msg.remove', 'MDC', 'Security setting removed'));
                    me.store.load();
                },
                failure: function (response, request) {
                    var errorText = "Unknown error occurred";

                    if (response.status == 400) {
                        var result = Ext.JSON.decode(response.responseText, true);
                        if (result && result.message) {
                            errorText = result.message;
                        }

                        me.getApplication().getController('Uni.controller.Error').showError("Error during removing of security setting", errorText);
                    }
                }
            });
        }
    },

    selectFirstIfPossible: function (store) {
        var grid = this.getSecurityGridPanel();
        if (!Ext.isEmpty(grid)) {
            var gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                securityCount = store.getCount();
            if (securityCount > 1) {
                var index = store.find("id", this.secId);
                if (index == -1) {
                    selectionModel.select(0);
                } else {
                    selectionModel.select(index);
                    this.secId = -1;
                }
            } else if (securityCount == 1) {
                selectionModel.select(0);
            }
        }
    },


    loadStore: function () {
        this.store.load({
            params: {
                sort: 'name'
            }
        });
    },

    loadGridItemDetail: function (rowmodel, record, index) {
        var detailPanel = Ext.ComponentQuery.query('securitySettingSetup securitySettingPreview')[0],
            form = detailPanel.down('form'),
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading','MDC','Loading...'),
                target: form
            });

        preloader.show();
        detailPanel.setTitle(Ext.String.htmlEncode(record.getData().name));
        form.loadRecord(record);

        var executionLevelsgrid = Ext.ComponentQuery.query('securitySettingSetup execution-level-grid')[0];
        var executionLevelscontainer = Ext.ComponentQuery.query('securitySettingSetup #execution-levels-grid-preview-container')[0];
        var executionLevelsTitle = Ext.ComponentQuery.query('securitySettingSetup #execution-level-grid-title')[0];
        this.getExecutionLevelGridAddLink() &&
            this.getExecutionLevelGridAddLink().getEl().set({href: executionLevelscontainer.emptyComponent.stepItems[0].href + record.get('id') + '/privileges/add'});
        this.getExecutionLevelAddLink() &&
            this.getExecutionLevelAddLink().getEl().set({href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings/' + record.get('id') + '/privileges/add'});

        var preview = Ext.ComponentQuery.query('securitySettingSetup #execution-levels-grid-preview-container')[0];
        preview.bindStore(record.executionLevels());
        executionLevelsgrid.store = record.executionLevels();
        var view = executionLevelsgrid.getView();
        view.bindStore(record.executionLevels());

        preview.onLoad();

        executionLevelsTitle.show();
        executionLevelscontainer.show();

        this.getExecutionLevelsForSecuritySettingPreview().setTitle(
            Uni.I18n.translate('securitySetting.executionLevel.gridTitle', 'MDC', "Privileges of '{0}'", [record.getData().name])
        );

        executionLevelsgrid.down('pagingtoolbartop').store = record.executionLevels();
        executionLevelsgrid.down('pagingtoolbartop').store.totalCount = record.executionLevels().getCount();
        executionLevelsgrid.down('pagingtoolbartop').displayMsg =
            Uni.I18n.translatePlural('executionLevel.pagingtoolbartop.displayMsg', record.executionLevels().getCount(), 'MDC',
                'No privileges', '{0} privilege', '{0} privileges');
        executionLevelsgrid.down('pagingtoolbartop').updateInfo();

        preloader.destroy();
    },

    showSecuritySettings: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            widget = Ext.widget('securitySettingSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId});
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfig.get('name'));
                        me.deviceTypeName = deviceType.get('name');
                        me.deviceConfigName = deviceConfig.get('name');
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    showSecuritySettingsCreateView: function (deviceTypeId, deviceConfigurationId) {
        var me = this;
        var form = Ext.widget('securitySettingForm', {
            deviceTypeId: deviceTypeId,
            deviceConfigurationId: deviceConfigurationId,
            securityHeader: Uni.I18n.translate('securitySetting.addSecuritySetting', 'MDC', 'Add security setting'),
            actionButtonName: Uni.I18n.translate('general.add', 'MDC', 'Add'),
            securityAction: 'add'
        });
        var record  =  me.createSecuritySettingModel(deviceTypeId, deviceConfigurationId).create();
        form.down('form#myForm').loadRecord(record);
        me.getApplication().fireEvent('changecontentevent', form);
    },

    showSecuritySettingsEditView: function (deviceTypeId, deviceConfigurationId, securitySettingId) {
        var me = this;
        me.createSecuritySettingModel(deviceTypeId, deviceConfigurationId).load(securitySettingId, {
            success: function (securitySetting) {
                var form = Ext.widget('securitySettingForm', {
                    deviceTypeId: deviceTypeId,
                    deviceConfigurationId: deviceConfigurationId,
                    securityHeader: Ext.String.format(Uni.I18n.translate('securitySetting.editX', 'MDC', "Edit security setting '{0}'"), securitySetting.get('name')),
                    actionButtonName: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                    securityAction: 'save'
                });
                form.down('form#myForm').loadRecord(securitySetting);
                me.getApplication().fireEvent('changecontentevent', form);
            }
        });
    },

    createSecuritySettingModel: function(deviceTypeId, deviceConfigurationId){
        var securitySettingModel = Ext.ModelManager.getModel('Mdc.model.SecuritySetting');
        securitySettingModel.getProxy().url = '/api/dtc/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securityproperties/';
        return securitySettingModel;
    },

    onSubmit: function (btn) {
        var me = this,
            formPanel = me.getFormPanel(),
            form = formPanel.down('form#myForm').getForm(),
            formErrorsPanel = formPanel.down('panel[name=errors]');

        if (form.isValid()) {
            formErrorsPanel.hide();
            var preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.saving','MDC','Saving...'),
                target: formPanel
            });
            preloader.show();
            form.updateRecord();
            form.getRecord().save({
                 success: function (response) {
                     me.handleSuccessRequest(response, Uni.I18n.translate('devicesecuritysetting.saveSuccess.msg.edit', 'MDC', 'Security setting saved'));
                 },
                 failure: function (response, operation) {
                     if (operation) {
                         if (operation.error.status == 400) {
                             var result = Ext.JSON.decode(operation.response.responseText, true);
                             if (result && result.errors) {
                                 form.markInvalid(result.errors)
                             }
                             me.showErrorPanel();
                         }
                     }
                 },
                 callback: function () {
                      preloader.destroy();
                 }
            });
        } else {
            me.showErrorPanel();
        }
    },

    showErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getFormPanel().down('panel[name=errors]');

        formErrorsPanel.hide();
        Ext.suspendLayouts();
        formErrorsPanel.removeAll();
        formErrorsPanel.add({
            html: Uni.I18n.translate('general.formErrors', 'MDC', 'There are errors on this page that require your attention.')
        });
        Ext.resumeLayouts();
        formErrorsPanel.show();
    },

    handleSuccessRequest: function (response, headerText) {
        var data = Ext.JSON.decode(response.responseText, true);
        if (data && data.id) {
            this.secId = parseInt(data.id);
        }
        window.location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings';
        this.getApplication().fireEvent('acknowledge', headerText);
    },

    showAddExecutionLevelsView: function (deviceTypeId, deviceConfigId, securitySettingId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            store = Ext.data.StoreManager.lookup('AvailableExecLevelsForSecSettingsOfDevConfig');
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('deviceConfig', deviceConfigId);
        store.getProxy().setExtraParam('securityProperty', securitySettingId);
        store.getProxy().setExtraParam('available', true);
        store.load(
            {
                callback: function () {
                    var self = this;
                    Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                            model.getProxy().setExtraParam('deviceType', deviceTypeId);
                            model.load(deviceConfigId, {
                                success: function (deviceConfig) {
                                    me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                                    var modelSecuritySet = Ext.ModelManager.getModel('Mdc.model.SecuritySetting');
                                    modelSecuritySet.getProxy().setExtraParam('deviceType', deviceTypeId);
                                    modelSecuritySet.getProxy().setExtraParam('deviceConfig', deviceConfigId);
                                    modelSecuritySet.load(securitySettingId, {
                                        success: function (securitySetting) {
                                            me.getApplication().fireEvent('loadSecuritySetting', securitySetting);
                                            var widget = Ext.widget('add-execution-levels', {deviceTypeId: deviceTypeId, deviceConfigurationId: deviceConfigId, securitySettingId: securitySettingId})
                                            me.getApplication().fireEvent('changecontentevent', widget);
                                            me.getAddExecutionLevelPanel().setTitle(Uni.I18n.translate('executionlevels.addExecutionLevels', 'MDC', 'Add privileges'));
                                            store.load(function(){
                                                me.getExecutionLevelAddGrid().getSelectionModel().deselectAll();
                                            });
                                            //  var numberOfExecutionLevelsLabel = Ext.ComponentQuery.query('add-execution-levels toolbar label[name=ExecutionLevelCount]')[0],
                                            //var grid = Ext.ComponentQuery.query('add-execution-levels grid')[0];
                                            //numberOfExecutionLevelsLabel.setText(Uni.I18n.translate('executionlevels.noExecutionLevelsSelected', 'MDC','No execution levels selected'));
                                            //  if (self.getCount() < 1) {
                                            //     grid.hide();
                                            //     grid.next().show();
                                            // }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        );
    },

    addExecutionLevels: function (btn) {
        var self = this,
            addView = this.getAddExecutionLevels(),
            grid = this.getExecutionLevelAddGrid();
        var url = '/api/dtc/devicetypes/' + addView.deviceTypeId + '/deviceconfigurations/' + addView.deviceConfigurationId + '/securityproperties/' + addView.securitySettingId + '/executionlevels/',
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading','MDC','Loading...'),
                target: addView
            }),
            records = grid.getSelectionModel().getSelection(),
            ids = [];
        if (records.length === 0) {
            self.showExecutionLevelsErrorPanel();
        } else {
            Ext.Array.each(records, function (item) {
                ids.push(item.internalId);
            });
            var jsonIds = Ext.encode(ids);
            //preloader.show();
            var router = this.getController('Uni.controller.history.Router');
            Ext.Ajax.request({
                url: url,
                method: 'POST',
                jsonData: jsonIds,
                success: function () {
                    router.getRoute('administration/devicetypes/view/deviceconfigurations/view/securitysettings').forward();
                    self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('executionlevels.acknowlegment.added', 'MDC', 'Privileges added'));
                },
                failure: function (response) {
                    if (response.status == 400) {
                        var result = Ext.decode(response.responseText, true),
                            errorTitle = 'Failed to add',
                            errorText = 'Privileges could not be added. There was a problem accessing the database';

                        if (result !== null) {
                            errorTitle = result.error;
                            errorText = result.message;
                        }

                        self.getApplication().getController('Uni.controller.Error').showError(errorTitle, errorText);
                    }
                },
                callback: function () {
                    preloader.destroy();
                }
            });
        }
    },

    removeExecutionLevel: function () {
        var me = this,
            grid = me.getExecutionLevelGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        var securitySettingsGrid = me.getSecurityGridPanel(),
            securitySetting = securitySettingsGrid.getView().getSelectionModel().getLastSelected().getData().id;
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('executionlevel.removeExecutionLevel', 'MDC', 'The privilege will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' \'' + lastSelected.getData().name + '\'?',
            config: {
                executionLevelToDelete: lastSelected,
                securitySetting: securitySetting,
                me: me
            },
            fn: me.removeExecutionLevelRecord
        });
    },

    removeExecutionLevelRecord: function (btn, text, cfg) {
        if (btn === 'confirm') {
            var me = cfg.config.me,
                executionLevelToDelete = cfg.config.executionLevelToDelete,
                securitySetting = cfg.config.securitySetting;
            var securitySettingSelected = me.getSecurityGridPanel().getSelectionModel().getLastSelected();
            var selectedIndex = me.store.indexOf(securitySettingSelected);
            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securityproperties/' + securitySetting + '/executionlevels/' + executionLevelToDelete.getData().id,
                method: 'DELETE',
                waitMsg: Uni.I18n.translate('general.removing','MDC','Removing...'),
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('executionlevel.acknowlegment.removed', 'MDC', 'Privilege removed'));
                    me.store.load(function(){
                        me.getSecurityGridPanel().getSelectionModel().select(selectedIndex);
                    });
                },
                failure: function (response, request) {
                    var errorText = "Unknown error occurred";

                    if (response.status == 400) {
                        var result = Ext.JSON.decode(response.responseText, true);
                        if (result && result.message) {
                            errorText = result.message;
                        }

                        me.getApplication().getController('Uni.controller.Error').showError("Error during removing of privilege", errorText);
                    }
                }
            });
        }
    },

    showExecutionLevelsErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddExecutionLevelPanel().down('#add-execution-level-errors'),
            errorPanel = me.getAddExecutionLevelPanel().down('#add-execution-level-selection-error');

        formErrorsPanel.show();
        errorPanel.show();
    },

    hideExecutionLevelsErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddExecutionLevelPanel().down('#add-execution-level-errors'),
            errorPanel = me.getAddExecutionLevelPanel().down('#add-execution-level-selection-error');

        formErrorsPanel.hide();
        errorPanel.hide();

    }


});