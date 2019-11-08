/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.changeusagepointlifecycle.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.change-usage-point-life-cycle-step2',
    html: '',
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                htmlEncode: false,
                margin: 0,
                width: 400,
                itemId: 'change-usage-point-life-cycle-failed',
                hidden: true,
            }
        ];

        me.callParent(arguments);
    },
    setResultMessage: function (result, success) {
        var me = this;

        if (success) {
            me.update('<h3>' + Uni.I18n.translate('usagePointLifeCycle.change.successMsg', 'IMT', 'Successfully changed usage point life cycle') + '</h3>');
        } else {
            me.down('#change-usage-point-life-cycle-failed').setText(Uni.I18n.translate('usagePointLifeCycle.change.errorMsg1', 'IMT', 'Cannot change life cycle.'));
            me.down('#change-usage-point-life-cycle-failed').show();
            me.add({
                 xtype: 'component',
                 padding: '15 0 0 0',
                 height: 55,
                 style: 'color: #eb5642',
                 html: Uni.I18n.translate('usagePointLifeCycle.change.errorMsg2', 'IMT', 'The life cycle \'{0}\' doesn\'t contain the current state \'{1}\'.', [result.newUsagePointLifeCycle, result.usagePointState], false) + '<br>' +
                       Uni.I18n.translate('general.errorCode', 'UNI', 'Error code') + ': ' + result.errorCode,
                 itemId: 'change-usage-point-life-cycle-failed-msg'
            })
        }
    }
});