Ext.define('Mdc.view.setup.estimationdeviceconfigurations.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.estimation-deviceconfigurations-add',
    router: null,
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.view.grid.BulkSelection',
        'Uni.view.container.EmptyGridContainer'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('estimationDeviceConfigurations.addDeviceConfigurations', 'MDC', 'Add device configurations'),
                items: [
                    {
                        xtype: 'emptygridcontainer',
                        grid: {
                            xtype: 'bulk-selection-grid',
                            itemId: 'add-deviceconfigurations-grid',
                            store: 'Mdc.store.EstimationDeviceConfigurationsBuffered',

                            counterTextFn: function (count) {
                                return Uni.I18n.translatePlural('general.nrOfDeviceConfigurations.selected', count, 'MDC',
                                    'No device configurations selected', '{0} device configuration selected', '{0} device configurations selected'
                                );
                            },

                            allLabel: Uni.I18n.translate('estimationDeviceConfigurations.bulk.allLabel', 'MDC', 'All device configurations'),
                            allDescription: Uni.I18n.translate('estimationDeviceConfigurations.bulk.allDescription', 'MDC', 'Select all device configurations related to filters'),

                            selectedLabel: Uni.I18n.translate('estimationDeviceConfigurations.bulk.selectedLabel', 'MDC', 'Selected device configurations'),
                            selectedDescription: Uni.I18n.translate('estimationDeviceConfigurations.bulk.selectedDescription', 'MDC', 'Select device configurations in table'),

                            radioGroupName: 'selected-deviceconfigurations',
                            cancelHref: me.router.getRoute('administration/estimationrulesets/estimationruleset/deviceconfigurations').buildUrl(),

                            columns: [
                                {
                                    header: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                                    dataIndex: 'name',
                                    renderer: function (value, metaData, record) {
                                        var url = me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({deviceTypeId: record.get('deviceTypeId'), deviceConfigurationId: record.get('id')});
                                        return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                                    },
                                    flex: 1
                                },
                                {
                                    header: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                                    dataIndex: 'deviceTypeName',
                                    flex: 1
                                },
                                {
                                    header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                    dataIndex: 'config_active',
                                    flex: 1
                                }
                            ]
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-items-found-panel',
                            title: Uni.I18n.translate('estimationDeviceConfigurations.empty.title', 'MDC', 'No device configurations found'),
                            reasons: [
                                Uni.I18n.translate('estimationDeviceConfigurations.empty.list.item1', 'MDC', 'No device configurations have been defined yet'),
                                Uni.I18n.translate('estimationDeviceConfigurations.empty.list.item2', 'MDC', 'There are no device configurations that have reading types that match the rules in the estimation rule set'),
                                Uni.I18n.translate('estimationDeviceConfigurations.empty.list.item3', 'MDC', 'Matching device configurations exists, but you do not have permission to view them')
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
