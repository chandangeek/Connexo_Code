Ext.define('Bpm.view.process.ProcessPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.bpm-process-preview',
    requires: [
        'Bpm.view.process.ProcessPreviewForm',
        'Bpm.view.process.ProcessActionMenu'
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

