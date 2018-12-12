/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.view.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    xtype: 'certificates-bulk-step4',
    name: 'confirmPage',
    ui: 'large',

    requires: [
        'Uni.view.notifications.NotificationPanel'
    ],
    title: Uni.I18n.translate('readingtypesmanagment.bulk.step4.title', 'PKJ', 'Step 4: Confirmation'),

    addNotificationPanel: function (title, message, htmlMsg) {
        var me = this;

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
    },
    addErrorPanel: function (title, message, certs, itemId) {
        var me = this;

        Ext.suspendLayouts();

        var widget = Ext.create('Uni.view.notifications.NoItemsFoundPanel',{
            noStepItems: true,
            title: title,
            reasonsText: '',
            margin: '0 0 16 0',
            itemId: itemId ? itemId : undefined

        });


        widget.down('#wrapper').removeAll();
        widget.down('#wrapper').add({
            xtype: 'component',
            html: message
        });

        if(certs){
            _.map(_.first(certs, 10), function(cert){
                var url = me.router.getRoute('administration/certificates/view').buildUrl({certificateId: cert.id});
                // return ;
                widget.down('#wrapper').add({
                    xtype: 'component',
                    html: '<a href="' + url + '">' + Ext.String.htmlEncode(cert.name) + '</a>'
                });
            });
        }
        me.add(widget);

        Ext.resumeLayouts(true);
    }
});