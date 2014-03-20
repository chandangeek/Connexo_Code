Ext.define('Isu.view.workspace.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.workspace-overview',

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
            html: '<h1>Workspace</h1>',
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
            text: 'Workspace',
            pressed: true,
            href: '#/workspace',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Data collection',
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