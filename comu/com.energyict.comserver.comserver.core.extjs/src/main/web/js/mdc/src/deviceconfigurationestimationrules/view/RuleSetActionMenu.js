/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.deviceconfigurationestimationrules.view.RuleSetActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-configuration-estimation-rule-set-action-menu',
    items: [
        {
            text: Uni.I18n.translate('general.activate', 'MDC', 'Activate'),
            privileges: Mdc.privileges.DeviceConfigurationEstimations.administrate,
            itemId: 'changeRuleSetState',
            action: 'changeRuleSetState',
            changeRuleSetState: function(){
                return this.record.get('isEstimationRuleSetActive');
            },
            section: this.SECTION_ACTION
        },
        {
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            itemId: 'remove-action',
            action: 'remove'
        }
    ],

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