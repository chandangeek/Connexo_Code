Ext.define('Mdc.store.LoadProfilesOfDevice', {
    extend: 'Uni.data.store.Filterable',
    model: 'Mdc.model.LoadProfileOfDevice',
    storeId: 'LoadProfilesOfDevice',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/loadprofiles',
        reader: {
            type: 'json',
            root: 'loadProfiles'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});