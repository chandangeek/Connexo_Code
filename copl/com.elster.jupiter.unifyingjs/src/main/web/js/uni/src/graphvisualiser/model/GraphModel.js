Ext.define('Uni.graphvisualiser.model.GraphModel', {
    extend: 'Ext.data.Model',
    fields: ['nodes','links'],
    requires: [
        'Uni.graphvisualiser.model.LinkModel',
        'Uni.graphvisualiser.model.NodeModel'
    ],
    associations: [
        {
            name: 'nodes',
            type: 'hasMany',
            model: 'Uni.graphvisualiser.model.NodeModel',
            associationKey: 'nodes',
            foreignKey: 'nodes'
        },
        {
            name: 'links',
            type: 'hasMany',
            model: 'Uni.graphvisualiser.model.LinkModel',
            associationKey: 'links'
        }
    ]
});