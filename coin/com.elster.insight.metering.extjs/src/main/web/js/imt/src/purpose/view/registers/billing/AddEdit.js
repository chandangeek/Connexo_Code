Ext.define('Imt.purpose.view.registers.billing.AddEdit', {
    extend: 'Imt.purpose.view.registers.MainAddEdit',
    alias: 'widget.add-billing-register-reading',
    itemId: 'add-billing-register-reading',

    requires: [
        'Uni.form.field.DateTime'
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
                    {
                        xtype: 'displayfield',
                        name: 'timeStamp',
                        fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'IMT', 'Measurement time'),
                        itemId: 'timeStampDisplayField',
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
                                return Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',
                                    [ Uni.DateTime.formatDateShort(new Date(value)), Uni.DateTime.formatTimeShort(new Date(value))]
                                )
                            }
                        },
                        submitValue: true,
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'timeStampContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('usagepoint.registerData.measurementTime', 'IMT', 'Measurement time'),
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
                        itemId: 'intervalStartContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('usagepoint.registerData.interval.start', 'IMT', 'Start of period'),
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
                        fieldLabel: Uni.I18n.translate('usagepoint.registerData.interval.end', 'IMT', 'End of period'),
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
                        fieldLabel: Uni.I18n.translate('usagepoint.registerData.value', 'IMT', 'Value'),
                        required: true,
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
                                text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'addRegisterDataAction',
                                itemId: 'addEditButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: me.returnLink
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

