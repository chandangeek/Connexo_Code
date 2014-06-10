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
        'AuthenticationLevels',
        'EncryptionLevels'
    ],

    refs: [
        {ref: 'formPanel', selector: 'securitySettingForm'},
        {ref: 'securityGridPanel', selector: 'securitySettingGrid'}
    ],

    deviceTypeName: null,
    deviceConfigName: null,
    secId: -1,

    init: function () {
        this.control({
            'securitySettingSetup securitySettingGrid': {
                select: this.loadGridItemDetail
            },
            'securitySettingSetup': {
                afterrender: this.loadStore
            },
            'securitySettingForm': {
                afterrender: this.addAuthAndEncrStores
            },
            'securitySettingForm button[name=securityaction]': {
                click: this.onSubmit
            },
            'menu menuitem[action=editsecuritysetting]': {
                click: this.editRecord
            },
            'menu menuitem[action=deletesecuritysetting]': {
                click: this.removeSecuritySetting
            }
        });

        this.listen({
            store: {
                '#Mdc.store.SecuritySettingsOfDeviceConfiguration': {
                    load: this.onStoreLoad
                }
            }
        });

        this.store = this.getStore('Mdc.store.SecuritySettingsOfDeviceConfiguration');
        this.authstore = this.getStore('Mdc.store.AuthenticationLevels');
        this.encrstore = this.getStore('Mdc.store.EncryptionLevels');
    },

    editRecord: function () {
        var grid = this.getSecurityGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        window.location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings/' + lastSelected.getData().id + '/edit';
    },

    removeSecuritySetting: function() {
        var me = this,
            grid = me.getSecurityGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: "This security setting configuration will no longer be available",
            title: "Remove " + ' ' + lastSelected.getData().name + '?',
            config: {
                securitySettingToDelete: lastSelected,
                me: me
            },
            fn: me.removeSecuritySettingRecord
        });
    },

    removeSecuritySettingRecord: function(btn, text, cfg) {
        if (btn === 'confirm') {
            var me = cfg.config.me,
                securitySettingToDelete = cfg.config.securitySettingToDelete;

            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securityproperties/' + securitySettingToDelete.getData().id,
                method: 'DELETE',
                waitMsg: 'Removing...',
                success: function () {
                    Ext.create('widget.uxNotification', {
                        html: 'Security setting was removed successfully',
                        ui: 'notification-success'
                    }).show();
                    me.store.load();
                },
                failure: function (response, request) {
                    var errorText = "Unknown error occurred";

                    if(response.status == 400) {
                        var result = Ext.JSON.decode(response.responseText, true);
                        if(result && result.message) {
                            errorText = result.message;
                        }

                        me.getApplication().getController('Uni.controller.Error').showError("Error during removing of security setting", errorText);
                    }
                }
            });
        }
    },

    addAuthAndEncrStores: function (form) {
        var authCombobox = form.down('combobox[name=authenticationLevelId]'),
            encrCombobox = form.down('combobox[name=encryptionLevelId]');
        authCombobox.store = this.authstore;
        encrCombobox.store = this.encrstore;
    },

    onStoreLoad: function (store) {
        var grid = this.getSecurityGridPanel();
        if (!Ext.isEmpty(grid)) {
            var numberOfSecuritiesContainer = Ext.ComponentQuery.query('#securitySettingGrid #securityCount')[0],
                gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                securityCount = store.getCount(),
                widget,
                securityWord;

            if (securityCount == 1) {
                securityWord = ' security setting'
            } else {
                securityWord = ' security settings'
            }
            var widget = Ext.widget('container', {
                html: securityCount + securityWord
            });

            numberOfSecuritiesContainer.removeAll(true);
            numberOfSecuritiesContainer.add(widget);

            if (securityCount > 0) {
                if(securityCount > 1) {
                    var index = store.find("id", this.secId);
                    if(index == -1) {
                        selectionModel.select(0);
                    } else {
                        selectionModel.select(index);
                        this.secId = -1;
                    }
                } else {
                    selectionModel.select(0);
                }
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
                msg: "Loading...",
                target: form
            });

        preloader.show();
        detailPanel.setTitle(record.getData().name);
        form.loadRecord(record);
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
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadAuthAndEncrStores(deviceTypeId, deviceConfigurationId);
        var widget = Ext.widget('securitySettingForm', {deviceTypeId: deviceTypeId, deviceConfigurationId: deviceConfigurationId, securityHeader: 'Add security setting', actionButtonName: 'Add'});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        me.fillCombobox(widget.down('combobox[name=encryptionLevelId]'), me.encrstore, null, 'No encryption');
                        me.fillCombobox(widget.down('combobox[name=authenticationLevelId]'), me.authstore, null, 'No authentication');
                        me.deviceTypeName = deviceType.get('name');
                        me.deviceConfigName = deviceConfig.get('name');
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    showSecuritySettingsEditView: function (deviceTypeId, deviceConfigurationId, securitySettingId) {
        var me = this;
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.securitySettingId = securitySettingId;
        me.loadAuthAndEncrStores(deviceTypeId, deviceConfigurationId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        Ext.Ajax.request({
                            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securityproperties/' + me.securitySettingId,
                            params: {scope: 'edit'},
                            method: 'GET',
                            success: function (response) {
                                var security = Ext.JSON.decode(response.responseText),
                                    widget = Ext.widget('securitySettingForm', {deviceTypeId: deviceTypeId, deviceConfigurationId: deviceConfigurationId, securityHeader: 'Edit ' + security.name, actionButtonName: 'Save'});
                                me.deviceTypeName = deviceType.get('name');
                                me.deviceConfigName = deviceConfig.get('name');
                                me.getApplication().fireEvent('changecontentevent', widget);
                                widget.down('textfield[name=name]').setValue(security.name);
                                me.fillCombobox(widget.down('combobox[name=encryptionLevelId]'), me.encrstore, security.encryptionLevelId, 'No encryption');
                                me.fillCombobox(widget.down('combobox[name=authenticationLevelId]'), me.authstore, security.authenticationLevelId, 'No authentication');
                            }
                        });
                    }
                });
            }
        });
    },

    loadAuthAndEncrStores: function (deviceTypeId, deviceConfigurationId) {
        this.authstore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        this.encrstore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        this.authstore.load();
        this.encrstore.load();
    },

    fillCombobox: function (combobox, store, value, emptyText) {
        switch (store.getCount()) {
            case 0:
                store.add({
                    name: emptyText,
                    id: -1
                });
                combobox.setValue(store.first());
                combobox.disable();
                break;
            case 1:
                combobox.setValue(store.first());
                combobox.disable();
                break;
            default:
                if (!Ext.isEmpty(value)) {
                    if (value == -1) {
                        store.add({
                            name: emptyText,
                            id: -1
                        });
                    }
                    combobox.setValue(value);
                }
                break;
        }
    },

    onSubmit: function (btn) {
        var me = this,
            formPanel = me.getFormPanel(),
            form = formPanel.down('form').getForm(),
            formErrorsPanel = Ext.ComponentQuery.query('securitySettingForm panel[name=errors]')[0],
            jsonValues = Ext.JSON.encode(form.getValues()),
            preloader;
        if (form.isValid()) {
            formErrorsPanel.hide();
            switch (btn.text) {
                case 'Add':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Creating security setting",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securityproperties',
                        method: 'POST',
                        jsonData: jsonValues,
                        success: function (response) {
                            me.handleSuccessRequest(response, 'Successfully created');
                        },
                        failure: function (response) {
                            me.handleFailureRequest(response, 'Error during create');
                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
                case 'Save':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Updating security setting",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securityproperties/' + me.securitySettingId,
                        method: 'PUT',
                        jsonData: jsonValues,
                        success: function (response) {
                            me.handleSuccessRequest(response, 'Successfully updated');
                        },
                        failure: function (response) {
                            me.handleFailureRequest(response, 'Error during update');
                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
            }
        } else {
            me.showErrorPanel();
        }
    },

    showErrorPanel: function() {
        var me = this,
            formErrorsPanel = me.getFormPanel().down('panel[name=errors]');

        formErrorsPanel.hide();
        formErrorsPanel.removeAll();
        formErrorsPanel.add({
            html: 'There are errors on this page that require your attention.'
        });
        formErrorsPanel.show();
    },

    handleSuccessRequest: function (response, headerText) {
        var data = Ext.JSON.decode(response.responseText, true);
        if(data && data.id) {
            this.secId = parseInt(data.id);
        }
        window.location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings';

        Ext.create('widget.uxNotification', {
            html: headerText,
            ui: 'notification-success'
        }).show();
    },

    handleFailureRequest: function (response, headerText) {
        var me = this,
            form = me.getFormPanel().down('form').getForm(),
            errorText = "Unknown error occurred";

        if(response.status == 400) {
            var result = Ext.JSON.decode(response.responseText, true);
            if(result && result.errors) {
                form.markInvalid(result.errors);
                me.showErrorPanel();
                return;
            }
            if(result && result.message) {
                errorText = result.message;
            }

            me.getApplication().getController('Uni.controller.Error').showError(headerText, errorText);
        }
    }

});