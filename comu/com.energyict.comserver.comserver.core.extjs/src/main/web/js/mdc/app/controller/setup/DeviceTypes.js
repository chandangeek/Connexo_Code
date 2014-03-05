Ext.define('Mdc.controller.setup.DeviceTypes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
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
        {ref: 'deviceTypeDetailRegistersLink', selector: '#deviceTypeDetailRegistersLink'},
        {ref: 'deviceTypeLogBookLink', selector: '#deviceTypeLogBooksLink'},
        {ref: 'deviceTypeDetailLogBookLink', selector: '#deviceTypeDetailLogBooksLink'},
        {ref: 'deviceConfigurationsLink', selector: '#deviceConfigurationsLink'},
        {ref: 'deviceConfigurationsDetailLink', selector: '#deviceConfigurationsDetailLink'},
        {ref: 'deviceTypeLoadProfilesLink', selector: '#deviceTypeLoadProfilesLink'},
        {ref: 'deviceTypeDetailLoadProfilesLink', selector: '#deviceTypeDetailLoadProfilesLink'},
        {ref: 'deviceTypeDetailForm', selector: '#deviceTypeDetailForm'},
        {ref: 'editDeviceTypeNameField', selector: '#editDeviceTypeNameField'},
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
            '#devicetypegrid actioncolumn': {
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
            this.getDeviceTypeRegisterLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/registertypes'});
            this.getDeviceTypeRegisterLink().getEl().setHTML(deviceTypes[0].get('registerCount') + ' ' + Uni.I18n.translate('devicetype.registers', 'MDC', 'register types'));
            this.getDeviceTypeLogBookLink().getEl().set({hrbef: '#/setup/devicetypes/' + deviceTypeId + '/logbooks'});
            this.getDeviceTypeLogBookLink().getEl().setHTML(deviceTypes[0].get('logBookCount') + ' ' + Uni.I18n.translate('devicetype.logbooks', 'MDC', 'logbook types'));
            this.getDeviceTypeLoadProfilesLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/loadprofiles'});
            this.getDeviceTypeLoadProfilesLink().getEl().setHTML(deviceTypes[0].get('loadProfileCount') + ' ' + Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'load profile types'));
            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);
            this.getDeviceTypePreview().getLayout().setActiveItem(1);
            this.getDeviceTypeDetailsLink().update('<a href="#/setup/devicetypes/' + deviceTypeId + '">' + Uni.I18n.translate('general.viewDetails', 'MDC', 'View details') + '</a>');
            this.getDeviceConfigurationsLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/configurations'});
            this.getDeviceConfigurationsLink().getEl().setHTML(deviceTypes[0].get('deviceConfigurationCount') + ' ' + Uni.I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations'));
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
                var deviceTypeId = deviceType.get('id');
                me.detailBreadCrumb(deviceType.get('name'), deviceTypeId);

                me.getDeviceTypeDetailRegistersLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/registertypes'});
                me.getDeviceTypeDetailRegistersLink().getEl().setHTML(deviceType.get('registerCount') + ' ' + Uni.I18n.translate('devicetype.registers', 'MDC', 'register types'));
                me.getDeviceTypeDetailLogBookLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/logbooks'});
                me.getDeviceTypeDetailLogBookLink().getEl().setHTML(deviceType.get('logBookCount') + ' ' + Uni.I18n.translate('devicetype.logbooks', 'MDC', 'logbook types'));
                me.getDeviceTypeDetailLoadProfilesLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/loadprofiles'});
                me.getDeviceTypeDetailLoadProfilesLink().getEl().setHTML(deviceType.get('loadProfileCount') + ' ' + Uni.I18n.translate('devicetype.loadprofiles', 'MDC', 'loadprofile types'));
                me.getDeviceConfigurationsDetailLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypeId + '/configurations'});
                me.getDeviceConfigurationsDetailLink().getEl().setHTML(deviceType.get('deviceConfigurationCount') + ' ' + Uni.I18n.translate('devicetype.deviceconfigurations', 'MDC', 'device configurations'));
                widget.down('form').loadRecord(deviceType);

                me.getDeviceTypePreviewTitle().update('<h1>' + deviceType.get('name') + ' - ' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h1>');
            }
        });
        this.getApplication().getMainController().showContent(widget);
    },

    createDeviceTypeHistory: function () {
        location.href = '#setup/devicetypes/create';
    },

    editDeviceTypeHistory: function () {
        location.href = '#setup/devicetypes/' + this.getDeviceTypeGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    deleteDeviceType: function () {
        var deviceTypeToDelete = this.getDeviceTypeGrid().getSelectionModel().getSelection()[0];
        deviceTypeToDelete.destroy({
            callback: function () {
                location.href = '#setup/devicetypes/';
            }
        });
    },

    deleteDeviceTypeFromDetails: function () {
        var deviceTypeToDelete = this.getDeviceTypeDetailForm().getRecord();
        deviceTypeToDelete.destroy({
            callback: function () {
                location.href = '#setup/devicetypes/';
            }
        });
    },

    showDeviceTypeEditView: function (deviceTypeId) {
        var protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols');
        var me=this;
        var widget = Ext.widget('deviceTypeEdit', {
            edit: true,
            returnLink: me.getApplication().getHistorySetupController().tokenizePreviousTokens(),
            deviceCommunicationProtocols: protocolStore
        });
        this.getApplication().getMainController().showContent(widget);
        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.editBreadCrumb(deviceType.get('name'), deviceTypeId)
                protocolStore.load({
                    callback: function (store) {
                        widget.down('form').loadRecord(deviceType);
                        widget.down('#deviceTypeEditCreateTitle').update('<H2>'+Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' "' + deviceType.get('name')+'"</H2>');
                        widget.setLoading(false);
                    }
                })
            }
        });


    },

    showDeviceTypeCreateView: function () {
        var protocolStore = Ext.StoreManager.get('DeviceCommunicationProtocols');
        var widget = Ext.widget('deviceTypeEdit', {
            edit: false,
            returnLink: '#setup/devicetypes/',
            deviceCommunicationProtocols: protocolStore
        });
        var me= this;
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        protocolStore.load({
            callback: function (store) {
                widget.down('#deviceTypeEditCreateTitle').update('<H2>'+Uni.I18n.translate('general.create', 'MDC', 'Create') + ' ' + 'device type'+'</H2>');
                me.createBreadCrumb();
                widget.setLoading(false);
            }
        });
    },

    createDeviceType: function () {
        var me=this;
        var record = Ext.create(Mdc.model.DeviceType),
            values = this.getDeviceTypeEditForm().getValues();
        if (record) {
            record.set(values);
            record.save({
                success: function (record) {
                    location.href = '#setup/devicetypes/' + record.get('id');
                },
                failure: function(record,operation){
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceTypeEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });

        }
    },

    editDeviceType: function () {
        var record = this.getDeviceTypeEditForm().getRecord(),
            values = this.getDeviceTypeEditForm().getValues();
        var me=this;
        if (record) {
            record.set(values);
            record.save({
                callback: function (record) {
                    location.href = me.getApplication().getHistorySetupController().tokenizePreviousTokens();
                }
            });

        }
    },

    overviewBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    createBreadCrumb: function () {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
            href: 'create'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editBreadCrumb: function (deviceTypeName, deviceTypeId) {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' "' + deviceTypeName + '"',
            href: 'edit'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    detailBreadCrumb: function (deviceTypeName, deviceTypeId) {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem',{
            text: Uni.I18n.translate('general.overview', 'MDC', 'Overview') + ' "' + deviceTypeName + '"'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editDeviceTypeFromDetails: function () {
        var record = this.getDeviceTypeDetailForm().getRecord();
        location.href = '#setup/devicetypes/' + record.get('id') + '/edit';
    },

    proposeDeviceTypeName: function (t, newValue) {
        if (!this.getDeviceTypeEditView().isEdit()) {
            this.getEditDeviceTypeNameField().setValue(newValue);
        }

    }
});
