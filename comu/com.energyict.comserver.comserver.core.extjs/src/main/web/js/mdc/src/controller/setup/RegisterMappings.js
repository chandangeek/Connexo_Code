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
        'Mdc.store.RegisterTypesOfDevicetype',
        'Mdc.store.AvailableRegisterTypes',
        'Mdc.model.RegisterTypeOnDeviceType',
        'Mdc.store.CustomAttributeSetsOnRegister'
    ],

    stores: [
        'RegisterTypesOfDevicetype',
        'AvailableRegisterTypes'
    ],

    refs: [
        {ref: 'registerMappingGrid', selector: '#registermappinggrid'},
        {ref: 'registerMappingPreviewForm', selector: '#registerMappingPreviewForm'},
        {ref: 'registerMappingPreview', selector: '#registerMappingPreview'},
        {ref: 'registerMappingPreviewTitle', selector: '#registerMappingPreviewTitle'},
        {ref: 'addRegisterMappingBtn', selector: '#addRegisterMappingBtn'},
        {ref: 'registerMappingAddGrid', selector: '#register-mapping-add-grid'},
        {ref: 'addRegisterMappingPanel', selector: '#addRegisterTypePanel'},
        {ref: 'editRegisterTypePage', selector: '#register-mapping-edit-container-id'}
    ],

    deviceTypeId: null,

    init: function () {

        this.control({
            '#registermappinggrid': {
                selectionchange: this.previewRegisterMapping
            },
            '#registerMappingSetup button[action = addRegisterMapping]': {
                click: this.addRegisterMappingHistory
            },
            '#registermappinggrid actioncolumn': {
                removeTheRegisterMapping: this.removeRegisterMapping,
                editTheRegisterMapping: this.moveToEditPage
            },
            '#addButton[action=addRegisterMappingAction]': {
                click: this.addRegisterMappingsToDeviceType
            },
            '#registerMappingPreview menuitem[action=removeTheRegisterMapping]': {
                click: this.removeRegisterMappingFromPreview
            },
            '#registerMappingPreview #edit-register-mapping-btn-id': {
                click: this.moveToEditPage
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

    moveToEditPage: function () {
        var me = this,
            grid = me.getRegisterMappingGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            router = me.getController('Uni.controller.history.Router');

        router.arguments.registerTypeId = lastSelected.get('id');
        router.getRoute('administration/devicetypes/view/registertypes/edit').forward();
    },

    addRegisterMappingHistory: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/registertypes/add').forward();
    },

    previewRegisterMapping: function (grid, record) {
        var registerMappings = this.getRegisterMappingGrid().getSelectionModel().getSelection();
        if (registerMappings.length == 1) {
            this.getRegisterMappingPreviewForm().loadRecord(registerMappings[0]);
            this.getRegisterMappingPreview().getLayout().setActiveItem(1);
            this.getRegisterMappingPreview().setTitle(registerMappings[0].get('readingType').fullAliasName);
        } else {
            this.getRegisterMappingPreview().getLayout().setActiveItem(0);
        }
    },

    showRegisterMappings: function (id) {
        var me = this,
            widget = Ext.widget('registerMappingsSetup', {deviceTypeId: id});

        this.getRegisterTypesOfDevicetypeStore().getProxy().setExtraParam('deviceType', id);
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
                            me.getRegisterTypesOfDevicetypeStore().add(registerMappings);
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

    removeRegisterMapping: function (registerMappingToDelete, id) {
        if (id === undefined) {
            id = this.getDeviceTypeIdFromHref();
        }
        var me = this;

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
            success: function (deviceType) {
                var msg = Uni.I18n.translate('registerMapping.deleteRegisterType', 'MDC', 'The register type will no longer be available on this device type.');

                if (deviceType.get('isLinkedByActiveRegisterConfig') === false &&
                    deviceType.get('isLinkedByInactiveRegisterConfig') === true) {
                    msg = Uni.I18n.translate('registerMapping.deleteUsedRegisterType', 'MDC', 'The register type will no longer be available on this device type.  It is used by one or more deactivated device configurations.');
                }

                Ext.create('Uni.view.window.Confirmation').show({
                    msg: msg,
                    title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [registerMappingToDelete.get('name')]),
                    config: {
                        registerMappingToDelete: registerMappingToDelete,
                        deviceType: deviceType,
                        me: me
                    },
                    fn: me.removeRegisterMappingFromDeviceType
                });
            }
        });
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
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                scope.getApplication().fireEvent('loadDeviceType', deviceType);
                if (setSideMenu) {
                    widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
                    widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', [deviceType.get('deviceConflictsCount')]));
                }
            }
        });
    },

    removeRegisterMappingFromPreview: function () {
        var me = this;
        var registerMappingToDelete = me.getRegisterMappingGrid().getSelectionModel().getSelection()[0];

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(this.getRegisterMappingPreview().deviceTypeId, {
            success: function (deviceType) {
                var msg = Uni.I18n.translate('registerMapping.deleteRegisterType', 'MDC', 'The register type will no longer be available on this device type.');

                if (deviceType.get('isLinkedByActiveRegisterConfig') === false &&
                    deviceType.get('isLinkedByInactiveRegisterConfig') === true) {
                    msg = Uni.I18n.translate('registerMapping.deleteUsedRegisterType', 'MDC', 'The register type will no longer be available on this device type.  It is used by one or more deactivated device configurations.');
                }

                Ext.create('Uni.view.window.Confirmation').show({
                    msg: msg,
                    title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", [registerMappingToDelete.get('name')]),
                    config: {
                        registerMappingToDelete: registerMappingToDelete,
                        deviceType: deviceType,
                        me: me
                    },
                    fn: me.removeRegisterMappingFromDeviceType
                });
            }
        });
    },

    removeRegisterMappingFromDeviceType: function (btn, text, opt) {
        if (btn === 'confirm') {
            var deviceType = opt.config.deviceType;
            var registerMappingToDelete = opt.config.registerMappingToDelete;
            var me = opt.config.me;
            deviceType.registerTypes().remove(registerMappingToDelete);
            deviceType.save({
                success: function () {
                    me.getRegisterTypesOfDevicetypeStore().remove(registerMappingToDelete);
                    location.href = '#/administration/devicetypes/' + deviceType.get('id') + '/registertypes';
                    me.getApplication().fireEvent('acknowledge', 'Register type removed');
                }
            });
        }
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
