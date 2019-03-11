/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.firmware-grid',
    itemId: 'FirmwareGrid',
    store: null,
    requires: [
        'Fwc.view.firmware.ActionMenu',
        'Mdc.privileges.DeviceType'
    ],
    showImageIdentifierColumn: false,
    showButtons: true,
    editOrder: false,
    maxRankValue: 0,
    isEditedRank: false,

    columns: [
        {
            text: Uni.I18n.translate('firmware.field.rank', 'FWC', 'Ranking'),
            flex: 1,
            dataIndex: 'rank',
            renderer: function (value, metaData, record, rowIndex, fullIndex, dataSource) {
                var rIndex = value ? value : '';
                var maxRankValue = this.maxRankValue;
                if (this.isEditedRank) {
                    rIndex = maxRankValue ? maxRankValue - rowIndex : rIndex;
                    Ext.each(dataSource.proxy.reader.jsonData.firmwares, function(data) {
                        if (record.getId() === data.id) data.rank = rIndex - 1;
                    });
                }
                return rIndex;
            }
        },
        {
            text: Uni.I18n.translate('general.version', 'FWC', 'Version'),
            dataIndex: 'firmwareVersion',
            flex:2
        },
        {
            text: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
            flex: 1,
            dataIndex: 'type'
        },
        {
            text: Uni.I18n.translate('general.imageIdentifier', 'FWC', 'Image identifier'),
            flex: 2,
            dataIndex: 'imageIdentifier'
        },
        {
            text: Uni.I18n.translate('firmware.field.status', 'FWC', 'Firmware status'),
            flex: 1,
            dataIndex: 'status'
        },
        {
            text: Uni.I18n.translate('firmware.field.communicationDepVersion', 'FWC', 'Min level Com FW'),
            flex: 1,
            dataIndex: 'communicationFirmwareDependency',
            renderer: function (value) {
                  return value && value.name ? Ext.String.htmlEncode(value.name) : '-';
            }
        },
        {
            text: Uni.I18n.translate('firmware.field.meterDepVersion', 'FWC', 'Min level Meter FW'),
            flex: 1,
            dataIndex: 'meterFirmwareDependency',
            renderer: function (value) {
                  return value && value.name ? Ext.String.htmlEncode(value.name) : '-';
            }
        },
        {
            xtype: 'uni-actioncolumn',
            width: 120,
            isDisabled: function(view, rowIndex, colIndex, item, record) {
                return !Mdc.privileges.DeviceType.canAdministrate()
                    || (record.getAssociatedData().firmwareStatus
                    && record.getAssociatedData().firmwareStatus.id === 'deprecated'
                    );
            },
            menu: {
                xtype: 'firmware-action-menu',
                itemId: 'firmware-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;
        var buttons = [];
        if (!this.showImageIdentifierColumn) {
            this.columns = Ext.Array.filter(this.columns,function(col){return col.dataIndex!=="imageIdentifier"})
        }

        if (me.router && me.router.queryParams && me.router.queryParams.editOrder) me.editOrder = Boolean(me.router.queryParams.editOrder);

        Ext.getStore(this.store).on('load', function(store, records, successful, eOpts){
            me.maxRankValue = records && records[0] && records[0].data && records[0].data.rank ? records[0].data.rank : 0;
        })

        if (me.editOrder) {
            me.viewConfig = {
                 plugins: {
                      ptype: 'gridviewdragdrop',
                      dragText: '&nbsp;'
                 },
                 listeners: {
                     drop: {
                         fn: function () {
                             me.isEditedRank = true;
                             me.getView().refresh();
                             me.isEditedRank = false;
                         }
                     }
                 }
            };
            me.selModel = {
                 mode: 'MULTI'
            };
            me.columns.push({
                 header: Uni.I18n.translate('general.ordering', 'EST', 'Ordering'),
                 align: 'center',
                 renderer: function () {
                        return '<span class="icon-stack3"></span>';
                 }
            });

            buttons = [
                {
                    xtype: 'button',
                    itemId: 'btn-save-firmware-version-order',
                    text: Uni.I18n.translate('general.saveOrder', 'FWC', 'Save order'),
                    action: 'saveFirmwareVersionOrder',
                },
                {
                    xtype: 'button',
                    itemId: 'btn-undo-firmware-version-order',
                    text: Uni.I18n.translate('general.undo', 'FWC', 'Undo'),
                    action: 'undoFirmwareVersionOrder',
                    href: me.router.getRoute(me.router.currentRoute).buildUrl(me.router.arguments, null),
                }
            ]
        }else{
            buttons =[{
                 text: Uni.I18n.translate('firmwareVersion.add', 'FWC', 'Add firmware version'),
                 itemId: 'addFirmware',
                 xtype: 'button',
                 action: 'addFirmware'
            },
            {
                 xtype: 'button',
                 itemId: 'btn-edit-order-estimation-rules',
                 text: Uni.I18n.translate('general.editOrder', 'FWC', 'Edit order'),
                 action: 'editOrderEstimationRules',
                 href: me.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl(me.router.arguments, {editOrder: true}),
             }]
        }
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmware versions'),
                displayMoreMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmware versions'),
                emptyMsg: Uni.I18n.translate('firmware.pagingtoolbartop.emptyMsg', 'FWC', 'There are no firmware versions to display'),
                items: buttons
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                deferLoading: true,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('firmware.pagingtoolbarbottom.itemsPerPage', 'FWC', 'Firmware versions per page')
            }
        ];

        this.callParent(arguments);
    }
});