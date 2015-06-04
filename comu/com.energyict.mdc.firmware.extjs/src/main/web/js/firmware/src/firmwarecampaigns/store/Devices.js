Ext.define('Fwc.firmwarecampaigns.store.Devices', {
    extend: 'Ext.data.Store',
    model: 'Fwc.firmwarecampaigns.model.Device',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/campaigns/{firmwareCampaignId}/devices',
        reader: {
            type: 'json',
            root: 'devices'
        },
        setUrl: function (firmwareCampaignId) {
            this.url = this.urlTpl.replace('{firmwareCampaignId}', firmwareCampaignId);
        }
    }
});