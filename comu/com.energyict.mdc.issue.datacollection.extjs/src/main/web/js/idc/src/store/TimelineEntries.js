Ext.define('Idc.store.TimelineEntries', {
    extend: 'Ext.data.Store',
    model: 'Idc.model.Timeline',
    autoLoad: false,
    proxy: 'memory'
});