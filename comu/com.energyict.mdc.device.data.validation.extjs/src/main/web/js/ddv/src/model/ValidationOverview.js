Ext.define('Ddv.model.ValidationOverview', {
    extend: 'Ext.data.Model',
    requires: [
        'Ddv.model.ValidationOverviewResult',
        //'Dsh.model.Filterable'
    ],
    proxy: {
        type: 'ajax',
        urlTpl: '/api/ddr/devicegroups/{groupId}/devices',

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{groupId}', params.get('deviceGroup'));
        }
    }
});
