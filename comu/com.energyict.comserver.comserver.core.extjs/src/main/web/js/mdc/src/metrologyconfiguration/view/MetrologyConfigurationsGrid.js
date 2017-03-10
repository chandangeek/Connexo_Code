/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.metrologyconfiguration.view.MetrologyConfigurationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrology-configurations-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.metrologyconfiguration.view.ActionsMenu'
    ],

    store: 'Mdc.metrologyconfiguration.store.MetrologyConfigurations',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                flex: 2,
                dataIndex: 'name'
            },
            {
                header: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                flex: 4,
                dataIndex: 'description'
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                flex: 1,
                dataIndex: 'status',
                renderer: function (value) {
                    return value ? value.name : '';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.MetrologyConfiguration.canAdmin(),
                menu: {
                    xtype: 'metrology-configuration-actions-menu',
                    itemId: 'metrology-configuration-actions-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} metrology configurations'),
                displayMoreMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} metrologyconfigurations'),
                emptyMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no metrology configurations to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'metrology-configurations-grid-add-btn',
                        text: Uni.I18n.translate('general.addMetrologyConfiguration', 'MDC', 'Add metrology configuration'),
                        privileges: Mdc.privileges.MetrologyConfiguration.canAdmin(),
                        href: me.router.getRoute('administration/metrologyconfiguration/add').buildUrl()
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Metrology configurations per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});