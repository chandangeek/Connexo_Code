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
        {ref: 'addRegisterMappingBtn', selector: '#addRegisterMappingBtn'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'}
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
                showReadingTypeInfo: this.showReadingTypeInfo
            }
        });
    },

    showEditView: function (id) {

    },

    addRegisterMappingHistory: function () {
        location.href = this.getAddRegisterMappingBtn().href;
    },

    previewRegisterMapping: function (grid, record) {
        console.log('preview register mapping');
        console.log(record);
        console.log(grid);
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
        this.getAddRegisterMappingBtn().href = '#/setup/devicetypes/' + id + '/registermappings/add';
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
        console.log('adding register mappings');
        var widget = Ext.widget('registerMappingAdd');
        Mdc.getApplication().getMainController().showContent(widget);
    },

    createBreadCrumbs: function (deviceTypeId, deviceTypeName) {
        var me = this;

        var breadcrumbRegisterTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types'),
            href: 'registermappings'
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

    showReadingTypeInfo: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record);
        widget.show();
    }

});
