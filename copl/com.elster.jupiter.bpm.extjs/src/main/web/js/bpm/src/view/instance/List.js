Ext.define('Bpm.view.instance.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.instanceList',
    itemId: 'instanceList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Bpm.store.ProcessInstances'
    ],

    store: 'Bpm.store.ProcessInstances',

    initComponent: function () {
        this.columns = {
            defaults: {
                flex: 1,
                sortable: false,
                hideable: false,
                fixed: true
            },
            items: [
                {
                    header: Uni.I18n.translate('bpm.instance.id', 'BPM', 'Process id'),
                    dataIndex: 'id',
                    renderer: function (value, b, record) {
                        return '<a href="#workspace/processes/' +record.get('deploymentId')+ "/" + record.get('id') + '">' + Ext.String.htmlEncode(value) + '</a>';
                    }
                },
                {
                    header: Uni.I18n.translate('bpm.instance.name', 'BPM', 'Definition name'),
                    dataIndex: 'name',
                    flex: 4
                },
                {
                    header: Uni.I18n.translate('bpm.instance.initiator', 'BPM', 'Initiator'),
                    dataIndex: 'initiator',
                    flex: 3
                },
                {
                    header: Uni.I18n.translate('bpm.instance.state', 'BPM', 'State'),
                    dataIndex: 'state',
                    renderer: function (value) {
                        switch(value) {
                            case 1 : return Uni.I18n.translate('bpm.instance.state.active', 'BPM', 'Active');
                            case 2 : return Uni.I18n.translate('bpm.instance.state.completed', 'BPM', 'Completed');
                            case 3 : return Uni.I18n.translate('bpm.instance.state.aborted', 'BPM', 'Aborted');
                        }
                    },
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('bpm.instance.startDate', 'BPM', 'Start date'),
                    dataIndex: 'startDate',
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('bpm.instance.endDate', 'BPM', 'End date'),
                    dataIndex: 'endDate',
                    flex: 2
                }
            ]
        };

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('bpm.instance.list.top', 'BPM', '{0} - {1} of {2} processes')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                limit: 10,
                itemsPerPageMsg: Uni.I18n.translate('bpm.instance.list.bottom', 'BPM', 'Processes per page')
            }
        ];

        this.callParent();
    }
});