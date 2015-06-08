Ext.define('Mdc.controller.setup.LoadProfileConfigurations', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.loadprofileconfiguration.LoadProfileConfigurationSetup',
        'setup.loadprofileconfiguration.LoadProfileConfigurationSorting',
        'setup.loadprofileconfiguration.LoadProfileConfigurationFiltering',
        'setup.loadprofileconfiguration.LoadProfileConfigurationGrid',
        'setup.loadprofileconfiguration.LoadProfileConfigurationPreview',
        'setup.loadprofileconfiguration.LoadProfileConfigurationForm',
        'Cfg.view.validation.RulePreview'
    ],

    requires: [
        'Mdc.store.AvailableRegisterTypesForDeviceConfiguration',
        'Mdc.store.RegisterTypesOfDevicetype',
        'Uni.util.Common'
    ],


    stores: [
        'Mdc.store.Intervals',
        'Mdc.store.LoadProfileTypes',
        'Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration',
        'Mdc.store.LoadProfileConfigurationsOnDeviceConfigurationAvailable'
    ],

    refs: [
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'loadProfileConfigurationDetailForm', selector: '#loadProfileConfigurationDetails'},
        {ref: 'loadConfigurationGrid', selector: '#loadProfileConfigurationGrid'},
        {ref: 'loadConfigurationCountContainer', selector: '#loadProfileConfigurationCountContainer'},
        {ref: 'loadConfigurationEmptyListContainer', selector: '#loadProfileConfigurationEmptyListContainer'},
        {ref: 'loadProfileConfigurationPreview', selector: '#loadProfileConfigurationPreview'},
        {ref: 'loadConfigurationForm', selector: '#LoadProfileConfigurationFormId'},
        {ref: 'loadProfileConfigPreviewForm', selector: '#loadProfileConfigPreviewForm'}
    ],

    deviceTypeId: null,
    deviceConfigurationId: null,

    init: function () {
        this.control({
            'loadProfileConfigurationSetup': {
                afterrender: this.loadStore
            },
            'loadProfileConfigurationSetup button[action=addloadprofileconfiguration]': {
                click: this.renderAddPage
            },
            'loadProfileConfigurationSetup loadProfileConfigurationGrid': {
                select: this.loadGridItemDetail
            },
            'loadProfileConfigurationForm combobox[name=id]': {
                select: this.changeDisplayedObisCode
            },
            'loadProfileConfigurationForm button[name=loadprofileconfigurationaction]': {
                click: this.onSubmit
            },
            'button[action=loadprofileconfigurationnotificationerrorretry]': {
                click: this.retrySubmit
            },
            'menu menuitem[action=editloadprofileconfigurationondeviceconfiguration]': {
                click: this.editRecord
            },
            'menu menuitem[action=deleteloadprofileconfigurationondeviceonfiguration]': {
                click: this.showConfirmationPanel
            }
        });

        this.intervalStore = this.getStore('Intervals').load();
        this.store = this.getStore('LoadProfileConfigurationsOnDeviceConfiguration');
        this.availableLoadProfileTypesStore = this.getStore('LoadProfileConfigurationsOnDeviceConfigurationAvailable');
    },

    previewValidationRule: function (grid, record) {
            var selectedRules = this.getRulesForLoadProfileConfigGrid().getSelectionModel().getSelection();

            if (selectedRules.length === 1) {
                var selectedRule = selectedRules[0];
                this.getValidationRulesForLoadProfileConfigPreview().updateValidationRule(selectedRule)
                this.getValidationRulesForLoadProfileConfigPreview().show();
            } else {
                this.getValidationRulesForLoadProfileConfigPreview().hide();
            }
    },

    renderAddPage: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/add').forward();
    },

    editRecord: function () {
        var grid = this.getLoadConfigurationGrid(),
            lastSelected,
            me,
            loadProfileConfigurationId;

        if (grid) {
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
            me = this;
            loadProfileConfigurationId = lastSelected.getData().id;
        } else {
            me = this.getController('Mdc.controller.setup.LoadProfileConfigurationDetails');
            loadProfileConfigurationId = me.loadProfileConfigurationId;
        }

        window.location.href = '#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(me.deviceConfigurationId) + '/loadprofiles/' + encodeURIComponent(loadProfileConfigurationId) + '/edit';
    },

    showConfirmationPanel: function () {
        var grid = this.getLoadConfigurationGrid(),
            lastSelectedName;

        if (grid) {
            lastSelectedName = grid.getView().getSelectionModel().getLastSelected().get('name');
        } else {
            lastSelectedName = this.getController('Mdc.controller.setup.LoadProfileConfigurationDetails').loadProfileConfiguration.name;
        }

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('loadProfileConfigurations.confirmWindow.removeMsg', 'MDC', 'This load profile configuration will be removed from the list.'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + " '" + lastSelectedName + "'?",
            config: {
                me: this
            },
            fn: this.confirmationPanelHandler
        });
    },

    confirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            lastSelectedId,
            self;

        if (me.getLoadConfigurationGrid()) {
            self = me;
            lastSelectedId = me.getLoadConfigurationGrid().getView().getSelectionModel().getLastSelected().getData().id;
        } else {
            self = me.getController('Mdc.controller.setup.LoadProfileConfigurationDetails');
            lastSelectedId = self.loadProfileConfiguration.id;
        }

        if (state === 'confirm') {
            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + self.deviceTypeId + '/deviceconfigurations/' + self.deviceConfigurationId + '/loadprofileconfigurations/' + lastSelectedId,
                method: 'DELETE',
                waitMsg: 'Removing...',
                success: function () {
                    me.handleSuccessRequest(Uni.I18n.translate('loadProfileConfigurations.removeSuccessMsg', 'MDC', 'Load profile configuration was removed successfully'));
                    me.store.loadPage(1);
                },
                failure: function (response) {
                    var errorText,
                        errorTitle;

                    if (response.status == 400) {
                        errorText = Ext.decode(response.responseText, true).message;
                        errorTitle = Uni.I18n.translate('loadProfileConfigurations.removeErrorMsg', 'MDC', 'Error during removing of load profile configuration');

                        me.getApplication().getController('Uni.controller.Error').showError(errorTitle, errorText);
                    }
                }
            });
        }
    },

    changeDisplayedObisCode: function (combobox) {
        var record = this.availableLoadProfileTypesStore.findRecord('id', combobox.getValue());
        combobox.next().setValue(record.getData().obisCode);
    },

    retrySubmit: function (btn) {
        var formPanel = this.getLoadConfigurationForm();
        btn.up('messagebox').hide();
        if (!Ext.isEmpty(formPanel)) {
            var submitBtn = formPanel.down('button[name=loadprofileconfigurationaction]');
            if (!Ext.isEmpty(submitBtn)) {
                this.onSubmit(submitBtn);
            }
        }
    },

    onSubmit: function (btn) {
        var me = this,
            formPanel = me.getLoadConfigurationForm(),
            form = formPanel.getForm(),
            formErrorsPanel = formPanel.down('uni-form-error-message[name=errors]'),
            formValue = form.getValues(),
            preloader,
            jsonValues;
        if (form.isValid()) {
            jsonValues = Ext.JSON.encode(formValue);
            formErrorsPanel.hide();
            switch (btn.action) {
                case 'Add':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Adding load profile configuration",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations',
                        method: 'POST',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest(Uni.I18n.translate('loadprofileconfig.acknowlegment.added', 'MDC', 'Load profile configuration added'));
                        },
//                        failure: function (response) {
//                            me.handleFailureRequest(response, 'Error during create', 'loadprofileconfigurationnotificationerrorretry');
//                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
                case 'Save':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Editing load profile configuration",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId,
                        method: 'PUT',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest(Uni.I18n.translate('loadprofileconfig.acknowlegment.saved', 'MDC', 'Load profile configuration saved'));
                        },
//                        failure: function (response) {
//                            me.handleFailureRequest(response, 'Error during update', 'loadprofileconfigurationnotificationerrorretry');
//                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
            }
        } else {
            formErrorsPanel.show();
        }
    },

    handleSuccessRequest: function (headerText) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles').forward();
        this.getApplication().fireEvent('acknowledge', headerText);
    },

    handleFailureRequest: function (response, headerText, retryAction) {
        var result = Ext.JSON.decode(response.responseText),
            errormsgs = '',
            me = this;
        Ext.each(result.errors, function (error) {
            errormsgs += error.msg + '<br>'
        });
        if (errormsgs == '') {
            errormsgs = result.message;
        }
        Ext.widget('messagebox', {
            buttons: [
//                {
//                    text: 'Retry',
//                    action: retryAction,
//                    ui: 'remove'
//                },
                {
                    text: 'Cancel',
                    action: 'cancel',
                    ui: 'link',
                    href: '#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(me.deviceConfigurationId) + '/loadprofiles',
                    handler: function (button, event) {
                        this.up('messagebox').hide();
                    }
                }
            ]
        }).show({
            ui: 'notification-error',
            title: headerText,
            msg: errormsgs,
            icon: Ext.MessageBox.ERROR
        })
    },

    loadStore: function () {
        this.store.load({
            params: {
                sort: 'name'
            }
        });
    },

    loadGridItemDetail: function (selectionModel, record) {
        Ext.suspendLayouts();
        this.getLoadProfileConfigPreviewForm().loadRecord(record);
        this.getLoadProfileConfigurationPreview().setTitle(record.get('name'));
        Ext.resumeLayouts(true);
    },

    showDeviceConfigurationLoadProfilesView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            preloader = Ext.create('Ext.container.Container'),
            widget;

        me.getApplication().fireEvent('changecontentevent', preloader);
        preloader.setLoading(true);

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        Uni.util.Common.loadNecessaryStores([
            'Mdc.store.Intervals'
        ], function () {
            widget = Ext.widget('loadProfileConfigurationSetup', {
                config: {
                    gridStore: me.store,
                    deviceTypeId: deviceTypeId,
                    deviceConfigurationId: deviceConfigurationId
                },
                router: me.getController('Uni.controller.history.Router')
            });
            me.getApplication().fireEvent('changecontentevent', widget);
        }, false);
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

                    }
                });
            }
        });
    },

    showDeviceConfigurationLoadProfilesAddView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            widget = Ext.widget('loadProfileConfigurationForm', {deviceTypeId: deviceTypeId, deviceConfigurationId: deviceConfigurationId, loadProfileConfigurationAction: 'Add'});

        var title = Uni.I18n.translate('loadprofileconfigurations.addloadprofileconfigurations', 'MDC', 'Add load profile configuration');
        widget.down('#LoadProfileConfigurationFormId').setTitle(title);

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        me.availableLoadProfileTypesStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
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
                        me.availableLoadProfileTypesStore.load();
                        widget.down('combobox[name=id]').store = me.availableLoadProfileTypesStore
                    }
                });
            }
        });
    },

    showDeviceConfigurationLoadProfilesEditView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
        var me = this,
            widget = Ext.widget('loadProfileConfigurationForm', {deviceTypeId: deviceTypeId, deviceConfigurationId: deviceConfigurationId, loadProfileConfigurationAction: 'Save'});

        var title = Uni.I18n.translate('loadprofileconfigurations.editloadprofileconfigurations', 'MDC', 'Edit');
        widget.down('#LoadProfileConfigurationFormId').setTitle(title);

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadProfileConfigurationId = loadProfileConfigurationId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        me.availableLoadProfileTypesStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfig);
                        Ext.Ajax.request({
                            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + loadProfileConfigurationId,
                            params: {},
                            method: 'GET',
                            success: function (response) {
                                var record = Ext.JSON.decode(response.responseText).data[0],
                                    overruledObisCode = (record.obisCode == record.overruledObisCode) ? '' : record.overruledObisCode;
                                me.getApplication().fireEvent('loadLoadProfile', record);
                                me.deviceTypeName = deviceType.get('name');
                                me.deviceConfigName = deviceConfig.get('name');
                                me.getApplication().fireEvent('changecontentevent', widget);
                                widget.down('combobox[name=id]').store = me.store;
                                me.store.add(record);
                                widget.down('combobox[name=id]').setValue(record.id);
                                widget.down('combobox[name=id]').hide();
                                widget.down('displayfield[name=loadprofiletype]').setValue(record.name);
                                widget.down('displayfield[name=loadprofiletype]').show();
                                widget.down('[name=obisCode]').setValue(record.obisCode);
                                widget.down('[name=overruledObisCode]').setValue(overruledObisCode);
                                var title = Uni.I18n.translate('loadprofileconfigurations.editloadprofileconfigurations', 'MDC', 'Edit') + " '" + record.name + "'";
                                widget.down('#LoadProfileConfigurationFormId').setTitle(title);
                            }
                        });
                    }
                });
            }
        });
    }
});