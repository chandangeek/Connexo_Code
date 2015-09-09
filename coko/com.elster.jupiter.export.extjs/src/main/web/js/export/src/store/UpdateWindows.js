Ext.define('Dxp.store.UpdateWindows', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.ExportPeriod',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/tmr/relativeperiods',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        },
        //params: {
        //    category: 'relativeperiod.category.updateWindow'
        //}
    }
});