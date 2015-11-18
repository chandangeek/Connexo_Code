Ext.define('Dbp.processes.view.PrivilegesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.privileges-grid',
    overflowY: 'auto',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dbp.processes.view.PrivilegesActionMenu'
    ],
    store: 'ext-empty-store',


    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('editProcess.privilege', 'DBP', 'Privilege'),
                dataIndex: 'name',
                flex: 1
            },

            {
                header: Uni.I18n.translate('editProcess.userRoles', 'DBP', 'User roles'),
                dataIndex: 'userRoles',
                renderer: function (value) {
                    var resultArray = [];
                    Ext.Array.each(value, function (userRole) {
                        resultArray.push(Ext.String.htmlEncode(userRole.name));
                    });
                    return resultArray.join('<br>');
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                menu: {
                    xtype: 'dbp-privileges-action-menu',
                    itemId: 'mnu-privileges-action'
                }
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                usesExactCount: true,
                dock: 'top',
                displayMsg: Uni.I18n.translatePlural('privileges.pagingtoolbartop.displayMsg', 0, 'DBP', 'No privileges', '{0} privilege', '{0} privileges'),
                emptyMsg: Uni.I18n.translate('privileges.pagingtoolbartop.emptyMsg', 'DBP', 'There are no privileges'),
                items: [
                    {
                        text: Uni.I18n.translate('privileges.addPrivileges', 'DBP', 'Add privileges'),
                        itemId: 'addPrivileges',
                        privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                        xtype: 'button',
                        action: 'addPrivileges',
                        href: ''
                    }
                ]
            }
        ];

        this.callParent();
    }
})
;
