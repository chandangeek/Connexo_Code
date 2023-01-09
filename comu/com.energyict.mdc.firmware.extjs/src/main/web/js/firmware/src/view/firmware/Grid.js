/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
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
            text: Uni.I18n.translate('firmware.field.rank', 'FWC', 'Rank'),
            flex: 0.75,
            dataIndex: 'rank',
            renderer: function (value, metaData, record, rowIndex, fullIndex, dataSource) {
                var rIndex = value ? value : '';
                var maxRankValue = this.maxRankValue;
                if (this.isEditedRank) {
                    rIndex = maxRankValue ? maxRankValue - rowIndex : rIndex;
                }
                return rIndex;
            }
        },
        {
            text: Uni.I18n.translate('general.version', 'FWC', 'Version'),
            dataIndex: 'firmwareVersion',
            flex: 2
        },
        {
            text: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
            flex: 2,
            dataIndex: 'type'
        },
        {
            text: Uni.I18n.translate('general.imageIdentifier', 'FWC', 'Image identifier'),
            flex: 2,
            dataIndex: 'imageIdentifier'
        },
        {
            text: Uni.I18n.translate('firmware.field.status', 'FWC', 'Firmware status'),
            flex: 2,
            dataIndex: 'status'
        },
        {
            text: Uni.I18n.translate('firmware.field.meterDepVersion', 'FWC', 'Min meter FW'),
            flex: 1.5,
            dataIndex: 'meterFirmwareDependency',
            itemId: 'minMeterLevel',
            hidden: true,
            renderer: function (value) {
                return value && value.name ? Ext.String.htmlEncode(value.name) : '-';
            }
        },
        {
            text: Uni.I18n.translate('firmware.field.communicationDepVersion', 'FWC', 'Min com FW'),
            flex: 1.5,
            itemId: 'minCommLevel',
            dataIndex: 'communicationFirmwareDependency',
            hidden: true,
            renderer: function (value) {
                return value && value.name ? Ext.String.htmlEncode(value.name) : '-';
            }
        },
        {
            text: Uni.I18n.translate('firmware.field.auxiliaryDepVersion', 'FWC', 'Min aux FW'),
            flex: 1.5,
            itemId: 'minAuxiliaryLevel',
            dataIndex: 'auxiliaryFirmwareDependency',
            hidden: true,
            renderer: function (value) {
                return value && value.name ? Ext.String.htmlEncode(value.name) : '-';
            }
        }
    ],

    initComponent: function () {
        var me = this;
        var buttons = [];
        /*if (!this.showImageIdentifierColumn) {
            this.columns = Ext.Array.filter(this.columns, function(col){ return col.dataIndex!=="imageIdentifier" });
        }*/


        if (me.router && me.router.queryParams && me.router.queryParams.editOrder) me.editOrder = Boolean(me.router.queryParams.editOrder);

        var firmwareVersionStore = Ext.getStore(this.store) || Ext.create(this.store);

        firmwareVersionStore.on('load', function (store, records, successful, eOpts) {
            me.maxRankValue = records && records[0] && records[0].data && records[0].data.rank ? records[0].data.rank : 0;
        })

        function delColbyId(itemIds, cols) {
            return Ext.Array.filter(cols, function (item) {
                var exists = false;
                Ext.Array.each(itemIds, function (itemId) {
                    if (itemId === item.itemId) exists = true;
                })
                return !exists;
            });
        }

        me.columns = delColbyId(['ordering-col', 'uni-actioncolumn'], me.columns);

        if (!me.isFirmwareCampaignVersions) {
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
                    header: Uni.I18n.translate('general.ordering', 'FWC', 'Ordering'),
                    itemId: 'ordering-col',
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
            } else {
                buttons = [
                    {
                        xtype: 'button',
                        itemId: 'btn-edit-order-estimation-rules',
                        text: Uni.I18n.translate('general.editOrder', 'FWC', 'Edit order'),
                        action: 'editOrderEstimationRules',
                        href: me.router.getRoute('administration/devicetypes/view/firmwareversions').buildUrl(me.router.arguments, {editOrder: true}),
                    },
                    {
                        text: Uni.I18n.translate('firmwareVersion.add', 'FWC', 'Add firmware version'),
                        itemId: 'addFirmware',
                        xtype: 'button',
                        action: 'addFirmware'
                    }]
                me.columns.push({
                    xtype: 'uni-actioncolumn',
                    itemId: 'uni-actioncolumn',
                    width: 120,
                    isDisabled: function () {
                        return !Mdc.privileges.DeviceType.canAdministrate();
                    },
                    menu: {
                        xtype: 'firmware-action-menu',
                        itemId: 'firmware-action-menu'
                    }
                });
            }
        }

        this.dockedItems = [{
            xtype: 'pagingtoolbartop',
            store: this.store,
            dock: 'top',
            displayMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmware versions'),
            displayMoreMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmware versions'),
            emptyMsg: Uni.I18n.translate('firmware.pagingtoolbartop.emptyMsg', 'FWC', 'There are no firmware versions to display'),
            items: buttons
        }
        ];
        this.callParent(arguments);
    }
});