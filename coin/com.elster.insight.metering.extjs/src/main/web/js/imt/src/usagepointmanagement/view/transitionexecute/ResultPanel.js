/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.transitionexecute.ResultPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.transition-result-panel',

    router: null,
    response: null,
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
                        html: '- ' + failedItem.id
                    },
                    {
                        xtype: 'button',
                        tooltip: failedItem.name,
                        iconCls: 'uni-icon-info-small',
                        cls: 'uni-btn-transparent'
                    }
                ]
            });
        });
        Ext.resumeLayouts(true);
    },

    initComponent: function () {
        var me = this,
            failed = me.response.status.id == 'FAILED',
            scheduled = me.response.status.id == 'SCHEDULED',
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
                    title: Uni.I18n.translate('usagepointtransitionexecute.wizard.step2fail', 'IMT', "Unable to change usage point state to '{0}'", me.response.toStateName),
                    items: [
                        {
                            html: me.response.message
                        },
                        {
                            margin: '0 0 10 0',
                            itemId: 'errors-panel'
                        }
                    ]
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
