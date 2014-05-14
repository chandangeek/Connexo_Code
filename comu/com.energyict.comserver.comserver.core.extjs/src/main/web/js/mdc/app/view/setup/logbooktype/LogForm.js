Ext.define('Mdc.view.setup.logbooktype.LogForm', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.TextArea',
        'Ext.button.Button'
    ],
    alias: 'widget.form-logbook',

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'sideMenu'
        }
    ],

    content: [
        {
            cls: 'content-wrapper',
            items: [
                {
                    name: 'header',
                    margin: '0 0 10 0'
                },
                {
                    xtype: 'form',
                    width: '50%',
                    defaults: {
                        labelWidth: 150,
                        labelAlign: 'right',
                        margin: '0 0 20 0',
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            name: 'errors',
                            layout: 'hbox',
                            margin: '0 0 20 100',
                            hidden: true,
                            defaults: {
                                xtype: 'container',
                                cls: 'isu-error-panel'
                            }
                        },
                        {
                            xtype: 'textfield',
                            name: 'name',
                            labelSeparator: ' *',
                            regex: /[a-zA-Z0-9]+/,
                            allowBlank: false,
                            fieldLabel: 'Name',
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'textfield',
                            labelSeparator: ' *',
                            allowBlank: false,
                            fieldLabel: 'OBIS code',
                            name: 'obis',
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
                            margin: '0 0 0 100',
                            defaults: {
                                xtype: 'button'
                            },
                            items: [
                                {
                                    name: 'logAction',
                                    ui: 'action',
                                    margin: 10
                                },
                                {
                                    text: 'Cancel',
                                    name: 'cancel',
                                    margin: 10,
                                    hrefTarget: '',
                                    href: '#/setup/logbooktypes',
                                    cls: 'isu-btn-link'
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


