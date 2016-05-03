Ext.define('Mdc.model.DeviceChannel', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.ReadingType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'readingType', persist:false},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'overruledObisCode', type: 'string', useNull: true},
        {name: 'nbrOfFractionDigits', type: 'number', useNull: true},
        {name: 'overflowValue', type: 'number', useNull: true}
    ],
    associations: [
        {
            name: 'readingType',
            type: 'hasOne',
            model: 'Mdc.model.ReadingType',
            associationKey: 'readingType',
            getterName: 'getReadingType',
            setterName: 'setReadingType',
            foreignKey: 'readingType'
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        urlTpl: '/api/ddr/devices/{0}/channels/',
        reader: {
            type: 'json'
        },
        setUrl: function (mRID) {
            this.url = Ext.String.format(this.urlTpl, encodeURIComponent(mRID));
        }
    }
});