Ext.define('Mdc.store.DeviceCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',

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