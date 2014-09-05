Ext.define('Cfg.model.Validator', {
    extend: 'Ext.data.Model',

    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        'implementation',
        'displayName'
    ],

    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],

    idProperty: 'implementation',

    proxy: {
        type: 'rest',
        url: '../../api/val/validation/validators',
        reader: {
            type: 'json',
            root: 'validators'
        }
    }
});
