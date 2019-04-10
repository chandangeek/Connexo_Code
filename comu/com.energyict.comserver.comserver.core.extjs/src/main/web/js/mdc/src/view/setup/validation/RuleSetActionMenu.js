/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.validation.RuleSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.validation-ruleset-actionmenu',

    initComponent: function () {
        this.items = [

             {
                text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
                privileges: Cfg.privileges.Validation.deviceConfiguration,
                itemId: 'changeRuleSetState',
                action: 'changeRuleSetState',
                 changeRuleSetState: function(){
                    return this.record.get('isValidationRuleSetActive');
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.view', 'MDC', 'View'),
                itemId: 'viewRuleSet',
                action: 'viewRuleSet',
                section: this.SECTION_VIEW
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Cfg.privileges.Validation.deviceConfiguration,
                itemId: 'removeRuleSet',
                action: 'removeRuleSet',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                if(item.changeRuleSetState !== undefined){
                    item.setText(item.changeRuleSetState.call(me) ?
                        Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                        Uni.I18n.translate('general.activate', 'MDC', 'Activate'));
                }
            })
        }
    }
});
