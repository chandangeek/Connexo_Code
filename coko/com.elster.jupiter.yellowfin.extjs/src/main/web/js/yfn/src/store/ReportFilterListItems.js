/**
 * @class Yfn.store.ReportFilterListItems
 */
Ext.define('Yfn.store.ReportFilterListItems', {
    extend: 'Ext.data.Store',
    model: 'Yfn.model.FilterListItem',
    storeId: 'ReportFilterListItems',
    autoLoad: false,
    requires:[
       'Yfn.model.FilterListItem'
    ],

    proxy: {
        type: 'ajax',
        url: '/api/yfn/report/filterlistitems',
        reader: {
            type: 'json',
            root: 'listitems'
        }
    }
});