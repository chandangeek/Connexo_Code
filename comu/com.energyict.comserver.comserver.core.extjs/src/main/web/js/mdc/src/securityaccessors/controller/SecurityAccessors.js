/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.controller.SecurityAccessors', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.securityaccessors.view.Setup',
        'Mdc.securityaccessors.view.AddEditSecurityAccessor',
        'Uni.view.window.Confirmation',
        'Mdc.securityaccessors.view.SecurityAccessorsPrivilegesEditWindow',
        'Mdc.securityaccessors.view.AddSecurityAccessorToDeviceType'
    ],

    stores: [
        'Mdc.securityaccessors.store.SecurityAccessors',
        'Mdc.securityaccessors.store.TrustStores',
        'Mdc.store.TimeUnitsYearsSeconds',
        'Mdc.securityaccessors.store.UnusedSecurityAccessors',
        'Mdc.securityaccessors.store.SecurityAccessorsOnDeviceType'
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
            selector: '#mdc-devicetype-security-accessors-preview-form'
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
        },
        {
            ref: 'availableSecurityAccessorsGrid',
            selector: 'security-accessor-add-to-device-type-form #available-security-accessors-grd'
        }
    ],

    fromEditForm: false,
    deviceTypeId: null,
    deviceType: null,
    selectedRecord: undefined,

    init: function () {
        var me = this;

        me.control({
            'device-type-security-accessors-setup #mdc-security-accessors-grid': {
                select: me.recordSelected
            },
            '#mdc-add-security-accessor': {
                click: me.navigateToAddSecurityAccessor
            },
            '#mdc-security-accessor-cancel-link[action=cancelAddEditSecurityAccessor]': {
                click: me.navigateToOverviewPage
            },
            '#btn-cancel-add-security-accessors': {
                click: me.navigateToOverviewPage
            },
            '#mdc-security-accessor-add-button': {
                click: me.addSecurityAccessor
            },
            '#btn-add-security-accessors': {
                click: me.addAvailableSecurityAccessorToDeviceType
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
            router = me.getController('Uni.controller.history.Router');

        (me.deviceTypeId
            ? router.getRoute('administration/devicetypes/view/securityaccessors', {deviceTypeId: me.deviceTypeId})
            : router.getRoute('administration/securityaccessors')).forward();
    },

    showSecurityAccessorsOverview: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.securityaccessors.store.SecurityAccessorsOnDeviceType');
        me.deviceTypeId = deviceTypeId;
        store.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceType = deviceType;
                view = Ext.widget('device-type-security-accessors-setup', {
                    deviceTypeId: deviceTypeId
                });

                me.getApplication().fireEvent('changecontentevent', view);
                me.reconfigureMenu(deviceType, view);
            }
        });
    },

    showSecurityAccessorsOverviewAdministration: function () {
        var me = this,
            view = Ext.widget('device-type-security-accessors-setup');

        me.deviceTypeId = null;
        me.deviceType = null;
        me.getApplication().fireEvent('changecontentevent', view);
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'edit':
                me.navigateToEditSecurityAccessor(menu.record);
                break;
            case 'remove':
                me.deviceType ? me.removeSecurityAccessorFromDeviceType(menu.record) :me.removeSecurityAccessor(menu.record);
                break;
            case 'changePrivileges':
                Ext.widget('security-accessors-privileges-edit-window', {
                    securityAccessorRecord: me.selectedRecord
                }).show();
                break;
        }
    },

    recordSelected: function (grid, recordParam) {
        var me = this,
            processRecord = function (record) {
                me.selectedRecord = record;
                me.getPreviewForm().doLoadRecord(record);
                me.getPreview().setTitle(Ext.htmlEncode(record.get('name')));
                if (me.getPreview().down('security-accessors-action-menu')) {
                    me.getPreview().down('security-accessors-action-menu').record = record;
                }
                me.getPreview().setLoading(false);
            };

        me.getPreview().setLoading();
        if (recordParam.get('isKey')) {
            var model = Ext.ModelManager.getModel('Mdc.securityaccessors.model.SecurityAccessor');
            model.load(recordParam.get('id'), {
                success: function (keyRecord) {
                    processRecord(keyRecord);
                }
            });
        } else {
            processRecord(recordParam);
        }
    },

    reconfigureMenu: function (deviceType, view) {
        var me = this;
        view.suspendLayouts();
        view.setLoading(true);
        me.getApplication().fireEvent('loadDeviceType', deviceType);
        if (view.down('deviceTypeSideMenu')) {
            view.down('deviceTypeSideMenu').setDeviceTypeTitle(deviceType.get('name'));
            view.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
            );
        }
        view.setLoading(false);
        view.resumeLayouts();
    },

    navigateToAddSecurityAccessor: function (button) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        (me.deviceTypeId
            ? router.getRoute('administration/devicetypes/view/securityaccessors/add', {deviceTypeId: me.deviceTypeId})
            : router.getRoute('administration/securityaccessors/add')).forward();
    },

    showAddSecurityAccessorToDeviceType: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.securityaccessors.store.UnusedSecurityAccessors');

        me.deviceTypeId = deviceTypeId;
        store.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceType = deviceType;
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                view = Ext.widget('security-accessor-add-to-device-type-form', {deviceTypeId: deviceTypeId});
                store.load({
                    callback: function (records, operation, success) {
                        if (records === null || records.length === 0) {
                            view.down('#btn-add-security-accessors').hide();
                        }
                    }
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });

    },

    addAvailableSecurityAccessorToDeviceType: function () {
        var me = this,
            grid = this.getAvailableSecurityAccessorsGrid(),
            securityAccessors = _.map(grid.getSelectionModel().getSelection(), function (accessorToAdd) {
                return accessorToAdd.getData();
            }),
            jsonData = {
                name: me.deviceType.get('name'),
                version: me.deviceType.get('version'),
                securityAccessors: securityAccessors
            };

        Ext.Ajax.request({
            url: Ext.String.format('/api/dtc/devicetypes/{0}/securityaccessors', me.deviceTypeId),
            method: 'POST',
            jsonData: jsonData,
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('securityaccessors.addSecurityAccessorSuccess', 'MDC', 'Security accessor added'));
                me.getController('Uni.controller.history.Router')
                    .getRoute('administration/devicetypes/view/securityaccessors', {deviceTypeId: me.deviceTypeId})
                    .forward();
            }
        });
    },

    removeSecurityAccessorFromDeviceType: function (record) {
        var me = this,
            store = me.getStore('Mdc.securityaccessors.store.SecurityAccessorsOnDeviceType'),
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            jsonData = {
                name: me.deviceType.get('name'),
                version: me.deviceType.get('version')
            };

        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('deviceType.securityaccessors.remove.msg', 'MDC', 'This security accessor will no longer be available on this device type .'),
                title: Uni.I18n.translate('general.removeX', 'MDC', "Remove '{0}'?", Ext.htmlEncode(record.get('name')), false),
                fn: function (state) {
                    if (state === 'confirm') {
                        Ext.Ajax.request({
                            url: Ext.String.format('/api/dtc/devicetypes/{0}/securityaccessors/{1}', me.deviceTypeId, record.get('id')),
                            method: 'DELETE',
                            jsonData: jsonData,
                            success: function () {
                                me.getSecurityAccessorsGrid().setLoading();
                                me.getApplication().fireEvent('acknowledge',
                                    Uni.I18n.translate('securityaccessors.removeSecurityAccessorSuccess', 'MDC', 'Security accessor removed')
                                );
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

    showAddSecurityAccessor: function () {
        var me = this,
            view,
            unitStore = me.getStore('Mdc.store.TimeUnitsYearsSeconds'),
            keyTypesStore = me.getStore('Mdc.securityaccessors.store.KeyTypes'),
            trustStoresStore = me.getStore('Mdc.securityaccessors.store.TrustStores');

        keyTypesStore.load();
        trustStoresStore.load();
        view = Ext.widget('security-accessor-add-form');
        view.down('form').loadRecord(Ext.create('Mdc.securityaccessors.model.SecurityAccessor'));
        unitStore.load({
            callback: function (records, operation, success) {
                if (success && records.length > 0) {
                    view.down('#cbo-security-accessor-validity-period-delay').select(records[0]);
                }
            }
        });
        me.getApplication().fireEvent('changecontentevent', view);

    },

    addSecurityAccessor: function () {
        var me = this,
            form = me.getAddEditForm(),
            errorMessage = form.down('#mdc-security-accessor-error-message'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            record;

        errorMessage.hide();
        if (!form.isValid()) {
            errorMessage.show();
            return;
        }
        viewport.setLoading();
        form.updateRecord();
        record = form.getRecord();
        record.beginEdit();
        if (record.get('keyType') && record.get('keyType').requiresDuration) {
            record.set('duration', {
                count: form.down('#num-security-accessor-validity-period').getValue(),
                timeUnit: form.down('#cbo-security-accessor-validity-period-delay').getValue()
            });
        } else {
            record.set('duration', null);
        }
        if (!Ext.isArray(record.get('defaultEditLevels'))) {
            record.set('defaultEditLevels', null);
        }
        if (!Ext.isArray(record.get('defaultViewLevels'))) {
            record.set('defaultViewLevels', null);
        }
        if (!Ext.isArray(record.get('editLevels'))) {
            record.set('editLevels', null);
        }
        if (!Ext.isArray(record.get('viewLevels'))) {
            record.set('viewLevels', null);
        }
        record.endEdit();
        record.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                viewport.setLoading(false);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', operation.action === 'update'
                        ? Uni.I18n.translate('securityaccessors.saveSecurityAccessorSuccess', 'MDC', 'Security accessor saved')
                        : Uni.I18n.translate('securityaccessors.addSecurityAccessorSuccess', 'MDC', 'Security accessor added'));
                    me.navigateToOverviewPage();
                } else if (responseText && responseText.errors) {
                    errorMessage.show();
                    if (Ext.isArray(responseText.errors)) {
                        Ext.Array.each(responseText.errors, function (error) {
                            if (error.id === 'duration') {
                                form.down('#num-security-accessor-validity-period').markInvalid(error.msg);
                            }
                        })
                    }
                    form.getForm().markInvalid(responseText.errors);
                }
            }
        });
    },

    navigateToEditSecurityAccessor: function (record) {
        this.getController('Uni.controller.history.Router')
            .getRoute('administration/securityaccessors/edit').forward({securityAccessorId: record.get('id')});
    },

    showEditSecurityAccessor: function (securityAccessorId) {
        var me = this,
            view,
            timeUnitsStore = me.getStore('Mdc.store.TimeUnitsYearsSeconds'),
            keyTypesStore = me.getStore('Mdc.securityaccessors.store.KeyTypes'),
            model = Ext.ModelManager.getModel('Mdc.securityaccessors.model.SecurityAccessor'),
            callBackFunction,
            storesToLoad = 2;

        model.load(securityAccessorId, {
            success: function (record) {
                me.getApplication().fireEvent('securityaccessorload', record.get('name'));
                callBackFunction = function () {
                    storesToLoad--;
                    if (storesToLoad <= 0) {
                        view = Ext.widget('security-accessor-add-form', {
                            isEdit: true,
                            title: Uni.I18n.translate('general.editX', 'MDC', "Edit '{0}'", record.get('name'), false)
                        });
                        view.down('form').loadRecord(record);
                        if (record.get('keyType').isKey) {
                            view.down('#mdc-security-accessor-key').setValue(true);
                        } else {
                            view.down('#mdc-security-accessor-certificate').setValue(true);
                        }
                        view.down('#mdc-security-accessor-key-type-combobox').setValue(record.get('keyType').id);
                        if (record.get('duration')) {
                            view.down('#num-security-accessor-validity-period').setValue(record.get('duration').count);
                            view.down('#cbo-security-accessor-validity-period-delay').select(record.get('duration').timeUnit);
                        }
                        view.down('#mdc-security-accessor-storage-method-combobox').setDisabled(true);
                        me.getApplication().fireEvent('changecontentevent', view);
                    }
                };
                timeUnitsStore.load({callback: callBackFunction});
                keyTypesStore.load({callback: callBackFunction});
            }
        });
    },

    removeSecurityAccessor: function (record) {
        var me = this,
            store = me.getStore('Mdc.securityaccessors.store.SecurityAccessors'),
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('securityaccessors.remove.msg', 'MDC', 'This security accessor will no longer be available.'),
                title: Uni.I18n.translate('general.removeX', 'MDC', "Remove '{0}'?", Ext.htmlEncode(record.get('name')), false),
                fn: function (state) {
                    if (state === 'confirm') {
                        record.destroy({
                            success: function () {
                                me.getSecurityAccessorsGrid().setLoading();
                                me.getApplication().fireEvent('acknowledge',
                                    Uni.I18n.translate('securityaccessors.removeSecurityAccessorSuccess', 'MDC', 'Security accessor removed')
                                );
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

    keyTypeChanged: function (combobox, newValue) {
        var me = this,
            keyEncryptionMethodStore = me.getStore('Mdc.securityaccessors.store.KeyEncryptionMethods'),
            storageMethodCombo = combobox.up('form').down('#mdc-security-accessor-storage-method-combobox'),
            validityPeriod = combobox.up('form').down('#mdc-security-accessor-validity-period'),
            requiresDuration = newValue && newValue.requiresDuration,
            requiresKeyEncryptionMethod = newValue && newValue.requiresKeyEncryptionMethod;

        validityPeriod.setVisible(requiresDuration);
        storageMethodCombo.setVisible(requiresKeyEncryptionMethod);
        storageMethodCombo.setDisabled(!requiresKeyEncryptionMethod);
        if (!Ext.isEmpty(newValue)) {
            keyEncryptionMethodStore.getProxy().setUrl(newValue.id);
            keyEncryptionMethodStore.on('load', function (store, records, successful) {
                storageMethodCombo.bindStore(keyEncryptionMethodStore);
                if (successful && store.getCount() === 1) {
                    storageMethodCombo.setValue(store.getAt(0).get('name'));
                }
            }, me, {single: true});
            keyEncryptionMethodStore.load();
        }
    },

    saveSecurityAccessor: function () {
        var me = this,
            editWindow = me.getSecurityAccessorPrivilegesEditWindow(),
            securityAccessorRecordInEditWindow = editWindow.securityAccessorRecord,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        editWindow.close();
        viewport.setLoading();
        securityAccessorRecordInEditWindow.save({
            callback: function (record, operation, success) {
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('securityaccessors.saveSecurityAccessorSuccess', 'MDC', 'Security accessor saved'));
                    me.recordSelected(me.getSecurityAccessorsGrid(), securityAccessorRecordInEditWindow);
                    viewport.setLoading(false);
                }
            }
        });
    }
});