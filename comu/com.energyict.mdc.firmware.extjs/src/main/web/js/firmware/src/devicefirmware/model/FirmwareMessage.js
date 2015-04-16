Ext.define('Fwc.devicefirmware.model.FirmwareMessage', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'string', useNull: true},
        {name: 'displayValue', type: 'string', useNull: true},
        {name: 'releaseDate', type: 'date', useNull: true}
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/device/{mRID}/firmwaremessages',
        reader: {
            type: 'json',
            root: 'firmwareCommand'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    }
});