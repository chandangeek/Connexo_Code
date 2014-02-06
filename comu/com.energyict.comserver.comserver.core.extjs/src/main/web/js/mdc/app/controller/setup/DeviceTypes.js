Ext.define('Mdc.controller.setup.DeviceTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.devicetype.DeviceTypesSetup',
        'setup.devicetype.DeviceTypesGrid',
        'setup.devicetype.DeviceTypePreview',
        'setup.devicetype.DeviceTypeDetail'
    ],

    stores: [
        'DeviceTypes'
    ],

    refs: [
        {ref: 'deviceTypeGrid', selector: '#devicetypegrid'},
        {ref: 'deviceTypePreviewForm', selector: '#deviceTypePreviewForm'},
        {ref: 'deviceTypePreview', selector: '#deviceTypePreview'},
        {ref: 'deviceTypeDetailsLink', selector: '#deviceTypeDetailsLink'},
        {ref: 'deviceTypeRegisterLink', selector: '#deviceTypeRegistersLink'},
        {ref: 'deviceTypeLogBookLink', selector: '#deviceTypeLogBooksLink'},
        {ref: 'deviceTypeLoadProfilesLink', selector: '#deviceTypeLoadProfilesLink'},
        {ref: 'deviceTypePreviewTitle', selector: '#deviceTypePreviewTitle'}
    ],

    init: function () {
        this.control({
            '#devicetypegrid': {
                selectionchange: this.previewDeviceType
            },
            '#devicetypegrid actioncolumn': {
                edit: this.editDeviceType,
                deleteItem: this.deleteDeviceType
            },
            '#deviceTypeSetup button[action = createDeviceType]': {
                click: this.createDeviceType
            },
            '#deviceTypePreview menuitem[action=editDeviceType]': {
                click: this.editDeviceType
            },
            '#deviceTypePreview menuitem[action=deleteDeviceType]': {
                click: this.deleteDeviceType
            }
        });
    },

    showEditView: function (id) {

    },

    previewDeviceType: function (grid, record) {
        var deviceTypes = this.getDeviceTypeGrid().getSelectionModel().getSelection();
        if (deviceTypes.length == 1) {
            this.getDeviceTypeRegisterLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypes[0].get('name') + '/registermappings'});
            this.getDeviceTypeRegisterLink().getEl().setHTML(deviceTypes[0].get('registerCount') + ' registers');
            this.getDeviceTypeLogBookLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypes[0].get('name') + '/logbooks'});
            this.getDeviceTypeLogBookLink().getEl().setHTML(deviceTypes[0].get('logBookCount') + ' logbooks');
            this.getDeviceTypeLoadProfilesLink().getEl().set({href: '#/setup/devicetypes/' + deviceTypes[0].get('name') + '/loadprofiles'});
            this.getDeviceTypeLoadProfilesLink().getEl().setHTML(deviceTypes[0].get('loadProfileCount') + ' loadprofiles');
            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);
            var deviceTypeName = this.getDeviceTypePreviewForm().form.findField('name').getSubmitValue();
            this.getDeviceTypePreview().show();
            this.getDeviceTypeDetailsLink().update('<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/setup/devicetypes/' + deviceTypeName + '">View details</a>');
            this.getDeviceTypePreviewTitle().update('<h4>' + deviceTypeName + '</h4>');
        } else {
            this.getDeviceTypePreview().hide();
        }
    },

    showDeviceTypeDetailsView: function (deviceType) {
        var me = this;
        var widget = Ext.widget('deviceTypeDetail');
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceType, {
            success: function (deviceType) {
                widget.down('form').loadRecord(deviceType);
                me.getDeviceTypePreviewTitle().update('<h4>' + deviceType.get('name') + ' Overview' + '</h4>');
            }
        });
        Mdc.getApplication().getMainController().showContent(widget);
    },

    createDeviceType: function () {
        location.href = '#setup/devicetypes/create';
    },

    editDeviceType: function () {
        location.href = '#setup/devicetypes/' + this.getDeviceTypeGrid().getSelectionModel().getSelection()[0].get('name') + '/edit';
    },

    deleteDeviceType: function () {
        var deviceTypeToDelete = this.getDeviceTypeGrid.getSelectionModel().getSelection()[0];
        deviceTypeToDelete.destroy();
    }

})
;
