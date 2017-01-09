Ext.define('Imt.usagepointmanagement.model.UsagePointFavorite', {
    extend: 'Uni.model.Favorite',

    proxy: {
        type: 'rest',
        url: '/api/udr/favorites/usagepoints',
        reader: {
            type: 'json'
        }
    }
});