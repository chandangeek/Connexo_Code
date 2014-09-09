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
            xtype: 'panel',
            ui: 'large',
            title: 'Edit logbook configuration',
            items: [
                {
                    xtype: 'form',
                    width: '50%',
                    defaults: {
                        labelWidth: 160,
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            name: 'errors',
                            layout: 'hbox',
                            hidden: true,
                            defaults: {
                                xtype: 'container',
                                cls: 'isu-error-panel'
                            }
                        },
                        {
                            xtype: 'displayfield',
                            name: 'name',
                            fieldLabel: 'Name'
                        },
                        {
                            xtype: 'obis-displayfield',
                            allowBlank: false,
                            required: true,
                            fieldLabel: 'Logbook OBIS code',
                            name: 'obisCode'
                        },
                        {
                            xtype: 'obis-field',
                            allowBlank: false,
                            required: true,
                            fieldLabel: 'Overruled OBIS code',
                            name: 'overruledObisCode'
                        }
                    ],
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                            action: 'save',
                            ui: 'action',
                            margin: '0 0 0 10'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
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


