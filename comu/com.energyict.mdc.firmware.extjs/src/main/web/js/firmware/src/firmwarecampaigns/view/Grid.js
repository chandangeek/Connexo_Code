Ext.define('Fwc.firmwarecampaigns.view.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.Action',
        'Fwc.firmwarecampaigns.view.ActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.panel.FilterToolbar'
    ],
    alias: 'widget.firmware-campaigns-grid',
    store: 'Fwc.firmwarecampaigns.store.FirmwareCampaigns',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'FWC', 'Name'),
                dataIndex: 'name',
                flex: 2,
                renderer: function (value, metaData, record) {
                    return value ? '<a href="' + me.router.getRoute('workspace/firmwarecampaigns/firmwarecampaign').buildUrl({firmwareCampaignId: record.getId()}) +'">' + value + '</a>' : '';
                }
            },
            {
                header: Uni.I18n.translate('general.deviceType', 'FWC', 'Device type'),
                dataIndex: 'deviceType',
                flex: 1,
                renderer: function (value) {
                    return value ? value.localizedValue : '';
                }
            },
            {
                header: Uni.I18n.translate('firmware.campaigns.firmwareType', 'FWC', 'Firmware type'),
                dataIndex: 'firmwareType',
                flex: 1,
                renderer: function (value) {
                    return value ? value.localizedValue : '';
                }
            },
            {
                header: Uni.I18n.translate('general.version', 'FWC', 'Version'),
                dataIndex: 'firmwareVersion',
                flex: 1,
                renderer: function (value) {
                    return value && value.firmwareVersion ? value.firmwareVersion : '';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'FWC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function (value) {
                    return value ? value.localizedValue : '';
                }
            },
            {
                header: Uni.I18n.translate('general.devices', 'FWC', 'Devices'),
                dataIndex: 'devicesStatus',
                flex: 2,
                renderer: function (value, metaData) {
                    var result = '';

                    if (!Ext.isArray(value)) {
                        return result;
                    }

                    metaData.tdCls = 'firmware-campaign-status';
                    Ext.Array.each(value, function (devicesStatus) {
                        var iconCls = '';

                        switch (devicesStatus.status.id) {
                            case 'failed':
                                iconCls = 'icon-close';
                                break;
                            case 'success':
                                iconCls = 'icon-checkmark';
                                break;
                            case 'ongoing':
                                iconCls = 'icon-stop2';
                                break;
                            case 'pending':
                                iconCls = 'icon-stop2';
                                break;
                            case 'configurationError':
                                iconCls = 'icon-notification';
                                break;
                        }

                        result += '<span class="' + iconCls + '" data-qtip="' + devicesStatus.status.localizedValue + '"></span><span style="margin-right: 10px">' + devicesStatus.amount + '</span>';
                    });
                    return result;
                }
            },
            {
                header: Uni.I18n.translate('general.startedOn', 'FWC', 'Started on'),
                dataIndex: 'startedOn',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                isDisabled: function(view, rowIndex, colIndex, item, record) {
                    return record.get('status').id === 'CANCELLED';
                },
                menu: {
                    xtype: 'firmware-campaigns-action-menu',
                    itemId: 'firmware-campaigns-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'filter-toolbar',
                itemId: 'firmware-campaigns-grid-sort-toolbar',
                dock: 'top',
                title: Uni.I18n.translate('general.sort', 'FWC', 'Sort'),
                showClearButton: false,
                content: {
                    xtype: 'button',
                    ui: 'tag',
                    iconCls: 'x-btn-sort-item-desc',
                    text: Uni.I18n.translate('general.startedOn', 'FWC', 'Started on')
                }
            },
            {
                xtype: 'pagingtoolbartop',
                itemId: 'firmware-campaigns-grid-paging-toolbar-top',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmware campaigns'),
                displayMoreMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmware campaigns'),
                emptyMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbartop.emptyMsg', 'FWC', 'There are no firmware campaigns to display'),
                items: [
                    {
                        itemId: 'firmware-campaigns-add-button',
                        text: Uni.I18n.translate('firmware.campaigns.addFirmwareCampaign', 'FWC', 'Add firmware campaign'),
                        action: 'addFirmwareCampaign',
                        href: me.router.getRoute('workspace/firmwarecampaigns/add').buildUrl(),
                        privileges: Fwc.privileges.FirmwareCampaign.administrate
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemId: 'firmware-campaigns-grid-paging-toolbar-bottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('firmware.campaigns.pagingtoolbarbottom.itemsPerPage', 'FWC', 'Firmware campaigns per page')
            }
        ];

        me.callParent(arguments);
    }
});