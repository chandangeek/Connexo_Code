Ext.define('Mdc.view.setup.logbooktype.LogForm', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.TextArea',
        'Ext.button.Button'
    ],
    alias: 'widget.form-logbook',

    content: [
        {
            itemId: 'form',
            xtype: 'form',
            ui: 'large',
            width: '50%',
            defaults: {
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'errors',
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
                    itemId: 'name',
                    xtype: 'textfield',
                    name: 'name',
                    required: true,
                    regex: /[a-zA-Z0-9]+/,
                    allowBlank: false,
                    fieldLabel: 'Name',
                    msgTarget: 'under'
                },
                {
                    itemId: 'obis',
                    xtype: 'textfield',
                    required: true,
                    allowBlank: false,
                    fieldLabel: 'OBIS code',
                    name: 'obis',
                    maskRe: /[\d.]+/,
                    vtype: 'obisCode',
                    msgTarget: 'under'
                }
            ],
            buttons: [
                {
                    itemId: 'logAction',
                    name: 'logAction',
                    ui: 'action',
                    margin: '0 0 0 10'
                },
                {
                    itemId: 'Cancel',
                    text: 'Cancel',
                    name: 'cancel',
                    hrefTarget: '',
                    href: '#/administration/logbooktypes',
                    ui: 'link'
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


