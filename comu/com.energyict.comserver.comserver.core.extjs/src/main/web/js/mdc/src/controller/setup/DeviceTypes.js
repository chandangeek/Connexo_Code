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
        'AvailableLogbookTypes'
    ],

    refs: [
        {ref: 'deviceTypeGrid', selector: '#devicetypegrid'},
        {ref: 'deviceTypePreviewForm', selector: '#deviceTypePreviewForm'},
        {ref: 'deviceTypePreview', selector: '#deviceTypePreview'},
        //{ref: 'deviceTypeDetailsLink', selector: '#deviceTypeDetailsLink'},
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
        {ref: 'addLogbookPanel', selector: '#addLogbookPanel'}
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
            }
        });
    },

    previewDeviceType: function (grid, record) {
        var deviceTypes = this.getDeviceTypeGrid().getSelectionModel().getSelection();
        if (deviceTypes.length == 1) {
            Ext.suspendLayouts();
            var deviceTypeId = deviceTypes[0].get('id');
            this.getDeviceTypeRegisterLink().getEl().set({href: '#/administration/devicetypes/' + deviceTypeId + '/registertypes'});
            this.getDeviceTypeRegisterLink().getEl().setHTML(deviceTypes[0].get('registerCount') + ' ' + Uni.I18n.translatePlural('devicetype.registers', deviceTypes[0].get('registerCount'), 'MDC', 'register types'));
            this.getDeviceTypeLogBookLink().getEl().set({href: '#/administration/devicetypes/' + deviceTypeId + '/logbooktypes'});
            this.getDeviceTypeLogBookLink().getEl().setHTML(deviceTypes[0].get('logBookCount') + ' ' + Uni.I18n.translatePlural('devicetype.logbooks', deviceTypes[0].get('logBookCount'), 'MDC', 'logbook types'));
            this.getDeviceTypeLoadProfilesLink().getEl().set({href: '#/administration/devicetypes/' + deviceTypeId + '/loadprofiles'});
            this.getDeviceTypeLoadProfilesLink().getEl().setHTML(deviceTypes[0].get('loadProfileCount') + ' ' + Uni.I18n.translatePlural('devicetype.loadprofiles', deviceTypes[0].get('loadProfileCount'), 'MDC', 'load profile types'));
            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);
            //this.getDeviceTypeDetailsLink().update('<a href="#/administration/devicetypes/' + deviceTypeId + '">' + Uni.I18n.translate('general.viewDetails', 'MDC', 'View details') + '</a>');
            this.getDeviceConfigurationsLink().getEl().set({href: '#/administration/devicetypes/' + deviceTypeId + '/deviceconfigurations'});
            this.getDeviceConfigurationsLink().getEl().setHTML(deviceTypes[0].get('deviceConfigurationCount') + ' ' + Uni.I18n.translatePlural('devicetype.deviceconfigurations', deviceTypes[0].get('deviceConfigurationCount'), 'MDC', 'device configurations'));
            //this.getDeviceTypePreview().getHeader().setTitle(deviceTypes[0].get('name'));
            this.getDeviceTypePreview().setTitle(deviceTypes[0].get('name'));
            Ext.resumeLayouts(true);
        }
    },

    showDeviceTypeDetailsView: function (deviceType) {
        var me = this;
        var widget = Ext.widget('deviceTypeDetail', {
            deviceTypeId: deviceType
        });
        var model = Ext.ModelManager.getModel('Mdc.model.DeviceType');
        model.load(deviceType, {
            success: function (deviceType) {
                me.getApplication().fireEvent('changecontentevent', widget);
                var deviceTypeId = deviceType.get('id');
                widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
                me.getDeviceTypeDetailRegistersLink().getEl().set({href: '#/administration/devicetypes/' + deviceTypeId + '/registertypes'});
                me.getDeviceTypeDetailRegistersLink().getEl().setHTML(deviceType.get('registerCount') + ' ' + Uni.I18n.translatePlural('devicetype.registers', deviceType.get('registerCount'), 'MDC', 'register types'));
                me.getDeviceTypeDetailLogBookLink().getEl().set({href: '#/administration/devicetypes/' + deviceTypeId + '/logbooktypes'});
                me.getDeviceTypeDetailLogBookLink().getEl().setHTML(deviceType.get('logBookCount') + ' ' + Uni.I18n.translatePlural('devicetype.logbooks', deviceType.get('logBookCount'), 'MDC', 'logbook types'));
                me.getDeviceTypeDetailLoadProfilesLink().getEl().set({href: '#/administration/devicetypes/' + deviceTypeId + '/loadprofiles'});
                me.getDeviceTypeDetailLoadProfilesLink().getEl().setHTML(deviceType.get('loadProfileCount') + ' ' + Uni.I18n.translatePlural('devicetype.loadprofiles', deviceType.get('loadProfileCount'), 'MDC', 'loadprofile types'));
                me.getDeviceConfigurationsDetailLink().getEl().set({href: '#/administration/devicetypes/' + deviceTypeId + '/deviceconfigurations'});
                me.getDeviceConfigurationsDetailLink().getEl().setHTML(deviceType.get('deviceConfigurationCount') + ' ' + Uni.I18n.translatePlural('devicetype.deviceconfigurations', deviceType.get('deviceConfigurationCount'), 'MDC', 'device configurations'));
                widget.down('form').loadRecord(deviceType);
                me.getApplication().fireEvent('loadDeviceType', deviceType);
            }
        });

    },

    createDeviceTypeHistory: function () {
        location.href = '#/administration/devicetypes/add';
    },

    editDeviceTypeHistory: function (record) {
        location.href = '#/administration/devicetypes/' + record.get('id') + '/edit';
    },

    editDeviceTypeHistoryFromPreview: function () {
        location.href = '#/administration/devicetypes/' + this.getDeviceTypeGrid().getSelectionModel().getSelection()[0].get("id") + '/edit';
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
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('deviceTypeEdit', {
                edit: true,
                deviceCommunicationProtocols: protocolStore,
                cancelLink: router.queryParams.fromDetails ? router.getRoute('administration/devicetypes/view').buildUrl() : router.getRoute('administration/devicetypes').buildUrl()
            });

        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);

        var when = new Uni.util.When();
        when.when([
            {action: Ext.ModelManager.getModel('Mdc.model.DeviceType').load, context: Ext.ModelManager.getModel('Mdc.model.DeviceType'), args: [deviceTypeId]},
            {action: protocolStore.load, context: protocolStore, simple: true}

        ]).then(
            {
                success: function (results) {
                    var deviceType = results[0][0];
                    me.getApplication().fireEvent('loadDeviceType', deviceType);
                    Ext.suspendLayouts();
                    me.getDeviceTypeEditForm().loadRecord(deviceType);
                    me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + deviceType.get('name') + "'");
                    Ext.resumeLayouts(true);
                    widget.setLoading(false);
                },
                failure: function () {
                    Ext.suspendLayouts();
                    me.getDeviceTypeEditForm().loadRecord(deviceType);
                    me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + deviceType.get('name') + "'");
                    Ext.resumeLayouts(true);
                    widget.setLoading(false);
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

    showDeviceTypeCreateView: function () {
        var me = this,
            protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('deviceTypeEdit', {
            edit: false,
            cancelLink: router.getRoute('administration/devicetypes').buildUrl(),
            deviceCommunicationProtocols: protocolStore
        });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        protocolStore.load({
            callback: function (store) {
                me.getDeviceTypeEditForm().setTitle(Uni.I18n.translate('general.add', 'MDC', 'Add') + ' ' + 'device type');
                widget.setLoading(false);
            }
        });
    },

    createDeviceType: function () {
        var me = this;
        var record = Ext.create(Mdc.model.DeviceType),
            values = this.getDeviceTypeEditForm().getValues();
        if (record) {
            record.set(values);
            record.save({
                success: function (record) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceType.acknowlegment.added', 'MDC', 'Device type added'));
                    location.href = '#/administration/devicetypes/' + record.get('id');
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceTypeEditForm().getForm().markInvalid(json.errors);
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
            var found = false;
            var oldValue = this.getEditDeviceTypeNameField().getValue();
            if (oldValue == '') {
                this.getEditDeviceTypeNameField().setValue(newValue);
            } else {
                var i = 0;
                while ((i < numberOfProtocols) && (!found)) {
                    if (oldValue == this.getDeviceCommunicationProtocolsStore().data.items[i].data.name) {
                        found = true;
                    }
                    i++;
                }
                if (found) {
                    this.getEditDeviceTypeNameField().setValue(newValue);
                }
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
                            me.getDeviceTypeLogbookPanel().setTitle(Uni.I18n.translate('logbooktype.logbookTypes', 'MDC', 'Logbook types'));
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
            widget = Ext.widget('add-logbook-types', {deviceTypeId: deviceTypeId});
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
    }
});
