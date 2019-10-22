/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.view.Browse', {

    extend: 'Uni.view.container.ContentContainer',

    xtype: 'add-group-browse',

    requires: [
        'Imt.usagepointgroupsfromissues.view.Wizard',
        'Imt.usagepointgroupsfromissues.view.Navigation'
    ],

    router: null,

    returnLink: null,

    usagePointDomainSearchService: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
            itemId: 'add-group-side-panel',
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'add-group-navigation-panel',
                    itemId: 'add-group-navigation-panel',
                    title: Uni.I18n.translate('usagepointgroupfromissues.browse.title', 'IMT', 'Add usage point group')
                }
            ]
        };

        me.content = [
            {
                xtype: 'add-group-wizard',
                itemId: 'add-group-wizard',
                router: me.router,
                returnLink: me.returnLink,
                usagePointDomainSearchService: me.usagePointDomainSearchService
            }
        ];

        me.callParent(arguments);
    }

});
