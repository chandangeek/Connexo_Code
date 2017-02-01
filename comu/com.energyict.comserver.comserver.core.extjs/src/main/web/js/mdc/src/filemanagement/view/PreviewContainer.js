/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.filemanagement.view.PreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.files-devicetype-preview-container',
    deviceTypeId: null,
    fileManagementEnabled: false,


    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.filemanagement.view.FilesGrid'
    ],


    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'files-grid',
            itemId: 'files-grid',
            fileManagementEnabled: me.fileManagementEnabled,
            deviceTypeId: me.deviceTypeId
        };

        me.emptyComponent = {
            xtype: 'no-items-found-panel',
            itemId: 'no-files',
            title: Uni.I18n.translate('filemanagement.files.empty.title', 'MDC', 'No files found'),
            reasons: [
                Uni.I18n.translate('filemanagement.files.empty.list.item', 'MDC', 'No files have been defined yet.'),
            ],
            stepItems: [
                {
                    xtype: 'form',
                    layout: 'hbox',
                    autoEl: {
                        tag: 'form',
                        enctype: 'multipart/form-data'
                    },
                    items: [
                        {
                            xtype: 'filefield',
                            name: 'uploadField',
                            buttonText: Uni.I18n.translate('filemanagement.addFile', 'MDC', 'Add file'),
                            privileges: Mdc.privileges.DeviceType.admin,
                            buttonConfig: {
                                ui: 'default',
                                style: {
                                    cursor: me.fileManagementEnabled ? 'pointer' :'default'
                                }
                            },
                            disabledCls: 'x-btn-disabled',
                            buttonOnly: true,
                            itemId: 'add-file-btn2',
                            vtype: 'fileUpload',
                            disabled: !me.fileManagementEnabled
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('tou.allowFilemanagement', 'MDC', 'Allow file management'),
                            itemId: 'enable-file-management-btn',
                            disabled: me.fileManagementEnabled
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});