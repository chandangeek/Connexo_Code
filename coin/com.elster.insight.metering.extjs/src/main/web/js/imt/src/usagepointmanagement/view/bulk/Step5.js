Ext.define('Imt.usagepointmanagement.view.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagepoints-bulk-step5',
    title: Uni.I18n.translate('usagepoints.bulk.step5title', 'IMT', 'Step 5: Status'),
    requires: [
        'Uni.view.notifications.NotificationPanel'
    ],
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
        itemId: 'usagepointsbulkactiontitle'
    },

    showAddCalendarSuccess: function (text) {
        var widget = {
            xtype: 'uni-notification-panel',
            margin: '0 0 0 -13',
            message: Uni.I18n.translate('usagepoints.bulk.devicesAddedToQueueTitle', 'IMT', 'This task has been queued'),
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