Ext.define('Cfg.view.validation.CreateRuleSet', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.createRuleSet',
    itemId: 'createRuleSet',

    requires: [

    ],

    content: [
        {
            xtype: 'form',
            title: Uni.I18n.translate('validation.createRuleSet', 'CFG', 'Create rule set'),
            itemId: 'newRuleSetForm',
            ui: 'large',
            width: '100%',
            defaults: {
                labelWidth: 250
            },
            items: [
                {
                    xtype: 'textfield',
                    name: 'name',
                    required: true,
                    width: 600,
                    msgTarget: 'under',
                    fieldLabel: Uni.I18n.translate('validation.name', 'CFG', 'Name'),
                    enforceMaxLength: true
                },
                {
                    xtype: 'textarea',
                    name: 'description',
                    width: 600,
                    fieldLabel: Uni.I18n.translate('validation.description', 'CFG', 'Description'),
                    enforceMaxLength: true
                }
            ],
            buttons: [
                {
                    text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                    xtype: 'button',
                    ui: 'action',
                    action: 'createNewRuleSet',
                    itemId: 'createNewRuleSet'
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    xtype: 'button',
                    ui: 'link',
                    href: '#/administration/validation'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

