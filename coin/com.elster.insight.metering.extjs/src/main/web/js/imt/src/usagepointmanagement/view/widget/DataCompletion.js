Ext.define('Imt.usagepointmanagement.view.widget.DataCompletion', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.data-completion-widget',
    requires: [
        'Imt.usagepointmanagement.store.DataCompletion',
        'Imt.usagepointmanagement.view.widget.OutputKpi',
        'Ext.util.Bindable'
    ],
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    overflowY: 'auto',
    title: Uni.I18n.translate('deviceGeneralInformation.whatsGoingOn', 'UNI', 'What\'s going on'),
    ui: 'tile',
    router: null,
    layout: 'fit',
    store: 'Imt.usagepointmanagement.store.DataCompletion',

    //todo: change it after integration
    config: {
        purpose: {id: 1},
        period: {id: 1}
    },

    initComponent: function () {
        var me = this,
            store;

        me.items = {
            xtype: 'tabpanel',
            layout: 'fit',
            tabPosition: 'bottom',
            tabBar: {
                layout: {pack: 'center'}
            },
            defaults: {
                listeners: {
                    activate: function (tab, eOpts) {
                        tab.setIconCls('icon-circle');
                    },
                    deactivate: function (tab, eOpts) {
                        tab.setIconCls('icon-circle2');
                    }
                }
            }
        };

        // todo: add purpose and period stores here
        //var healthTypeStore = Ext.getStore('Uni.store.HealthCategories') || Ext.create('Uni.store.HealthCategories');
        //me.tools = [
        //    {
        //        xtype: 'toolbar',
        //        itemId: 'comboTool',
        //        margin: '0 20 0 0',
        //        layout: 'fit',
        //        items: [
        //            {
        //                xtype: 'combobox',
        //                value: 'all',
        //                store: healthTypeStore,
        //                displayField: 'displayValue',
        //                valueField: 'type',
        //                listeners: {
        //                    change: function (combo, newvalue) {
        //                        me.buildWidget(newvalue);
        //                    }
        //                }
        //            }
        //        ]
        //
        //    }
        //];

        me.callParent(arguments);
        me.bindStore(me.store || 'ext-empty-store', true);
        store = me.getStore();
        store.getProxy().extraParams = {
            usagePointMRID: me.usagePoint.get('mRID'),
            purposeId: me.getPurpose().id,
            periodId: me.getPeriod().id
        };

        store.load();
    },

    getStoreListeners: function () {
        return {
            beforeload: this.onBeforeLoad,
            load: this.onLoad
        };
    },

    onBeforeLoad: function () {
        this.setLoading(true);
    },

    onLoad: function () {
        this.reconfigure();
        this.setLoading(false);
    },

    reconfigure: function() {
        var me = this,
            store = me.getStore(),
            tabs = [];

        store.each(function (item, index, total) {
            var mod = Math.floor(index / 3);
            if (!tabs[mod]) {
                tabs[mod] = me.addTab()
            }

            tabs[mod].items.push(me.addWidget(item));
        });

        me.down('tabpanel').add(tabs);
    },

    addTab: function() {
        return {
            layout: 'column',
            columnWidth: 0.3,
            iconCls: mod === 0 ? 'icon-circle' : 'icon-circle2',
            items: []
        };
    },


    addWidget: function (output) {
        var me = this;

        return {
            xtype: 'output-kpi-widget',
            output: output,
            purpose: me.getPurpose(),
            router: me.router
        };
    }
});