/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceTypes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Uni.util.When'
    ],

    views: [
        'setup.devicetype.DeviceTypesSetup',
        'setup.devicetype.DeviceTypesGrid',
        'setup.devicetype.DeviceTypePreview',
        'setup.devicetype.DeviceTypeDetail',
        'setup.devicetype.DeviceTypeEdit',
        'setup.devicetype.DeviceTypeLogbooks',
        'setup.devicetype.AddLogbookTypes',
        'setup.devicetype.AddEditDeviceIcon',
        'setup.devicetype.DeviceTypeCustomAttributesEdit',
        'Uni.view.error.NotFound'
    ],

    stores: [
        'DeviceTypes',
        'DeviceCommunicationProtocols',
        'LogbookTypesOfDeviceType',
        'AvailableLogbookTypes',
        'Mdc.store.DeviceLifeCycles',
        'Mdc.store.DeviceTypePurposes',
        'Mdc.store.DeviceTypeCustomAttributesSets',
        'Mdc.store.CommunicationSchedules'
    ],

    models: [
        'Mdc.model.DeviceType',
        'Mdc.model.AttributeSetOnDeviceType'
    ],

    refs: [
        {ref: 'deviceTypeGrid', selector: '#devicetypegrid'},
        {ref: 'deviceTypePreviewForm', selector: '#deviceTypePreviewForm'},
        {ref: 'deviceTypePreview', selector: '#deviceTypePreview'},
        {ref: 'deviceTypePreviewTitle', selector: '#deviceTypePreviewTitle'},
        {ref: 'deviceTypeEditView', selector: '#deviceTypeEdit'},
        {ref: 'deviceTypeEditForm', selector: '#deviceTypeEditForm'},
        {ref: 'deviceTypeRegisterLink', selector: '#deviceTypeRegistersLink'},
        {ref: 'deviceTypeDetailRegistersLink', selector: '#deviceTypeDetailRegistersLink'},
        {ref: 'deviceTypeLogBookLink', selector: '#deviceTypeLogBooksLink'},
        {ref: 'deviceTypeDetailLogBookLink', selector: '#deviceTypeDetailLogBooksLink'},
        {ref: 'deviceConfigurationsLink', selector: '#deviceConfigurationsLink'},
        {ref: 'deviceConfigurationsDetailLink', selector: '#deviceConfigurationsDetailLink'},
        {ref: 'deviceTypeLoadProfilesLink', selector: '#deviceTypeLoadProfilesLink'},
        {ref: 'deviceTypeDetailLoadProfilesLink', selector: '#deviceTypeDetailLoadProfilesLink'},
        {ref: 'deviceTypeDetailForm', selector: '#deviceTypeDetailForm'},
        {ref: 'editDeviceTypeNameField', selector: '#editDeviceTypeNameField'},
        {ref: 'protocolCombo', selector: '#communicationProtocolComboBox'},
        {ref: 'sharedCombo', selector: '#sharedScheduleComboBox'},
        {ref: 'deviceTypeLogbookPanel', selector: '#deviceTypeLogbookPanel'},
        {ref: 'addLogbookPanel', selector: '#addLogbookPanel'},
        {ref: 'deviceLifeCycleLink', selector: '#device-life-cycle-link'},
        {ref: 'deviceLifeCycleCombo', selector: '#no-device-life-cycles'},
        {ref: 'deviceTypeDetailLifeCycleLink', selector: 'deviceTypeDetail #details-device-life-cycle-link'},
        {ref: 'deviceTypeDetailOverviewLink', selector: 'deviceTypeDetail deviceTypeSideMenu #overviewLink'},
        {ref: 'deviceTypeDetailConflictingMappingLink', selector: 'deviceTypeDetail deviceTypeSideMenu #conflictingMappingLink'},
        {ref: 'deviceTypeDetailActionMenu', selector: 'deviceTypeDetail device-type-action-menu'},
        {ref: 'addEditDeviceIcon', selector: 'add-edit-device-icon'},
        {ref: 'deviceIconDisplayField', selector: 'deviceTypeDetail #deviceIconDisplayField'},
        {ref: 'noDeviceIconDisplayField', selector: 'deviceTypeDetail #noDeviceIconDisplayField'},
        {
            ref: 'editCustomAttributePropertyForm',
            selector: '#device-type-custom-attributes-edit-id #device-type-custom-attributes-property-form'
        },
        {ref: 'deviceTypeCustomAttributesEditView', selector: '#device-type-custom-attributes-edit-id '}
    ],

    init: function () {
        this.control({
            '#devicetypegrid': {
                selectionchange: this.previewDeviceType
            },
            '#devicetypegrid actioncolumn': {
                editDeviceType: this.editDeviceTypeHistory,
                deleteDeviceType: this.deleteDeviceType
            },
            '#deviceTypeSetup button[action = createDeviceType]': {
                click: this.createDeviceTypeHistory
            },
            '#deviceTypePreview menuitem[action=editDeviceType]': {
                click: this.editDeviceTypeHistoryFromPreview
            },
            '#deviceTypePreview menuitem[action=deleteDeviceType]': {
                click: this.deleteDeviceTypeFromPreview
            },
            '#createEditButton[action=createDeviceType]': {
                click: this.createDeviceType
            },
            '#createEditButton[action=editDeviceType]': {
                click: this.editDeviceType
            },
            '#deviceTypeDetail menuitem[action=deleteDeviceType]': {
                click: this.deleteDeviceTypeFromDetails
            },
            '#deviceTypeDetail menuitem[action=editDeviceType]': {
                click: this.editDeviceTypeFromDetails
            },
            '#deviceTypeEdit #communicationProtocolComboBox': {
                select: this.proposeDeviceTypeName
            },
            //#checkComment
            '#deviceTypeEdit #sharedScheduleComboBox': {
                select: this.proposeDeviceTypeName
            },
            '#deviceTypeEdit #mdc-deviceTypeEdit-typeComboBox': {
                change: this.onDeviceTypePurposeChange
            },
            'device-type-action-menu': {
                click: this.chooseAction
            },
            '#addDeviceIconButton': {
                click: this.saveDeviceIcon
            },
            '#deviceIconFileField': {
                change: this.onDeviceIconFieldChange
            },
            '#device-type-custom-attributes-edit-id #device-type-custom-attributes-save-btn': {
                click: this.saveCustomAttributes
            },
            '#device-type-custom-attributes-edit-id #device-type-custom-attributes-restore-default-btn': {
                click: this.restoreDefaultCustomAttributes
            },
            '#device-type-custom-attributes-edit-id #device-type-custom-attributes-cancel-btn': {
                click: this.goToAttributesLandingFromCas
            },

        });
    },

    previewDeviceType: function (grid, record) {
        var deviceTypes = this.getDeviceTypeGrid().getSelectionModel().getSelection(),
            deviceTypeId,
            registerLink,
            logBookLink,
            loadProfilesLink,
            deviceConfigurationsLink,
            deviceLifeCycleLink;

        if (deviceTypes.length == 1) {
            Ext.suspendLayouts();
            deviceTypeId = deviceTypes[0].get('id');
            registerLink = this.getDeviceTypeRegisterLink();
            logBookLink = this.getDeviceTypeLogBookLink();
            loadProfilesLink = this.getDeviceTypeLoadProfilesLink();
            deviceConfigurationsLink = this.getDeviceConfigurationsLink();
            deviceLifeCycleLink = this.getDeviceLifeCycleLink();

            deviceLifeCycleLink.setHref('#/administration/devicelifecycles/' + encodeURIComponent(record[0].get('deviceLifeCycleId')));
            deviceLifeCycleLink.setText(Ext.String.htmlEncode(record[0].get('deviceLifeCycleName')));

            registerLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/registertypes');
            registerLink.setText(
                Uni.I18n.translatePlural('devicetype.registers', deviceTypes[0].get('registerCount'), 'MDC', 'No register types', '{0} register type', '{0} register types')
            );

            if (deviceTypes[0].get('deviceTypePurpose') === 'DATALOGGER_SLAVE' || deviceTypes[0].get('deviceTypePurpose') === 'MULTI_ELEMENT_SLAVE') {
                logBookLink.hide();
            } else {
                logBookLink.show();
                logBookLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/logbooktypes');
                logBookLink.setText(
                    Uni.I18n.translatePlural('devicetype.logbooks', deviceTypes[0].get('logBookCount'), 'MDC', 'No logbook types', '{0} logbook type', '{0} logbook types')
                );
            }

            loadProfilesLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/loadprofiles');
            loadProfilesLink.setText(
                Uni.I18n.translatePlural('devicetype.loadprofiles', deviceTypes[0].get('loadProfileCount'), 'MDC', 'No load profile types', '{0} load profile type', '{0} load profile types')
            );

            deviceConfigurationsLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/deviceconfigurations');
            deviceConfigurationsLink.setText(
                Uni.I18n.translatePlural('devicetype.deviceconfigurations', deviceTypes[0].get('deviceConfigurationCount'), 'MDC', 'No device configurations', '{0} device configuration', '{0} device configurations')
            );

            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);

            if (this.getDeviceTypePreview().down('device-type-action-menu'))
                this.getDeviceTypePreview().down('device-type-action-menu').record = deviceTypes[0];

            this.getDeviceTypePreview().setTitle(Ext.String.htmlEncode(deviceTypes[0].get('name')));
            Ext.resumeLayouts(true);
        }
    },

    showDeviceTypeDetailsView: function (deviceType) {
        var me = this,
            deviceTypePurposesStore = me.getStore('Mdc.store.DeviceTypePurposes'),
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            router = me.getController('Uni.controller.history.Router');

        deviceTypePurposesStore.load(function () {
            var widget = Ext.widget('deviceTypeDetail', {
                deviceTypeId: deviceType,
                purposeStore: deviceTypePurposesStore,
                router: router
            });

            me.getApplication().fireEvent('changecontentevent', widget);
            model.load(deviceType, {
                success: function (deviceType) {
                    var deviceTypeId = deviceType.get('id'),
                        registersLink = me.getDeviceTypeDetailRegistersLink(),
                        logBookLink = me.getDeviceTypeDetailLogBookLink(),
                        loadProfilesLink = me.getDeviceTypeDetailLoadProfilesLink(),
                        deviceConfigurationsLink = me.getDeviceConfigurationsDetailLink(),
                        deviceLifeCycleLink = widget.down('#details-device-life-cycle-link'),
                        actionMenu = widget.down('device-type-action-menu');

                    Ext.suspendLayouts();
                    if (widget.rendered) {
                        widget.down('deviceTypeSideMenu').setDeviceTypeTitle(deviceType.get('name'));
                        widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                            Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
                        );

                        deviceLifeCycleLink.setHref('#/administration/devicelifecycles/' + encodeURIComponent(deviceType.get('deviceLifeCycleId')));
                        deviceLifeCycleLink.setText(Ext.String.htmlEncode(deviceType.get('deviceLifeCycleName')));

                        registersLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/registertypes');
                        registersLink.setText(
                            Uni.I18n.translatePlural('devicetype.registers', deviceType.get('registerCount'), 'MDC',
                                'No register types', '{0} register type', '{0} register types')
                        );

                        if (deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE' || deviceType.get('deviceTypePurpose') === 'MULTI_ELEMENT_SLAVE') {
                            logBookLink.hide();
                        } else {
                            logBookLink.show();
                            logBookLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/logbooktypes');
                            logBookLink.setText(
                                Uni.I18n.translatePlural('devicetype.logbooks', deviceType.get('logBookCount'), 'MDC',
                                    'No logbook types', '{0} logbook type', '{0} logbook types')
                            );
                        }

                        loadProfilesLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/loadprofiles');
                        loadProfilesLink.setText(
                            Uni.I18n.translatePlural('devicetype.loadprofiles', deviceType.get('loadProfileCount'), 'MDC',
                                'No load profile types', '{0} load profile type', '{0} load profile types')
                        );

                        deviceConfigurationsLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/deviceconfigurations');
                        deviceConfigurationsLink.setText(
                            Uni.I18n.translatePlural('devicetype.deviceconfigurations', deviceType.get('deviceConfigurationCount'), 'MDC',
                                'No device configurations', '{0} device configuration', '{0} device configurations')
                        );

                        widget.down('form').loadRecord(deviceType);
                        if (actionMenu) {
                            actionMenu.record = deviceType;
                        }
                        me.updateDeviceTypeIcon(deviceType.get('deviceIcon'));
                    }

                    Ext.resumeLayouts(true);

                    me.getApplication().fireEvent('loadDeviceType', deviceType);

                    /* Load custom property sets  */
                    var customAttributesStore = me.getStore('Mdc.store.DeviceTypeCustomAttributesSets');
                    customAttributesStore.getProxy().setUrl(deviceTypeId);

                    customAttributesStore.load(function () {
                        widget.down('#custom-attribute-sets-placeholder-form-id').loadStore(this);
                    });
                }
            });
        });
    },

    createDeviceTypeHistory: function () {
        location.href = '#/administration/devicetypes/add';
    },

    editDeviceTypeHistory: function (record) {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(record.get('id')) + '/edit';
    },

    editDeviceTypeHistoryFromPreview: function () {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.getDeviceTypeGrid().getSelectionModel().getSelection()[0].get("id")) + '/edit';
    },

    deleteDeviceType: function (deviceTypeToDelete) {
        var me = this,
            msg = Uni.I18n.translate('deviceType.deleteDeviceTypeWithConfig', 'MDC', 'The device type and its configurations will no longer be available.');

        if (deviceTypeToDelete.get('deviceConfigurationCount') === 0) {
            msg = Uni.I18n.translate('deviceType.deleteDeviceType', 'MDC', 'The device type will no longer be available.');
        }

        Ext.create('Uni.view.window.Confirmation').show({
            msg: msg,
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", [deviceTypeToDelete.get('name')]),
            config: {
                deviceTypeToDelete: deviceTypeToDelete,
                me: me
            },
            fn: me.removeDeviceType
        });
    },

    deleteDeviceTypeFromPreview: function () {
        this.deleteDeviceType(this.getDeviceTypeGrid().getSelectionModel().getSelection()[0]);
    },

    deleteDeviceTypeFromDetails: function () {
        var deviceTypeToDelete = this.getDeviceTypeDetailForm().getRecord();
        this.deleteDeviceType(deviceTypeToDelete);
    },

    showDeviceTypeEditView: function (deviceTypeId) {
        var me = this,
            protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols'),
            configurationStore = me.getStore('Mdc.store.DeviceConfigurations'),
            lifeCycleStore = me.getStore('Mdc.store.DeviceLifeCycles'),
            deviceTypePurposesStore = me.getStore('Mdc.store.DeviceTypePurposes'),
            deviceSharedScheduleStore = me.getStore('Mdc.store.CommunicationSchedules'),

            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('deviceTypeEdit', {
                edit: true,
                deviceCommunicationProtocols: protocolStore,
                deviceTypePurposes: deviceTypePurposesStore,
                deviceSharedSchedules:deviceSharedScheduleStore,
                cancelLink: router.queryParams.fromDetails
                    ? router.getRoute('administration/devicetypes/view').buildUrl()
                    : router.getRoute('administration/devicetypes').buildUrl()
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        configurationStore.getProxy().url = configurationStore.getProxy().baseUrl.replace('{deviceType}', deviceTypeId);
        var when = new Uni.util.When();
        when.when([
            {
                action: Ext.ModelManager.getModel('Mdc.model.DeviceType').load,
                context: Ext.ModelManager.getModel('Mdc.model.DeviceType'),
                args: [deviceTypeId]
            },
            {action: protocolStore.load, context: protocolStore, simple: true},
            {action: lifeCycleStore.load, context: lifeCycleStore, simple: true},
            {action: deviceTypePurposesStore.load, context: deviceTypePurposesStore, simple: true},
            {action: configurationStore.load, context: configurationStore, simple: true},
            {action: deviceSharedScheduleStore.load, context: deviceSharedScheduleStore, simple: true}
        ]).then(
            {
                success: function (results) {
                    if (widget.rendered) {
                        var deviceType = results[0][0];
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        Ext.suspendLayouts();
                        me.getDeviceTypeEditForm().loadRecord(deviceType);
                        widget.down('#mdc-deviceTypeEdit-typeComboBox').setValue(deviceType.get('deviceTypePurpose'));
                        me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", deviceType.get('name')));
                        me.modifyEditView(widget, configurationStore);
                        Ext.resumeLayouts(true);
                        widget.setLoading(false);
                    }
                },
                failure: function () {
                    if (widget.rendered) {
                        Ext.suspendLayouts();
                        me.getDeviceTypeEditForm().loadRecord(deviceType);
                        me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", deviceType.get('name')));
                        Ext.resumeLayouts(true);
                        widget.setLoading(false);
                    }
                }
            }
        );

    },

    showDeviceTypeCustomAttributesEditView: function (deviceTypeId, customAttributeSetId) {

            var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            model = Ext.ModelManager.getModel('Mdc.model.AttributeSetOnDeviceType'),
            widget;

            model.getProxy().setUrl(deviceTypeId);
            viewport.setLoading();

            widget = Ext.widget('device-type-custom-attributes-edit', {deviceTypeId: deviceTypeId});

            me.getApplication().fireEvent('changecontentevent', widget);

            model.load(customAttributeSetId, {
                success: function (record) {
                    me.getApplication().fireEvent('loadCustomAttributeSetOnDeviceType', record);
                    widget.down('#custom-attribute-set-edit-panel').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'", [record.get('name')]));
                    widget.down('#device-type-custom-attributes-property-form').loadRecord(record);
                },
                callback: function () {
                    viewport.setLoading(false);
                }
            })
    },

    modifyEditView: function (widget, store) {
        var me = this,
            form = widget.down('#deviceTypeEditForm'),
            infoPanel = widget.down('#info-panel'),
            typeField = form.down('#mdc-deviceTypeEdit-typeComboBox'),
            commProtocolField = form.down('#communicationProtocolComboBox'),
            lifeCycleField = form.down('#device-life-cycle-field'),
            sharedScheduleField = form.down('#sharedScheduleComboBox'),

            activeConfig = store.find('active', true);
        if (store.getCount() > 0) {
            if (activeConfig < 0) {
                typeField.disable();
                commProtocolField.disable();
                sharedScheduleField.enable();
                lifeCycleField.enable();
                infoPanel.show();
                infoPanel.setText(Uni.I18n.translate('deviceType.edit.notificationMsg3', 'MDC', 'This device type has one or more device configurations. Only fields name, device life cycle and shared schedules are editable'));
            } else {
                typeField.disable();
                commProtocolField.disable();
                lifeCycleField.disable();
                infoPanel.show();
                infoPanel.setText(Uni.I18n.translate('deviceType.edit.notificationMsg4', 'MDC', 'This device type has one or more active device configurations. Only the name field and shared schedule field are editable'));
            }
        } else {
            typeField.enable();
            commProtocolField.enable();
            lifeCycleField.enable();
            sharedScheduleField.enable();
        }
    },

    showDeviceTypeCreateView: function () {
        var me = this,
            protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols'),
            deviceLifeCyclesStore = me.getStore('Mdc.store.DeviceLifeCycles'),
            deviceTypePurposesStore = me.getStore('Mdc.store.DeviceTypePurposes'),
            deviceSharedScheduleStore = me.getStore('Mdc.store.CommunicationSchedules'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('deviceTypeEdit', {
                edit: false,
                cancelLink: router.getRoute('administration/devicetypes').buildUrl(),
                deviceCommunicationProtocols: protocolStore,
                deviceTypePurposes: deviceTypePurposesStore,
                deviceSharedSchedules:deviceSharedScheduleStore
            }),
            counter = 0,
            onAllStoresLoaded = function () {
                counter += 1;
                if (counter === 3) {
                    deviceLifeCyclesStore.load(function () {
                        if (deviceLifeCyclesStore.getCount() == 0) {
                            me.getDeviceLifeCycleCombo().hide();
                            widget.down('#no-device-life-cycles').show();
                        }
                        me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.addDeviceType', 'MDC', 'Add device type'));
                        widget.down('#mdc-deviceTypeEdit-typeComboBox').setValue('REGULAR');
                        widget.setLoading(false);
                    });
                }
            };

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        protocolStore.load(onAllStoresLoaded);
        deviceTypePurposesStore.load(onAllStoresLoaded);
        deviceSharedScheduleStore.load(onAllStoresLoaded);
    },

    createDeviceType: function (image) {
        var me = this,
            record = Ext.create(Mdc.model.DeviceType),
            editForm = me.getDeviceTypeEditForm(),
            values = editForm.getValues(),
            form = editForm.getForm();

        form.clearInvalid();
        me.hideErrorPanel();
        editForm.down('#device-life-cycle-field').unsetActiveError();
        editForm.down('#communicationProtocolComboBox').unsetActiveError();
        if (record) {
            record.set(values);
            record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceType.acknowledgment.added', 'MDC', 'Device type added'));
                    location.href = '#/administration/devicetypes/' + encodeURIComponent(record.get('id'));
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        form.markInvalid(json.errors);
                        Ext.Array.each(json.errors, function (item) {
                            if (item.id === 'deviceLifeCycle') {
                                editForm.down('#device-life-cycle-field').setActiveError(item.msg);
                            } else if (item.id === 'deviceProtocolPluggableClassId') {
                                editForm.down('#communicationProtocolComboBox').setActiveError(item.msg);
                            }
                        });
                        me.showErrorPanel();
                    }
                }
            });
        }
    },

    editDeviceType: function () {
        var me = this,
            record = me.getDeviceTypeEditForm().getRecord(),
            values = me.getDeviceTypeEditForm().getValues(),
            router = me.getController('Uni.controller.history.Router'),
            page = me.getDeviceTypeEditView(),
            editForm = me.getDeviceTypeEditForm(),
            baseForm = editForm.getForm();

        me.hideErrorPanel();
        if (record) {
            page.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));
            record.set(values);
            record.save({
                backUrl: router.queryParams.fromDetails
                    ? router.getRoute('administration/devicetypes/view').buildUrl()
                    : router.getRoute('administration/devicetypes').buildUrl(),
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceType.acknowlegment.saved', 'MDC', 'Device type saved'));
                    router.queryParams.fromDetails ? router.getRoute('administration/devicetypes/view').forward() : router.getRoute('administration/devicetypes').forward();
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        baseForm.markInvalid(json.errors);
                        Ext.Array.each(json.errors, function (item) {
                            if (item.id === 'deviceLifeCycle') {
                                editForm.down('#device-life-cycle-field').setActiveError(item.msg);
                            } else if (item.id === 'deviceProtocolPluggableClassId') {
                                editForm.down('#communicationProtocolComboBox').setActiveError(item.msg);
                            }
                        });
                        me.showErrorPanel();
                    }
                },
                callback: function () {
                    page.setLoading(false);
                }
            });

        }
    },

    editDeviceTypeFromDetails: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('administration/devicetypes/view/edit').forward(router.arguments, {fromDetails: true});
    },

    proposeDeviceTypeName: function (combo, newValue) {
        if (!this.getDeviceTypeEditView().isEdit() && Ext.isEmpty(this.getEditDeviceTypeNameField().getValue()) && !Ext.isEmpty(newValue[0])) {
            this.getEditDeviceTypeNameField().setValue(newValue[0].get('name'));
        }
    },

    onDeviceTypePurposeChange: function (combo, newValue) {
        switch (newValue) {
            case 'REGULAR':
                this.getProtocolCombo().show();
                break;
            case 'DATALOGGER_SLAVE':
                this.getProtocolCombo().hide();
                break;
            case 'MULTI_ELEMENT_SLAVE':
                this.getProtocolCombo().hide();
                break;
        }
    },

    removeDeviceType: function (btn, text, opt) {
        if (btn === 'confirm') {
            var deviceTypeToDelete = opt.config.deviceTypeToDelete;
            var me = opt.config.me;
            var router = me.getController('Uni.controller.history.Router');

            deviceTypeToDelete.destroy({
                success: function () {
                    var grid = me.getDeviceTypeGrid(),
                        gridPagingToolbar;
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceType.acknowledgment.removed', 'MDC', 'Device type removed'));
                    if (router.currentRoute === 'administration/devicetypes/view') {
                        router.getRoute('administration/devicetypes').forward();
                    } else if (grid) {
                        gridPagingToolbar = grid.down('pagingtoolbartop');
                        gridPagingToolbar.isFullTotalCount = false;
                        gridPagingToolbar.totalCount = -1;
                        grid.down('pagingtoolbarbottom').totalCount--;
                        grid.getStore().loadPage(1);
                    }
                }
            });

        }
    },

    showDeviceTypeLogbookTypesView: function (deviceTypeId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            store = Ext.data.StoreManager.lookup('LogbookTypesOfDeviceType');
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', false);

        var widget = Ext.widget('device-type-logbooks', {deviceTypeId: deviceTypeId});
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        model.load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);

                if (widget.down('deviceTypeSideMenu')) {
                    widget.down('deviceTypeSideMenu').setDeviceTypeTitle(deviceType.get('name'));
                    widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                        Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
                    );
                }

                me.getDeviceTypeLogbookPanel().setTitle(Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'));
                widget.setLoading(false);
            }
        });
    },

    showAddLogbookTypesView: function (deviceTypeId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            store = Ext.data.StoreManager.lookup('AvailableLogbookTypes'),
            widget = Ext.widget('add-logbook-types', {
                router: me.getController('Uni.controller.history.Router'),
                deviceTypeId: deviceTypeId
            });

        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', true);
        store.load();
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        model.load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                me.getAddLogbookPanel().setTitle(Uni.I18n.translate('general.addLogbookTypes', 'MDC', 'Add logbook types'));
                widget.setLoading(false);
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        switch (item.action) {
            case 'changeDeviceLifeCycle':
                router.arguments.deviceTypeId = menu.record.getId();
                route = 'administration/devicetypes/view/change';
                break;
            case 'addDeviceIcon':
                router.arguments.deviceTypeId = menu.record.getId();
                me.triggeredFromGrid = !Ext.isEmpty(me.getDeviceTypeGrid());
                route = 'administration/devicetypes/view/adddeviceicon';
                break;
            case 'editDeviceIcon':
                router.arguments.deviceTypeId = menu.record.getId();
                me.triggeredFromGrid = !Ext.isEmpty(me.getDeviceTypeGrid());
                route = 'administration/devicetypes/view/editdeviceicon';
                break;
            case 'removeDeviceIcon':
                me.removeDeviceIcon(menu.record);
                return;
                break;
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments);
    },

    showAddDeviceIconView: function (deviceTypeId) {
        this.showChangeDeviceIconView(deviceTypeId, false);
    },

    showEditDeviceIconView: function (deviceTypeId) {
        this.showChangeDeviceIconView(deviceTypeId, true);
    },

    showChangeDeviceIconView: function (deviceTypeId, isEdit) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            router = me.getController('Uni.controller.history.Router'),
            widget,
            route = me.triggeredFromGrid ? 'administration/devicetypes' : 'administration/devicetypes/view';


        model.load(deviceTypeId, {
            success: function (deviceType) {
                if ((isEdit && Ext.isEmpty(deviceType.get('deviceIcon')) || (!isEdit && !Ext.isEmpty(deviceType.get('deviceIcon'))))) {
                    crossroads.parse("/error/notfound");
                } else {
                    widget = Ext.widget('add-edit-device-icon', {
                        router: me.getController('Uni.controller.history.Router'),
                        deviceTypeId: deviceTypeId,
                        version: deviceType.get('version'),
                        isEdit: isEdit,
                        deviceTypeName: deviceType.get('name'),
                        cancelLink: router.getRoute(route).buildUrl()
                    });
                    me.getApplication().fireEvent('loadDeviceType', deviceType);
                    me.getApplication().fireEvent('changecontentevent', widget);
                    if (!Ext.isEmpty(deviceType.get('deviceIcon'))) {
                        me.showDeviceIconPreview('data:image;base64,' + deviceType.get('deviceIcon'));
                    }
                }
            }
        });
    },

    onDeviceIconFieldChange: function () {
        var me = this,
            addEditDeviceIconPage = me.getAddEditDeviceIcon(),
            file = addEditDeviceIconPage.down('#deviceIconFileField').getEl().down('input[type=file]').dom.files[0],
            preview = addEditDeviceIconPage.down('#deviceIconPreview'),
            reader = new FileReader();

        if (addEditDeviceIconPage.down('#deviceIconFileField').isValid()) {
            reader.readAsDataURL(file);
            reader.addEventListener("load", function () {
                me.showDeviceIconPreview(reader.result);
            }, false);
        } else {
            preview.hide();
        }
    },

    showDeviceIconPreview: function (base64string) {
        var me = this,
            addEditDeviceIconPage = me.getAddEditDeviceIcon(),
            preview = addEditDeviceIconPage.down('#deviceIconPreview');

        preview.setSrc(base64string);
        preview.show();

    },

    removeDeviceIcon: function (devicetype) {
        var me = this,
            detailForm = me.getDeviceTypeDetailForm(),
            grid = me.getDeviceTypeGrid();
        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('general.removeDeviceIcon.message', 'MDC', "The device icon will be removed from the device type."),
            title: Uni.I18n.translate('general.removeDeviceIcon', 'MDC', "Remove device icon?"),
            fn: function (btn, opt, text) {
                if (btn === 'confirm') {
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + devicetype.get('id') + '/removedeviceicon',
                        method: 'DELETE',
                        jsonData: {
                            version: devicetype.get('version'),
                            id: devicetype.get('name')
                        },
                        success: function (response) {
                            var json = JSON.parse(response.responseText),
                                deviceType = Ext.create('Mdc.model.DeviceType', json);
                            if (!Ext.isEmpty(detailForm)) {
                                detailForm.loadRecord(deviceType);
                                if (detailForm.up('#deviceTypeDetail').down('#device-type-action-menu')) {
                                    detailForm.up('#deviceTypeDetail').down('#device-type-action-menu').record = deviceType;
                                }
                                me.updateDeviceTypeIcon(deviceType.get('deviceIcon'));
                            } else if (!Ext.isEmpty(grid)) {
                                grid.getStore().load();
                            }
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceIcon.acknowlegment.removed', 'MDC', 'Device icon removed'));
                        }
                    });

                }
            }
        });
    },

    updateDeviceTypeIcon: function(iconInfo) {
        var me = this;
        me.getDeviceIconDisplayField().setVisible(!Ext.isEmpty(iconInfo));
        me.getNoDeviceIconDisplayField().setVisible(Ext.isEmpty(iconInfo));
        if (!Ext.isEmpty(iconInfo)) {
            me.getDeviceIconDisplayField().setSrc('data:image;base64,' + iconInfo);
        }
    },

    saveDeviceIcon: function () {
        var me = this,
            addEditDeviceIconPage = me.getAddEditDeviceIcon(),
            form = addEditDeviceIconPage.down('form'),
            formToSubmit = form.getEl().dom,
            router = this.getController('Uni.controller.history.Router'),
            route = router.previousRoute && router.previousRoute !== 'administration/devicetypes/view/adddeviceicon' && router.previousRoute !== 'administration/devicetypes/view/editdeviceicon'
                ? router.previousRoute : 'administration/devicetypes/view';

        form.down('#form-errors').hide();
        form.down('#deviceIconFileField').clearInvalid();
        if (form.isValid()) {
            addEditDeviceIconPage.setLoading(true);
            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + addEditDeviceIconPage.deviceTypeId + '/adddeviceicon',
                method: 'POST',
                form: formToSubmit,
                isFormUpload: true,
                params: {
                    version: addEditDeviceIconPage.version,
                    name: addEditDeviceIconPage.deviceTypeName
                },
                headers: {'Content-type': 'multipart/form-data'},
                callback: function (config, success, response) {
                    if (!Ext.isEmpty(response.responseText)) {
                        var responseObject = JSON.parse(response.responseText);
                        if (!Ext.isEmpty(responseObject.errorCode) && responseObject.errorCode !== 'RUT0005') {
                            form.down('#deviceIconFileField').markInvalid(responseObject.message);
                        } else if (!Ext.isEmpty(responseObject.errorCode) && responseObject.errorCode === 'RUT0005') {
                            response.status = 409;
                            Ext.Ajax.fireEvent('requestexception', null, response, config);
                        } else if (Ext.isEmpty(responseObject.errorCode)) {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceIcon.acknowlegment.added', 'MDC', 'Device icon added'));
                            router.getRoute(route).forward();
                        }
                    }
                    addEditDeviceIconPage.setLoading(false);
                }
            });
        } else {
            form.down('#form-errors').show();
        }
    },

    showErrorPanel: function () {
        this.getDeviceTypeEditForm().down('#deviceTypeEditFormErrors').show();
    }
    ,

    hideErrorPanel: function () {
        this.getDeviceTypeEditForm().down('#deviceTypeEditFormErrors').hide();
    },

    saveCustomAttributes: function () {
        var me = this,
            form = me.getEditCustomAttributePropertyForm(),
            editView = me.getDeviceTypeCustomAttributesEditView(),
            errorPanel = editView.down('#device-type-custom-attributes-error-msg');

        editView.setLoading();
        Ext.suspendLayouts();
        errorPanel.hide();
        form.clearInvalid();
        Ext.resumeLayouts(true);
        form.updateRecord();

        form.getRecord().save({
            backUrl: me.getLandingUrl(),
            success: function (record) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceTypeAttributes.saved', 'MDC', 'Device Type attributes saved'));
                me.goToAttributesLandingFromCas();
            },
            failure: function (rec, operation) {
                var json = Ext.decode(operation.response.responseText, true);

                if (json && json.errors) {
                    Ext.suspendLayouts();
                    form.markInvalid(json.errors);
                    errorPanel.show();
                    Ext.resumeLayouts(true);
                }
            },
            callback: function () {
                editView.setLoading(false);
            }
        });
    },

    restoreDefaultCustomAttributes: function () {
        this.getEditCustomAttributePropertyForm().restoreAll();
    },

    getLandingUrl: function () {
        return this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view').buildUrl();
    },

    goToAttributesLandingFromCas: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view').forward();
    }
})
;
