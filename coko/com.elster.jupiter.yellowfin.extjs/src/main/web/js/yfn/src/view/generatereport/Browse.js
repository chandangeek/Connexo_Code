/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Yfn.view.generatereport.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'generatereport-browse',
    itemId: 'generatereport-browse',
    requires: [
        'Yfn.view.generatereport.Navigation',
        'Yfn.view.generatereport.Wizard'
    ],

    side: {
        itemId: 'generatereportpanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'generatereportnavigation',
                xtype: 'generatereport-navigation'
            }
        ]
    },

    content: [
        {
            xtype: 'generatereport-wizard',
            itemId: 'generatereportwizard',
            defaults: {
                cls: 'content-wrapper'
            }
        }
    ]
});
