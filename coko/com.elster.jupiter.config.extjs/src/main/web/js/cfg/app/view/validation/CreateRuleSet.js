Ext.define('Cfg.view.validation.CreateRuleSet', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.createRuleSet',
    itemId: 'createRuleSet',
    overflowY: true,

    requires: [

    ],

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'container',
                overflowY: true,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '<h1>' + Uni.I18n.translate('validation.createRuleSet', 'CFG', 'Create rule set') + '</h1>'
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'newRuleSetForm',
                                width: ' 100%',
                                layout: {
                                    type: 'vbox'
                                },
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
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                labelWidth: 250,
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                width: '100%',
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.create', 'CFG', 'Create'),
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
                        ]
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});

