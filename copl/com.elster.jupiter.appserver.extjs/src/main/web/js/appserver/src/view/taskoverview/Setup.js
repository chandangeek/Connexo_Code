/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.task-overview-setup',
    router: null,

    requires: [
        'Uni.util.FormEmptyMessage',
        'Apr.view.taskoverview.ActionMenu',
        'Uni.grid.commander.SortingPanel',
        'Uni.component.sort.model.Sort',
        'Apr.view.taskoverview.SortMenu',
        'Apr.store.Tasks'
    ],
    initComponent: function () {
        var me = this;

        me.content = [
        {
            ui: 'large',
            title: Uni.I18n.translate('general.taskOverview', 'APR', 'Task overview'),
            items: [
                {
                    xtype: 'taskFilter'
                },
                {
                    store: 'Apr.store.Tasks',
                    xtype: 'uni-grid-commander-sortingpanel',
                    itemId: 'taskoverview-sortingpanel',
                    menu: 'taskoverview-sort-menu',
                    items: [
                      {
                        property: 'nextRun',
                        direction: Uni.component.sort.model.Sort.ASC
                      },
                      {
                        property: 'queue',
                        direction: Uni.component.sort.model.Sort.ASC
                      },
                      {
                        property: 'priority',
                        direction: Uni.component.sort.model.Sort.DESC
                      }
                    ]
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'task-overview-grid',
                        itemId: 'task-overview-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        itemId: 'no-tasks-found',
                        text: Uni.I18n.translate('taskOverview.empty', 'APR', 'There are no tasks in the system')
                    },
                    previewComponent: {
                        xtype: 'task-preview',
                        itemId: 'task-preview'
                    }
                }
            ]
        }]
        me.callParent(arguments);
    }
});
