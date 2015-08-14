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
        'Mdc.store.DeviceLifeCycles'
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
        {ref: 'deviceTypeLogbookPanel', selector: '#deviceTypeLogbookPanel'},
        {ref: 'addLogbookPanel', selector: '#addLogbookPanel'},
        {ref: 'deviceLifeCycleLink', selector: '#device-life-cycle-link'}
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
                change: this.proposeDeviceTypeName
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
            deviceLifeCycleLink.setText(record[0].get('deviceLifeCycleName'));

            registerLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/registertypes');
            registerLink.setText(deviceTypes[0].get('registerCount') + ' ' + Uni.I18n.translatePlural('devicetype.registers', deviceTypes[0].get('registerCount'), 'MDC', 'register types'));

            logBookLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/logbooktypes');
            logBookLink.setText(deviceTypes[0].get('logBookCount') + ' ' + Uni.I18n.translatePlural('devicetype.logbooks', deviceTypes[0].get('logBookCount'), 'MDC', 'logbook types'));

            loadProfilesLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/loadprofiles');
            loadProfilesLink.setText(deviceTypes[0].get('loadProfileCount') + ' ' + Uni.I18n.translatePlural('devicetype.loadprofiles', deviceTypes[0].get('loadProfileCount'), 'MDC', 'load profile types'));

            deviceConfigurationsLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/deviceconfigurations');
            deviceConfigurationsLink.setText(deviceTypes[0].get('deviceConfigurationCount') + ' ' + Uni.I18n.translatePlural('devicetype.deviceconfigurations', deviceTypes[0].get('deviceConfigurationCount'), 'MDC', 'device configurations'));

            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);

            if(this.getDeviceTypePreview().down('device-type-action-menu'))
                this.getDeviceTypePreview().down('device-type-action-menu').record = deviceTypes[0];

            this.getDeviceTypePreview().setTitle(deviceTypes[0].get('name'));
            Ext.resumeLayouts(true);
        }
    },

    showDeviceTypeDetailsView: function (deviceType) {
        var me = this,
            widget = Ext.widget('deviceTypeDetail', {
                deviceTypeId: deviceType
            }),
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType');

        model.load(deviceType, {
            success: function (deviceType) {
                var deviceTypeId = deviceType.get('id'),
                    registersLink = me.getDeviceTypeDetailRegistersLink(),
                    logBookLink = me.getDeviceTypeDetailLogBookLink(),
                    loadProfilesLink = me.getDeviceTypeDetailLoadProfilesLink(),
                    deviceConfigurationsLink = me.getDeviceConfigurationsDetailLink(),
                    deviceLifeCycleLink = widget.down('#details-device-life-cycle-link');

                me.getApplication().fireEvent('changecontentevent', widget);

                Ext.suspendLayouts();

                widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));

                deviceLifeCycleLink.setHref('#/administration/devicelifecycles/' + encodeURIComponent(deviceType.get('deviceLifeCycleId')));
                deviceLifeCycleLink.setText(deviceType.get('deviceLifeCycleName'));

                registersLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/registertypes');
                registersLink.setText(deviceType.get('registerCount') + ' ' + Uni.I18n.translatePlural('devicetype.registers', deviceType.get('registerCount'), 'MDC', 'register types'));

                logBookLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/logbooktypes');
                logBookLink.setText(deviceType.get('logBookCount') + ' ' + Uni.I18n.translatePlural('devicetype.logbooks', deviceType.get('logBookCount'), 'MDC', 'logbook types'));

                loadProfilesLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/loadprofiles');
                loadProfilesLink.setText(deviceType.get('loadProfileCount') + ' ' + Uni.I18n.translatePlural('devicetype.loadprofiles', deviceType.get('loadProfileCount'), 'MDC', 'loadprofile types'));

                deviceConfigurationsLink.setHref('#/administration/devicetypes/' + encodeURIComponent(deviceTypeId) + '/deviceconfigurations');
                deviceConfigurationsLink.setText(deviceType.get('deviceConfigurationCount') + ' ' + Uni.I18n.translatePlural('devicetype.deviceconfigurations', deviceType.get('deviceConfigurationCount'), 'MDC', 'device configurations'));

                widget.down('form').loadRecord(deviceType);
                widget.down('device-type-action-menu').record = deviceType;

                Ext.resumeLayouts(true);

                me.getApplication().fireEvent('loadDeviceType', deviceType);
            }
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
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + " '" + deviceTypeToDelete.get('name') + "'?",
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
            confugurationStore = me.getStore('Mdc.store.DeviceConfigurations'),
            lifeCycleStore = me.getStore('Mdc.store.DeviceLifeCycles'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('deviceTypeEdit', {
                edit: true,
                deviceCommunicationProtocols: protocolStore,
                cancelLink: router.queryParams.fromDetails ? router.getRoute('administration/devicetypes/view').buildUrl() : router.getRoute('administration/devicetypes').buildUrl()
            });

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        confugurationStore.getProxy().url = confugurationStore.getProxy().baseUrl.replace('{deviceType}', deviceTypeId);
        var when = new Uni.util.When();
        when.when([
            {action: Ext.ModelManager.getModel('Mdc.model.DeviceType').load, context: Ext.ModelManager.getModel('Mdc.model.DeviceType'), args: [deviceTypeId]},
            {action: protocolStore.load, context: protocolStore, simple: true},
            {action: lifeCycleStore.load, context: lifeCycleStore, simple: true},
            {action: confugurationStore.load, context: confugurationStore, simple: true}
        ]).then(
            {
                success: function (results) {
                    if (widget.rendered) {
                        var deviceType = results[0][0];
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        Ext.suspendLayouts();
                        me.getDeviceTypeEditForm().loadRecord(deviceType);
                        me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + deviceType.get('name') + "'");
                        me.modifyEditView(widget, confugurationStore);
                        Ext.resumeLayouts(true);
                        widget.setLoading(false);
                    }
                },
                failure: function () {
                    if (widget.rendered) {
                        Ext.suspendLayouts();
                        me.getDeviceTypeEditForm().loadRecord(deviceType);
                        me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + deviceType.get('name') + "'");
                        Ext.resumeLayouts(true);
                        widget.setLoading(false);
                    }
                }
            }
        );


