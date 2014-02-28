Ext.define('Mtr.model.types.Quantity', {
    requires: ['Ext.data.Types'],
    singleton: true,
    constructor: function () {
        Ext.data.Types.QUANTITY = {
            convert: function (value, record) {
                if (value === null) {
                    return null;
                }

                return {
                    value: value.value,
                    unit: value.unit,
                    multiplier: value.multiplier
                };
            },
            type: 'quantity'
        }
    }
});