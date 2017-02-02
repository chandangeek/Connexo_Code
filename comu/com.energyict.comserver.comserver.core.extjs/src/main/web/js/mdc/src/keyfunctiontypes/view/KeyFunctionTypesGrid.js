/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.keyfunctiontypes.view.KeyFunctionTypesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.key-function-types-grid',
    store: 'Mdc.keyfunctiontypes.store.KeyFunctionTypes',
    deviceTypeId: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Mdc.keyfunctiontypes.view.KeyFunctionTypesActionMenu'
    ],
    forceFit: true,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.keyType', 'MDC', 'Key type'),
                dataIndex: 'keyType',
                flex: 3,
                renderer: function (value) {
                    value = value && value.name ?  value.name : '-';
                    return value;
                }
            },
            {
                header: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
                dataIndex: 'validityPeriod',
                flex: 3,
                renderer: function (val) {
                    val ? val = val.count + ' ' + val.timeUnit : '-';
                    return val;
                }
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'key-function-types-action-menu',
                    itemId: 'key-function-types-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                isFullTotalCount: true,
                store: me.store,
                dock: 'top',
                emptyMsg: Uni.I18n.translate('keyfunctiontype.pagingtoolbartop.emptyMsg', 'MDC', 'No key function types'),
                displayMsg: Uni.I18n.translate('keyfunctiontype.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} key function types'),
                displayMoreMsg: Uni.I18n.translate('keyfunctiontype.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} key function types'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addKeyFunctionType', 'MDC', 'Add key function type'),
                        itemId: 'add-key-function-type',
                        deviceTypeId: me.deviceTypeId
                    }

                ]

            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('keyfunctiontype.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Key function types per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});