Ext.define('Mdc.controller.setup.DeviceTypes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    views: [
        'setup.devicetype.DeviceTypesSetup',
        'setup.devicetype.DeviceTypesGrid',
        'setup.devicetype.DeviceTypePreview',
        'setup.devicetype.DeviceTypeDetail',
        'setup.devicetype.DeviceTypeEdit'
    ],

    stores: [
        'DeviceTypes',
        'DeviceCommunicationProtocols'
    ],

    refs: [
        {ref: 'deviceTypeGrid', selector: '#devicetypegrid'},
        {ref: 'deviceTypePreviewForm', selector: '#deviceTypePreviewForm'},
        {ref: 'deviceTypePreview', selector: '#deviceTypePreview'},
        {ref: 'deviceTypeDetailsLink', selector: '#deviceTypeDetailsLink'},
        {ref: 'deviceTypePreviewTitle', selector: '#deviceTypePreviewTitle'},
        {ref: 'deviceTypeEditView', selector: '#deviceTypeEdit'},
        {ref: 'deviceTypeEditForm', selector: '#deviceTypeEditForm'},
        {ref: 'deviceTypeRegisterLink', selector: '#deviceTypeRegistersLink'},
        {ref: 'deviceTypeLogBookLink', selector: '#deviceTypeLogBooksLink'},
        {ref: 'deviceConfigurationsLink', selector: '#deviceConfigurationsLink'},
        {ref: 'deviceTypeLoadProfilesLink', selector: '#deviceTypeLoadProfilesLink'},
        {ref: 'deviceTypeDetailForm',selector:'#deviceTypeDetailForm'},
        {ref: 'editDeviceTypeNameField',selector:'#editDeviceTypeNameField'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'}
    ],

    init: function () {
        this.control({
            '#devicetypegrid': {
                selectionchange: this.previewDeviceType
            },
            '#deviceTypeSetup breadcrumbTrail': {
                afterrender: this.overviewBreadCrumb
            },
            '#devicetypegrid actioncolumn':{
                editItem: this.editDeviceTypeHistory,
                deleteItem: this.deleteDeviceType
            },
            '#deviceTypeSetup button[action = createDeviceType]': {
                click: this.createDeviceTypeHistory
            },
            '#deviceTypePreview menuitem[action=editDeviceType]': {
                click: this.editDeviceTypeHistory
            },
            '#deviceTypePreview menuitem[action=deleteDeviceType]': {
                click: this.deleteDeviceType
            },
            '#createEditButton[action=createDeviceType]': {
                click: this.createDeviceType
            },
            '#createEditButton[action=editDeviceType]': {
                click: this.editDeviceType
            },
            '#deviceTypeDetail menuitem[action=deleteDeviceType]': {
                click: this.deleteDeviceTypeFromDetails
            },
            '#deviceTypeDetail menuitem[action=editDeviceType]': {
                click: this.editDeviceTypeFromDetails
            },
            '#deviceTypeEdit #communicationProtocolComboBox': {
                change: this.proposeDeviceTypeName
            }
        });
    },

    showEditView: function (id) {

    },

    previewDeviceType: function (grid, record) {
        var deviceTypes = this.getDeviceTypeGrid().getSelectionModel().getSelection();
        if (deviceTypes.length == 1) {
            var deviceTypeId = deviceTypes[0].get('id');
            this.getDeviceTypeRegisterLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/registermappings'});
            this.getDeviceTypeRegisterLink().getEl().setHTML(deviceTypes[0].get('registerCount') + ' ' + I18n.translate('devicetype.registers', 'MDC', 'registers'));
            this.getDeviceTypeLogBookLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/logbooks'});
            this.getDeviceTypeLogBookLink().getEl().setHTML(deviceTypes[0].get('logBookCount') + ' '+ I18n.translate('devicetype.logbooks', 'MDC', 'logbooks'));
            this.getDeviceTypeLoadProfilesLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/loadprofiles'});
            this.getDeviceTypeLoadProfilesLink().getEl().setHTML(deviceTypes[0].get('loadProfileCount') + ' ' + I18n.translate('devicetype.loadprofiles', 'MDC', 'loadprofiles'));
            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);
            this.getDeviceTypePreview().getLayout().setActiveItem(1);
            this.getDeviceTypeDetailsLink().update('<a href="#/setup/devicetypes/' + deviceTypeId + '">'+ I18n.translate('general.viewDetails', 'MDC', 'View details')+'</a>');
            this.getDeviceConfigurationsLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/configurations'});
            this.getDeviceConfigurationsLink().getEl().setHTML(deviceTypes[0].get('deviceConfigurationCount') + ' ' + I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations'));
            this.getDeviceTypePreviewTitle().update('<h4>' + deviceTypes[0].get('name') + '</h4>');
        } else {
            this.getDeviceTypePreview().getLayout().setActiveItem(0);
        }
    },

    showDeviceTypeDetailsView: function (deviceType) {
        var me = this;
        var widget = Ext.widget('deviceTypeDetail');
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceType, {
            success: function (deviceType) {
                me.detailBreadCrumb(deviceType.get('name'),deviceType.get('id'));
                widget.down('form').loadRecord(deviceType);
                me.getDeviceTypePreviewTitle().update('<h4>' + deviceType.get('name') + ' ' + I18n.translate('general.overview', 'MDC', 'Overview') + '</h4>');
            }
        });
        Mdc.getApplication().getMainController().showContent(widget);
    },

    createDeviceTypeHistory: function () {
        location.href = '#setup/devicetypes/create';
    },

    editDeviceTypeHistory: function () {
        location.href = '#setup/devicetypes/' + this.getDeviceTypeGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    deleteDeviceType: function () {
        var deviceTypeToDelete = this.getDeviceTypeGrid.getSelectionModel().getSelection()[0];
        deviceTypeToDelete.destroy();
    },

    deleteDeviceTypeFromDetails: function () {
        var deviceTypeToDelete = this.getDeviceTypeDetailForm().getRecord();
        deviceTypeToDelete.destroy({
            callback: function(){
                location.href = '#setup/devicetypes/';
            }
        });
    },

    showDeviceTypeEditView: function (deviceTypeId) {
        var protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols');
        var widget = Ext.widget('deviceTypeEdit',{
            edit: true,
            returnLink: '#setup/devicetypes/' + deviceTypeId,
            deviceCommunicationProtocols: protocolStore
        });
        Mdc.getApplication().getMainController().showContent(widget);
        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.editBreadCrumb(deviceType.get('name'),deviceTypeId)
                protocolStore.load({
                    callback: function(store){
                        widget.down('form').loadRecord(deviceType);
                        widget.setLoading(false);
                    }
                })
            }
        });


    },

    showDeviceTypeCreateView: function () {
        var protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols');
        var widget = Ext.widget('deviceTypeEdit',{
            edit: false,
            returnLink: '#setup/devicetypes/',
            deviceCommunicationProtocols: protocolStore
        });
        this.createBreadCrumb();

        Mdc.getApplication().getMainController().showContent(widget);
        widget.setLoading(true);
        protocolStore.load({
            callback: function(store){
                widget.setLoading(false);
            }
        });
    },

    createDeviceType: function () {
        var record = Ext.create(Mdc.model.DeviceType),
            values = this.getDeviceTypeEditForm().getValues();
        if (record) {
            record.set(values);
            record.save({
                callback: function (record) {
                    location.href = '#setup/devicetypes/' + record.get('id');
                }
            });

        }
    },

    editDeviceType: function () {
        var record = this.getDeviceTypeEditForm().getRecord(),
            values = this.getDeviceTypeEditForm().getValues();
        if (record) {
            record.set(values);
            record.save({
                callback: function (record) {
                    location.href = '#setup/devicetypes/' + record.get('id');
                }
            });

        }
    },

    overviewBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    createBreadCrumb: function(){
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('general.create', 'MDC', 'Create'),
            href: 'create'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editBreadCrumb: function(deviceTypeName, deviceTypeId){
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('general.edit', 'MDC', 'Edit'),
            href: 'edit'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    detailBreadCrumb: function(deviceTypeName, deviceTypeId){
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editDeviceTypeFromDetails: function(){
        var record = this.getDeviceTypeDetailForm().getRecord();
        location.href = '#setup/devicetypes/' + record.get('id')+'/edit';
    },

    proposeDeviceTypeName: function(t,newValue){
        if(!this.getDeviceTypeEditView().isEdit()){
            this.getEditDeviceTypeNameField().setValue(newValue);
        }

    }
});
