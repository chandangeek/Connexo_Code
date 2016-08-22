Ext.define('Tme.view.relativeperiod.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.relative-periods-preview',

    requires: [
        'Tme.view.relativeperiod.ActionMenu',
        'Tme.view.relativeperiod.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'relative-periods-action-menu'
            }
        }
    ],

    items: {
        xtype: 'relative-periods-preview-form'
    }
});
