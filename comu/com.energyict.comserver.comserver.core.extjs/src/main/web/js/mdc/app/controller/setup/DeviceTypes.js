Ext.define('Mdc.controller.setup.DeviceTypes', {
    extend: 'Ext.app.Controller',

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
        {ref: 'deviceTypeEditForm', selector: '#deviceTypeEditForm'},
        {ref: 'deviceTypeRegisterLink', selector: '#deviceTypeRegistersLink'},
        {ref: 'deviceTypeLogBookLink', selector: '#deviceTypeLogBooksLink'},
        {ref: 'deviceTypeLoadProfilesLink', selector: '#deviceTypeLoadProfilesLink'},
        {ref: 'deviceTypeDetailForm',selector:'#deviceTypeDetailForm'},
        {ref: 'editDeviceTypeNameField',selector:'#editDeviceTypeNameField'}
    ],

    init: function () {
        this.control({
            '#devicetypegrid': {
                selectionchange: this.previewDeviceType
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
            '#deviceTypeDetail #deleteButtonFromDetails[action=deleteDeviceType]': {
                click: this.deleteDeviceTypeFromDetails
            },
            '#deviceTypeDetail #editButtonFromDetails[action=editDeviceType]': {
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
            this.getDeviceTypeLoadProfilesLink().getEl().setHTML(deviceTypes[0].get('loadProfileCount') + ' ' + I18n.translate('deviceType.loadProfiles', 'MDC', 'loadprofiles'));
            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);
            this.getDeviceTypePreview().getLayout().setActiveItem(1);
            this.getDeviceTypeDetailsLink().update('<a href="#/setup/devicetypes/' + deviceTypeId + '">'+ I18n.translate('general.viewDetails', 'MDC', 'View details')+'</a>');
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
        var widget = Ext.widget('deviceTypeEdit');
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                widget.down('form').loadRecord(deviceType);
                widget.setEdit(true, '#setup/devicetypes/' + deviceType.get('id'));
                Mdc.getApplication().getMainController().showContent(widget);
            }
        });


    },

    showDeviceTypeCreateView: function () {
        var widget = Ext.widget('deviceTypeEdit');
        widget.setEdit(false, '#setup/devicetypes/');
        Mdc.getApplication().getMainController().showContent(widget);
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

    editDeviceTypeFromDetails: function(){
        var record = this.getDeviceTypeDetailForm().getRecord();
        location.href = '#setup/devicetypes/' + record.get('id')+'/edit';

    },

    proposeDeviceTypeName: function(t,newValue){
        console.log('propostion');
        this.getEditDeviceTypeNameField().setValue(newValue);

    }
});
