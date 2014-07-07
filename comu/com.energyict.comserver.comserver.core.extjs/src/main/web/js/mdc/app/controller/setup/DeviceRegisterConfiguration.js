Ext.define('Mdc.controller.setup.DeviceRegisterConfiguration', {
    extend: 'Ext.app.Controller',
    mRID: null,

    views: [
        'setup.deviceregisterconfiguration.DeviceRegisterConfigurationSetup',
        'setup.deviceregisterconfiguration.DeviceRegisterConfigurationGrid',
        'setup.deviceregisterconfiguration.DeviceRegisterConfigurationPreview'
    ],

    stores: [
        'RegisterConfigsOfDevice'
    ],

    refs: [
        {ref: 'deviceRegisterConfigurationGrid', selector: '#deviceRegisterConfigurationGrid'},
        {ref: 'deviceRegisterConfigurationPreviewForm', selector: '#deviceRegisterConfigurationPreviewForm'},
        {ref: 'deviceRegisterConfigurationPreview', selector: '#deviceRegisterConfigurationPreview'}
    ],

    init: function () {
        var me = this;
        me.control({
            '#deviceRegisterConfigurationGrid': {
                select: me.onDeviceRegisterConfigurationGridSelect
            }
        });
    },

    showDeviceRegisterConfigurationsView: function (mRID) {
        var me = this;
        me.mRID = mRID;
        var widget = Ext.widget('deviceRegisterConfigurationSetup', {mRID: mRID});
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getDeviceRegisterConfigurationGrid().getSelectionModel().select(0);
            }

        });
    },

    onDeviceRegisterConfigurationGridSelect: function(rowmodel, record, index) {
        var me = this;
        me.previewRegisterConfiguration(record);
    },

    previewRegisterConfiguration: function (record) {
        var me = this,
            form = me.getDeviceRegisterConfigurationPreviewForm();

        me.getDeviceRegisterConfigurationPreview().setTitle(record.getData().name);
        form.loadRecord(record);
    }
});

