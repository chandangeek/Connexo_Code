Ext.define('Mdc.controller.setup.DeviceRegisterConfiguration', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.deviceregisterconfiguration.numerical.Preview',
        'setup.deviceregisterconfiguration.numerical.Detail',
        'setup.deviceregisterconfiguration.text.Preview',
        'setup.deviceregisterconfiguration.text.Detail',
        'setup.deviceregisterconfiguration.flags.Preview',
        'setup.deviceregisterconfiguration.flags.Detail',
        'setup.deviceregisterconfiguration.billing.Preview',
        'setup.deviceregisterconfiguration.billing.Detail',
        'setup.deviceregisterconfiguration.Setup',
        'setup.deviceregisterconfiguration.Grid'
    ],

    stores: [
        'RegisterConfigsOfDevice'
    ],

    refs: [
        {ref: 'deviceRegisterConfigurationGrid', selector: '#deviceRegisterConfigurationGrid'},
        {ref: 'deviceRegisterConfigurationSetup', selector: '#deviceRegisterConfigurationSetup'}
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
                widget.down('#stepsMenu').setTitle(device.get('mRID'));
            }
        });
    },

    onDeviceRegisterConfigurationGridSelect: function(rowmodel, record, index) {
        var me = this;
        me.previewRegisterConfiguration(record);
    },

    previewRegisterConfiguration: function (record) {
        var me = this,
            type = record.get('type'),
            widget = Ext.widget('deviceRegisterConfigurationPreview-' + type),
            form = widget.down('#deviceRegisterConfigurationPreviewForm'),
            previewContainer = me.getDeviceRegisterConfigurationSetup().down('#previewComponentContainer');

        form.loadRecord(record);
        widget.setTitle(record.get('name'));

        previewContainer.removeAll();
        previewContainer.add(widget);
    },

    showDeviceRegisterConfigurationDetailsView: function(mRID, registerId) {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        contentPanel.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
                var model = Ext.ModelManager.getModel('Mdc.model.Register');
                model.getProxy().setExtraParam('mRID', mRID);
                model.load(registerId, {
                    success: function (register) {
                        var type = register.get('type');
                        var widget = Ext.widget('deviceRegisterConfigurationDetail-' + type, {mRID:mRID, registerId:registerId});
                        var form = widget.down('#deviceRegisterConfigurationDetailForm');
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.getApplication().fireEvent('loadRegisterConfiguration', register);
                        form.loadRecord(register);
                        widget.down('#stepsMenu').setTitle(register.get('name'));
                    },
                    callback: function () {
                        contentPanel.setLoading(false);
                    }
                });
            }

        });
    }
});

