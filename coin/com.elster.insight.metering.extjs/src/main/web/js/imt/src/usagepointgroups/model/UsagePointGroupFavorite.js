Ext.define('Imt.usagepointgroups.model.UsagePointGroupFavorite', {
    extend: 'Uni.model.Favorite',

    proxy: {
        type: 'rest',
        url: '/api/udr/favorites/usagepointgroups',
        reader: {
            type: 'json'
        }
    }
});