Ext.define('Cfg.store.EventTypes', {
    extend: 'Ext.data.Store',
    model: 'Cfg.model.EventType',

    proxy: {
        type: 'rest',
        url: '/api/evt/eventtypes',
        reader: {
            type: 'json',
            root: 'eventTypes'
        }
    }
});

