Ext.define('Mdc.controller.setup.DeviceLogbookData', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicelogbooks.Data'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LogbookOfDevice'
    ],

    stores: [
        'Mdc.store.LogbookOfDeviceData'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'deviceLogbookData'
        }
    ],

    loadProfileModel: null,

    init: function () {
        this.control({
            'deviceLogbookData #deviceLogbookDataGrid': {
                select: this.showPreview
            }
        });
    },

    showOverview: function (mRID, logbookId) {
        var me = this,
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            dataStore = me.getStore('Mdc.store.LogbookOfDeviceData'),
            widget;

        dataStore.getProxy().setUrl({
            mRID: mRID,
            logbookId: logbookId
        });

        widget = Ext.widget('deviceLogbookData', {
            router: me.getController('Uni.controller.history.Router')
        });
        me.getApplication().fireEvent('changecontentevent', widget);

        me.getModel('Mdc.model.Device').load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
            }
        });

        logbookModel.getProxy().setUrl(mRID);
        logbookModel.load(logbookId, {
            success: function (record) {
                me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                widget.down('#deviceLogbookSubMenuPanel').setParams(mRID, record);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPage().down('#deviceLogbookDataPreview');

        preview.setTitle(Uni.I18n.formatDate('devicelogbooks.eventDate.dateFormat', record.get('eventDate'), 'MDC', 'M d, Y H:i:s'));
        preview.down('#deviceLogbookDataPreviewForm').loadRecord(record);
    }
});