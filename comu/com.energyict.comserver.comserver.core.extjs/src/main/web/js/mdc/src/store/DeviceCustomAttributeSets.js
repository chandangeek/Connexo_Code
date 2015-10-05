Ext.define('Mdc.store.DeviceCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Cps.common.valuesobjects.model.AttributeSetOnObject'
    ],
    model: 'Cps.common.valuesobjects.model.AttributeSetOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/mds/devices/{mRID}/custompropertysets',
        reader: {
            type: 'json',
            root: 'custompropertysets'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});