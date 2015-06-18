/**
 * @class Uni.grid.column.search.DeviceType
 */
Ext.define('Uni.grid.column.search.DeviceType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-grid-column-search-devicetype',

    columnTpl: '<a href="../multisense/index.html#/administration/devicetypes/{deviceTypeId}">{value}</a>',

    renderer: function (value, metaData, record) {
        var me = metaData.column,
            deviceTypeId = record.raw.deviceTypeId,
            result = me.columnTpl;

        result = result.replace('{value}', value);
        result = result.replace('{deviceTypeId}', deviceTypeId);

        return result;
    }
});