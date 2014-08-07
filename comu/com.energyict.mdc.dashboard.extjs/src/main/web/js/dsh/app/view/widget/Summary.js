Ext.define('Dsh.view.widget.Summary', {
    alias: 'widget.summary',
    extend: 'Ext.panel.Panel',
    title: 'Summary',
    layout: 'fit',
    ui: 'small',

    requires: [
        'Dsh.view.widget.summary.Dataview'
    ],

    initComponent: function () {
        this.items = {
            xtype: 'summary-dataview',
            cls: 'summary',
            store: Ext.create('Ext.data.Store', {
                fields: ['title', 'alias', 'count', 'child'],
                data: [
                    {
                        title: 'Success', alias: 'success', count: 245, child: [
                        {title: 'All task successful', alias: 'sdd', count: 83},
                        {title: 'At least one task failed', alias: 'success', count: 162}
                    ]
                    },
                    {title: 'Pending', alias: 'pending', count: 62},
                    {title: 'Failed', alias: 'failed', count: 42}
                ]
            }),
            total: 945
        };

        this.tbar = {
            xtype: 'text',
            html: '<h3>' + this.title + '</h3>'
        };

        this.callParent(arguments);
    }
});