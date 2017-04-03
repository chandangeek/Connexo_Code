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
                            xtype: 'numberfield',
                            disabled: true,
                            width: 100,
                            value: 0,
                            itemId: 'value_number_field_' + me.key,
                        }
                    ]
                }

            ]
        };
    },


    getValueNumberField: function () {
        return this.down('#value_number_field_' + this.key);
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
            if(value.isNone){
                me.getNoneRadioField().setValue(true);
            } else {
                me.getValueRadioField().setValue(true);
                me.getValueNumberField().setValue(value.value);
            }
        } else {
            this.callParent([value]);
        }
    },
    
    getValue: function () {
        var me = this;
        return {
            isNone: me.getNoneRadioField().getValue(),
            value: me.getNoneRadioField().getValue() ? null : me.getValueNumberField().getValue()
        };
    }

});