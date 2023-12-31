/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.text.Edit', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainEdit',
    alias: 'widget.deviceregisterreportedit-text',
    itemId: 'deviceregisterreportedit',

    requires: [
        'Uni.form.field.DateTime',
        'Uni.util.FormErrorMessage'
    ],

    setEdit: function (edit, returnLink) {
        var me = this;
        me.callParent(arguments);

        if (me.isEdit()) {
            me.down('#timeStampDisplayField').setDisabled(false);
            me.down('#timeStampDisplayField').show();
            me.down('#timeStampContainer').hide();
            me.down('#timeStampContainer').setDisabled(true);
        } else {
            me.down('#timeStampDisplayField').hide();
            me.down('#timeStampDisplayField').setDisabled(true);
            me.down('#timeStampContainer').setDisabled(false);
            me.down('#timeStampContainer').show();
        }
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'registerDataEditForm',
                defaults: {
                    labelWidth: 200,
                    labelAlign: 'right'
                },
                items: [
                    {
                        itemId: 'registerDataEditFormErrors',
                        xtype: 'uni-form-error-message',
                        hidden: true
                    },
                    {
                        xtype: 'displayfield',
                        name: 'timeStamp',
                        fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                        itemId: 'timeStampDisplayField',
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
                                return Uni.DateTime.formatDateTimeShort(new Date(value));
                            }
                        },
                        submitValue: true,
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'timeStampContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                        defaults: {
                            width: '100%'
                        },
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'timeStampEditField',
                                name: 'timeStamp',
                                layout: 'hbox',
                                valueInMilliseconds: true
                            }
                        ]
                    },
                    {
                        xtype: 'textareafield',
                        enforceMaxLength: true,
                        maxLength: 4000,
                        name: 'value',
                        width: 500,
                        height: 300,
                        fieldLabel: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                        itemId: 'valueTextareaField'
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'addRegisterDataAction',
                                itemId: 'addEditButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: '#/devices'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.setEdit(me.isEdit(), me.returnLink);
    }
});

