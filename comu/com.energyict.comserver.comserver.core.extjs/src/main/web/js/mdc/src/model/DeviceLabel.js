Ext.define('Mdc.model.DeviceLabel', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', mapping: function (data) {return data.category.id;}, persist: false},
        {name: 'category', type: 'auto'},
        {name: 'comment', type: 'string', useNull: true},
        {name: 'creationDate', dateFormat: 'time', type: 'date', persist: false}
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/devicelabels',
        reader: {
            type: 'json',
            root: 'deviceLabels',
            totalProperty: 'total'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});

