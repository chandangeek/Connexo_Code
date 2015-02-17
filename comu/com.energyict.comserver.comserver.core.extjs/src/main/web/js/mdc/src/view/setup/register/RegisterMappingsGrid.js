Ext.define('Mdc.view.setup.register.RegisterMappingsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerMappingsGrid',
    overflowY: 'auto',
    itemId: 'registermappinggrid',

    deviceTypeId: null,

    selModel: {
        mode: 'SINGLE'
    },

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterTypesOfDevicetype',
        'Mdc.view.setup.register.RegisterMappingActionMenu',
        'Uni.grid.column.Obis',
        'Uni.grid.column.ReadingType'
    ],

    store: 'RegisterTypesOfDevicetype',

    initComponent: function () {
        var me = this;
        this.columns = [
            {
                xtype: 'reading-type-column',
                dataIndex: 'readingType',
                flex: 1
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode'
            },

            {
                xtype: 'uni-actioncolumn',
                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
                items: 'Mdc.view.setup.register.RegisterMappingActionMenu'
            }
        ];

        this.dockedItems = [
            {

                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register types'),
                displayMoreMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register types'),
                emptyMsg: Uni.I18n.translate('registerMappings.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register types to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {

                        text: Uni.I18n.translate('registerMapping.addRegisterMapping', 'MDC', 'Add register types'),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
                        itemId: 'addRegisterMappingBtn',
                        xtype: 'button',
                        href: '#/administration/devicetypes/' + this.deviceTypeId + '/registertypes/add',
                        hrefTarget: '_self',
                        action: 'addRegisterMapping'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {deviceType: this.deviceTypeId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerTypes.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register types per page')
            }
        ];

        this.callParent();
    }
});
