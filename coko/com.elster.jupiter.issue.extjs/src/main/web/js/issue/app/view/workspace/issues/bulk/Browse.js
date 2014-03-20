Ext.define('Isu.view.workspace.issues.bulk.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bulk-browse',
    componentCls: 'isu-bulk-browse',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.workspace.issues.bulk.BulkWizard',
        'Isu.view.workspace.issues.bulk.Navigation'
    ],

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'sideMenu'
        },
        {
            xtype: 'bulk-navigation'
        }
    ],

    content: [
        {
            xtype: 'bulk-wizard',
            defaults: {
                cls: 'content-wrapper'
            }
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
            text: 'Issues bulk action',
            pressed: true,
            href: '#/workspace/datacollection/issuesbulkaction',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});