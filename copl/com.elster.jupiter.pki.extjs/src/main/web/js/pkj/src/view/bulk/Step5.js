/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    xtype: 'certificates-bulk-step5',
    name: 'statusPage',
    ui: 'large',

    requires: [
        'Uni.view.notifications.NotificationPanel'
    ],
    title: Uni.I18n.translate('readingtypesmanagment.bulk.step5.title', 'PKJ', 'Step 5: Status'),

    addNotificationPanel: function (title, msg, additionalItems) {
        var me = this;

        Ext.suspendLayouts();

        me.removeAll(true);
        me.add({
                xtype: 'uni-notification-panel',
                itemId: 'step5-success-panel',
                margin: '0 0 0 -16',
                type: 'success',
                title: title,
                message: msg,

                additionalItems: additionalItems
            }
        );

        Ext.resumeLayouts(true);
    }
});