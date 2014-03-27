Ext.define('Isu.view.administration.datacollection.licensing.addlicense.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.File'
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
                    margin: '0 0 10 0'
                },
                {
                    xtype: 'form',
                    layout: 'hbox',
                    buttonAlign: 'left',
                    items: [
                        {
                            xtype: 'filefield',
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
                            defaults: {
                                xtype: 'button'
                            },
                            items: [
                                {
                                    text: 'Add',
                                    name: 'add',
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
            text: 'Add license',
            pressed: true,
            href: '#/issue-administration/datacollection/licensing/addlicense',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});
