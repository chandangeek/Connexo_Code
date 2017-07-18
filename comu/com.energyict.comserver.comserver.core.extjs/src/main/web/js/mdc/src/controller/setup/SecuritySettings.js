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
        'ResponseSecurityLevels',
        'ConfigurationSecurityProperties'
    ],

    models: [
        'Uni.property.model.Property'
    ],

    refs: [
        {ref: 'formPanel', selector: 'securitySettingForm'},
        {ref: 'securitySettingPreview', selector: 'securitySettingPreview'},
        {ref: 'securitySettingPreviewForm', selector: '#mdc-security-settings-preview-form'},
        {ref: 'securitySettingPreviewDetailsTitle', selector: '#mdc-security-settings-preview-details-title'},
        {ref: 'securitySettingFormDetailsTitle', selector: '#mdc-security-settings-form-details-title'},
        {ref: 'securityGridPanel', selector: 'securitySettingGrid'},
        {ref: 'securitySuiteCombobox', selector: '#securitySuiteCombobox'},
        {ref: 'authCombobox', selector: '#authCombobox'},
        {ref: 'encrCombobox', selector: '#encrCombobox'},
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
    deviceProtocolSupportsClient: undefined,
    deviceProtocolSupportSecuritySuites: undefined,
    storeCounter: 0,
    avoidReloadOfPropertiesStore: false,

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
                select: this.updateUsedSecurityLevelPossibilitiesBasedOnSecuritySuiteAndTriggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified
            },
            '#authCombobox': {
                select: this.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified
            },
            '#encrCombobox': {
                select: this.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified
            },

            '#requestSecurityCombobox': {
                select: this.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified
            },
            '#responseSecurityCombobox': {
                select: this.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified
            }
        });

        me.store = this.getStore('Mdc.store.SecuritySettingsOfDeviceConfiguration');
        me.getSecuritySuitesStore().on('load', function (store, records, success) {
            if (me.getSecuritySuiteCombobox() !== undefined) {
                if (success && records && records.length === 0) {
                    me.getSecuritySuiteCombobox().setValue(-1);
                    me.getSecuritySuiteCombobox().hide();
                } else if (success && records) {
                    if (records.length === 1) me.getSecuritySuiteCombobox().setValue(records[0].get('id'));
                    if (me.getSecuritySuiteCombobox().getValue() === -1) {
                        me.getSecuritySuiteCombobox().clearValue();
                        me.getSecuritySuiteCombobox().clearInvalid();
                    }
                    me.getSecuritySuiteCombobox().show();
                }
            }
        });

        me.getAuthenticationLevelsStore().on('load', function (store, records, success) {
            if (success && records && records.length === 0) {
                me.getAuthCombobox().setValue(-1);
                me.getAuthCombobox().hide();
            } else if (success && records) {
                if (records.length === 1) {
                    me.getAuthCombobox().setValue(records[0].get('id'));
                }
                if (me.isInvalidLevel(records, me.getAuthCombobox().getValue())) {
                    me.getAuthCombobox().clearValue();
                    me.getAuthCombobox().clearInvalid();
                }
                me.getAuthCombobox().show();
            }
            me.storeCounter--;
            if (me.storeCounter <= 0) {
                me.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified();
            }
        });
        me.getEncryptionLevelsStore().on('load', function (store, records, success) {
            if (success && records && records.length === 0) {
                me.getEncrCombobox().setValue(-1);
                me.getEncrCombobox().hide();
            } else if (success && records) {
                if (records.length === 1) {
                    me.getEncrCombobox().setValue(records[0].get('id'));
                }
                if (me.isInvalidLevel(records, me.getEncrCombobox().getValue())) {
                    me.getEncrCombobox().clearValue();
                    me.getEncrCombobox().clearInvalid();
                }
                me.getEncrCombobox().show();
            }
            me.storeCounter--;
            if (me.storeCounter <= 0) {
                me.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified();
            }
        });
        me.getRequestSecurityLevelsStore().on('load', function (store, records, success) {
            if (success && records && records.length === 0) {
                me.getRequestSecurityCombobox().setValue(-1);
                me.getRequestSecurityCombobox().hide();
            } else if (success && records) {
                if (records.length === 1) {
                    me.getRequestSecurityCombobox().setValue(records[0].get('id'));
                }
                if (me.isInvalidLevel(records, me.getRequestSecurityCombobox().getValue())) {
                    me.getRequestSecurityCombobox().clearValue();
                    me.getRequestSecurityCombobox().clearInvalid();
                }
                me.getRequestSecurityCombobox().show();
            }
            me.storeCounter--;
            if (me.storeCounter <= 0) {
                me.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified();
            }
        });
        me.getResponseSecurityLevelsStore().on('load', function (store, records, success) {
            if (success && records && records.length === 0) {
                me.getResponseSecurityCombobox().setValue(-1);
                me.getResponseSecurityCombobox().hide();
            } else if (success && records) {
                if (records.length === 1) {
                    me.getResponseSecurityCombobox().setValue(records[0].get('id'));
                }
                if (me.isInvalidLevel(records, me.getResponseSecurityCombobox().getValue())) {
                    me.getResponseSecurityCombobox().clearValue();
                    me.getResponseSecurityCombobox().clearInvalid();
                }
                me.getResponseSecurityCombobox().show();
            }
            me.storeCounter--;
            if (me.storeCounter <= 0) {
                me.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified();
            }
        });
        me.getConfigurationSecurityPropertiesStore().on('load', function (store, records, success) {
            var formPanel = me.getFormPanel(),
                form = formPanel.down('form#myForm'),
                propertyForm = formPanel.down('property-form'),
                record;
            record = form.getRecord();
            if (success && records.length) {
                record.propertiesStore.removeAll();
                record.propertiesStore.add(records);
                propertyForm.loadRecord(record);
                propertyForm.show();
                me.getSecuritySettingFormDetailsTitle().setVisible(true);
            } else {
                propertyForm.hide();
                propertyForm.removeAll();
                me.getSecuritySettingFormDetailsTitle().setVisible(false);
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
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicesecuritysetting.saveSuccess.msg.remove', 'MDC', 'Security set removed'));
                    me.store.load();
                },
                failure: function (response, request) {
                    var errorInfo = Uni.I18n.translate('devicesecuritysetting.removeErrorMsg', 'MDC', 'Error during removal of security set'),
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
        var me = this;
        var securitySetting = me.getSecurityGridPanel().getSelectionModel().getSelection();
        me.getSecuritySettingPreview().down('property-form').remove();
        if (securitySetting.length == 1) {
            var securitySettingName = securitySetting[0].get('name');
            me.getSecuritySettingPreview().setTitle(Ext.String.htmlEncode(securitySettingName));
            me.getSecuritySettingPreview().loadRecord(securitySetting[0]);
            me.getSecuritySettingPreview().down('property-form').readOnly = true;
            me.getSecuritySettingPreview().down('property-form').loadRecord(securitySetting[0]);
            me.getSecuritySettingPreviewDetailsTitle().setVisible(securitySetting[0].propertiesStore.data.items.length > 0);
        }
    },

    triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified: function () {
        var me = this,
            authCombobox = me.getAuthCombobox(),
            encrCombobox = me.getEncrCombobox(),
            securitySuiteCombobox = me.getSecuritySuiteCombobox(),
            requestSecurityCombobox = me.getRequestSecurityCombobox(),
            responseSecurityCombobox = me.getResponseSecurityCombobox(),
            authenticationLevelId = authCombobox.getValue(),
            encryptionLevelId = encrCombobox.getValue(),
            securitySuiteId = securitySuiteCombobox.getValue(),
            requestSecurityLevelId = requestSecurityCombobox.getValue(),
            responseSecurityLevelId = responseSecurityCombobox.getValue(),
            configurationSecurityPropertiesStore = me.getConfigurationSecurityPropertiesStore();

        if ((!authCombobox.isVisible() || authenticationLevelId !== null) &&
            (!encrCombobox.isVisible() || encryptionLevelId !== null) &&
            (!securitySuiteCombobox.isVisible() || securitySuiteId !== null) &&
            (!requestSecurityCombobox.isVisible() || requestSecurityLevelId !== null) &&
            (!responseSecurityCombobox.isVisible() || responseSecurityLevelId !== null)) {
            configurationSecurityPropertiesStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
            configurationSecurityPropertiesStore.getProxy().setExtraParam('authenticationLevelId', authenticationLevelId);
            configurationSecurityPropertiesStore.getProxy().setExtraParam('encryptionLevelId', encryptionLevelId);
            configurationSecurityPropertiesStore.getProxy().setExtraParam('securitySuiteId', securitySuiteId);
            configurationSecurityPropertiesStore.getProxy().setExtraParam('requestSecurityLevelId', requestSecurityLevelId);
            configurationSecurityPropertiesStore.getProxy().setExtraParam('responseSecurityLevelId', responseSecurityLevelId);
            if (me.avoidReloadOfPropertiesStore) {
                me.avoidReloadOfPropertiesStore = false;
            } else {
                configurationSecurityPropertiesStore.load();
            }
        } else {
            // Else, not all of the security levels are specified
            me.getFormPanel().down('property-form').hide();
            me.getFormPanel().down('property-form').removeAll();
            me.getSecuritySettingFormDetailsTitle().setVisible(false);
        }
    },

    showSecuritySettings: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            securitySuitesStore = me.getSecuritySuitesStore(),
            widget;

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
                        me.deviceProtocolSupportsClient = deviceConfig.get('deviceProtocolSupportsClient');
                        me.deviceProtocolSupportSecuritySuites = deviceConfig.get('deviceProtocolSupportSecuritySuites');
                        securitySuitesStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
                        securitySuitesStore.getProxy().setExtraParam('securitySuiteId', null);
                        widget = Ext.widget('securitySettingSetup', {
                            deviceTypeId: deviceTypeId,
                            deviceConfigId: deviceConfigurationId,
                            deviceProtocolSupportsClient: me.deviceProtocolSupportsClient,
                            deviceProtocolSupportSecuritySuites: me.deviceProtocolSupportSecuritySuites
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
        var me = this,
            record,
            clientSecurity,
            container,
            reader;

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
                        me.deviceProtocolSupportsClient = deviceConfig.get('deviceProtocolSupportsClient');
                        me.deviceProtocolSupportSecuritySuites = deviceConfig.get('deviceProtocolSupportSecuritySuites');
                        container = Ext.widget('securitySettingForm', {
                            deviceTypeId: deviceTypeId,
                            deviceConfigurationId: deviceConfigurationId,
                            securityHeader: Uni.I18n.translate('securitySetting.addSecuritySet', 'MDC', 'Add security set'),
                            actionButtonName: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            securityAction: 'add'
                        });
                        record = me.createSecuritySettingModel(deviceTypeId, deviceConfigurationId).create();
                        me.configureProxyOfAllSecurityStores(null);
                        me.loadAllSecurityStores(true, me.deviceProtocolSupportSecuritySuites);
                        container.down('form#myForm').loadRecord(record);
                        container.down('property-form').loadRecord(record);
                        if (me.deviceProtocolSupportsClient) {
                            Ext.Ajax.request({
                                url: '/api/dtc/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/securityproperties/clienttype',
                                method: 'GET',
                                success: function (response) {
                                    var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                                    if (!Ext.isEmpty(decoded)) {
                                        var reader = Ext.create('Ext.data.reader.Json', {
                                            model: 'Uni.property.model.Property'
                                        });
                                        clientSecurity = reader.read(decoded).records[0];
                                        record = container.down('form#myForm').getRecord();
                                        record.beginEdit();
                                        record.setClient(clientSecurity);
                                        container.createClientField(clientSecurity);
                                    }
                                    me.getApplication().fireEvent('changecontentevent', container);
                                }
                            });
                        }
                        else {
                            me.getApplication().fireEvent('changecontentevent', container);
                        }

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
                me.avoidReloadOfPropertiesStore = true;
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.setDeviceTypeName(deviceType.get('name'));
                        me.setDeviceConfigName(deviceConfig.get('name'));
                        me.deviceProtocolSupportsClient = deviceConfig.get('deviceProtocolSupportsClient');
                        me.deviceProtocolSupportSecuritySuites = deviceConfig.get('deviceProtocolSupportSecuritySuites');
                        me.createSecuritySettingModel(deviceTypeId, deviceConfigurationId).load(securitySettingId, {
                            success: function (securitySetting) {
                                var container = Ext.widget('securitySettingForm', {
                                    deviceTypeId: deviceTypeId,
                                    deviceConfigurationId: deviceConfigurationId,
                                    securityHeader: Ext.String.htmlDecode(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", securitySetting.get('name'))),
                                    actionButtonName: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                    securityAction: 'save'
                                });
                                me.configureProxyOfAllSecurityStores(securitySetting.get('securitySuiteId'));
                                container.down('form#myForm').loadRecord(securitySetting);
                                var propertyForm = container.down('property-form');
                                if (securitySetting.properties().count()) {
                                    propertyForm.show();
                                    propertyForm.loadRecord(securitySetting);
                                    me.getSecuritySettingFormDetailsTitle().setVisible(true);
                                } else {
                                    propertyForm.hide();
                                    me.getSecuritySettingFormDetailsTitle().setVisible(false);
                                }
                                me.getApplication().fireEvent('loadSecuritySetting', securitySetting);
                                if (me.deviceProtocolSupportsClient) {
                                    container.createClientField(securitySetting.getClient());
                                }
                                me.getApplication().fireEvent('changecontentevent', container);
                                me.loadAllSecurityStores(false, me.deviceProtocolSupportSecuritySuites);
                            }
                        });
                    }
                });
            }
        });
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
                authCombobox.hide();
                encrCombobox.hide();
                requestSecurityCombobox.hide();
                responseSecurityCombobox.hide();
            } else {
                me.storeCounter = 2;
                authenticationLevelStore.load();
                encryptionLevelStore.load();
                me.hideSecuritySuiteFields();
            }
        } else {
            me.storeCounter = 2;
            authenticationLevelStore.load();
            encryptionLevelStore.load();
            if (deviceProtocolSupportSecuritySuites) {
                me.storeCounter += 2;
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

    updateUsedSecurityLevelPossibilitiesBasedOnSecuritySuiteAndTriggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified: function (combobox, record) {
        var me = this,
            authCombobox = me.getAuthCombobox(),
            authSecurityLevelsStore = authCombobox.getStore(),
            encrCombobox = me.getEncrCombobox(),
            encrSecurityLevelsStore = encrCombobox.getStore(),
            requestSecurityCombobox = me.getRequestSecurityCombobox(),
            requestSecurityLevelsStore = requestSecurityCombobox.getStore(),
            responseSecurityCombobox = me.getResponseSecurityCombobox(),
            responseSecurityLevelsStore = responseSecurityCombobox.getStore(),
            storesToLoad = me.storeCounter = 4;

        callBackFunction = function () {
            storesToLoad--;
            if (storesToLoad <= 0) {
                // Delay the update of attributes till all stores are loaded
                me.triggerUpdateOfAttributesIfAllSecurityLevelsAreSpecified();
            }
        };
        authSecurityLevelsStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        authSecurityLevelsStore.getProxy().setExtraParam('securitySuiteId', me.getSecuritySuiteCombobox().getValue());
        authSecurityLevelsStore.load({callback: callBackFunction});
        encrSecurityLevelsStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        encrSecurityLevelsStore.getProxy().setExtraParam('securitySuiteId', me.getSecuritySuiteCombobox().getValue());
        encrSecurityLevelsStore.load({callback: callBackFunction});
        requestSecurityLevelsStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        requestSecurityLevelsStore.getProxy().setExtraParam('securitySuiteId', me.getSecuritySuiteCombobox().getValue());
        requestSecurityLevelsStore.load({callback: callBackFunction});
        responseSecurityLevelsStore.getProxy().setUrl(me.currentDeviceTypeId, me.currentDeviceConfigurationId);
        responseSecurityLevelsStore.getProxy().setExtraParam('securitySuiteId', me.getSecuritySuiteCombobox().getValue());
        responseSecurityLevelsStore.load({callback: callBackFunction});
    },

    createSecuritySettingModel: function (deviceTypeId, deviceConfigurationId) {
        var securitySettingModel = Ext.ModelManager.getModel('Mdc.model.SecuritySetting');
        securitySettingModel.getProxy().url = '/api/dtc/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/securityproperties/';
        return securitySettingModel;
    },

    onSubmit: function (btn) {
        var me = this,
            formPanel = me.getFormPanel(),
            form = formPanel.down('form#myForm'),
            propertyForm = formPanel.down('property-form'),
            record = form.getRecord(),
            property,
            value,
            error;

        form.getForm().clearInvalid();
        propertyForm.getForm().clearInvalid();
        if (form.isValid() && propertyForm.isValid()) {
            record.beginEdit();
            me.hideErrorPanel();
            var preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.saving', 'MDC', 'Saving...'),
                target: formPanel
            });
            preloader.show();
            form.updateRecord();
            propertyForm.updateRecord();
            record.propertiesStore = propertyForm.getRecord() !== undefined ? propertyForm.getRecord().properties() : undefined;
            if (!Ext.isEmpty(form.clientKey)) {
                record.getClient().getPropertyValue().set('value', form.down('#' + form.clientKey).getValue());
            }
            record.endEdit();
            record.save({
                backUrl: me.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/deviceconfigurations/view/securitysettings').buildUrl(),
                success: function (response) {
                    me.handleSuccessRequest(response, Uni.I18n.translate('devicesecuritysetting.saveSuccess.msg.edit', 'MDC', 'Security set saved'));
                },
                failure: function (response, operation) {
                    if (operation) {
                        if (operation.error.status == 400) {
                            var result = Ext.JSON.decode(operation.response.responseText, true);
                            if (result && result.errors) {
                                error = result.errors.filter(function (obj) {
                                    return obj.id === 'clientDbValue';
                                });
                                if (!Ext.isEmpty(error)) {
                                    form.down('#' + form.clientKey).markInvalid(error[0].msg);
                                }
                                form.getForm().markInvalid(result.errors);
                                propertyForm.getForm().markInvalid(result.errors);
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