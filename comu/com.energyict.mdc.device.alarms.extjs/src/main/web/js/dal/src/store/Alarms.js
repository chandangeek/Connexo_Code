Ext.define('Dal.store.Alarms', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.Alarm',
    pageSize: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/isu/issues',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
