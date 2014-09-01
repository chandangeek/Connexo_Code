Ext.define('Mdc.controller.setup.DeviceLogbookOverview', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicelogbooks.Overview'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LogbookOfDevice'
    ],

    showOverview: function (mRID, logbookId) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            widget = Ext.widget('deviceLogbookOverview', {
                router: me.getController('Uni.controller.history.Router')
            });

        me.getApplication().fireEvent('changecontentevent', widget);

        widget.setLoading(true);
        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
            }
        });

        logbookModel.getProxy().setUrl(mRID);
        logbookModel.load(logbookId, {
            success: function (record) {
                me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                widget.down('#deviceLogbooksPreviewForm').loadRecord(record);
                widget.down('#deviceLogbookSubMenuPanel').setParams(mRID, record);
                widget.setLoading(false);
            }
        });
    }
});