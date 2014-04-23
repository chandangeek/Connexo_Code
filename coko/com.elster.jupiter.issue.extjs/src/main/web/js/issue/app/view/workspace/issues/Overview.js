Ext.define('Isu.view.workspace.issues.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-overview',
    title: 'Issues overview',

    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.workspace.issues.Browse',
        'Isu.view.workspace.issues.SideFilter'
    ],

    content: { xtype: 'issues-browse'},

    side: {
        xtype: 'panel',
        ui: 'medium',
        title: "Navigation",
        subtitle: 'subtitle',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
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
                    },
                    {
                        text: 'sub',
                        href: '#/workspace/datacollection/issues/sub',
                        hrefTarget: '_self'
                    }
                ]
            },
            {
                xtype: 'issues-side-filter'
            }
        ]
    }
//
//    initComponent: function () {
//        this.callParent(this);
//        this.initMenu();
//    },
//
//
//    initMenu: function () {
//        var me = this,
//            menu = this.getSideMenuCmp();
//
//        menu.setActiveItem(0);
//    },
//
//    getSideMenuCmp: function () {
//        return this.down('menu');
//    }
});