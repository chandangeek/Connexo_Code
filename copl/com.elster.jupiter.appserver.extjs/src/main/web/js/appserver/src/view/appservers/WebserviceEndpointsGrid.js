/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.WebserviceEndpointsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.apr-web-service-endpoints-grid',
    width: '100%',
    maxHeight: 300,
    needLink: false,
    router: null,
    requires: [
        'Uni.grid.column.RemoveAction'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.webserviceEndpoint', 'APR', 'Web service endpoint'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, metadata, record) {

                    var url;
                    if (Ext.isEmpty(value)) {
                        return '-';
                    }
                    if (!me.needLink) {
                        return Ext.String.htmlEncode(value);
                    } else {
                        url = me.router.getRoute('administration/webserviceendpoints/view').buildUrl({endpointId: record.get('id')});
                        return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.webservice', 'APR', 'Web service'),
                dataIndex: 'webServiceName',
                flex: 1,
                renderer: function(value, field, record) {
                    if(record) {
                        if(value && record.get('available')) {
                            return value;
                        }else if(value && !record.get('available')) {
                            return value + ' (' + Uni.I18n.translate('general.notAvailable', 'APR', 'not available') + ')' + '<span class="icon-warning" style="margin-left:5px; position:absolute; color:#eb5642;"></span>';;
                        }
                    }
                    return '-';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'APR', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.active', 'APR', 'Active');
                    } else {
                        return Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn-remove',
                handler: function (grid, rowIndex, colIndex, column, event, record) {
                    this.fireEvent('removeEvent', record);
                }
            }
        ];

        me.callParent(arguments);
    }
});
