Ext.define('Fwc.view.firmware.OptionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.options-grid',
    itemId: 'OptionsGrid',
    store: null,
    columns: [
        {
            dataIndex: 'displayValue',
            flex: 1
        }
    ],
    hideHeaders: true
});