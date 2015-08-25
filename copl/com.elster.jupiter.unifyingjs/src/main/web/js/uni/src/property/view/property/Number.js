Ext.define('Uni.property.view.property.Number', {
    extend: 'Uni.property.view.property.BaseCombo',

    getNormalCmp: function () {
        var me = this;

        // quick fix. Extjs numberfield works incorrect with numbers which are not in range below.
        // To allow usage of any number, this should be transformed to textfield. (also backend changes need to be done)
        var minValue = -9000000000000000;
        var maxValue = 9000000000000000;

        return {
            xtype: 'numberfield',
            name: this.getName(),
            itemId: me.key + 'numberfield',
            width: me.width,
            hideTrigger: false,
            keyNavEnabled: false,
            mouseWheelEnabled: false,
            minValue: minValue,
            maxValue: maxValue,
            allowDecimals: false,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            inputType: me.inputType,
            allowBlank: me.allowBlank,
            listeners: {
                change: {
                    fn: me.checkValidNumber,
                    scope: me
                }
            }
        };
    },

    checkValidNumber: function () {
        var me = this,
            field = me.getField(),
            number = field.getValue();

        if (Ext.isNumber(number)) {
            if (number > field.maxValue) me.getField().setValue(field.maxValue);
            if (number < field.minValue) me.getField().setValue(field.minValue);
        } else {
            me.getField().setValue(null);
        }
    },

    getDisplayCmp: function () {
        return {
            xtype: 'displayfield',
            name: this.getName(),
            itemId: this.key + 'displayfield'
        }
    },

    getComboCmp: function () {
        var result = this.callParent(arguments);
        result.fieldStyle = 'text-align:right;';

        return result;
    },

    markInvalid: function (error) {
        this.down('numberfield').markInvalid(error);
    },

    clearInvalid: function () {
        this.down('numberfield').clearInvalid();
    },

    getField: function () {
        var me = this;
        if (me.isCombo()) {
            return me.down('combobox')
        } else {
            return me.down('numberfield')
        }
    }
});