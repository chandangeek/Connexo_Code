Ext.define('Isu.view.administration.datacollection.licensing.upgradelicense.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu'
    ],
    alias: 'widget.upgrade-license-overview',

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
                    html: '<h1>Upgrade license</h1>',
                    margin: '0 0 10 0'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Application',
                    margin: '0 0 0 10',
                    name: 'appType'
                },
                {
                    xtype: 'form',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'filefield',
                            name: 'uploadField',
                            fieldLabel: 'License file',
                            emptyText: 'Choose license file *.lic',
                            text: 'Browse...',
                            msgTarget: 'side',
                            vtype: 'fileUpload',
                            margin: 10
                        }
                    ],
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'bottom',
                            border: false,
                            margin: '0 0 0 100',
                            defaults: {
                                xtype: 'button'
                            },
                            items: [
                                {
                                    text: 'Upgrade',
                                    name: 'upgrade',
                                    margin: 10,
                                    disabled: true
                                },
                                {
                                    text: 'Cancel',
                                    name: 'cancel',
                                    margin: 10,
                                    hrefTarget: '',
                                    href: '#/issue-administration/datacollection/licensing',
                                    cls: 'isu-btn-link'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        this.initMenu();
        Ext.apply(Ext.form.VTypes, {
            fileUpload: function (val, field) {
                var fileName = /^.*\.(lic)$/i;
                return fileName.test(val);
            },
            fileUploadText: 'License must be in .lic format'
        });
    },

    initMenu: function () {
        var menu = this.getSideMenuCmp();

        menu.add({
            text: 'Upgrade license',
            pressed: true,
            href: '#/issue-administration/datacollection/licensing/upgradelicense',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});

