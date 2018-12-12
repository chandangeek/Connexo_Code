/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorissueprocesses.view.AlarmProcessesMainView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bpm-alarm-processes-main-view',
    requires: [
        'Bpm.monitorissueprocesses.view.AlarmProcesses',
        'Bpm.monitorissueprocesses.view.IssueProcessPreview'
    ],
    store:{},
    properties: {},

    title: Uni.I18n.translate('processes.title', 'BPM', 'Processes'),
    ui: 'large',
    margin: '0 0 0 15',
    border: true,
    overflowY: 'auto',
    items: [
        {
            xtype: 'bpm-alarm-processes',
            margin: '0 0 0 -15',
            itemId: 'alarm-processes',
        }
    ],
    initComponent: function () {
        var me = this;
        me.fireEvent('initStores',this.properties);
        me.callParent(arguments);

    }

});