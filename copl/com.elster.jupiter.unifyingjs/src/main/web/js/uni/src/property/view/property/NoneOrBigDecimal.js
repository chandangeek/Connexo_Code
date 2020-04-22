/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.NoneOrBigDecimal', {
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
                            boxLabel: Uni.I18n.translate('none', me.translationKey, 'None'),
                            name: 'isNone',
                            style: {
                                margin: '0 10px 0 0'
                            },
                            checked: true,
                            itemId: 'none_radio_' + me.key,
                            listeners: {
                                change: function(fld, newValue, oldValue, eOpts){
                                    me.getValueNumberField().setDisabled(newValue);
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
                            boxLabel: Uni.I18n.translate('value', me.translationKey, 'Value'),
                            name: 'isNone',
                            style: {
                                margin: '0 10px 0 0'
                            },
                            itemId: 'value_radio_' + me.key
                        },
                        {
                            xtype: 'textfield',
                            disabled: true,
                            width: 100,
                            minValue: 0,
                            value: 0,
                            itemId: 'value_bigdecimal_field_' + me.key,
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
        return this.down('#value_bigdecimal_field_' + this.key);
    },

    getValueRadioField: function () {
        return this.down('#value_radio_' + this.key);
    },

    getNoneRadioField: function () {
        return this.down('#none_radio_' + this.key);
    },

    getField: function () {
        return this.down('radiogroup');
    },

    setValue: function (value) {
        var me =this;
        if(me.isEdit){
            me.getValueRadioField().setValue(true);
            if (value && value.value) {
                /*
                    Properties is a common feature which using a many and many places in connexo and sometimes they have a slightly different format
                    This behaviour is coded in base property class(Base.js) and now this is extended by NoneOrBigDecimal property within CONM-1364
                */
                if (Ext.isObject(value.value) && value.value.value){
                    value = value.value;
                }
                if (!value.isNone){
                    me.getValueNumberField().setValue(value.value);
                }
            }
        } else {
            this.callParent([me.getValueAsDisplayString(value)]);
        }
    },

    getValueAsDisplayString: function (value) {
        var me = this;
        if (value.isNone) {
            return Uni.I18n.translate('value.none1', me.translationKey, 'None');
        } else{
            return value.value;
        }
    },
    
    getValue: function () {
        var me = this;
        return {
            isNone: me.getNoneRadioField().getValue(),
            value: me.getNoneRadioField().getValue() ? null : me.getValueNumberField().getValue()
        };
    },

    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    },

    initListeners: function () {
        var me = this,
            resetButtonListener = function(){
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            };

        this.callParent(arguments);
        me.getValueNumberField() && me.getValueNumberField().on('change', resetButtonListener);
        me.getValueRadioField() && me.getValueRadioField().on('change', resetButtonListener);
    }

});