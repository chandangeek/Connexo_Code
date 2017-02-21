/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.ObisCode', {
    extend: 'Uni.property.view.property.ObisCodeCombo',

    requires: [
        'Uni.form.field.Obis'
    ],

    getNormalCmp: function () {
        var me = this;
        return {
            xtype: 'obis-field',
            name: this.getName(),
            itemId: me.key,
            width: me.width,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            fieldLabel: undefined,
            allowBlank: me.allowBlank,
            inputType: me.inputType,
            blankText: me.blankText,
            listeners: {
                blur: {
                    fn: me.checkValidObisCode,
                    scope: me
                }
            }
        }
    },

    checkValidObisCode: function () {
        return true;
        //var me = this,
        //    field = me.getField(),
        //    obisCode = field.getValue(),
        //    split,
        //    valid = true;
        //
        //split = obisCode.split('.');
        //
        //if(split.length !== 6) {
        //   valid = false;
        //} else {
        //    Ext.each(split, function(obisPart) {
        //            valid = !!(!Ext.isEmpty(obisPart) && !isNaN(obisPart) && obisPart >= 0 && obisPart <= 255 && valid);
        //    });
        //}
        //
        //if(valid === false) {
        //    me.getField().setValue(null);
        //}
    },

    getField: function () {
        return this.down('textfield');
    }
});