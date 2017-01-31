/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-life-cycle-transitions-grid',
    store: 'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitions',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.transition', 'DLC', 'Transition'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.from', 'DLC', 'From'),
                dataIndex: 'fromState_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.to', 'DLC', 'To'),
                dataIndex: 'toState_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.triggeredBy', 'DLC', 'Triggered by'),
                dataIndex: 'triggeredBy_name',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Dlc.privileges.DeviceLifeCycle.configure,
                menu: {
                    xtype: 'transitions-action-menu',
                    itemId: 'transitions-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceLifeCycleTransitions.pagingtoolbartop.displayMsg', 'DLC', '{0} - {1} of {2} transitions'),
                displayMoreMsg: Uni.I18n.translate('deviceLifeCycleTransitions.pagingtoolbartop.displayMoreMsg', 'DLC', '{0} - {1} of more than {2} transitions'),
                emptyMsg: Uni.I18n.translate('deviceLifeCycleTransitions.pagingtoolbartop.emptyMsg', 'DLC', 'There are no transitions to display'),
                items: [
                    {
                        xtype: 'button',
                        privileges: Dlc.privileges.DeviceLifeCycle.configure,
                        itemId: 'toolbar-button',
                        text: Uni.I18n.translate('general.addTransition', 'DLC', 'Add transition'),
                        href: me.router.getRoute('administration/devicelifecycles/devicelifecycle/transitions/add').buildUrl()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceLifeCycleTransitions.pagingtoolbarbottom.itemsPerPage', 'DLC', 'Transitions per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

