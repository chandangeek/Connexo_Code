/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.ValidationTasksStatus', {
    extend: 'Ext.container.Container',
    alias: 'widget.output-validation-tasks-status',
    router: null,
    requires: [
        'Imt.purpose.store.ValidationTasks',
        'Cfg.model.ValidationTask'
    ],
    store: 'Imt.purpose.store.ValidationTasks',
    purpose: null,
    usagePoint: null,


    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.validationSchedule', 'IMT', 'Validation schedule'),
                itemId: 'output-validation-tasks-status-validation-schedule',
                labelAlign: 'top',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'output-validation-tasks-status-view-button',
                        text: Uni.I18n.translate('general.viewValidationSchedule', 'IMT', 'View validation schedule'),
                        listeners: {
                            click: me.viewValidationSchedule,
                            scope: me
                        }
                    },
                    {
                        xtype: 'progressbar',
                        itemId: 'output-validation-tasks-status-progressbar',
                        hidden: true,
                        width: '50%'
                    },
                    {
                        xtype: 'panel',
                        itemId: 'output-validation-tasks-status-field',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-info-message',
                        itemId: 'output-validation-tasks-status-empty-msg',
                        hidden: true
                    }
                ]
            }
        ];
        me.callParent();
    },


    viewValidationSchedule: function () {
        var me = this,
            viewBtn = me.down('#output-validation-tasks-status-view-button'),
            progressbar = me.down('#output-validation-tasks-status-progressbar'),
            store = Ext.getStore(me.store),
            isEmpty;

        Ext.suspendLayouts();
        viewBtn.hide();
        progressbar.show();
        progressbar.wait({
            interval: 50,
            increment: 20
        });
        store.getProxy().setUrl(me.usagePoint.get('name'));
        store.load(function (records, operation, success) {
            isEmpty = Ext.isDefined(success)
                ? !(success && store.getCount() && store.getTotalCount())
                : true;

            progressbar.hide();
            isEmpty ? me.setEmptyMsgAndShow() : me.setTasksAndShow(store);
        });

        Ext.resumeLayouts(true);
    },

    setEmptyMsgAndShow: function () {
        var me = this,
            emptyMsg = me.down('#output-validation-tasks-status-empty-msg');

        emptyMsg.setText(Uni.I18n.translate('usagepoint.purpose.validation.task.noTasks.on.usagePoint', 'IMT', 'No validation tasks have been configured for this usage point yet ({0}manage validation tasks{1})',
            [
                '<a href="'
                + me.router.getRoute('administration/validationtasks').buildUrl()
                + '">',
                '</a>'
            ],
            false));
        emptyMsg.show();
    },

    setTasksAndShow: function (store) {
        var me = this,
            tasks = me.down('#output-validation-tasks-status-field'),
            result = '<table>',
            url;

        store.each(function (task) {
            url = me.router.getRoute('administration/validationtasks/validationtask').buildUrl({
                taskId: task.get('id')
            });

            result += '<tr>';
            result += '<td>';
            result += '<a href="' + url + '">' + task.get('name') + '</a>';
            result += '</td>';
            result += '<td>';
            result += task.getTriggerText();
            result += '</td>';
            result += '</tr>';
        });
        result += '</table>';

        tasks.update(result);
        tasks.show();
    }

});