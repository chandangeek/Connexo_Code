Ext.define('Mdc.view.setup.devicecommand.widget.CommandForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-command-add-form',
    requires: [
        'Mdc.widget.DateTimeField'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        xtype: 'combobox',
        labelWidth: 250,
        maxWidth: 586,
        minWidth: 400,
        allowBlank: false,
        validateOnBlur: false,
        required: true
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('deviceCommand.add.commandCategorys', 'MDC', 'Command categories'),
            name: 'commandCategory',
            store: 'Mdc.store.DeviceMessageCategories',
            displayField: 'name',
            valueField: 'id',
            editable: false
        },
        {
            fieldLabel: Uni.I18n.translate('deviceCommand.add.command', 'MDC', 'Command'),
            name: 'command',
            displayField: 'name',
            valueField: 'id',
            editable: false,
            queryMode: 'local'
        },
        {
            xtype: 'dateTimeField',
            name: 'releaseDate',
            required: true,
            fieldLabel: Uni.I18n.translate('deviceCommand.add.releaseDate', 'MDC', 'Release date'),
            dateCfg: {
                width: 155
            },
            hourCfg: {
                width: 60
            },
            minuteCfg: {
                width: 60
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.down('dateTimeField[name=releaseDate]').setValue(new Date().getTime())
    }
});




