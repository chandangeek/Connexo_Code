Ext.define('Isu.store.TimelineEntries', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.Timeline',
    autoLoad: false,
    proxy: 'memory'
});