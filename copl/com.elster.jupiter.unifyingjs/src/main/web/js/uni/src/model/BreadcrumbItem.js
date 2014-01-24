/**
 * @class Uni.model.BreadcrumbItem
 */
Ext.define('Uni.model.BreadcrumbItem', {
    extend: 'Ext.data.Model',
    fields: [
        'text',
        'href'
    ],
    associations: [
        {
            type: 'hasOne',
            model: 'Uni.model.BreadcrumbItem',
            associationKey: 'child',
            getterName: 'getChild',
            setterName: 'setChild'
        }
    ],
    proxy: {
        type: 'memory'
    }
});