Ext.define('Dsh.model.ConnectionTaskProperties', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
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
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dsr/connections/properties',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json'
        }
    }
});