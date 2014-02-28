Ext.define('Mtr.model.Quantity', {
    extend: 'Ext.data.Model',
    fields: [
        'value',
        'unit', // Symbol.
        'multiplier'
    ],

    toString: function () {
        return this.data.value +
            ' ' + this.data.unit;
    }
});