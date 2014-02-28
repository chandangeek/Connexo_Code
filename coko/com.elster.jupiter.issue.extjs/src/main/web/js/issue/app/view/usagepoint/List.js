Ext.define('Mtr.view.usagepoint.List', {
    extend: 'Ext.container.Container',
    alias: 'widget.usagePointList',
    cls: 'content-wrapper',
    overflowY: 'auto',
    store: 'UsagePoints',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
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
                    html: '<h1>Usage points</h1>'
                },
                {
                    xtype: 'grid',
                    itemId: 'grid',
                    title: 'All usage points',
                    store: 'UsagePoints',
                    columns: {
                        defaults: {
                            flex: 1
                        },
                        items: [
                            { header: '', dataIndex: 'id', align: 'center', renderer: function (id) {
                                var href = Mtr.getApplication().getHistoryUsagePointController().tokenizeBrowse(id);
                                return '<a href="' + href + '">View dashboard</a>';
                            }},
                            { header: 'Id', dataIndex: 'id', hidden: true},
                            { header: 'MRID', dataIndex: 'mRID'},
                            { header: 'ServiceCategory', dataIndex: 'serviceCategory'},
                            { header: 'Check Billing', dataIndex: 'checkBilling', xtype: 'checkcolumn' },
                            { header: 'Phase Code', dataIndex: 'phaseCode', hidden: true},
                            { header: 'Number', dataIndex: 'number', hidden: true},
                            { header: 'Street', dataIndex: 'street', hidden: true},
                            { header: 'Town', dataIndex: 'town', hidden: true},
                            { header: 'State', dataIndex: 'state', hidden: true},
                            { header: 'Zip', dataIndex: 'zip', hidden: true},
                            { header: 'Country', dataIndex: 'country'},
                            { header: 'Create Time', dataIndex: 'createTime'},
                            { header: 'Modification Time', dataIndex: 'modTime'},
                            { header: 'Version', dataIndex: 'version', hidden: true}
                        ]
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        var grid = this.down('grid');

        grid.addDocked({
            xtype: 'pagingtoolbartop',
            store: grid.store,
            dock: 'top'
        });

        grid.addDocked({
            xtype: 'pagingtoolbarbottom',
            store: grid.store,
            dock: 'bottom'
        });
    }
});