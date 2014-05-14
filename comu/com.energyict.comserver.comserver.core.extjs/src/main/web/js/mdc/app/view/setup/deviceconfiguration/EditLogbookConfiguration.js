Ext.define('Mdc.view.setup.deviceconfiguration.EditLogbookConfiguration', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.edit-logbook-configuration',
    deviceConfigurationId: null,
    logbookConfigurationId: null,
    deviceTypeId: null,
    content: [
        {
            items: [
                {
                    xtype: 'panel',
                    ui: 'large',
                    itemId: 'editLogbookConfigurationTitle'
                },
                {
                    xtype: 'form',
                    width: '50%',
                    defaults: {
                        labelWidth: 160,
                        labelAlign: 'right',
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
                            xtype: 'displayfield',
                            allowBlank: false,
                            fieldLabel: 'Logbook OBIS code',
                            name: 'obisCode'
                        },
                        {
                            xtype: 'textfield',
                            allowBlank: false,
                            fieldLabel: 'Overruled OBIS code',
                            name: 'overruledObisCode',
                            maskRe: /[\d.]+/,
                            vtype: 'obisCode',
                            msgTarget: 'under'
                        }
                    ],
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'bottom',
                            border: false,
                            defaults: {
                                xtype: 'button'
                            },
                            items: [
                                '->',
                                {
                                    text: 'Save',
                                    action: 'save',
                                    ui: 'action'
                                },
                                {
                                    text: 'Cancel',
                                    action: 'cancel',
                                    ui: 'link',
                                    listeners: {
                                        click: {
                                            fn: function () {
                                                window.location.href = '#setup/devicetypes/' + this.up('edit-logbook-configuration').deviceTypeId + '/deviceconfigurations/' + this.up('edit-logbook-configuration').deviceConfigurationId + '/logbookconfigurations';
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        Ext.apply(Ext.form.VTypes, {
            obisCode: function (val, field) {
                var obis = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return obis.test(val);
            },
            obisCodeText: 'OBIS code is wrong'
        });
    }
});


