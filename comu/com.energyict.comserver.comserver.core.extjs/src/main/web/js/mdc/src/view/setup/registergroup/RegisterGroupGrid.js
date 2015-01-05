Ext.define('Mdc.view.setup.registergroup.RegisterGroupGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.registerGroupGrid',
    overflowY: 'auto',
    itemId: 'registerGroupGrid',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Mdc.store.RegisterGroups',
        'Mdc.view.setup.registergroup.RegisterGroupActionMenu'
    ],
    store: 'RegisterGroups',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('registerGroup.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },


            {
                xtype: 'uni-actioncolumn',
                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.masterData'),
                items: 'Mdc.view.setup.registergroup.RegisterGroupActionMenu'
            }
        ];

        this.dockedItems = [
            {

                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerGroups.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} register groups'),
                displayMoreMsg: Uni.I18n.translate('registerGroups.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} register groups'),
                emptyMsg: Uni.I18n.translate('registerGroups.pagingtoolbartop.emptyMsg', 'MDC', 'There are no register groups to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: Uni.I18n.translate('registerGroup.add', 'MDC', 'Add register group'),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.masterData'),
                        itemId: 'createRegisterGroup',
                        xtype: 'button',
                        action: 'createRegisterGroup'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerGroups.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Register groups per page')
            }
        ];

        this.callParent();
    }
})
;
