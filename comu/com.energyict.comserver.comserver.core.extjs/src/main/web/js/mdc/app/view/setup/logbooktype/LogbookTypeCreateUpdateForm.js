Ext.define('Mdc.view.setup.logbooktype.LogbookTypeCreateUpdateForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.logbookTypeCreateUpdateForm',
    itemId: 'logbookTypeCreateUpdateForm',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.TextArea',
        'Ext.button.Button'
    ],
    content: [
        {
            xtype: 'form',
            itemId: 'createUpdateForm',
            ui: 'large',
            width: '50%',
            defaults: {
                validateOnChange: false,
                validateOnBlur: false
            },
            items: [
                {
                    itemId: 'errors',
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
                    itemId: 'logbookTypeName',
                    name: 'name',
                    required: true,
                    regex: /[a-zA-Z0-9]+/,
                    allowBlank: false,
                    fieldLabel: Uni.I18n.translate('logbooktype.name', 'MDC', 'Name'),
                    msgTarget: 'under'
                },
                {
                    xtype: 'textfield',
                    itemId: 'logbookTypeObis',
                    name: 'obis',
                    required: true,
                    disabled: true,
                    allowBlank: false,
                    fieldLabel: Uni.I18n.translate('logbooktype.obis', 'MDC', 'OBIS code'),
                    maskRe: /[\d.]+/,
                    vtype: 'obisCode',
                    msgTarget: 'under'
                }
            ],
            buttons: [
                {
                    itemId: 'submit',
                    margin: '0 0 0 10',
                    ui: 'action'
                },
                {
                    itemId: 'cancel',
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    ui: 'link',
                    hrefTarget: '',
                    href: '#/administration/logbooktypes'
                }
            ]
        }
    ],

    initComponent: function () {
        Ext.apply(Ext.form.VTypes, {
            obisCode: function (val, field) {
                var obis = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
                return obis.test(val);
            },
            obisCodeText: Uni.I18n.translate('logbooktype.obis.wrong', 'MDC', 'OBIS code is wrong')
        });
        this.callParent(this);
    }
});


