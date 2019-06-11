/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Tou.view.AddForm'
    ],
    alias: 'widget.tou-campaigns-add',
    returnLink: null,
    action: null,

    initComponent: function () {
        var me = this;

        me.content = [{
                xtype: 'tou-campaigns-add-form',
                itemId: 'tou-campaigns-add-form',
                title: Uni.I18n.translate('tou.campaigns.addTouCampaign', 'TOU', 'Add ToU calendar campaign'),
                ui: 'large',
                returnLink: me.returnLink,
                action: me.action
            }
        ];

        me.callParent(arguments);
    }
});