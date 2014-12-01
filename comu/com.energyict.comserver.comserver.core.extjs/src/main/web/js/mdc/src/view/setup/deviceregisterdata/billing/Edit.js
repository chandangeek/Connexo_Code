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
        if (!Ext.isEmpty(record.get("unitOfMeasure"))) {
            me.down('#valueUnitDisplayField').setValue(record.get("unitOfMeasure"));
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
                        fieldLabel: Uni.I18n.translate('device.registerData.measurementTime', 'MDC', 'Measurement time'),
                        itemId: 'timeStampDisplayField',
                        format: 'M j, Y \\a\\t G:i',
                        renderer: function (value) {
                            if(!Ext.isEmpty(value)) {
                                return Ext.util.Format.date(new Date(value), this.format);
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
                                getRawValue: true
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
                                getRawValue: true
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
                                getRawValue: true
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'valueContainer',
                        fieldLabel: Uni.I18n.translate('device.registerData.value', 'MDC', 'Value'),
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
                                padding: '0 0 0 10'
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

