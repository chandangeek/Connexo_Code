Ext.define('Mdc.model.LogbookOfDevice', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int'},
        {name: 'name', type: 'string'},
        {name: 'obisCode', type: 'string'},
        {name: 'overruledObisCode', type: 'string'},
        {name: 'lastEventDate', dateFormat: 'time', type: 'date'},
        {name: 'lastEventType', type: 'auto'},
        {name: 'lastReading', dateFormat: 'time', type: 'date'}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/logbooks',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});