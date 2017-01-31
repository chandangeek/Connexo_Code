/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagepoint-group-wizard-step2',
    xtype: 'usagepoint-group-wizard-step2',
    ui: 'large',

    requires: [
        'Imt.usagepointgroups.view.NoUsagePointsFound',
        'Imt.usagepointgroups.view.StaticGroupUsagePointsGrid',
        'Imt.usagepointgroups.view.DynamicGroupUsagePointsGrid'
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
                    xtype: 'static-group-usagepoints-grid',
                    itemId: 'static-group-usagepoints-grid',
                    service: me.getService()
                },
                emptyComponent: {
                    itemId: 'static-group-no-usagepoints-found-panel',
                    xtype: 'no-usagepoints-found-panel'
                }
            },
            {
                xtype: 'emptygridcontainer',
                itemId: 'dynamic-group-preview-container',
                grid: {
                    maxHeight: 450,
                    xtype: 'dynamic-group-usagepoints-grid',
                    itemId: 'dynamic-group-usagepoints-grid',
                    service: me.getService()
                },
                emptyComponent: {
                    itemId: 'dynamic-group-no-usagepoints-found-panel',
                    xtype: 'no-usagepoints-found-panel'
                }
            }
        ];

        me.tbar = {
            xtype: 'panel',
            items: [
                {
                    itemId: 'step2-add-usagepointgroup-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true
                },
                {
                    xtype: 'panel',
                    itemId: 'usagepoint-group-filter',
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
                            lbar: {
                                xtype: 'label',
                                text: Uni.I18n.translate('general.criteria', 'IMT', 'Criteria'),
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
                                text: Uni.I18n.translate('general.preview', 'IMT', 'Preview'),
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
                    html: Uni.I18n.translate('usagepointgroup.noCriteriasSelected', 'IMT', 'Specify at least one search criterion'),
                    hidden: true
                }
            ]
        };

        me.callParent(arguments);

        var panel = me.down('#usagepoint-group-filter'),
            listeners = store.on({
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
            me.down('search-criteria-selector').setSearchContainer(Ext.ComponentQuery.query('contentcontainer')[0]);
        });
        me.on('destroy', function () {
            listeners.destroy();
        });
    }
});
