/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.firmware-campaigns-action-menu',
    requires:[
        'Fwc.privileges.FirmwareCampaign'
    ],
    returnToCampaignOverview: false,
    privileges: Fwc.privileges.FirmwareCampaign.administrate,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('firmware.campaigns.editCampaign', 'FWC', 'Edit campaign'),
                action: me.returnToCampaignOverview ? 'editCampaignAndReturnToOverview' : 'editCampaign',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('firmware.campaigns.cancelCampaign', 'FWC', 'Cancel campaign'),
                action: 'cancelCampaign',
                section: this.SECTION_ACTION
            }
        ];
        me.callParent(arguments);
    }
});