Ext.define('Uni.property.view.property.Number', {
    extend: 'Uni.property.view.property.BaseCombo',

    getNormalCmp: function () {
        var me = this;
        var minValue = null;
        var maxValue = null;

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
        };
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

    getField: function () {
        var me = this;
        if (me.isCombo()) {
            return me.down('combobox')
        } else {
            return me.down('numberfield')
        }
    }
});