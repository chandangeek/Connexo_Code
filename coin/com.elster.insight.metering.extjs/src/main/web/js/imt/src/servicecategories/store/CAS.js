Ext.define('Imt.servicecategories.store.CAS', {
    extend: 'Ext.data.Store',
    model: 'Imt.customattributesets.model.CustomAttributeSet',
    proxy: {
        type: 'rest',
        urlTpl: '/api/mtr/servicecategory/{serviceCategoryId}/custompropertysets',
        reader: {
            type: 'json',
            root: 'serviceCategoryCustomPropertySets'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUrl: function(serviceCategoryId) {
            this.url = this.urlTpl.replace('{serviceCategoryId}', encodeURIComponent(serviceCategoryId));
        }
    }
});