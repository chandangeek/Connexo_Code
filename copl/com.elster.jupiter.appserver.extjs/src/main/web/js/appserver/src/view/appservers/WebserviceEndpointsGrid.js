Ext.define('Apr.view.appservers.WebserviceEndpointsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.apr-web-service-endpoints-grid',
    width: '100%',
    maxHeight: 300,
    columns: [
        {
            header: Uni.I18n.translate('general.webserviceEndpoint', 'APR', 'Webservice endpoint'),
            dataIndex: 'name',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.webservice', 'APR', 'Webservice'),
            dataIndex: 'webServiceName',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'APR', 'Status'),
            dataIndex: 'isActive',
            flex: 1,
            renderer: function (value) {
                if(value) {
                    return Uni.I18n.translate('general.active', 'APR', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
                }
            }
        },
        {
            xtype: 'actioncolumn',
            align: 'right',
            header: Uni.I18n.translate('general.actions', 'APR', 'Actions'),
            items: [
                {
                    iconCls: 'uni-icon-delete',
                    itemId: 'apr-remove-webservice-endpoint-btn',
                    tooltip: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
                    handler: function (grid, rowIndex, colIndex, column, event, record) {
                        this.fireEvent('removeEvent', record);
                    }
                }
            ]
        }
    ]
});
