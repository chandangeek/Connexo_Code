/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.keyfunctiontypes.controller.KeyFunctionTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.keyfunctiontypes.view.Setup',
        'Mdc.keyfunctiontypes.view.AddEditKeyFunctionType',
        'Uni.view.window.Confirmation',
        'Mdc.keyfunctiontypes.view.KeyFunctionTypesPrivilegesEditWindow'
    ],

    stores: [
        'Mdc.keyfunctiontypes.store.KeyFunctionTypes',
        'Mdc.store.TimeUnitsYearsSeconds'
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
        },
        {
            ref: 'keyFunctionTypePrivilegesEditWindow',
            selector: 'keyfunctiontype-privileges-edit-window'
        }
    ],

    fromEditForm: false,
    deviceTypeId: null,
    deviceType: null,
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
            '#key-function-type-add-button': {
                click: me.addKeyFunctionType
            },
            'key-function-types-action-menu': {
                click: me.chooseAction
            },
            '#key-function-type-key-type-combobox': {
                change: me.keyTypeChanged
            },
            '#mdc-keyfunctiontype-privileges-edit-window-save': {
                click: this.saveKeyFunctionType
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
        store.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('device-type-key-function-types-setup', {
                    deviceTypeId: deviceTypeId
                });
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
            case 'changePrivileges':
                Ext.widget('keyfunctiontype-privileges-edit-window', {
                    keyFunctionTypeRecord: menu.record
                }).show();
                break;
        }
    },

    recordSelected: function (grid, record) {
        var me = this;
        me.getPreviewForm().loadRecord(record);
        me.getPreview().setTitle(Ext.htmlEncode(record.get('name')));
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
            view,
            store = me.getStore('Mdc.store.TimeUnitsYearsSeconds'),
            keyTypesStore = me.getStore('Mdc.keyfunctiontypes.store.KeyTypes');

        keyTypesStore.getProxy().setUrl(deviceTypeId);

        keyTypesStore.load();
        me.deviceTypeId = deviceTypeId;
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('key-function-type-add-form', {deviceTypeId: deviceTypeId});
                view.down('form').loadRecord(Ext.create('Mdc.keyfunctiontypes.model.KeyFunctionType'));
                //the device type is needed for the versioning
                me.deviceType = deviceType;
                store.load({
                    callback: function (records, operation, success) {
                        if (success && records.length > 0) {
                            view.down('#cbo-key-function-type-validity-period-delay').select(records[0]);
                        }
                    }
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });

    },

    addKeyFunctionType: function () {
        var me = this,
            form = me.getAddEditForm(),
            errorMessage = form.down('#key-function-type-error-message'),
            record;

        errorMessage.hide();
        form.updateRecord();
        record = form.getRecord();
        record.getProxy().setUrl(me.deviceTypeId);
        record.beginEdit();
        if(record.get('keyType') && record.get('keyType').requiresDuration) {
            record.set('validityPeriod', {
                count: form.down('#num-key-function-type-validity-period').getValue(),
                timeUnit: form.down('#cbo-key-function-type-validity-period-delay').getValue()
            });
        } else {
            record.set('validityPeriod', null);
        }
        record.set('parent', {
            id: me.deviceType.get('name'),
            version: me.deviceType.get('version')
        });
        record.endEdit();
        record.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', operation.action === 'update'
                        ? Uni.I18n.translate('keyfunctiontypes.saveKeyFunctionTypeSuccess', 'MDC', 'Key function type saved')
                        : Uni.I18n.translate('keyfunctiontypes.addKeyFunctionTypeSuccess', 'MDC', 'Key function type added'));
                    me.navigateToOverviewPage();
                } else {
                    if (responseText && responseText.errors) {
                        errorMessage.show();
                        form.getForm().markInvalid(responseText.errors);
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
            view,
            store = me.getStore('Mdc.store.TimeUnitsYearsSeconds'),
            keyTypesStore = me.getStore('Mdc.keyfunctiontypes.store.KeyTypes'),
            model =  Ext.ModelManager.getModel('Mdc.keyfunctiontypes.model.KeyFunctionType'),
            callBackFunction,
            counter = 2;

        keyTypesStore.getProxy().setUrl(deviceTypeId);
        me.deviceTypeId = deviceTypeId;
        model.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceType = deviceType;
            }
        });
        model.load(keyFunctionTypeId, {
            success: function (record) {
                me.getApplication().fireEvent('keyfunctiontypeload', record.get('name'));
                callBackFunction = function() {
                    counter --;
                    if(counter <= 0) {
                        view = Ext.widget('key-function-type-add-form', {
                            deviceTypeId: deviceTypeId,
                            isEdit: true,
                            title: Uni.I18n.translate('general.editX', 'MDC', "Edit '{0}'", record.get('name'), false)
                        });
                        view.down('form').loadRecord(record);
                        view.down('#key-function-type-key-type-combobox').select(record.get('keyType').id);
                        if(record.get('validityPeriod')) {
                            view.down('#num-key-function-type-validity-period').setValue(record.get('validityPeriod').count);
                            view.down('#cbo-key-function-type-validity-period-delay').select(record.get('validityPeriod').timeUnit);
                        }
                        me.deviceTypeId = deviceTypeId;
                        me.getApplication().fireEvent('changecontentevent', view);
                    }
                };
                store.load({callback: callBackFunction});
                keyTypesStore.load({callback: callBackFunction});

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
                msg: Uni.I18n.translate('keyfunctiontype.remove.msg', 'MDC', 'This key function type will no longer be available.'),
                title: Uni.I18n.translate('general.removeX', 'MDC', "Remove '{0}'?", Ext.htmlEncode(record.get('name')), false),
                fn: function (state) {
                    if (state === 'confirm') {
                        me.getTypesGrid().setLoading();
                        record.destroy({
                            callback: function () {
                                Uni.I18n.translate('keyfunctiontypes.removeKeyFunctionTypeSuccess', 'MDC', 'Key function type removed');
                                me.getTypesGrid().down('pagingtoolbartop').resetPaging();
                                me.getTypesGrid().down('pagingtoolbarbottom').resetPaging();
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
    },

    keyTypeChanged: function(combobox, newValue) {
        combobox.up('form').down('#key-function-type-validity-period').setVisible(newValue.requiresDuration);
    },

    saveKeyFunctionType: function () {
        var me = this,
            editWindow = me.getKeyFunctionTypePrivilegesEditWindow(),
            keyFunctionTypeRecordInEditWindow = editWindow.keyFunctionTypeRecord,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        editWindow.close();
        viewport.setLoading();
        keyFunctionTypeRecordInEditWindow.getProxy().setUrl(me.deviceTypeId);
        keyFunctionTypeRecordInEditWindow.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('keyfunctiontypes.saveKeyFunctionTypeSuccess', 'MDC', 'Key function type saved'));
                    me.recordSelected(me.getTypesGrid(), keyFunctionTypeRecordInEditWindow);
                    viewport.setLoading(false);
                }
            }
        });
    }
});