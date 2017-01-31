/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.LandingPage', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.webservice-landing-page',
    requires: [
        'Wss.view.Menu',
        'Wss.view.PreviewForm',
        'Wss.view.ActionMenu'
    ],

    router: null,
    record: null,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'webservices-menu',
                        itemId: 'webservices-menu',
                        router: me.router,
                        record: me.record
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    title: Uni.I18n.translate('general.overview', 'WSS', 'Overview'),
                    flex: 1,
                    items: {
                        xtype: 'webservices-preview-form',
                        isLandingPage: true,
                        router: me.router,
                        margin: '0 0 0 100'
                    }
                },
                {
                    xtype: 'uni-button-action',
                    privileges: Wss.privileges.Webservices.admin,
                    margin: '20 0 0 0',
                    menu: {
                        xtype: 'webservices-action-menu'
                    }
                }
            ]
        };

        me.callParent(arguments);
        me.down('webservices-preview-form').down('form').loadRecord(me.record);
    }
});