/**
 * @class Uni.model.MenuItem
 */
Ext.define('Uni.model.MenuItem', {
    extend: 'Ext.data.Model',
    fields: [
        'text',
        'href',
        'glyph',
        'index'
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Uni.model.MenuItem',
            associationKey: 'children',
            name: 'children'
        }
    ],
    proxy: {
        type: 'memory'
    }
});