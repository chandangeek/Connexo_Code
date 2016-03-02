Ext.define('Imt.usagepointmanagement.store.UsagePointPrivileges', {
    extend: 'Ext.data.Store',
    fields: [
        'name'
    ],

    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/customproperties/{customAttributeSetId}/privileges',

        reader: {
            type: 'json',
            root: 'privileges'
        },

        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(params) {
            this.url = this.urlTpl.replace('{mRID}', params.mRID).replace('{customAttributeSetId}', params.customAttributeSetId);
        }
    }
});