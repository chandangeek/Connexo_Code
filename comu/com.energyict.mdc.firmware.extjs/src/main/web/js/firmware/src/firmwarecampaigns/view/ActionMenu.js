Ext.define('Fwc.firmwarecampaigns.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.firmware-campaigns-action-menu',
    requires:[
        'Fwc.privileges.FirmwareCampaign'
    ],
    plain: true,
    border: false,
    shadow: false,
    returnToCampaignOverview: false,
    privileges: Fwc.privileges.FirmwareCampaign.administrate,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('firmware.campaigns.editCampaign', 'FWC', 'Edit campaign'),
                action: me.returnToCampaignOverview ? 'editCampaignAndReturnToOverview' : 'editCampaign'
            },
            {
                text: Uni.I18n.translate('firmware.campaigns.cancelCampaign', 'FWC', 'Cancel campaign'),
                action: 'cancelCampaign'
            }
        ];
        me.callParent(arguments);
    }
});