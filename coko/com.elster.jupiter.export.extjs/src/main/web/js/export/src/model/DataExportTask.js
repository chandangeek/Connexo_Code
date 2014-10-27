Ext.define('Dxp.model.DataExportTask', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        'name', 'deviceGroup', 'dataProcessor', 'schedule', 'exportperiod', 'prefix', 'separator', 'updatePeriod', 'properties', 'readingTypes'
    ],

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        },
        {
            type: 'hasMany',
            model: 'Dxp.model.ReadingType',
            associationKey: 'readingTypes',
            name: 'readingTypes'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/dxp/addexporttask'
    }
});
