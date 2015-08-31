Ext.define('Sam.view.licensing.Upload', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.File'
    ],
    alias: 'widget.upload-licenses-overview',

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
                    autoEl: {
                        tag: 'form',
                        enctype: 'multipart/form-data'
                    },
                    items: [
                        {
                            itemId: 'uploadfileField',
                            xtype: 'filefield',
                            name: 'uploadField',
                            fieldLabel: 'License file',
                            emptyText: Uni.I18n.translate('general.chooseLicense','SAM','Choose license file *.lic'),
                            text: Uni.I18n.translate('general.browse','SAM','Browse...'),
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
                                    text: Uni.I18n.translate('general.upload','SAM','Upload'),
                                    name: 'upload',
                                    disabled: true,
                                    ui: 'action'
                                },
                                {
                                    itemId: 'Cancel',
                                    text: Uni.I18n.translate('general.cancel','SAM','Cancel'),
                                    ui: 'link',
                                    hrefTarget: '',
                                    href: '#/administration/licenses'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;
        me.side = [
            {
                ui: 'medium',
                items: {
                    xtype: 'uni-view-menu-side',
                    title: Uni.I18n.translate('general.licensing', 'SAM', 'Licensing'),
                    itemId: 'sideMenu',
                    menuItems: [
                        {
                            itemId: 'navEl',
                            text: Uni.I18n.translate('licensing.uploadLicenses', 'SAM', 'Upload licenses'),
                            href: '#/administration/licenses/upload'
                        }
                    ]
                }}
        ];
        this.callParent(this);
        Ext.apply(Ext.form.VTypes, {
            fileUpload: function (val, field) {
                var fileName = /^.*\.(lic)$/i;
                return fileName.test(val);
            },
            fileUploadText: 'License must be in .lic format'
        });
    }
});

