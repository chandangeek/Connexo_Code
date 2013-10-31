Ext.define('Uni.view.Viewport', {
    extend: 'Ext.container.Viewport',

    requires: [
        'Ext.layout.container.Border',
        'Uni.view.navigation.Header',
        'Uni.view.navigation.Footer',
        'Uni.view.navigation.Menu'
    ],

    layout: 'border',
    items: [
        {
            xtype: 'navigationHeader',
            region: 'north'
        },
        {
            xtype: 'navigationMenu',
            region: 'west'
        },
        {
            xtype: 'container',
            region: 'center',
            itemId: 'contentPanel',
            layout: 'fit'
        },
        {
            xtype: 'navigationFooter',
            region: 'south'
        }
    ]
});