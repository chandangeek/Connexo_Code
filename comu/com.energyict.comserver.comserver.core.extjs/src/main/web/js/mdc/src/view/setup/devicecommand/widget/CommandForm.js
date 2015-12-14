Ext.define('Mdc.view.setup.devicecommand.widget.CommandForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-command-add-form',
    requires: [
        'Mdc.widget.DateTimeField',
        'Uni.form.field.DateTime'
    ],
    layout: {
        type: 'vbox',
        align: 'stretchmax'
    },
    defaults: {
        xtype: 'combobox',
        labelWidth: 250,
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
            editable: false,
            blankText: Uni.I18n.translate('general.required.field', 'MDC', 'This field is required')
        },
        {
            fieldLabel: Uni.I18n.translate('deviceCommand.add.command', 'MDC', 'Command'),
            name: 'command',
            displayField: 'name',
            valueField: 'id',
            editable: false,
            queryMode: 'local',
            blankText: Uni.I18n.translate('general.required.field', 'MDC', 'This field is required')
        },
        {
            xtype: 'date-time',
            name: 'releaseDate',
            itemId: 'releaseDate',
            layout: 'hbox',
            required: true,
            fieldLabel: Uni.I18n.translate('deviceCommand.add.releaseDate', 'MDC', 'Release date'),
            dateConfig: {
                width: 155
            },
            hoursConfig: {
                width: 60
            },
            minutesConfig: {
                width: 60
            },
            dateTimeSeparatorConfig: {
                html: Uni.I18n.translate('deviceCommand.add.at', 'MDC', 'at'),
                margin: '0 10 0 10'
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.down('date-time[name=releaseDate]').setValue(new Date().getTime())
    }
});




