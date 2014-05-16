Ext.define('Mdc.view.setup.logbooktype.LogbookTypesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu'
    ],
    alias: 'widget.logbook-overview',

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    html: '<h1>Logbook types</h1>',
                    margin: '0 0 10 0'
                },
                {
                    xtype: 'logbook-docked-buttons'
                },
                {
                    xtype: 'logbook-list',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'logbook-empty-list-message',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'logbook-item',
                    margin: '0 0 20 0'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        this.initMenu();
    },

    initMenu: function () {
        var menu = this.getSideMenuCmp();

        menu.add({
            text: 'Logbook types',
            pressed: true,
            href: '#/administration/logbooktypes',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});


