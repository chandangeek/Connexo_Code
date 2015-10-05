Ext.define('Dxp.model.DataSelector', {
    extend: 'Ext.data.Model',

    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        'name',
        'displayName',
        'selectorType',
        'properties'
    ],

    idProperty: 'name',

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
    ]
});


