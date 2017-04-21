/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.NoneOrTimeDuration', {
    extend: 'Uni.property.view.property.Base',
    unitComboWidth: 128,
    gapWidth: 6,
    listeners: {
        afterrender: function () {
            if (this.isEdit) {
                this.getTimeDurationComboField().getStore().load();
            }
        }
    },

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'radiogroup',
            itemId: me.key + 'radiogroup',
            name: this.getName(),
            allowBlank: me.allowBlank,
            blankText: me.blankText,
            vertical: true,
            columns: 1,
            readOnly: me.isReadOnly,
            items: [
                {
                    xtype: 'fieldcontainer',
                    margin: '0 0 10 0',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'radiofield',
                            boxLabel: Uni.I18n.translate('general.none', 'UNI', 'None'),
                            name: 'isNone',
                            margin: '0 10 0 0',
                            checked: true,
                            itemId: 'none-' + me.key,
                            listeners: {
                                change: function (field, newValue) {
                                    me.getTimeDurationNumberField().setDisabled(newValue);
                                    me.getTimeDurationComboField().setDisabled(newValue);
                                }
                            }
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'radiofield',
                            name: 'isNone',
                            margin: '0 10 0 0',
                            itemId: 'time-duration-' + me.key
                        },
                        {
                            xtype: 'numberfield',
                            itemId: me.key + 'numberfield',
                            name: me.getName() + '.numberfield',
                            width: me.width ? me.width - me.unitComboWidth - me.gapWidth : undefined,
                            required: me.required,
                            readOnly: me.isReadOnly,
                            allowBlank: me.allowBlank,
                            blankText: me.blankText,
                            minValue: 0
                        },
                        {
                            xtype: 'combobox',
                            margin: '0 0 0 5',
                            itemId: me.key + 'combobox',
                            name: me.getName() + '.combobox',
                            store: 'Uni.property.store.TimeUnits',
                            displayField: 'localizedValue',
                            valueField: 'timeUnit',
                            width: me.unitComboWidth,
                            forceSelection: false,
                            editable: false,
                            required: me.required,
                            readOnly: me.isReadOnly,
                            allowBlank: me.allowBlank,
                            blankText: me.blankText
                        }
                    ]
                }

            ]
        };
    },

    getTimeDurationComboField: function () {
        return this.down('#' + this.key + 'combobox');
    },

    getTimeDurationNumberField: function () {
        return this.down('#' + this.key + 'numberfield');
    },

    getTimeDurationRadioField: function () {
        return this.down('#time-duration-' + this.key);
    },

    getNoneRadioField: function () {
        return this.down('#none-' + this.key);
    },

    getField: function () {
        return this.down('radiogroup');
    },

    setValue: function (value) {
        var me = this;
        if (me.isEdit) {
            if (value.isNone) {
                me.getNoneRadioField().setValue(true);
            } else {
                me.getTimeDurationRadioField().setValue(true);
                me.getTimeDurationNumberField().setValue(value.count);
                me.getTimeDurationNumberField().setValue(value.timeUnit);
            }
        } else {
            me.callParent([me.getValueAsDisplayString(value)]);
        }
    },

    getValueAsDisplayString: function (value) {
        if (value.isNone) {
            return Uni.I18n.translate('general.none', 'UNI', 'None');
        } else {
            return value ? value.count + ' ' + value.timeUnit : '';
        }
    },

    getValue: function () {
        var me = this;
        return {
            isNone: me.getNoneRadioField().getValue(),
            value: {
                count: me.getTimeDurationNumberField().getValue(),
                timeUnit: me.getTimeDurationComboField().getValue()
            }
        };
    },

    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    }
});
