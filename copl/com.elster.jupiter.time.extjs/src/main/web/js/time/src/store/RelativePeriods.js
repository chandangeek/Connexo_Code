Ext.define('Tme.store.RelativePeriods', {
    extend: 'Ext.data.Store',
    requires: [
        'Tme.model.RelativePeriod'
    ],
    model: 'Tme.model.RelativePeriod',
    storeId: 'ProcessRelativePeriods',
    remoteSort: true,
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/tmr/relativeperiods',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
