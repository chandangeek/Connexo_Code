/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.controller.SecurityAccessors', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.securityaccessors.view.Setup',
        'Mdc.securityaccessors.view.AddEditSecurityAccessor',
        'Uni.view.window.Confirmation',
        'Mdc.securityaccessors.view.SecurityAccessorsPrivilegesEditWindow'
    ],

    stores: [
        'Mdc.securityaccessors.store.SecurityAccessors',
        'Mdc.store.TimeUnitsYearsSeconds'
    ],

    models: [
        'Mdc.model.DeviceType',
        'Mdc.securityaccessors.model.SecurityAccessor'
    ],

    refs: [
        {
            ref: 'securityAccessorsGrid',
            selector: '#mdc-security-accessors-grid'
        },
        {
            ref: 'previewForm',
            selector: '#mdc-devicetype-security-accessors-preview-form form'
        },
        {
            ref: 'preview',
            selector: 'security-accessors-preview'
        },
        {
            ref: 'addEditForm',
            selector: 'security-accessor-add-form form'
        },
        {
            ref: 'securityAccessorPrivilegesEditWindow',
            selector: 'security-accessors-privileges-edit-window'
        }
    ],

    fromEditForm: false,
    deviceTypeId: null,
    deviceType: null,
    init: function () {
        var me = this;

        me.control({
            'device-type-security-accessors-setup #mdc-security-accessors-grid': {
                select: me.recordSelected
            },
            '#mdc-add-security-accessor': {
                click: me.navigateToAddSecurityAccessor
            },
            '#mdc-security-accessor-cancel-add-button[action=cancelAddEditSecurityAccessor]': {
                click: me.navigateToOverviewPage
            },
            '#mdc-security-accessor-add-button': {
                click: me.addSecurityAccessor
            },
            'security-accessors-action-menu': {
                click: me.chooseAction
            },
            '#mdc-security-accessor-key-type-combobox': {
                change: me.keyTypeChanged
            },
            '#mdc-security-accessors-privileges-edit-window-save': {
                click: this.saveSecurityAccessor
            }
        });
    },

    navigateToOverviewPage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/devicetypes/view/securityaccessors', {deviceTypeId: me.deviceTypeId});
        route.forward();
    },

    showSecurityAccessorsOverview: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.securityaccessors.store.SecurityAccessors');
        store.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('device-type-security-accessors-setup', {
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
                me.navigateToEditSecurityAccessor(menu.record);
                break;
            case 'remove':
                me.removeSecurityAccessor(menu.record);
                break;
            case 'changePrivileges':
                Ext.widget('security-accessors-privileges-edit-window', {
                    securityAccessorRecord: menu.record
                }).show();
                break;
        }
    },

    recordSelected: function (grid, record) {
        var me = this;
        me.getPreviewForm().loadRecord(record);
        me.getPreview().setTitle(Ext.htmlEncode(record.get('name')));
        if (me.getPreview().down('security-accessors-action-menu')) {
            me.getPreview().down('security-accessors-action-menu').record = record;
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

    navigateToAddSecurityAccessor: function (button) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        route = router.getRoute('administration/devicetypes/view/securityaccessors/add', {deviceTypeId: me.deviceTypeId});
        route.forward();
    },

    showAddSecurityAccessor: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.store.TimeUnitsYearsSeconds'),
            keyTypesStore = me.getStore('Mdc.securityaccessors.store.KeyTypes');

        keyTypesStore.getProxy().setUrl(deviceTypeId);

        keyTypesStore.load();
        me.deviceTypeId = deviceTypeId;
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                view = Ext.widget('security-accessor-add-form', {deviceTypeId: deviceTypeId});
                view.down('form').loadRecord(Ext.create('Mdc.securityaccessors.model.SecurityAccessor'));
                //the device type is needed for the versioning
                me.deviceType = deviceType;
                store.load({
                    callback: function (records, operation, success) {
                        if (success && records.length > 0) {
                            view.down('#cbo-security-accessor-validity-period-delay').select(records[0]);
                        }
                    }
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });

    },

    addSecurityAccessor: function () {
        var me = this,
            form = me.getAddEditForm(),
            errorMessage = form.down('#mdc-security-accessor-error-message'),
            record;

        debugger;
        errorMessage.hide();
        form.updateRecord();
        record = form.getRecord();
        record.getProxy().setUrl(me.deviceTypeId);
        record.beginEdit();
        if(record.get('keyType') && record.get('keyType').requiresDuration) {
            record.set('validityPeriod', {
                count: form.down('#num-security-accessor-validity-period').getValue(),
                timeUnit: form.down('#cbo-security-accessor-validity-period-delay').getValue()
            });
        } else {
            record.set('validityPeriod', null);
        }
        record.set('parent', {
            id: me.deviceType.get('name'),
            version: me.deviceType.get('version')
        });
        record.set('defaultEditLevels', null);
        record.set('defaultViewLevels', null);
        record.set('editLevels', null);
        record.set('editLevelsInfo', null);
        record.set('viewLevels', null);
        record.set('viewLevelsInfo', null);
        record.endEdit();
        record.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', operation.action === 'update'
                        ? Uni.I18n.translate('securityaccessors.saveSecurityAccessorSuccess', 'MDC', 'Security accessor saved')
                        : Uni.I18n.translate('securityaccessors.addSecurityAccessorSuccess', 'MDC', 'Security accessor added'));
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

    navigateToEditSecurityAccessor: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route;

        router.arguments.deviceTypeId = me.deviceTypeId;
        router.arguments.keyFunctionTypeId = record.get('id');

        route = router.getRoute('administration/devicetypes/view/securityaccessors/edit');
        route.forward(router.arguments);
    },

    showEditSecurityAccessor: function (deviceTypeId, securityAccessorId) {
        var me = this,
            view,
            store = me.getStore('Mdc.store.TimeUnitsYearsSeconds'),
            keyTypesStore = me.getStore('Mdc.securityaccessors.store.KeyTypes'),
            model =  Ext.ModelManager.getModel('Mdc.securityaccessors.model.SecurityAccessor'),
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
        model.load(securityAccessorId, {
            success: function (record) {
                me.getApplication().fireEvent('securityaccessorload', record.get('name'));
                callBackFunction = function() {
                    counter --;
                    if(counter <= 0) {
                        view = Ext.widget('security-accessor-add-form', {
                            deviceTypeId: deviceTypeId,
                            isEdit: true,
                            title: Uni.I18n.translate('general.editX', 'MDC', "Edit '{0}'", record.get('name'), false)
                        });
                        view.down('form').loadRecord(record);
                        view.down('#mdc-security-accessor-key-type-combobox').select(record.get('keyType').id);
                        if(record.get('validityPeriod')) {
                            view.down('#num-security-accessor-validity-period').setValue(record.get('validityPeriod').count);
                            view.down('#cbo-security-accessor-validity-period-delay').select(record.get('validityPeriod').timeUnit);
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

    removeSecurityAccessor: function (record) {
        var me = this,
            store = me.getStore('Mdc.securityaccessors.store.SecurityAccessors'),
            confirmationWindow;
        record.getProxy().setUrl(me.deviceTypeId);
        confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('securityaccessors.remove.msg', 'MDC', 'This security accessor will no longer be available.'),
                title: Uni.I18n.translate('general.removeX', 'MDC', "Remove '{0}'?", Ext.htmlEncode(record.get('name')), false),
                fn: function (state) {
                    if (state === 'confirm') {
                        me.getSecurityAccessorsGrid().setLoading();
                        record.destroy({
                            callback: function () {
                                Uni.I18n.translate('securityaccessors.removeSecurityAccessorSuccess', 'MDC', 'Security accessor removed');
                                me.getSecurityAccessorsGrid().down('pagingtoolbartop').resetPaging();
                                me.getSecurityAccessorsGrid().down('pagingtoolbarbottom').resetPaging();
                                store.load({
                                    callback: function (records, operation, success) {
                                        me.getSecurityAccessorsGrid().setLoading(false);
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
        combobox.up('form').down('#mdc-security-accessor-validity-period').setVisible(newValue.requiresDuration);
    },

    saveSecurityAccessor: function () {
        var me = this,
            editWindow = me.getSecurityAccessorPrivilegesEditWindow(),
            securityAccessorRecordInEditWindow = editWindow.securityAccessorRecord,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        editWindow.close();
        viewport.setLoading();
        securityAccessorRecordInEditWindow.getProxy().setUrl(me.deviceTypeId);
        securityAccessorRecordInEditWindow.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('securityaccessors.saveSecurityAccessorSuccess', 'MDC', 'Security accessor saved'));
                    me.recordSelected(me.getSecurityAccessorsGrid(), securityAccessorRecordInEditWindow);
                    viewport.setLoading(false);
                }
            }
        });
    }
});