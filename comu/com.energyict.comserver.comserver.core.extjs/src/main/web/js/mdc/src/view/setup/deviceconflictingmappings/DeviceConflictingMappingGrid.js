/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconflictingmappings.DeviceConflictingMappingGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-conflicting-mapping-grid',
    store: null,
    requires: [
        'Mdc.view.setup.deviceconflictingmappings.ActionMenu'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceConflictingMappings.from', 'MDC', 'From'),
                dataIndex: 'fromConfiguration',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceConflictingMappings.to', 'MDC', 'To'),
                dataIndex: 'toConfiguration',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceConflictingMappings.state', 'MDC', 'State'),
                dataIndex: 'solved',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'device-conflicting-mapping-action-menu',
                    itemId: 'device-conflicting-mapping-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'topPaging',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceConflictingMappings.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} conflicting mappings'),
                displayMoreMsg: Uni.I18n.translate('deviceConflictingMappings.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} conflicting mappings'),
                emptyMsg: Uni.I18n.translate('deviceConflictingMappings.pagingtoolbartop.emptyMsg', 'MDC', 'There are no conflicting mappings to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceConflictingMappings.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Conflicting mappings per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});