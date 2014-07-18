Ext.define('Mdc.controller.setup.DeviceRegisterConfiguration', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterconfiguration.DeviceRegisterConfigurationSetup',
        'setup.deviceregisterconfiguration.DeviceRegisterConfigurationGrid',
        'setup.deviceregisterconfiguration.DeviceRegisterConfigurationPreview',
        'setup.deviceregisterconfiguration.DeviceRegisterConfigurationDetail'
    ],

    stores: [
        'RegisterConfigsOfDevice'
    ],

    refs: [
        {ref: 'deviceRegisterConfigurationGrid', selector: '#deviceRegisterConfigurationGrid'},
        {ref: 'deviceRegisterConfigurationPreviewForm', selector: '#deviceRegisterConfigurationPreviewForm'},
        {ref: 'deviceRegisterConfigurationDetailForm', selector: '#deviceRegisterConfigurationDetailForm'},
        {ref: 'deviceRegisterConfigurationPreview', selector: '#deviceRegisterConfigurationPreview'},
        {ref: 'deviceRegisterConfigurationMenu', selector: '#deviceRegisterConfigurationMenu'},
        {ref: 'previewMrId', selector: '#preview_mrid'}
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

        form.loadRecord(record);
        me.getDeviceRegisterConfigurationPreview().setTitle(record.get('name'));
        me.getPreviewMrId().setValue(record.getReadingType().get('mrid'));
    },

    showDeviceRegisterConfigurationDetailsView: function(mRID, registerId) {
        var me = this,
            widget = Ext.widget('deviceRegisterConfigurationDetail', {mRID:mRID, registerId:registerId});

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', mRID);
                model.load(registerId, {
                    success: function (register) {
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        me.getDeviceRegisterConfigurationDetailForm().loadRecord(register);
                        widget.down('#stepsMenu').setTitle(register.get('name'));
                    },
                    callback: function () {
                        widget.setLoading(false);
                    }
                });
            }

        });
    }
});

