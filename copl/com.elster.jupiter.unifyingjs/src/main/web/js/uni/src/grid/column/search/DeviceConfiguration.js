/**
 * @class Uni.grid.column.search.DeviceConfiguration
 */
Ext.define('Uni.grid.column.search.DeviceConfiguration', {
    extend: 'Ext.grid.column.Column',
    xtype: 'uni-grid-column-search-deviceconfiguration',

    columnTpl: '<a href="../multisense/index.html#/administration/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigurationId}">{value}</a>',

    renderer: function (value, metaData, record) {
        var me = metaData.column,
            deviceTypeId = record.raw.deviceTypeId,
            deviceConfigurationId = record.raw.deviceConfigurationId,
            result = me.columnTpl;

        result = result.replace('{value}', value);
        result = result.replace('{deviceTypeId}', deviceTypeId);
        result = result.replace('{deviceConfigurationId}', deviceConfigurationId);

        return result;
    }
});