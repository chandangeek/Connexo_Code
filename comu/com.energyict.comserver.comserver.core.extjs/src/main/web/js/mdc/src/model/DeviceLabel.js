Ext.define('Mdc.model.DeviceLabel', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', mapping: function (data) {return data.category.id;}, persist: false},
        {name: 'category', type: 'auto'},
        {name: 'comment', type: 'string', useNull: true},
        {name: 'creationDate', type: 'auto', defaultValue: null},
        {
            name: 'parent',
            type: 'auto',
            defaultValue: null
        }
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

