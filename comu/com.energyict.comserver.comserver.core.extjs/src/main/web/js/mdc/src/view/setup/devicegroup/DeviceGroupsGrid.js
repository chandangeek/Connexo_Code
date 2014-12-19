Ext.define('Mdc.view.setup.devicegroup.DeviceGroupsGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    xtype: 'deviceGroupsGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceGroups',
        'Mdc.view.setup.devicegroup.DeviceGroupActionMenu'
    ],
    selModel: {
        mode: 'SINGLE'
    },
    store: 'DeviceGroups',

    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('devicegroup.name', 'MDC', 'Name'),
                dataIndex: 'name',
                /*renderer: function (value, b, record) {
                    if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceGroup','privilege.view.deviceGroupDetail'])) {
                        return '<a href="#/devices/devicegroups/' + record.get('id') + '">' + value + '</a>';
                    } /*else if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceOfEnumeratedGroup'])) {
                        if (!record.get('dynamic')) {
                            return '<a href="#/devices/devicegroups/' + record.get('id') + '">' + value + '</a>';
                        }*/
                    /*else {
                        return value;
                    }
                },*/
                flex: 0.4
            },
            {
                header: Uni.I18n.translate('devicegroup.type', 'MDC', 'Type'),
                dataIndex: 'dynamic',
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('devicegroup.dynamic', 'MDC', 'Dynamic')
                    } else {
                        return Uni.I18n.translate('devicegroup.static', 'MDC', 'Static')
                    }
                },
                flex: 0.4
            },
            {
                xtype: 'uni-actioncolumn',
                hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceGroup'),
                menu: {
                    xtype: 'device-group-action-menu'
                }
            }
        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} device groups'),
                displayMoreMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} device groups'),
                emptyMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.emptyMsg', 'MDC', 'There are no device groups to display'),
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        text: Uni.I18n.translate('deviceGroup.createDeviceGroup', 'MDC', 'Add device group'),
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceGroup'),
                        itemId: 'createDeviceGroupButton',
                        xtype: 'button',
                        action: 'createDeviceGroupButton'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('deviceGroup.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Device groups per page')
            }
        ];

        this.callParent();
    }
});



