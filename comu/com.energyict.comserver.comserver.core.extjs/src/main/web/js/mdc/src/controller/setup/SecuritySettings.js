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
        'setup.securitysettings.SecuritySettingForm'
    ],

    stores: [
        'SecuritySettingsOfDeviceConfiguration',
        'SecuritySuites',
        'AuthenticationLevels',
        'EncryptionLevels',
        'RequestSecurityLevels',
        'ResponseSecurityLevels'
    ],

    refs: [
        {ref: 'formPanel', selector: 'securitySettingForm'},
        {ref: 'securityGridPanel', selector: 'securitySettingGrid'},
        {ref: 'securitySuiteCombobox', selector: '#securitySuiteCombobox'},
        {ref: 'authCombobox', selector: '#authCombobox'},
        {ref: 'encrCombobox', selector: '#encrCombobox'},
        {ref: 'clientField', selector: '#client-field'},
        {ref: 'requestSecurityCombobox', selector: '#requestSecurityCombobox'},
        {ref: 'responseSecurityCombobox', selector: '#responseSecurityCombobox'}
    ],
    config: {
        deviceTypeName: null,
        deviceConfigName: null
    },

    secId: -1,
    currentDeviceTypeId: undefined,
    currentDeviceConfigurationId: undefined,
    deviceProtocolSupportSecuritySuites: undefined,

    init: function () {
        var me = this;
        me.control({
            'securitySettingSetup securitySettingGrid': {
                select: me.loadGridItemDetail
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
            '#securitySuiteCombobox': {
                select: this.updateUsedSecurityLevelPossibilitiesBasedOnSecuritySuite
            }

        });

        me.store = this.getStore('Mdc.store.SecuritySettingsOfDeviceConfiguration');
        me.getSecuritySuitesStore().on('load', function (store, records, success) {
            if (me.getSecuritySuiteCombobox() !== undefined) {
                if (success && records && records.length === 0) {
                    store.add(Mdc.model.SecuritySuite.noSecuritySuite());
                    me.getSecuritySuiteCombobox().setValue(-1);
                    me.getSecuritySuiteCombobox().hide();
                } else if (success && records) {
                    if (records.length === 1) me.getSecuritySuiteCombobox().setValue(records[0].get('id'));
                    if (me.getSecuritySuiteCombobox().getValue() === -1) {
                        me.getSecuritySuiteCombobox().clearValue();
                        me.getSecuritySuiteCombobox().clearInvalid();
                    }
                    me.getSecuritySuiteCombobox().enable();
                    me.getSecuritySuiteCombobox().show();
                }
            }
        });

        me.getAuthenticationLevelsStore().on('load', function (store, records, success) {
            if (success && records && records.length === 0) {
                store.add(Mdc.model.AuthenticationLevel.noAuthentication());
                me.getAuthCombobox().setValue(-1);
                me.getAuthCombobox().hide();
            } else if (success && records) {
                if (records.length === 1) me.getAuthCombobox().setValue(records[0].get('id'));
                if (me.isInvalidLevel(records, me.getAuthCombobox().getValue())) {
                    me.getAuthCombobox().clearValue();
                    me.getAuthCombobox().clearInvalid();
                }
                me.getAuthCombobox().enable();
                me.getAuthCombobox().show();
            }
        });
        me.getEncryptionLevelsStore().on('load', function (store, records, success) {
            if (success && records && records.length === 0) {
                store.add(Mdc.model.EncryptionLevel.noEncryption());
                me.getEncrCombobox().setValue(-1);
                me.getEncrCombobox().hide();
            } else if (success && records) {
                if (records.length === 1) me.getEncrCombobox().setValue(records[0].get('id'));
                if (me.isInvalidLevel(records, me.getEncrCombobox().getValue())) {
                    me.getEncrCombobox().clearValue();
                    me.getEncrCombobox().clearInvalid();
                }
                me.getEncrCombobox().enable();
                me.getEncrCombobox().show();
            }
        });
        me.getRequestSecurityLevelsStore().on('load', function (store, records, success) {
            if (success && records && records.length === 0) {
                store.add(Mdc.model.RequestSecurityLevel.noRequestSecurity());
                me.getRequestSecurityCombobox().setValue(-1);
                me.getRequestSecurityCombobox().hide();
            } else if (success && records) {
                if (records.length === 1) me.getRequestSecurityCombobox().setValue(records[0].get('id'));
                if (me.isInvalidLevel(records, me.getRequestSecurityCombobox().getValue())) {
                    me.getRequestSecurityCombobox().clearValue();
                    me.getRequestSecurityCombobox().clearInvalid();
                }
                me.getRequestSecurityCombobox().enable();
                me.getRequestSecurityCombobox().show();
            }
        });
        me.getResponseSecurityLevelsStore().on('load', function (store, records, success) {
            if (success && records && records.length === 0) {
                store.add(Mdc.model.ResponseSecurityLevel.noResponseSecurity());
                me.getResponseSecurityCombobox().setValue(-1);
                me.getResponseSecurityCombobox().hide();
            } else if (success && records) {
                if (records.length === 1) me.getResponseSecurityCombobox().setValue(records[0].get('id'));
                if (me.isInvalidLevel(records, me.getResponseSecurityCombobox().getValue())) {
                    me.getResponseSecurityCombobox().clearValue();
                    me.getResponseSecurityCombobox().clearInvalid();
                }
                me.getResponseSecurityCombobox().enable();
                me.getResponseSecurityCombobox().show();
            }
        });
    },

    isInvalidLevel: function (records, level) {
        if (level === -1) return true;
        for (i = 0; i < records.length; i++) {
            if (records[i].getId() === level) {
                return false;
            }
        }
        return true;
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
                    me.store.load();
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
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: form
            });

        preloader.show();
        detailPanel.setTitle(Ext.String.htmlEncode(record.getData().name));
        form.loadRecord(record);
        preloader.destroy();
    },

    showSecuritySettings: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            securitySuitesStore = me.getSecuritySuitesStore();

        me.currentDeviceTypeId = deviceTypeId;
        me.currentDeviceConfigurationId = deviceConfigurationId;

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
                        me.deviceProtocolSupportSecuritySuites = deviceConfig.get('deviceProtocolSupportSecuritySuites');
                        securitySuitesStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
                        securitySuitesStore.getProxy().setExtraParam('securitySuiteId', null);
                        widget = Ext.widget('securitySettingSetup', {
                            deviceTypeId: deviceTypeId,
                            deviceConfigId: deviceConfigurationId,
                            deviceProtocolSupportsClient: deviceConfig.get('deviceProtocolSupportsClient'),
                            deviceProtocolSupportSecuritySuites: deviceConfig.get('deviceProtocolSupportSecuritySuites')
                        });
                        widget.down('#stepsMenu').setHeader(deviceConfig.get('name'));
                        me.getApplication().fireEvent('changecontentevent', widget);

                        if (mainView) mainView.setLoading(false);
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.deviceTypeName = deviceType.get('name');
                        me.deviceConfigName = deviceConfig.get('name');
                    }
                });
            }
        });
    },

    showSecuritySettingsCreateView: function (deviceTypeId, deviceConfigurationId) {
        var me = this;

        me.currentDeviceTypeId = deviceTypeId;
        me.currentDeviceConfigurationId = deviceConfigurationId;
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

                        me.deviceProtocolSupportSecuritySuites = deviceConfig.get('deviceProtocolSupportSecuritySuites');
                        var deviceProtocolSupportsClient = deviceConfig.get('deviceProtocolSupportsClient'),
                            deviceProtocolSupportSecuritySuites = deviceConfig.get('deviceProtocolSupportSecuritySuites'),
                            container = Ext.widget('securitySettingForm', {
                                deviceTypeId: deviceTypeId,
                                deviceConfigurationId: deviceConfigurationId,
                                securityHeader: Uni.I18n.translate('securitySetting.addSecuritySetting', 'MDC', 'Add security setting'),
                                actionButtonName: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                securityAction: 'add'
                            }),
                            record = me.createSecuritySettingModel(deviceTypeId, deviceConfigurationId).create();
                        me.configureProxyOfAllSecurityStores(null);
                        me.loadAllSecurityStores(true, deviceProtocolSupportSecuritySuites);
                        me.hideClientFieldIfNotApplicable(deviceProtocolSupportsClient);
                        container.down('form#myForm').loadRecord(record);
                        me.getApplication().fireEvent('changecontentevent', container);

                    }
                });
            }
        });
    },

    showSecuritySettingsEditView: function (deviceTypeId, deviceConfigurationId, securitySettingId) {
        var me = this;

        me.currentDeviceTypeId = deviceTypeId;
        me.currentDeviceConfigurationId = deviceConfigurationId;
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
                        me.deviceProtocolSupportSecuritySuites = deviceConfig.get('deviceProtocolSupportSecuritySuites');
                        var deviceProtocolSupportsClient = deviceConfig.get('deviceProtocolSupportsClient'),
                            deviceProtocolSupportSecuritySuites = deviceConfig.get('deviceProtocolSupportSecuritySuites');
                        me.createSecuritySettingModel(deviceTypeId, deviceConfigurationId).load(securitySettingId, {
                            success: function (securitySetting) {
                                var container = Ext.widget('securitySettingForm', {
                                    deviceTypeId: deviceTypeId,
                                    deviceConfigurationId: deviceConfigurationId,
                                    securityHeader: Ext.String.format(Uni.I18n.translate('securitySetting.editX', 'MDC', "Edit security setting '{0}'"), securitySetting.get('name')),
                                    actionButtonName: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                    securityAction: 'save'
                                });
                                me.configureProxyOfAllSecurityStores(securitySetting.get('securitySuiteId'));
                                me.loadAllSecurityStores(false, deviceProtocolSupportSecuritySuites);
                                me.hideClientFieldIfNotApplicable(deviceProtocolSupportsClient);
                                container.down('form#myForm').loadRecord(securitySetting);
                                me.getApplication().fireEvent('changecontentevent', container);
                            }
                        });
                    }
                });
            }
        });
    },

    hideClientFieldIfNotApplicable: function (deviceProtocolSupportsClient) {
        var me = this,
            clientField = me.getClientField();

        if (!deviceProtocolSupportsClient) {
            clientField.allowBlank = true;   // If the protocol doesn't support it, then don't require a values
            clientField.hide();
        }
    },

    configureProxyOfAllSecurityStores: function (securitySuiteId) {
        var me = this,
            securitySuitesStore = me.getSecuritySuitesStore(),
            authenticationLevelStore = me.getAuthenticationLevelsStore(),
            encryptionLevelStore = me.getEncryptionLevelsStore(),
            requestSecurityLevelStore = me.getRequestSecurityLevelsStore(),
            responseSecurityLevelStore = me.getResponseSecurityLevelsStore();

        securitySuitesStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        authenticationLevelStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        encryptionLevelStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        requestSecurityLevelStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        responseSecurityLevelStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        securitySuitesStore.getProxy().setExtraParam('securitySuiteId', securitySuiteId);
        authenticationLevelStore.getProxy().setExtraParam('securitySuiteId', securitySuiteId);
        encryptionLevelStore.getProxy().setExtraParam('securitySuiteId', securitySuiteId);
        requestSecurityLevelStore.getProxy().setExtraParam('securitySuiteId', securitySuiteId);
        responseSecurityLevelStore.getProxy().setExtraParam('securitySuiteId', securitySuiteId);
    },

    loadAllSecurityStores: function (createView, deviceProtocolSupportSecuritySuites) {
        var me = this,
            authCombobox = me.getAuthCombobox(),
            encrCombobox = me.getEncrCombobox(),
            requestSecurityCombobox = me.getRequestSecurityCombobox(),
            responseSecurityCombobox = me.getResponseSecurityCombobox(),
            securitySuitesStore = me.getSecuritySuitesStore(),
            authenticationLevelStore = me.getAuthenticationLevelsStore(),
            encryptionLevelStore = me.getEncryptionLevelsStore(),
            requestSecurityLevelStore = me.getRequestSecurityLevelsStore(),
            responseSecurityLevelStore = me.getResponseSecurityLevelsStore();

        if (createView) {
            if (deviceProtocolSupportSecuritySuites) {
                authCombobox.disable(); // Disable these fields (they will become enabled again after selection of a security suite)
                encrCombobox.disable();
                requestSecurityCombobox.disable();
                responseSecurityCombobox.disable();
            } else {
                authenticationLevelStore.load();
                encryptionLevelStore.load();
                me.hideSecuritySuiteFields();
            }
        } else {
            authenticationLevelStore.load();
            encryptionLevelStore.load();
            if (deviceProtocolSupportSecuritySuites) {
                securitySuitesStore.load();
                requestSecurityLevelStore.load();
                responseSecurityLevelStore.load();
            } else {
                me.hideSecuritySuiteFields();
            }
        }
    },

    hideSecuritySuiteFields: function () {
        var me = this;

        me.getSecuritySuiteCombobox().allowBlank = true;
        me.getSecuritySuiteCombobox().hide();
        me.getRequestSecurityCombobox().allowBlank = true;
        me.getRequestSecurityCombobox().hide();
        me.getResponseSecurityCombobox().allowBlank = true;
        me.getResponseSecurityCombobox().hide();
    },

    updateUsedSecurityLevelPossibilitiesBasedOnSecuritySuite: function (combobox, record) {
        var me = this,
            authCombobox = me.getAuthCombobox(),
            authSecurityLevelsStore = authCombobox.getStore(),
            encrCombobox = me.getEncrCombobox(),
            encrSecurityLevelsStore = encrCombobox.getStore(),
            requestSecurityCombobox = me.getRequestSecurityCombobox(),
            requestSecurityLevelsStore = requestSecurityCombobox.getStore(),
            responseSecurityCombobox = me.getResponseSecurityCombobox(),
            responseSecurityLevelsStore = responseSecurityCombobox.getStore();

        authSecurityLevelsStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        authSecurityLevelsStore.getProxy().setExtraParam('securitySuiteId', me.getSecuritySuiteCombobox().getValue());
        authSecurityLevelsStore.load();
        encrSecurityLevelsStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        encrSecurityLevelsStore.getProxy().setExtraParam('securitySuiteId', me.getSecuritySuiteCombobox().getValue());
        encrSecurityLevelsStore.load();
        requestSecurityLevelsStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        requestSecurityLevelsStore.getProxy().setExtraParam('securitySuiteId', me.getSecuritySuiteCombobox().getValue());
        requestSecurityLevelsStore.load();
        responseSecurityLevelsStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        responseSecurityLevelsStore.getProxy().setExtraParam('securitySuiteId', me.getSecuritySuiteCombobox().getValue());
        responseSecurityLevelsStore.load();
    },

    createSecuritySettingModel: function (deviceTypeId, deviceConfigurationId) {
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
                msg: Uni.I18n.translate('general.saving', 'MDC', 'Saving...'),
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