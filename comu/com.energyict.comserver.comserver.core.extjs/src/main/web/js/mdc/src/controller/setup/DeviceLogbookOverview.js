Ext.define('Mdc.controller.setup.DeviceLogbookOverview', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.store.TimeUnits'
    ],

    stores: [
      'TimeUnits'
    ],

    views: [
        'Mdc.view.setup.devicelogbooks.Overview',
        'Mdc.view.setup.devicelogbooks.TabbedDeviceLogBookView'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.LogbookOfDevice'
    ],

    refs: [
        {
            ref: 'deviceLogBookDetailTitle',
            selector: '#deviceLogBookDetailTitle'
        }
    ],

    showOverview: function (mRID, logbookId, tabController) {
        var me = this,
            deviceModel = me.getModel('Mdc.model.Device'),
            logbookModel = me.getModel('Mdc.model.LogbookOfDevice'),
            widget;


            overview = Ext.widget('deviceLogbookOverview', {
                router: me.getController('Uni.controller.history.Router')
            });



        deviceModel.load(mRID, {
            success: function (record) {
                me.getApplication().fireEvent('loadDevice', record);
                widget = Ext.widget('tabbedDeviceLogBookView', {
                    device: record,
                    router: me.getController('Uni.controller.history.Router')
                });
                widget.setLoading(true);
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#logBook-specifications').add(overview);
                tabController.showTab(0);
                logbookModel.getProxy().setUrl(mRID);
                logbookModel.load(logbookId, {
                    success: function (record) {
                        me.getApplication().fireEvent('logbookOfDeviceLoad', record);
                        me.getDeviceLogBookDetailTitle().update('<H2>'+record.get('name')+'</H2>');
                        widget.down('#deviceLogbooksPreviewForm').loadRecord(record);
                        widget.setLoading(false);
                    }
                });
            }
        });


    }
});