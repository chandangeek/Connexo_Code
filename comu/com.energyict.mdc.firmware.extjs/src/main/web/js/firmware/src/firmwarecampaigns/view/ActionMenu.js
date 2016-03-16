Ext.define('Fwc.firmwarecampaigns.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.firmware-campaigns-action-menu',
    requires:[
        'Fwc.privileges.FirmwareCampaign'
    ],
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('firmware.campaigns.editCampaign', 'FWC', 'Edit campaign'),
            action: 'editCampaign',
            privileges: Fwc.privileges.FirmwareCampaign.administrate
        },
        {
            text: Uni.I18n.translate('firmware.campaigns.cancelCampaign', 'FWC', 'Cancel campaign'),
            action: 'cancelCampaign'
        }
    ]
});