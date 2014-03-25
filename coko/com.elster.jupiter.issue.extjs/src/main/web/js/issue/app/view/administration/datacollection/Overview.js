Ext.define('Isu.view.administration.datacollection.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.administration-datacollection-overview',

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
            href: '#/administration/datacollection',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Issue assignment rules',
            href: '#/administration/datacollection/issueassignmentrules',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Issue creation rules',
            href: '#/administration/datacollection/issuecreationrules',
            hrefTarget: '_self'
        });

        menu.add({
            text: 'Licensing',
            href: '#/administration/datacollection/licensing',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});