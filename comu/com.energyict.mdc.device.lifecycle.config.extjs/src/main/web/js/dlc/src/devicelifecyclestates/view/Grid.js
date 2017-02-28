/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecyclestates.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-life-cycle-states-grid',
    store: 'Dlc.devicelifecyclestates.store.DeviceLifeCycleStates',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dlc.devicelifecyclestates.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                xtype: 'uni-default-column',
                header: Uni.I18n.translate('general.initial', 'DLC', 'Initial'),
                dataIndex: 'isInitial',
                minWidth: 70,
                width: 70
            },
            {
                header: Uni.I18n.translate('general.state', 'DLC', 'State'),
                dataIndex: 'sorted_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.stage', 'DLC', 'Stage'),
                dataIndex: 'stage',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                dynamicPrivilege: Dlc.dynamicprivileges.DeviceLifeCycle.viable,
                privileges: Dlc.privileges.DeviceLifeCycle.configure,
                menu: {
                    xtype: 'device-life-cycle-states-action-menu',
                    itemId: 'statesActionMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceLifeCycleStates.pagingtoolbartop.displayMsg', 'DLC', '{0} - {1} of {2} states'),
                displayMoreMsg: Uni.I18n.translate('deviceLifeCycleStates.pagingtoolbartop.displayMoreMsg', 'DLC', '{0} - {1} of more than {2} states'),
                emptyMsg: Uni.I18n.translate('deviceLifeCycleStates.pagingtoolbartop.emptyMsg', 'DLC', 'There are no states to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-state-button',
                        text: Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'),
                        action: 'addState',
                        dynamicPrivilege: Dlc.dynamicprivileges.DeviceLifeCycle.viable,
                        privileges: Dlc.privileges.DeviceLifeCycle.configure
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceLifeCycleStates.pagingtoolbarbottom.itemsPerPage', 'DLC', 'States per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

