Ext.define('Imt.registerdata.view.RegisterDataMainGrid', {
    extend: 'Ext.grid.Panel',

    requires: [
        'Uni.grid.column.Edited',
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.registerdata.view.RegisterDataActionMenu'
    ],

    mRID: null,
    registerId: null,

    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} readings'),
                displayMoreMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} readings'),
                emptyMsg: Uni.I18n.translate('registerdata.pagingtoolbartop.emptyMsg', 'IMT', 'There are no readings to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addReading', 'IMT', 'Add reading'),
             //           privileges: Imt.privileges.Device.administrateDeviceData,
                        href: '#/usagepoints/' + encodeURIComponent(me.mRID) + '/registers/' + me.registerId + '/add',
                  //      dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                deferLoading: true,
                params: [
                    {mRID: me.mRID},
                    {registerId: me.registerId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('registerdata.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Readings per page')
            }
        ];

        me.callParent(arguments);
    }
});