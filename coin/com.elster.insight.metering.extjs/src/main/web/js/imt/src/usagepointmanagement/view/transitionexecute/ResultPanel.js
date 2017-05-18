/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.transitionexecute.ResultPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.transition-result-panel',

    router: null,
    response: null,

    requires: [
        'Uni.util.FormInfoMessage'
    ],

    addErrorItems: function (failedItems) {
        var me = this,
            errorsPanel = me.down('#errors-panel');

        Ext.suspendLayouts();
        Ext.each(failedItems, function (failedItem) {
            errorsPanel.add({
                margin: '10 0 0 30',
                layout: 'hbox',
                items: [
                    {
                        html: '<span style="color:#eb5642;">' + '- ' + failedItem.id + '</span>'
                    },
                    {
                        xtype: 'button',
                        tooltip: failedItem.name,
                        text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#eb5642; font-size:16px;"></span>',
                        cls: 'uni-btn-transparent'
                    }
                ]
            });
        });
        Ext.resumeLayouts(true);
    },

    initComponent: function () {
        var me = this,
            failed = me.response.status.id === 'FAILED',
            scheduled = me.response.status.id === 'SCHEDULED',
            failedItems;

        if (!failed) {
            me.items = [
                {
                    title: scheduled
                        ? Uni.I18n.translate('usagepointtransitionexecute.wizard.step2success', 'IMT', "Usage point state will be changed at {0} to '{1}'",
                        [Uni.DateTime.formatDateTimeLong(new Date(me.response.transitionTime)), me.response.toStateName], false)
                        : Uni.I18n.translate('usagepointtransitionexecute.wizard.step2successNow', 'IMT', "Usage point state is changed to '{0}'", me.response.toStateName)
                }
            ];
        } else {
            me.items = [
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
                            text: Uni.I18n.translate('general.bulkActionError', 'IMT', 'An error was encountered when performing the bulk action.'),
                            itemId: 'imt-transition-result-panel-error-msg'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    html: '<span style="color:#eb5642;">' + (me.response.microChecks && me.response.microChecks.length
                            ? Uni.I18n.translate('devicetransitionexecute.wizard.step2.checksFail', 'IMT', "Unable to change usage point state to '{0}' due to failed pretransition checks" + ':', me.response.toStateName)
                            : Uni.I18n.translate('devicetransitionexecute.wizard.step2.fail', 'IMT', "Unable to change usage point state to '{0}' ({1})", [me.response.toStateName, me.response.message])
                    ) + '</span>'
                },
                {
                    xtype: 'container',
                    itemId: 'errors-panel'
                }
            ];
        }

        me.bbar = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.finish', 'IMT', 'Finish'),
                itemId: 'finishButton',
                ui: 'action',
                handler: function () {
                    me.router.getRoute('usagepoints/view').forward();
                }
            }
        ];

        me.callParent();
        if (failed) {
            failedItems = me.response.microActions.length ? me.response.microActions : me.response.microChecks;
            me.addErrorItems(failedItems);
        }
    }
});
