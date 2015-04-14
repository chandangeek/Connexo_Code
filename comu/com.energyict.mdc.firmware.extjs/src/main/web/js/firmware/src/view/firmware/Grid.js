Ext.define('Fwc.view.firmware.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.firmware-grid',
    itemId: 'FirmwareGrid',
    store: null,
    requires: [
        'Fwc.view.firmware.ActionMenu'
    ],

    columns: [
        {
            text: Uni.I18n.translate('firmware.field.version', 'FWC', 'Version'),
            dataIndex: 'firmwareVersion'
        },
        {
            text: Uni.I18n.translate('firmware.field.type', 'FWC', 'Type'),
            flex: 1,
            dataIndex: 'type'
        },
        {
            text: Uni.I18n.translate('firmware.field.status', 'FWC', 'Status'),
            dataIndex: 'status'
        },
        {
            xtype: 'uni-actioncolumn',
            isDisabled: function(view, rowIndex, colIndex, item, record) {
                return Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType')
                    || (record.getAssociatedData().firmwareStatus
                    && record.getAssociatedData().firmwareStatus.id === 'deprecated'
                    );
            },
            menu: {
                xtype: 'firmware-action-menu'
            }
        }
    ],

    initComponent: function () {
        this.dockedItems = [
            {
                dock: 'top',
                title: Uni.I18n.translate('firmware.sort.title', 'FWC', 'Sort'),
                xtype: 'filter-toolbar',
                itemId: 'firmware-sort-top',
                showClearButton: false,
                content: {
                    xtype: 'button',
                    ui: 'tag',
                    iconCls: 'x-btn-sort-item-desc',
                    text: Uni.I18n.translate('firmware.sort.version', 'FWC', 'Version')
                }
            },
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMsg', 'FWC', '{0} - {1} of {2} firmware versions'),
                displayMoreMsg: Uni.I18n.translate('firmware.pagingtoolbartop.displayMoreMsg', 'FWC', '{0} - {1} of more than {2} firmware versions'),
                emptyMsg: Uni.I18n.translate('firmware.pagingtoolbartop.emptyMsg', 'FWC', 'There are no firmware versions to display'),
                items: [{
                    text: Uni.I18n.translate('firmware.add', 'FWC', 'Add firmware version'),
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