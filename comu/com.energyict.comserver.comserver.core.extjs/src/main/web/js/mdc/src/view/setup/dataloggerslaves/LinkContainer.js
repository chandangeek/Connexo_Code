/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkContainer', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'slave-link-container',
    requires: [
        'Mdc.view.setup.dataloggerslaves.LinkNavigationMenu',
        'Mdc.view.setup.dataloggerslaves.LinkWizard'
    ],
    itemId: 'mdc-dataloggerslave-link-container',
    purpose: undefined,
    router: null,
    returnLink: null,
    service: null,

    initComponent: function () {
        var me = this;

        me.side = {
            itemId: 'mdc-link-slave-sidemenu-panel',
            xtype: 'panel',
            title: me.purpose.displayValue,
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'slave-link-navigation',
                    itemId: 'mdc-link-slave-navigation-menu',
                    purpose: me.purpose
                }
            ]
        };

        me.content = [
            {
                xtype: 'slave-link-wizard',
                itemId: 'mdc-slave-link-wizard',
                purpose: me.purpose,
                router: me.router,
                service: me.service,
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});
