Ext.define('Imt.usagepointsetup.model.EffectiveMetrologyConfig', {
    extend: 'Imt.metrologyconfiguration.model.MetrologyConfiguration',
    fields: [
        {
            name: 'meterRoles', type: 'auto'
        }
    ],

    proxy: {
        urlTpl: '/api/udr/usagepoints/{mrid}/metrologyconfiguration',
        type: 'rest',
        url: '/api/udr/usagepoints/{mrid}/metrologyconfiguration',
        timeout: 240000,
        reader: {
            type: 'json',
        },
        setUrl: function (mrid) {
            this.url = this.urlTpl.replace('{mrid}', encodeURIComponent(mrid));
        }
    }
});
