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

    columns: [
        {
            text: Uni.I18n.translate('firmware.field.rank', 'FWC', 'Rank'),
            flex: 1,
            dataIndex: 'rank'
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
        if (!this.showImageIdentifierColumn) {
            Ext.Array.erase(this.columns, 2,2); //Remove the column identifier
        }
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmware versions'),
                displayMoreMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmware versions'),
                emptyMsg: Uni.I18n.translate('firmware.pagingtoolbartop.emptyMsg', 'FWC', 'There are no firmware versions to display'),
                items: [{
                    text: Uni.I18n.translate('firmwareVersion.add', 'FWC', 'Add firmware version'),
                    itemId: 'addFirmware',
                    xtype: 'button',
                    action: 'addFirmware'
                }]
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