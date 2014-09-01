Ext.define('Dsh.view.widget.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dsh-side-filter',
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    ui: 'medium',
    requires: [
        'Uni.component.filter.view.Filter',
        'Dsh.store.ConnectionCurrentStates'
    ],
    items: [
        {
            xtype: 'filter-form',
            ui: 'filter',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                xtype: 'combobox',
                labelAlign: 'top'
            },
            items: [
                {
                    name: 'deviceGroup',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceGroup', 'DSH', 'Device group')
                },
                {
                    name: 'currentState',
                    store: 'Dsh.store.ConnectionCurrentStates',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.currentState', 'DSH', 'Current state'),
                    displayField: 'localizedValue',
                    valueField: 'name',
                    forceSelection: false,
                    editable: false,
                    allowBlank: true,
                    multiSelect: true,
                    queryMode: 'local',
                    triggerAction: 'all',
                    listConfig: {
                        getInnerTpl: function () {
                            return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {name} </div>';
                        }
                    }
                },
                {
                    name: 'latestStatus',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestStatus', 'DSH', 'Latest status')
                },
                {
                    name: 'latestResult',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.latestResult', 'DSH', 'Latest result')
                },
                {
                    name: 'comPortPool',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.comPortPool', 'DSH', 'Communication port pool')
                },
                {
                    name: 'connectionType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.connectionType', 'DSH', 'Connection type')
                },
                {
                    name: 'deviceType',
                    fieldLabel: Uni.I18n.translate('connection.widget.sideFilter.deviceType', 'DSH', 'Device type')
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('searchItems.searchAll', 'MDC', 'Apply'),
                            ui: 'action',
                            itemId: 'searchAllItems',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('searchItems.clearAll', 'MDC', 'Clear all'),
                            itemId: 'clearAllItems',
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});
