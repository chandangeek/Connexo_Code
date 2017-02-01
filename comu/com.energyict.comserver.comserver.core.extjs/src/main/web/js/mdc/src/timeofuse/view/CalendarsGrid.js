/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.CalendarsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.tou-calendars-grid',
    store: 'Mdc.timeofuse.store.UsedCalendars',
    deviceTypeId: null,
    timeOfUseAllowed: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Mdc.timeofuse.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.view,
                isDisabled: me.fnIsDisabled,
                timeOfUseAllowed: me.timeOfUseAllowed,
                menu: {
                    xtype: 'tou-devicetype-action-menu'
                },
                flex: 0.7
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('timeofuse.pagingtoolbartop.displayMsg', 'MDC', 'No time of use calendars'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('tou.addTouCalendars', 'MDC', 'Add time of use calendars'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        itemId: 'add-tou-calendars-btn',
                        disabled: !me.timeOfUseAllowed
                    }
                ],
                usesExactCount: true,
                noBottomPaging: true

            }
        ];

        me.callParent(arguments);
    },

    fnIsDisabled: function () {
        return !this.timeOfUseAllowed;
    }
});