Ext.define('Tme.store.RelativePeriods', {
    extend: 'Ext.data.Store',
    model: 'Tme.model.RelativePeriod',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '../../api/tmr/relativeperiods',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
