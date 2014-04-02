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
        'Uni.view.container.ContentContainer'
    ],

    layout: 'border',
    items: [
        {
            xtype: 'navigationHeader',
            region: 'north'
        },
        {
            xtype: 'container',
            cls: 'nav-wrapper',
            region: 'west',
            layout: 'absolute',
            width: 55,
            items: [
                {
                    xtype: 'navigationMenu'
                }
            ]
        },
        {
            xtype: 'container',
            region: 'center',
            itemId: 'contentPanel',
            layout: 'fit'
        }
    ]
});