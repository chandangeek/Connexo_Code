Ext.define('Isu.view.workspace.datacollection.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.datacollection-overview',

    requires: [
        'Uni.view.navigation.SubMenu'
    ],

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            xtype: 'panel',
            html: '<h1>Data collection</h1>',
            flex: 1
        }
    ],

    initComponent: function () {
        this.callParent(this);

        this.initMenu();
    },

    initMenu: function () {
        var me = this,
            menu = this.getSideMenuCmp();

        menu.add({
            text: 'Data collection',
            pressed: true,
            href: '#/workspace/datacollection',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Issues',
            href: '#/workspace/datacollection/issues',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});