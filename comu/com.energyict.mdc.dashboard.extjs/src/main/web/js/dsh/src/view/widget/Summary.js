/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.Summary', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    requires: [ 'Uni.view.widget.Bar' ],
    alias: 'widget.summary',
    itemId: 'summary',
    flex: 1,
    title: Uni.I18n.translate('overview.widget.summary.title', 'DSH', 'Summary'),
    header: {
        ui: 'small'
    },
    layout: 'hbox',
    initComponent: function () {
        var me = this;

        this.items = [
            {
                flex: 1,
                xtype: 'container',
                itemId: 'target-container',
                layout: 'vbox',
                style: {
                    marginRight: '20px',
                    color: '#686868'
                },
                items: []
            },
            {
                flex: 2,
                xtype: 'dataview',
                itemId: 'summary-dataview',
                itemSelector: 'tbody.item',
                cls: 'summary',
                total: 0,
                tpl: '<table style="margin-left:5px">' +
                    '<tpl for=".">' +
                    '<tbody class="item item-{#}">' +
                    '{% var parentIndex = xindex; %}' +
                    '<tr>' +
                    '<td class="label">' +
                    '<tpl if="href">' +
                    '<a id="label-{displayName}" href="{href}">{displayName}</a><tpl else>{displayName}</tpl>' +
                    '</td>' +
                    '<td width="100%" id="bar-{[parentIndex]}" class="bar-{[parentIndex]} bar-{name}"></td>' +
                    '</tr>' +
                    '<tpl for="counters">' +
                    '<tr class="child">' +
                    '<td class="label">{displayName}</td>' +
                    '<td width="100%" id="bar-{[parentIndex]}-{#}" class="bar-{[parentIndex]}-{#} bar-{name}"></td>' +
                    '</tr>' +
                    '</tpl>' +
                    '</tbody>' +
                    '</tpl>' +
                    '</table>',
                listeners: {
                    refresh: function (view) {
                        Ext.suspendLayouts();
                        Ext.each(view.getNodes(), function (node, index) {
                            var record = view.getRecord(node),
                                pos = index + 1;

                            if (record.counters()) {
                                record.counters().each(function (data, idx) {
                                    var bar = Ext.widget('bar', {
                                        limit: record.get('count'),
                                        total: view.total,
                                        count: data.get('count'),
                                        label: !record.get('count') ? 0 : Math.round(!view.total ? 0 : data.get('count') * 100 / record.get('count')) + '% (' + data.get('count') + ')'
                                    });
                                    bar.render(node.querySelector('.bar-' + pos + '-' + (idx + 1)));
                                });
                            }

                            var bar = Ext.widget('bar', {
                                limit: view.total,
                                total: view.total,
                                count: record.get('count'),
                                label: Math.round(!view.total ? 0 : record.get('count') * 100 / view.total) + '% (' + record.get('count') + ')'
                            });
                            bar.render(node.querySelector('.bar-' + pos));
                        });
                        view.updateLayout();
                        Ext.resumeLayouts(true);
                    }
                }
            }
        ];
        this.callParent(arguments);
    },

    setRecord: function (record) {
        var me = this,
            view = me.down('#summary-dataview'),
            targetContainer = me.down('#target-container'),
            total = record.get('total'),
            target = record.get('target'),
            title = 'Title';

        view.total = total || 0;
        view.record = record;
        me.setTitle(' ');

        record.counters().each(function (item) {
            if (item.get('id')) {
                var filter = me.router.filter.getWriteData(true, true);
                filter[record.get('alias')] = item.get('id');
                var href = me.createUrl(me.router.getRoute('workspace/' + me.parent + '/details').buildUrl(null, {}), filter);
                item.set('href', href);
                item.set('itemId', 'summary-percentage-' + item.get('displayName').toLowerCase());
            }
        });

        if (target) {
            targetContainer.show();
            me.initKpi(record);
        } else {
            targetContainer.hide();
        }

        view.bindStore(record.counters());

        switch (me.parent) {
            case 'connections':
                title = Uni.I18n.translate('overview.widget.connections.header', 'DSH', 'Active connections ({0})', [total]);
                break;
            case 'communications':
                title = Uni.I18n.translate('overview.widget.communications.header', 'DSH', 'Active communications ({0})', [total]);
                break;
            case 'communicationServers':
                title = Uni.I18n.translate('overview.widget.communicationServers.header', 'DSH', 'Active communication servers ({0})', [total]);
                break;
            case 'favoriteDeviceGroups':
                title = Uni.I18n.translate('overview.widget.favouriteDeviceGroups.header', 'DSH', 'My favourite device groups ({0})', [total]);
                break;
            case 'flaggedDevices':
                title = Uni.I18n.translate('overview.widget.flaggedDevices.header', 'DSH', 'My flagged devices ({0})', [total]);
                break;
            case 'openDataCollectionIssues':
                title = Uni.I18n.translate('overview.widget.openDataCollectionIssues.header', 'DSH', 'Open data collection issues ({0})', [total]);
                break;
        }
        me.setTitle('<h3>' + title + '</h3>');
    },

    createUrl: function(basePath, filter) {
        var url = basePath,
            questionMarkAdded = false;

        if (filter) {
            if (filter.hasOwnProperty('deviceGroup')) {
                if (!questionMarkAdded) {
                    url += '?';
                    questionMarkAdded = true;
                }
                url += ('deviceGroups=' + filter.deviceGroup);
            }
            if (filter.hasOwnProperty('currentStates') && Array.isArray(filter.currentStates)) {
                if (!questionMarkAdded) {
                    url += '?';
                }
                Ext.Array.each(filter.currentStates, function(state) {
                    url += ('&currentStates=' + state);
                });
            }
        }
        return url;
    },

    initKpi: function (record) {
        var me = this,
            targetContainer = me.down('#target-container'),
            total = record.get('total'),
            target = record.get('target'),
            counters = record.counters();

        var success = counters.getAt(counters.findBy(function(r){return r.get('name') === 'success'}));
        var successRate = Math.round(!total ? 0 : success.get('count') * 100 / total);
        var diff = successRate - target;
        var direction = diff >= 0 ? 'above' : 'below';
        var color = diff >= 0 ? 'bar-success' : 'bar-failed';

        Ext.suspendLayouts();
        targetContainer.removeAll();
        targetContainer.add([
            {
                xtype: 'bar',
                threshold: record.get('target'),
                margin: '10 0',
                width: '100%',
                limit: total,
                total: total,
                count: success.get('count'),
                cls: color
            },
            {
                xtype: 'label',
                cls: 'large',
                text: Uni.I18n.translate('overview.widget.label.success', 'DSH', '{0}% success', [successRate])
            },
            {
                xtype: 'label',
                cls: direction,
                text: direction === 'above'?
                    Uni.I18n.translate('overview.widget.percentage.aboveLabel', 'DSH', '{0}% above target ({1}%)', [Math.abs(diff),target]) :
                    Uni.I18n.translate('overview.widget.percentage.belowLabel', 'DSH', '{0}% below target ({1}%)', [Math.abs(diff),target])
            }
        ]);
        Ext.resumeLayouts(true);
    }
});