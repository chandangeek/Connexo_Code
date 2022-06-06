/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.SecurityAccessorsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.security-accessors-grid',
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
                header: Uni.I18n.translate('general.accessorType', 'MDC', 'Accessor type'),
                dataIndex: 'isKey',
                flex: 1,
                renderer: function(value) {
                    return value
                        ? Uni.I18n.translate('general.key', 'MDC', 'Key')
                        : Uni.I18n.translate('general.certificate', 'MDC', 'Certificate');
                }
            },
            {
                header: Uni.I18n.translate('general.purpose', 'MDC', 'Purpose'),
                dataIndex: 'purpose',
                flex: 2,
                renderer: function (value) {
                    return value ? value.name : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'keyType',
                flex: 2,
                renderer: function (value) {
                    return Ext.isEmpty(value) || Ext.isEmpty(value.name) ? '-' : value.name;
                }
            },
            {
                header: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
                dataIndex: 'duration',
                flex: 1,
                renderer: function (val) {
                    return Ext.isEmpty(val) ? '-' : val.count + ' ' + val.localizedTimeUnit;
                }
            },
            {
                header: Uni.I18n.translate('general.wrappedBy', 'MDC', 'Wrapped by'),
                dataIndex: 'wrapperIdAndName',
                flex: 2,
                hidden: true,
                renderer: function (val) {
                    return Ext.isEmpty(val) ? '-' : val.name;
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: 150,
                isDisabled: function(view, rowIndex, colIndex, item, record) {
                    return !Mdc.privileges.SecurityAccessor.canAdmin();
                },
                menu: {
                    xtype: 'security-accessors-action-menu',
                    itemId: 'mdc-security-accessors-action-menu',
                    deviceTypeId: me.deviceTypeId
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
                        text: me.deviceTypeId
                            ? Uni.I18n.translate('general.addSecurityAccessors', 'MDC', 'Add security accessors')
                            : Uni.I18n.translate('general.addSecurityAccessor', 'MDC', 'Add security accessor'),
                        itemId: 'mdc-add-security-accessor-empty-grid',
                        privileges: Mdc.privileges.SecurityAccessor.canAdmin(),
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
    },

    listeners: {
        'beforerender' : function(grid) {
            if (grid.deviceTypeId == null){
                grid.columns[5].hidden = true;
            } else {
                grid.columns[5].hidden = false;
            }
        }
    }
});
