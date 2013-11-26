Ext.define('Uni.model.AppItem', {
    extend: 'Ext.data.Model',
    fields: [
        'text',
        'glyph',
        'href'
    ],
    proxy: {
        type: 'memory'
    }
});