Ext.define('Cfg.model.EventType', {
    extend: 'Ext.data.Model',
    fields: [
        'topic',
        'component',
        'scope',
        'category',
        'name',
        'publish'
    ],
    idProperty: 'topic'
});