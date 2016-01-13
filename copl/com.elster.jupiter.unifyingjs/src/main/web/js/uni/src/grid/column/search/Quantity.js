/**
 * @class Uni.grid.column.search.Quantity
 */
Ext.define('Uni.grid.column.search.Quantity', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-grid-column-search-quantity',


    renderer: function (value, metaData, record) {

        if (value) {
            if (value.multiplier == 0)
                return value.value + ' ' + value.unit;
            else
                return value.value + '*10<sup style="vertical-align: top; position: relative; top: -0.5em;">' + value.multiplier + '</sup> ' + value.unit;

        } else return ' ';
    }
});