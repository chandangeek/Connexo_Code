Ext.define('Mdc.view.setup.deviceregisterdata.billing.Edit', {
    extend: 'Mdc.view.setup.deviceregisterdata.MainEdit',
    alias: 'widget.deviceregisterreportedit-billing',
    itemId: 'deviceregisterreportedit',

    requires: [
        'Uni.form.field.DateTime'
    ],

    setEdit: function (edit, returnLink) {
        var me = this;
        me.callParent(arguments);

        if (me.isEdit()) {
            //me.down('#timeStampDisplayField').setDisabled(true);
            //me.down('#timeStampDisplayField').hide();
            me.down('#timeStampContainer').show();
            me.down('#timeStampContainer').setDisabled(false);
        } else {
            //me.down('#timeStampDisplayField').hide();
            //me.down('#timeStampDisplayField').setDisabled(true);
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
                    labelAlign: 'right'
                },
                items: [
                    {
                        name: 'errors',
                        ui: 'form-error-framed',
                        itemId: 'registerDataEditFormErrors',
                        layout: 'hbox',
                        margin: '0 0 10 0',
                        hidden: true,
                        defaults: {
                            xtype: 'container'
                        }
                    },
                   // {
                   //     xtype: 'fieldcontainer',
                   //     itemId: 'hiddenContainer',
                   // //    required: true,
                   //     fieldLabel: this.hasEvent?Uni.I18n.translate('device.registerData.eventTime', 'MDC', 'Event time'):Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                   ////     hidden: true,
                   //     defaults: {
                   //         width: '100%'
                   //     },
                   //     items: [
                   //         {
                   //             xtype: 'date-time',
                   //             itemId: 'timeStampField',
                   //             name: 'timeStamp',
                   //             layout: 'hbox',
                   //             valueInMilliseconds: true,
                   //             value: 0
                   //         }
                   //     ]
                   // },
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
                                name: 'eventDate',
                                layout: 'hbox',
                                valueInMilliseconds: true
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'intervalStartContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('device.registerData.interval.start', 'MDC', 'Start of period'),
                        defaults: {
                            width: '100%'
                        },
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'intervalStartField',
                                name: 'interval.start',
                                layout: 'hbox',
                                valueInMilliseconds: true
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'intervalEndtContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('device.registerData.interval.end', 'MDC', 'End of period'),
                        defaults: {
                            width: '100%'
                        },
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'intervalEndField',
                                name: 'interval.end',
                                layout: 'hbox',
                                valueInMilliseconds: true
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'valueContainer',
                        fieldLabel: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
                    //    required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'value',
                                maskRe: /[0-9\.]+/,
                                itemId: 'valueTextField',
                                allowBlank:false
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'valueUnitDisplayField',
                                padding: '0 0 0 10',
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

