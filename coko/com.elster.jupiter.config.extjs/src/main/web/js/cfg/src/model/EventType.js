Ext.define('Cfg.model.EventType', {
    extend: 'Uni.model.Version',
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