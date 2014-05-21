Ext.define('Mdc.view.setup.logbooktype.LogbookTypesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu'
    ],
    alias: 'widget.logbook-overview',

    side: {
        xtype: 'panel',
        ui: 'medium',
        title: "Navigation",
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                xtype: 'menu',
                ui: 'side-menu',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                floating: false,
                plain: true,
                items: [
                    {
                        text: 'Logbook types',
                        cls: 'current'
                    }
                ]
            }
        ]
    },

    content: [
        {
            ui: 'large',
            xtype: 'panel',
            title: 'Logbook types',
            items: [
                {
                    xtype: 'logbook-docked-buttons'
                },
                {
                    xtype: 'logbook-list'
                },
                {
                    xtype: 'logbook-empty-list-message'
                },
                {
                    xtype: 'logbook-item'
                }
            ]
        }
    ]
});


