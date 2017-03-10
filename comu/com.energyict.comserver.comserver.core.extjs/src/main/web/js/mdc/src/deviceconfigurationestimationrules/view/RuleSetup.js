/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.deviceconfigurationestimationrules.view.RuleSetup', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.device-configuration-estimation-rules-setup',

    requires: [
        'Mdc.deviceconfigurationestimationrules.view.RulesGrid',
        'Mdc.deviceconfigurationestimationrules.view.Preview',
        'Uni.util.FormEmptyMessage'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: 'device-configuration-estimation-rules-grid',
            router: me.router,
            itemId: 'estimationRulesGrid'
        };

        me.emptyComponent = {
            xtype: 'uni-form-empty-message',
            text: Uni.I18n.translate('deviceconfiguration.estimation.rules.empty', 'MDC', 'No estimation rules have been created yet.')
        };

        me.previewComponent = {
            xtype: 'device-configuration-estimation-rules-preview',
            router: me.router
        };

        me.callParent(arguments);
    }

});

