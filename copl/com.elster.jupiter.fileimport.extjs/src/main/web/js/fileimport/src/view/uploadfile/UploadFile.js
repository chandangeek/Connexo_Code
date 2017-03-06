/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.uploadfile.UploadFile', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.upload-file',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormInfoMessage',
        'Uni.property.form.Property'
    ],
    returnLink: undefined,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                itemId: 'upload-file-form',
                title: Uni.I18n.translate('uploadFile.title', 'FIM', 'Upload file for import'),
                ui: 'large',
                defaults: {
                    labelWidth: 250,
                    width: 600
                },
                autoEl: {
                    tag: 'form',
                    enctype: 'multipart/form-data'
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'upload-file-form-errors',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-info-message',
                        itemId: 'upload-file-info-message',
                        text: Uni.I18n.translate('uploadFile.infoMsg', 'FIM', 'The uploaded file will be imported by the system according to the frequency of the folder scan as set by your system administrator'),
                        hidden: true
                    },
                    {
                        xtype: 'textfield',
                        itemId: 'scheduleid',
                        name: 'scheduleId',
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),
                        itemId: 'import-service-container',
                        required: true,
                        items: [
                            {
                                xtype: 'combobox',
                                editable: false,
                                width: 325,
                                itemId: 'import-service-combobox',
                                emptyText: Uni.I18n.translate('uploadFile.emptyImportServices', 'FIM', 'Select an import service...'),
                                store: 'Fim.store.AvailableImportServices',
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                forceSelection: true,
                                listeners: {
                                    change: function (combo, newValue) {
                                        Ext.suspendLayouts();
                                        me.down('#import-service-property-form').loadRecord(combo.getStore().getById(newValue));
                                        if (me.down('#upload-file-button').isDisabled()) {
                                            me.down('#upload-file-button').enable();
                                            me.down('#upload-file-field').show();
                                        }
                                        Ext.resumeLayouts(true);
                                    }
                                }
                            },
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('uploadFile.noImportServices', 'FIM', 'There are no active import services in the system with the option to upload file in application'),
                                itemId: 'no-import-services',
                                hidden: true,
                                style: {
                                    'color': '#FF0000',
                                    'margin': '6px 10px 6px 0px'
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'property-form',
                        itemId: 'import-service-property-form',
                        isEdit: false
                    },
                    {
                        xtype: 'filefield',
                        itemId: 'upload-file-field',
                        name: 'file',
                        fieldLabel: Uni.I18n.translate('general.file', 'FIM', 'File'),
                        required: true,
                        hidden: true,
                        buttonText: Uni.I18n.translate('general.file', 'FIM', 'Select file...'),
                        afterBodyEl: [
                            '<div class="x-form-display-field"><i>', Uni.I18n.translate('uploadFile.filesize', 'FIM', 'Maximum file size is 100MB'), '</i></div>'
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'upload-file-buttons',
                        fieldLabel: ' ',
                        layout: 'hbox',
                        margin: '20 0 0 0',
                        defaultType: 'button',
                        items: [
                            {
                                itemId: 'upload-file-button',
                                text: Uni.I18n.translate('general.upload', 'FIM', 'Upload'),
                                disabled: true,
                                ui: 'action',
                                action: 'uploadFile'
                            },
                            {
                                itemId: 'upload-file-cancel-button',
                                text: Uni.I18n.translate('general.cancel', 'FIM', 'Cancel'),
                                ui: 'link',
                                action: 'cancelUploadFile',
                                href: me.returnLink
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();
    }
});