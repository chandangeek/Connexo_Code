Ext.define('Isu.view.administration.datacollection.licensing.addlicense.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu'
    ],
    alias: 'widget.add-license-overview',

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
                    html: '<h1>Add license</h1>',
                    margin: '0 0 20 0'
                },
                {
                    border: false,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'form',
                            width: 600,
                            layout: 'anchor',
                            border: false,
                            defaults: {
                                anchor: '100%'
                            },
                            defaultType: 'textfield',
                            items: [
                                {
                                    fieldLabel: 'License file',
                                    name: 'choose',
                                    emptyText: 'Choose license file',
                                    allowBlank: false
                                }
                            ]
                        },
                        {
                            xtype: 'button',
                            text: 'Browse...',
                            margin: '0 0 0 10',
                            hrefTarget: '',
                            href: '#/administration/datacollection/licensing'
                        }
                    ]
                },
                {
                    layout: 'hbox',
                    defaults: {
                        xtype: 'button',
                        margin: '20 15 0 0'
                    },
                    items: [
                        {
                            text: 'Add'
                        },
                        {
                            text: 'Cancel',
                            hrefTarget: '',
                            href: '#/administration/datacollection/licensing',
                            cls: 'isu-btn-link'
                        }
                    ]
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
            text: 'Add license',
            pressed: true,
            href: '#/administration/datacollection/licensing/addlicense',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});
