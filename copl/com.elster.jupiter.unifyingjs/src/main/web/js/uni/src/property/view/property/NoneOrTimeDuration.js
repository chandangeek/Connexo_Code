/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.NoneOrTimeDuration', {
    extend: 'Uni.property.view.property.Base',
    unitComboWidth: 128,
    gapWidth: 6,

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'radiogroup',
            itemId: me.key + 'radiogroup',
            name: this.getName(),
            vertical: true,
            columns: 1,
            items: [
                {
                    xtype: 'fieldcontainer',
                    margin: '0 0 10 0',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'radiofield',
                            boxLabel: Uni.I18n.translate('general.none', 'UNI', 'None'),
                            name: 'noneOrTimeDuration',
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
                            name: 'noneOrTimeDuration',
                            margin: '0 10 0 0',
                            itemId: 'time-duration-' + me.key
                        },
                        {
                            xtype: 'numberfield',
                            itemId: me.key + 'numberfield',
                            name: me.getName() + '.numberfield',
                            width: me.width ? me.width - me.unitComboWidth - me.gapWidth : undefined,
                            allowBlank: false,
                            disabled: true,
                            minValue: 1,
                            value: 1,
                            listeners: {
                                blur: me.recurrenceNumberFieldValidation
                            }
                        },
                        {
                            xtype: 'combobox',
                            margin: '0 0 0 5',
                            itemId: me.key + 'combobox',
                            name: me.getName() + '.combobox',
                            store: 'Uni.property.store.TimeUnits',
                            displayField: 'localizedValue',
                            valueField: 'timeUnit',
                            queryMode: 'local',
                            width: me.unitComboWidth,
                            editable: false,
                            allowBlank: false,
                            disabled: true,
                            listeners: {
                                afterrender: function (combo) {
                                    var store = combo.getStore();

                                    if (me.isEdit && me.property.get('value').isNone) {
                                        if (!store.getRange().length) {
                                            store.load(function() {
                                                if (combo.getStore()) {
                                                    combo.setValue('days');
                                                }
                                            });
                                        } else {
                                            combo.setValue('days');
                                        }
                                    }
                                }
                            }
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
        var me = this,
            setTimeDurationValues = function () {
                me.getTimeDurationNumberField().setValue(value.count);
                me.getTimeDurationComboField().setValue(value.timeUnit);
            };

        if (me.isEdit) {
            if (value.isNone) {
                me.getNoneRadioField().setValue(true);
            } else {
                me.getTimeDurationRadioField().setValue(true);
                if (!me.getTimeDurationComboField().getStore().getRange().length) {
                    me.getTimeDurationComboField().getStore().load(setTimeDurationValues);
                } else {
                    setTimeDurationValues();
                }
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
            count: me.getTimeDurationNumberField().getValue(),
            timeUnit: me.getTimeDurationComboField().getValue()
        };
    },

    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    }
});
