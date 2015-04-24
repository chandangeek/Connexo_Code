Ext.define('Est.estimationrulesets.view.RuleSetEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rule-set-edit',
    itemId: 'rule-set-edit',
    record: null,
    initComponent: function () {
        var me = this,
            title;

        if (me.record) {
            title = Uni.I18n.translate('estimationrulesets.edit.title', 'EST', "Edit '{ruleSet}'").replace('{ruleSet}', me.record.get('name'))
        } else {
            title = Uni.I18n.translate('estimationrulesets.add.title', 'EST', 'Add estimation rule set')
        }

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: title,
                items: {
                    xtype: 'form',
                    itemId: 'rule-set-edit-form',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    defaults: {
                        labelWidth: 150,
                        maxWidth: 600,
                        allowBlank: false
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            hidden: true,
                            width: 600
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: Uni.I18n.translate('general.name', 'EST', 'Name'),
                            name: 'name',
                            itemId: 'name',
                            required: true
                        },
                        {
                            xtype: 'textareafield',
                            fieldLabel: Uni.I18n.translate('general.description', 'EST', 'Description'),
                            name: 'description',
                            itemId: 'description',
                            minHeight: 100
                        }
                    ],
                    buttons: [
                        {
                            text: me.record ?
                                Uni.I18n.translate('general.save', 'EST', 'Save') :
                                Uni.I18n.translate('general.add', 'EST', 'Add'),
                            ui: 'action',
                            itemId: 'save-button'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'EST', 'Cancel'),
                            ui: 'link',
                            itemId: 'cancel-button'
                        }
                    ]
                }
            }
        ];
        this.callParent(arguments);
    }
});




