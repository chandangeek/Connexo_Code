Ext.define('Bpm.view.task.bulk.TaskGroupPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.bpm-task-group-preview',
    tools: [
        {
            xtype: 'button',

        }
    ],
    items: [
        {
            xtype: 'form',
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            itemId: 'task-execute-form',
            items: [
                {
                    xtype: 'property-form'
                }
            ]
        }    ]
});

