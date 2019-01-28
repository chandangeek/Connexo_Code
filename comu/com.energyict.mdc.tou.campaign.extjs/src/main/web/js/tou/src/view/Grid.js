/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.Action',
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
            header: Uni.I18n.translate('general.name', 'TOU', 'Name'),
            dataIndex: 'name',
            flex: 2,
            renderer: function (value, metaData, record) {
               return value ? '<a href="' + me.router.getRoute('workspace/toucampaigns/toucampaign').buildUrl({touCampaignName : value}) + '">' + Ext.String.htmlEncode(value) + '</a>' : '-';
            }
        },
        {
             header: Uni.I18n.translate('general.deviceType', 'TOU', 'Device type'),
             dataIndex: 'deviceType',
             flex: 2,
             renderer: function (value) {
                   return value && value.name ? Ext.String.htmlEncode(value.name) : '-';
             }
        },
        {
              header: Uni.I18n.translate('general.updateType', 'TOU', 'Update type'),
              dataIndex: 'updateType',
              flex: 1,
              renderer: function (value) {
                    var res = '-';
                    if (value){
                            switch(value){
                                   case 'fullCalendar':
                                         res = 'Full calendar';
                                         break;
                                   case 'specialDays':
                                         res = 'Only special days';
                                         break;
                                   default:
                                         res = value;
                                         break;
                            }
                    }
              return res;
              }
        },
        {
              header: Uni.I18n.translate('general.calendar', 'TOU', 'Calendar'),
              dataIndex: 'calendar',
              flex: 1,
              renderer: function (value) {
                     return value && value.name ? Ext.String.htmlEncode(value.name) : '-';
              }
        },
        {
            header: Uni.I18n.translate('general.status', 'TOU', 'Status'),
            dataIndex: 'status',
            flex: 1,
            renderer: function (value) {
                 return value ? Ext.String.htmlEncode(value) : '-';
            }
        },
        {
              header: Uni.I18n.translate('general.devices', 'TOU', 'Devices'),
              dataIndex: 'devices',
              flex: 2,
              renderer: function (value, field) {
                       var result = '';

                       if (!Ext.isArray(value)) {
                            return result;
                       }

                       field.tdCls = 'tou-campaign-status';
                       Ext.Array.each(value, function (devicesStatus, index) {
                                  var iconCls = '';

                                  switch (devicesStatus.status) {
                                       case 'Failed':
                                           iconCls = 'icon-cancel-circle';
                                       break;
                                       case 'Successful':
                                            iconCls = 'icon-checkmark-circle';
                                       break;
                                       case 'Ongoing':
                                            iconCls = 'icon-spinner3';
                                       break;
                                       case 'Pending':
                                            iconCls = 'icon-forward2';
                                       break;
                                       case 'Configuration Error':
                                            iconCls = 'icon-notification';
                                       break;
                                       case 'Canceled':
                                            iconCls = 'icon-blocked';
                                       break;
                                  }

                                  if (index) {
                                        result += ' ';
                                  }

                                  result += '<span class="' + iconCls + '" data-qtip="' + devicesStatus.status + '"></span>' + devicesStatus.quantity;
                       });
                       return result;
              }
        },{
            header: Uni.I18n.translate('general.startedOn', 'TOU', 'startedOn'),
            dataIndex: 'startedOn',
            flex: 2,
            renderer: function (value) {
                 return value ? Ext.String.htmlEncode(Uni.DateTime.formatDateTimeShort(value)) : '-';
            }
        },
        {
            xtype: 'uni-actioncolumn',
            width: 120,
            privileges: Tou.privileges.TouCampaign.administrate,
            isDisabled: function(view, rowIndex, colIndex, item, record) {
                return record.get('status') !== 'Ongoing';
            },
            menu: {
                xtype: 'tou-campaigns-action-menu',
                itemId: 'tou-campaigns-action-menu'
            }
        }
];

                me.dockedItems = [
                    {
                        xtype: 'pagingtoolbartop',
                        itemId: 'tou-campaigns-grid-paging-toolbar-top',
                        dock: 'top',
                        store: me.store,
                        displayMsg: Uni.I18n.translate('tou.campaigns.pagingtoolbartop.displayMsg', 'TOU', '{0} - {1} of {2} firmware campaigns'),
                        displayMoreMsg: Uni.I18n.translate('tou.campaigns.pagingtoolbartop.displayMoreMsg', 'TOU', '{0} - {1} of more than {2} firmware campaigns'),
                        emptyMsg: Uni.I18n.translate('tou.campaigns.pagingtoolbartop.emptyMsg', 'TOU', 'There are no firmware campaigns to display'),
                        items: [
                            {
                                itemId: 'tou-campaigns-add-button',
                                text: Uni.I18n.translate('tou.campaigns.addTouCampaign', 'TOU', 'Add ToU campaign'),
                                action: 'addTouCampaign',
                                href: me.router.getRoute('workspace/toucampaigns/add').buildUrl(),
                                privileges: Tou.privileges.TouCampaign.administrate
                            }
                        ]
                    },
                    {
                        xtype: 'pagingtoolbarbottom',
                        itemId: 'tou-campaigns-grid-paging-toolbar-bottom',
                        dock: 'bottom',
                        store: me.store,
                        itemsPerPageMsg: Uni.I18n.translate('tou.campaigns.pagingtoolbarbottom.itemsPerPage', 'TOU', 'ToU campaigns per page')
                    }
                ];

                me.callParent(arguments);
    }
});
