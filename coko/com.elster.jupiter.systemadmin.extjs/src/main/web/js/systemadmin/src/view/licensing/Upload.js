/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.licensing.Upload', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.File'
    ],
    alias: 'widget.upload-licenses-overview',

    content: [
        {
            xtype: 'form',
            ui: 'large',
            title: Uni.I18n.translate('licensing.uploadLicenses', 'SAM', 'Upload licenses'),
            autoEl: {
                tag: 'form',
                enctype: 'multipart/form-data'
            },
            defaults: {
                labelWidth: 100
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true
                },
                {
                    itemId: 'uploadfileField',
                    xtype: 'filefield',
                    name: 'uploadField',
                    fieldLabel: Uni.I18n.translate('licensing.licenseFile', 'SAM', 'License file'),
                    emptyText: Uni.I18n.translate('general.chooseLicense','SAM','Choose license file *.lic'),
                    buttonText: Uni.I18n.translate('general.browse','SAM','Browse') + '...',
                    msgTarget: 'under',
                    vtype: 'fileUpload',
                    width: 450
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: ' ',
                    defaultType: 'button',
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
            fileUploadText: Uni.I18n.translate('licensing.validationFailed.msg', 'SAM', 'License must be in .lic format')
        });
    }
});

