/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.SecurityAccessorsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.security-accessors-grid',
    store: 'Mdc.securityaccessors.store.SecurityAccessors',
    deviceTypeId: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Mdc.securityaccessors.view.SecurityAccessorsActionMenu'
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
                    xtype: 'security-accessors-action-menu',
                    itemId: 'mdc-security-accessors-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                isFullTotalCount: true,
                store: me.store,
                dock: 'top',
                emptyMsg: Uni.I18n.translate('securityaccessors.pagingtoolbartop.emptyMsg', 'MDC', 'No security accessors'),
                displayMsg: Uni.I18n.translate('securityaccessors.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} security accessors'),
                displayMoreMsg: Uni.I18n.translate('securityaccessors.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} security accessors'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addSecurityAccessor', 'MDC', 'Add security accessor'),
                        itemId: 'mdc-add-security-accessor',
                        deviceTypeId: me.deviceTypeId
                    }

                ]

            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('securityaccessors.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Security accessors per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});