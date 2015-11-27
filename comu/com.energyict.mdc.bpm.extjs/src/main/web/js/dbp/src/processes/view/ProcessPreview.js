Ext.define('Dbp.processes.view.ProcessPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dbp-process-preview',
    requires: [
        'Dbp.processes.view.ProcessPreviewForm',
        'Dbp.processes.view.ProcessActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'DBP', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
            menu: {
                xtype: 'dbp-process-action-menu'
            }
        }
    ],

    items: {
        itemId: 'frm-preview-process',
        xtype: 'dbp-process-preview-form'
    }
});

