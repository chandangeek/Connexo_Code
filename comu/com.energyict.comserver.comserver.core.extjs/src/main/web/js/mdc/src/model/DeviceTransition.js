Ext.define('Mdc.model.DeviceTransition', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'deviceVersion', type: 'int'},
        {name: 'transitionNow', type: 'boolean'},
        {name: 'effectiveTimestamp', type: 'long', useNull: true}
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
        urlTpl: '/api/ddr/devices/{mRID}/transitions',
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});
