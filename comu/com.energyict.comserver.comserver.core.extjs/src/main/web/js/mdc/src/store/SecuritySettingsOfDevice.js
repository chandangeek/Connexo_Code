Ext.define('Mdc.store.SecuritySettingsOfDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceSecuritySetting'
    ],
    model: 'Mdc.model.DeviceSecuritySetting',
    storeId: 'SecuritySettingsOfDevice',
    proxy: {
        type: 'rest',
        urlTpl: '../../api/ddr/devices/{mRID}/securityproperties',
        reader: {
            type: 'json',
            root: 'securityPropertySets'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});
