/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetransitionexecute.ResultPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.transition-result-panel',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    router: null,
    response: null,

    requires: [
        'Uni.util.FormInfoMessage'
    ],

    addErrorItems: function (checks) {
        var me = this,
            errorsContainer = me.down('#errors-container');

        Ext.each(checks, function (check) {
            errorsContainer.add({
                xtype: 'container',
                margin: '10 0 0 30',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'container',
                        html: '<span style="color:#eb5642;">' + '- ' + check.id + '</span>'
                    },
                    {
                        xtype: 'button',
                        tooltip: check.name,
                        text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#eb5642; font-size:16px;"></span>',
                        cls: 'uni-btn-transparent',
                        style: {
                            display: 'inline-block',
                            "text-decoration": 'none !important'
                        }
                    }
                ]
            })
        })


    },

    initComponent: function () {
        var me = this;

        if (me.response.result) {
            me.items = [
                {
                    xtype: 'panel',
                    title: new Date(me.response.effectiveTimestamp)<new Date() ?
                        Uni.I18n.translate('devicetransitionexecute.wizard.step2successNow', 'MDC', "Successfully changed device state to '{0}'", me.response.targetState) :
                        Uni.I18n.translate('devicetransitionexecute.wizard.step2success', 'MDC', "Device state will change at {0}", Uni.DateTime.formatDateTimeLong(new Date(me.response.effectiveTimestamp)), false)
                }
            ];
        } else {
            me.items = [
                {
                    xtype: 'panel',
                    style: {
                        iconCls: 'isu-icon-ok isu-msg-success-icon',
                        colorCls: 'isu-msg-success'
                    }
                },
                {
                    xtype: 'panel',
                    items: [
                        {
                            xtype: 'uni-form-info-message',
                            margin: '7 0 17 0',
                            iconCmp: {
                                xtype: 'component',
                                style: 'font-size: 22px; color: #eb5642; margin: 0px -22px 0px 0px;',
                                cls: 'icon-warning'
                            },
                            style: 'border: 1px solid #eb5642; border-radius: 10px;',
                            bodyStyle: 'color: #eb5642; padding: 5px 0 5px 32px',
                            text: Uni.I18n.translate('general.bulkActionError', 'MDC', 'An error was encountered when performing the bulk action.'),
                            itemId: 'mdc-transition-result-panel-error-msg'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    html: '<span style="color:#eb5642;">' + (me.response.microChecks && me.response.microChecks.length
                        ? Uni.I18n.translate('devicetransitionexecute.wizard.step2.checksFail', 'MDC', "Unable to change device state to '{0}' due to failed pretransition checks" + ':', me.response.targetState)
                        : Uni.I18n.translate('devicetransitionexecute.wizard.step2.fail', 'MDC', "Unable to change device state to '{0}' ({1})", [me.response.targetState, me.response.message])
                    ) + '</span>'
                },
                {
                    xtype: 'container',
                    itemId: 'errors-container'
                }
            ];
        }

        me.bbar = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                itemId: 'finishButton',
                style: {
                    'background-color': '#71adc7'
                },
                handler: function () {
                    if(me.deviceRemoved === true){
                        me.router.getRoute('devices').forward();
                    } else {
                        me.router.getRoute('devices/device').forward();
                    }
                }
            }
        ];

        this.callParent(arguments);

        Ext.suspendLayouts();
        if (!me.response.result) {
            me.addErrorItems(me.response.microChecks);
        }
        Ext.resumeLayouts(true);
    }

});
