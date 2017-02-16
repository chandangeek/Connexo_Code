/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.RegisterMappings', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.register.RegisterMappingsSetup',
        'Mdc.view.setup.register.RegisterMappingsGrid',
        'Mdc.view.setup.register.RegisterMappingPreview',
        'Mdc.view.setup.register.RegisterMappingAdd',
        'Mdc.view.setup.register.RegisterMappingAddGrid',
        'Mdc.view.setup.register.RegisterMappingEdit'
    ],

    requires: [
        'Mdc.store.AvailableRegisterTypes',
        'Mdc.model.RegisterTypeOnDeviceType',
        'Mdc.store.CustomAttributeSetsOnRegister'
    ],

    stores: [
        'Mdc.store.RegisterTypesOfDevicetype',
        'AvailableRegisterTypes'
    ],

    models: [
        'Mdc.model.RegisterTypeOnDeviceType'
    ],

    refs: [
        {ref: 'registerMappingGrid', selector: '#registermappinggrid'},
        {ref: 'registerMappingPreviewForm', selector: '#registerMappingPreviewForm'},
        {ref: 'registerMappingPreview', selector: '#registerMappingPreview'},
        {ref: 'registerMappingPreviewTitle', selector: '#registerMappingPreviewTitle'},
        {ref: 'addRegisterMappingBtn', selector: '#addRegisterMappingBtn'},
        {ref: 'registerMappingAddGrid', selector: '#register-mapping-add-grid'},
        {ref: 'addRegisterMappingPanel', selector: '#addRegisterTypePanel'},
        {ref: 'editRegisterTypePage', selector: '#register-mapping-edit-container-id'},
        {ref: 'registerMappingPage', selector: 'registerMappingsSetup'}
    ],

    deviceTypeId: null,

    init: function () {

        this.control({
            '#registermappinggrid': {
                select: this.previewRegisterMapping
            },
            '#registerMappingSetup button[action = addRegisterMapping]': {
                click: this.addRegisterMappingHistory
            },
            '#register-mapping-action-menu': {
                click: this.chooseAction
            },
            '#addButton[action=addRegisterMappingAction]': {
                click: this.addRegisterMappingsToDeviceType
            },
            '#register-mapping-edit-container-id #cancel-edit-register-mapping-type-button': {
                click: this.moveToRegistersPage
            },
            'registerMappingAdd grid': {
                selectionchange: this.hideRegisterMappingsErrorPanel
            },
            '#register-mapping-edit-container-id #edit-register-type-form-panel': {
                saverecord: this.saveRegisterMapping
            }
        });
    },

    moveToRegistersPage: function() {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/registertypes').forward();
    },

    saveRegisterMapping: function(record, value) {
        var me = this,
            editPage = me.getEditRegisterTypePage();

        editPage.setLoading();

        record.set('customPropertySet', value);
        record.save({
            success: function () {
                me.moveToRegistersPage();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registertype.customattribute.saved', 'MDC', 'Register type saved'));
                editPage.setLoading(false);
            },
            failure: function () {
                editPage.setLoading(false);
            }
        });
    },

    addRegisterMappingHistory: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/registertypes/add').forward();
    },

    previewRegisterMapping: function (selectionModel, record) {
        var me = this,
            preview = me.getRegisterMappingPreview(),
            menu = preview.down('menu');

        Ext.suspendLayouts();
        preview.setTitle(Ext.htmlEncode(record.get('readingType').fullAliasName));
        preview.down('form').loadRecord(record);
        Ext.resumeLayouts(true);
        if (menu) {
            menu.record = record;
        }
    },

    showRegisterMappings: function (id) {
        var me = this,
            widget = Ext.widget('registerMappingsSetup', {deviceTypeId: id});

        me.getModel('Mdc.model.RegisterTypeOnDeviceType').getProxy().setUrl(id);
        me.getStore('Mdc.store.RegisterTypesOfDevicetype').getProxy().setExtraParam('deviceType', id);
        if (me.getAddRegisterMappingBtn()) {
            me.getAddRegisterMappingBtn().href = '#/administration/devicetypes/' + id + '/registertypes/add';
        }

        me.getApplication().fireEvent('changecontentevent', widget);
        me.loadDeviceTypeModel(me, widget, id, true);
    },

    addRegisterMappings: function (id) {
        var me = this,
            store = Ext.data.StoreManager.lookup('AvailableRegisterTypes'),
            widget = Ext.widget('registerMappingAdd', {deviceTypeId: id});

        store.getProxy().setExtraParam('deviceType', id);
        store.getProxy().setExtraParam('filter', Ext.encode([
            {
                property: 'available',
                value: true
            }
        ]));
        me.loadDeviceTypeModel(me, widget, id, false);
        me.getApplication().fireEvent('changecontentevent', widget);
        store.load({
            callback: function () {
                me.deviceTypeId = id;
                store.fireEvent('load', store);
            }
        });
    },

    addRegisterMappingsToDeviceType: function () {
        var me = this,
            registerMappings = this.getRegisterMappingAddGrid().getSelectionModel().getSelection(),
            widget = this.getRegisterMappingAddGrid();

        if (registerMappings.length === 0) {
            me.showRegisterMappingsErrorPanel();
        } else {
            widget.setLoading(true);
            Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
                success: function (deviceType) {
                    deviceType.registerTypes().add(registerMappings);
                    deviceType.save({
                        success: function () {
                            deviceType.commit();
                            me.getStore('Mdc.store.RegisterTypesOfDevicetype').add(registerMappings);
                            widget.setLoading(false);
                            me.moveToRegistersPage();
                            me.getApplication().fireEvent('acknowledge', 'Register type(s) added');

                        }
                    });
                }
            });
        }
    },

    getDeviceTypeIdFromHref: function () {
        var urlPart = 'administration/devicetypes/';
        var index = location.href.indexOf(urlPart);
        return parseInt(location.href.substring(index + urlPart.length));
    },

    showRegisterTypesEditView: function(deviceTypeId, registerTypeId) {
        var me = this,
            registerTypeModel = Ext.ModelManager.getModel('Mdc.model.RegisterTypeOnDeviceType'),
            widget,
            form;

        registerTypeModel.getProxy().setUrl(deviceTypeId);
        widget = Ext.widget('register-mapping-edit-container');
        me.loadDeviceTypeModel(me, widget, deviceTypeId, false);
        me.getApplication().fireEvent('changecontentevent', widget);
        registerTypeModel.load(registerTypeId, {
            success: function (registerType) {
                me.getApplication().fireEvent('registertypeondevicetype', registerType);
                form = widget.down('#edit-register-type-form-panel');
                form.setTitle(me.getController('Uni.controller.history.Router').getRoute().title);
                form.customLoadRecord(registerType, deviceTypeId);
            }
        });
    },

    loadDeviceTypeModel: function (scope, widget, deviceTypeId, setSideMenu) {
        var me = this;

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                widget.deviceType = deviceType;
                scope.getApplication().fireEvent('loadDeviceType', deviceType);
                if (widget.down('deviceTypeSideMenu') && setSideMenu) {
                    widget.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
                    widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', [deviceType.get('deviceConflictsCount')]));
                }
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            record = menu.record,
            deviceType = me.getRegisterMappingPage().deviceType;

        switch (item.action) {
            case 'removeTheRegisterMapping':
                Ext.create('Uni.view.window.Confirmation').show({
                    msg: deviceType.get('isLinkedByActiveRegisterConfig') === false && deviceType.get('isLinkedByInactiveRegisterConfig') === true
                        ? Uni.I18n.translate('registerMapping.deleteUsedRegisterType', 'MDC', 'The register type will no longer be available on this device type.  It is used by one or more deactivated device configurations.')
                        : Uni.I18n.translate('registerMapping.deleteRegisterType', 'MDC', 'The register type will no longer be available on this device type.'),
                    title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.get('name')]),
                    fn: function (action) {
                        if (action === 'confirm') {
                            me.removeRegisterMappingFromDeviceType(record);
                        }
                    }
                });
                break;
            case 'editTheRegisterMapping':
                router.getRoute('administration/devicetypes/view/registertypes/edit').forward(Ext.merge(router.arguments, {registerTypeId: record.getId()}));
                break;
        }
    },

    removeRegisterMappingFromDeviceType: function (record) {
        var me = this,
            page = me.getRegisterMappingPage();

        page.setLoading();
        record.destroy({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registertype.acknowledgment.removed', 'MDC', 'Register type removed'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    showRegisterMappingsErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddRegisterMappingPanel().down('#add-register-type-errors'),
            errorPanel = me.getAddRegisterMappingPanel().down('#add-register-type-selection-error');

        formErrorsPanel.show();
        errorPanel.show();
    },

    hideRegisterMappingsErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddRegisterMappingPanel().down('#add-register-type-errors'),
            errorPanel = me.getAddRegisterMappingPanel().down('#add-register-type-selection-error');

        formErrorsPanel.hide();
        errorPanel.hide();
    }
});
