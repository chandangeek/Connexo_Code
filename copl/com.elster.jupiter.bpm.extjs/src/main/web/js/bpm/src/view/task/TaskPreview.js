Ext.define('Bpm.view.task.TaskPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.bpm-task-preview',
    requires: [
        'Bpm.view.task.TaskPreviewForm',
        'Bpm.view.task.TaskActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'BPM', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'bpm-task-action-menu'
            }
        }
    ],

    items: {
        itemId: 'frm-preview-task',
        xtype: 'bpm-task-preview-form'
    }
});

