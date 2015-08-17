Ext.define('Sam.view.licensing.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop'
    ],
    alias: 'widget.licensing-list',
    store: 'Sam.store.Licensing',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'License',
                header: Uni.I18n.translate('licensing.license', 'SAM', 'License'),
                dataIndex: 'applicationname',
                flex: 1
            },
            {
                itemId: 'Status',
                header: Uni.I18n.translate('general.status', 'SAM', 'Status'),
                dataIndex: 'status',
                width: 150
            },
            {
                itemId: 'expirationDate',
                header: Uni.I18n.translate('licensing.expirationDate', 'SAM', 'Expiration date'),
                dataIndex: 'expires',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 200
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('licensing.grid.displayMsg', 'SAM', '{0} - {1} of {2} licenses'),
                displayMoreMsg: Uni.I18n.translate('licensing.grid.displayMoreMsg', 'SAM', '{0} - {1} of more than {2} licenses'),
                emptyMsg: '',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'uploadButton',
                        text: Uni.I18n.translate('licensing.uploadLicenses', 'SAM', 'Upload licenses'),
                        action: 'uploadlicenses',
                        href: me.router.getRoute('administration/licenses/upload').buildUrl(),
                        privileges: Sam.privileges.License.upload
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


