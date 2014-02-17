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
        'Mdc.store.RegisterMappings',
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'RegisterMappings',
        'RegisterMappingsNotPartOfDeviceType'
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

    init: function () {

        this.control({
            '#registermappinggrid': {
                selectionchange: this.previewRegisterMapping
            },
            '#registerMappingSetup button[action = addRegisterMapping]': {
                click: this.addRegisterMappingHistory
            },
            '#registermappinggrid actioncolumn': {
                showReadingTypeInfo: this.showReadingType
            },
            '#addButton[action=addRegisterMappingAction]': {
                click: this.addRegisterMappingsToDeviceType
            },
            '#registerMappingPreviewForm button[action = showReadingTypeInfo]': {
                showReadingTypeInfo: this.showReadingType
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
        this.getRegisterMappingsStore().getProxy().setExtraParam('deviceType', id);
        var widget = Ext.widget('registerMappingsSetup', {deviceTypeId: id});
        this.getAddRegisterMappingBtn().href = '#/setup/devicetypes/' + id + '/registertypes/add';
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
            success: function (deviceType) {
                var deviceTypeName = deviceType.get('name');
                widget.down('#registerTypeTitle').html = '<h1>' + deviceTypeName + ' > ' + I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types') + '</h1>';
                Mdc.getApplication().getMainController().showContent(widget);
                me.createBreadCrumbs(id, deviceTypeName);
            }
        });
    },

    addRegisterMappings: function (id) {
        var me = this;
        var widget = Ext.widget('registerMappingAdd', {deviceTypeId: id});
        console.log('add register mappings ');
        console.log(id);
        this.getRegisterMappingsNotPartOfDeviceTypeStore().getProxy().setExtraParam('deviceType', id);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
            success: function (deviceType) {
                var deviceTypeName = deviceType.get('name');
                widget.down('#registerTypeAddTitle').html = '<h1>' + deviceTypeName + ' > ' + I18n.translate('registerMappingAdd.addRegisterTypes', 'MDC', 'Add register types') + '</h1>';
                Mdc.getApplication().getMainController().showContent(widget);
                me.createBreadCrumbsAddRegisterType(id, deviceTypeName);
            }
        });
    },

    createBreadCrumbs: function (deviceTypeId, deviceTypeName) {
        var me = this;

        var breadcrumbRegisterTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
            href: 'registertypes'
        });

        var breadcrumbDevicetype = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });

        var breadcrumbDeviceTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('registerMapping.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });

        breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbRegisterTypes);

        me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    createBreadCrumbsAddRegisterType: function (deviceTypeId, deviceTypeName) {
        var me = this;

        var breadcrumbAddRegisterTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('registerMappingAdd.addRegisterTypes', 'MDC', 'Add register types'),
            href: 'Add register types'
        });

        var breadcrumbRegisterTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
            href: 'registertypes'
        });

        var breadcrumbDevicetype = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });

        var breadcrumbDeviceTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('registerMapping.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });

        breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbRegisterTypes).setChild(breadcrumbAddRegisterTypes);

        me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    showReadingType: function (record) {
        console.log('show reading type info');
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record);
        widget.show();
    },

    addRegisterMappingsToDeviceType: function () {
        var registerMappings = this.getRegisterMappingAddGrid().getSelectionModel().getSelection();
        console.log(registerMappings);

    }

});
