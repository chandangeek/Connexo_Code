Ext.define('Fwc.devicefirmware.model.FirmwareMessage', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'uploadOption', type: 'string', useNull: true},
        {name: 'localizedValue', type: 'string', useNull: true},
        {name: 'releaseDate', type: 'int', useNull: true}
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/device/{mRID}/firmwaremessages',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'firmwareCommand'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    },
    associations: [
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function () {
                return 'Uni.property.model.Property';
            }
        }
    ]
});