/**
 * @class Yfn.store.ReportFilterInfos
 */
Ext.define('Yfn.store.ReportFilterInfos', {
    extend: 'Ext.data.Store',
    model: 'Yfn.model.FilterInfo',
    storeId: 'ReportFilterInfos',
    autoLoad: false,
    requires:[
       'Yfn.model.FilterInfo'
    ],

    proxy: {
        type: 'ajax',
        url: '/api/yfn/report/filter',
        reader: {
            type: 'json',
            root: 'filters'
        }
    }
});