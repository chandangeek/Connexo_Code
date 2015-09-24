Ext.define('Mdc.devicetypecustomattributes.store.CustomAttributeSets', {
    extend: 'Ext.data.Store',
    model: 'Mdc.devicetypecustomattributes.model.CustomAttributeSet',
    requires: [
        'Mdc.devicetypecustomattributes.model.CustomAttributeSet'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceTypeId}/customattributesets',

        reader: {
            type: 'json',
            root: 'customAttributeSets'
        },

        setUrl: function(deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', encodeURIComponent(deviceTypeId));
        }
    }
});