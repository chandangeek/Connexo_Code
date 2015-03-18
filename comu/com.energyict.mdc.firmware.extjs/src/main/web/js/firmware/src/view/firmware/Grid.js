Ext.define('Fwc.view.firmware.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.firmware-grid',
    itemId: 'FirmwareGrid',
    store: null,
    requires: [
        'Fwc.view.firmware.ActionMenu'
    ],

    columns: [
        {text: 'Version', dataIndex: 'version'},
        {text: 'Type', dataIndex: 'type', flex: 1},
        {text: 'Status', dataIndex: 'status'},
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
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('firmware.pagingtoolbarbottom.itemsPerPage', 'FWC', 'Firmwares per page')
            }
        ];

        this.callParent(arguments);
    }
});