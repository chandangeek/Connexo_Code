

 Ext.define('Cfg.properties.view.EditPropertiesPage', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-config-properties-page',
    cancelLink: null,
    action: null,
    requires:[
        'Cfg.properties.view.EditPropertiesForm'
    ],
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                itemId: 'cfg-config-properties-form',
                title: me.title,
                ui: 'large',
                returnLink: me.returnLink,
                action: me.action,
                cancelLink: me.cancelLink,
                defaults: {
                    labelWidth: 250,
                    labelAlign: 'right'
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'form-errors',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'cfg-properties-form',
                        itemId: 'cfg-properties-form'
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'form-buttons',
                        fieldLabel: '&nbsp;',
                        layout: 'hbox',
                        margin: '20 0 0 0',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'btn-edit-properties',
                                text: Uni.I18n.translate('general.save', 'CFG', 'Save'),
                                ui: 'action',
                                action: 'editConfigProperties'
                            },
                            {
                                xtype: 'button',
                                itemId: 'btn-cancel-proeprties',
                                text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                ui: 'link',
                                action: 'cancel',
                                href: me.cancelLink
                            }
                        ]
                    }
                ]
            }

        ]
        me.callParent(arguments);
    }
});