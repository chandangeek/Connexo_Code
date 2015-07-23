Ext.define('Ddv.store.ValidationOverview', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Ddv.model.ValidationOverviewResult',
    pageSize: 10,

    proxy: {
        type: 'rest',
        urlTpl: '/api/val/validationresults/devicegroups/{groupId}',

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{groupId}', params.get('deviceGroup'));
        },
        appendId: false,
        reader: {
            type: 'json',
            root: 'summary',
            totalProperty: 'total'
        }
    }
});
