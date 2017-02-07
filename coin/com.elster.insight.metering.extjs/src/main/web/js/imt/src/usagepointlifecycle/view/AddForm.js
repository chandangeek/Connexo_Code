Ext.define('Imt.usagepointlifecycle.view.AddForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagepoint-life-cycles-add-form',
    xtype: 'usagepoint-life-cycles-add-form',
    required: [
        'Uni.util.FormInfoMessage',
        'Uni.util.FormErrorMessage'
    ],
    router: null,
    infoText: null,
    btnAction: null,
    btnText: null,
    route: null,
    hideInfoMsg: false,
    ui: 'large',
    width: '100%',
    title: Uni.I18n.translate('general.addUsagePointLifeCycle', 'IMT', 'Add usage point life cycle'),
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
                margin: '0 0 20 0',
                hidden: me.hideInfoMsg,
                width: 800
            },
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                name: 'form-errors',
                margin: '0 0 20 0',
                hidden: true,
                width: 800
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'usagepoint-life-cycle-name',
                width: 500,
                required: true,
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                allowBlank: false,
                enforceMaxLength: true,
                maxLength: 80,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
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
                        text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                        ui: 'link',
                        href: me.router.getRoute(me.route).buildUrl()
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});