Ext.define('Est.estimationrulesets.view.RuleSetDetails', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rule-set-details',
    itemId: 'rule-set-details',
    router: null,
    record: null,
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Est.main.view.RuleSetSideMenu'
    ],

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('estimationrulesets.details.title', 'EST', 'Overview'),
                ui: 'large',
                itemId: 'rule-set-form',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'name',
                        fieldLabel: Uni.I18n.translate('estimationrulesets.details.name', 'EST', 'Name')
                    },
                    {
                        name: 'description',
                        fieldLabel: Uni.I18n.translate('estimationrulesets.details.description', 'EST', 'Description')
                    },
                    {
                        name: 'numberOfActiveRules',
                        fieldLabel: Uni.I18n.translate('estimationrulesets.details.activeRules', 'EST', 'Active rules')
                    },
                    {
                        name: 'numberOfInactiveRules',
                        fieldLabel: Uni.I18n.translate('estimationrulesets.details.inactiveRules', 'EST', 'Inactive rules')
                    }
                ],
                tools: [{
                    xtype: 'button',
                    iconCls: 'x-uni-action-iconD',
                    itemId: 'action-button',
                    text: Uni.I18n.translate('estimationrulesets.details.action', 'EST', 'Actions'),
                    menu: {
                        xtype: 'estimation-rule-sets-action-menu'
                    }
                }]
            }

        ];
        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: {
                xtype: 'estimation-rule-set-side-menu',
                router: me.router
            }
        };
        this.callParent(arguments);
    }
});




