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
            }
        ];

        me.callParent(arguments);
    }
});