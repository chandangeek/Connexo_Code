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
        'DeviceTypes'
    ],

    refs: [
        {ref: 'deviceTypeGrid',selector: '#devicetypegrid'},
        {ref: 'deviceTypePreviewForm',selector: '#deviceTypePreviewForm'},
        {ref: 'deviceTypePreview',selector: '#deviceTypePreview'},
        {ref: 'deviceTypeDetailsLink',selector: '#deviceTypeDetailsLink'},
        {ref: 'deviceTypePreviewTitle',selector: '#deviceTypePreviewTitle'}
    ],

    init: function () {
        this.control({
            '#devicetypegrid': {
                selectionchange: this.previewDeviceType
            },
            '#devicetypegrid actioncolumn':{
                edit: this.editDeviceTypeHistory,
                delete: this.deleteDeviceType
            },
            '#deviceTypeSetup button[action = createDeviceType]':{
                click: this.createDeviceTypeHistory
            },
            '#deviceTypePreview menuitem[action=editDeviceType]':{
                click: this.editDeviceTypeHistory
            },
            '#deviceTypePreview menuitem[action=deleteDeviceType]':{
                click: this.deleteDeviceType
            }
        });
    },

    showEditView: function (id) {

    },

    previewDeviceType: function(grid,record){
        var deviceTypes = this.getDeviceTypeGrid().getSelectionModel().getSelection();
        if (deviceTypes.length == 1) {
            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);
            var deviceTypeId = deviceTypes[0].get('id');
            this.getDeviceTypePreview().show();
            this.getDeviceTypeDetailsLink().update('<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/setup/devicetypes/' + deviceTypeId + '">View details</a>');
            this.getDeviceTypePreviewTitle().update('<h4>' + deviceTypeName + '</h4>');
        } else {
            this.getDeviceTypePreview().hide();
        }
    },

    showDeviceTypeDetailsView: function(deviceType){
        var me= this;
        var widget = Ext.widget('deviceTypeDetail');
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceType, {
            success: function (deviceType) {
                widget.down('form').loadRecord(deviceType);
                me.getDeviceTypePreviewTitle().update('<h4>' + deviceType.get('name') + ' Overview' + '</h4>');
            }
        });
        Mdc.getApplication().getMainController().showContent(widget);
    },

    createDeviceTypeHistory: function(){
        location.href = '#setup/devicetypes/create';
    },

    editDeviceTypeHistory: function(){
        location.href = '#setup/devicetypes/' + this.getDeviceTypeGrid().getSelectionModel().getSelection()[0].get('name')+'/edit';
    },

    deleteDeviceType: function(){
      var deviceTypeToDelete = this.getDeviceTypeGrid.getSelectionModel().getSelection()[0];
        deviceTypeToDelete.destroy();
    },

    showDeviceTypeEditView: function(deviceType){
        var widget = Ext.widget('deviceTypeEdit');
        Mdc.getApplication().getMainController().showContent(widget);
    },

    showDeviceTypeCreateView: function(){
        var widget = Ext.widget('deviceTypeEdit');
        Mdc.getApplication().getMainController().showContent(widget);
    }

})
;
