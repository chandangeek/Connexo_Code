Ext.define('Imt.metrologyconfiguration.store.LinkableMetrologyConfigurations', {
    extend: 'Ext.data.Store',
    model: 'Imt.metrologyconfiguration.model.LinkableMetrologyConfiguration',
    autoLoad: false,
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/metrologyconfiguration/linkable',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function(mRID){
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});