/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.MaximumAbsoluteDifference', {
    extend: 'Uni.property.view.property.Base',

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
                    style: {
                        margin: '0 0 10px 0'
                    },
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'radiofield',
                            boxLabel: Uni.I18n.translate('value', me.translationKey, 'Value'),
                            name: 'type',
                            style: {
                                margin: '0 10px 0 0'
                            },
                            checked: true,
                            itemId: 'value_radio_' + me.key,
                            listeners: {
                                change: function(fld, newValue, oldValue, eOpts){
                                    me.getValueNumberField().setDisabled(!newValue);
                                    me.getPercentNumberField().setDisabled(newValue);
                                }
                            }
                        },
                        {
                            xtype: 'numberfield',
                            value: 0,
                            width: 100,
                            itemId: 'value_number_field_' + me.key,
                            listeners: {
                                blur: me.recurrenceNumberFieldValidation
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
                            boxLabel: Uni.I18n.translate('percent', me.translationKey, 'Percent'),
                            name: 'type',
                            style: {
                                margin: '0 10px 0 0'
                            },
                            itemId: 'percent_radio_' + me.key
                        },
                        {
                            xtype: 'numberfield',
                            minValue: 0,
                            disabled: true,
                            value: 0,
                            width: 100,
                            itemId: 'percent_number_field_' + me.key,
                            maxValue: 100,
                            listeners: {
                                blur: me.recurrenceNumberFieldValidation
                            }
                        }
                    ]
                }

            ]
        };
    },

    getValueNumberField: function () {
        return this.down('#value_number_field_' + this.key);
    },

    getPercentNumberField: function () {
        return this.down('#percent_number_field_' + this.key);
    },

    getValueRadioField: function () {
        return this.down('#value_radio_' + this.key);
    },

    getPercentRadioField: function () {
        return this.down('#percent_radio_' + this.key);
    },

    getField: function () {
        return this.down('radiogroup');
    },

    setValue: function (value) {
        var me =this;
        if(me.isEdit){
            if(value.type === 'absolute'){
                me.getValueRadioField().setValue(true);
                me.getValueNumberField().setValue(value.value);
            } else if(value.type === 'percent') {
                me.getPercentRadioField().setValue(true);
                me.getPercentNumberField().setValue(value.value);
            }
        } else {
            this.callParent([me.getValueAsDisplayString(value)]);
        }
    },

    getValueAsDisplayString: function (value) {

        if (value.type === 'absolute') {
            return value.value;
        } else if (value.type === 'percent') {
            return value.value + '%';
        } else {
            return arguments;
        }
    },

    getValue: function(){
        var me = this, result;
        if(me.getValueRadioField().getValue()){
            return {
                type: 'absolute',
                value: me.getValueNumberField().getValue()
            }
        } else {
            return {
                type: 'percent',
                value: me.getPercentNumberField().getValue()
            }
        }
    },

    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || (!Ext.isEmpty(field.minValue) && value < field.minValue)) {
            field.setValue(field.minValue);
        } else if(!Ext.isEmpty(field.maxValue) && value > field.maxValue){
            field.setValue(field.maxValue);
        }
    }
});