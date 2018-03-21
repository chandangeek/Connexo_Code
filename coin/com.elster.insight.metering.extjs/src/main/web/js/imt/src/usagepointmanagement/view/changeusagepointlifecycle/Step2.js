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
        var me = this,
            states = '';

        if (success) {
            me.update('<h3>' + Uni.I18n.translate('usagePointLifeCycle.change.successMsg', 'IMT', 'Successfully changed usage point life cycle') + '</h3>');
        } else {
            if (result.notMappableStates && result.notMappableStates.length) {
                states = '<ul>';
                Ext.Array.each(result.notMappableStates, function (state) {
                    states += '<li style="margin-left: 20px">' + state.name + '</li>';
                });
                states += '</ul>';
            }

            me.down('#change-usage-point-life-cycle-failed').setText(Uni.I18n.translate('usagePointLifeCycle.change.errorMsg', 'IMT', '{0} has states that cannot be mapped to states of {1}', ['<h3>' + result.errorMessage + '</h3><br><a href="#/administration/devicelifecycles/' + result.currentDeviceLifeCycle.id + '">' + result.currentDeviceLifeCycle.name + '</a>', '<a href="#/administration/devicelifecycles/' + result.targetDeviceLifeCycle.id + '">' + result.targetDeviceLifeCycle.name + '</a>', states], false));
            me.down('#change-usage-point-life-cycle-failed').show();
        }
    }
});