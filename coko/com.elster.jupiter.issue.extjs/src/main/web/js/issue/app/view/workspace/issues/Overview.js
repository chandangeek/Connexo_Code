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
        items: [
            {
                xtype: 'navigationSubMenu',
                itemId: 'sideMenu'
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

        menu.add({
            text: 'Issues',
            pressed: true,
            href: '#/workspace/datacollection/issues',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});