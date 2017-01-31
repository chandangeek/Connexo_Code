/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.ImportServicesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.apr-import-services-grid',
    requires: [
        'Apr.view.appservers.ImportServiceActionMenu',
        'Uni.grid.column.RemoveAction'
    ],
    width: '100%',
    maxHeight: 300,
    columns: [
        {
            header: Uni.I18n.translate('general.importService', 'APR', 'Import service'),
            dataIndex: 'importService',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'APR', 'Status'),
            dataIndex: 'status',
            flex: 0.8
        },
        {
            xtype: 'uni-actioncolumn-remove',
            handler: function (grid, rowIndex, colIndex, column, event, record) {
                this.fireEvent('removeEvent', record);
            }
         }
    ]
});
