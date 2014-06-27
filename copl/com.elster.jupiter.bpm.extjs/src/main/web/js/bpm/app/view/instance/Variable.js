Ext.define('Bpm.view.instance.Variable', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.variableList',
    itemId: 'variableList',

    requires: [
        'Bpm.store.Variables'
    ],

    store: undefined,
    title: Uni.I18n.translate('bpm.instance.variables', 'BPM', 'Variables'),
    height: 250,

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
                    header: Uni.I18n.translate('bpm.instance.variable.name', 'BPM', 'Name'),
                    dataIndex: 'variableInstanceId',
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('bpm.instance.variable.value', 'BPM', 'Value'),
                    dataIndex: 'value',
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('bpm.instance.variable.date', 'BPM', 'Last modification'),
                    dataIndex: 'date',
                    flex: 2
                }
            ]
        };

        this.callParent();
    }
});
