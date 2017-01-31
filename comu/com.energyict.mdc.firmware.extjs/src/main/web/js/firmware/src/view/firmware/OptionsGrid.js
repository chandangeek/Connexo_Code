/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.OptionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.options-grid',
    itemId: 'OptionsGrid',
    store: null,
    columns: [
        {
            dataIndex: 'localizedValue',
            flex: 1
        }
    ],
    hideHeaders: true
});