/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.Viewport
 */
Ext.define('Uni.view.Viewport', {
    extend: 'Ext.container.Viewport',

    requires: [
        'Ext.layout.container.Border',
        'Uni.view.navigation.Header',
        'Uni.view.navigation.Footer',
        'Uni.view.navigation.Menu',
        'Uni.view.container.ContentContainer',
        'Uni.view.breadcrumb.Trail'
    ],

    layout: 'border',
    items: [
        {
            xtype: 'navigationHeader',
            region: 'north',
            weight: 30
        },
        {
            xtype: 'container',
            ui: 'navigationwrapper',
            region: 'west',
            layout: 'fit',
            items: [
                {
                    xtype: 'navigationMenu'
                }
            ],
            weight: 25
        },
        {
            region: 'north',
            cls: 'north',
            xtype: 'breadcrumbTrail',
            itemId: 'breadcrumbTrail',
            weight: 20
        },
        {
            xtype: 'container',
            region: 'center',
            itemId: 'contentPanel',
            maskElement: 'el',
            layout: 'fit',
            overflowX: 'auto',
            overflowY: 'hidden',
            defaults: {
                minWidth: 1160
            }
        }
    ]
});