/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificateFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    store: 'Pkj.store.Certificates',
    xtype: 'certificateFilter',

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'text',
                dataIndex: 'alias',
                emptyText: Uni.I18n.translate('general.alias', 'PKJ', 'Alias')
            },
            //{
            //    type: 'combobox',
            //    dataIndex: 'queue',
            //    emptyText: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
            //    multiSelect: true,
            //    displayField: 'queue',
            //    valueField: 'queue',
            //    store: 'Apr.store.Queues'
            //    //   hidden: !me.includeServiceCombo
            //},
            //{
            //    type: 'interval',
            //    dataIndex: 'startedBetween',
            //    dataIndexFrom: 'startedOnFrom',
            //    dataIndexTo: 'startedOnTo',
            //    text: Uni.I18n.translate('validationtask.historyFilter.startedBetween', 'APR', 'Started between')
            //}
        ];

        me.callParent(arguments);
    }
});