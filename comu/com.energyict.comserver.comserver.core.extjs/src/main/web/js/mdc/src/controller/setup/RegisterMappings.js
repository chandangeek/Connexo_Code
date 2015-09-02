Ext.define('Mdc.controller.setup.RegisterMappings', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.register.RegisterMappingsSetup',
        'setup.register.RegisterMappingsGrid',
        'setup.register.RegisterMappingPreview',
        'setup.register.RegisterMappingAdd',
        'setup.register.RegisterMappingAddGrid'
    ],

    requires: [
        'Mdc.store.RegisterTypesOfDevicetype',
        'Mdc.store.AvailableRegisterTypes'
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
        {ref: 'addRegisterMappingPanel', selector: '#addRegisterTypePanel'}
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
                removeTheRegisterMapping: this.removeRegisterMapping
            },
            '#addButton[action=addRegisterMappingAction]': {
                click: this.addRegisterMappingsToDeviceType
            },
            '#registerMappingPreview menuitem[action=removeTheRegisterMapping]': {
                click: this.removeRegisterMappingFromPreview
            },
            'registerMappingAdd grid': {
                selectionchange: this.hideRegisterMappingsErrorPanel
            }
        });
    },

    addRegisterMappingHistory: function () {
        location.href = this.getAddRegisterMappingBtn().href;
    },

    previewRegisterMapping: function (grid, record) {
        var registerMappings = this.getRegisterMappingGrid().getSelectionModel().getSelection();
        if (registerMappings.length == 1) {
            this.getRegisterMappingPreviewForm().loadRecord(registerMappings[0]);
            //var registerMappingsName = this.getRegisterMappingPreviewForm().form.findField('readingType').getSubmitValue();
            this.getRegisterMappingPreview().getLayout().setActiveItem(1);
            this.getRegisterMappingPreview().setTitle(registerMappings[0].get('readingType').fullAliasName);
            //this.getRegisterMappingPreviewForm().form.findField('unit').setValue(registerMappings[0].data.unitOfMeasure.localizedValue);
        } else {
            this.getRegisterMappingPreview().getLayout().setActiveItem(0);
        }
    },

    showRegisterMappings: function (id) {
        var me = this,
            widget = Ext.widget('registerMappingsSetup', {deviceTypeId: id});

        this.getRegisterTypesOfDevicetypeStore().getProxy().setExtraParam('deviceType', id);
        if(me.getAddRegisterMappingBtn()) {
            me.getAddRegisterMappingBtn().href = '#/administration/devicetypes/' + id + '/registertypes/add';
        }

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getRegisterMappingGrid().getSelectionModel().doSelect(0);
                widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
            }
        });
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

        me.getApplication().fireEvent('changecontentevent', widget);
        store.load({
            callback: function () {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
                    success: function (deviceType) {
                        me.deviceTypeId = id;
                        store.fireEvent('load', store);
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                    }
                });
            }
        });
    },

    addRegisterMappingsToDeviceType: function () {
        var me = this;
        var registerMappings = this.getRegisterMappingAddGrid().getSelectionModel().getSelection();
        var widget = this.getRegisterMappingAddGrid();

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
                            location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/registertypes';
                            me.getApplication().fireEvent('acknowledge', 'Register type(s) added');
                            widget.setLoading(false);
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
                    title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?",[registerMappingToDelete.get('name')]),
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
