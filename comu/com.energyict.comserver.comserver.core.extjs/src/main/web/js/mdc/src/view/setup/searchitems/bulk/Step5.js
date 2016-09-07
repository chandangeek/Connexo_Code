Ext.define('Mdc.view.setup.searchitems.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step5',
    title: Uni.I18n.translate('searchItems.bulk.step5title', 'MDC', 'Bulk action - Step 5 of 5: Status'),
    ui: 'large',
    name: 'statusPage',
    defaults: {
        margin: '0 0 8 0'
    },
    tbar: {
        xtype: 'panel',
        ui: 'medium',
        style: {
            padding: '0 0 0 3px'
        },
        title: '',
        itemId: 'searchitemsbulkactiontitle'
    },

    showChangeDeviceConfigSuccess: function (text) {
        var widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: Uni.I18n.translate('searchItems.bulk.devicesAddedToQueueTitle', 'MDC', 'This task has been put on the queue successfully'),
            type: 'success',
            additionalItems: [
                {
                    xtype: 'container',
                    html: text
                }
            ]
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts(true);
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});