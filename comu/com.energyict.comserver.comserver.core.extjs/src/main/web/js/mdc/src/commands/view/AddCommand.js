/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.AddCommand', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-command',

    requires: [
        'Mdc.commands.view.AddCommandSideNavigation',
        'Mdc.commands.view.AddCommandWizard'
    ],

    returnLink: undefined,

    initComponent: function () {
        var me = this;

        me.side = {
            itemId: 'mdc-add-command-side-menu',
            xtype: 'panel',
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'add-command-side-navigation',
                    title: Uni.I18n.translate('general.addCommand', 'MDC', 'Add command'),
                    itemId: 'mdc-add-command-side-navigation'
                }
            ]
        };

        me.content = [
            {
                xtype: 'add-command-wizard',
                itemId: 'mdc-add-command-wizard',
                router: me.router,
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }

});