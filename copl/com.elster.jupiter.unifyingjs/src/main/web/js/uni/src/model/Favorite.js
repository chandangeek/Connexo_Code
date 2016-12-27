Ext.define('Uni.model.Favorite', {
    extend: 'Uni.model.ParentVersion',
    fields: ['favorite', 'comment',
        {name: 'creationDate', defaultValue: null}
    ]
});