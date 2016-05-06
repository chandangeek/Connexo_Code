Ext.define('Mdc.metrologyconfiguration.view.MetrologyConfigurationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrology-configurations-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    store: 'Mdc.metrologyconfiguration.store.MetrologyConfigurations',

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
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} metrology configurations'),
                displayMoreMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} metrologyconfigurations'),
                emptyMsg: Uni.I18n.translate('metrologyconfiguration.pagingtoolbartop.emptyMsg', 'MDC', 'There are no metrology configurations to display')
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