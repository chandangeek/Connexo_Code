/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.servicecalls-grid',
    store: 'Scs.store.ServiceCalls',
    menuItemId: '',
    router: null,
    actionMenuHidden: false,
    cancelAllHidden: true,
    usesExactCount: false,
    defaultPageSize: 10,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Scs.view.ActionMenu',
        'Scs.view.object.CancelAllActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.serviceCall', 'SCS', 'Service call'),
                 dataIndex: 'name',
                 renderer: function (value, metaData, record) {
                     var url = '#/workspace/servicecalls/';
                     url += record.get('id');
                     return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                 },
                 flex: 1
            },
            {
                header: Uni.I18n.translate('servicecalls.externalReference', 'SCS', 'External reference'),
                dataIndex: 'externalReference',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'SCS', 'Type'),
                dataIndex: 'type',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'SCS', 'Status'),
                dataIndex: 'state',
                flex: 0.8,
                renderer: function(value) {
                    if(value.displayValue) {
                        return value.displayValue;
                    } else {
                        return '-';
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.receivedDate', 'SCS', 'Received date'),
                dataIndex: 'creationTimeDisplayShort',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.modificationDate', 'SCS', 'Modification date'),
                dataIndex: 'lastModificationTimeDisplayShort',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Scs.privileges.ServiceCall.admin,
                menu: {
                    xtype: 'scs-action-menu',
                    itemId: me.menuItemId
                },
                isDisabled: me.fnIsDisabled,
                hidden: me.actionMenuHidden,
                flex: 0.7
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('serviceCalls.pagingtoolbartop.displayMsg', 'SCS', '{0} - {1} of {2} service calls'),
                displayMoreMsg: Uni.I18n.translate('serviceCalls.pagingtoolbartop.displayMoreMsg', 'SCS', '{0} - {1} of more than {2} service calls'),
                emptyMsg: Uni.I18n.translate('serviceCalls.pagingtoolbartop.emptyMsg', 'SCS', 'There are no service calls to display'),
                usesExactCount: me.usesExactCount,
                items: [
                    {
                        xtype: 'uni-button-action',
                        privileges: Scs.privileges.ServiceCall.admin,
                        itemId: 'cancelAllServiceCallsButton',
                        menu: {
                            xtype: 'cancel-all-action-menu'
                        },
                        hidden: me.cancelAllHidden
                    }
                ]

            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('serviceCalls.pagingtoolbarbottom.itemsPerPage', 'SCS', 'Service calls per page'),
                dock: 'bottom',
                defaultPageSize: me.defaultPageSize
            }
        ];

        me.callParent(arguments);
    },

    fnIsDisabled: function (view, rowIndex, colIndex, item, record) {
        return !record.get('canCancel')
    }
});