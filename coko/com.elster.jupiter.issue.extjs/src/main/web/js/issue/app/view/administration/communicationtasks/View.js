Ext.define('Isu.view.administration.communicationtasks.View', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.communicationtasks.SortToolbar',
        'Isu.view.administration.communicationtasks.List'
    ],
    alias: 'widget.communication-tasks-view',
    content: [
        {
            title: 'Communication tasks',
            items: [
                {
                    xtype: 'communication-tasks-sort-toolbar'
                },
                {
                    xtype: 'communication-tasks-list'
                },
                {
                    xtype: 'communication-tasks-item'
                }
            ]
        }
    ]
});