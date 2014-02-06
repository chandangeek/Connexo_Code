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
        {ref: 'deviceTypeGrid',selector: '#devicetypegrid'},
        {ref: 'deviceTypePreviewForm',selector: '#deviceTypePreviewForm'},
        {ref: 'deviceTypePreview',selector: '#deviceTypePreview'},
        {ref: 'deviceTypeDetailsLink',selector: '#deviceTypeDetailsLink'},
        {ref: 'deviceTypePreviewTitle',selector: '#deviceTypePreviewTitle'},
        {ref: 'deviceTypeEditForm',selector: '#deviceTypeEditForm'}
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
            },
            '#createEditButton[action=createDeviceType]':{
                click: this.createDeviceType
            },
            '#createEditButton[action=editDeviceType]':{
                click: this.editDeviceType
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
            this.getDeviceTypePreviewTitle().update('<h4>' + deviceTypes[0].get('name') + '</h4>');
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
        location.href = '#setup/devicetypes/' + this.getDeviceTypeGrid().getSelectionModel().getSelection()[0].get('id')+'/edit';
    },

    deleteDeviceType: function(){
      var deviceTypeToDelete = this.getDeviceTypeGrid.getSelectionModel().getSelection()[0];
        deviceTypeToDelete.destroy();
    },

    showDeviceTypeEditView: function(deviceTypeId){
        var widget = Ext.widget('deviceTypeEdit');
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                widget.down('form').loadRecord(deviceType);
                widget.setEdit(true,'#setup/devicetypes/'+deviceType.get('id'));
                Mdc.getApplication().getMainController().showContent(widget);
            }
        });


    },

    showDeviceTypeCreateView: function(){
        var widget = Ext.widget('deviceTypeEdit');
        widget.setEdit(false,'#setup/devicetypes/');
        Mdc.getApplication().getMainController().showContent(widget);
    },

    createDeviceType: function(){
        var record = Ext.create(Mdc.model.DeviceType),
            values = this.getDeviceTypeEditForm().getValues();
        if (record) {
            record.set(values);
            record.save({
                callback: function (record) {
                    location.href = '#setup/devicetypes/'+record.get('id');
                }
            });

        }
    },

    editDeviceType: function(){
        var record = this.getDeviceTypeEditForm().getRecord(),
            values = this.getDeviceTypeEditForm().getValues();
        if (record) {
            record.set(values);
            record.save({
                callback: function (record) {
                    location.href = '#setup/devicetypes/'+record.get('id');
                }
            });

        }
    }
});
