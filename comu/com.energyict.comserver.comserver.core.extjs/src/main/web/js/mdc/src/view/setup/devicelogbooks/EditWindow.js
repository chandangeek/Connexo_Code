Ext.define('Mdc.view.setup.devicelogbooks.EditWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.devicelogbook-edit-window',
    modal: true,
    closable: false,
    title: Uni.I18n.translate('general.changeNextReadingBlockStart', 'MDC', 'Change next reading block start'),
    logbookRecord: null,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.form.field.DateTime'
    ],

    initComponent: function () {
        var me = this,
            currentNextReadingBlockStart = me.logbookRecord && me.logbookRecord.get('lastReading')
                ? me.logbookRecord.get('lastReading') : new Date();

        me.items = {
            xtype: 'form',
            itemId: 'devicelogbook-edit-window-form',
            padding: 0,
            defaults: {
                width: 500,
                labelWidth: 175
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'label',
                    itemId: 'error-label',
                    hidden: true,
                    margin: '10 0 10 20'
                },
                {
                    xtype: 'fieldcontainer',
                    margin: '10 0 10 0',
                    fieldLabel: Uni.I18n.translate('general.nextReadingBlockStart', 'MDC', 'Next reading block start'),
                    itemId: 'lbl-logbook-next-reading',
                    required: true,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'date-time',
                            valueInMilliseconds: true,
                            itemId: 'mdc-devicelogbook-edit-window-date-picker',
                            layout: 'hbox',
                            labelAlign: 'left',
                            value: currentNextReadingBlockStart,
                            style: {
                                border: 'none',
                                padding: 0,
                                marginBottom: '10px'
                            },
                            dateConfig: {
                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault),
                                width: 155
                            },
                            hoursConfig: {
                                width: 60
                            },
                            minutesConfig: {
                                width: 60
                            }
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'mdc-devicelogbook-edit-window-save',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'mdc-devicelogbook-edit-window-cancel',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});