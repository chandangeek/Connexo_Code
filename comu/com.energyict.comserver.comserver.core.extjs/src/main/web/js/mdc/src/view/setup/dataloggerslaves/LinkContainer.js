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
    purpose: undefined,
    router: null,
    returnLink: null,
    service: null,

    initComponent: function () {
        var me = this;

        me.side = {
            itemId: 'mdc-link-dataloggerslave-sidemenu-panel',
            xtype: 'panel',
          /*  title: me.getTitle(me.purpose),  */
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'dataloggerslave-link-navigation',
                    itemId: 'mdc-link-dataloggerslave-navigation-menu',
                    title: me.purpose
                }
            ]
        };

        me.content = [
            {
                xtype: 'dataloggerslave-link-wizard',
                itemId: 'mdc-dataloggerslave-link-wizard',
                purpose: me.purpose,
                router: me.router,
                service: me.service,
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    },
    setPurpose: function(purpose){
        var me = this;
        me.down('#mdc-link-dataloggerslave-sidemenu-panel').setTitle(purpose.displayValue);

    }
});
