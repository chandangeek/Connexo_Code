Ext.define('Mdc.controller.setup.DeviceGroups', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.view.setup.devicegroup.DeviceGroupsGrid',
        'Mdc.view.setup.devicegroup.DeviceGroupSetup',
        'Mdc.view.setup.devicegroup.DeviceGroupPreview'
    ],
    views: [
        'setup.devicegroup.DeviceGroupsGrid',
        'setup.devicegroup.DeviceGroupsGrid',
        'setup.devicegroup.DeviceGroupPreview'
    ],

    stores: [
        'DeviceGroups'
    ],

    mixins: [

    ],

    refs: [
        {ref: 'deviceGroupsGrid', selector: '#deviceGroupsGrid'},
        {ref: 'deviceGroupPreviewForm', selector: '#deviceGroupPreviewForm'},
        {ref: 'deviceGroupPreview', selector: '#deviceGroupPreview'},
        {ref: 'searchCriteriaContainer', selector: '#searchCriteriaContainer'}

    ],

    init: function () {
        this.control({
            '#deviceGroupsGrid': {
                selectionchange: this.previewDeviceGroup
            }
        });
    },

    showDeviceGroups: function () {
        var widget = Ext.widget('deviceGroupSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    previewDeviceGroup: function (grid, record) {
        var deviceGroups = this.getDeviceGroupsGrid().getSelectionModel().getSelection();
        if (deviceGroups.length == 1) {
            this.getDeviceGroupPreviewForm().loadRecord(deviceGroups[0]);
            this.getDeviceGroupPreview().setTitle(deviceGroups[0].get('name'));
        }
        this.getSearchCriteriaContainer().items.add(

        )
    },

    back: function () {
        location.href = "#devices";
    }
});

