Ext.define('Mdc.view.setup.searchitems.SearchResults', {
    extend: 'Ext.grid.Panel',
    requires:[
        'Yfn.privileges.Yellowfin'
    ],
    alias: 'widget.searchResults',
    overflowY: 'auto',
    itemId: 'searchResults',
    selModel: {
        mode: 'SINGLE'
    },
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID'),
                dataIndex: 'mRID',
                sortable: false,
                hideable: false,
                renderer: function (value, b, record) {
                    return '<a href="#/devices/' + encodeURIComponent(record.get('mRID')) + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number'),
                dataIndex: 'serialNumber',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.type', 'MDC', 'Type'),
                dataIndex: 'deviceTypeName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('searchItems.configuration', 'MDC', 'Configuration'),
                dataIndex: 'deviceConfigurationName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'searchItemsToolbarTop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('devices.pagingtoolbartop.emptyMsg', 'MDC', 'There are no devices to display'),
                items: [
                    {
                        xtype: 'button',
                        privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                        action: 'bulk',
                        itemId: 'searchResultsBulkActionButton',
                        text: Uni.I18n.translate('general.bulkAction', 'MDC', 'Bulk action')
                    },
                    {
                        xtype:'button',
                        privileges: Yfn.privileges.Yellowfin.view,
                        itemId:'generate-report',
                        action: 'generate-report',
                        text:Uni.I18n.translate('generatereport.generateReportButton', 'MDC', 'Generate report')
                }]
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemId: 'searchItemsToolbarBottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('devices.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Devices per page'),
                deferLoading: true
            }
        ];

        this.callParent();
    }
})
;

