Ext.define('Dsh.store.FavoriteDeviceGroups', {
    extend: 'Ext.data.Store',
    storeId: 'FavoriteDeviceGroups',
    requires: ['Dsh.model.DeviceGroup'],
    model: 'Dsh.model.DeviceGroup',
    sorters: [{direction: 'ASC', property: 'name'}],
    proxy: {
        type: 'ajax',
//        url: 'http://localhost:8080/apps/dsh/src/fake/favoritesgroups.json',
        url: '../../api/dsr/favoritedevicegroups',
        reader: {
            type: 'json',
            root: 'favoriteDeviceGroups'
        }
    }
});