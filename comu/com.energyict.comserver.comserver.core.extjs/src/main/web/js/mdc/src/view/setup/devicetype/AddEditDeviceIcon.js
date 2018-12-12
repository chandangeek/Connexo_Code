/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.AddEditDeviceIcon', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-edit-device-icon',

    deviceTypeId: null,
    isEdit: false,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
    ],


    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                title: me.isEdit ? Uni.I18n.translate('deviceType.editDeviceIcon', 'MDC', 'Edit device icon') : Uni.I18n.translate('deviceType.addDeviceIcon', 'MDC', 'Add device icon'),
                itemId: 'addEditDeviceIconForm',
                autoEl: {
                    tag: 'form',
                    enctype: 'multipart/form-data'
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true,
                        maxWidth: 512
                    },
                    {
                        itemId: 'deviceIconFileField',
                        xtype: 'filefield',
                        required: true,
                        name: 'deviceIconField',
                        fieldLabel: Uni.I18n.translate('genereal.deviceIcon', 'MDC', 'Device icon'),
                        emptyText: Uni.I18n.translate('devicetype.chooseIcon', 'MDC', 'Choose icon (*.jpg, *.png)'),
                        buttonText: Uni.I18n.translate('general.selectFile', 'MDC', 'Select file') + '...',
                        msgTarget: 'under',
                        vtype: 'image',
                        allowBlank: false,
                        labelWidth: 150,
                        width: 692,
                        afterBodyEl: [
                            '<div class="x-form-display-field"><i>',
                            Uni.I18n.translate('deviceType.maxIconSize', 'MDC', 'File size limit is 500kB'),
                            '</i></div>'
                        ]
                    },
                    {
                        xtype: 'image',
                        itemId: 'deviceIconPreview',
                        height: 100,
                        margin: '0 0 0 165',
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        labelWidth: 150,
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                text: me.isEdit ? Uni.I18n.translate('general.save', 'MDC', 'Save') : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'addDeviceIcon',
                                itemId: 'addDeviceIconButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: me.cancelLink
                            }
                        ]
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});

