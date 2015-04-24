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
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'panel',
                        ui: 'large',
                        flex: 1,
                        title: Uni.I18n.translate('estimation.ruleSetDetail.title', 'EST', 'Overvirew'),
                        items: [
                            {
                                xtype: 'form',
                                itemId: 'rule-set-form',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 150
                                },
                                items: [
                                    {
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('general.name', 'EST', 'Name')
                                    },
                                    {
                                        name: 'description',
                                        fieldLabel: Uni.I18n.translate('general.description', 'EST', 'Description')
                                    },
                                    {
                                        name: 'numberOfActiveRules',
                                        fieldLabel: Uni.I18n.translate('estimationrulesets.activeRules', 'EST', 'Active rules')
                                    },
                                    {
                                        name: 'numberOfInactiveRules',
                                        fieldLabel: Uni.I18n.translate('estimationrulesets.inactiveRules', 'EST', 'Inactive rules')
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        width: 100,
                        items: Uni.Auth.hasAnyPrivilege(['privilege.administrate.EstimationConfiguration']) && {
                            xtype: 'button',
                            iconCls: 'x-uni-action-iconD',
                            itemId: 'action-button',
                            text: Uni.I18n.translate('general.actions', 'EST', 'Actions'),
                            menu: {
                                xtype: 'estimation-rule-sets-action-menu',
                                record: me.record
                            }
                        }
                    }
                ]
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




