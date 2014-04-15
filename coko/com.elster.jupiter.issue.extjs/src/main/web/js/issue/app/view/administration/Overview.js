Ext.define('Isu.view.administration.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.administration-overview',

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
            html: '<h1>Administration</h1>',
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
            text: 'Administration',
            pressed: true,
            href: '#/issue-administration',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Data collection',
            href: '#/issue-administration/datacollection',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Issue assignment rules',
            href: '#/issue-administration/datacollection/issueassignmentrules',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Issue creation rules',
            href: '#/issue-administration/issuecreationrules',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});