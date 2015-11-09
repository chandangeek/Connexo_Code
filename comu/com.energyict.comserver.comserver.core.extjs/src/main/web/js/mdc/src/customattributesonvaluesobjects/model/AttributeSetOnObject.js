Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        {name: 'id', type: 'integer'},
        {name: 'name', type: 'string'},
        {name: 'editable', type: 'boolean'},
        {name: 'timesliced', type: 'boolean'}
    ],

    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ]
});