//        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
//            success: function (deviceType) {
//                protocolStore.load({
//                    callback: function (store) {
//                        widget.down('form').loadRecord(deviceType);
//                        widget.down('#deviceTypeEditCreateTitle').update('<h1>'+Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' "' + deviceType.get('name')+'"</h1>');
//                        widget.setLoading(false);
//                    }
//                })
//            }
//        });


    },

    modifyEditView: function (widget, store) {
        var me = this,
            form = widget.down('#deviceTypeEditForm'),
            infoPanel = widget.down('#info-panel'),
            commProtocolField = form.down('#communicationProtocolComboBox'),
            lifeCycleField = form.down('#device-life-cycle-field'),
            activeConfig = store.find('active', true);
        if (store.getCount() > 0) {
            if (activeConfig < 0) {
                commProtocolField.disable();
                infoPanel.show();
                infoPanel.setText(Uni.I18n.translate('deviceType.edit.notificationMsg1', 'MDC', 'This device type has one or more device configurations. Only fields name and device life cycle are editable'))
            } else {
                commProtocolField.disable();
                lifeCycleField.disable();
                infoPanel.show();
                infoPanel.setText(Uni.I18n.translate('deviceType.edit.notificationMsg2', 'MDC', 'This device type has one or more active device configurations. Only the name field is editable'))
            }
        } else {
            commProtocolField.enable();
            lifeCycleField.enable();
        }
    },

    showDeviceTypeCreateView: function () {
        var me = this,
            protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols'),
            deviceLifeCyclesStore = me.getStore('Mdc.store.DeviceLifeCycles'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('deviceTypeEdit', {
                edit: false,
                cancelLink: router.getRoute('administration/devicetypes').buildUrl(),
                deviceCommunicationProtocols: protocolStore
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        protocolStore.load();
        deviceLifeCyclesStore.load(function () {
            if (deviceLifeCyclesStore.getCount() == 0) {
                widget.down('#device-life-cycle-combo').hide();
                widget.down('#no-device-life-cycles').show();
            }
            me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.add', 'MDC', 'Add') + ' ' + 'device type');
            widget.setLoading(false);
        });
    },

    createDeviceType: function () {
        var me = this;
        var record = Ext.create(Mdc.model.DeviceType),
            values = this.getDeviceTypeEditForm().getValues();
        me.getDeviceTypeEditForm().getForm().clearInvalid();
        if (record) {
            record.set(values);
            record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceType.acknowlegment.added', 'MDC', 'Device type added'));
                    location.href = '#/administration/devicetypes/' + encodeURIComponent(record.get('id'));
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceTypeEditForm().getForm().markInvalid(json.errors);
                        Ext.Array.each(json.errors, function (item) {
                            if (item.id.indexOf('deviceLifeCycle') !== -1) {
                                me.getDeviceTypeEditForm().down('#device-life-cycle-combo').markInvalid(item.msg);
                            }
                        });
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
            page = me.getDeviceTypeEditView();

        if (record) {
            page.setLoading(Uni.I18n.translate('general.saving', 'MDC', 'Saving...'));
            record.set(values);
            record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceType.acknowlegment.saved', 'MDC', 'Device type saved'));
                    router.queryParams.fromDetails ? router.getRoute('administration/devicetypes/view').forward() : router.getRoute('administration/devicetypes').forward();
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceTypeEditForm().getForm().markInvalid(json.errors);
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

    proposeDeviceTypeName: function (t, newValue) {
        if (!this.getDeviceTypeEditView().isEdit()) {
            var numberOfProtocols = this.getDeviceCommunicationProtocolsStore().data.items.length;
            var i = 0;
            while ((i < numberOfProtocols)) {
                if (newValue == this.getDeviceCommunicationProtocolsStore().data.items[i].data.name) {
                    this.getEditDeviceTypeNameField().setValue(newValue);
                }
                i++;
            }
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
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceType.acknowlegment.removed', 'MDC', 'Device type removed'));
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
        store.load(
            {
                callback: function () {
                    var self = this,
                        widget = Ext.widget('device-type-logbooks', {deviceTypeId: deviceTypeId});
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.setLoading(true);
                    model.load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
                            me.getDeviceTypeLogbookPanel().setTitle(Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'));
                            widget.setLoading(false);
                        }
                    });
                    var grid = Ext.ComponentQuery.query('device-type-logbooks grid')[0],
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
                me.getAddLogbookPanel().setTitle(Uni.I18n.translate('general.add', 'MDC', 'Add') + ' ' + 'logbook types');
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
    }
});
