Ext.define('Sam.view.licensing.Upload', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.File'
    ],
    alias: 'widget.upload-licenses-overview',

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
                    itemId: 'pageTitle',
                    html: '<h1>Upload licenses</h1>',
                    cls: 'license-overview-title'
                },
                {
                    xtype: 'form',
                    layout: 'hbox',
                    items: [
                        {
                            itemId: 'uploadfileField',
                            xtype: 'filefield',
                            name: 'uploadField',
                            fieldLabel: 'License file',
                            emptyText: 'Choose license file *.lic',
                            text: 'Browse...',
                            msgTarget: 'side',
                            vtype: 'fileUpload'
                        }
                    ],
                    dockedItems: [
                        {
                            itemId: 'toolbarBot',
                            xtype: 'toolbar',
                            dock: 'bottom',
                            border: false,
                            cls: 'license-upload-tool',
                            defaults: {
                                xtype: 'button'
                            },
                            items: [
                                {
                                    itemId: 'upload',
                                    text: 'Upload',
                                    name: 'upload',
                                    disabled: true
                                },
                                {
                                    itemId: 'Cancel',
                                    text: 'Cancel',
                                    ui: 'link',
                                    hrefTarget: '',
                                    href: '#/sysadministration/licensing/licenses',
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
            text: 'Upload licenses',
            pressed: true,
            href: '#/sysadministration/licensing/upload',
            hrefTarget: '_self'
        });
    },

    getSideMenuCmp: function () {
        return this.down('#sideMenu');
    }
});

