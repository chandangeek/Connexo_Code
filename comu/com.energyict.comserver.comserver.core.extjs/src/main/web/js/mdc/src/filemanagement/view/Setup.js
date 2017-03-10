/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.filemanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-files-setup',

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.filemanagement.view.PreviewContainer',
        'Mdc.filemanagement.view.Specifications',
        'Uni.util.FormEmptyMessage'
    ],

    deviceTypeId: null,
    fileManagementEnabled: false,
    fromEditForm: false,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'deviceTypeSideMenu',
                        deviceTypeId: me.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.fileManagement', 'MDC', 'File management'),
            items: [
                {
                    xtype: 'tabpanel',
                    itemId: 'files-tab-panel',
                    ui: 'large',
                    activeTab: me.fileManagementEnabled && !me.fromEditForm ? 1 : 0,
                    items: [
                        {
                            title: Uni.I18n.translate('general.specifications', 'MDC', 'Specifications'),
                            itemId: 'files-specifications-tab',
                            items: [
                                {
                                    xtype: 'files-specifications-preview-panel'
                                }
                            ]
                        },
                        {
                            title: Uni.I18n.translate('general.files', 'MDC', 'Files'),
                            itemId: 'grid-tab',
                            //disabled: true,
                            items: [
                                {

                                            xtype: 'uni-form-empty-message',
                                            itemId: 'maxFileSizeMessage',
                                            margin: '5 0 5 0',
                                            text: Uni.I18n.translate('filemanagement.maxAllowedFileSize2MB', 'MDC', 'The maximum allowed file size is 2MB')


                                },
                                {
                                    xtype: 'files-devicetype-preview-container',
                                    itemId: 'files-devicetype-preview-container',
                                    fileManagementEnabled: me.fileManagementEnabled,
                                    deviceTypeId: me.deviceTypeId
                                }

                            ]
                        }

                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});