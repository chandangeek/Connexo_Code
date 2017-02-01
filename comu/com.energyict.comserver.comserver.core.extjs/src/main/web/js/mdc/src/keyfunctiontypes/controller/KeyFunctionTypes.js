Ext.define('Mdc.keyfunctiontypes.controller.KeyFunctionTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.keyfunctiontypes.view.Setup',
        'Mdc.keyfunctiontypes.view.AddEditKeyFunctionType',
        'Uni.view.window.Confirmation'
    ],

    stores: [
        'Mdc.keyfunctiontypes.store.KeyFunctionTypes',
        'Mdc.store.TimeUnits'
    ],

    models: [
        'Mdc.model.DeviceType',
        'Mdc.keyfunctiontypes.model.KeyFunctionType'
    ],

    refs: [
        {
            ref: 'typesGrid',
            selector: '#key-function-types-grid'
        },
        {
            ref: 'previewForm',
            selector: '#devicetype-key-function-types-preview-form form'
        },
        {
            ref: 'preview',
            selector: 'key-function-types-preview'
        },
        {
            ref: 'addEditForm',
            selector: 'key-function-type-add-form form'
        }
    ],

    fromEditForm: false,
    deviceTypeId: null,
    init: function () {
        var me = this;

        me.control({
            'device-type-key-function-types-setup #key-function-types-grid': {
                select: me.recordSelected
            },
            '#add-key-function-type': {
                click: me.navigateToAddKeyFunctionType
            },
            '#key-function-type-cancel-add-button[action=cancelAddEditKeyFunctionType]': {
                click: me.navigateToOverviewPage
            },
            '#key-function-type-add-button[action=addKeyFunctionType]': {
                click: me.addKeyFunctionType
            },
            'key-function-types-action-menu': {
                click: me.chooseAction
            }

        });
    },

    navigateToOverviewPage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/devicetypes/view/keyfunctiontypes', {deviceTypeId: me.deviceTypeId});
        route.forward();
    },

    showKeyFunctionTypesOverview: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.keyfunctiontypes.store.KeyFunctionTypes');
        //store.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('device-type-key-function-types-setup', {
                    deviceTypeId: deviceTypeId,
                });
                //view.down('files-devicetype-specifications-form').loadRecord(deviceType);
                me.deviceTypeId = deviceTypeId;
                me.getApplication().fireEvent('changecontentevent', view);
                view.suspendLayouts();
                view.setLoading(true);
                me.reconfigureMenu(deviceType, view);
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'edit':
                me.navigateToEditKeyFunctionType(menu.record);
                break;
            case 'remove':
                me.removeKeyFunctionType(menu.record);
                break;
        }
    },

    recordSelected: function (grid, record) {
        var me = this;
        me.getPreviewForm().loadRecord(record);
        me.getPreview().setTitle(record.get('name'));
        if (me.getPreview().down('key-function-types-action-menu')) {
            me.getPreview().down('key-function-types-action-menu').record = record;
        }
    },

    reconfigureMenu: function (deviceType, view) {
        var me = this;
        me.getApplication().fireEvent('loadDeviceType', deviceType);
        if (view.down('deviceTypeSideMenu')) {
            view.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
            view.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
            );
        }
        view.setLoading(false);
        view.resumeLayouts();
    },

    navigateToAddKeyFunctionType: function (button) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/devicetypes/view/keyfunctiontypes/add', {deviceTypeId: me.deviceTypeId});
        route.forward();
    },

    showAddKeyFunctionType: function (deviceTypeId) {
        var me = this,
            view = Ext.widget('key-function-type-add-form', {deviceTypeId: deviceTypeId}),
            store = me.getStore('Mdc.store.TimeUnits');

        view.down('form').loadRecord(Ext.create('Mdc.keyfunctiontypes.model.KeyFunctionType'));
        //load key types as well
        me.deviceTypeId = deviceTypeId;
        store.load({
            callback: function (records, operation, success) {
                if (success && records.length > 0) {
                    view.down('#cbo-key-function-type-validity-period-delay').select(records[0]);
                    me.getApplication().fireEvent('changecontentevent', view);
                }
            }
        });
    },

    addKeyFunctionType: function () {
        var me = this,
            form = me.getAddEditForm(),
            errorMessage = form.down('#key-function-type-error-message'),
            record;

        Ext.suspendLayouts();
        errorMessage.hide();
        Ext.resumeLayouts(true);
        form.updateRecord();
        record = form.getRecord();
        record.beginEdit();
        record.set('validityPeriod', {
            count: form.down('#num-key-function-type-validity-period').getValue(),
            timeUnit: form.down('#cbo-key-function-type-validity-period-delay').getValue()
        });
        record.endEdit();
        record.getProxy().setUrl(me.deviceTypeId);
        record.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', operation.action === 'update'
                        ? Uni.I18n.translate('keyfunctiontypes.saveKeyFunctionTypeSuccess', 'MDC', 'Key function type saved')
                        : Uni.I18n.translate('metrologyconfiguration.addKeyFunctionTypeSuccess', 'MDC', 'Key function type saved'));
                    me.navigateToOverviewPage();
                } else {
                    if (responseText && responseText.errors) {
                        debugger;
                        errorMessage.show();
                        form.markInvalid(responseText.errors);
                    }
                }
            }
        });
    },

    navigateToEditKeyFunctionType: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        router.arguments.deviceTypeId = me.deviceTypeId;
        router.arguments.keyFunctionTypeId = record.get('id');

        route = router.getRoute('administration/devicetypes/view/keyfunctiontypes/edit');
        route.forward(router.arguments);
    },

    showEditKeyFunctionType: function (deviceTypeId, keyFunctionTypeId) {
        var me = this,
            view = Ext.widget('key-function-type-add-form', {deviceTypeId: deviceTypeId, isEdit: true}),
            store = me.getStore('Mdc.store.TimeUnits');

        me.deviceTypeId = deviceTypeId;
        Ext.ModelManager.getModel('Mdc.keyfunctiontypes.model.KeyFunctionType').load(keyFunctionTypeId, {
            success: function (record) {
                view.loadRecord(record);
                me.deviceTypeId = deviceTypeId;
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    removeKeyFunctionType: function (record) {
        var me = this,
            store = me.getStore('Mdc.keyfunctiontypes.store.KeyFunctionTypes'),
            confirmationWindow;
        record.getProxy().setUrl(me.deviceTypeId);
        confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('keyfunctiontype.remove.msg', 'MDC', "MESSAGE TO BE DEFINED"),
                title: Uni.I18n.translate('general.removeX', 'MDC', "Remove '{0}'?", record.get('name')),
                fn: function (state) {
                    if (state === 'confirm') {
                        me.getTypesGrid().setLoading();
                        record.destroy({
                            callback: function () {
                                store.load({
                                    callback: function (records, operation, success) {
                                        me.getTypesGrid().setLoading(false);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        );
    }
});