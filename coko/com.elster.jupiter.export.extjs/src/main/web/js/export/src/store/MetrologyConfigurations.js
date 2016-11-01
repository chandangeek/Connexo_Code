Ext.define('Dxp.store.MetrologyConfigurations', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.IdWithName',
    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/metrologyconfigurations',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        }
    }
});