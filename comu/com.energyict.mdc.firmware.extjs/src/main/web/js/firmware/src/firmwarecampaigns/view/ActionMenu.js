Ext.define('Fwc.firmwarecampaigns.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.firmware-campaigns-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('firmware.campaigns.cancelCampaign', 'FWC', 'Cancel campaign'),
            action: 'cancelCampaign'
        }
    ]
});