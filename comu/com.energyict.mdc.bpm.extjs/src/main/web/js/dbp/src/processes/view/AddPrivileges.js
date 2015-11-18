Ext.define('Dbp.processes.view.AddPrivileges', {
    extend: 'Uni.view.grid.SelectionGrid',
    xtype: 'dbp-add-privileges-grid',
    store: 'Dbp.processes.store.Privileges',
    height: 310,
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'editProcess.nrOfDeviceStates.selected', count, 'DBP',
            'No device state selected', '{0} device state selected', '{0} device states selected'
        );
    },

    columns: {
        items: [
            {
                header: Uni.I18n.translate('editProcess.privilege', 'DBP', 'Privilege'),
                dataIndex: 'name',
                flex: 2
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
            }
        ]
    },
    buttonAlign: 'left',
    buttons: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('editProcess.add', 'DBP', 'Add'),
            itemId: 'btn-add-privileges',
            action: 'addSelectedPrivileges',
            disabled: true,
            ui: 'action'
        },
        {
            xtype: 'button',
            itemId: 'btn-cancel-add-privileges',
            text: Uni.I18n.translate('editProcess.cancel', 'DBP', 'Cancel'),
            href: '#',
            ui: 'link'
        }
    ]

});

