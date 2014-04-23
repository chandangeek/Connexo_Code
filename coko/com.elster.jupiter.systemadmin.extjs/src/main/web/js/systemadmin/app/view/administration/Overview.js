Ext.define('Sam.view.administration.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.sam-administration-overview',

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
            html: '<h1>System administration</h1>',
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
            text: 'System administration',
            pressed: true,
            href: '#/sysadministration',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Licenses',
            href: '#/sysadministration/licensing/licenses',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});
