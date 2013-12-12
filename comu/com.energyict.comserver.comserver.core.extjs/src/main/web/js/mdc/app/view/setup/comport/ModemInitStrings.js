Ext.define('Mdc.view.setup.comport.ModemInitStrings', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.modemInitStrings',
    layout: 'auto',
    itemId: 'modeminitstringgrid',

    initComponent: function () {
        this.columns = [
            {
                header: 'Modem init strings',
                dataIndex: 'modemInitString',
                flex:1,
                editor: 'textfield'
            }
        ];

        this.selType = 'cellmodel';

        this.plugins = [
            Ext.create('Ext.grid.plugin.CellEditing', {
                clicksToEdit: 1
            })
        ]

        this.buttons = [
            {
                text: 'Add',
                action: 'add',
                style: {
                    background: '#404040 ',
                    borderColor: '#282828 '
                }
            },
            {
                text: 'Delete',
                action: 'delete',
                style: {
                    background: '#404040 ',
                    borderColor: '#282828 '
                }
            }
        ];

        this.callParent();
    }
});
