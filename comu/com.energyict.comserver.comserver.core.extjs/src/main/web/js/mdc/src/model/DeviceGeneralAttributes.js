Ext.define('Mdc.model.DeviceGeneralAttributes', {
    extend: 'Uni.model.ParentVersion',
    fields: ['name'],
    requires: [
        'Uni.property.model.Property'
    ],
    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{mRID}/protocols'
    }

});