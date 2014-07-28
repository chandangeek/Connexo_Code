Ext.define('Mdc.view.setup.registerconfig.RegisterConfigGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerConfigGrid',
    overflowY: 'auto',
    itemId: 'registerconfiggrid',

    deviceTypeId: null,
    deviceConfigId: null,

    selModel: {
        mode: 'SINGLE'
    },

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterConfigsOfDeviceConfig',
        'Mdc.view.setup.registerconfig.RegisterConfigActionMenu',
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],

    store: 'RegisterConfigsOfDeviceConfig',

    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('registerConfigs.name', 'MDC', 'Register type'),
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType'
            },
            {
                xtype: 'obis-column',
                dataIndex: 'overruledObisCode'
            },

            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.registerconfig.RegisterConfigActionMenu'
            }
        ];

        this.dockedItems = [
            {

                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register configurations'),
                displayMoreMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register configurations'),
                emptyMsg: Uni.I18n.translate('registerConfigs.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register configurations to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {

                        text: Uni.I18n.translate('registerConfigs.createRegisterConfig', 'MDC', 'Add register configuration'),
                        itemId: 'createRegisterConfigBtn',
                        xtype: 'button',
                        action: 'createRegisterConfig'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {deviceType: this.deviceTypeId},
                    {deviceConfig: this.deviceConfigId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerConfigs.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register configurations per page')
            }
        ];

        this.callParent();
    }
});
