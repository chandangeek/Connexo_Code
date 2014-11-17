Ext.define('Uni.property.view.property.Number', {
    extend: 'Uni.property.view.property.BaseCombo',

    getNormalCmp: function () {
        var me = this;
        var minValue = null;
        var maxValue = null;
        var allowDecimals = true;
        var rule = me.getProperty().getValidationRule();

        if (rule != null) {
            minValue = rule.get('minimumValue');
            maxValue = rule.get('maximumValue');
            allowDecimals = rule.get('allowDecimals');
        }

        return {
            xtype: 'numberfield',
            name: this.getName(),
            itemId: me.key + 'numberfield',
            width: me.width,
            hideTrigger: true,
            keyNavEnabled: false,
            mouseWheelEnabled: false,
            minValue: minValue,
            maxValue: maxValue,
            allowDecimals: allowDecimals,
            msgTarget: 'under',
            readOnly: me.isReadOnly,
            inputType: me.inputType
        };
    },

    getComboCmp: function () {
        var result = this.callParent(arguments);
        result.fieldStyle = 'text-align:right;';

        return result;
    },

    getField: function () {
        return this.down('numberfield');
    }
});