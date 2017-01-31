/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    xtype: 'reading-types-bulk-step5',
    name: 'statusPage',
    ui: 'large',

    requires: [
        'Uni.view.notifications.NotificationPanel'
    ],
    title: Uni.I18n.translate('readingtypesmanagment.bulk.step5.title', 'MTR', 'Step 5: Status'),

    addNotificationPanel: function (title, htmlMsg) {
        var me = this;

        Ext.suspendLayouts();

        me.removeAll(true);
        me.add({
                xtype: 'uni-notification-panel',
                itemId: 'step5-success-panel',
                margin: '0 0 0 -16',
                type: 'success',
                title: title,
                message: Uni.I18n.translate('readingtypesmanagment.bulk.step5success', 'MTR', 'This task has been put on queue successfully'),

                additionalItems: [
                    {
                        xtype: 'container',
                        html: Ext.htmlEncode(htmlMsg)
                    }
                ]
            },
            {
                xtype: 'uni-notification-panel',
                itemId: 'step5-error-panel',
                margin: '0 0 0 -16',
                hidden: true,
                type: 'error',
                title: title,
                message: Uni.I18n.translate('readingtypesmanagment.bulk.step5error', 'MTR', 'This task hasn\'t been put on queue'),

                additionalItems: [
                    {
                        xtype: 'container',
                        html: Uni.I18n.translate('readingtypesmanagment.bulk.step5erroroperation', 'MTR', 'There was an error during the operation')
                    }
                ]
            });

        Ext.resumeLayouts(true);
    }
});