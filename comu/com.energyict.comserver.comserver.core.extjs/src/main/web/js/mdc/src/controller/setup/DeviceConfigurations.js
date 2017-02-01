/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceConfigurations', {
    extend: 'Ext.app.Controller',

    requires: [],
    deviceTypeId: null,
    deviceConfigurationId: null,
    views: [
        'setup.deviceconfiguration.DeviceConfigurationsSetup',
        'setup.deviceconfiguration.DeviceConfigurationsGrid',
        'setup.deviceconfiguration.DeviceConfigurationPreview',
        'setup.deviceconfiguration.DeviceConfigurationDetail',
        'setup.deviceconfiguration.DeviceConfigurationEdit',
        'setup.deviceconfiguration.DeviceConfigurationLogbooks',
        'setup.deviceconfiguration.AddLogbookConfigurations',
        'setup.deviceconfiguration.EditLogbookConfiguration',
        'setup.deviceconfiguration.DeviceConfigurationClone',
        'setup.deviceconfiguration.ChangeDeviceConfigurationView'
    ],

    stores: [
        'DeviceConfigurations',
        'DeviceTypes',
        'LogbookConfigurations'
    ],

    refs: [
        {ref: 'deviceConfigurationsGrid', selector: '#deviceconfigurationsgrid'},
        {ref: 'deviceConfigurationPreviewForm', selector: '#deviceConfigurationPreviewForm'},
        {ref: 'deviceConfigurationPreview', selector: '#deviceConfigurationPreview'},
        {ref: 'deviceConfigurationDetail', selector: '#deviceConfigurationDetail'},
        {ref: 'deviceConfigurationDetailForm', selector: '#deviceConfigurationDetailForm'},
        {ref: 'deviceConfigurationPreviewTitle', selector: '#deviceConfigurationPreviewTitle'},
        {ref: 'deviceConfigurationRegisterLink', selector: '#deviceConfigurationRegistersLink'},
        {ref: 'deviceConfigurationLogBookLink', selector: '#deviceConfigurationLogBooksLink'},
        {ref: 'deviceConfigurationLoadProfilesLink', selector: '#deviceConfigurationLoadProfilesLink'},
        {ref: 'deviceConfigurationDetailRegisterLink', selector: '#deviceConfigurationDetailRegistersLink'},
        {ref: 'deviceConfigurationDetailLogBookLink', selector: '#deviceConfigurationDetailLogBooksLink'},
        {ref: 'deviceConfigurationDetailLoadProfilesLink', selector: '#deviceConfigurationDetailLoadProfilesLink'},
        {ref: 'deviceConfigurationDetailDeviceTypeLink', selector: '#deviceConfigurationDetailDeviceTypeLink'},
        {ref: 'deviceConfigurationDetailForm', selector: '#deviceConfigurationDetailForm'},
        {ref: 'deviceConfigurationEditForm', selector: '#deviceConfigurationEditForm'},
        {ref: 'deviceConfigurationEdit', selector: '#deviceConfigurationEdit'},
        {ref: 'deviceConfigurationsSetup', selector: '#deviceConfigurationsSetup'},
        {ref: 'activateDeviceconfigurationMenuItem', selector: '#activateDeviceconfigurationMenuItem'},
        {ref: 'gatewayMessage', selector: '#deviceConfigurationEditForm #gatewayMessage'},
        {ref: 'addressableMessage', selector: '#deviceConfigurationEditForm #addressableMessage'},
        {ref: 'gatewayRadioGroup', selector: '#deviceConfigurationEditForm #deviceIsGatewayCombo'},
        {ref: 'addressableRadioGroup', selector: '#deviceConfigurationEditForm #directlyAddressableCombo'},
        {ref: 'typeOfGatewayRadioGroup', selector: '#deviceConfigurationEditForm #typeOfGatewayCombo'},
        {ref: 'typeOfGatewayRadioGroupContainer', selector: '#deviceConfigurationEditForm #typeOfGatewayComboContainer'},
        {ref: 'dataLoggerRadioGroup', selector: '#deviceConfigurationEditForm #dataLoggerRadioGroup'},
        {ref: 'dataLoggerMessage', selector: '#deviceConfigurationEditForm #dataLoggerMessage'},
        {ref: 'editLogbookConfiguration', selector: 'edit-logbook-configuration'},
        {ref: 'previewActionMenu', selector: 'deviceConfigurationPreview #device-configuration-action-menu'},
        {ref: 'deviceConfigurationCloneForm', selector: '#deviceConfigurationCloneForm'},
        {ref: 'deviceConfigurationsWaitReuest', selector: '#wait-request'},
        {ref: 'noDeviceConfigurationsLabel', selector: '#no-device-configuration'},
        {ref: 'newDeviceConfigurationCombo', selector: '#new-device-configuration'},
        {ref: 'changeDeviceConfigurationView', selector: '#change-device-configuration-view'},
        {ref: 'changeDeviceConfigurationForm', selector: '#change-device-configuration-form'},
        {ref: 'changeDeviceConfigurationFormErrors', selector: '#change-device-configuration-form #form-errors'},
        {ref: 'saveChangeDeviceConfigurationBtn', selector: '#save-change-device-configuration'},
        {ref: 'deviceConfigurationClonePage', selector: 'deviceConfigurationClone'},
        {
            ref: 'obisCodeField',
            selector: 'edit-logbook-configuration #obis-code-field'
        },
        {
            ref: 'restoreObisCodeBtn',
            selector: 'edit-logbook-configuration #mdc-restore-obiscode-btn'
        }
    ],

    originalObisCode: null,

    init: function () {
        this.control({
            '#deviceconfigurationsgrid': {
                selectionchange: this.previewDeviceConfiguration
            },
            '#deviceConfigurationsSetup button[action = createDeviceConfiguration]': {
                click: this.createDeviceConfigurationHistory
            },
            '#deviceconfigurationsgrid actioncolumn': {
                editDeviceConfiguration: this.editDeviceConfigurationHistory,
                cloneDeviceConfiguration: this.cloneDeviceConfigurationHistory,
                deleteDeviceConfiguration: this.deleteTheDeviceConfiguration
            },
            '#deviceConfigurationPreview menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationHistoryFromPreview
            },
            '#deviceConfigurationPreview menuitem[action=cloneDeviceConfiguration]': {
                click: this.cloneDeviceConfigurationHistoryFromPreview
            },
            'add-logbook-configurations add-logbook-configurations-grid': {
                selectionchange: this.enableAddBtn
            },
            '#deviceConfigurationPreview menuitem[action=deleteDeviceConfiguration]': {
                click: this.deleteDeviceConfigurationFromPreview
            },
            '#deviceConfigurationDetail menuitem[action=deleteDeviceConfiguration]': {
                click: this.deleteDeviceConfigurationFromDetails
            },
            '#deviceConfigurationDetail menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationFromDetailsHistory
            },
            '#deviceConfigurationDetail menuitem[action=cloneDeviceConfiguration]': {
                click: this.cloneDeviceConfigurationFromDetailsHistory
            },
            '#device-configuration-action-menu': {
                click: this.chooseAction
            },
            'deviceConfigurationEdit #createEditButton': {
                click: this.createEditDeviceConfiguration
            },
            'deviceConfigurationClone #clone-button': {
                click: this.cloneDeviceConfiguration
            },
            '#deviceConfigurationEditForm #deviceIsGatewayCombo': {
                change: this.showTypeOfGatewayRadioGroup
            },
            '#change-device-configuration-view button[action=save-change-device-configuration]': {
                click: this.saveChangeDeviceConfiguration
            },
            '#change-device-configuration-view button[action=cancel-change-device-configuration]': {
                click: this.cancelChangeDeviceConfiguration
            },
            '#new-device-configuration': {
                afterrender: this.deviceConfigRestoreState,
                select: this.deviceConfigSaveState
            },
            'edit-logbook-configuration #obis-code-field': {
                change: this.onObisCodeChange
            },
            'edit-logbook-configuration #mdc-restore-obiscode-btn': {
                click: this.onRestoreObisCodeBtnClicked
            }
        });
    },

    enableAddBtn: function (selectionModel) {
        var addBtn = Ext.ComponentQuery.query('add-logbook-configurations #logbookConfAdd')[0];
        if (addBtn) {
            selectionModel.getSelection().length > 0 ? addBtn.enable() : addBtn.disable();
        }
    },

    deviceConfigRestoreState: function (combo) {
        var device = this.getChangeDeviceConfigurationView().device,
            deviceConfigState = Ext.state.Manager.get('deviceConfigState');
        if (Ext.isObject(deviceConfigState)) {
            combo.setValue(deviceConfigState[device.get('name')]);
        }
        if (!Ext.isEmpty(combo.getValue())) {
            this.getSaveChangeDeviceConfigurationBtn().setDisabled(false);
        }
    },

    deviceConfigSaveState: function (combo) {
        var device = this.getChangeDeviceConfigurationView().device, deviceConfigState = {};
        deviceConfigState[device.get('name')] = combo.getValue();
        Ext.state.Manager.set('deviceConfigState', deviceConfigState);
        this.getSaveChangeDeviceConfigurationBtn().setDisabled(false);
    },

    showTypeOfGatewayRadioGroup: function (deviceIsGatewayRadioGroup) {
        this.getTypeOfGatewayRadioGroupContainer().setVisible(deviceIsGatewayRadioGroup.getValue().canBeGateway);
    },

    showDeviceConfigurations: function (id) {
        var me = this, widget = Ext.widget('deviceConfigurationsSetup', {deviceTypeId: id});
        this.deviceTypeId = id;
        this.getDeviceConfigurationsStore().getProxy().setExtraParam('deviceType', id);
        me.getApplication().fireEvent('changecontentevent', widget);
        this.getDeviceConfigurationsStore().load({
            callback: function () {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        if (widget.down('deviceTypeSideMenu')) {
                            widget.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
                            widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                                Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
                            );
                        }
                    }
                });
            }
        });
    },

    previewDeviceConfiguration: function () {
        var me = this,
            deviceConfigurations = me.getDeviceConfigurationsGrid().getSelectionModel().getSelection(),
            onDeviceTypeLoad = function() {
                var deviceConfigurationId = deviceConfigurations[0].get('id'),
                    registerLink = me.getDeviceConfigurationRegisterLink(),
                    logBookLink = me.getDeviceConfigurationLogBookLink(),
                    loadProfilesLink = me.getDeviceConfigurationLoadProfilesLink(),
                    preview = me.getDeviceConfigurationPreview(),
                    previewForm = me.getDeviceConfigurationPreviewForm();

                Ext.suspendLayouts();

                if (registerLink) {
                    registerLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/registerconfigurations');
                    registerLink.setText(
                        Uni.I18n.translatePlural('general.registerConfigurations', deviceConfigurations[0].get('registerCount'), 'MDC',
                            'No register configurations', '1 register configuration', '{0} register configurations')
                    );
                }

                if (logBookLink) {
                    logBookLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/logbookconfigurations');
                    logBookLink.setText(
                        Uni.I18n.translatePlural('general.logbookConfigurations', deviceConfigurations[0].get('logBookCount'), 'MDC',
                            'No logbook configurations', '1 logbook configuration', '{0} logbook configurations')
                    );
                }

                if (loadProfilesLink) {
                    loadProfilesLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/loadprofiles');
                    loadProfilesLink.setText(
                        Uni.I18n.translatePlural('general.loadProfileConfigurations', deviceConfigurations[0].get('loadProfileCount'), 'MDC',
                            'No load profile configurations', '1 load profile configuration', '{0} load profile configurations')
                    );
                }

                if (previewForm) {
                    previewForm.loadRecord(deviceConfigurations[0]);

                    var actionMenu = preview.down('#device-configuration-action-menu');
                    if (actionMenu) {
                        actionMenu.record = deviceConfigurations[0];
                    }

                    if (preview.down('#device-configuration-action-menu')) {
                        preview.down('#device-configuration-action-menu').record = deviceConfigurations[0];
                    }
                    preview.getLayout().setActiveItem(1);
                    preview.setTitle(Ext.String.htmlEncode(deviceConfigurations[0].get('name')));
                }

                Ext.resumeLayouts(true);
            };

        if (deviceConfigurations.length === 1) {
            Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
                success: function (deviceType) {
                    var logBookLink = me.getDeviceConfigurationLogBookLink();
                    if (logBookLink) {
                        if (deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE') {
                            logBookLink.hide();
                        } else {
                            logBookLink.show();
                        }
                    }
                    onDeviceTypeLoad();
                }
            });
        } else {
            me.getDeviceConfigurationPreview().getLayout().setActiveItem(0);
        }
    },

    showDeviceConfigurationDetailsView: function (devicetype, deviceconfiguration) {
        var me = this;
        var widget = Ext.widget('deviceConfigurationDetail', {
            deviceTypeId: devicetype,
            deviceConfigurationId: deviceconfiguration
        });
        var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
        this.deviceTypeId = devicetype;
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        deviceConfigModel.getProxy().setExtraParam('deviceType', devicetype);
        deviceConfigModel.load(deviceconfiguration, {
            success: function (deviceConfiguration) {
                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfiguration.get('name'));
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(devicetype, {
                    success: function (deviceType) {
                        var deviceConfigurationId = deviceConfiguration.get('id'),
                            registerLink = me.getDeviceConfigurationDetailRegisterLink(),
                            logBookLink = me.getDeviceConfigurationDetailLogBookLink(),
                            loadProfilesLink = me.getDeviceConfigurationDetailLoadProfilesLink();

                        me.getApplication().fireEvent('loadDeviceType', deviceType);

                        Ext.suspendLayouts();

                        registerLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/registerconfigurations');
                        registerLink.setText(
                            Uni.I18n.translatePlural('general.registerConfigurations', deviceConfiguration.get('registerCount'), 'MDC',
                                'No register configurations', '1 register configuration', '{0} register configurations')
                        );

                        if (deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE') {
                            logBookLink.hide();
                        } else {
                            logBookLink.show();
                            logBookLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/logbookconfigurations');
                            logBookLink.setText(
                                Uni.I18n.translatePlural('general.logbookConfigurations', deviceConfiguration.get('logBookCount'), 'MDC',
                                    'No logbook configurations', '1 logbook configuration', '{0} logbook configurations')
                            );
                        }

                        loadProfilesLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/loadprofiles');
                        loadProfilesLink.setText(
                            Uni.I18n.translatePlural('general.loadProfileConfigurations', deviceConfiguration.get('loadProfileCount'), 'MDC',
                                'No load profile configurations', '1 load profile configuration', '{0} load profile configurations')
                        );

                        widget.down('form').loadRecord(deviceConfiguration);
                        var menuItem = widget.down('#device-configuration-action-menu');
                        if (menuItem)
                            menuItem.record = deviceConfiguration;

                        Ext.resumeLayouts(true);

                        widget.setLoading(false);
                    }
                });
            }
        });
    },

    createDeviceConfigurationHistory: function () {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/add';
    },

    editDeviceConfigurationHistory: function (record) {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(record.get('id')) + '/edit';
    },

    editDeviceConfigurationHistoryFromPreview: function () {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0].get('id')) + '/edit';
    },

    editDeviceConfigurationFromDetailsHistory: function () {
        this.editDeviceConfigurationHistory(this.getDeviceConfigurationDetailForm().getRecord());
    },

    cloneDeviceConfigurationHistory: function (record) {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(record.get('id')) + '/clone';
    },

    cloneDeviceConfigurationHistoryFromPreview: function () {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0].get('id')) + '/clone';
    },

    cloneDeviceConfigurationFromDetailsHistory: function () {
        this.cloneDeviceConfigurationHistory(this.getDeviceConfigurationDetailForm().getRecord());
    },

    chooseAction: function (menu, item) {
        var me = this,
            grid = me.getDeviceConfigurationsGrid(),
            router = this.getController('Uni.controller.history.Router'),
            activeChange = 'notChanged',
            record,
            form,
            gridView,
            viewport;


        if (grid) {
            viewport = me.getDeviceConfigurationsSetup();
            gridView = grid.getView();
            record = gridView.getSelectionModel().getLastSelected();
            form = this.getDeviceConfigurationPreview().down('form');
        } else {
            viewport = me.getDeviceConfigurationDetail();
            record = menu.record;
            form = this.getDeviceConfigurationDetailForm();
        }

        switch (item.action) {
            case 'activateDeviceConfiguration':
                activeChange = true;
                break;
            case 'deactivateDeviceConfiguration':
                activeChange = false;
                break;
        }

        if (activeChange != 'notChanged') {
            viewport.setLoading(true);

            var recordId = record.get('id');
            var deviceTypeId = router.arguments['deviceTypeId'];
            record.set('active', activeChange);
            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + recordId + '/status',
                method: 'PUT',
                jsonData: record.getRecordData(),
                isNotEdit: true,
                success: function (response) {
                    router.getRoute().forward();
                    me.getApplication().fireEvent('acknowledge', activeChange ? Uni.I18n.translate('deviceconfiguration.activated', 'MDC', 'Device configuration activated') :
                        Uni.I18n.translate('deviceconfiguration.deactivated', 'MDC', 'Device configuration deactivated'));
                },
                failure: function () {
                    viewport.setLoading(false);
                    record.reject();
                }
            });
        }
    },

    deleteTheDeviceConfiguration: function (deviceConfigurationToDelete) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceconfiguration.removeDeviceConfiguration', 'MDC', 'This device configuration will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", deviceConfigurationToDelete.get('name')),
            config: {
                registerConfigurationToDelete: deviceConfigurationToDelete,
                me: me
            },
            fn: function (btn) {
                if (btn === 'confirm') {
                    deviceConfigurationToDelete.getProxy().setExtraParam('deviceType', me.deviceTypeId);
                    deviceConfigurationToDelete.destroy({
                        success: function () {
                            var grid = me.getDeviceConfigurationsGrid(),
                                gridPagingToolbar;
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconfiguration.acknowledgment.removed', 'MDC', 'Device configuration removed'));
                            if (router.currentRoute === 'administration/devicetypes/view/deviceconfigurations/view') {
                                router.getRoute('administration/devicetypes/view/deviceconfigurations').forward();
                            } else if (grid) {
                                gridPagingToolbar = grid.down('pagingtoolbartop');
                                gridPagingToolbar.isFullTotalCount = false;
                                gridPagingToolbar.totalCount = -1;
                                grid.down('pagingtoolbarbottom').totalCount--;
                                grid.getStore().loadPage(1);
                            }
                        },
                        failure: function () {

                        }
                    });
                }
            }
        });
    },

    deleteDeviceConfigurationFromPreview: function () {
        this.deleteTheDeviceConfiguration(this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0])
    },

    deleteDeviceConfigurationFromDetails: function () {
        var deviceConfigurationToDelete = this.getDeviceConfigurationDetailForm().getRecord();
        this.deleteTheDeviceConfiguration(deviceConfigurationToDelete);
    },

    showDeviceConfigurationCreateView: function (deviceTypeId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        var widget = Ext.widget('deviceConfigurationEdit', {
            edit: false,
            returnLink: '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations'
        });

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#deviceConfigurationEditCreateTitle').setTitle(
                    Uni.I18n.translate('general.adddeviceconfiguration', 'MDC', "Add device configuration")
                );
                me.setRadioButtons(deviceType);
            }
        });
    },

    setRadioButtons: function (deviceType, deviceConfiguration) {
        var addressableRadioGroup = this.getAddressableRadioGroup(),
            gatewayRadioGroup = this.getGatewayRadioGroup(),
            gatewayMessage = this.getGatewayMessage(),
            addressableMessage = this.getAddressableMessage(),
            typeOfGatewayRadioGroup = this.getTypeOfGatewayRadioGroup(),
            dataLoggerMessage = this.getDataLoggerMessage(),
            dataLoggerRadioGroup = this.getDataLoggerRadioGroup(),
            isDataLoggerSlaveType = deviceType.isDataLoggerSlave();

        if (deviceConfiguration) {
            addressableRadioGroup.setValue({isDirectlyAddressable: deviceConfiguration.get('isDirectlyAddressable')});
            gatewayRadioGroup.setValue({canBeGateway: deviceConfiguration.get('canBeGateway')});
            typeOfGatewayRadioGroup.setValue({gatewayType: deviceConfiguration.get('gatewayType')});
            dataLoggerRadioGroup.setValue({dataloggerEnabled: deviceConfiguration.get('dataloggerEnabled')});
            if (deviceConfiguration.get('active')) {
                addressableRadioGroup.disable();
                gatewayRadioGroup.disable();
                typeOfGatewayRadioGroup.disable();
                dataLoggerRadioGroup.disable();
            }
        }

        if (isDataLoggerSlaveType || !deviceType.get('canBeDirectlyAddressed')) {
            addressableMessage.down('container').setText(
                isDataLoggerSlaveType
                    ? Uni.I18n.translate('deviceconfiguration.addressableMessage.dataLoggerSlaves', 'MDC', 'Data logger slaves are not directly addressable')
                    : Uni.I18n.translate('deviceconfiguration.directlyAddressableMessage', 'MDC', 'The device cannot be directly addressed')
            );
            addressableRadioGroup.setValue({isDirectlyAddressable: false});
            addressableRadioGroup.disable();
        }

        if (isDataLoggerSlaveType || !deviceType.get('canBeGateway')) {
            gatewayMessage.down('container').setText(
                isDataLoggerSlaveType
                ? Uni.I18n.translate('deviceconfiguration.gatewayMessage.dataLoggerSlaves', 'MDC', 'Data logger slaves cannot act as gateways')
                : Uni.I18n.translate('deviceconfiguration.gatewayMessage.cannotActAsGateway', 'MDC', 'The device cannot act as a gateway')
            );
            gatewayRadioGroup.setValue({canBeGateway: false});
            gatewayRadioGroup.disable();
        }

        if (isDataLoggerSlaveType) {
            dataLoggerMessage.down('container').setText(
                Uni.I18n.translate('deviceconfiguration.dataLoggerMessage.dataLoggerSlaves', 'MDC', 'Data logger slaves cannot have data logger functionality')
            );
            dataLoggerRadioGroup.setValue({dataloggerEnabled: false});
            dataLoggerRadioGroup.disable();
        }
    },

    showDeviceConfigurationEditView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            router = me.getController('Uni.controller.history.Router'),
            previousPath = me.getController('Uni.controller.history.EventBus').getPreviousPath(),
            widget,
            returnLink;

        me.deviceTypeId = deviceTypeId;
        model.getProxy().setExtraParam('deviceType', this.deviceTypeId);


        if (!previousPath || previousPath.indexOf(deviceConfigurationId.toString()) == -1) {
            returnLink = router.getRoute('administration/devicetypes/view/deviceconfigurations').buildUrl({deviceTypeId: deviceTypeId});
        } else {
            returnLink = '#' + previousPath;
        }

        widget = Ext.widget('deviceConfigurationEdit', {
            edit: true,
            returnLink: returnLink
        });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);


        model.load(deviceConfigurationId, {
            success: function (deviceConfiguration) {
                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        widget.down('form').loadRecord(deviceConfiguration);
                        widget.down('#deviceConfigurationEditCreateTitle').setTitle(
                            Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [deviceConfiguration.get('name')]));
                        me.setRadioButtons(deviceType, deviceConfiguration);
                        widget.setLoading(false);
                    }
                });

            }
        });
    },

    showDeviceConfigurationCloneView: function (deviceTypeId, deviceConfigurationId) {
        var me = this, widget,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        model.getProxy().setExtraParam('deviceType', this.deviceTypeId);

        pageMainContent.setLoading(true);

        model.load(deviceConfigurationId, {
            success: function (deviceConfiguration) {
                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        widget = Ext.widget('deviceConfigurationClone', {
                            primaryDeviceConfiguration: deviceConfiguration,
                            router: router,
                            deviceConfigurationName: deviceConfiguration.get('name')
                        });
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        pageMainContent.setLoading(false);
                    }
                });

            }
        });
    },

    cloneDeviceConfiguration: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            name = me.getDeviceConfigurationCloneForm().getValues(),
            record = Ext.create(Mdc.model.DeviceConfiguration);

        me.getDeviceConfigurationCloneForm().getForm().clearInvalid();
        record.beginEdit();
        record.set(_.pick(me.getDeviceConfigurationClonePage().primaryDeviceConfiguration.getRecordData(), 'parent', 'version'));
        record.set(name);
        record.endEdit();
        pageMainContent.setLoading(true);

        record.save({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId,
            backUrl: router.getRoute('administration/devicetypes/view/deviceconfigurations').buildUrl(),
            success: function () {
                router.getRoute('administration/devicetypes/view/deviceconfigurations').forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconfiguration.acknowledgment.cloned', 'MDC', 'Device configuration cloned'));
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    me.getDeviceConfigurationCloneForm().getForm().markInvalid(json.errors);
                }
            },
            callback: function () {
                pageMainContent.setLoading(false);
            }
        });
    },

    createEditDeviceConfiguration: function (btn) {
        var me = this,
            editForm = me.getDeviceConfigurationEdit(),
            values = me.getDeviceConfigurationEditForm().getValues(),
            addressableRadioGroup = me.getAddressableRadioGroup(),
            gatewayRadioGroup = this.getGatewayRadioGroup(),
            dataLoggerRadioGroup = this.getDataLoggerRadioGroup(),
            record;


        editForm.setLoading(true);

        if (btn.action === 'createDeviceConfiguration') {
            record = Ext.create(Mdc.model.DeviceConfiguration);
            if (addressableRadioGroup.isDisabled()) {
                values.isDirectlyAddressable = false;
            }
            if (gatewayRadioGroup.isDisabled()) {
                values.canBeGateway = false;
            }
            if (dataLoggerRadioGroup.isDisabled()) {
                values.dataloggerEnabled = false;
            }
        } else {
            record = this.getDeviceConfigurationEditForm().getRecord();
        }

        if (record) {
            me.hideErrorPanel();
            record.set(values);
            if (!record.get('canBeGateway')) {
                record.set('gatewayType', 'NONE')
            }
            record.getProxy().setExtraParam('deviceType', this.deviceTypeId);
            record.save({
                backUrl: me.getDeviceConfigurationEdit().returnLink,
                success: function (record) {
                    window.location.href = me.getDeviceConfigurationEdit().returnLink;
                    me.getApplication().fireEvent('acknowledge',
                        btn.action === 'createDeviceConfiguration'
                        ? Uni.I18n.translate('deviceconfiguration.acknowledgment.added', 'MDC', 'Device configuration added')
                        : Uni.I18n.translate('deviceconfiguration.acknowledgment.saved', 'MDC', 'Device configuration saved')
                    );
                    editForm.setLoading(false);
                },
                failure: function (record, operation) {
                    me.showErrorPanel();
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceConfigurationEditForm().getForm().markInvalid(json.errors);
                    }
                    editForm.setLoading(false);
                }
            });
        }
    },

    showErrorPanel: function () {
        this.getDeviceConfigurationEdit().down('#mdc-device-config-form-errors').show();
    },

    hideErrorPanel: function () {
        this.getDeviceConfigurationEdit().down('#mdc-device-config-form-errors').hide();
    },

    showDeviceConfigurationLogbooksView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            store = Ext.data.StoreManager.lookup('LogbookConfigurations');
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', false);
        store.getProxy().setExtraParam('deviceConfiguration', deviceConfigurationId);
        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.load(
            {
                callback: function () {
                    var self = this,
                        widget = Ext.widget('device-configuration-logbooks', {
                            deviceTypeId: deviceTypeId,
                            deviceConfigurationId: deviceConfigurationId
                        });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.setLoading(true);
                    model.load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                    widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfiguration.get('name'));
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                    var grid = Ext.ComponentQuery.query('device-configuration-logbooks grid')[0],
                        gridView = grid.getView(),
                        selectionModel = gridView.getSelectionModel();
                    if (self.getCount() > 0) {
                        selectionModel.select(0);
                        grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
                    }
                }
            }
        );
    },

    showAddDeviceConfigurationLogbooksView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            store = Ext.data.StoreManager.lookup('LogbookConfigurations'),
            widget = Ext.widget('add-logbook-configurations', {
                deviceTypeId: deviceTypeId,
                deviceConfigurationId: deviceConfigurationId
            });
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', true);
        store.getProxy().setExtraParam('deviceConfiguration', deviceConfigurationId);
        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.load(
            {
                callback: function () {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    if (!this.getCount()) {
                        widget.down('button[action=add]').disable();
                    }
                    widget.setLoading(true);
                    model.load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                    var numberOfLogbooksLabel = Ext.ComponentQuery.query('add-logbook-configurations #logbook-count')[0],
                        grid = Ext.ComponentQuery.query('add-logbook-configurations grid')[0];
                    numberOfLogbooksLabel.setText(
                        Uni.I18n.translatePlural('general.nrOfLogbookConfigurations.selected', 0, 'MDC',
                            'No logbook configurations selected', '{0} logbook configuration selected', '{0} logbook configurations selected')
                    );
                }
            }
        );
    },

    showEditDeviceConfigurationLogbooksView: function (deviceTypeId, deviceConfigurationId, logbookConfigurationId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            store = Ext.data.StoreManager.lookup('LogbookConfigurations');
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', false);
        store.getProxy().setExtraParam('deviceConfiguration', deviceConfigurationId);
        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.load(
            {
                callback: function (logbookConfigurations) {
                    var widget = Ext.widget('edit-logbook-configuration', {
                        deviceTypeId: deviceTypeId,
                        deviceConfigurationId: deviceConfigurationId,
                        logbookConfigurationId: logbookConfigurationId
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.setLoading(true);
                    var form = me.getEditLogbookConfiguration().down('form');
                    Ext.Array.each(logbookConfigurations, function (logbookConfiguration) {
                        if (logbookConfiguration.get('id') == logbookConfigurationId) {
                            me.originalObisCode = logbookConfiguration.get('obisCode');
                            form.loadRecord(logbookConfiguration);
                            return false; // break out of the loop
                        }
                    });
                    model.load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                    me.getApplication().fireEvent('loadLogbooksConfiguration', form.down('[name=name]'));
                                    widget.down('#editLogbookPanel').setTitle(
                                        Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [form.down('[name=name]').getValue()]));
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                }
            }
        );
    },
    showChangeDeviceConfigurationView: function (deviceId) {
        var me = this, viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();

        Ext.ModelManager.getModel('Mdc.model.Device').load(deviceId, {
            success: function (device) {
                var widget = Ext.widget('changeDeviceConfigurationView', {device: device}),
                    form = me.getChangeDeviceConfigurationForm(),
                    deviceConfigurationsStore = me.getNewDeviceConfigurationCombo().getStore(),
                    currentDeviceConfigurationId = device.get('deviceConfigurationId');

                form.loadRecord(device);
                deviceConfigurationsStore.clearFilter(false);
                deviceConfigurationsStore.addFilter([
                    function(record) {
                        return record.get('id')!==currentDeviceConfigurationId && !record.get('dataloggerEnabled');
                    }
                ]);
                deviceConfigurationsStore.getProxy().pageParam = undefined;
                deviceConfigurationsStore.getProxy().startParam = undefined;
                deviceConfigurationsStore.getProxy().limitParam = undefined;
                deviceConfigurationsStore.getProxy().setUrl({deviceType: device.get('deviceTypeId')});
                deviceConfigurationsStore.getProxy().setExtraParam('filter', Ext.encode([
                    {
                        property: 'active',
                        value: true
                    }
                ]));

                deviceConfigurationsStore.load(function () {
                    me.getNoDeviceConfigurationsLabel().setVisible(!this.count());
                    me.getNewDeviceConfigurationCombo().setVisible(this.count());
                    if (this.count() === 1) {
                        me.getNewDeviceConfigurationCombo().setValue(this.getAt(0).get('id'));
                        me.getSaveChangeDeviceConfigurationBtn().setDisabled(false);
                    }
                });

                widget.down('#device-configuration-name').setValue(device.get('deviceConfigurationName'));

                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    saveChangeDeviceConfiguration: function () {
        var me = this, canSolveConflictingMappings = Mdc.privileges.DeviceType.canAdministrate(),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            device = me.getChangeDeviceConfigurationView().device,
            router = me.getController('Uni.controller.history.Router');

        device.set('deviceConfigurationId', me.getNewDeviceConfigurationCombo().getValue());
        me.getChangeDeviceConfigurationFormErrors().hide();
        me.getChangeDeviceConfigurationForm().getForm().clearInvalid();
        viewport.setLoading(true);

        Ext.Ajax.request({
            url: '/api/ddr/devices/' + device.get('id'),
            method: 'PUT',
            jsonData: device.data,
            success: function () {
                Ext.state.Manager.clear('deviceConfigState');
                router.getRoute('devices/device').forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('device.changeDeviceConfiguration.changed', 'MDC', 'Device configuration changed'));
            },
            failure: function (response, request) {
                if (response.status == 400) {
                    var json = Ext.decode(response.responseText, true);
                    if (json && json.changeDeviceConfigConflict)  {
                            var title = (canSolveConflictingMappings ? Uni.I18n.translate('device.changeDeviceConfiguration.failedToChangeDeviceConfiguration', 'MDC', 'Failed to change device configuration')
                                : Uni.I18n.translate('device.changeDeviceConfiguration.unableToChangeDeviceConfiguration', 'MDC', 'Unable to change device configuration'));

                            var errorWindow = Ext.create('Ext.window.MessageBox', {
                                buttonAlign: canSolveConflictingMappings ? 'right' : 'left',
                                buttons: [
                                    {
                                        xtype: 'button',
                                        text: (canSolveConflictingMappings ? Uni.I18n.translate('general.close', 'MDC', 'Close') : Uni.I18n.translate('general.finish', 'MDC', 'Finish')),
                                        action: 'close',
                                        name: 'close',
                                        ui: 'remove',
                                        style: {
                                            marginLeft: (canSolveConflictingMappings ? '0px' : '50px')
                                        },
                                        handler: function () {
                                            errorWindow.close();
                                        }
                                    }
                                ]
                            });

                            errorWindow.down('displayfield').htmlEncode = false;

                            var solveConflictsLink = router.getRoute('administration/devicetypes/view/conflictmappings/edit').buildUrl({
                                    deviceTypeId: device.get('deviceTypeId'),
                                    id: json.changeDeviceConfigConflict
                                }),
                                message = canSolveConflictingMappings
                                    ? Uni.I18n.translate('device.changeDeviceConfiguration.cannotbeChanged', 'MDC', "The configuration of device '{0}' cannot be changed to '{1}' due to unsolved conflicts. <br/><br/>", [device.get('name'), me.getNewDeviceConfigurationCombo().getRawValue()])
                                + Uni.I18n.translate('device.changeDeviceConfiguration.SolveTheConflictsBeforeYouRetry', 'MDC', '<a href="{0}">Solve the conflicts</a> before you retry.', solveConflictsLink)
                                    : Uni.I18n.translate('device.changeDeviceConfiguration.noRightsToSolveTheConflicts', 'MDC', 'You cannot solve the conflicts in conflicting mappings on device type because you do not have the privileges. Contact the administrator.');

                            router.addListener('routeChangeStart', function () {
                                errorWindow.close();
                            }, this, {single: true});

                            me.getController('Mdc.controller.setup.DeviceConflictingMapping').returnInfo = {
                                from: 'changeDeviceConfiguration',
                                id: device.get('name')
                            };

                            errorWindow.show(
                                {
                                    title: title,
                                    msg: message,
                                    ui: 'message-error',
                                    icon: 'icon-warning2'
                                }
                            );

                        }
                    if (json && json.errors) {
                        me.getChangeDeviceConfigurationFormErrors().setText(json.errors[0].msg);
                        me.getChangeDeviceConfigurationFormErrors().show();
                    }
                }
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    cancelChangeDeviceConfiguration: function () {
        var router = this.getController('Uni.controller.history.Router');
        Ext.state.Manager.clear('deviceConfigState');
        router.getRoute('devices/device').forward();
    },

    onObisCodeChange: function(obisCodeField, newValue) {
        var me = this;
        me.getRestoreObisCodeBtn().setDisabled(newValue === me.originalObisCode);
        me.getRestoreObisCodeBtn().setTooltip(
            newValue === me.originalObisCode
                ? null
                : Uni.I18n.translate('general.obisCode.reset.tooltip3', 'MDC', 'Reset to {0}, the OBIS code of the logbook type', me.originalObisCode)
        );
    },

    onRestoreObisCodeBtnClicked: function() {
        var me = this;
        me.getObisCodeField().setValue(me.originalObisCode);
        me.onObisCodeChange(me.getObisCodeField(), me.originalObisCode);
    }

});
