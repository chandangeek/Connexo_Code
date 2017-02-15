/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        {ref: 'executionLevelsForSecuritySettingPreview', selector: '#execution-level-grid-title'},
        {ref: 'executionLevelsPreviewContainer', selector: '#execution-levels-grid-preview-container'}
    ],
    config: {
        deviceTypeName: null,
        deviceConfigName: null
    },

    secId: -1,

    init: function () {
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

    removeSecuritySetting: function () {
        var me = this,
            grid = me.getSecurityGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('securitySetting.remove.info', 'MDC', 'This security setting will no longer be available'),
            title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', lastSelected.getData().name),
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
                waitMsg: Uni.I18n.translate('general.removing', 'MDC', 'Removing...'),
                jsonData: securitySettingToDelete.getRecordData(),
                waitMsg: Uni.I18n.translate('general.removing', 'MDC', 'Removing...'),
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicesecuritysetting.saveSuccess.msg.remove', 'MDC', 'Security setting removed'));
                    me.store.load({
                        callback: function(records) {
                            if(records.length === 0){
                                me.getExecutionLevelsForSecuritySettingPreview().setVisible(false);
                                me.getExecutionLevelsPreviewContainer().setVisible(false);
                            }
                        }
                    });
                },
                failure: function (response, request) {
                    var errorInfo = Uni.I18n.translate('devicesecuritysetting.removeErrorMsg', 'MDC', 'Error during removal of security setting'),
                        errorText = Uni.I18n.translate('general.error.unknown', 'MDC', "Unknown error occurred");

                    if (response.status == 400) {
                        var result = Ext.JSON.decode(response.responseText, true);
                        if (result && result.message) {
                            errorText = result.message;
                        }
                        me.getApplication().getController('Uni.controller.Error').showError(errorInfo, errorText);
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
        preloader.destroy();
    },

    showSecuritySettings: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            widget = Ext.widget('securitySettingSetup', {deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId}),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        if (mainView) mainView.setLoading(Uni.I18n.translate('general.loading', 'MDC', 'Loading...'));
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
                        if (mainView) mainView.setLoading(false);
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

                    }
                });
            },
        });
    },

    showSecuritySettingsEditView: function (deviceTypeId, deviceConfigurationId, securitySettingId) {
        var me = this;
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
                    }
                });
            },
        });
    },

    createSecuritySettingModel: function(deviceTypeId, deviceConfigurationId){
        var securitySettingModel = Ext.ModelManager.getModel('Mdc.model.SecuritySetting');
        securitySettingModel.getProxy().url = '/api/dtc/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/securityproperties/';
        return securitySettingModel;
    },

    onSubmit: function (btn) {
        var me = this,
            formPanel = me.getFormPanel(),
            form = formPanel.down('form#myForm').getForm();

        if (form.isValid()) {
            me.hideErrorPanel();
            var preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.saving','MDC','Saving...'),
                target: formPanel
            });
            preloader.show();
            form.updateRecord();
            form.getRecord().save({
                backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/deviceconfigurations/view/securitysettings').buildUrl(),
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
        this.getFormPanel().down('#mdc-security-settings-form-errors').show();
    },

    hideErrorPanel: function () {
        this.getFormPanel().down('#mdc-security-settings-form-errors').hide();
    },

    handleSuccessRequest: function (response, headerText) {
        var me = this,
            data = Ext.JSON.decode(response.responseText, true);

        if (data && data.id) {
            this.secId = parseInt(data.id);
        }
        me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/deviceconfigurations/view/securitysettings').forward();
        this.getApplication().fireEvent('acknowledge', headerText);
    }


});