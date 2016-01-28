Ext.define('Dbp.deviceprocesses.view.VariablesPreview', {
    extend: 'Ext.form.Panel',
    frame: true,
    alias: 'widget.dbp-node-variables-preview',

    items: [
        {
            itemId: 'frm-node-variable-preview',
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
    ]
});
