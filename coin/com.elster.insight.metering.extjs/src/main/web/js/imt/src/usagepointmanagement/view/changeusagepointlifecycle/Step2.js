/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.changeusagepointlifecycle.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.change-usage-point-life-cycle-step2',
    html: '',
    margin: '0 0 15 0',
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                htmlEncode: false,
                margin: 0,
                padding: 10,
                itemId: 'change-usage-point-life-cycle-failed',
                hidden: true,
                layout: {
                    type: 'hbox',
                    defaultMargins: '5 10 5 5'
                }
            }
        ];

        me.callParent(arguments);
    },
    setResultMessage: function (result, success) {
        var me = this;

        if (success) {
            me.update('<h3>' + Uni.I18n.translate('usagePointLifeCycle.change.successMsg', 'IMT', 'Successfully changed usage point life cycle') + '</h3>');
        } else {

            me.down('#change-usage-point-life-cycle-failed').setText('<h3>' + Uni.I18n.translate('usagePointLifeCycle.change.errorMsg1', 'IMT', 'Cannot change life cycle.') + '</h3>' + Uni.I18n.translate('usagePointLifeCycle.change.errorMsg2', 'IMT', 'The life cycle \'{0}\' doesn\'t contain the current state \'{1}\'.', [result.newUsagePointLifeCycle, result.usagePointState], false));
            me.down('#change-usage-point-life-cycle-failed').show();
        }
    }
});