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
        'Mdc.store.AvailableRegisterTypes',
        'Uni.model.BreadcrumbItem'
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
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'registerMappingAddGrid', selector: '#registermappingaddgrid'}
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
            this.getRegisterMappingPreviewTitle().update('<h4>' + registerMappingsName + '</h4>');
            this.getRegisterMappingPreviewForm().loadRecord(registerMappings[0]);
        } else {
            this.getRegisterMappingPreview().getLayout().setActiveItem(0);
        }
    },

    showRegisterMappings: function (id) {
        var me = this;
        this.getRegisterTypesOfDevicetypeStore().getProxy().setExtraParam('deviceType', id);
        this.getRegisterTypesOfDevicetypeStore().load(
            {
                callback: function () {
                    var widget = Ext.widget('registerMappingsSetup', {deviceTypeId: id});
                    me.getAddRegisterMappingBtn().href = '#/setup/devicetypes/' + id + '/registertypes/add';
                    Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
                        success: function (deviceType) {
                            var deviceTypeName = deviceType.get('name');
                            widget.down('#registerTypeTitle').html = '<h1>' + deviceTypeName + ' > ' + Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types') + '</h1>';
                            me.getApplication().getMainController().showContent(widget);
                            me.createBreadCrumbs(id, deviceTypeName);
                        }
                    });
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
                            var deviceTypeName = deviceType.get('name');
                            widget.down('#registerTypeAddTitle').html = '<h1>' + deviceTypeName + ' > ' + Uni.I18n.translate('registerMappingAdd.addRegisterTypes', 'MDC', 'Add register types') + '</h1>';
                            me.getApplication().getMainController().showContent(widget);
                            me.createBreadCrumbsAddRegisterType(id, deviceTypeName);
                        }
                    });
                }
            });
    },

    createBreadCrumbs: function (deviceTypeId, deviceTypeName) {
        var me = this;

        var breadcrumbRegisterTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
            href: 'registertypes'
        });

        var breadcrumbDevicetype = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });

        var breadcrumbDeviceTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerMapping.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });

        breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbRegisterTypes);

        me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    createBreadCrumbsAddRegisterType: function (deviceTypeId, deviceTypeName) {
        var me = this;

        var breadcrumbAddRegisterTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerMappingAdd.addRegisterTypes', 'MDC', 'Add register types'),
            href: 'Add register types'
        });

        var breadcrumbRegisterTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
            href: 'registertypes'
        });

        var breadcrumbDevicetype = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });

        var breadcrumbDeviceTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerMapping.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });

        breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbRegisterTypes).setChild(breadcrumbAddRegisterTypes);

        me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record);
        widget.show();
    },

    addRegisterMappingsToDeviceType: function () {
        console.log('addRegisterMappingsToDeviceType');
        var me = this;
        var registerMappings = this.getRegisterMappingAddGrid().getSelectionModel().getSelection();

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
            success: function (deviceType) {
                console.log(deviceType);
                deviceType.registerTypes().add(registerMappings);
                deviceType.save({
                    callback: function () {
                        deviceType.commit();
                        me.getRegisterTypesOfDevicetypeStore().add(registerMappings);
                        location.href = '#setup/devicetypes/' + me.deviceTypeId + '/registertypes';
                    }
                });
            }
        });
    },

    removeRegisterMapping: function (grid, selectionmodel,id) {
        var me = this;
        var registerMappingToDelete = me.getRegisterMappingGrid().getSelectionModel().getSelection()[0];
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
            success: function (deviceType) {
                Ext.MessageBox.show({
                    msg: 'Are you sure you want to remove register type "' + registerMappingToDelete.get('name') + '"? <br />This action cannot be undone.',
                    title: 'Remove register type: "' + registerMappingToDelete.get('name') + '"',
                    config: {
                        registerMappingToDelete: registerMappingToDelete,
                        deviceType: deviceType,
                        me: me
                    },
                    buttons: Ext.MessageBox.YESNO,
                    fn: me.removeRegisterMappingFromDeviceType,
                    icon: Ext.MessageBox.WARNING
                });

            }
        });


    },
    removeRegisterMappingFromPreview: function () {
           var me = this;
           var registerMappingToDelete = me.getRegisterMappingGrid().getSelectionModel().getSelection()[0];
           Ext.ModelManager.getModel('Mdc.model.DeviceType').load(this.getRegisterMappingPreview().deviceTypeId, {
               success: function (deviceType) {
                   Ext.MessageBox.show({
                       msg: 'Are you sure you want to remove register type "' + registerMappingToDelete.get('name') + '"? <br />This action cannot be undone.',
                       title: 'Remove register type: "' + registerMappingToDelete.get('name') + '"',
                       config: {
                           registerMappingToDelete: registerMappingToDelete,
                           deviceType: deviceType,
                           me: me
                       },
                       buttons: Ext.MessageBox.YESNO,
                       fn: me.removeRegisterMappingFromDeviceType,
                       icon: Ext.MessageBox.WARNING
                   });

               }
           });


       },

    removeRegisterMappingFromDeviceType: function (btn, text, opt) {
        if (btn === 'yes') {
            var deviceType = opt.config.deviceType;
            var registerMappingToDelete = opt.config.registerMappingToDelete;
            var me = opt.config.me;
            deviceType.registerTypes().remove(registerMappingToDelete);
            deviceType.save({
                callback: function () {
                    me.getRegisterTypesOfDevicetypeStore().remove(registerMappingToDelete);
                    location.href = '#setup/devicetypes/' + deviceType.get('id') + '/registertypes';
                }
            });

        }
    }

});
