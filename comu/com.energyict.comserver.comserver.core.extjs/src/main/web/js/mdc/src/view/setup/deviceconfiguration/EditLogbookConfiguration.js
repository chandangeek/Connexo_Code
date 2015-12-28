Ext.define('Mdc.view.setup.deviceconfiguration.EditLogbookConfiguration', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.edit-logbook-configuration',
    deviceConfigurationId: null,
    logbookConfigurationId: null,
    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ObisDisplay'
    ],
    deviceTypeId: null,
    content: [
        {
            xtype: 'form',
            itemId: 'editLogbookPanel',
            ui: 'large',
            width: '100%',
            defaults: {
                labelWidth: 250,
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    margin: '0 0 10 0',
                    hidden: true,
                    width: 500
                },
                {
                    xtype: 'displayfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('general.logbookType', 'MDC', 'Logbook type'),
                    width: 500,
                    listeners: {
                        afterrender: function (field) {
                            field.focus(false, 500);
                        }
                    }
                },
                {
                    xtype: 'obis-displayfield',
                    allowBlank: false,
                    required: true,
                    fieldLabel: Uni.I18n.translate('logbooktype.obis', 'MDC', 'OBIS code'),
                    name: 'obisCode',
                    width: 500
                },
                {
                    xtype: 'obis-field',
                    itemId: 'overruled-obis-code-field',
                    fieldLabel: 'Overruled OBIS code',
                    required: false,
                    name: 'overruledObisCode',
                    width: 700
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'save-logbook-type-button',
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                            action: 'save',
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            itemId: 'cancel-logbook-type-button',
                            action: 'cancel',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/administration/devicetypes/' + this.up('edit-logbook-configuration').deviceTypeId + '/deviceconfigurations/' + this.up('edit-logbook-configuration').deviceConfigurationId + '/logbookconfigurations';
                                    }
                                }
                            }
                        }
                    ]
                }
            ]
        }
    ]
});


