Ext.define('Imt.usagepointmanagement.view.bulk.UsagePointsSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'usagepoints-selection-grid',
    store: null,

    requires: [
        'Ext.grid.plugin.BufferedRenderer'
    ],

    plugins: {
        ptype: 'bufferedrenderer',
        synchronousRender: true
    },

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfUsagePoints.selected', count, 'IMT',
            'No usage points selected', '{0} usage point selected', '{0} usage points selected'
        );
    },

    allLabel: Uni.I18n.translate('usagePoints.bulk.allUsagePoints', 'IMT', 'All usage points'),
    allDescription: Uni.I18n.translate('usagePoints.bulk.selectMsg', 'IMT', 'Select all usage points (related to filters on previous screen)'),

    selectedLabel: Uni.I18n.translate('usagePoints.bulk.selectedDevices', 'IMT', 'Selected usage points'),
    selectedDescription: Uni.I18n.translate('usagePoints.bulk.selectedDevicesInTable', 'IMT', 'Select usage points in table'),

    cancelHref: '#/search',

    radioGroupName: 'usagepoints-selection-grid-step1',

    columns: [
        {
            itemId: 'name',
            header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
            dataIndex: 'name',
            flex: 1,
            renderer: function (value) {
                return '<a href="#usagepoints/' + value + '">' + Ext.String.htmlEncode(value) + '</a>';
            }
        },
        {
            itemId: 'serviceCategory',
            header: Uni.I18n.translate('usagepoints.bulk.serviceCategory', 'IMT', ' Service category'),
            dataIndex: 'displayServiceCategory',
            flex: 1
        },
        {
            itemId: 'metrologyConfiguration',
            header: Uni.I18n.translate('usagepoints.metrologyConfiguration', 'IMT', 'Metrology configuration'),
            dataIndex: 'displayMetrologyConfiguration',
            flex: 1
        },
        {
            itemId: 'type',
            header: Uni.I18n.translate('usagepoints.type', 'IMT', 'Type'),
            dataIndex: 'displayType',
            flex: 1
        },
        {
            itemId: 'connectionState',
            header: Uni.I18n.translate('usagepoints.connectionState', 'IMT', 'Connection state'),
            dataIndex: 'displayConnectionState',
            flex: 1
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.onChangeSelectionGroupType();
        this.getBottomToolbar().setVisible(false);
    }
});