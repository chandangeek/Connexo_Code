/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceloadprofiles.Data', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfilesData',
    itemId: 'deviceLoadProfilesData',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.TableView',
        'Mdc.view.setup.deviceloadprofiles.GraphView',
        'Mdc.view.setup.deviceloadprofiles.LoadProfileTopFilter'
    ],

    router: null,
    loadProfile: null,
    channels: null,
    layout: 'fit',

    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'button',
                itemId: 'deviceLoadProfilesTableViewBtn',
                text: Uni.I18n.translate('deviceloadprofiles.tableView', 'MDC', 'Table view'),
                action: 'showTableView',
                disabled: me.isTable,
                ui: 'link'
            },
            {
                xtype: 'tbtext',
                text: '|'
            },
            {
                xtype: 'button',
                itemId: 'deviceLoadProfilesGraphViewBtn',
                text: Uni.I18n.translate('deviceloadprofiles.graphView', 'MDC', 'Graph view'),
                action: 'showGraphView',
                disabled: !me.isTable,
                ui: 'link'
            }
        ];

        me.tbar = {
            xtype: 'mdc-loadprofiles-topfilter',
            itemId: 'deviceloadprofilesdatafilterpanel',
            hasDefaultFilters: true,
            filterDefault: me.filter
        };

        me.items = me.widget;

        me.callParent(arguments);
    }
});

