Ext.define('Isu.view.workspace.issues.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-overview',
    title: 'Issues overview',

    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.workspace.issues.Browse',
        'Isu.view.workspace.issues.SideFilter'
    ],

    content: {
        itemId: 'issues-browse',
        xtype: 'issues-browse'
    },

    side: {
        itemId: 'navigation',
        xtype: 'panel',
        ui: 'medium',
        title: "Navigation",
//        subtitle: 'subtitle',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'overview',
                xtype: 'menu',
                title: 'Overview',
                ui: 'side-menu',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                floating: false,
                plain: true,
                items: [
                    {
                        text: 'Issues',
                        cls: 'current'
                    }
                ]
            },
            {   itemId: 'issues-side-filter',
                xtype: 'issues-side-filter'
            }
        ]
    }
});