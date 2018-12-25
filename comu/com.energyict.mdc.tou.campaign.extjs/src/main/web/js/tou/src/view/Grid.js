/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.Action',
        //'Fwc.firmwarecampaigns.view.ActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.panel.FilterToolbar'
    ],
    alias: 'widget.tou-campaigns-grid',
    store: 'Tou.store.TouCampaigns',
    router: null,

    initComponent: function () {
        var me = this;
        me.columns = [{
            header: 'Name',
            dataIndex: 'name',
            flex: 2,
            renderer: function (value, metaData, record) {
               return value ? Ext.String.htmlEncode(value) : '$';
            }
        },
        {
            header: 'Status',
            dataIndex: 'status',
            flex: 1,
            renderer: function (value) {
                 return value ? Ext.String.htmlEncode(value) : '';
            }
        },];

                me.dockedItems = [
                    {
                        xtype: 'pagingtoolbartop',
                        itemId: 'tou-campaigns-grid-paging-toolbar-top',
                        dock: 'top',
                        store: me.store,
                        displayMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmware campaigns'),
                        displayMoreMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmware campaigns'),
                        emptyMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.emptyMsg', 'FWC', 'There are no firmware campaigns to display'),
                        items: [
                            {
                                itemId: 'tou-campaigns-add-button',
                                text: 'Add tou campaign',
                                action: 'addTouCampaign',
                                href: me.router.getRoute('workspace/toucampaigns/add').buildUrl(),
                                privileges: Fwc.privileges.FirmwareCampaign.administrate
                            }
                        ]
                    },
                    {
                        xtype: 'pagingtoolbarbottom',
                        itemId: 'tou-campaigns-grid-paging-toolbar-bottom',
                        dock: 'bottom',
                        store: me.store,
                        itemsPerPageMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbarbottom.itemsPerPage', 'FWC', 'Firmware campaigns per page')
                    }
                ];

                me.callParent(arguments);
    }
});
