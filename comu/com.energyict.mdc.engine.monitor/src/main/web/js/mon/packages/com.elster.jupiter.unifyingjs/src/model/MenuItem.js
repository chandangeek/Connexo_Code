/**
 * @class Uni.model.MenuItem
 */
Ext.define('Uni.model.MenuItem', {
    extend: 'Ext.data.Model',
    fields: [
        'text',
        'portal',
        'href',
        'glyph',
        'index',
        'hidden'
    ],
    proxy: {
        type: 'memory'
    }
});