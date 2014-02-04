Ext.define('Mdc.controller.setup.DeviceTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.devicetype.DeviceTypesSetup',
        'setup.devicetype.DeviceTypesGrid',
        'setup.devicetype.DeviceTypePreview'
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
            }
        });
    },

    showEditView: function (id) {

    },

    previewDeviceType: function(grid,record){
        var deviceTypes = this.getDeviceTypeGrid().getSelectionModel().getSelection();
        if (deviceTypes.length == 1) {
            this.getDeviceTypePreviewForm().loadRecord(deviceTypes[0]);
            var deviceTypeName = this.getDeviceTypePreviewForm().form.findField('name').getSubmitValue();
            this.getDeviceTypePreview().show();
            this.getDeviceTypeDetailsLink().update('<a style="font-family:VAGRoundedStdLight,Arial,Helvetica,Sans-Serif;color:#007dc3" href="#/setup/devicetypes/' + deviceTypeName + '">View details</a>');
            this.getDeviceTypePreviewTitle().update('<h4>' + deviceTypeName + '</h4>');
        } else {
            this.getDeviceTypePreview().hide();
        }
    }

})
;
