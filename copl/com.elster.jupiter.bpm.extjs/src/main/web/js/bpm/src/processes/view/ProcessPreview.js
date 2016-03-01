Ext.define('Bpm.processes.view.ProcessPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.bpm-process-preview',
    requires: [
        'Bpm.processes.view.ProcessPreviewForm',
        'Bpm.processes.view.ProcessActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'BPM', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            privileges: Bpm.privileges.BpmManagement.administrateProcesses,
            menu: {
                xtype: 'bpm-process-action-menu'
            }
        }
    ],

    items: {
        itemId: 'frm-preview-process',
        xtype: 'bpm-process-preview-form'
    }
});

