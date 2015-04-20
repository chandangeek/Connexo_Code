
Ext.define('Fwc.devicefirmware.model.FirmwareMessageSpec', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'string', useNull: true},
        {name: 'localizedValue', type: 'string'}
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/device/{mRID}/firmwaremessagespecs',
        reader: 'json',
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    },
    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ]
});