Ext.define('Cfg.store.EventTypes', {
    extend: 'Ext.data.Store',
    autoLoad: true,
    model: 'Cfg.model.EventType',


    proxy: {
        type: 'rest',
        url: '/api/evt/eventtypes',
        reader: {
            type: 'json',
            root: 'eventTypes'
        }
    }

    /*proxy: {
        type: 'ajax',
        url: './resources/data/eventtypes.json',
        reader: {
            type: 'json',
            root: 'rows'
        }
    }  */


});

