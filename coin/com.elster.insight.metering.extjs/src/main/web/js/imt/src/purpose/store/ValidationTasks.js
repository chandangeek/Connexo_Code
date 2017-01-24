Ext.define('Imt.purpose.store.ValidationTasks', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.ValidationTask',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/validationtasks',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataValidationTasks'
        }
    }
});
