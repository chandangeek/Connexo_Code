Ext.define('Cfg.view.validation.CreateRuleSet', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.createRuleSet',
    itemId: 'createRuleSet',
    cls: 'content-container',
    overflowY: 'auto',
    requires: [
        'Uni.view.breadcrumb.Trail'
    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('validation.createRuleSet', 'CFG', 'Create rule set') + '</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'form',
                    itemId: 'newRuleSetForm',
                    padding: '10 10 0 10',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    width: 500,

                    items: [
                        {
                            xtype: 'textfield',
                            name: 'name',
                            msgTarget: 'under',
                            fieldLabel: Uni.I18n.translate('validation.name', 'CFG', 'Name'),
                            labelAlign: 'right',
                            labelWidth: 150,
                            maxLength: 80,
                            enforceMaxLength: true
                        },
                        {
                            xtype: 'textarea',
                            name: 'description',
                            fieldLabel: Uni.I18n.translate('validation.description', 'CFG', 'Description'),
                            labelWidth: 150,
                            maxLength: 256,
                            enforceMaxLength: true
                        },
                        {
                            xtype: 'fieldcontainer',
                            margin: '20 0 0 0',
                            fieldLabel: '&nbsp',
                            labelWidth: 150,
                            layout: 'hbox',
                            items: [
                                {
                                    text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                                    xtype: 'button',
                                    ui: 'action',
                                    action: 'createNewRuleSet',
                                    itemId: 'createNewRuleSet'
                                },
                                {
                                    xtype: 'button',
                                    ui: 'link',
                                    text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                    href: '#/administration/validation'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

