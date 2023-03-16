/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterdata.numerical.Edit', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainEdit',
    alias: 'widget.deviceregisterreportedit-numerical',
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

    setValues: function (record) {
        var me = this;
        if (!Ext.isEmpty(record.get("readingType")) &&
            !Ext.isEmpty(record.get("readingType").names) &&
            !Ext.isEmpty(record.get("readingType").names.unitOfMeasure)) {
            me.down('#valueUnitDisplayField').setValue(record.get("readingType").names.unitOfMeasure);
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
                    labelAlign: 'right',
                    width: 650
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
                                return Uni.DateTime.formatDateTimeShort(new Date(value))
                            }
                        },
                        submitValue: true,
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'timeStampContainer',
                        required: true,
                        fieldLabel: this.hasEvent?Uni.I18n.translate('device.registerData.eventTime', 'MDC', 'Event time'):Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
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
                        xtype: 'fieldcontainer',
                        itemId: 'valueContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'value',
                                maskRe: /[0-9\.]+/,
                                itemId: 'valueTextField',
                                margin: '0 5 0 0',
                                vtype: 'checkForBlacklistCharacters',
                                allowBlank: false
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'valueUnitDisplayField',
                                renderer: function(value) {
                                    return Ext.isEmpty(value) ? '' : value;
                                }
                            }
                        ]
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

