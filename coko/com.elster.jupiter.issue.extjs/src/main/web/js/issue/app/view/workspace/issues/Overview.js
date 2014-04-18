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
                floating: false
            },
            {
                xtype: 'issues-side-filter'
            }
        ]
    },

    initComponent: function () {
        this.callParent(this);
        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            menu = this.getSideMenuCmp();

        menu.add([
            {
                text: 'Issues',
                activated: true
            },
            {
                text: 'sub',
                href: '#/workspace/datacollection/issues/sub',
                hrefTarget: '_self'
            }
        ]);
    },

    getSideMenuCmp: function () {
        return this.down('menu');
    }
});