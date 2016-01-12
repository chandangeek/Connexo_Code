Ext.define('Ldr.model.Pluggable', {
    extend: 'Ext.data.Model',

    fields: [
        'name',
        'basePath',
        'startPage',
        'icon',
        'mainController',
        'scripts'
    ]
});