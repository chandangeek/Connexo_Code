Ext.define('Mdc.view.setup.comport.GlobalModemInitStrings', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.globalModemInitStrings',
    layout: 'auto',
    itemId: 'globalmodeminitstringgrid',

    initComponent: function () {
        this.columns = [
            {
                header: 'Modem init strings',
                dataIndex: 'globalModemInitString',
                flex:1,
                editor: 'textfield'
            }
        ];

        this.selType = 'cellmodel';

        this.plugins = [
            Ext.create('Ext.grid.plugin.CellEditing', {
                clicksToEdit: 1
            })
        ];

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
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
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
