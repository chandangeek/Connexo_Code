Ext.define('Isu.view.administration.datacollection.licensing.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.licensing.List'
    ],
    alias: 'widget.administration-licensing-overview',

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'sideMenu'
        },
        {
            xtype: 'licensing-side-filter'
        }
    ],

    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    html: '<h1>Licensing</h1>',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'container',
                    height: 45,
                    layout: {
                        type: 'hbox',
                        align: 'middle'
                    },
                    items: [
                        {
                            xtype: 'component',
                            html: '<b>Filters</b>',
                            width: 50
                        },
                        {
                            xtype: 'component',
                            html: 'None',
                            name: 'empty-text'
                        },
                        {
                            xtype: 'container',
                            name: 'filter',
                            header: false,
                            border: false,
                            margin: '10 0 10 0',
                            layout: {
                                type: 'hbox',
                                align: 'stretch',
                                defaultMargins: '0 5'
                            },
                            flex: 1
                        }
                    ]
                },
                {
                    xtype: 'licensing-list',
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
        var me = this,
            menu = this.getSideMenuCmp();

        menu.add({
            text: 'Licensing',
            pressed: true,
            href: '#/administration/datacollection/licensing',
            hrefTarget: '_self'
        });

    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});
