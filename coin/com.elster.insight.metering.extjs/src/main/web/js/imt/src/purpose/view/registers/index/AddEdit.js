Ext.define('Imt.purpose.view.registers.index.AddEdit', {
    extend: 'Imt.purpose.view.registers.MainAddEdit',
    alias: 'widget.add-index-register-reading',
    itemId: 'add-index-register-reading',
    requires: [
    ],
    router: null,
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
        var items = [
            {
                name: 'errors',
                ui: 'form-error-framed',
                itemId: 'registerDataEditFormErrors',
                layout: 'hbox',
                margin: '0 0 10 0',
                hidden: true,
                width: 530,
                defaults: {
                    xtype: 'container'
                }
            },
            {
                xtype: 'displayfield',
                name: 'timeStamp',
                fieldLabel: me.output.get('hasEvent')?Uni.I18n.translate('usagepoint.registerData.eventTime', 'IMT', 'Event time'):Uni.I18n.translate('usagepoint.registerData.measurementTime', 'IMT', 'Measurement time'),
                itemId: 'timeStampDisplayField',
                renderer: function (value) {
                    if(!Ext.isEmpty(value)) {
                        return Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}',
                            [ Uni.DateTime.formatDateShort(new Date(value)), Uni.DateTime.formatTimeShort(new Date(value))]
                        );
                    }
                },
                submitValue: true,
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'timeStampContainer',
                required: true,
                fieldLabel: me.output.get('hasEvent')?Uni.I18n.translate('usagepoint.registerData.eventTime', 'IMT', 'Event time'):Uni.I18n.translate('usagepoint.registerData.measurementTime', 'IMT', 'Measurement time'),
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
            }];
        if(me.output.get('isBilling')){
            items.push(
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
                }
            )
        }
        items.push(
            {
                xtype: 'fieldcontainer',
                itemId: 'valueContainer',
                required: true,
                fieldLabel: Uni.I18n.translate('usagepoint.registerData.value', 'IMT', 'Value'),
                layout: 'hbox',
                items: [
                    {
                        xtype: 'textfield',
                        name: 'value',
                        maskRe: /[0-9\.]+/,
                        itemId: 'valueTextField',
                        margin: '0 5 0 0',
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
        );
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
                items: items
            }
        ];

        me.callParent(arguments);
        me.setEdit(me.isEdit(), me.returnLink);
    }
});