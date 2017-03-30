/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-group-wizard-step2',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.view.search.field.internal.Adapter',
        'Uni.view.search.field.Selection',
        'Uni.view.search.field.Simple',
        'Uni.grid.column.search.DeviceType',
        'Uni.grid.column.search.DeviceConfiguration',
        'Uni.grid.column.search.Quantity',
        'Uni.grid.column.search.Boolean',
        'Uni.view.container.EmptyGridContainer',
        'Mdc.view.setup.devicegroup.NoDevicesFound',
        'Mdc.view.setup.devicegroup.StaticGroupDevicesGrid',
        'Mdc.view.setup.devicegroup.DynamicGroupDevicesGrid'
    ],

    layout: 'card',

    config: {
        service: null
    },
    isPrepared: false,

    initComponent: function () {
        var me = this,
            store = me.service.getSearchPropertiesStore();

        me.items = [
            {
                xtype: 'emptygridcontainer',
                itemId: 'static-group-preview-container',
                grid: {
                    xtype: 'static-group-devices-grid',
                    itemId: 'static-group-devices-grid',
                    service: me.getService()
                },
                emptyComponent: {
                    itemId: 'static-group-no-devices-found-panel',
                    xtype: 'no-devices-found-panel'
                }
            },
            {
                xtype: 'emptygridcontainer',
                itemId: 'dynamic-group-preview-container',
                grid: {
                    maxHeight: 450,
                    xtype: 'dynamic-group-devices-grid',
                    itemId: 'dynamic-group-devices-grid',
                    service: me.getService()
                },
                emptyComponent: {
                    itemId: 'dynamic-group-no-devices-found-panel',
                    xtype: 'no-devices-found-panel'
                }
            }
        ];

        me.tbar = {
            xtype: 'panel',
            items: [
                {
                    itemId: 'step2-adddevicegroup-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true
                },
                {
                    xtype: 'panel',
                    itemId: 'device-group-filter',
                    maskElement: 'el',
                    ui: 'filter',
                    defaults: {
                        xtype: 'panel',
                        layout: 'hbox'
                    },
                    items: [
                        {
                            // Sticky criteria.
                            xtype: 'uni-search-internal-criteriapanel',
                            itemId: 'search-criteria-sticky',
                            layout: 'column',
                            //hidden: true,
                            lbar: {
                                xtype: 'label',
                                text: Uni.I18n.translate('searchItems.filter.criteria', 'MDC', 'Criteria'),
                                width: 100
                            },
                            rbar: [{
                                disabled: true,
                                itemId: 'search-criteria-selector',
                                xtype: 'search-criteria-selector',
                                service: me.getService()
                            }],
                            margin: '10 0 0 0',
                            service: me.getService(),
                            sticky: true
                        },
                        {
                            // Removable criteria.
                            xtype: 'uni-search-internal-criteriapanel',
                            itemId: 'search-criteria-removable',
                            layout: 'column',
                            //hidden: true,
                            margin: '0 0 0 100',
                            service: me.getService()
                        }
                    ],
                    bbar: {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            pack: 'end'
                        },
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'search-button',
                                ui: 'action',
                                text: Uni.I18n.translate('general.preview', 'MDC', 'Preview'),
                                action: 'search'
                            },
                            {
                                xtype: 'button',
                                itemId: 'clear-all-button',
                                text: Uni.I18n.translate('general.clearAll', 'MDC', 'Clear all'),
                                action: 'clearFilters',
                                margin: '0 0 0 0',
                                disabled: true
                            }
                        ]
                    }
                },
                {
                    xtype: 'component',
                    itemId: 'selection-criteria-error',
                    cls: 'x-form-invalid-under',
                    html: Uni.I18n.translate('devicegroup.noCriteriasSelected', 'MDC', 'Specify at least one search criterion'),
                    hidden: true
                }
            ]
        };

        me.callParent(arguments);

        var panel = me.down('#device-group-filter');
        var listeners = store.on({
            beforeload:  function() {
                panel.setLoading(true);
            },
            load: function() {
                panel.setLoading(false);
            },
            scope: me,
            destroyable: true
        });

        me.on('render', function() {
            me.down('search-criteria-selector').setSearchContainer(Ext.ComponentQuery.query('contentcontainer')[0]);
        });
        me.on('destroy', function () {
            listeners.destroy();
        });
    }
});
