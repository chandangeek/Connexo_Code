/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                title: Uni.I18n.translate('general.details', 'EST', 'Details'),
                ui: 'large',
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
                ],
                tools: [{
                    xtype: 'uni-button-action',
                    itemId: 'action-button',
                    privileges: Est.privileges.EstimationConfiguration.administrate,
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




