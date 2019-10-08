/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.devicegroupfromissues.Browse', {

    extend: 'Uni.view.container.ContentContainer',

    xtype: 'add-group-browse',

    requires: [
        'Isu.view.devicegroupfromissues.Wizard',
        'Isu.view.devicegroupfromissues.Navigation'
    ],

    router: null,

    returnLink: null,

    deviceDomainSearchService: null,

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
                    title: Uni.I18n.translate('devicegroupfromissues.browse.title', 'ISU', 'Add device group')
                }
            ]
        };

        me.content = [
            {
                xtype: 'add-group-wizard',
                itemId: 'add-group-wizard',
                router: me.router,
                returnLink: me.returnLink,
                deviceDomainSearchService: me.deviceDomainSearchService
            }
        ];

        me.callParent(arguments);
    }

});