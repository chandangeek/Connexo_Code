Ext.define('Mdc.view.setup.deviceloadprofiles.EditWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.deviceloadprofile-edit-window',
    modal: true,
    title: Uni.I18n.translate('general.changeNextReadingBlockStart', 'MDC', 'Change next reading block start'),
    loadProfileRecord: null,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.form.field.DateTime'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'deviceloadprofile-edit-window-form',
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
                    required: true,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'date-time',
                            valueInMilliseconds: true,
                            itemId: 'mdc-deviceloadprofile-edit-window-date-picker',
                            layout: 'hbox',
                            //labelWidth: 30,
                            labelAlign: 'left',
                            value: me.loadProfileRecord.get('lastReading'),
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
                            itemId: 'mdc-deviceloadprofile-edit-window-save',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'mdc-deviceloadprofile-edit-window-cancel',
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