Ext.define('Mdc.controller.setup.RegisterMappings', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.register.RegisterMappingsSetup',
        'setup.register.RegisterMappingsGrid',
        'setup.register.RegisterMappingPreview',
        'setup.register.RegisterMappingAdd',
        'setup.register.RegisterMappingAddGrid',
        'setup.register.ReadingTypeDetails'
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
        {ref: 'registerTypeAddTitle', selector: '#registerTypeAddTitle'},
        {ref: 'addRegisterMappingBtn', selector: '#addRegisterMappingBtn'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'registerMappingAddGrid', selector: '#registermappingaddgrid'},
        {ref: 'previewMrId', selector: '#preview_mrid'}
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
                showReadingTypeInfo: this.showReadingType,
                removeItem: this.removeRegisterMapping
            },
            '#loadProfileTypeAddMeasurementTypesGrid actioncolumn': {
                showReadingTypeInfo: this.showReadingType
            },
            '#addButton[action=addRegisterMappingAction]': {
                click: this.addRegisterMappingsToDeviceType
            },
            '#registerMappingPreviewForm button[action = showReadingTypeInfo]': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registermappingaddgrid actioncolumn': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registerMappingPreview menuitem[action=removeRegisterMapping]': {
                click: this.removeRegisterMappingFromPreview
            }
        });
    },

    showEditView: function (id) {

    },

    addRegisterMappingHistory: function () {
        location.href = this.getAddRegisterMappingBtn().href;
    },

    previewRegisterMapping: function (grid, record) {
        var registerMappings = this.getRegisterMappingGrid().getSelectionModel().getSelection();
        if (registerMappings.length == 1) {
            this.getRegisterMappingPreviewForm().loadRecord(registerMappings[0]);
            var registerMappingsName = this.getRegisterMappingPreviewForm().form.findField('name').getSubmitValue();
            this.getRegisterMappingPreview().getLayout().setActiveItem(1);
            //this.getRegisterMappingPreviewTitle().update('<h4>' + registerMappingsName + '</h4>');
            this.getRegisterMappingPreview().setTitle(registerMappingsName);
            this.getPreviewMrId().setValue(registerMappings[0].getReadingType().get('mrid'));
            this.getRegisterMappingPreviewForm().loadRecord(registerMappings[0]);
        } else {
            this.getRegisterMappingPreview().getLayout().setActiveItem(0);
        }
    },

    showRegisterMappings: function (id) {
        var me = this;
        this.getRegisterTypesOfDevicetypeStore().getProxy().setExtraParam('deviceType', id);
        var widget = Ext.widget('registerMappingsSetup', {deviceTypeId: id});
        me.getAddRegisterMappingBtn().href = '#/administration/devicetypes/' + id + '/registertypes/add';
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                var deviceTypeName = deviceType.get('name');
                // widget.down('#registerTypeTitle').html = '<h1>' + deviceTypeName + ' > ' + Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types') + '</h1>';
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getRegisterMappingGrid().getSelectionModel().doSelect(0);
            }
        });
    },

    addRegisterMappings: function (id) {
        var me = this;
        var widget = Ext.widget('registerMappingAdd', {deviceTypeId: id});
        me.deviceTypeId = id;
        this.getAvailableRegisterTypesStore().getProxy().setExtraParam('deviceType', id);
        this.getAvailableRegisterTypesStore().load(
            {
                callback: function () {
                    Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            var deviceTypeName = deviceType.get('name');
                            //widget.down('#registerTypeAddTitle').html = '<h1>' + deviceTypeName + ' > ' + Uni.I18n.translate('registerMappingAdd.addRegisterTypes', 'MDC', 'Add register types') + '</h1>';
                            me.getApplication().fireEvent('changecontentevent', widget);
                            me.getRegisterMappingGrid().getSelectionModel().doSelect(0);
                        }
                    });
                }
            }
        );
    },

    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record.getReadingType());
        widget.show();
    },

    addRegisterMappingsToDeviceType: function () {
        var me = this;
        var registerMappings = this.getRegisterMappingAddGrid().getSelectionModel().getSelection();
        var widget = this.getRegisterMappingAddGrid();
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
            success: function (deviceType) {
                deviceType.registerTypes().add(registerMappings);
                deviceType.save({
                    callback: function () {
                        deviceType.commit();
                        me.getRegisterTypesOfDevicetypeStore().add(registerMappings);
                        location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/registertypes';
                        widget.setLoading(false);
                    }
                });
            }
        });
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
                    title: Uni.I18n.translate('general.delete', 'MDC', 'Delete') + ' ' + registerMappingToDelete.get('name') + '?',
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
                    title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' ' + registerMappingToDelete.get('name') + '?',
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
                }
            });
        }
    }

});
