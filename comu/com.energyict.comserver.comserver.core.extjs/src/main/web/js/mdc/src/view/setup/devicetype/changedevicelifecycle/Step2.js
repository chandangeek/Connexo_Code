/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.changedevicelifecycle.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.change-device-life-cycle-step2',
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
                itemId: 'change-device-life-cycle-failed',
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
            states = '',
            errorMessage = '',
            message = '',
            additionalInfo = '';

        if (success) {
            if (result.message){
                message = result.message;
                if (result.affectedRules && result.affectedRules.length){
                    additionalInfo = '<ul>';
                    Ext.Array.each(result.affectedRules, function (rule) {
                        if (rule){
                            additionalInfo += '<li style="margin-left: 20px">' + rule + '</li>';
                        }
                    });
                    additionalInfo += '</ul>';
                }
            }else{
                message = Uni.I18n.translate('deviceLifeCycle.change.successMsg', 'MDC', 'Successfully changed device life cycle');
            }
            me.update('<h3>' + message + '</h3>' + additionalInfo);
        } else {
            if (result.notMappableStates && result.notMappableStates.length) {
                states = '<ul>';
                Ext.Array.each(result.notMappableStates, function (state) {
                    states += '<li style="margin-left: 20px">' + state.name + '</li>';
                });
                states += '</ul>';

                me.down('#change-device-life-cycle-failed').setText(Uni.I18n.translate('deviceLifeCycle.change.errorMsg', 'MDC', '{0} has states that cannot be mapped to states of {1} and there are devices in that states: {2}', ['<h3>' + result.errorMessage + '</h3><br><a href="#/administration/devicelifecycles/' + result.currentDeviceLifeCycle.id + '">' + result.currentDeviceLifeCycle.name + '</a>', '<a href="#/administration/devicelifecycles/' + result.targetDeviceLifeCycle.id + '">' + result.targetDeviceLifeCycle.name + '</a>', states], false));
                me.down('#change-device-life-cycle-failed').show();
            } else {
                errorMessage = result.errorMessage;
                if (!errorMessage && result.errors && result.errors.length){
                    errorMessage = '<ul>';
                    Ext.Array.each(result.errors, function(error){
                       if (error.id){
                            errorMessage += '<li style="margin-left: 20px">' + Uni.I18n.translate('general.errorOnItem', 'MDC', 'Error on item {0}: {1}', [error.msg, error.id]) + '</li>';
                       }else{
                            errorMessage += '<li style="margin-left: 20px">' + Uni.I18n.translate('general.error', 'MDC', 'Error:') + ': ' + error.msg + '</li>';
                       }
                    })
                    errorMessage += '</ul>';
                }
                me.down('#change-device-life-cycle-failed').setText('<h3>' + errorMessage + '</h3>');
                me.down('#change-device-life-cycle-failed').show();
            }
        }
    }
});