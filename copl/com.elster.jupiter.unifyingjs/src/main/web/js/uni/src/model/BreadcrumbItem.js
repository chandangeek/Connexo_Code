/**
 * @class Uni.model.BreadcrumbItem
 */
Ext.define('Uni.model.BreadcrumbItem', {
    extend: 'Ext.data.Model',
    fields: [
        'key',
        'text',
        'href',
        {name: 'relative', type: 'boolean', defaultValue: true}
    ],

    associations: [
        {
            type: 'hasOne',
            model: 'Uni.model.BreadcrumbItem',
            associationKey: 'child',
            getterName: 'getChild',
            setterName: 'doSetChild'
        }
    ],

    proxy: {
        type: 'memory'
    },

    setChild: function (value, options, scope) {
        this.doSetChild(value, options, scope);
        return value;
    }

});