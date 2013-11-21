Ext.define('Cfg.view.validation.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.validationrulesetBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Cfg.view.validation.List'
    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Rule Sets</h1>'
                },
                {
                    xtype: 'validationrulesetList'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
