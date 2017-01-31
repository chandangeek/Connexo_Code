/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    xtype: 'reading-types-bulk-step4',
    name: 'confirmPage',
    ui: 'large',

    requires: [
        'Uni.view.notifications.NotificationPanel'
    ],
    title: Uni.I18n.translate('readingtypesmanagment.bulk.step4.title', 'MTR', 'Step 4: Confirmation'),


    addNotificationPanel: function (title, message, htmlMsg) {
        var me =this;

        Ext.suspendLayouts();

        me.removeAll(true);
        me.add({
            xtype: 'uni-notification-panel',
            itemId: 'step4-notification-panel',
            margin: '0 0 0 -16',
            title: title,
            message: Ext.htmlEncode(message),

            additionalItems: [
                {
                    xtype: 'container',
                    html: htmlMsg
                }
            ]
        });

        Ext.resumeLayouts(true);
    }

});