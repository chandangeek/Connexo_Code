Ext.define('Dbp.deviceprocesses.view.StatusProcessPreview', {
    extend: 'Ext.form.Panel',
    frame: true,
    alias: 'widget.dbp-status-process-preview',
    requires: [],

    items: {
        itemId: 'dbp-preview-task-nodes-form',
        xtype: 'form',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        defaults: {
            labelWidth: 25
        },
        items: [

        ]
    }
});

