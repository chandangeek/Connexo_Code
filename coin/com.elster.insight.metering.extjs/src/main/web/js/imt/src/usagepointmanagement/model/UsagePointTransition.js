Ext.define('Imt.usagepointmanagement.model.UsagePointTransition', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'usagePoint', defaultValue: null},
        {name: 'transitionNow', type: 'boolean'},
        'effectiveTimestamp'
    ],

    associations: [
        {
            name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/upl/usagepoint/{usagePointId}/transitions'
    }
});
