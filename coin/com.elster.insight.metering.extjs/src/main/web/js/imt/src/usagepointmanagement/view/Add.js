/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'add-usage-point',

    requires: [
        'Imt.usagepointmanagement.view.Navigation',
        'Imt.usagepointmanagement.view.Wizard'
    ],

    returnLink: null,
    isPossibleAdd: true,

    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'add-usage-point-navigation',
                    itemId: 'add-usage-point-navigation',
                    title: Uni.I18n.translate('usagepoint.wizard.menu', 'IMT', 'Add usage point')
                }
            ]
        };

        me.content = [
            {
                xtype: 'add-usage-point-wizard',
                itemId: 'add-usage-point-wizard',
                returnLink: me.returnLink,
                isPossibleAdd: me.isPossibleAdd
            }
        ];

        me.callParent(arguments);
    }
});