Ext.define('Cfg.view.validation.RuleSetEdit', {
    extend: 'Ext.window.Window',
    alias: 'widget.validationrulesetEdit',
    title: 'New Rule Set',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    width: 400,
    height: 250,
    modal: true,
    constrain: true,
    autoShow: true,
    requires: [

    ],



    initComponent: function () {

        this.buttons = [
            {
                text: 'Save',
                action: 'saveRuleset'
            },
            {
                text: 'Cancel',
                scope: this,
                handler: this.close
            }
        ];

        this.items = [
            {
                xtype: 'form',
                border: false,
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    anchor: '100%',
                    margins: '0 0 5 0'
                },

                items: [
                    {
                        xtype: 'textfield',
                        name: 'name',
                        fieldLabel: 'Name'
                    },
                    {
                        xtype: 'textareafield',
                        grow: true,
                        name: 'description',
                        fieldLabel: 'Description',
                        margin: '0'
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});