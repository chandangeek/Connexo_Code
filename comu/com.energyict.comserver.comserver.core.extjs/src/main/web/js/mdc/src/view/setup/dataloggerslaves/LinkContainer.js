/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkContainer', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'dataloggerslave-link-container',

    requires: [
        'Mdc.view.setup.dataloggerslaves.LinkNavigationMenu',
        'Mdc.view.setup.dataloggerslaves.LinkWizard'
    ],

    router: null,
    returnLink: null,
    service: null,

    initComponent: function () {
        var me = this;

        me.side = {
            itemId: 'mdc-link-dataloggerslave-sidemenu-panel',
            xtype: 'panel',
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'dataloggerslave-link-navigation',
                    itemId: 'mdc-link-dataloggerslave-navigation-menu'
                }
            ]
        };

        me.content = [
            {
                xtype: 'dataloggerslave-link-wizard',
                itemId: 'mdc-dataloggerslave-link-wizard',
                router: me.router,
                service: me.service,
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});
