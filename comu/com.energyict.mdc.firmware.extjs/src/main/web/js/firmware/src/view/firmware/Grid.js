Ext.define('Fwc.view.firmware.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.firmware-grid',
    itemId: 'FirmwareGrid',
    store: null,
    requires: [
        'Fwc.view.firmware.ActionMenu'
    ],

    columns: [
        {text: 'Version', dataIndex: 'firmwareVersion'},
        {text: 'Type', dataIndex: 'firmwareType', flex: 1},
        {text: 'Status', dataIndex: 'firmwareStatus'},
        {
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'firmware-action-menu'
            }
        }
    ],

    initComponent: function () {
        this.dockedItems = [
            {
                dock: 'top',
                title: 'Sort',
                xtype: 'filter-toolbar',
                itemId: 'firmware-sort-top',
                showClearButton: false,
                content: {
                    xtype: 'button',
                    ui: 'tag',
                    iconCls: 'x-btn-sort-item-desc',
                    text: 'Version'
                }
            },
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmwares'),
                displayMoreMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmwares'),
                emptyMsg: Uni.I18n.translate('firmware.pagingtoolbartop.emptyMsg', 'FWC', 'There are no firmwares to display'),
                items: [
                    '->',
                    {
                        text: Uni.I18n.translate('firmware.createDeviceType', 'FWC', 'Add firmware'),
                        itemId: 'addFirmware',
                        xtype: 'button',
                        action: 'addFirmware'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                deferLoading: true,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('firmware.pagingtoolbarbottom.itemsPerPage', 'FWC', 'Firmwares per page')
            }
        ];

        this.callParent(arguments);
    }
});