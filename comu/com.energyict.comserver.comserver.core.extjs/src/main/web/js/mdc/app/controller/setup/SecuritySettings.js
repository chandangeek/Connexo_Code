Ext.define('Mdc.controller.setup.SecuritySettings', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.securitysettings.SecuritySettingSetup',
        'setup.securitysettings.SecuritySettingGrid',
        'setup.securitysettings.SecuritySettingPreview',
        'setup.securitysettings.SecuritySettingEdit',
        'setup.securitysettings.SecuritySettingFiltering',
        'setup.securitysettings.SecuritySettingSorting',
        'setup.securitysettings.SecuritySettingDockedItems',
        'setup.securitysettings.SecuritySettingEmptyList',
        'setup.securitysettings.SecuritySettingSideFilter',
        'setup.securitysettings.SecuritySettingFloatingPanel',
        'setup.securitysettings.SecuritySettingForm'
    ],

    stores: [
        'SecuritySettingsOfDeviceConfiguration',
        'AuthenticationLevels',
        'EncryptionLevels'
    ],

    refs: [
        {ref: 'formPanel', selector: 'securitySettingForm'},
        {ref: 'securityGridPanel', selector: 'securitySettingGrid'},
        {ref: 'securityPreviewPanel', selector: 'securitySettingPreview'}
    ],

    deviceTypeName: null,
    deviceConfigName: null,

    init: function () {
        this.control({
            'securitySettingSetup securitySettingGrid': {
                itemclick: this.loadGridItemDetail
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
                click: this.showConfirmationPanel
            },
            'securitySettingFloatingPanel button[action=cancel]': {
                click: this.closeFloatingMessage
            },
            'securitySettingFloatingPanel button[action=delete]': {
                click: this.deleteRecord
            }
        });

        this.listen({
            store: {
                '#Mdc.store.SecuritySettingsOfDeviceConfiguration': {
                    load: this.checkSecurityCount
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

    showConfirmationPanel: function () {
        var grid = this.getSecurityGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            overview = Ext.ComponentQuery.query('securitySettingSetup')[0],
            confirmationMessage = Ext.widget('securitySettingFloatingPanel');
        overview.disable();
        confirmationMessage.show({
            width: overview.getWidth() / 3,
            title: "Remove " + lastSelected.getData().name + "?",
            msg: "This security setting configuration will no longer be available",
            icon: Ext.MessageBox.WARNING
        });
    },


    enableOverviewPanel: function () {
        var overviewPanel = Ext.ComponentQuery.query('securitySettingSetup')[0];
        if (!Ext.isEmpty(overviewPanel)) {
            overviewPanel.enable();
        }
    },

    closeFloatingMessage: function (btn) {
        this.enableOverviewPanel();
        btn.up('securitySettingFloatingPanel').hide();
    },


    deleteRecord: function (btn) {
        var grid = this.getSecurityGridPanel(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            me = this;
        btn.up('securitySettingFloatingPanel').hide();
        this.enableOverviewPanel();
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/securityproperties/' + lastSelected.getData().id,
            method: 'DELETE',
            waitMsg: 'Removing...',
            success: function () {
                Ext.create('widget.uxNotification', {
                    html: 'Security setting was removed successfully',
                    ui: 'notification-success'
                }).show();
                me.store.load();
            },
            failure: function (result, request) {
                var data = Ext.JSON.decode(result.responseText);
                Ext.widget('messagebox', {
                    buttons: [
                        {
                            text: 'Close',
                            handler: function (button, event) {
                                this.up('messagebox').hide();
                            }
                        }
                    ]
                }).show({
                        ui: 'notification-error',
                        title: 'Error during removing of security setting',
                        msg: data.message,
                        icon: Ext.MessageBox.ERROR
                    })
            }
        });
    },

    addAuthAndEncrStores: function (form) {
        var authCombobox = form.down('combobox[name=authenticationLevelId]'),
            encrCombobox = form.down('combobox[name=encryptionLevelId]');
        authCombobox.store = this.authstore;
        encrCombobox.store = this.encrstore;
    },

    checkSecurityCount: function () {
        var grid = this.getSecurityGridPanel();
        if (!Ext.isEmpty(grid)) {
            var numberOfSecuritiesContainer = Ext.ComponentQuery.query('securitySettingSetup securitySettingDockedItems container[name=SecurityCount]')[0],
                emptyMessage = Ext.ComponentQuery.query('securitySettingSetup')[0].down('#SecuritySettingEmptyList'),
                gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                securityCount = this.store.getCount(),
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

            if (securityCount < 1) {
                grid.hide();
                emptyMessage.add(
                    {
                        xtype: 'securitySettingEmptyList',
                        deviceTypeId: this.deviceTypeId,
                        deviceConfigurationId: this.deviceConfigurationId
                    }
                );
                this.getSecurityPreviewPanel().hide();
            } else {
                selectionModel.select(0);
                grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
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

    loadGridItemDetail: function (grid, record) {
        var detailPanel = Ext.ComponentQuery.query('securitySettingSetup securitySettingPreview')[0],
            form = detailPanel.down('form'),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: form
            });
        if (this.displayedItemId != record.getData().id) {
            grid.clearHighlight();
            preloader.show();
        }
        this.displayedItemId = record.getData().id;
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
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
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
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
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
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
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
                        success: function () {
                            me.handleSuccessRequest('Successfully created');
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
                        success: function () {
                            me.handleSuccessRequest('Successfully updated');
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

    handleSuccessRequest: function (headerText) {
        window.location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings';

        Ext.create('widget.uxNotification', {
            html: headerText,
            ui: 'notification-success'
        }).show();
    },

    handleFailureRequest: function (response, headerText) {
        var me = this,
            result = Ext.JSON.decode(response.responseText, true),
            formErrorsPanel = me.getFormPanel().down('panel[name=errors]'),
            form = me.getFormPanel().down('form').getForm(),
            errorText = "Unknown error occurred";

        if(result && result.errors) {
            form.markInvalid(result.errors);
            me.showErrorPanel();
            return;
        }
        if(!Ext.isEmpty(response.statusText)) {
            errorText = response.statusText;
        }
        if(result && result.message) {
            errorText = result.message;
        }

        Ext.Msg.show({
            title: headerText,
            msg: errorText,
            icon: Ext.MessageBox.WARNING,
            buttons: Ext.MessageBox.CANCEL,
            ui: 'notification-error'
        });
    }

});