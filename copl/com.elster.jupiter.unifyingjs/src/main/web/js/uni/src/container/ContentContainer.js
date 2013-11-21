Ext.define('Uni.container.ContentContainer', {
    extend: 'Ext.container.Container',

    requires: [
        'Uni.breadcrumb.Trail'
    ],

    layout: {
        type: 'border'
    },

    items: [
        {
            xtype: 'container',
            itemId: 'northWrapper',
            region: 'north',
            layout: {
                type: 'hbox'
            },
            items: [
                {
                    xtype: 'container',
                    itemId: 'northWestWrapper',
                    items: [
                        {
                            xtype: 'breadcrumbTrail'
                        }
                    ]
                },
                {
                    xtype: 'component',
                    flex: 1,
                    html: '&#160;'
                },
                {
                    xtype: 'container',
                    itemId: 'northEastWrapper'
                }
            ]
        },
        {
            xtype: 'container',
            itemId: 'westWrapper',
            region: 'west'
        },
        {
            xtype: 'container',
            itemId: 'centerWrapper',
            region: 'center'
        },
        {
            xtype: 'container',
            itemId: 'eastWrapper',
            region: 'east'
        }
    ]
});