Ext.define('Mdc.model.StringResponse', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'response', type: 'string'}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '/api/mds/{resource}',
        reader: {
            type: 'json'
        },
        setUrl: function (resource) {
            this.url = this.urlTpl.replace('{resource}', resource);
        }
    }
});