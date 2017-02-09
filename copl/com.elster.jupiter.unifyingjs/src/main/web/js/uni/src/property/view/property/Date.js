/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Date', {
    extend: 'Uni.property.view.property.Base',

    format: Uni.DateTime.dateShortDefault,
    formats: [
        'd.m.Y',
        'd m Y'
    ],

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'datefield',
            name: this.getName(),
            itemId: me.key + 'datefield',
            format: me.format,
            altFormats: me.formats.join('|'),
            width: me.width,
            maxWidth: 128,
            required: me.required,
            readOnly: me.isReadOnly,
            inputType: me.inputType,
            allowBlank: me.allowBlank,
            blankText: me.blankText,
            editable: false,
            listeners: {
                change: {
                    fn: me.checkValidDate,
                    scope: me
                }
            }
        };
    },

    checkValidDate: function () {
        var me = this,
            date = me.getField().getValue();

        if (!Ext.isDate(date)) {
            me.getField().setValue(null);
        }
    },

    getField: function () {
        return this.down('datefield');
    },

    markInvalid: function (error) {
        this.down('datefield').markInvalid(error);
    },

    clearInvalid: function (error) {
        this.down('datefield').clearInvalid();
    },

    setValue: function (value /*Date in miliseconds*/) {
        if (!Ext.isEmpty(value)) {
            if (!this.isEdit) {
                value = this.getValueAsDisplayString(value);
                this.callParent([value]);
            } else {
                value = new Date(value);
                // verify if value is a valid datetime
                if (!isNaN( value.getTime())){
                    this.callParent([value]);
                }
            }
        }

    },

    getValue: function () {
        if (this.getField().getValue() != null) {
            return this.getField().getValue().getTime()
        } else {
            return null;
        }
    },

    getValueAsDisplayString: function (value /*Date as miliseconds*/) {
        return !Ext.isEmpty(value) ? Uni.DateTime.formatDateTimeShort(new Date(value)) : value;
    }

});