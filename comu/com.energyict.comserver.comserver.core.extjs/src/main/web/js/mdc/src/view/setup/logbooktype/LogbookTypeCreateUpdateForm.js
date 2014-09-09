Ext.define('Mdc.view.setup.logbooktype.LogbookTypeCreateUpdateForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.logbookTypeCreateUpdateForm',
    itemId: 'logbookTypeCreateUpdateForm',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.TextArea',
        'Ext.button.Button',
        'Uni.form.field.Obis'
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
                    xtype: 'obis-field',
                    itemId: 'logbookTypeObis',
                    name: 'obis',
                    disabled: true
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
    ]
});


