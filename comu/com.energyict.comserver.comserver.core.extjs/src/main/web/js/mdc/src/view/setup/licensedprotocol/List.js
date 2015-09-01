Ext.define('Mdc.view.setup.licensedprotocol.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.setupLicensedProtocols',
    itemId: 'licensedprotocolgrid',
    title: Uni.I18n.translate('protocol.allLicensedProtocols','MDC','All licensed protocols'),
    store: 'LicensedProtocols',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    columns: {
        defaults: {
            flex: 1
        },
        items: [
            { header: 'Id', dataIndex: 'licensedProtocolRuleCode'},
            { header: 'Protocol name', dataIndex: 'protocolName'},
            { header: 'Java class name', dataIndex: 'protocolJavaClassName'}
        ]
    },
    initComponent: function () {
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top'
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom'
            }
        ];
        this.callParent(arguments);
    }
});