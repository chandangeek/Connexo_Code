Ext.define('Dlc.devicelifecycles.view.AddForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.device-life-cycles-add-form',
    router: null,
    infoText: null,
    btnAction: null,
    btnText: null,
    itemId: 'device-life-cycles-add-form',
    ui: 'large',
    width: '100%',
    defaults: {
        labelWidth: 250
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-info-message',
                itemId: 'info-message',
                text: me.infoText,
                margin: '0 0 20 20',
                width: 800
            },
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                name: 'form-errors',
                margin: '0 0 20 20',
                hidden: true,
                width: 800
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'device-life-cycle-name',
                width: 500,
                required: true,
                fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                allowBlank: false,
                enforceMaxLength: true,
                maxLength: 80
            },
            {
                xtype: 'fieldcontainer',
                ui: 'actions',
                fieldLabel: '&nbsp',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'actionBtnContainer',
                        text: me.btnText,
                        ui: 'action',
                        action: me.btnAction
                    },
                    {
                        xtype: 'button',
                        itemId: 'cancel-link',
                        text: Uni.I18n.translate('general.cancel', 'DLC', 'Cancel'),
                        ui: 'link',
                        href: me.router.getRoute('administration/devicelifecycles').buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});