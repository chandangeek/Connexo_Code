/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.LoadProfileConfigurations', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationSetup',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationSorting',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationFiltering',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationGrid',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview',
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationForm',
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

    models: [
        'Mdc.model.DeviceType',
        'Mdc.model.DeviceConfiguration',
        'Mdc.model.LoadProfileConfiguration'
    ],

    refs: [
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'loadProfileConfigurationDetailForm', selector: '#loadProfileConfigurationDetails'},
        {ref: 'loadConfigurationGrid', selector: '#loadProfileConfigurationGrid'},
        {ref: 'loadConfigurationCountContainer', selector: '#loadProfileConfigurationCountContainer'},
        {ref: 'loadConfigurationEmptyListContainer', selector: '#loadProfileConfigurationEmptyListContainer'},
        {ref: 'loadProfileConfigurationPreview', selector: 'loadProfileConfigurationSetup loadProfileConfigurationPreview'},
        {ref: 'editPage', selector: 'loadProfileConfigurationForm'},
        {ref: 'loadProfileConfigPreviewForm', selector: '#loadProfileConfigPreviewForm'},
        {
            ref: 'obisCodeField',
            selector: '#LoadProfileConfigurationFormId #obis-code-field'
        },
        {
            ref: 'restoreObisCodeBtn',
            selector: '#LoadProfileConfigurationFormId #mdc-restore-obiscode-btn'
        }
    ],

    deviceTypeId: null,
    deviceConfigurationId: null,
    originalObisCode: null,

    init: function () {
        this.control({
            'loadProfileConfigurationSetup loadProfileConfigurationGrid': {
                select: this.loadGridItemDetail
            },
            'loadProfileConfigurationForm button[name=loadprofileconfigurationaction]': {
                click: this.onSubmit
            },
            'menu menuitem[action=editloadprofileconfigurationondeviceconfiguration]': {
                click: this.editRecord
            },
            'menu menuitem[action=deleteloadprofileconfigurationondeviceonfiguration]': {
                click: this.showConfirmationPanel
            },
            '#LoadProfileConfigurationFormId #obis-code-field': {
                change: this.onObisCodeChange
            },
            '#LoadProfileConfigurationFormId #mdc-restore-obiscode-btn': {
                click: this.onRestoreObisCodeBtnClicked
            }
        });
    },

    showDeviceConfigurationLoadProfilesView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            router = me.getController('Uni.controller.history.Router'),
            dependenciesCounter = 3,
            models = {
                deviceType: me.getModel('Mdc.model.DeviceType'),
                deviceConfiguration: me.getModel('Mdc.model.DeviceConfiguration'),
                loadProfileConfiguration: me.getModel('Mdc.model.LoadProfileConfiguration')
            },
            onDependenciesLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    mainView.setLoading(false);
                    me.getStore('Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration').getProxy().setUrl(deviceTypeId, deviceConfigurationId);
                    models.loadProfileConfiguration.getProxy().setUrl(deviceTypeId, deviceConfigurationId);
                    widget = Ext.widget('loadProfileConfigurationSetup', {
                        router: router,
                        deviceTypeId: deviceTypeId,
                        deviceConfigurationId: deviceConfigurationId
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfiguration.get('name'));
                }
            },
            widget,
            deviceConfiguration;

        mainView.setLoading();

        me.getStore('Mdc.store.Intervals').load(onDependenciesLoad);

        models.deviceType.load(deviceTypeId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDeviceType', record);
            },
            callback: onDependenciesLoad
        });

        models.deviceConfiguration.getProxy().setExtraParam('deviceType', deviceTypeId);
        models.deviceConfiguration.load(deviceConfigurationId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDeviceConfiguration', record);
                deviceConfiguration = record;
            },
            callback: onDependenciesLoad
        });
    },

    loadGridItemDetail: function (selectionModel, record) {
        var me = this,
            preview = me.getLoadProfileConfigurationPreview(),
            menu = preview.down('menu');

        Ext.suspendLayouts();
        preview.down('form').loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        Ext.resumeLayouts(true);
        if (menu) {
            menu.record = record;
        }
    },

    showDeviceConfigurationLoadProfilesEditView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
        var me = this,
            isEdit = arguments.length === 3,
            models = {
                deviceType: me.getModel('Mdc.model.DeviceType'),
                deviceConfiguration: me.getModel('Mdc.model.DeviceConfiguration'),
                loadProfileConfiguration: me.getModel('Mdc.model.LoadProfileConfiguration')
            },
            router = me.getController('Uni.controller.history.Router'),
            previousPath = me.getController('Uni.controller.history.EventBus').getPreviousPath(),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            widget = Ext.widget('loadProfileConfigurationForm', {
                cancelLink: previousPath
                    ? '#' + previousPath
                    : router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles').buildUrl(),
                edit: isEdit
            }),
            form = widget.down('form'),
            availableConfigurations;

        me.getApplication().fireEvent('changecontentevent', widget);

        models.loadProfileConfiguration.getProxy().setUrl(deviceTypeId, deviceConfigurationId);
        if (isEdit) {
            mainView.setLoading();
            models.loadProfileConfiguration.load(loadProfileConfigurationId, {
                success: function (record) {
                    me.originalObisCode = record.get('obisCode');
                    Ext.suspendLayouts();
                    widget.down('#LoadProfileConfigurationFormId').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'",[record.get('name')]));
                    form.loadRecord(record);
                    Ext.resumeLayouts(true);
                },
                callback: function () {
                    mainView.setLoading(false);
                }
            });
        } else {
            availableConfigurations = me.getStore('Mdc.store.LoadProfileConfigurationsOnDeviceConfigurationAvailable');
            availableConfigurations.getProxy().setUrl(deviceTypeId, deviceConfigurationId);
            availableConfigurations.load();
            Ext.suspendLayouts();
            form.loadRecord(Ext.create('Mdc.model.LoadProfileConfiguration'));
            Ext.resumeLayouts(true);
        }

        models.deviceType.load(deviceTypeId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDeviceType', record);
            }
        });

        models.deviceConfiguration.getProxy().setExtraParam('deviceType', deviceTypeId);
        models.deviceConfiguration.load(deviceConfigurationId, {
            success: function (record) {
                me.getApplication().fireEvent('loadDeviceConfiguration', record);
            }
        });
    },

    editRecord: function (menuItem) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            record = menuItem.up('menu').record;

        router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/edit').forward(Ext.merge(router.arguments, {loadProfileConfigurationId: record.getId()}));
    },

    showConfirmationPanel: function (menuItem) {
        var me = this,
            forRemoval = menuItem.up('menu').record;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('loadProfileConfigurations.confirmWindow.removeMsg', 'MDC', 'This load profile configuration will be removed from the list.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?",[forRemoval.get('name')]),
            fn: function (action) {
                if (action === 'confirm') {
                    me.confirmationPanelHandler(forRemoval);
                }
            }
        });
    },

    confirmationPanelHandler: function (forRemoval) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            router = me.getController('Uni.controller.history.Router');

        mainView.setLoading();
        forRemoval.destroy({
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('loadProfileConfigurations.removeSuccessMsg', 'MDC', 'Load profile configuration was removed successfully'));
                router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles').forward();
            },
            callback: function () {
                mainView.setLoading(false);
            }
        });
    },

    onSubmit: function (btn) {
        var me = this,
            editPage = me.getEditPage(),
            form = editPage.down('form'),
            basicForm = form.getForm(),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            formErrorsPanel = form.down('uni-form-error-message[name=errors]'),
            backUrl = editPage.cancelLink,
            record = form.getRecord(),
            modelProxy = me.getModel('Mdc.model.LoadProfileConfiguration').getProxy();

        if (basicForm.isValid()) {
            formErrorsPanel.hide();
            mainView.setLoading();
            form.updateRecord(record);
            if (!editPage.edit) {            // workaround to create entity with a given id
                record.phantom = true;       // force 'POST' method for request otherwise 'PUT' will be performed
                modelProxy.appendId = false; // remove 'id' part from request url
            }
            record.save({
                backUrl: backUrl,
                success: function () {
                    window.location.href = backUrl;
                    me.getApplication().fireEvent('acknowledge', editPage.edit
                        ? Uni.I18n.translate('loadprofileconfig.acknowledgment.saved', 'MDC', 'Load profile configuration saved')
                        : Uni.I18n.translate('loadprofileconfig.acknowledgment.added', 'MDC', 'Load profile configuration added'));
                },
                failure: function (record, options) {
                    var responseObj;

                    if (options && options.response && options.response.status === 400) {
                        responseObj = Ext.decode(options.response.responseText, true);
                        if (responseObj && responseObj.errors) {
                            Ext.suspendLayouts();
                            formErrorsPanel.show();
                            basicForm.markInvalid(responseObj.errors);
                            Ext.resumeLayouts(true);
                        }
                    }
                },
                callback: function () {
                    mainView.setLoading(false);
                }
            });
            modelProxy.appendId = true; // restore id for normal functionality
        } else {
            formErrorsPanel.show();
        }
    },

    onObisCodeChange: function(obisCodeField, newValue) {
        var me = this;
        me.getRestoreObisCodeBtn().setDisabled(newValue === me.originalObisCode);
        me.getRestoreObisCodeBtn().setTooltip(
            newValue === me.originalObisCode
                ? null
                : Uni.I18n.translate('general.obisCode.reset.tooltip2', 'MDC', 'Reset to {0}, the OBIS code of the load profile type', me.originalObisCode)
        );
    },

    onRestoreObisCodeBtnClicked: function() {
        var me = this;
        me.getObisCodeField().setValue(me.originalObisCode);
        me.onObisCodeChange(me.getObisCodeField(), me.originalObisCode);
    }
});