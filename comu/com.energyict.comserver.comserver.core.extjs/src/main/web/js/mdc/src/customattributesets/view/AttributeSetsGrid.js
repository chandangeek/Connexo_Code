Ext.define('Mdc.customattributesets.view.AttributeSetsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.custom-attribute-sets-grid',
    store: 'Mdc.customattributesets.store.CustomAttributeSets',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.customattributesets.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'domainName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.required', 'MDC', 'Required'),
                dataIndex: 'isRequired',
                flex: 1,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'MDC', 'Yes') :
                        Uni.I18n.translate('general.no', 'MDC', 'No');
                }
            },
            {
                header: Uni.I18n.translate('customattributesets.viewlevels', 'MDC', 'View levels'),
                dataIndex: 'viewPrivilegesString',
                flex: 2
            },
            {
                header: Uni.I18n.translate('customattributesets.editlevels', 'MDC', 'Edit levels'),
                dataIndex: 'editPrivilegesString',
                flex: 2
            },
            {
                header: Uni.I18n.translate('customattributesets.timesliced', 'MDC', 'Time-Sliced'),
                dataIndex: 'isVersioned',
                flex: 2,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.yes', 'MDC', 'Yes') :
                        Uni.I18n.translate('general.no', 'MDC', 'No');
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.active', 'MDC', 'Active') :
                        Uni.I18n.translate('general.noactive', 'MDC', 'Not active');
                }
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'custom-attribute-sets-action-menu',
                    itemId: 'custom-attribute-sets-action-menu-id'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} custom attribute sets'),
                displayMoreMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} custom attribute sets'),
                emptyMsg: Uni.I18n.translate('customattributesets.pagingtoolbartop.emptyMsg', 'MDC', 'There are no custom attribute sets to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('customattributesets.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Custom attribute sets per page')
            }
        ];

        me.callParent(arguments);
    }
});

