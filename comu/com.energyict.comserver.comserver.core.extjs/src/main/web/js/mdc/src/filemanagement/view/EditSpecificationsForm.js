/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.filemanagement.view.EditSpecificationsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.files-devicetype-edit-specs-form',
    requires: [
        'Uni.property.form.Property',
        'Uni.util.FormErrorMessage'
    ],
    layout: {
        type: 'form'
    },
    defaults: {
        labelWidth: 250
    },
    deviceTypeId: null,
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 750
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'file-management-radio',
                        fieldLabel: Uni.I18n.translate('filemanagement.fileManagementAllowed', 'MDC', 'File management allowed'),
                        defaultType: 'radiofield',
                        required: true,
                        layout: 'vbox',
                        items: [
                            {
                                boxLabel: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                                name: 'fileManagementEnabled',
                                inputValue: 'true',
                                itemId: 'files-allowed-radio-field'
                            }, {
                                boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'),
                                name: 'fileManagementEnabled',
                                inputValue: 'false',
                                checked: true,
                                afterBodyEl: [
                                    '<div class="x-form-display-field" style="padding-right:10px;padding-left:18px;color:#A0A0A0"><i>',
                                    Uni.I18n.translate('files.existingFilesWillBeRemoved', 'MDC', 'Existing files will be removed'),
                                    '</i></div>'
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'files-save-specs-button',
                                text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'files-edit-cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link',
                                href: '#/administration/devicetypes/' + me.deviceTypeId + '/filemanagement'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
