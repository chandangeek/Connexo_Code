Ext.define('Mdc.usagepointmanagement.controller.UsagePointHistory', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.usagepointmanagement.view.history.UsagePointHistory',
        'Mdc.usagepointmanagement.view.history.UsagePointHistoryDevices'
    ],

    models: [
        'Mdc.usagepointmanagement.model.UsagePointHistoryDevice',
        'Mdc.usagepointmanagement.model.UsagePoint'
    ],

    stores: [
        'Mdc.usagepointmanagement.store.UsagePointHistoryDevices'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'usage-point-history'
        },
        {
            ref: 'devicesPage',
            selector: 'usage-point-history usage-point-history-devices'
        }
    ],

    init: function () {
        this.control({
            'usage-point-history-devices usage-point-history-devices-grid': {
                select: this.showDevicePreview
            }
        });
    },

    showUsagePointHistory: function (id, tab) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];            

        if (!tab) {
            router.getRoute('usagepoints/usagepoint/history').forward({tab: 'devices'});
        } else {
            pageMainContent.setLoading();
            me.getModel('Mdc.usagepointmanagement.model.UsagePoint').load(id, {
                success: function (record) {
                    app.fireEvent('usagePointLoaded', record);
                    var widget = Ext.widget('usage-point-history', {
                        itemId: 'usage-point-history',
                        router: router, 
                        mRID: record.get('mRID'),
                        activeTab: tab,
                        controller: me
                    });
                    app.fireEvent('changecontentevent', widget);
                },
                callback: function () {
                    pageMainContent.setLoading(false);
                }
            });            
        }
    },

    showDevicesTab: function (panel) {
        var me = this,
            store = me.getStore('Mdc.usagepointmanagement.store.UsagePointHistoryDevices'),
            router = me.getController('Uni.controller.history.Router');

        Uni.util.History.suspendEventsForNextCall();
        Uni.util.History.setParsePath(false);
        router.getRoute('usagepoints/usagepoint/history').forward({tab: 'devices'});

        store.getProxy().extraParams = {
            mRID: router.arguments.usagePointId
        };
        store.load(function () {
            Ext.suspendLayouts();
            panel.removeAll();
            if (store.getCount()) {
                panel.add({
                    xtype: 'usage-point-history-devices',
                    itemId: 'usage-point-history-devices',
                    router: router
                });
            } else {
                panel.add({
                    xtype: 'form',
                    items: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('usagePoint.history.devices.emptyCmp.title', 'MDC', 'No devices have been linked to this usage point yet')
                    }
                });
            }
            Ext.resumeLayouts(true);
        });
    },

    showDevicePreview: function (selectionModel, record) {
        var me = this;
        
        Ext.suspendLayouts();
        me.getDevicesPage().down('usage-point-history-devices-preview').setTitle(record.get('mRID'));
        me.getDevicesPage().down('#usage-point-history-devices-preview-form').loadRecord(record);
        Ext.resumeLayouts(true);        
    }
});

