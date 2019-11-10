/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.view.step.SelectUsagePoints', {

    extend: 'Ext.panel.Panel',

    alias: 'widget.select-usage-points-step',

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
        'Imt.usagepointgroupsfromissues.view.grid.util.UsagePointsNotFoundPanel',
        'Imt.usagepointgroupsfromissues.view.grid.UsagePointsGrid'
    ],

    ui: 'large',

    layout: 'card',

    config: {
        service: null,
    },

    initComponent: function () {
        var me = this,
            store = me.service.getSearchPropertiesStore();

        me.items = [
            {
                xtype: 'preview-container',
                itemId: 'search-preview-container',
                grid: {
                    xtype: 'usage-points-grid',
                    itemId: 'usage-points-grid',
                    service: me.getService()
                },
                emptyComponent: {
                    xtype: 'usage-points-not-found-panel',
                    itemId: 'usage-points-not-found-panel'
                }
            }
        ];

        me.tbar = {
            xtype: 'panel',
            items: [
                {
                    itemId: 'step-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true
                },
                {
                    xtype: 'panel',
                    itemId: 'grid-filter',
                    maskElement: 'el',
                    ui: 'filter',
                    defaults: {
                        xtype: 'panel',
                        layout: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'uni-search-internal-criteriapanel',
                            itemId: 'search-criteria-sticky',
                            layout: 'column',
                            lbar: {
                                xtype: 'label',
                                text: Uni.I18n.translate('searchItems.filter.criteria', 'IMT', 'Criteria'),
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
                            xtype: 'uni-search-internal-criteriapanel',
                            itemId: 'search-criteria-removable',
                            layout: 'column',
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
                                text: Uni.I18n.translate('general.apply', 'IMT', 'Apply'),
                                action: 'search'
                            },
                            {
                                xtype: 'button',
                                itemId: 'clear-all-button',
                                text: Uni.I18n.translate('general.clearAll', 'IMT', 'Clear all'),
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
                    html: Uni.I18n.translate('usagepointgroupfromissues.wizard.step.selectUsagePoints.grid.validationMsg', 'IMT', 'Select at least one usage point'),
                    hidden: true
                }
            ]
        };

        me.callParent(arguments);

        var panel = me.down('#grid-filter');
        var listeners = store.on({
            beforeload: function () {
                panel.setLoading(true);
            },
            load: function () {
                panel.setLoading(false);
            },
            scope: me,
            destroyable: true
        });

        me.on('render', function () {
            me.down('#search-criteria-selector').setSearchContainer(Ext.ComponentQuery.query('contentcontainer')[0]);
        });
        me.on('destroy', function () {
            listeners.destroy();
        });
    }

});