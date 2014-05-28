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
            layout: 'absolute',
            width: 48,
            items: [
                {
                    xtype: 'navigationMenu'
                }
            ],
            weight: 20
        },
        {
            xtype: 'container',
            region: 'center',
            itemId: 'contentPanel',
            layout: 'fit'
        },
        {
            region: 'north',
            xtype: 'container',
            itemId: 'northContainer',
            cls: 'north',
            layout: 'hbox',
            ui: 'breadcrumbtrailcontainer',
            height: 48,
            items: [
                {
                    xtype: 'breadcrumbTrail',
                    itemId: 'breadcrumbTrail'
                }
            ],
            weight: 10
        }
    ]
});