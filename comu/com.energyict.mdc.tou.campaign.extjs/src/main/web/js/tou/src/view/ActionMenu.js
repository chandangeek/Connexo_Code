/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.tou-campaigns-action-menu',
    requires:[
        'Tou.privileges.TouCampaign'
    ],
    returnToCampaignOverview: false,
    privileges: Tou.privileges.TouCampaign.administrate,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                 text: 'Edit campaign',
                 action: me.returnToCampaignOverview ? 'editCampaignAndReturnToOverview' : 'editCampaign',
                 section: this.SECTION_EDIT
            },
            {
                text: 'Cancel campaign',
                action: 'cancelCampaign',
                section: this.SECTION_ACTION
            }
        ];
        me.callParent(arguments);
    }
});