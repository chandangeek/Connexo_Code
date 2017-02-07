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
        'setup.devicetype.AddLogbookTypes'
    ],

    stores: [
        'DeviceTypes',
        'DeviceCommunicationProtocols',
        'LogbookTypesOfDeviceType',
        'AvailableLogbookTypes',
        'Mdc.store.DeviceLifeCycles',
        'Mdc.store.DeviceTypePurposes'
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
        {ref: 'deviceTypeLogbookPanel', selector: '#deviceTypeLogbookPanel'},
        {ref: 'addLogbookPanel', selector: '#addLogbookPanel'},
        {ref: 'deviceLifeCycleLink', selector: '#device-life-cycle-link'},
        {ref: 'deviceLifeCycleCombo', selector: '#no-device-life-cycles'},
        {ref: 'deviceTypeDetailLifeCycleLink', selector: 'deviceTypeDetail #details-device-life-cycle-link'},
        {ref: 'deviceTypeDetailOverviewLink', selector: 'deviceTypeDetail deviceTypeSideMenu #overviewLink'},
        {ref: 'deviceTypeDetailConflictingMappingLink', selector: 'deviceTypeDetail deviceTypeSideMenu #conflictingMappingLink'},
        {ref: 'deviceTypeDetailForm', selector: 'deviceTypeDetail form'},
        {ref: 'deviceTypeDetailActionMenu', selector: 'deviceTypeDetail device-type-action-menu'}
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
            '#deviceTypeEdit #mdc-deviceTypeEdit-typeComboBox': {
                change: this.onDeviceTypePurposeChange
            },
            'device-type-action-menu': {
                click: this.chooseAction
            }
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

            if (deviceTypes[0].get('deviceTypePurpose') === 'DATALOGGER_SLAVE') {
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

            if(this.getDeviceTypePreview().down('device-type-action-menu'))
                this.getDeviceTypePreview().down('device-type-action-menu').record = deviceTypes[0];

            this.getDeviceTypePreview().setTitle(Ext.String.htmlEncode(deviceTypes[0].get('name')));
            Ext.resumeLayouts(true);
        }
    },

    showDeviceTypeDetailsView: function (deviceType) {
        var me = this,
            deviceTypePurposesStore = me.getStore('Mdc.store.DeviceTypePurposes'),
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType');

        deviceTypePurposesStore.load(function() {
            var widget = Ext.widget('deviceTypeDetail', {
                    deviceTypeId: deviceType,
                    purposeStore: deviceTypePurposesStore
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

                        widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
                        widget.down('#device-type-detail-panel').setTitle(Ext.String.htmlEncode(deviceType.get('name')));
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

                    if (deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE') {
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
                    }

                    Ext.resumeLayouts(true);

                    me.getApplication().fireEvent('loadDeviceType', deviceType);
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
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?",[deviceTypeToDelete.get('name')]),
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
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('deviceTypeEdit', {
                edit: true,
                deviceCommunicationProtocols: protocolStore,
                deviceTypePurposes: deviceTypePurposesStore,
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
            {action: configurationStore.load, context: configurationStore, simple: true}
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

    modifyEditView: function (widget, store) {
        var me = this,
            form = widget.down('#deviceTypeEditForm'),
            infoPanel = widget.down('#info-panel'),
            typeField = form.down('#mdc-deviceTypeEdit-typeComboBox'),
            commProtocolField = form.down('#communicationProtocolComboBox'),
            lifeCycleField = form.down('#device-life-cycle-field'),
            activeConfig = store.find('active', true);
        if (store.getCount() > 0) {
            if (activeConfig < 0) {
                typeField.disable();
                commProtocolField.disable();
                infoPanel.show();
                infoPanel.setText(Uni.I18n.translate('deviceType.edit.notificationMsg1', 'MDC', 'This device type has one or more device configurations. Only fields name and device life cycle are editable'))
            } else {
                typeField.disable();
                commProtocolField.disable();
                lifeCycleField.disable();
                infoPanel.show();
                infoPanel.setText(Uni.I18n.translate('deviceType.edit.notificationMsg2', 'MDC', 'This device type has one or more active device configurations. Only the name field is editable'))
            }
        } else {
            typeField.enable();
            commProtocolField.enable();
            lifeCycleField.enable();
        }
    },

    showDeviceTypeCreateView: function () {
        var me = this,
            protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols'),
            deviceLifeCyclesStore = me.getStore('Mdc.store.DeviceLifeCycles'),
            deviceTypePurposesStore = me.getStore('Mdc.store.DeviceTypePurposes'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('deviceTypeEdit', {
                edit: false,
                cancelLink: router.getRoute('administration/devicetypes').buildUrl(),
                deviceCommunicationProtocols: protocolStore,
                deviceTypePurposes: deviceTypePurposesStore
            }),
            counter = 0,
            onAllStoresLoaded = function() {
                counter += 1;
                if (counter === 2) {
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
    },

    createDeviceType: function () {
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

    onDeviceTypePurposeChange: function(combo, newValue) {
        switch(newValue) {
            case 'REGULAR':
                this.getProtocolCombo().show();
                break;
            case 'DATALOGGER_SLAVE':
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
                    widget.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
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
        }

        route && (route = router.getRoute(route));
        route && route.forward(router.arguments, {previousRoute: router.getRoute().buildUrl()});
    },

    showErrorPanel: function () {
        this.getDeviceTypeEditForm().down('#deviceTypeEditFormErrors').show();
    },

    hideErrorPanel: function () {
        this.getDeviceTypeEditForm().down('#deviceTypeEditFormErrors').hide();
    }

});
