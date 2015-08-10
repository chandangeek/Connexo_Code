Ext.define("Dsh.controller.BaseController", {
    extend: "Ext.app.Controller", prefix: "", init: function () {
        if (this.prefix) {
            var a = {};
            a[this.prefix + " filter-top-panel"] = {removeFilter: this.removeFilter, clearAllFilters: this.clearFilter};
            a[this.prefix + " #filter-form side-filter-combo"] = {updateTopFilterPanelTagButtons: this.onFilterChange};
            a[this.prefix + " button[action=applyfilter]"] = {click: this.applyFilter};
            a[this.prefix + " button[action=clearfilter]"] = {click: this.clearFilter};
            this.control(a)
        }
        this.callParent(arguments)
    }, initFilter: function () {
        var a = this.getController("Uni.controller.history.Router");
        this.getSideFilterForm().loadRecord(a.filter);
        this.setFilterTimeInterval(a.filter.startedBetween, "started", "startedBetween");
        this.setFilterTimeInterval(a.filter.finishedBetween, "finished", "finishedBetween")
    }, setFilterTimeInterval: function (b, e, d) {
        var k = "", h = b ? b.get("from") : undefined, f = b ? b.get("to") : undefined, g = e.charAt(0).toUpperCase() + e.slice(1), j = Uni.I18n.translate("connection.widget." + e, "DSH", g), l = Uni.I18n.translate("connection.widget.between", "DSH", "between"), a = Uni.I18n.translate("connection.widget.after", "DSH", "after"), i = Uni.I18n.translate("connection.widget.before", "DSH", "before"), c = Uni.I18n.translate("connection.widget.and", "DSH", "and");
        if (b && (h || f)) {
            if (h && f) {
                k += " " + l + " ";
                k += Uni.DateTime.formatDateTimeShort(h);
                k += " " + c + " ";
                k += Uni.DateTime.formatDateTimeShort(f)
            }
            if (h && !f) {
                k += Uni.DateTime.formatDateTimeShort(h)
            }
            if (!h && f) {
                k += Uni.DateTime.formatDateTimeShort(f)
            }
            this.getFilterPanel().setFilter(d, j, k)
        }
    }, onFilterChange: function (a) {
        if (!_.isEmpty(a.getRawValue())) {
            this.getFilterPanel().setFilter(a.getName(), a.getFieldLabel(), a.getRawValue())
        }
    }, applyFilter: function () {
        this.getSideFilterForm().updateRecord();
        this.getSideFilterForm().getRecord().save()
    }, clearFilter: function () {
        this.getSideFilterForm().getRecord().getProxy().destroy()
    }, removeFilter: function (c) {
        var b = this.getController("Uni.controller.history.Router"), a = b.filter;
        switch (c) {
            case"startedBetween":
                delete a.startedBetween;
                break;
            case"finishedBetween":
                delete a.finishedBetween;
                break;
            default:
                a.set(c, null)
        }
        a.save()
    }
});
Ext.define("Dsh.view.widget.HeaderSection", {
    extend: "Ext.panel.Panel",
    alias: "widget.header-section",
    itemId: "header-section",
    layout: "fit",
    router: null,
    ui: "large",
    initComponent: function () {
        var a = this;
        a.title = a.router.getRoute().title;
        this.items = [{
            xtype: "toolbar",
            items: [{xtype: "device-group-filter", router: a.router}, "->", {
                xtype: "displayfield",
                itemId: "last-updated-field",
                style: "margin-right: 10px"
            }, {
                xtype: "button",
                itemId: "refresh-btn",
                style: {"background-color": "#71adc7"},
                text: Uni.I18n.translate("overview.widget.headerSection.refreshBtnTxt", "DSH", "Refresh"),
                icon: "/apps/sky/resources/images/form/restore.png"
            }]
        }];
        this.callParent(arguments)
    }
});
Ext.define("Dsh.view.widget.common.Bar", {
    alias: "widget.bar",
    extend: "Ext.Component",
    requires: ["Ext.Template", "Ext.CompositeElement", "Ext.TaskManager", "Ext.layout.component.ProgressBar"],
    total: 100,
    limit: 100,
    count: 0,
    baseCls: Ext.baseCSSPrefix + "bar",
    trackTpl: '<div class="{baseCls}-track" style="width: {count}%;"></div>',
    renderTpl: ['<tpl for=".">', '<span class="{baseCls}-label">{label}</span>', '<div class="{baseCls}-container" style="width: 100%;">', '<div class="{baseCls}-fill" style="width: {limit}%;">', "{track}", "</div>", '<tpl if="threshold">', '<div class="threshold" style="left: {threshold}%;">', "</tpl>", "<div>", "</tpl>"],
    prepareData: function () {
        var a = this;
        return {
            limit: !a.total ? 0 : Math.round(a.limit * 100 / a.total),
            label: a.label,
            count: !a.limit ? 0 : Math.round(a.count * 100 / a.limit),
            threshold: a.threshold
        }
    },
    initRenderData: function () {
        var b = this;
        var a = new Ext.XTemplate(b.trackTpl);
        var c = Ext.apply(b.callParent(), b.prepareData());
        return Ext.apply(c, {track: a.apply(c)})
    }
});
Ext.define("Dsh.view.widget.Summary", {
    extend: "Ext.panel.Panel",
    ui: "tile",
    requires: ["Dsh.view.widget.common.Bar"],
    alias: "widget.summary",
    itemId: "summary",
    title: Uni.I18n.translate("overview.widget.summary.title", "DSH", "Summary"),
    header: {ui: "small"},
    layout: "hbox",
    initComponent: function () {
        var a = this;
        this.items = [{
            flex: 1,
            xtype: "container",
            itemId: "target-container",
            layout: "vbox",
            style: {marginRight: "20px"},
            items: []
        }, {
            flex: 2,
            xtype: "dataview",
            itemId: "summary-dataview",
            itemSelector: "tbody.item",
            cls: "summary",
            total: 0,
            tpl: '<table><tpl for="."><tbody class="item item-{#}">{% var parentIndex = xindex; %}<tr><td class="label"><tpl if="href"><a id="label-{displayName}" href="{href}">{displayName}</a><tpl else>{displayName}</tpl></td><td width="100%" id="bar-{[parentIndex]}" class="bar-{[parentIndex]} bar-{name}"></td></tr><tpl for="counters"><tr class="child"><td class="label">{displayName}</td><td width="100%" id="bar-{[parentIndex]}-{#}" class="bar-{[parentIndex]}-{#} bar-{name}"></td></tr></tpl></tbody></tpl></table>',
            listeners: {
                refresh: function (b) {
                    Ext.suspendLayouts();
                    Ext.each(b.getNodes(), function (f, d) {
                        var c = b.getRecord(f), g = d + 1;
                        if (c.counters()) {
                            c.counters().each(function (j, h) {
                                var i = Ext.widget("bar", {
                                    limit: c.get("count"),
                                    total: b.total,
                                    count: j.get("count"),
                                    label: !c.get("count") ? 0 : Math.round(!b.total ? 0 : j.get("count") * 100 / c.get("count")) + "% (" + j.get("count") + ")"
                                });
                                i.render(f.querySelector(".bar-" + g + "-" + (h + 1)))
                            })
                        }
                        var e = Ext.widget("bar", {
                            limit: b.total,
                            total: b.total,
                            count: c.get("count"),
                            label: Math.round(!b.total ? 0 : c.get("count") * 100 / b.total) + "% (" + c.get("count") + ")"
                        });
                        e.render(f.querySelector(".bar-" + g))
                    });
                    b.updateLayout();
                    Ext.resumeLayouts(true)
                }
            }
        }];
        this.callParent(arguments)
    },
    setRecord: function (b) {
        var e = this, a = e.down("#summary-dataview"), c = e.down("#target-container"), d = b.get("total"), f = b.get("target");
        a.total = d || 0;
        a.record = b;
        b.counters().each(function (i) {
            if (i.get("id")) {
                var h = e.router.filter.getWriteData(true, true);
                h[b.get("alias")] = i.get("id");
                var g = e.router.getRoute("workspace/" + e.parent + "/details").buildUrl(null, {filter: h});
                i.set("href", g)
            }
        });
        if (f) {
            c.show();
            e.initKpi(b)
        } else {
            c.hide()
        }
        a.bindStore(b.counters());
        e.setTitle(Uni.I18n.translatePlural("overview.widget." + e.parent + ".header", d, "DSH", "<h3>" + e.wTitle + " ({0})</h3>"))
    },
    initKpi: function (d) {
        var f = this, c = f.down("#target-container"), g = d.get("total"), e = d.get("target"), h = d.counters();
        var k = h.getAt(h.findBy(function (l) {
            return l.get("name") === "success"
        }));
        var a = Math.round(!g ? 0 : k.get("count") * 100 / g);
        var j = a - e;
        var i = j >= 0 ? "above" : "below";
        var b = j >= 0 ? "bar-success" : "bar-failed";
        Ext.suspendLayouts();
        c.removeAll();
        c.add([{
            xtype: "bar",
            threshold: d.get("target"),
            margin: "10 0",
            width: "100%",
            limit: g,
            total: g,
            count: k.get("count"),
            cls: b
        }, {
            cls: "large",
            html: "<h4>" + Uni.I18n.translatePlural("overview.widget." + f.parent + ".label.success", a, "DSH", "<b>{0}%</b> success") + "</h4>"
        }, {
            cls: i,
            html: Uni.I18n.translatePlural("overview.widget." + f.parent + ".label." + i, Math.abs(j), "DSH", "<b>{0}%</b> " + i)
        }]);
        Ext.resumeLayouts(true)
    }
});
Ext.define("Dsh.view.widget.CommunicationServers", {
    extend: "Ext.panel.Panel",
    alias: "widget.communication-servers",
    store: "CommunicationServerInfos",
    ui: "tile",
    tbar: {xtype: "container", itemId: "connection-summary-title-panel"},
    items: [{
        xtype: "dataview",
        itemId: "servers-dataview",
        itemSelector: "tbody.comserver",
        emptyText: Uni.I18n.translate("overview.widget.communicationServers.noServersFound", "DSH", "No communication servers found"),
        tpl: new Ext.XTemplate('<table  style="margin: 5px 0 10px 0">', '<tpl for=".">', '<tbody class="comserver">', '<tpl if="!values.expand">', '<tpl if="children">', "<tr>", '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{name}.png" /></td>', "<td>{children.length} {title}</td>", '<td style="padding-left: 15px;"><img data-qtitle="{children.length} {title}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" src="/apps/sky/resources/images/shared/icon-info-small.png" /></td>', "</tr>", "<tpl else>", "<tr>", '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{name}.png" /></td>', "<td>{title}</td>", '<td style="padding-left: 15px;"></td>', "</tr>", "</tpl>", "<tpl else>", '<tpl for="values.children">', '<tr id="{comServerId}">', '<td style="padding-right: 5px;"><img src="/apps/dsh/resources/images/widget/{[parent.name]}.png" /></td>', '<td><a href="{href}">{title}</a></td>', '<td style="padding-left: 15px;"><img data-qtitle="{title}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" src="/apps/sky/resources/images/shared/icon-info-small.png" /></td>', "</tr>", "</tpl>", "</tpl>", "</tbody>", "</tpl>", "</table>")
    }, {xtype: "container", itemId: "target-container", layout: "vbox", style: {marginRight: "20px"}, items: []}],
    serverTpl: new Ext.XTemplate("<table>", "<tr>", '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate("overview.widget.communicationServers.server.name", "DSH", "Name") + "</td>", "<td>{comServerName}</td>", "</tr>", "<tr>", '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate("overview.widget.communicationServers.server.type", "DSH", "Type") + "</td>", "<td>{comServerType}</td>", "</tr>", '<tpl if="blockedSince">', "<tr>", '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate("overview.widget.communicationServers.tt.downSince", "DSH", "Not responding since") + "</td>", '<td>{[Ext.util.Format.date(new Date(values.blockedSince), "D M j, Y G:i")]}</td>', "</tr>", "</tpl>", "</table>"),
    reload: function () {
        var c = this, b = c.down("#target-container"), d = c.down("#servers-dataview"), a = Ext.getStore(c.store);
        c.setLoading();
        b.removeAll();
        a.load(function () {
            var f = "<h3>" + Uni.I18n.translatePlural("overview.widget.communicationServers.header", a.count(), "DSH", "Active communication servers ({0})") + "</h3>";
            if (c.down("#connection-summary-title-panel")) {
                c.down("#connection-summary-title-panel").update(f)
            }
            var e = a.getGroups().map(function (h) {
                h.title = Uni.I18n.translate("overview.widget.communicationServers.title." + h.name, "DSH", h.name);
                h.expand = (h.name === "blocked" && h.children && h.children.length < 5);
                var g = "";
                if (h.children) {
                    h.children = h.children.map(function (j) {
                        var i = j.getData();
                        i.title = i.comServerName + " " + Uni.I18n.translate("overview.widget.communicationServers.status." + h.name, "DSH", h.name);
                        i.href = c.router.getRoute("administration/comservers/detail/overview").buildUrl({id: i.comServerId});
                        i.tooltip = c.serverTpl.apply(i);
                        g += i.tooltip;
                        return i
                    })
                }
                h.tooltip = g;
                return h
            });
            d.bindStore(Ext.create("Ext.data.Store", {
                fields: ["children", "name", "title", "tooltip", "expand"],
                data: e
            }));
            b.add({
                xtype: "button",
                itemId: "lnk-view-all-communication-servers",
                ui: "link",
                text: Uni.I18n.translate("overview.widget.communicationServers.viewAll", "DSH", "View all"),
                href: typeof c.router.getRoute("administration/comservers") !== "undefined" ? c.router.getRoute("administration/comservers").buildUrl() : ""
            });
            c.setLoading(false)
        })
    }
});
Ext.define("Dsh.view.widget.QuickLinks", {
    extend: "Ext.panel.Panel",
    ui: "tile",
    alias: "widget.quick-links",
    data: [],
    items: [{
        itemId: "quicklinksTplPanel",
        tpl: new Ext.XTemplate('<div class="quick-links">', "<h3>" + Uni.I18n.translate("overview.widget.quicklinks.title", "DSH", "Quick links") + "</h3>", "<ul>", '<tpl for=".">', '<tpl if="href">', '<li><a href="{href}"', '<tpl if="target">', ' target="{target}"', "</tpl>", ">{link}</a></li>", "</tpl>", "</tpl>", "</ul>", "</div>")
    }],
    initComponent: function () {
        this.callParent(arguments);
        this.down("#quicklinksTplPanel").data = this.data
    }
});
Ext.define("Dsh.view.widget.ReadOutsOverTime", {
    extend: "Ext.panel.Panel",
    alias: "widget.read-outs-over-time",
    itemId: "read-outs-over-time",
    hidden: true,
    layout: "fit",
    yLabel: "",
    initComponent: function () {
        var a = this;
        this.tbar = [{
            xtype: "container",
            itemId: "readOutsTitle",
            baseCls: "x-panel-header-text-container-medium",
            html: a.wTitle
        }, {
            xtype: "container",
            flex: 1,
            width: 200,
            height: 30,
            html: '<svg id="read-outs-legend-container" style="width: 100%"></svg>'
        }];
        this.items = [{
            hidden: true,
            xtype: "container",
            itemId: "empty",
            html: "Connections over time are not measured for the selected group"
        }, {
            xtype: "container",
            flex: 1,
            hidden: true,
            itemId: "chart",
            height: 400,
            listeners: {
                resize: {
                    fn: function () {
                        if (a.chart) {
                            a.chart.setSize(Ext.getBody().getViewSize().width - 100, 400);
                            a.doLayout()
                        }
                    }
                }
            }
        }];
        this.callParent(arguments)
    },
    colorMap: {0: "#70BB52", 1: "#71ADC6", 2: "#EB5642", 3: "#a9a9a9"},
    setRecord: function (b) {
        var d = this;
        var c = d.router.filter;
        if (!c.get("deviceGroup") || !b) {
            d.hide()
        } else {
            d.show();
            var a = d.down("#chart");
            var e = d.down("#empty");
            if (b.get("time")) {
                a.show();
                e.hide();
                d.renderChart(a);
                d.chart.series.map(function (f) {
                    f.remove()
                });
                b.series().each(function (h, f) {
                    var g = h.getData();
                    g.color = d.colorMap[f];
                    g.data = _.zip(b.get("time"), g.data);
                    d.chart.addSeries(g)
                })
            } else {
                a.hide();
                e.show()
            }
        }
    },
    renderChart: function (a) {
        var b = this;
        Highcharts.setOptions({global: {useUTC: false}});
        this.chart = new Highcharts.Chart({
            chart: {
                type: "spline",
                zoomType: "x",
                renderTo: a.el.dom,
                reflow: false,
                width: Ext.getBody().getViewSize().width - 100,
                height: 400,
                events: {
                    load: function () {
                        $("#" + a.getId() + " .highcharts-legend").appendTo("#read-outs-legend-container")
                    }
                }
            },
            title: {text: ""},
            legend: {align: "left", verticalAlign: "top", floating: true, x: 25, y: -5},
            credits: {enabled: false},
            exporting: {enabled: false},
            tooltip: {
                positioner: function (d, g, c) {
                    var f, e;
                    if (c.plotY < 0) {
                        e = 0
                    } else {
                        e = c.plotY
                    }
                    f = c.plotY > g ? c.plotY - g : e + g / 2;
                    return {x: c.plotX, y: f}
                }, valueSuffix: "%"
            },
            plotOptions: {
                series: {animation: false},
                spline: {lineWidth: 3, states: {hover: {lineWidth: 5}}, marker: {enabled: false}}
            },
            xAxis: {
                type: "datetime",
                gridLineDashStyle: "Dot",
                gridLineWidth: 1,
                dateTimeLabelFormats: {
                    second: "%H:%M<br/>%a %e %b",
                    minute: "%H:%M<br/>%a %e %b",
                    hour: "%H:%M<br/>%a %e %b",
                    day: "%H:%M<br/>%a %e %b",
                    week: "%a %e<br/>%b %Y",
                    month: "%b<br/>%Y",
                    year: "%Y"
                }
            },
            yAxis: {
                title: {text: b.yLabel},
                labels: {format: "{value}%"},
                lineWidth: 2,
                tickWidth: 1,
                floor: 0,
                ceiling: 100,
                tickInterval: 10
            }
        }, function () {
            b.doLayout()
        })
    }
});
Ext.define("Dsh.view.widget.Overview", {
    extend: "Ext.panel.Panel",
    requires: ["Dsh.view.widget.common.Bar"],
    alias: "widget.overview",
    itemId: "overview",
    ui: "medium",
    mixins: {bindable: "Ext.util.Bindable"},
    layout: {type: "hbox", align: "stretch"},
    defaults: {flex: 1},
    total: 0,
    dockedItems: [{
        xtype: "toolbar",
        dock: "top",
        style: {padding: 0},
        items: [{
            xtype: "container",
            itemId: "title",
            html: "<h2>" + Uni.I18n.translate("overview.widget.overview.title", "DSH", "Overview") + "</h2>"
        }]
    }],
    bindStore: function (a) {
        var b = this;
        b.removeAll(true);
        a.each(function (e, c) {
            e.counters().sort([{property: "count", direction: "DESC"}, {property: "displayName", direction: "ASC"}]);
            e.counters().each(function (f) {
                if (f.get("id")) {
                    var h = b.router.filter.getWriteData(true, true);
                    h[e.get("alias")] = f.get("id");
                    var g = b.router.getRoute("workspace/" + b.parent + "/details").buildUrl(null, {filter: h});
                    f.set("href", g)
                }
            });
            var d = Ext.create("Ext.panel.Panel", {
                style: {padding: "20px", marginRight: !(c % 2) ? "20px" : 0},
                ui: "tile",
                tbar: {xtype: "container", itemId: "title", html: "<h3>" + e.get("displayName") + "</h3>"},
                items: {
                    xtype: "dataview",
                    itemId: e.get("alias") + "-dataview",
                    itemSelector: "tbody.item",
                    total: e.get("total") || b.total,
                    store: e.counters(),
                    tpl: '<table width="100%"><tpl for="."><tbody class="item item-{#}"><tr><td width="50%"><div style="overflow: hidden; text-overflow: ellipsis; padding-right: 20px"><tpl if="href"><a href="{href}">{displayName}</a><tpl else>{displayName}</tpl></div></td><td width="50%" id="bar-{#}"></td></tr></tbody></tpl></table>',
                    listeners: {
                        refresh: function (f) {
                            Ext.each(f.getNodes(), function (j, h) {
                                var g = f.getRecord(j), k = h + 1;
                                var i = Ext.widget("bar", {
                                    limit: g.get("count"),
                                    total: f.total,
                                    count: g.get("count"),
                                    label: g.get("count")
                                }).render(f.getEl().down("#bar-" + k))
                            })
                        }
                    }
                }
            });
            b.add(d)
        });
        b.mixins.bindable.bindStore.apply(this, arguments)
    }
});
Ext.define("Dsh.view.widget.common.StackedBar", {
    alias: "widget.stacked-bar",
    extend: "Dsh.view.widget.common.Bar",
    tooltipTpl: '<table><tpl foreach="."><tr><td>{[Uni.I18n.translate("overview.widget.breakdown." + xkey, "DSH", xkey)]}</td><td>{.}</td></tr></tpl></table>',
    trackTpl: ['<div data-qtip="{tooltip}">', '<tpl foreach="count">', '<div class="{parent.baseCls}-track {parent.baseCls}-track-stacked {[xkey]}" style="width: {.}%;"></div>', "</tpl>", "</div>"],
    prepareData: function () {
        var b = this;
        var a = _.object(_.map(b.count, function (d, c) {
            return [c, !b.limit ? 0 : d * 100 / b.limit]
        }));
        return Ext.apply(b.callParent(), {count: a, tooltip: new Ext.XTemplate(b.tooltipTpl).apply(b.count)})
    }
});
Ext.define("Dsh.view.widget.HeatMap", {
    extend: "Ext.panel.Panel",
    alias: "widget.heat-map",
    layout: "fit",
    items: {xtype: "panel", ui: "tile", minHeight: "300", itemId: "heatmapchart"},
    setChartData: function (b) {
        var a = this;
        a.chart.series[0].setData([], true);
        a.chart.series[0].setData(b, true)
    },
    setXAxis: function (a, c) {
        var b = this;
        c = c[0].toUpperCase() + c.slice(1);
        b.chart.series[0].xAxis.update({title: {text: c}}, false);
        b.chart.series[0].xAxis.update({categories: a}, false)
    },
    setYAxis: function (a, c) {
        var b = this;
        c = c[0].toUpperCase() + c.slice(1);
        b.chart.series[0].yAxis.update({title: {text: c}}, false);
        b.chart.series[0].yAxis.update({categories: a}, false)
    },
    findBorders: function (d) {
        var b = 0, e = 0, a = 0, c = 0;
        d.each(function (f) {
            Ext.each(f.data.data, function (h) {
                var g = h.count, i = (g == 0 ? g.toString() : g);
                c += parseInt(i);
                a = a < parseInt(i) ? parseInt(i) : a;
                ++e
            });
            e = 0;
            ++b
        });
        return {max: a, total: c}
    },
    storeToHighchartData: function (b) {
        var c = [], a = 0, d = 0;
        b.each(function (e) {
            Ext.each(e.data.data, function (g) {
                var f = g.count, h = (f == 0 ? f.toString() : f);
                if (d === 0) {
                    h = -h
                }
                c.push([d, a, h]);
                ++d
            });
            d = 0;
            ++a
        });
        return c
    },
    getCombo: function () {
        return this.down("#combine-combo")
    },
    loadChart: function (b, e) {
        if (b.getCount() > 0) {
            b.sort([{property: "displayValue", direction: "DESC"}]);
            var d = this, a = [], c = b.collect("displayValue");
            Ext.each(b.getAt(0).data.data, function (f) {
                a.push(f.displayName)
            });
            d.setXAxis(a, "Latest result");
            d.setYAxis(c, e);
            d.setChartData(d.storeToHighchartData(b))
        }
    },
    initComponent: function () {
        var b = this;
        b.store = Ext.getStore(b.store || "ext-empty-store");
        if (b.parent == "connections") {
            var a = Ext.create("Dsh.store.CombineStore");
            a.load();
            b.tbar = [{
                xtype: "container",
                itemId: "title",
                html: "<h2>" + Uni.I18n.translate("overview.widget.connections.heatmap.combineLabel", "DSH", "Combine latest result and") + "&nbsp;</h2>"
            }, {
                xtype: "combobox",
                labelWidth: 200,
                itemId: "combine-combo",
                displayField: "localizedValue",
                queryMode: "local",
                valueField: "breakdown",
                store: a,
                autoSelect: true,
                editable: false
            }]
        } else {
            if (b.parent == "communications") {
                b.tbar = [{
                    xtype: "container",
                    itemId: "title",
                    html: "<h2>" + Uni.I18n.translate("overview.widget.communications.heatmap.combineLabel", "DSH", "Combine latest result and device type") + "</h2>"
                }]
            }
        }
        this.callParent(arguments);
        if (b.parent == "connections") {
            var c = b.getCombo();
            c.on("change", function (e, d) {
                b.store.addFilter({property: "breakdown", value: d}, false);
                b.reload()
            });
            c.getStore().load(function () {
                if (c.getStore().getCount() > 0) {
                    c.select(c.getStore().getAt(1))
                }
            })
        } else {
            if (b.parent == "communications") {
                b.reload()
            }
        }
    },
    reload: function () {
        var c = this, a = c.store, b;
        a.load({
            callback: function () {
                var d = c.down("#heatmapchart");
                if (a.count() && d) {
                    b = 80 + a.count() * 50;
                    d.setHeight(b);
                    c.renderChart(d.getEl().down(".x-panel-body").dom, c.findBorders(a), b);
                    c.loadChart(a, c.getCombo() ? c.getCombo().getDisplayValue() : "Device type");
                    c.show();
                    c.doLayout()
                } else {
                    c.hide()
                }
            }
        })
    },
    renderChart: function (a, d, b) {
        var c = this;
        this.chart = new Highcharts.Chart({
            chart: {type: "heatmap", renderTo: a, reflow: false, height: b},
            exporting: {enabled: false},
            credits: {enabled: false},
            title: null,
            xAxis: {title: {style: {color: "#707070", fontWeight: "bold"}}, opposite: true},
            yAxis: {title: {style: {color: "#707070", fontWeight: "bold"}}},
            colorAxis: {stops: [[0, "#70BB51"], [0.5, "#ffffff"], [1, "#EB5642"]], min: -d.max, max: d.max},
            legend: {enabled: false},
            tooltip: {
                useHTML: true, formatter: function () {
                    var e, f;
                    switch (c.parent) {
                        case"connections":
                            e = Uni.I18n.translate("overview.widget.summary.connections", "DSH", "connections");
                            break;
                        case"communications":
                            e = Uni.I18n.translate("overview.widget.summary.communications", "DSH", "communications");
                            break
                    }
                    f = "<table><tbody>";
                    f += "<tr>";
                    f += '<td style="padding-right: 10px; text-align: right">' + Uni.I18n.translate("overview.widget.summary.numberOf", "DSH", "Number of") + " " + e + "</td>";
                    f += '<td style="padding-right: 1px; text-align: left"><b>' + Math.abs(this.point.value) + "</b></td>";
                    f += "</tr>";
                    f += "<tr>";
                    f += '<td style="padding-right: 10px; text-align: right">' + c.chart.options.yAxis[0].title.text + "</td>";
                    f += '<td style="padding-right: 1px; text-align: left"><b>' + this.series.yAxis.categories[this.point.y] + "</b></td>";
                    f += "</tr>";
                    f += "<tr>";
                    f += '<td style="padding-right: 10px; text-align: right">' + c.chart.options.xAxis[0].title.text + "</td>";
                    f += '<td style="padding-right: 1px; text-align: left"><b>' + this.series.xAxis.categories[this.point.x] + "</b></td>";
                    f += "</tr>";
                    f += "</tbody></table>";
                    return f
                }
            },
            series: [{
                name: "Latest Result",
                borderWidth: 1,
                dataLabels: {
                    enabled: true,
                    style: {color: "black", fontWeight: "normal", fontSize: 12, HcTextStroke: "0px rgba(0, 0, 0, 0.5)"},
                    formatter: function () {
                        if (this.point.value > 0) {
                            return "<b>" + ((this.point.value / d.total) * 100).toFixed(1) + "%</b>"
                        } else {
                            if (this.point.value < 0) {
                                return ((Math.abs(this.point.value) / d.total) * 100).toFixed(1) + "%"
                            } else {
                                return "0%"
                            }
                        }
                    }
                },
                states: {hover: {color: "#CBCBCB", borderColor: "#CBCBCB"}}
            }]
        }, function () {
            c.doLayout()
        })
    }
});
Ext.define("Dsh.view.widget.Breakdown", {
    extend: "Ext.panel.Panel",
    alias: "widget.breakdown",
    itemId: "breakdown",
    ui: "medium",
    cls: "breakdown",
    layout: {type: "vbox", align: "stretch"},
    requires: ["Ext.view.View", "Dsh.view.widget.common.StackedBar", "Ext.button.Button", "Dsh.view.widget.HeatMap"],
    mixins: {bindable: "Ext.util.Bindable"},
    itemsInCollapsedMode: 5,
    dockedItems: [{
        xtype: "toolbar",
        dock: "top",
        style: {padding: 0},
        items: [{
            xtype: "container",
            itemId: "title",
            html: "<h2>" + Uni.I18n.translate("overview.widget.breakdown.title", "DSH", "Breakdown") + "</h2>"
        }, "->", {
            xtype: "container",
            html: '<div class="legend"><ul><li><span class="color failed"></span> ' + Uni.I18n.translate("overview.widget.breakdown.failed", "DSH", "Failed") + '</li><li><span class="color success"></span> ' + Uni.I18n.translate("overview.widget.breakdown.success", "DSH", "Success") + '</li><li><span class="color ongoing"></span> ' + Uni.I18n.translate("overview.widget.breakdown.ongoing", "DSH", "Ongoing") + "</li></ul></div>"
        }]
    }],
    items: [{
        xtype: "panel",
        layout: {type: "hbox", align: "stretch"},
        defaults: {flex: 1},
        items: [{xtype: "panel", itemId: "summaries-0", style: {marginRight: "20px"}}, {
            xtype: "panel",
            itemId: "summaries-1"
        }]
    }],
    bindStore: function (a) {
        var b = this;
        b.down("#summaries-0").removeAll(true);
        b.down("#summaries-1").removeAll(true);
        a.each(function (f, c) {
            f.counters().sort([{property: "total", direction: "DESC"}, {property: "displayName", direction: "ASC"}]);
            var g = f.counters().first();
            var e = g ? g.get("total") : 0;
            var d = Ext.create("Ext.panel.Panel", {
                ui: "tile",
                tbar: {xtype: "container", itemId: "title", html: "<h3>" + f.get("displayName") + "</h3>"},
                buttonAlign: "left",
                buttons: [{
                    text: Uni.I18n.translate("overview.widget.breakdown.showMore", "DSH", "Show more"),
                    hidden: f.counters().count() <= b.itemsInCollapsedMode,
                    handler: function () {
                        b.summaryMoreLess(d)
                    }
                }],
                items: {
                    xtype: "dataview",
                    itemId: f.get("alias") + "-dataview",
                    itemSelector: "tbody.item",
                    total: f.get("total"),
                    store: f.counters(),
                    tpl: '<table width="100%"><tpl for="."><tbody class="item item-{#}"><tr><td width="50%"> <a><div style="overflow: hidden; text-overflow: ellipsis; padding-right: 20px">{displayName}</div></a></td><td width="50%" id="bar-{#}"></td></tr></tbody></tpl></table>',
                    listeners: {
                        refresh: function (h) {
                            Ext.each(h.getNodes(), function (n, k) {
                                var i = h.getRecord(n), p = k + 1;
                                var o = {
                                    failed: i.get("failedCount"),
                                    success: i.get("successCount"),
                                    ongoing: i.get("pendingCount")
                                };
                                var m = Ext.widget("stacked-bar", {
                                    limit: i.get("total"),
                                    total: e,
                                    count: o,
                                    label: i.get("total")
                                });
                                m.render(h.getEl().down("#bar-" + p));
                                var l = b.router.filter.getWriteData(true, true);
                                l[f.get("alias")] = i.get("id");
                                var j = b.router.getRoute("workspace/" + b.parent + "/details").buildUrl(null, {filter: l});
                                h.getEl().down(".item-" + p + " a").set({href: j})
                            });
                            h.collapsed = f.counters().count() > b.itemsInCollapsedMode;
                            h.expandedHeight = h.getHeight();
                            h.collapsedHeight = h.expandedHeight / f.counters().count() * b.itemsInCollapsedMode;
                            if (h.collapsed) {
                                h.setHeight(h.collapsedHeight)
                            }
                        }
                    }
                }
            });
            b.down("#summaries-" + c % 2).add(d)
        });
        b.mixins.bindable.bindStore.apply(this, arguments)
    },
    summaryMoreLess: function (b) {
        var a = b.down("dataview");
        b.down("button").setText(a.collapsed ? Uni.I18n.translate("overview.widget.breakdown.showLess", "DSH", "Show less") : Uni.I18n.translate("overview.widget.breakdown.showMore", "DSH", "Show more"));
        a.animate({duration: 300, to: {height: (a.collapsed ? a.expandedHeight : a.collapsedHeight)}});
        a.collapsed = !a.collapsed
    }
});
Ext.define("Dsh.view.CommunicationOverview", {
    extend: "Ext.container.Container",
    requires: ["Dsh.view.widget.HeaderSection", "Dsh.view.widget.Summary", "Dsh.view.widget.CommunicationServers", "Dsh.view.widget.QuickLinks", "Dsh.view.widget.ReadOutsOverTime", "Dsh.view.widget.Overview", "Dsh.view.widget.Breakdown"],
    alias: "widget.communication-overview",
    itemId: "communication-overview",
    autoScroll: true,
    layout: {type: "vbox", align: "stretch"},
    style: {padding: "0 20px"},
    defaults: {style: {marginBottom: "20px", padding: 0}},
    initComponent: function () {
        var a = this;
        a.items = [{xtype: "header-section", router: a.router, style: "none"}, {
            xtype: "panel",
            layout: {type: "hbox", align: "stretch"},
            defaults: {style: {marginRight: "20px", padding: "20px"}, flex: 1},
            items: [{
                xtype: "summary",
                flex: 2,
                wTitle: Uni.I18n.translate("communication.widget.summary.title", "DSH", "Communications summary"),
                parent: "communications",
                router: a.router
            }, {
                xtype: "communication-servers",
                itemId: "communication-servers",
                router: a.router
            }, {
                xtype: "quick-links",
                itemId: "quick-links",
                maxHeight: 256,
                overflowY: "auto",
                style: {marginRight: "0", padding: "20px"},
                data: [{
                    link: Uni.I18n.translate("communication.widget.quicklinks.viewAll", "DSH", "View all communications"),
                    href: a.router.getRoute("workspace/communications/details").buildUrl(null, a.router.queryParams)
                }, {
                    link: a.router.getRoute("workspace/connections").title,
                    href: a.router.getRoute("workspace/connections").buildUrl(null, a.router.queryParams)
                }, {
                    link: Uni.I18n.translate("communication.widget.quicklinks.myIssues", "DSH", "My open issues"),
                    href: typeof a.router.getRoute("workspace/datacollectionissues") !== "undefined" ? a.router.getRoute("workspace/datacollectionissues").buildUrl(null, a.router.queryParams) + "?myopenissues=true" : null
                }]
            }]
        }, {
            xtype: "read-outs-over-time",
            wTitle: Uni.I18n.translate("communications.widget.readOutsOverTime.title", "DSH", "Communications over time"),
            yLabel: Uni.I18n.translate("communications.widget.readOutsOverTime.yLabel", "DSH", "Number of communications"),
            router: a.router,
            parent: "communications"
        }, {
            xtype: "overview",
            category: "Communication",
            parent: "communications",
            router: a.router
        }, {xtype: "breakdown", parent: "communications", router: a.router}, {
            xtype: "heat-map",
            itemId: "heatmap",
            store: "Dsh.store.CommunicationResultsStore",
            router: a.router,
            parent: "communications"
        }];
        this.callParent(arguments)
    }
});
Ext.define("Dsh.model.Filterable", {
    extend: "Ext.data.Model",
    requires: ["Ext.data.writer.Json"],
    inheritableStatics: {
        setFilter: function (a) {
            var b = this.getProxy();
            var d = Ext.create("Ext.data.writer.Json", {writeRecordId: false});
            var c = _.map(d.getRecordData(a), function (f, e) {
                return {property: e, value: f}
            });
            b.setExtraParam("filter", Ext.encode(_.filter(c, function (e) {
                return !!e.value
            })))
        }
    }
});
Ext.define("Dsh.model.Counter", {
    extend: "Ext.data.Model",
    fields: [{name: "count", type: "int"}, {name: "alias", type: "string"}, {
        name: "displayName",
        type: "string"
    }, {
        name: "name", type: "string", mapping: function (a) {
            return Ext.isString(a.name) ? a.name.toLowerCase() : ""
        }
    }, {name: "total", type: "int"}],
    hasMany: {model: "Dsh.model.Counter", name: "counters"}
});
Ext.define("Dsh.model.Summary", {
    extend: "Ext.data.Model",
    requires: ["Dsh.model.Counter"],
    fields: [{name: "total", type: "int"}, {name: "target", type: "int"}, {name: "alias", type: "string"}],
    hasMany: {model: "Dsh.model.Counter", name: "counters"}
});
Ext.define("Dsh.model.BreakdownCounter", {
    extend: "Ext.data.Model",
    fields: [{name: "id", type: "int"}, {name: "displayName", type: "string"}, {
        name: "successCount",
        type: "int"
    }, {name: "failedCount", type: "int"}, {name: "pendingCount", type: "int"}, {
        name: "total",
        type: "int",
        persist: false,
        convert: function (b, a) {
            return a.get("successCount") + a.get("failedCount") + a.get("pendingCount")
        }
    }]
});
Ext.define("Dsh.model.Breakdown", {
    extend: "Ext.data.Model",
    requires: ["Dsh.model.BreakdownCounter"],
    fields: [{name: "displayName", type: "string"}, {name: "alias", type: "string"}, {
        name: "total",
        type: "int"
    }, {name: "totalSuccessCount", type: "int"}, {name: "totalPendingCount", type: "int"}, {
        name: "totalFailedCount",
        type: "int"
    }],
    hasMany: {model: "Dsh.model.BreakdownCounter", name: "counters"}
});
Ext.define("Dsh.model.Series", {extend: "Ext.data.Model", fields: [{name: "name", type: "string"}, {name: "data"}]});
Ext.define("Dsh.model.Kpi", {
    extend: "Ext.data.Model",
    requires: ["Dsh.model.Series"],
    fields: [{name: "time"}],
    hasMany: {model: "Dsh.model.Series", name: "series"}
});
Ext.define("Dsh.model.communication.Overview", {
    extend: "Dsh.model.Filterable",
    requires: ["Dsh.model.Summary", "Dsh.model.Counter", "Dsh.model.Breakdown", "Dsh.model.Kpi"],
    hasOne: [{
        model: "Dsh.model.Summary",
        associationKey: "communicationSummary",
        name: "summary",
        getterName: "getSummary",
        setterName: "setSummary"
    }, {model: "Dsh.model.Kpi", associationKey: "kpi", name: "kpi", getterName: "getKpi", setterName: "setKpi"}],
    hasMany: [{model: "Dsh.model.Counter", name: "overviews"}, {model: "Dsh.model.Breakdown", name: "breakdowns"}],
    proxy: {type: "ajax", url: "/api/dsr/communicationoverview"}
});
Ext.define("Dsh.model.CommunicationServerInfo", {
    extend: "Ext.data.Model",
    fields: [{name: "comServerId", type: "int"}, {name: "comServerName", type: "string"}, {
        name: "comServerType",
        type: "string"
    }, {name: "running", type: "boolean"}, {name: "blocked", type: "boolean"}, {
        name: "blockTime",
        type: "auto"
    }, {
        name: "status", type: "string", convert: function (b, a) {
            if (a.get("running")) {
                return a.get("blocked") ? "blocked" : "running"
            } else {
                return "stopped"
            }
        }
    }],
    associations: [{name: "blockTime", type: "hasOne", model: "Dsh.model.TimeInfo", associationKey: "blockTime"}],
    proxy: {
        type: "ajax",
        url: "../../api/dsr/comserverstatussummary",
        reader: {type: "json", root: "comServerStatusInfos"}
    }
});
Ext.define("Dsh.store.CommunicationServerInfos", {
    extend: "Ext.data.Store",
    storeId: "CommunicationServerInfos",
    requires: ["Dsh.model.CommunicationServerInfo"],
    model: "Dsh.model.CommunicationServerInfo",
    autoLoad: false,
    groupers: [{direction: "ASC", property: "status"}]
});
Ext.define("Dsh.controller.CommunicationOverview", {
    extend: "Ext.app.Controller",
    models: ["Dsh.model.communication.Overview"],
    stores: ["CommunicationServerInfos"],
    views: ["Dsh.view.CommunicationOverview"],
    refs: [{ref: "communicationOverview", selector: "#communication-overview"}, {
        ref: "header",
        selector: "#header-section"
    }, {ref: "summary", selector: "#summary"}, {
        ref: "communicationServers",
        selector: "#communication-servers"
    }, {ref: "overview", selector: "#overview"}, {ref: "breakdown", selector: "#breakdown"}, {
        ref: "kpi",
        selector: "#communication-overview read-outs-over-time"
    }, {ref: "quickLinks", selector: "#communication-overview #quick-links"}],
    init: function () {
        this.control({
            "#communication-overview #refresh-btn": {click: this.loadData},
            "#communication-overview #device-group": {change: this.updateQuickLinks}
        })
    },
    showOverview: function () {
        var b = this;
        var a = this.getController("Uni.controller.history.Router");
        this.getApplication().fireEvent("changecontentevent", Ext.widget("communication-overview", {router: a}));
        this.loadData()
    },
    loadData: function () {
        var c = this, b = c.getModel("Dsh.model.communication.Overview"), a = this.getController("Uni.controller.history.Router");
        b.setFilter(a.filter);
        c.getCommunicationOverview().setLoading();
        c.getCommunicationServers().reload();
        b.load(null, {
            success: function (d) {
                c.getSummary().setRecord(d.getSummary());
                c.getOverview().bindStore(d.overviews());
                c.getBreakdown().bindStore(d.breakdowns());
                if (d.raw.kpi) {
                    c.getKpi().setRecord(d.getKpi())
                }
                c.getHeader().down("#last-updated-field").setValue("Last updated at " + Uni.DateTime.formatTimeShort(new Date()))
            }, callback: function () {
                c.getCommunicationOverview().setLoading(false)
            }
        })
    },
    updateQuickLinks: function () {
        if (Uni.Auth.hasAnyPrivilege(["privilege.view.reports"])) {
            var e = this;
            var b = e.getHeader().down("#device-group");
            var f = b.groupName;
            var d = false;
            if (f && f.length) {
                d = encodeURIComponent(Ext.JSON.encode({GROUPNAME: f}))
            }
            var a = Ext.getStore("ReportInfos");
            if (a) {
                var c = a.getProxy();
                c.setExtraParam("category", "MDC");
                c.setExtraParam("subCategory", "Device Communication");
                a.load(function (h) {
                    var g = Ext.isArray(e.getQuickLinks().data) ? e.getQuickLinks().data : [];
                    Ext.each(h, function (j) {
                        var l = j.get("name");
                        var k = j.get("reportUUID");
                        g.push({
                            link: l,
                            href: "#/administration/generatereport?reportUUID=" + k + "&subCategory=Device%20Communication" + (d ? "&filter=" + d : "")
                        })
                    });
                    var i = e.getQuickLinks().down("#quicklinksTplPanel");
                    i.update(g)
                })
            }
        }
    }
});
Ext.define("Dsh.view.widget.CommunicationsList", {
    extend: "Ext.grid.Panel",
    alias: "widget.communications-list",
    store: "Dsh.store.CommunicationTasks",
    requires: ["Ext.grid.column.Date", "Ext.form.field.ComboBox", "Ext.grid.column.Template", "Uni.grid.column.Action", "Uni.view.toolbar.PagingTop", "Uni.view.toolbar.PagingBottom"],
    columns: {
        defaults: {sortable: false, menuDisabled: true},
        items: [{
            itemId: "name",
            text: Uni.I18n.translate("communication.widget.details.commmunication", "DSH", "Communication"),
            dataIndex: "name",
            flex: 2
        }, {
            itemId: "device",
            text: Uni.I18n.translate("communication.widget.details.device", "DSH", "Device"),
            dataIndex: "device",
            flex: 1,
            renderer: function (a) {
                return a.name ? a.name : ""
            }
        }, {
            itemId: "currentState",
            text: Uni.I18n.translate("communication.widget.details.currentState", "DSH", "Current state"),
            dataIndex: "currentState",
            flex: 1,
            renderer: function (a) {
                return a.displayValue ? a.displayValue : ""
            }
        }, {
            itemId: "latestResult",
            text: Uni.I18n.translate("connection.widget.details.latestResult", "DSH", "Latest result"),
            dataIndex: "latestResult",
            flex: 1,
            renderer: function (a) {
                return a.displayValue ? a.displayValue : ""
            }
        }, {
            itemId: "nextCommunication",
            text: Uni.I18n.translate("communication.widget.details.nextCommunication", "DSH", "Next communication"),
            dataIndex: "nextCommunication",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeShort(a) : ""
            },
            flex: 2
        }, {
            itemId: "startTime",
            text: Uni.I18n.translate("communication.widget.details.startedOn", "DSH", "Started on"),
            dataIndex: "startTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeShort(a) : ""
            },
            flex: 2
        }, {
            itemId: "successfulFinishTime",
            text: Uni.I18n.translate("communication.widget.details.finishedOn", "DSH", "Finished successfully on"),
            dataIndex: "successfulFinishTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeShort(a) : ""
            },
            flex: 2
        }, {itemId: "communicationsGridActionMenu", xtype: "uni-actioncolumn", menu: {}}]
    },
    initComponent: function () {
        var a = this;
        a.dockedItems = [{
            itemId: "pagingtoolbartop",
            xtype: "pagingtoolbartop",
            dock: "top",
            store: a.store,
            displayMsg: Uni.I18n.translate("communication.widget.details.displayMsg", "DDSH", "{0} - {1} of {2} communications"),
            displayMoreMsg: Uni.I18n.translate("communication.widget.details.displayMoreMsg", "DSH", "{0} - {1} of more than {2} communications"),
            emptyMsg: Uni.I18n.translate("communication.widget.details.emptyMsg", "DSH", "There are no communications to display"),
            items: [{
                xtype: "button",
                itemId: "generate-report",
                hidden: !Uni.Auth.hasAnyPrivilege(["privilege.view.reports"]),
                text: Uni.I18n.translate("generatereport.generateReportButton", "DSH", "Generate report")
            }]
        }, {
            itemId: "pagingtoolbarbottom",
            xtype: "pagingtoolbarbottom",
            store: a.store,
            dock: "bottom",
            deferLoading: true,
            itemsPerPageMsg: Uni.I18n.translate("communication.widget.details.itemsPerPage", "DSH", "Communications per page")
        }];
        a.callParent(arguments)
    }
});
Ext.define("Dsh.view.widget.PreviewCommunication", {
    extend: "Ext.form.Panel",
    alias: "widget.preview_communication",
    title: "",
    frame: true,
    layout: {type: "column"},
    tools: [{
        xtype: "button",
        text: Uni.I18n.translate("general.actions", "ISE", "Actions"),
        iconCls: "x-uni-action-iconD",
        itemId: "communicationPreviewActionMenu",
        menu: {}
    }],
    items: [{
        columnWidth: 0.5,
        defaults: {xtype: "displayfield", labelWidth: 200},
        items: [{
            fieldLabel: Uni.I18n.translate("communication.widget.details.commTaskName", "DSH", "Name"),
            name: "name"
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.commTasks", "DSH", "Communication task(s)"),
            name: "comTasks",
            renderer: function (b) {
                if (b !== "") {
                    var a = "";
                    Ext.each(b, function (c) {
                        a = a + "<li>" + c.name + "</li>"
                    });
                    return a
                } else {
                    return ""
                }
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.device", "DSH", "Device"),
            name: "device",
            renderer: function (b) {
                var a = "";
                if (b) {
                    Uni.Auth.hasAnyPrivilege(["privilege.view.device", "privilege.administrate.deviceData"]) ? a = '<a href="#/devices/' + b.id + '">' + b.name + "</a>" : a = b.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.deviceType", "DSH", "Device type"),
            name: "deviceType",
            renderer: function (b) {
                var a = "";
                if (b) {
                    Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceType", "privilege.view.deviceType"]) ? a = '<a href="#/administration/devicetypes/' + b.id + '">' + b.name + "</a>" : a = b.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.deviceConfig", "DSH", "Device configuration"),
            name: "devConfig",
            renderer: function (b) {
                var a = "";
                b && (a = '<a href="#/administration/devicetypes/' + b.devType.id + "/deviceconfigurations/" + b.config.id + '">' + b.config.name + "</a>");
                if (a !== "" && !Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceType", "privilege.view.deviceType"])) {
                    a = b.config.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.frequency", "DSH", "Frequency"),
            name: "comScheduleFrequency",
            renderer: function (b) {
                var a = "";
                if (b) {
                    a = Uni.I18n.translate("communication.widget.details.every", "DSH", "Every") + " " + b.every.count + " " + b.every.timeUnit
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.urgency", "DSH", "Urgency"),
            name: "urgency"
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.executeOnInbound", "DSH", "Always execute on inbound"),
            name: "alwaysExecuteOnInbound",
            renderer: function (a) {
                if (!_.isUndefined(a)) {
                    return a ? "Yes" : "No"
                } else {
                    return ""
                }
            }
        }]
    }, {
        columnWidth: 0.5,
        defaults: {xtype: "displayfield", labelWidth: 200},
        items: [{
            fieldLabel: Uni.I18n.translate("communication.widget.details.currentState", "DSH", "Current state"),
            name: "currentState",
            renderer: function (a) {
                return a.displayValue ? a.displayValue : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.latestResult", "DSH", "Latest result"),
            name: "latestResult",
            renderer: function (a) {
                return a.displayValue ? a.displayValue : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.startedOn", "DSH", "Started on"),
            name: "startTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.finishedOn", "DSH", "Finished successfully on"),
            name: "successfulFinishTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("communication.widget.details.nextComm", "DSH", "Next communication"),
            name: "nextCommunication",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }]
    }],
    initComponent: function () {
        var a = this;
        a.callParent(arguments)
    }
});
Ext.define("Dsh.view.widget.common.SideFilterCombo", {
    extend: "Ext.form.field.ComboBox",
    alias: "widget.side-filter-combo",
    editable: false,
    multiSelect: true,
    queryMode: "local",
    triggerAction: "all",
    initComponent: function () {
        var a = this;
        a.listConfig = {
            getInnerTpl: function () {
                return '<div class="x-combo-list-item"><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" /> {' + a.displayField + "}</div>"
            }
        };
        a.callParent(arguments);
        a.store.load({
            callback: function () {
                a.select(a.getValue());
                a.fireEvent("updateTopFilterPanelTagButtons", a)
            }
        })
    },
    getValue: function () {
        var a = this;
        a.callParent(arguments);
        if (_.isArray(a.value)) {
            a.value = _.compact(a.value)
        }
        return a.value
    }
});
Ext.define("Dsh.view.widget.common.DateTimeField", {
    extend: "Ext.form.FieldSet",
    alias: "widget.datetime-field",
    layout: {type: "vbox", align: "stretch"},
    defaults: {labelWidth: 30, labelAlign: "left", labelStyle: "font-weight: normal"},
    items: [{
        xtype: "datefield",
        name: "date",
        editable: false,
        format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
    }, {
        xtype: "fieldcontainer",
        fieldLabel: "&nbsp;",
        style: "padding-left: 30px",
        layout: "hbox",
        defaultType: "numberfield",
        defaults: {
            flex: 1,
            value: 0,
            minValue: 0,
            enforceMaxLength: true,
            maxLength: 2,
            enableKeyEvents: true,
            stripCharsRe: /\D/,
            listeners: {
                blur: function (a) {
                    if (Ext.isEmpty(a.getValue())) {
                        a.setValue(0)
                    }
                }
            }
        },
        items: [{
            name: "hours", maxValue: 23, style: {marginRight: "5px"}, valueToRaw: function (a) {
                return a < 10 ? Ext.String.leftPad(a, 2, "0") : a > 23 ? 23 : a
            }
        }, {
            name: "minutes", maxValue: 59, style: {marginLeft: "5px"}, valueToRaw: function (a) {
                return a < 10 ? Ext.String.leftPad(a, 2, "0") : a > 59 ? 59 : a
            }
        }]
    }],
    initComponent: function () {
        this.callParent(arguments);
        this.down("datefield").setFieldLabel(this.label)
    }
});
Ext.define("Dsh.view.widget.common.SideFilterDateTime", {
    extend: "Ext.form.FieldSet",
    alias: "widget.side-filter-date-time",
    requires: ["Dsh.view.widget.common.DateTimeField"],
    layout: {type: "vbox", align: "stretch"},
    style: {border: "none", padding: 0, margin: 0},
    defaults: {xtype: "datetime-field", style: {border: "none", padding: 0, margin: 0}},
    items: [{
        xtype: "panel",
        name: "header",
        baseCls: "x-form-item-label",
        style: "margin: 15px 0"
    }, {
        label: Uni.I18n.translate("connection.widget.sideFilter.from", "DSH", "From"),
        name: "from"
    }, {label: Uni.I18n.translate("connection.widget.sideFilter.to", "DSH", "To"), name: "to"}],
    initComponent: function () {
        this.callParent(arguments);
        this.down("panel[name=header]").update(this.wTitle)
    }
});
Ext.define("Dsh.util.FilterHydrator", {
    extract: function (b) {
        var c = {}, a = b.startedBetween, d = b.finishedBetween;
        Ext.merge(c, b.getData());
        if (!_.isEmpty(a)) {
            c.startedBetween = {};
            if (a.get("from")) {
                c.startedBetween.from = {
                    date: a.get("from"),
                    hours: a.get("from").getHours(),
                    minutes: a.get("from").getMinutes()
                }
            }
            if (a.get("to")) {
                c.startedBetween.to = {
                    date: a.get("to"),
                    hours: a.get("to").getHours(),
                    minutes: a.get("to").getMinutes()
                }
            }
        }
        if (!_.isEmpty(d)) {
            c.finishedBetween = {};
            if (d.get("from")) {
                c.finishedBetween.from = {
                    date: d.get("from"),
                    hours: d.get("from").getHours(),
                    minutes: d.get("from").getMinutes()
                }
            }
            if (d.get("to")) {
                c.finishedBetween.to = {
                    date: d.get("to"),
                    hours: d.get("to").getHours(),
                    minutes: d.get("to").getMinutes()
                }
            }
        }
        return c
    }, hydrate: function (g, c) {
        var b = g.startedBetween, h = g.finishedBetween, a = this.parseDate(b.from.date, b.from.hours, b.from.minutes), f = this.parseDate(b.to.date, b.to.hours, b.to.minutes), e = this.parseDate(h.from.date, h.from.hours, h.from.minutes), d = this.parseDate(h.to.date, h.to.hours, h.to.minutes);
        delete g.startedBetween;
        delete g.finishedBetween;
        c.set(g);
        c.setStartedBetween(Ext.create("Dsh.model.DateRange", {from: a, to: f}));
        c.setFinishedBetween(Ext.create("Dsh.model.DateRange", {from: e, to: d}))
    }, parseDate: function (b, a, c) {
        return new Date(new Date(b).getTime() + a * 3600000 + c * 60000) || null
    }
});
Ext.define("Dsh.view.widget.CommunicationSideFilter", {
    extend: "Ext.panel.Panel",
    alias: "widget.dsh-comm-side-filter",
    requires: ["Uni.component.filter.view.Filter", "Dsh.view.widget.common.SideFilterCombo", "Dsh.view.widget.common.SideFilterDateTime", "Dsh.util.FilterHydrator"],
    cls: "filter-form",
    width: 250,
    title: Uni.I18n.translate("connection.widget.sideFilter.title", "DSH", "Filter"),
    ui: "medium",
    items: [{
        xtype: "nested-form",
        itemId: "filter-form",
        hydrator: "Dsh.util.FilterHydrator",
        ui: "filter",
        layout: {type: "vbox", align: "stretch"},
        defaults: {xtype: "side-filter-combo", labelAlign: "top"},
        items: [{
            itemId: "device-group",
            name: "deviceGroup",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.deviceGroup", "DSH", "Device group"),
            displayField: "name",
            valueField: "id",
            store: "Dsh.store.filter.DeviceGroup"
        }, {
            itemId: "current-state",
            name: "currentStates",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.currentState", "DSH", "Current state"),
            displayField: "localizedValue",
            valueField: "taskStatus",
            store: "Dsh.store.filter.CurrentState"
        }, {
            itemId: "latest-result",
            name: "latestResults",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.latestResult", "DSH", "Latest result"),
            displayField: "localizedValue",
            valueField: "completionCode",
            store: "Dsh.store.filter.CompletionCodes"
        }, {
            itemId: "communication-task",
            name: "comTasks",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.commTask", "DSH", "Communication task"),
            displayField: "name",
            valueField: "id",
            store: "Dsh.store.filter.CommunicationTask"
        }, {
            itemId: "communication-schedule",
            name: "comSchedules",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.communicationSchedule", "DSH", "Communication schedule"),
            displayField: "name",
            valueField: "id",
            store: "Dsh.store.filter.CommunicationSchedule"
        }, {
            itemId: "device-type",
            name: "deviceTypes",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.deviceType", "DSH", "Device type"),
            displayField: "name",
            valueField: "id",
            store: "Dsh.store.filter.DeviceType"
        }, {
            xtype: "side-filter-date-time",
            itemId: "started-between",
            name: "startedBetween",
            wTitle: Uni.I18n.translate("connection.widget.sideFilter.startedBetween", "DSH", "Started between")
        }, {
            xtype: "side-filter-date-time",
            itemId: "finished-between",
            name: "finishedBetween",
            wTitle: Uni.I18n.translate("connection.widget.sideFilter.finishedBetween", "DSH", "Finished successfully between")
        }],
        dockedItems: [{
            xtype: "toolbar",
            dock: "bottom",
            items: [{
                text: Uni.I18n.translate("connection.widget.sideFilter.apply", "DSH", "Apply"),
                ui: "action",
                action: "applyfilter",
                itemId: "btn-apply-filter"
            }, {
                text: Uni.I18n.translate("connection.widget.sideFilter.clearAll", "DSH", "Clear all"),
                action: "clearfilter",
                itemId: "btn-clear-filter"
            }]
        }]
    }]
});
Ext.define("Dsh.view.widget.ConnectionActionMenu", {
    extend: "Ext.menu.Menu",
    alias: "widget.connection-action-menu",
    items: [{
        text: "Run now",
        hidden: Uni.Auth.hasNoPrivilege("privilege.operate.deviceCommunication"),
        action: "run"
    }, {text: "View history", action: "viewHistory"}, {text: "View log", action: "viewLog"}]
});
Ext.define("Dsh.view.widget.PreviewConnection", {
    extend: "Ext.form.Panel",
    alias: "widget.preview_connection",
    title: "",
    frame: true,
    layout: {type: "column"},
    tools: [{
        xtype: "button",
        itemId: "connectionsPreviewActionBtn",
        text: Uni.I18n.translate("general.actions", "ISE", "Actions"),
        iconCls: "x-uni-action-iconD",
        menu: {xtype: "connection-action-menu", itemId: "connectionsActionMenu"}
    }],
    items: [{
        columnWidth: 0.5,
        defaults: {xtype: "displayfield", labelWidth: 200},
        items: [{
            fieldLabel: Uni.I18n.translate("connection.widget.details.device", "DSH", "Device"),
            name: "device",
            renderer: function (b) {
                var a = "";
                if (b) {
                    Uni.Auth.hasAnyPrivilege(["privilege.view.device", "privilege.administrate.deviceData"]) ? a = '<a href="#/devices/' + b.id + '">' + b.name + "</a>" : a = b.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.deviceType", "DSH", "Device type"),
            name: "deviceType",
            renderer: function (b) {
                var a = "";
                if (b) {
                    Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceType", "privilege.view.deviceType"]) ? a = '<a href="#/administration/devicetypes/' + b.id + '">' + b.name + "</a>" : a = b.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.deviceConfig", "DSH", "Device configuration"),
            name: "devConfig",
            renderer: function (b) {
                var a = "";
                b && (a = '<a href="#/administration/devicetypes/' + b.devType.id + "/deviceconfigurations/" + b.config.id + '">' + b.config.name + "</a>");
                if (a !== "" && !Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceType", "privilege.view.deviceType"])) {
                    a = b.config.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.connMethod", "DSH", "Connection method"),
            name: "connectionMethod",
            renderer: function (a) {
                return a ? a.name : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.connType", "DSH", "Connection type"),
            name: "connectionType"
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.direction", "DSH", "Direction"),
            name: "direction"
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.connWindow", "DSH", "Connection window"),
            name: "window"
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.strategy", "DSH", "Strategy"),
            name: "connectionStrategy",
            renderer: function (a) {
                return a ? a.displayValue : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.commPortPool", "DSH", "Communication port pool"),
            name: "comPortPool",
            renderer: function (a) {
                return a ? a.name : ""
            }
        }]
    }, {
        columnWidth: 0.5,
        defaults: {xtype: "displayfield", labelWidth: 200},
        items: [{
            fieldLabel: Uni.I18n.translate("connection.widget.details.currentState", "DSH", "Current state"),
            name: "currentState",
            renderer: function (a) {
                return a ? a.displayValue : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.latestStatus", "DSH", "Latest status"),
            name: "latestStatus",
            renderer: function (a) {
                return a ? a.displayValue : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.latestResult", "DSH", "Latest result"),
            name: "latestResult",
            renderer: function (a) {
                return a ? a.displayValue : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.commTasks", "DSH", "Communication tasks"),
            name: "taskCount",
            height: 60,
            cls: "communication-tasks-status",
            renderer: function (d) {
                var a = d.numberOfFailedTasks ? d.numberOfFailedTasks : 0, c = d.numberOfSuccessfulTasks ? d.numberOfSuccessfulTasks : 0, b = d.numberOfIncompleteTasks ? d.numberOfIncompleteTasks : 0;
                if (a === 0 && c === 0 && b === 0) {
                    return ""
                } else {
                    return '<tpl><span class="icon-checkmark"></span>' + c + '<br></tpl><tpl><span class="icon-close"></span>' + a + '<br></tpl><tpl><span class="icon-stop2"></span>' + b + "</tpl>"
                }
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.startedOn", "DSH", "Started on"),
            name: "startDateTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.finishedOn", "DSH", "Finished on"),
            name: "endDateTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.duration", "DSH", "Duration"),
            name: "duration",
            renderer: function (a) {
                return a ? a.count + " " + a.timeUnit : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.commPort", "DSH", "Communication port"),
            name: "comPort",
            renderer: function (a) {
                return a ? a.name : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.widget.details.nextConnection", "DSH", "Next connection"),
            name: "nextExecution",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }]
    }],
    initComponent: function () {
        var a = this;
        a.callParent(arguments)
    }
});
Ext.define("Dsh.model.CommunicationTask", {
    extend: "Ext.data.Model",
    fields: ["name", "device", "id", "deviceConfiguration", "deviceType", "comScheduleName", "comScheduleFrequency", "urgency", "currentState", "alwaysExecuteOnInbound", "latestResult", "connectionTask", "sessionId", "comTasks", {
        name: "startTime",
        type: "date",
        dateFormat: "time"
    }, {name: "successfulFinishTime", type: "date", dateFormat: "time"}, {
        name: "nextCommunication",
        type: "date",
        dateFormat: "time"
    }, {
        name: "title", persist: false, mapping: function (a) {
            return a.name + " on " + a.device.name
        }
    }, {
        name: "devConfig", persist: false, mapping: function (a) {
            var b = {};
            b.config = a.deviceConfiguration;
            b.devType = a.deviceType;
            return b
        }
    }],
    hasOne: {
        model: "Dsh.model.ConnectionTask",
        associationKey: "connectionTask",
        name: "connectionTask",
        getterName: "getConnectionTask"
    },
    run: function (a) {
        Ext.Ajax.request({method: "PUT", url: this.proxy.url + "/{id}/run".replace("{id}", this.getId()), success: a})
    },
    runNow: function (a) {
        Ext.Ajax.request({
            method: "PUT",
            url: this.proxy.url + "/{id}/runnow".replace("{id}", this.getId()),
            success: a
        })
    },
    proxy: {
        type: "ajax",
        url: "/api/dsr/communications",
        reader: {type: "json", root: "communicationTasks", totalProperty: "total"}
    }
});
Ext.define("Dsh.util.FilterStoreHydrator", {
    extract: function (b) {
        var c = b.getData();
        c.deviceGroups = c.deviceGroup;
        delete c.deviceGroup;
        _.map(c, function (f, e) {
            if (f) {
                if (!_.isArray(f)) {
                    c[e] = [f]
                }
            }
            return f
        });
        if (b.startedBetween) {
            var d = b.getStartedBetween();
            if (d.get("from")) {
                c.startIntervalFrom = d.get("from").getTime()
            }
            if (d.get("to")) {
                c.startIntervalTo = d.get("to").getTime()
            }
        }
        if (b.finishedBetween) {
            var a = b.getFinishedBetween();
            if (a.get("from")) {
                c.finishIntervalFrom = a.get("from").getTime()
            }
            if (a.get("to")) {
                c.finishIntervalTo = a.get("to").getTime()
            }
        }
        return c
    }
});
Ext.define("Dsh.store.CommunicationTasks", {
    extend: "Uni.data.store.Filterable",
    requires: ["Dsh.model.CommunicationTask", "Dsh.util.FilterStoreHydrator"],
    model: "Dsh.model.CommunicationTask",
    hydrator: "Dsh.util.FilterStoreHydrator",
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: "ajax",
        url: "/api/dsr/communications",
        reader: {type: "json", root: "communicationTasks", totalProperty: "total"}
    }
});
Ext.define("Dsh.view.Communications", {
    extend: "Uni.view.container.ContentContainer",
    alias: "widget.communications-details",
    itemId: "communicationsdetails",
    requires: ["Dsh.view.widget.CommunicationsList", "Dsh.view.widget.PreviewCommunication", "Dsh.view.widget.CommunicationSideFilter", "Dsh.view.widget.PreviewConnection", "Dsh.store.CommunicationTasks"],
    content: [{
        xtype: "panel",
        ui: "large",
        title: Uni.I18n.translate("workspace.dataCommunication.communication.title", "DSH", "Communications")
    }, {xtype: "filter-top-panel", itemId: "dshcommunicationsfilterpanel"}, {
        xtype: "preview-container",
        grid: {xtype: "communications-list", itemId: "communicationslist"},
        emptyComponent: {
            xtype: "no-items-found-panel",
            title: Uni.I18n.translate("workspace.dataCommunication.communication.empty.title", "DSH", "No communications found"),
            reasons: [Uni.I18n.translate("workspace.dataCommunication.communication.empty.list.item1", "DSH", "No communications in the system."), Uni.I18n.translate("workspace.dataCommunication.communication.empty.list.item2", "DSH", "No communications found due to applied filters.")]
        },
        previewComponent: {
            hidden: true,
            items: [{xtype: "preview_communication", itemId: "communicationdetails"}, {
                style: {"margin-top": "32px"},
                xtype: "preview_connection",
                itemId: "connectiondetails"
            }]
        }
    }],
    side: [{xtype: "dsh-comm-side-filter", itemId: "dshcommunicationssidefilter"}],
    initComponent: function () {
        this.callParent(arguments)
    }
});
Ext.define("Dsh.model.ConnectionTask", {
    extend: "Ext.data.Model",
    fields: [{name: "id", type: "auto"}, {name: "device", type: "auto"}, {
        name: "deviceConfiguration",
        type: "auto"
    }, {name: "deviceType", type: "auto"}, {
        name: "devConfig", persist: false, mapping: function (a) {
            var b = {};
            b.config = a.deviceConfiguration;
            b.devType = a.deviceType;
            return b
        }
    }, {
        name: "title", persist: false, mapping: function (a) {
            return a.connectionMethod.name + " on " + a.device.name
        }
    }, {name: "currentState", type: "auto"}, {name: "latestStatus", type: "auto"}, {
        name: "latestResult",
        type: "auto"
    }, {name: "taskCount", type: "auto"}, {
        name: "startDateTime",
        type: "date",
        dateFormat: "time"
    }, {name: "endDateTime", type: "date", dateFormat: "time"}, {name: "duration", type: "auto"}, {
        name: "comPortPool",
        type: "auto"
    }, {name: "direction", type: "auto"}, {name: "connectionType", type: "auto"}, {
        name: "comServer",
        type: "auto"
    }, {name: "connectionMethod", type: "auto"}, {name: "window", type: "auto"}, {
        name: "connectionStrategy",
        type: "auto"
    }, {name: "nextExecution", type: "date", dateFormat: "time"}, {
        name: "comPort",
        type: "auto"
    }, {name: "comSessionId", type: "auto"}],
    run: function (a) {
        Ext.Ajax.request({method: "PUT", url: this.proxy.url + "/{id}/run".replace("{id}", this.getId()), success: a})
    },
    proxy: {
        type: "rest",
        url: "/api/dsr/connections",
        reader: {type: "json", root: "connectionTasks", totalProperty: "total"}
    }
});
Ext.define("Dsh.model.CommTasks", {
    extend: "Ext.data.Model",
    fields: [{name: "count", type: "auto"}, "communicationTasks"],
    hasMany: [{model: "Dsh.model.CommunicationTask", name: "communicationsTasks"}]
});
Ext.define("Dsh.model.DateRange", {
    extend: "Ext.data.Model",
    proxy: "memory",
    fields: [{name: "from", type: "date", dateFormat: "Y-m-dTH:i:s"}, {
        name: "to",
        type: "date",
        dateFormat: "Y-m-dTH:i:s"
    }]
});
Ext.define("Dsh.model.Filter", {
    extend: "Ext.data.Model",
    requires: ["Uni.data.proxy.QueryStringProxy", "Dsh.model.DateRange"],
    proxy: {type: "querystring", root: "filter"},
    fields: [{name: "currentStates", type: "auto"}, {name: "latestStates", type: "auto"}, {
        name: "latestResults",
        type: "auto"
    }, {name: "comPortPools", type: "auto"}, {name: "comSchedules", type: "auto"}, {
        name: "comTasks",
        type: "auto"
    }, {name: "connectionTypes", type: "auto"}, {name: "deviceGroup", type: "auto"}, {
        name: "deviceTypes",
        type: "auto"
    }],
    associations: [{
        type: "hasOne",
        model: "Dsh.model.DateRange",
        name: "startedBetween",
        instanceName: "startedBetween",
        associationKey: "startedBetween",
        getterName: "getStartedBetween",
        setterName: "setStartedBetween"
    }, {
        type: "hasOne",
        model: "Dsh.model.DateRange",
        name: "finishedBetween",
        instanceName: "finishedBetween",
        associationKey: "finishedBetween",
        getterName: "getFinishedBetween",
        setterName: "setFinishedBetween"
    }]
});
Ext.define("Dsh.store.filter.CommunicationSchedule", {
    extend: "Ext.data.Store",
    fields: ["id", "name"],
    autoLoad: false,
    proxy: {type: "rest", url: "/api/dsr/field/comschedules", reader: {type: "json", root: "comSchedules"}}
});
Ext.define("Dsh.store.filter.CommunicationTask", {
    extend: "Ext.data.Store",
    fields: ["id", "name"],
    autoLoad: false,
    proxy: {type: "rest", url: "/api/dsr/field/comtasks", reader: {type: "json", root: "comTasks"}}
});
Ext.define("Dsh.store.filter.CurrentState", {
    extend: "Ext.data.Store",
    fields: ["localizedValue", "taskStatus"],
    autoLoad: false,
    proxy: {type: "rest", url: "/api/dsr/field/taskstatus", reader: {type: "json", root: "taskStatuses"}}
});
Ext.define("Dsh.store.filter.LatestResult", {
    extend: "Ext.data.Store",
    fields: ["localizedValue", "successIndicator"],
    autoLoad: false,
    proxy: {
        type: "rest",
        url: "/api/dsr/field/comsessionsuccessindicators",
        reader: {type: "json", root: "successIndicators"}
    }
});
Ext.define("Dsh.store.filter.ConnectionType", {
    extend: "Ext.data.Store",
    fields: ["id", "name"],
    autoLoad: false,
    proxy: {
        type: "rest",
        url: "/api/dsr/field/connectiontypepluggableclasses",
        reader: {type: "json", root: "connectiontypepluggableclasses"}
    }
});
Ext.define("Dsh.store.filter.DeviceType", {
    extend: "Ext.data.Store",
    fields: ["id", "name"],
    autoLoad: false,
    proxy: {type: "rest", url: "/api/dsr/field/devicetypes", reader: {type: "json", root: "deviceTypes"}}
});
Ext.define("Dsh.controller.Communications", {
    extend: "Dsh.controller.BaseController",
    models: ["Dsh.model.ConnectionTask", "Dsh.model.CommTasks", "Dsh.model.CommunicationTask", "Dsh.model.Filter"],
    views: ["Dsh.view.Communications", "Dsh.view.widget.PreviewCommunication", "Dsh.view.widget.PreviewConnection"],
    stores: ["Dsh.store.CommunicationTasks", "Dsh.store.filter.CommunicationSchedule", "Dsh.store.filter.CommunicationTask", "Dsh.store.filter.CurrentState", "Dsh.store.filter.LatestResult", "Dsh.store.filter.ConnectionType", "Dsh.store.filter.DeviceType"],
    refs: [{
        ref: "communicationPreview",
        selector: "#communicationsdetails #communicationdetails"
    }, {ref: "connectionPreview", selector: "#communicationsdetails #connectiondetails"}, {
        ref: "filterPanel",
        selector: "#communicationsdetails filter-top-panel"
    }, {ref: "sideFilterForm", selector: "#communicationsdetails #filter-form"}, {
        ref: "communicationsGrid",
        selector: "communications-list"
    }, {
        ref: "communicationsGridActionMenu",
        selector: "#communicationsGridActionMenu"
    }, {
        ref: "communicationPreviewActionMenu",
        selector: "#communicationPreviewActionMenu"
    }, {ref: "connectionsPreviewActionBtn", selector: "#connectionsPreviewActionBtn"}],
    prefix: "#communicationsdetails",
    init: function () {
        this.control({
            "#communicationsdetails #communicationslist": {selectionchange: this.onSelectionChange},
            "communications-list #generate-report": {click: this.onGenerateReport}
        });
        this.callParent(arguments)
    },
    showOverview: function () {
        var b = Ext.widget("communications-details"), a = this.getStore("Dsh.store.CommunicationTasks");
        this.getApplication().fireEvent("changecontentevent", b);
        this.initFilter();
        a.load()
    },
    initMenu: function (a, b, c) {
        this.getCommunicationsGridActionMenu().menu.removeAll();
        this.getCommunicationPreviewActionMenu().menu.removeAll();
        this.getConnectionsPreviewActionBtn().menu.removeAll();
        Ext.suspendLayouts();
        Ext.each(a.get("comTasks"), function (e) {
            if (a.get("sessionId") !== 0) {
                b.push({
                    text: Ext.String.format(Uni.I18n.translate("connection.widget.details.menuItem", "MDC", "View '{0}' log"), e.name),
                    action: {
                        action: "viewlog",
                        comTask: {mRID: a.get("device").id, sessionId: a.get("sessionId"), comTaskId: e.id}
                    },
                    listeners: {click: c.viewCommunicationLog}
                })
            }
        });
        if (a.get("connectionTask").connectionStrategy && a.get("connectionTask").connectionStrategy.id) {
            if (a.get("connectionTask").connectionStrategy.id === "minimizeConnections") {
                b.push({
                    text: Uni.I18n.translate("connection.widget.details.menuItem.run", "MDC", "Run"),
                    action: {action: "run", record: a, me: c},
                    listeners: {click: c.communicationRun}
                })
            }
            b.push({
                text: Uni.I18n.translate("connection.widget.details.menuItem.runNow", "MDC", "Run now"),
                action: {action: "runNow", record: a, me: c},
                listeners: {click: c.communicationRunNow}
            })
        }
        var d = {
            text: Uni.I18n.translate("connection.widget.details.connectionMenuItem", "MDC", "View connection log"),
            action: {
                action: "viewlog",
                connection: {
                    mRID: a.get("device").id,
                    connectionMethodId: a.get("connectionTask").id,
                    sessionId: a.get("connectionTask").comSessionId
                }
            },
            listeners: {click: c.viewConnectionLog}
        };
        this.getCommunicationsGridActionMenu().menu.add(b);
        this.getCommunicationPreviewActionMenu().menu.add(b);
        if (a.get("connectionTask").comSessionId !== 0) {
            this.getConnectionsPreviewActionBtn().menu.add(d)
        }
        Ext.resumeLayouts(true)
    },
    onSelectionChange: function (d, f) {
        var g = this, h = g.getCommunicationPreview(), b = g.getConnectionPreview(), a = f[0], c = [];
        if (a) {
            this.initMenu(a, c, g);
            h.loadRecord(a);
            h.setTitle(a.get("name") + " on " + a.get("device").name);
            if (a.getData().connectionTask) {
                var e = a.getConnectionTask();
                b.setTitle(e.get("connectionMethod").name + " on " + e.get("device").name);
                b.show();
                b.loadRecord(e)
            } else {
                b.hide()
            }
        }
    },
    viewCommunicationLog: function (a) {
        location.href = "#/devices/" + a.action.comTask.mRID + "/communicationtasks/" + a.action.comTask.comTaskId + "/history/" + a.action.comTask.sessionId + "/viewlog?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D"
    },
    viewConnectionLog: function (a) {
        location.href = "#/devices/" + a.action.connection.mRID + "/connectionmethods/" + a.action.connection.connectionMethodId + "/history/" + a.action.connection.sessionId + "/viewlog?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D"
    },
    onGenerateReport: function () {
        var c = this;
        var b = this.getController("Uni.controller.history.Router");
        var d = {};
        d.deviceGroup = "GROUPNAME";
        d.currentStates = "STATUS";
        d.latestResults = null;
        d.comSchedules = "SCHEDULENAME";
        d.deviceTypes = null;
        d.comTasks = "COMTASKNAME";
        var g = false;
        var a = c.getSideFilterForm().getForm().getFields();
        a.each(function (j) {
            g = g || {};
            var h = d[j.getName()];
            if (h) {
                var i = j.getRawValue();
                if (j.getXType() == "side-filter-combo") {
                    i = Ext.isString(i) && i.split(", ") || i;
                    i = _.isArray(i) && _.compact(i) || i
                }
            }
            g[h] = i
        });
        if (b.filter && b.filter.startedBetween) {
            var f = b.filter.startedBetween.get("from");
            var e = b.filter.startedBetween.get("to");
            g.CONNECTIONDATE = {
                from: f && Ext.Date.format(f, "Y-m-d H:i:s"),
                to: e && Ext.Date.format(e, "Y-m-d H:i:s")
            }
        }
        b.getRoute("generatereport").forward(null, {category: "MDC", subCategory: "Device Communication", filter: g})
    },
    communicationRun: function (c) {
        var b = c.action.me;
        var a = c.action.record;
        a.run(function () {
            b.getApplication().fireEvent("acknowledge", Uni.I18n.translate("device.communication.run.wait", "MDC", "Run succeeded"));
            a.set("plannedDate", new Date());
            b.showOverview()
        })
    },
    communicationRunNow: function (c) {
        var b = c.action.me;
        var a = c.action.record;
        a.run(function () {
            b.getApplication().fireEvent("acknowledge", Uni.I18n.translate("device.communication.run.now", "MDC", "Run now succeeded"));
            a.set("plannedDate", new Date());
            b.showOverview()
        })
    }
});
Ext.define("Dsh.view.ConnectionOverview", {
    extend: "Ext.container.Container",
    requires: ["Dsh.view.widget.HeaderSection", "Dsh.view.widget.Summary", "Dsh.view.widget.CommunicationServers", "Dsh.view.widget.QuickLinks", "Dsh.view.widget.ReadOutsOverTime", "Dsh.view.widget.Overview", "Dsh.view.widget.Breakdown"],
    alias: "widget.connection-overview",
    itemId: "connection-overview",
    autoScroll: true,
    layout: {type: "vbox", align: "stretch"},
    style: {padding: "0 20px"},
    defaults: {style: {marginBottom: "20px", padding: 0}},
    initComponent: function () {
        var a = this;
        a.items = [{xtype: "header-section", router: a.router, style: "none"}, {
            xtype: "panel",
            layout: {type: "hbox", align: "stretch"},
            defaults: {style: {marginRight: "20px", padding: "20px"}, flex: 1},
            items: [{
                xtype: "summary",
                flex: 2,
                wTitle: Uni.I18n.translate("connection.widget.summary.title", "DSH", "Connections summary"),
                router: a.router,
                parent: "connections"
            }, {
                xtype: "communication-servers",
                itemId: "communication-servers",
                router: a.router
            }, {
                xtype: "quick-links",
                itemId: "quick-links",
                maxHeight: 256,
                overflowY: "auto",
                style: {marginRight: "0", padding: "20px"},
                data: [{
                    link: Uni.I18n.translate("connection.widget.quicklinks.viewAll", "DSH", "View all connections"),
                    href: a.router.getRoute("workspace/connections/details").buildUrl(null, a.router.queryParams)
                }, {
                    link: a.router.getRoute("workspace/communications").title,
                    href: a.router.getRoute("workspace/communications").buildUrl(null, a.router.queryParams)
                }, {
                    link: Uni.I18n.translate("communication.widget.quicklinks.myIssues", "DSH", "My open issues"),
                    href: typeof a.router.getRoute("workspace/datacollectionissues") !== "undefined" ? a.router.getRoute("workspace/datacollectionissues").buildUrl(null, a.router.queryParams) + "?myopenissues=true" : null
                }]
            }]
        }, {
            xtype: "read-outs-over-time",
            wTitle: Uni.I18n.translate("connection.widget.readOutsOverTime.title", "DSH", "Connections over time"),
            yLabel: Uni.I18n.translate("connection.widget.readOutsOverTime.yLabel", "DSH", "Number of connections"),
            router: a.router,
            parent: "connections"
        }, {xtype: "overview", category: "Connection", parent: "connections", router: a.router}, {
            xtype: "breakdown",
            parent: "connections",
            router: a.router
        }, {
            xtype: "heat-map",
            itemId: "heatmap",
            store: "Dsh.store.ConnectionResultsStore",
            router: a.router,
            parent: "connections"
        }];
        this.callParent(arguments)
    }
});
Ext.define("Dsh.model.connection.Overview", {
    extend: "Dsh.model.Filterable",
    requires: ["Dsh.model.Summary", "Dsh.model.Counter", "Dsh.model.Breakdown", "Dsh.model.Kpi"],
    hasOne: [{
        model: "Dsh.model.Summary",
        associationKey: "connectionSummary",
        name: "summary",
        getterName: "getSummary",
        setterName: "setSummary"
    }, {model: "Dsh.model.Kpi", associationKey: "kpi", name: "kpi", getterName: "getKpi", setterName: "setKpi"}],
    hasMany: [{model: "Dsh.model.Counter", name: "overviews"}, {model: "Dsh.model.Breakdown", name: "breakdowns"}],
    proxy: {type: "ajax", url: "/api/dsr/connectionoverview"}
});
Ext.define("Dsh.model.Combine", {
    extend: "Ext.data.Model",
    fields: [{name: "localizedValue", type: "string"}, {name: "breakdown", type: "string"}]
});
Ext.define("Dsh.store.CombineStore", {
    extend: "Ext.data.Store",
    storeId: "CombineStore",
    model: "Dsh.model.Combine",
    autoLoad: false,
    proxy: {type: "rest", url: "/api/dsr/field/breakdown", reader: {type: "json", root: "breakdowns"}}
});
Ext.define("Dsh.model.ConnectionResults", {
    extend: "Ext.data.Model",
    fields: [{name: "displayValue", type: "string"}, {name: "alias", type: "string"}, {
        name: "id",
        type: "int"
    }, "data"],
    hasMany: [{model: "Dsh.model.Result", name: "data"}]
});
Ext.define("Dsh.store.ConnectionResultsStore", {
    extend: "Uni.data.store.Filterable",
    model: "Dsh.model.ConnectionResults",
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: "ajax",
        url: "/api/dsr/connectionheatmap",
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {type: "json", root: "heatMap"}
    }
});
Ext.define("Dsh.controller.ConnectionOverview", {
    extend: "Ext.app.Controller",
    models: ["Dsh.model.connection.Overview"],
    stores: ["CommunicationServerInfos", "Dsh.store.CombineStore", "Dsh.store.ConnectionResultsStore"],
    views: ["Dsh.view.ConnectionOverview"],
    refs: [{ref: "connectionOverview", selector: "#connection-overview"}, {
        ref: "header",
        selector: "#header-section"
    }, {ref: "summary", selector: "#summary"}, {
        ref: "communicationServers",
        selector: "#communication-servers"
    }, {ref: "overview", selector: "#overview"}, {ref: "breakdown", selector: "#breakdown"}, {
        ref: "kpi",
        selector: "#connection-overview read-outs-over-time"
    }, {ref: "quickLinks", selector: "#connection-overview #quick-links"}],
    init: function () {
        this.control({
            "#connection-overview #refresh-btn": {click: this.loadData},
            "#connection-overview #device-group": {change: this.updateQuickLinks}
        })
    },
    showOverview: function () {
        var a = this.getController("Uni.controller.history.Router");
        this.getApplication().fireEvent("changecontentevent", Ext.widget("connection-overview", {router: a}));
        this.loadData()
    },
    loadData: function () {
        var c = this, b = c.getModel("Dsh.model.connection.Overview"), a = this.getController("Uni.controller.history.Router");
        b.setFilter(a.filter);
        c.getConnectionOverview().setLoading();
        c.getCommunicationServers().reload();
        b.load(null, {
            success: function (d) {
                c.getSummary().setRecord(d.getSummary());
                c.getOverview().bindStore(d.overviews());
                c.getBreakdown().bindStore(d.breakdowns());
                if (d.raw.kpi) {
                    c.getKpi().setRecord(d.getKpi())
                }
                c.getHeader().down("#last-updated-field").setValue("Last updated at " + Uni.DateTime.formatTimeShort(new Date()))
            }, callback: function () {
                c.getConnectionOverview().setLoading(false)
            }
        })
    },
    updateQuickLinks: function () {
        if (Uni.Auth.hasAnyPrivilege(["privilege.view.reports"])) {
            var e = this;
            var b = e.getHeader().down("#device-group");
            var f = b.groupName;
            var d = false;
            if (f && f.length) {
                d = encodeURIComponent(Ext.JSON.encode({GROUPNAME: f}))
            }
            var a = Ext.getStore("ReportInfos");
            if (a) {
                var c = a.getProxy();
                c.setExtraParam("category", "MDC");
                c.setExtraParam("subCategory", "Device Connections");
                a.load(function (h) {
                    var g = Ext.isArray(e.getQuickLinks().data) ? e.getQuickLinks().data : [];
                    Ext.each(h, function (j) {
                        var l = j.get("name");
                        var k = j.get("reportUUID");
                        g.push({
                            link: l,
                            href: "#/administration/generatereport?reportUUID=&subCategory=Device%Connections" + k + (d ? "&filter=" + d : ""),
                            target: "_blank"
                        })
                    });
                    var i = e.getQuickLinks().down("#quicklinksTplPanel");
                    i.update(g)
                })
            }
        }
    }
});
Ext.define("Dsh.view.widget.ConnectionsList", {
    extend: "Ext.grid.Panel",
    alias: "widget.connections-list",
    requires: ["Ext.grid.column.Date", "Ext.form.field.ComboBox", "Ext.grid.column.Template", "Uni.grid.column.Action", "Uni.view.toolbar.PagingTop", "Uni.view.toolbar.PagingBottom", "Dsh.view.widget.ConnectionActionMenu"],
    itemId: "connectionslist",
    store: "Dsh.store.ConnectionTasks",
    columns: {
        defaults: {sortable: false, menuDisabled: true},
        items: [{
            itemId: "Device",
            text: Uni.I18n.translate("connection.widget.details.device", "DSH", "Device"),
            dataIndex: "device",
            flex: 1,
            renderer: function (a) {
                return Uni.Auth.hasAnyPrivilege(["privilege.view.device", "privilege.administrate.deviceData"]) ? '<a href="#/devices/' + a.id + '">' + a.name + "</a>" : a.name
            }
        }, {
            itemId: "connectionMethod",
            text: Uni.I18n.translate("connection.widget.details.connectionMethod", "DSH", "Connection method"),
            dataIndex: "connectionMethod",
            flex: 1,
            renderer: function (a) {
                return a ? a.name : ""
            }
        }, {
            itemId: "currentState",
            text: Uni.I18n.translate("connection.widget.details.currentState", "DSH", "Current state"),
            dataIndex: "currentState",
            flex: 1,
            renderer: function (a) {
                return a ? a.displayValue : ""
            }
        }, {
            itemId: "latestStatus",
            text: Uni.I18n.translate("connection.widget.details.latestStatus", "DSH", "Latest status"),
            dataIndex: "latestStatus",
            flex: 1,
            renderer: function (a) {
                return a ? a.displayValue : ""
            }
        }, {
            itemId: "latestResult",
            text: Uni.I18n.translate("connection.widget.details.latestResult", "DSH", "Latest result"),
            dataIndex: "latestResult",
            name: "latestResult",
            flex: 1,
            renderer: function (a) {
                return a ? a.displayValue : ""
            }
        }, {
            dataIndex: "taskCount", itemId: "taskCount", renderer: function (c, a) {
                a.tdCls = "communication-tasks-status";
                var b = "";
                if (c.numberOfSuccessfulTasks || c.numberOfFailedTasks || c.numberOfIncompleteTasks) {
                    b += '<tpl><span class="icon-checkmark"></span>' + (c.numberOfSuccessfulTasks ? c.numberOfSuccessfulTasks : "0") + "</tpl>";
                    b += '<tpl><span class="icon-close"></span>' + (c.numberOfFailedTasks ? c.numberOfFailedTasks : "0") + "</tpl>";
                    b += '<tpl><span  class="icon-stop2"></span>' + (c.numberOfIncompleteTasks ? c.numberOfIncompleteTasks : "0") + "</tpl>"
                }
                return b
            }, header: Uni.I18n.translate("connection.widget.details.commTasks", "DSH", "Communication tasks"), flex: 2
        }, {
            itemId: "startDateTime",
            text: Uni.I18n.translate("connection.widget.details.startedOn", "DSH", "Started on"),
            dataIndex: "startDateTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeShort(a) : ""
            },
            flex: 1
        }, {
            itemId: "connectionsActionMenu",
            xtype: "uni-actioncolumn",
            menu: {xtype: "connection-action-menu", itemId: "connectionsActionMenu"}
        }]
    },
    dockedItems: [{
        itemId: "pagingtoolbartop",
        xtype: "pagingtoolbartop",
        dock: "top",
        store: "Dsh.store.ConnectionTasks",
        displayMsg: Uni.I18n.translate("connection.widget.details.displayMsg", "DDSH", "{0} - {1} of {2} connections"),
        displayMoreMsg: Uni.I18n.translate("connection.widget.details.displayMoreMsg", "DSH", "{0} - {1} of more than {2} connections"),
        emptyMsg: Uni.I18n.translate("connection.widget.details.emptyMsg", "DSH", "There are no connections to display"),
        items: [{
            xtype: "button",
            itemId: "generate-report",
            hidden: !Uni.Auth.hasAnyPrivilege(["privilege.view.reports"]),
            text: Uni.I18n.translate("generatereport.generateReportButton", "DSH", "Generate report")
        }]
    }, {
        itemId: "pagingtoolbarbottom",
        xtype: "pagingtoolbarbottom",
        store: "Dsh.store.ConnectionTasks",
        dock: "bottom",
        deferLoading: true,
        itemsPerPageMsg: Uni.I18n.translate("connection.widget.details.itemsPerPage", "DSH", "Connections per page")
    }],
    addTooltip: function () {
        var d = this, a = d.getView(), e = Ext.create("Ext.tip.ToolTip", {
            target: a.el,
            delegate: "img.ct-result",
            trackMouse: true,
            renderTo: Ext.getBody(),
            listeners: {
                beforeshow: function c(j) {
                    var h, g = Ext.get(j.triggerElement).up("tr"), i = a.getRecord(g).get("taskCount"), f = i.numberOfFailedTasks + " " + Uni.I18n.translate("connection.widget.details.comTasksFailed", "DSH", "communication tasks failed"), l = i.numberOfSuccessfulTasks + " " + Uni.I18n.translate("connection.widget.details.comTasksSuccessful", "DSH", "communication tasks successful"), k = i.numberOfIncompleteTasks + " " + Uni.I18n.translate("connection.widget.details.comTasksNotCompleted", "DSH", "communication tasks not completed");
                    (j.triggerElement.className.search("ct-success") !== -1) && (h = l);
                    (j.triggerElement.className.search("ct-failure") !== -1) && (h = f);
                    (j.triggerElement.className.search("ct-incomplete") !== -1) && (h = k);
                    j.update(h)
                }
            }
        }), b = Ext.create("Ext.tip.ToolTip", {
            target: a.el,
            delegate: "td.x-grid-cell-headerId-latestResult",
            trackMouse: true,
            renderTo: Ext.getBody(),
            listeners: {
                show: function () {
                    var f = Ext.get(b.triggerElement).up("tr"), g = a.getRecord(f).get("latestResult");
                    if (g.retries) {
                        b.update(g.retries + " " + Uni.I18n.translate("connection.widget.details.retries", "DSH", "retries"))
                    } else {
                        b.hide()
                    }
                }
            }
        })
    },
    initComponent: function () {
        var a = this;
        a.on("afterrender", a.addTooltip);
        a.callParent(arguments)
    }
});
Ext.define("Dsh.store.filter.LatestStatus", {
    extend: "Ext.data.Store",
    fields: ["localizedValue", "successIndicator"],
    autoLoad: false,
    proxy: {
        type: "rest",
        url: "/api/dsr/field/connectiontasksuccessindicators",
        reader: {type: "json", root: "successIndicators"}
    }
});
Ext.define("Dsh.store.filter.CommPortPool", {
    extend: "Ext.data.Store",
    fields: ["name", "id"],
    autoLoad: false,
    proxy: {type: "rest", url: "/api/dsr/field/comportpools", reader: {type: "json", root: "comPortPools"}}
});
Ext.define("Dsh.view.widget.SideFilter", {
    extend: "Ext.panel.Panel",
    alias: "widget.dsh-side-filter",
    requires: ["Uni.component.filter.view.Filter", "Dsh.view.widget.common.SideFilterCombo", "Dsh.view.widget.common.SideFilterDateTime", "Dsh.store.filter.CurrentState", "Dsh.store.filter.LatestStatus", "Dsh.store.filter.LatestResult", "Dsh.store.filter.CommPortPool", "Dsh.store.filter.ConnectionType", "Dsh.store.filter.DeviceType"],
    cls: "filter-form",
    width: 250,
    title: Uni.I18n.translate("connection.widget.sideFilter.title", "DSH", "Filter"),
    ui: "medium",
    items: [{
        xtype: "nested-form",
        itemId: "filter-form",
        hydrator: "Dsh.util.FilterHydrator",
        ui: "filter",
        layout: {type: "vbox", align: "stretch"},
        defaults: {xtype: "side-filter-combo", labelAlign: "top"},
        items: [{
            itemId: "device-group",
            name: "deviceGroup",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.deviceGroup", "DSH", "Device group"),
            displayField: "name",
            valueField: "id",
            store: "Dsh.store.filter.DeviceGroup"
        }, {
            itemId: "current-state",
            name: "currentStates",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.currentState", "DSH", "Current state"),
            displayField: "localizedValue",
            valueField: "taskStatus",
            store: "Dsh.store.filter.CurrentState"
        }, {
            itemId: "latest-status",
            name: "latestStates",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.latestStatus", "DSH", "Latest status"),
            displayField: "localizedValue",
            valueField: "successIndicator",
            store: "Dsh.store.filter.LatestStatus"
        }, {
            itemId: "latest-result",
            name: "latestResults",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.latestResult", "DSH", "Latest result"),
            displayField: "localizedValue",
            valueField: "successIndicator",
            store: "Dsh.store.filter.LatestResult"
        }, {
            itemId: "comport-pool",
            name: "comPortPools",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.comPortPool", "DSH", "Communication port pool"),
            displayField: "name",
            valueField: "id",
            store: "Dsh.store.filter.CommPortPool"
        }, {
            itemId: "connection-type",
            name: "connectionTypes",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.connectionType", "DSH", "Connection type"),
            displayField: "name",
            valueField: "id",
            store: "Dsh.store.filter.ConnectionType"
        }, {
            itemId: "device-type",
            name: "deviceTypes",
            fieldLabel: Uni.I18n.translate("connection.widget.sideFilter.deviceType", "DSH", "Device type"),
            displayField: "name",
            valueField: "id",
            store: "Dsh.store.filter.DeviceType"
        }, {
            xtype: "side-filter-date-time",
            itemId: "started-between",
            name: "startedBetween",
            wTitle: Uni.I18n.translate("connection.widget.sideFilter.startedBetween", "DSH", "Started between")
        }, {
            xtype: "side-filter-date-time",
            itemId: "finished-between",
            name: "finishedBetween",
            wTitle: Uni.I18n.translate("connection.widget.sideFilter.finishedBetween", "DSH", "Finished successfully between")
        }],
        dockedItems: [{
            xtype: "toolbar",
            dock: "bottom",
            items: [{
                text: Uni.I18n.translate("connection.widget.sideFilter.apply", "DSH", "Apply"),
                ui: "action",
                action: "applyfilter",
                itemId: "btn-apply-filter"
            }, {
                text: Uni.I18n.translate("connection.widget.sideFilter.clearAll", "DSH", "Clear all"),
                action: "clearfilter",
                itemId: "btn-clear-filter"
            }]
        }]
    }]
});
Ext.define("Dsh.view.widget.connection.CommunicationsList", {
    extend: "Ext.grid.Panel",
    alias: "widget.connection-communications-list",
    store: "Dsh.store.CommunicationTasks",
    requires: ["Ext.grid.column.Date", "Ext.form.field.ComboBox", "Ext.grid.column.Template", "Uni.grid.column.Action", "Uni.view.toolbar.PagingTop", "Uni.view.toolbar.PagingBottom"],
    columns: {
        defaults: {sortable: false, menuDisabled: true},
        items: [{
            itemId: "name",
            text: Uni.I18n.translate("connection.communication.widget.details.commmunication", "DSH", "Communication task"),
            dataIndex: "comTask",
            renderer: function (a) {
                return a.name
            },
            flex: 2
        }, {
            itemId: "device",
            text: Uni.I18n.translate("connection.communication.widget.details.device", "DSH", "Device"),
            dataIndex: "device",
            flex: 1,
            renderer: function (a) {
                return a.name ? a.name : ""
            }
        }, {
            itemId: "currentState",
            text: Uni.I18n.translate("connection.communication.widget.details.currentState", "DSH", "Current state"),
            dataIndex: "currentState",
            flex: 1,
            renderer: function (a) {
                return a.displayValue ? a.displayValue : ""
            }
        }, {
            itemId: "Result",
            text: Uni.I18n.translate("connection.communication.widget.details.result", "DSH", "Result"),
            dataIndex: "result",
            flex: 1,
            renderer: function (a) {
                return a.displayValue ? a.displayValue : ""
            }
        }, {
            itemId: "nextCommunication",
            text: Uni.I18n.translate("connection.communication.widget.details.nextCommunication", "DSH", "Next communication"),
            dataIndex: "nextCommunication",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeShort(a) : ""
            },
            flex: 2
        }, {
            itemId: "startTime",
            text: Uni.I18n.translate("connection.communication.widget.details.startedOn", "DSH", "Started on"),
            dataIndex: "startTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeShort(a) : ""
            },
            flex: 2
        }, {
            itemId: "stopTime",
            text: Uni.I18n.translate("connection.communication.widget.details.finishedOn", "DSH", "Finished on"),
            dataIndex: "stopTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeShort(a) : ""
            },
            flex: 2
        }, {itemId: "communicationsGridActionMenu", xtype: "uni-actioncolumn", menu: {}}]
    },
    initComponent: function () {
        var a = this;
        a.dockedItems = [{
            itemId: "pagingtoolbartop",
            xtype: "pagingtoolbartop",
            dock: "top",
            store: a.store,
            displayMsg: Uni.I18n.translate("connection.communication.widget.details.displayMsg", "DDSH", "{0} - {1} of {2} communication tasks"),
            displayMoreMsg: Uni.I18n.translate("connection.communication.widget.details.displayMoreMsg", "DSH", "{0} - {1} of more than {2} communication tasks"),
            emptyMsg: Uni.I18n.translate("connection.communication.widget.details.emptyMsg", "DSH", "There are no communication task to display"),
            items: [{
                xtype: "button",
                itemId: "generate-report",
                hidden: !Uni.Auth.hasAnyPrivilege(["privilege.view.reports"]),
                text: Uni.I18n.translate("generatereport.generateReportButton", "DSH", "Generate report")
            }]
        }, {
            itemId: "pagingtoolbarbottom",
            xtype: "pagingtoolbarbottom",
            store: a.store,
            dock: "bottom",
            deferLoading: true,
            itemsPerPageMsg: Uni.I18n.translate("connection.communication.widget.details.itemsPerPage", "DSH", "Communication tasks per page")
        }];
        a.callParent(arguments)
    }
});
Ext.define("Dsh.view.widget.connection.PreviewCommunication", {
    extend: "Ext.form.Panel",
    alias: "widget.preview-connection-communication",
    title: "",
    frame: true,
    layout: {type: "column"},
    tools: [{
        xtype: "button",
        text: Uni.I18n.translate("general.actions", "ISE", "Actions"),
        iconCls: "x-uni-action-iconD",
        itemId: "communicationPreviewActionMenu",
        menu: {}
    }],
    items: [{
        columnWidth: 0.5,
        defaults: {xtype: "displayfield", labelWidth: 200},
        items: [{
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.commTaskName", "DSH", "Name"),
            name: "comTask",
            renderer: function (a) {
                return a.name
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.device", "DSH", "Device"),
            name: "device",
            renderer: function (b) {
                var a = "";
                if (b) {
                    Uni.Auth.hasAnyPrivilege(["privilege.view.device", "privilege.administrate.deviceData"]) ? a = '<a href="#/devices/' + b.id + '">' + b.name + "</a>" : a = b.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.deviceType", "DSH", "Device type"),
            name: "deviceType",
            renderer: function (b) {
                var a = "";
                if (b) {
                    Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceType", "privilege.view.deviceType"]) ? a = '<a href="#/administration/devicetypes/' + b.id + '">' + b.name + "</a>" : a = b.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.deviceConfig", "DSH", "Device configuration"),
            name: "devConfig",
            renderer: function (b) {
                var a = "";
                b && (a = '<a href="#/administration/devicetypes/' + b.devType.id + "/deviceconfigurations/" + b.config.id + '">' + b.config.name + "</a>");
                if (a !== "" && !Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceType", "privilege.view.deviceType"])) {
                    a = b.config.name
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.frequency", "DSH", "Frequency"),
            name: "comScheduleFrequency",
            renderer: function (b) {
                var a = "";
                if (b) {
                    a = Uni.I18n.translate("connection.communication.widget.details.every", "DSH", "Every") + " " + b.every.count + " " + b.every.timeUnit
                }
                return a
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.urgency", "DSH", "Urgency"),
            name: "urgency"
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.executeOnInbound", "DSH", "Always execute on inbound"),
            name: "alwaysExecuteOnInbound",
            renderer: function (a) {
                if (!_.isUndefined(a)) {
                    return a ? "Yes" : "No"
                } else {
                    return ""
                }
            }
        }]
    }, {
        columnWidth: 0.5,
        defaults: {xtype: "displayfield", labelWidth: 200},
        items: [{
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.currentState", "DSH", "Current state"),
            name: "currentState",
            renderer: function (a) {
                return a.displayValue ? a.displayValue : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.latestResult", "DSH", "Result"),
            name: "result",
            renderer: function (a) {
                return a.displayValue ? a.displayValue : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.startedOn", "DSH", "Started on"),
            name: "startTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.finishedOn", "DSH", "Finished on"),
            name: "stopTime",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }, {
            fieldLabel: Uni.I18n.translate("connection.communication.widget.details.nextComm", "DSH", "Next communication"),
            name: "nextCommunication",
            renderer: function (a) {
                return a ? Uni.DateTime.formatDateTimeLong(a) : ""
            }
        }]
    }],
    initComponent: function () {
        var a = this;
        a.callParent(arguments)
    }
});
Ext.define("Dsh.store.ConnectionTasks", {
    extend: "Uni.data.store.Filterable",
    requires: ["Dsh.model.ConnectionTask", "Dsh.util.FilterStoreHydrator"],
    model: "Dsh.model.ConnectionTask",
    hydrator: "Dsh.util.FilterStoreHydrator",
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: "rest",
        url: "/api/dsr/connections",
        reader: {type: "json", root: "connectionTasks", totalProperty: "total"}
    }
});
Ext.define("Dsh.view.Connections", {
    extend: "Uni.view.container.ContentContainer",
    alias: "widget.connections-details",
    itemId: "connectionsdetails",
    requires: ["Dsh.view.widget.ConnectionsList", "Dsh.view.widget.PreviewConnection", "Dsh.view.widget.SideFilter", "Dsh.view.widget.connection.CommunicationsList", "Dsh.view.widget.connection.PreviewCommunication", "Dsh.store.ConnectionTasks"],
    content: [{
        xtype: "panel",
        ui: "large",
        title: Uni.I18n.translate("workspace.dataCommunication.connections.title", "DSH", "Connections")
    }, {xtype: "filter-top-panel", itemId: "dshconnectionsfilterpanel"}, {
        xtype: "panel",
        items: {
            xtype: "preview-container",
            grid: {xtype: "connections-list", itemId: "connectionsdetails"},
            emptyComponent: {
                xtype: "no-items-found-panel",
                title: Uni.I18n.translate("workspace.dataCommunication.connections.empty.title", "DSH", "No connections found"),
                reasons: [Uni.I18n.translate("workspace.dataCommunication.connections.empty.list.item1", "DSH", "No connections in the system."), Uni.I18n.translate("workspace.dataCommunication.connections.empty.list.item2", "DSH", "No connections found due to applied filters.")]
            },
            previewComponent: {xtype: "preview_connection", itemId: "connectionpreview", hidden: true}
        }
    }, {
        ui: "medium",
        itemId: "communicationspanel",
        padding: 0,
        margin: "16 0 0 0",
        hidden: true,
        title: "",
        items: [{
            xtype: "preview-container",
            grid: {
                xtype: "connection-communications-list",
                itemId: "communicationsdetails",
                store: "Dsh.store.Communications"
            },
            emptyComponent: {
                xtype: "no-items-found-panel",
                title: Uni.I18n.translate("communication.widget.details.empty.title", "DSH", "No communications found"),
                reasons: [Uni.I18n.translate("communication.widget.details.empty.list.item1", "DSH", "No communications in the system."), Uni.I18n.translate("communication.widget.details.empty.list.item2", "DSH", "No communications found due to applied filters.")]
            },
            previewComponent: {xtype: "preview-connection-communication", itemId: "communicationpreview"}
        }]
    }],
    side: [{xtype: "dsh-side-filter", itemId: "dshconnectionssidefilter"}],
    initComponent: function () {
        this.callParent(arguments)
    }
});
Ext.define("Dsh.model.connection.CommunicationTask", {
    extend: "Ext.data.Model",
    fields: ["name", "device", "id", "deviceConfiguration", "deviceType", "comScheduleName", "comScheduleFrequency", "urgency", "currentState", "alwaysExecuteOnInbound", "result", "connectionTask", "sessionId", "comTask", {
        name: "startTime",
        type: "date",
        dateFormat: "time"
    }, {name: "stopTime", type: "date", dateFormat: "time"}, {
        name: "nextCommunication",
        type: "date",
        dateFormat: "time"
    }, {
        name: "title", persist: false, mapping: function (a) {
            return a.comTask.name + " on " + a.device.name
        }
    }, {
        name: "devConfig", persist: false, mapping: function (a) {
            var b = {};
            b.config = a.deviceConfiguration;
            b.devType = a.deviceType;
            return b
        }
    }],
    hasOne: {
        model: "Dsh.model.ConnectionTask",
        associationKey: "connectionTask",
        name: "connectionTask",
        getterName: "getConnectionTask"
    },
    run: function (a) {
        Ext.Ajax.request({method: "PUT", url: this.proxy.url + "/{id}/run".replace("{id}", this.getId()), success: a})
    },
    runNow: function (a) {
        Ext.Ajax.request({
            method: "PUT",
            url: this.proxy.url + "/{id}/runnow".replace("{id}", this.getId()),
            success: a
        })
    },
    proxy: {
        type: "ajax",
        url: "/api/dsr/communications",
        reader: {type: "json", root: "communicationTasks", totalProperty: "total"}
    }
});
Ext.define("Dsh.store.Communications", {
    extend: "Ext.data.Store",
    requires: ["Dsh.model.connection.CommunicationTask"],
    model: "Dsh.model.connection.CommunicationTask",
    autoLoad: false,
    remoteFilter: true,
    url: "/api/dsr/connections/",
    communicationsPostfix: "/latestcommunications",
    proxy: {type: "ajax", reader: {type: "json", root: "communications", totalProperty: "total"}},
    setConnectionId: function (a) {
        this.getProxy().url = this.url + a + this.communicationsPostfix
    }
});
Ext.define("Dsh.controller.Connections", {
    extend: "Dsh.controller.BaseController",
    models: ["Dsh.model.ConnectionTask", "Dsh.model.CommTasks", "Dsh.model.CommunicationTask", "Dsh.model.Filter"],
    stores: ["Dsh.store.ConnectionTasks", "Dsh.store.Communications"],
    views: ["Dsh.view.Connections", "Dsh.view.widget.PreviewConnection", "Dsh.view.widget.connection.CommunicationsList", "Dsh.view.widget.connection.PreviewCommunication"],
    refs: [{ref: "connectionsList", selector: "#connectionsdetails"}, {
        ref: "connectionPreview",
        selector: "#connectionsdetails #connectionpreview"
    }, {ref: "communicationList", selector: "#connectionsdetails #communicationsdetails"}, {
        ref: "communicationPreview",
        selector: "#connectionsdetails #communicationpreview"
    }, {ref: "communicationsPanel", selector: "#connectionsdetails #communicationspanel"}, {
        ref: "filterPanel",
        selector: "#connectionsdetails filter-top-panel"
    }, {ref: "sideFilterForm", selector: "#connectionsdetails #filter-form"}, {
        ref: "connectionsActionMenu",
        selector: "#connectionsActionMenu"
    }, {
        ref: "connectionsPreviewActionMenu",
        selector: "#connectionsPreviewActionBtn #connectionsActionMenu"
    }, {
        ref: "communicationsGridActionMenu",
        selector: "#communicationsGridActionMenu"
    }, {ref: "communicationPreviewActionMenu", selector: "#communicationPreviewActionMenu"}],
    prefix: "#connectionsdetails",
    init: function () {
        this.control({
            "connections-details #connectionsdetails": {selectionchange: this.onSelectionChange},
            "connections-details #communicationsdetails": {selectionchange: this.onCommunicationSelectionChange},
            "#connectionsActionMenu": {show: this.initConnectionMenu},
            "connections-list #generate-report": {click: this.onGenerateReport},
            "connections-details uni-actioncolumn": {
                run: this.connectionRun,
                viewLog: this.viewLog,
                viewHistory: this.viewHistory
            }
        });
        this.callParent(arguments)
    },
    showOverview: function () {
        var b = Ext.widget("connections-details"), a = this.getStore("Dsh.store.ConnectionTasks");
        this.getApplication().fireEvent("changecontentevent", b);
        this.initFilter();
        a.load()
    },
    onCommunicationSelectionChange: function (d, e) {
        var f = this, a = f.getCommunicationsPanel(), b = e[0], g = f.getCommunicationPreview(), c = [];
        a.show();
        b.data.devConfig = {config: b.data.deviceConfiguration, devType: b.data.deviceType};
        b.data.title = b.data.comTask.name + " on " + b.data.device.name;
        g.setTitle(b.data.title);
        g.loadRecord(b);
        this.initMenu(b, c)
    },
    initMenu: function (a, d) {
        var e = this, b = this.getCommunicationsGridActionMenu().menu, c = this.getCommunicationPreviewActionMenu().menu;
        Ext.suspendLayouts();
        b.removeAll();
        c.removeAll();
        if (a.get("sessionId") !== 0) {
            d.push({
                text: Ext.String.format(Uni.I18n.translate("connection.widget.details.menuItem", "MDC", "View '{0}' log"), a.get("comTask").name),
                action: {
                    action: "viewlog",
                    comTask: {mRID: a.get("device").id, sessionId: a.get("id"), comTaskId: a.get("comTask").id}
                },
                listeners: {click: e.viewCommunicationLog}
            })
        }
        b.add(d);
        c.add(d);
        Ext.resumeLayouts(true)
    },
    onSelectionChange: function (a, e) {
        var h = this, g = h.getConnectionPreview(), d = h.getCommunicationsPanel(), c = h.getStore("Dsh.store.Communications"), f = e[0];
        if (!_.isEmpty(f)) {
            h.getConnectionsPreviewActionMenu().record = f;
            var b = f.get("id"), i = " " + f.get("title");
            g.loadRecord(f);
            g.setTitle(i);
            d.setTitle(Uni.I18n.translate("connection.widget.details.communicationTasksOf", "DSH", "Communication tasks of") + i);
            if (b) {
                c.setConnectionId(b);
                c.load();
                d.hide()
            }
        }
    },
    initConnectionMenu: function (a) {
        if (a && a.record) {
            if (a.record.get("comSessionId") !== 0) {
                a.down("menuitem[action=viewLog]").show()
            } else {
                a.down("menuitem[action=viewLog]").hide()
            }
        }
    },
    viewCommunicationLog: function (a) {
        location.href = "#/devices/" + a.action.comTask.mRID + "/communicationtasks/" + a.action.comTask.comTaskId + "/history/" + a.action.comTask.sessionId + "/viewlog?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D"
    },
    onGenerateReport: function () {
        var c = this;
        var b = this.getController("Uni.controller.history.Router");
        var d = {};
        d.deviceGroup = "GROUPNAME";
        d.currentStates = "STATUS";
        d.latestResults = null;
        d.comSchedules = "SCHEDULENAME";
        d.deviceTypes = null;
        d.comTasks = "COMTASKNAME";
        d.comPortPools = "PORTPOOLNAME";
        d.connectionTypes = "CONNECTIONTYPE";
        var g = false;
        var a = c.getSideFilterForm().getForm().getFields();
        a.each(function (j) {
            g = g || {};
            var h = d[j.getName()];
            if (h) {
                var i = j.getRawValue();
                if (j.getXType() == "side-filter-combo") {
                    i = Ext.isString(i) && i.split(", ") || i;
                    i = _.isArray(i) && _.compact(i) || i
                }
            }
            g[h] = i
        });
        if (b.filter && b.filter.startedBetween) {
            var f = b.filter.startedBetween.get("from");
            var e = b.filter.startedBetween.get("to");
            g.CONNECTIONDATE = {
                from: f && Ext.Date.format(f, "Y-m-d H:i:s"),
                to: e && Ext.Date.format(e, "Y-m-d H:i:s")
            }
        }
        b.getRoute("generatereport").forward(null, {category: "MDC", subCategory: "Device Connections", filter: g})
    },
    connectionRun: function (a) {
        var b = this;
        a.run(function () {
            b.getApplication().fireEvent("acknowledge", Uni.I18n.translate("connection.run.now", "MDC", "Run succeeded"));
            a.set("nextExecution", new Date());
            b.showOverview()
        })
    },
    viewLog: function (a) {
        var c = this, b = c.getController("Uni.controller.history.Router");
        b.getRoute("devices/device/connectionmethods/history/viewlog").forward({
            mRID: a.get("device").id,
            connectionMethodId: a.get("id"),
            historyId: a.get("comSessionId")
        })
    },
    viewHistory: function (a) {
        var c = this, b = c.getController("Uni.controller.history.Router");
        b.getRoute("devices/device/connectionmethods/history").forward({
            mRID: a.get("device").id,
            connectionMethodId: a.get("id")
        })
    }
});
Ext.define("Dsh.view.widget.OpenDataCollectionIssues", {
    extend: "Ext.panel.Panel",
    ui: "tile",
    alias: "widget.open-data-collection-issues",
    buttonAlign: "left",
    layout: "fit",
    router: null,
    header: {ui: "small"},
    setRecord: function (e) {
        var h = this, g = h.down("#issues-dataview"), b = h.down("#open-data-collection-issues-count-container"), l = h.down("#open-data-collection-issues-docked-links"), f = e.getAssignedToMeIssues(), c = e.getUnassignedIssues(), k = f.topMyIssues(), a = k.getCount();
        var i = Uni.I18n.translatePlural("overview.widget.openDataCollectionIssues.header", f.get("total"), "DSH", "<h3>Open data collection issues ({0})</h3>");
        h.setTitle(i);
        k.each(function (n) {
            var m = n.get("dueDate");
            n.set("href", h.router.getRoute("workspace/datacollectionissues/view").buildUrl({issueId: n.get("id")}));
            if (m) {
                if (moment().isAfter(moment(m))) {
                    n.set("tooltip", Uni.I18n.translate("overview.widget.openDataCollectionIssues.overdue", "DSH", "Overdue"));
                    n.set("icon", "/apps/dsh/resources/images/widget/blocked.png")
                } else {
                    if (moment().endOf("day").isAfter(moment(m))) {
                        n.set("tooltip", Uni.I18n.translate("overview.widget.openDataCollectionIssues.dueToday", "DSH", "Due today"));
                        n.set("icon", "/apps/dsh/resources/images/widget/blocked.png")
                    } else {
                        if (moment().add(1, "day").endOf("day").isAfter(moment(m))) {
                            n.set("tooltip", Uni.I18n.translate("overview.widget.openDataCollectionIssues.dueTomorrow", "DSH", "Due tomorrow"));
                            n.set("icon", "/apps/dsh/resources/images/widget/inactive.png")
                        }
                    }
                }
            }
        });
        g.bindStore(k);
        Ext.suspendLayouts();
        b.removeAll();
        l.removeAll();
        if (a === 0) {
            b.add({
                xtype: "label",
                text: Uni.I18n.translate("operator.dashboard.issuesEmptyMsg", "DSH", "No open issues assigned to you.")
            })
        }
        if (a) {
            b.add({
                xtype: "container",
                html: Ext.String.format(Uni.I18n.translate("overview.widget.openDataCollectionIssues.topIssues", "DSH", "Top {0} most urgent issues assigned to me"), a)
            })
        }
        var d = Ext.apply(f.get("filter"), {
            assignee: f.get("filter").assigneeId + ":" + f.get("filter").assigneeType,
            status: ["status.open"]
        });
        var j = {assignee: c.get("filter").assigneeId + ":" + c.get("filter").assigneeType, status: ["status.open"]};
        l.add([{
            xtype: "button",
            itemId: "lnk-assigned-issues-link",
            text: Ext.String.format(Uni.I18n.translate("overview.widget.openDataCollectionIssues.assignedToMe", "DSH", "Assigned to me ({0})"), f.get("total")),
            ui: "link",
            href: h.router.getRoute("workspace/datacollectionissues").buildUrl(null, {filter: d})
        }, {
            xtype: "button",
            itemId: "lnk-unassigned-issues-link",
            text: Ext.String.format(Uni.I18n.translate("overview.widget.openDataCollectionIssues.unassigned", "DSH", "Unassigned ({0})"), c.get("total")),
            ui: "link",
            href: h.router.getRoute("workspace/datacollectionissues").buildUrl(null, {filter: j})
        }]);
        Ext.resumeLayouts(true)
    },
    tbar: {xtype: "container", itemId: "open-data-collection-issues-count-container"},
    items: [{
        xtype: "dataview",
        itemId: "issues-dataview",
        overflowY: "auto",
        itemSelector: "a.x-btn.flag-toggle",
        tpl: new Ext.XTemplate('<table  style="margin: 5px 0 10px 0">', '<tpl for=".">', '<tr id="{id}" class="issue">', '<td height="26" width="40" data-qtip="{tooltip}"><img style="margin: 5px 5px 0 0" src="{icon}" /></td>', '<td width="100%"><a href="{href}">{title}</a></td>', "</tr>", "</tpl>", "</table>")
    }],
    bbar: {xtype: "container", itemId: "open-data-collection-issues-docked-links"}
});
Ext.define("Dsh.view.widget.DeviceGroupFilter", {
    extend: "Ext.panel.Panel",
    alias: "widget.device-group-filter",
    layout: "hbox",
    initComponent: function () {
        var c = this;
        var a = Ext.getStore("Dsh.store.filter.DeviceGroup" || "ext-empty-store");
        this.items = [{
            xtype: "container",
            html: Uni.I18n.translate("overview.widget.headerSection.filter", "DSH", "Filter"),
            cls: "x-form-display-field",
            style: {paddingRight: "10px"}
        }, {
            xtype: "button",
            style: {"background-color": "#71adc7"},
            itemId: "device-group",
            label: Uni.I18n.translate("overview.widget.headerSection.deviceGroupLabel", "DSH", "Device group") + ": ",
            arrowAlign: "right",
            groupName: null,
            menuAlign: "tl-bl",
            menu: {
                enableScrolling: true,
                maxHeight: 350,
                itemId: "mnu-device-group-filter",
                router: c.router,
                listeners: {
                    click: function (e, d) {
                        this.router.filter.set("deviceGroup", d.value);
                        this.router.filter.save();
                        this.groupName = Ext.isNumber(d.value) ? d.text : null
                    }
                }
            },
            setValue: function (e) {
                var d = this.menu.items.findBy(function (f) {
                    return f.value == e
                });
                if (d) {
                    d.setActive();
                    this.setText(this.label + d.text);
                    this.groupName = Ext.isNumber(d.value) ? d.text : null;
                    this.fireEvent("change", this)
                }
            }
        }];
        this.callParent(arguments);
        var b = c.down("#device-group");
        Ext.suspendLayouts();
        a.load(function () {
            var d = b.menu;
            d.removeAll();
            d.add({text: Uni.I18n.translate("overview.widget.headerSection.none", "DSH", "None"), value: ""});
            a.each(function (e) {
                d.add({text: e.get("name"), value: e.get("id")})
            });
            b.setValue(c.router.filter.get("deviceGroup"))
        });
        Ext.resumeLayouts(true)
    }
});
Ext.define("Dsh.view.widget.FavoriteDeviceGroups", {
    extend: "Ext.panel.Panel",
    alias: "widget.favorite-device-groups",
    ui: "tile",
    store: "Dsh.store.FavoriteDeviceGroups",
    initComponent: function () {
        this.callParent(arguments);
        var b = this, a = Ext.getStore(b.store);
        a.load({
            callback: function () {
                b.add([{
                    xtype: "container",
                    style: {margin: "0 0 10px 0"},
                    html: a.count() > 0 ? "<h3>" + Ext.String.format(Uni.I18n.translate("overview.widget.favoriteDeviceGroups.header", "DSH", "My favorite device groups ({0})"), a.count()) + "</h3>" : "<h3>" + Uni.I18n.translate("overview.widget.favoriteDeviceGroups.headerNoItemsFound", "DSH", "My favorite device groups") + "</h3>"
                }, {
                    xtype: "dataview",
                    store: b.store,
                    itemSelector: "p a",
                    emptyText: Uni.I18n.translate("overview.widget.favoriteDeviceGroups.notFound", "DSH", "No favorite device groups found"),
                    overflowY: "auto",
                    style: "max-height: 120px",
                    tpl: new Ext.XTemplate('<table style="margin-top: 5px">', '<tpl for=".">', "<tr>", '<td style="height: 20px">', Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceGroup", "privilege.view.deviceGroupDetail"]) ? '<a href="#/devices/devicegroups/{id}">{name}</a>' : (Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceOfEnumeratedGroup"]) ? ('<tpl if="dynamic==true"{dynamic}>{name}<tpl else><a href="#/devices/devicegroups/{id}">{name}</a></tpl>') : "{name}"), "</td>", "</tr>", "</tpl>", "</table>")
                }, {
                    xtype: "button",
                    text: Uni.I18n.translate("overview.widget.favoriteDeviceGroups.selectBtn", "DSH", "Select"),
                    style: "margin-top: 15px",
                    href: "#/dashboard/selectfavoritedevicegroups"
                }])
            }
        })
    }
});
Ext.define("Dsh.view.widget.FlaggedDevices", {
    extend: "Ext.panel.Panel",
    ui: "tile",
    alias: "widget.flagged-devices",
    buttonAlign: "left",
    layout: "fit",
    title: Uni.I18n.translatePlural("overview.widget.flaggedDevices.header", 0, "DSH", "<h3>My flagged devices ({0})</h3>"),
    router: null,
    header: {ui: "small"},
    tooltipTpl: new Ext.XTemplate("<table>", "<tr>", '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate("overview.widget.flaggedDevices.device.mrid", "DSH", "MRID") + "</td>", "<td>{mRID}</td>", "</tr>", "<tr>", '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate("overview.widget.flaggedDevices.device.serialNumber", "DSH", "Serial number") + "</td>", "<td>{serialNumber}</td>", "</tr>", "<tr>", '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate("overview.widget.flaggedDevices.device.deviceTypeName", "DSH", "Device Type") + "</td>", "<td>{deviceTypeName}</td>", "</tr>", "<tr>", '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate("overview.widget.flaggedDevices.device.creationDate", "DSH", "Flagged date") + "</td>", "<td>{[Uni.DateTime.formatDateTimeLong(values.deviceLabelInfo.creationDate)]}</td>", "</tr>", "<tr>", '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate("overview.widget.flaggedDevices.device.comment", "DSH", "Comment") + "</td>", "<td>{[values.deviceLabelInfo.comment]}</td>", "</tr>", "</table>"),
    items: {
        xtype: "dataview",
        store: "Dsh.store.FlaggedDevices",
        itemId: "devices-dataview",
        style: "max-height: 160px",
        overflowY: "auto",
        itemSelector: "a.x-btn.flag-toggle",
        emptyText: Uni.I18n.translate("overview.widget.flaggedDevices.noDevicesFound", "DSH", "No flagged devices found"),
        tpl: new Ext.XTemplate('<table  style="margin: 5px 0 10px 0">', '<tpl for=".">', '<tr id="{mRID}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" class="device">', '<td width="100%"><a href="{href}">{mRID}</a></td>', "<td>", '<a data-qtip="' + Uni.I18n.translate("overview.widget.flaggedDevices.unflag", "DSH", "Click to remove from the list of flagged devices") + '" class="flag-toggle x-btn x-btn-plain-small">', '<span style="width: 16px; height: 16px; font-size: 16px" class="x-btn-button"><span class="x-btn-icon-el icon-star6"></span></span></a>', "</td>", "</tr>", "</tpl>", "</table>"),
        listeners: {
            itemclick: function (c, b, e) {
                var h = new Ext.dom.Element(e);
                var d = h.down(".x-btn-icon-el");
                var f = d.hasCls("icon-star6");
                var a = b.getLabel();
                a.proxy.setUrl(b.getId());
                var g = function () {
                    d.toggleCls("icon-star6");
                    d.toggleCls("icon-star4");
                    h.set({"data-qtip": f ? Uni.I18n.translate("overview.widget.flaggedDevices.flag", "DSH", "Click to flag the device") : Uni.I18n.translate("overview.widget.flaggedDevices.unflag", "DSH", "Click to remove from the list of flagged devices")})
                };
                f ? c.unflag(a, g) : c.flag(a, g)
            }
        },
        flag: function (a, d) {
            var c = new a.self();
            var b = a.getWriteData(false, true);
            c.set(b);
            c.save({callback: d})
        },
        unflag: function (a, b) {
            a.destroy({callback: b})
        }
    },
    reload: function () {
        var b = this, c = b.down("#devices-dataview"), a = c.getStore();
        a.load(function () {
            var d = Uni.I18n.translatePlural("overview.widget.flaggedDevices.header", a.count(), "DSH", "<h3>My flagged devices ({0})</h3>");
            b.setTitle(d);
            a.each(function (e) {
                e.set("href", b.router.getRoute("devices/device").buildUrl({mRID: e.getId()}));
                e.set("tooltip", b.tooltipTpl.apply(e.getData(true)))
            });
            c.bindStore(a)
        })
    }
});
Ext.define("Dsh.view.MyFavoriteDeviceGroups", {
    extend: "Ext.panel.Panel",
    alias: "widget.my-favorite-device-groups",
    itemId: "my-favorite-device-groups",
    title: Uni.I18n.translate("myFavoriteDeviceGroups.pageTitle", "DSH", "Select favorite device groups"),
    ui: "large",
    margin: "0 20",
    requires: ["Uni.view.container.EmptyGridContainer", "Uni.view.notifications.NoItemsFoundPanel"],
    initComponent: function () {
        var a = this;
        a.callParent(arguments);
        a.add({
            xtype: "emptygridcontainer",
            grid: {
                xtype: "grid",
                itemId: "my-favorite-device-groups-grid",
                extend: "Uni.view.grid.SelectionGrid",
                store: "Dsh.store.FavoriteDeviceGroups",
                margin: "0 40 0 0",
                disableSelection: true,
                overflowY: "auto",
                maxHeight: 450,
                viewConfig: {markDirty: false},
                tbar: [{
                    xtype: "panel",
                    layout: "hbox",
                    defaults: {style: {marginRight: "20px"}},
                    items: [{
                        xtype: "displayfield",
                        itemId: "selected-groups-summary",
                        margin: "0 20 0 0"
                    }, {xtype: "button", text: "Uncheck all", action: "uncheckall"}]
                }],
                columns: [{
                    xtype: "checkcolumn",
                    dataIndex: "favorite",
                    width: 35
                }, {
                    header: Uni.I18n.translate("myFavoriteDeviceGroups.grid.column.name", "DSH", "Name"),
                    dataIndex: "name",
                    flex: 1
                }, {
                    header: Uni.I18n.translate("myFavoriteDeviceGroups.grid.column.type", "DSH", "Type"),
                    dataIndex: "dynamic",
                    flex: 1,
                    renderer: function (b) {
                        return b ? Uni.I18n.translate("myFavoriteDeviceGroups.grid.type.dynamic", "DSH", "Dynamic") : Uni.I18n.translate("myFavoriteDeviceGroups.grid.type.static", "DSH", "Static")
                    }
                }],
                buttonAlign: "left",
                buttons: [{xtype: "button", text: "Save", action: "save", ui: "action"}, {
                    xtype: "button",
                    text: "Cancel",
                    href: "#/dashboard",
                    ui: "link"
                }]
            },
            emptyComponent: {
                xtype: "no-items-found-panel",
                title: Uni.I18n.translate("workspace.myFavoriteDeviceGroups.empty.title", "DSH", "No device groups found"),
                reasons: [Uni.I18n.translate("workspace.myFavoriteDeviceGroups.empty.list.reason1", "DSH", "No device groups have been defined yet.")],
                stepItems: [{
                    text: Uni.I18n.translate("workspace.myFavoriteDeviceGroups.empty.list.action1", "DSH", "Add device group"),
                    action: "addItem"
                }]
            }
        })
    }
});
Ext.define("Dsh.view.OperatorDashboard", {
    extend: "Ext.panel.Panel",
    requires: ["Dsh.view.widget.HeaderSection", "Dsh.view.widget.Summary", "Dsh.view.widget.CommunicationServers", "Dsh.view.widget.QuickLinks", "Dsh.view.widget.ReadOutsOverTime", "Dsh.view.widget.OpenDataCollectionIssues", "Dsh.view.widget.Overview", "Dsh.view.widget.Breakdown", "Dsh.view.widget.DeviceGroupFilter", "Dsh.view.widget.FavoriteDeviceGroups", "Dsh.view.widget.FlaggedDevices", "Dsh.view.MyFavoriteDeviceGroups"],
    alias: "widget.operator-dashboard",
    itemId: "operator-dashboard",
    autoScroll: true,
    layout: {type: "vbox", align: "stretch"},
    style: {"padding-left": "20px"},
    initComponent: function () {
        var a = this;
        a.items = [{
            xtype: "panel",
            ui: "large",
            title: a.router.getRoute().title,
            tools: [{
                xtype: "toolbar",
                items: ["->", {
                    xtype: "component",
                    itemId: "last-updated-field",
                    width: 150,
                    style: {font: "normal 13px/17px Lato", color: "#686868", "margin-right": "10px"}
                }, {
                    xtype: "button",
                    itemId: "refresh-btn",
                    style: {"background-color": "#71adc7"},
                    text: Uni.I18n.translate("overview.widget.headerSection.refreshBtnTxt", "DSH", "Refresh"),
                    icon: "/apps/sky/resources/images/form/restore.png"
                }]
            }],
            layout: {type: "hbox", align: "stretch"},
            defaults: {style: {marginRight: "20px", padding: "20px"}, flex: 1},
            items: []
        }, {
            xtype: "toolbar",
            margin: "50 0 0 0",
            items: {
                xtype: "device-group-filter",
                hidden: !Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication"]),
                router: a.router
            }
        }, {
            xtype: "panel",
            layout: {type: "vbox", align: "stretch"},
            height: 500,
            defaults: {flex: 1, style: {marginRight: "20px", padding: "20px"}},
            style: {"margin-right": "20px"},
            items: [],
            dockedItems: [{
                xtype: "communication-servers",
                width: 300,
                dock: "right",
                hidden: !Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication"]),
                itemId: "communication-servers",
                router: a.router,
                style: "border-width: 1px !important"
            }]
        }];
        if (Uni.Auth.hasAnyPrivilege(["privilege.view.issue", "privilege.comment.issue", "privilege.close.issue", "privilege.assign.issue", "privilege.action.issue"])) {
            a.items[0].items.push({
                xtype: "open-data-collection-issues",
                itemId: "open-data-collection-issues",
                router: a.router
            })
        }
        if (Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceData", "privilege.view.device", "privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication"])) {
            a.items[0].items.push({xtype: "flagged-devices", itemId: "flagged-devices", router: a.router})
        }
        a.items[0].items.push({xtype: "favorite-device-groups", itemId: "favorite-device-groups"});
        if (Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication"])) {
            a.items[2].items.push({
                xtype: "summary",
                itemId: "connection-summary",
                wTitle: Uni.I18n.translate("dashboard.widget.connections.title", "DSH", "Active connections"),
                router: a.router,
                parent: "connections",
                buttonAlign: "left",
                buttons: [{
                    text: Uni.I18n.translate("dashboard.widget.connections.link", "DSH", "View connections overview"),
                    itemId: "lnk-connections-overview",
                    ui: "link",
                    href: typeof a.router.getRoute("workspace/connections") !== "undefined" ? a.router.getRoute("workspace/connections").buildUrl(null, a.router.queryParams) : ""
                }]
            }, {
                xtype: "summary",
                itemId: "communication-summary",
                wTitle: Uni.I18n.translate("dashboard.widget.communications.title", "DSH", "Active communications"),
                parent: "communications",
                router: a.router,
                buttonAlign: "left",
                buttons: [{
                    text: Uni.I18n.translate("dashboard.widget.communications.link", "DSH", "View communications overview"),
                    itemId: "lnk-communications-overview",
                    ui: "link",
                    href: typeof a.router.getRoute("workspace/communications") !== "undefined" ? a.router.getRoute("workspace/communications").buildUrl(null, a.router.queryParams) : ""
                }]
            })
        }
        this.callParent(arguments)
    }
});
Ext.define("Dsh.model.connection.OverviewDashboard", {
    extend: "Dsh.model.connection.Overview",
    proxy: {type: "ajax", url: "/api/dsr/connectionoverview/widget"}
});
Ext.define("Dsh.model.communication.OverviewDashboard", {
    extend: "Dsh.model.communication.Overview",
    proxy: {type: "ajax", url: "/api/dsr/communicationoverview/widget"}
});
Ext.define("Dsh.model.TopMyIssue", {
    extend: "Ext.data.Model",
    fields: [{name: "id", type: "int"}, {name: "title", type: "string"}, {name: "dueDate", type: "auto"}]
});
Ext.define("Dsh.model.AssignedToMeIssues", {
    extend: "Ext.data.Model",
    requires: ["Dsh.model.TopMyIssue"],
    fields: [{name: "total", type: "int"}, {name: "filter", type: "auto"}],
    hasMany: [{model: "Dsh.model.TopMyIssue", name: "topMyIssues"}]
});
Ext.define("Dsh.model.UnassignedIssues", {
    extend: "Ext.data.Model",
    fields: [{name: "total", type: "int"}, {name: "filter", type: "auto"}]
});
Ext.define("Dsh.model.opendatacollectionissues.Overview", {
    extend: "Ext.data.Model",
    requires: ["Dsh.model.AssignedToMeIssues", "Dsh.model.UnassignedIssues"],
    hasOne: [{
        model: "Dsh.model.UnassignedIssues",
        associationKey: "unassignedIssues",
        name: "unassignedIssues",
        getterName: "getUnassignedIssues",
        setterName: "setUnassignedIssues"
    }, {
        model: "Dsh.model.AssignedToMeIssues",
        associationKey: "assignedToMeIssues",
        name: "assignedToMeIssues",
        getterName: "getAssignedToMeIssues",
        setterName: "setAssignedToMeIssues"
    }],
    proxy: {type: "ajax", url: "/api/dsr/myopenissuesoverview"}
});
Ext.define("Dsh.model.DeviceGroup", {
    extend: "Ext.data.Model",
    proxy: "memory",
    fields: [{name: "id", type: "int"}, {name: "mRID", type: "string"}, {
        name: "name",
        type: "string"
    }, {name: "dynamic", type: "boolean"}, {name: "favorite", type: "boolean"}, {name: "criteria"}]
});
Ext.define("Dsh.store.FavoriteDeviceGroups", {
    extend: "Ext.data.Store",
    storeId: "FavoriteDeviceGroups",
    requires: ["Dsh.model.DeviceGroup"],
    model: "Dsh.model.DeviceGroup",
    proxy: {
        type: "ajax",
        url: "../../api/dsr/favoritedevicegroups",
        reader: {type: "json", root: "favoriteDeviceGroups"},
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
Ext.define("Dsh.model.FlaggedDevice", {
    extend: "Ext.data.Model",
    idProperty: "mRID",
    requires: ["Mdc.model.DeviceLabel"],
    fields: [{name: "mRID", type: "string"}, {name: "serialNumber", type: "string"}, {
        name: "deviceTypeName",
        type: "string"
    }],
    hasOne: {
        model: "Mdc.model.DeviceLabel",
        name: "deviceLabelInfo",
        associationKey: "deviceLabelInfo",
        getterName: "getLabel"
    }
});
Ext.define("Dsh.store.FlaggedDevices", {
    extend: "Ext.data.Store",
    storeId: "FlaggedDevices",
    requires: ["Dsh.model.FlaggedDevice"],
    model: "Dsh.model.FlaggedDevice",
    autoLoad: false,
    proxy: {
        type: "ajax",
        url: "/api/dsr/mylabeleddevices?category=mdc.label.category.favorites",
        pageParam: false,
        limitParam: false,
        reader: {type: "json", root: "myLabeledDevices"}
    }
});
Ext.define("Dsh.controller.OperatorDashboard", {
    extend: "Ext.app.Controller",
    models: ["Dsh.model.connection.Overview", "Dsh.model.connection.OverviewDashboard", "Dsh.model.communication.Overview", "Dsh.model.communication.OverviewDashboard", "Dsh.model.opendatacollectionissues.Overview"],
    stores: ["CommunicationServerInfos", "Dsh.store.CombineStore", "Dsh.store.ConnectionResultsStore", "Dsh.store.FavoriteDeviceGroups", "Dsh.store.FlaggedDevices"],
    views: ["Dsh.view.OperatorDashboard"],
    refs: [{ref: "dashboard", selector: "#operator-dashboard"}, {
        ref: "header",
        selector: "operator-dashboard #header-section"
    }, {ref: "connectionSummary", selector: "operator-dashboard #connection-summary"}, {
        ref: "communicationSummary",
        selector: "operator-dashboard #communication-summary"
    }, {ref: "summary", selector: " operator-dashboard #summary"}, {
        ref: "communicationServers",
        selector: "operator-dashboard #communication-servers"
    }, {ref: "flaggedDevices", selector: "operator-dashboard #flagged-devices"}, {
        ref: "issuesWidget",
        selector: "operator-dashboard #open-data-collection-issues"
    }, {
        ref: "favoriteDeviceGroupsView",
        selector: "operator-dashboard #favorite-device-groups dataview"
    }, {ref: "favoriteDeviceGroups", selector: "#my-favorite-device-groups"}, {
        ref: "favoriteDeviceGroupsGrid",
        selector: "#my-favorite-device-groups-grid"
    }, {ref: "summaryOfSelected", selector: "#selected-groups-summary"}, {
        ref: "uncheckAllBtn",
        selector: "#my-favorite-device-groups button[action=uncheckall]"
    }],
    init: function () {
        this.control({
            "#operator-dashboard #refresh-btn": {click: this.loadData},
            "#my-favorite-device-groups-grid": {render: this.afterFavoriteDeviceGroupsGridRender},
            "#my-favorite-device-groups-grid checkcolumn": {checkchange: this.onFavoriteGroupsGridSelectionChange},
            "#my-favorite-device-groups button[action=uncheckall]": {click: this.uncheckAllSelectedGroups},
            "#my-favorite-device-groups button[action=save]": {click: this.saveFavoriteGroups},
            "#my-favorite-device-groups [action=addItem]": {
                click: function () {
                    var a = this.getController("Uni.controller.history.Router");
                    a.getRoute("devices/devicegroups/add").forward()
                }
            }
        })
    },
    showMyFavorieDeviceGroups: function () {
        this.getApplication().fireEvent("changecontentevent", Ext.widget("my-favorite-device-groups"))
    },
    afterFavoriteDeviceGroupsGridRender: function () {
        var a = this;
        a.getFavoriteDeviceGroupsGrid().getStore().load({
            params: {includeAllGroups: true}, callback: function () {
                a.onFavoriteGroupsGridSelectionChange()
            }
        })
    },
    onFavoriteGroupsGridSelectionChange: function () {
        var d = this.getSummaryOfSelected(), b = this.getFavoriteDeviceGroupsGrid().getStore(), a = b.queryBy(function (e) {
            return e.get("favorite") === true
        }), c = a.items.length;
        d.setValue(c > 0 ? Uni.I18n.translatePlural("myFavoriteDeviceGroups.summarySelectedTpl", c, "DSH", "{0} device groups selected") : Uni.I18n.translate("myFavoriteDeviceGroups.summaryTplNoSelected", "DSH", "No device groups selected"));
        this.getUncheckAllBtn().setDisabled(c < 1)
    },
    uncheckAllSelectedGroups: function () {
        this.getFavoriteDeviceGroupsGrid().getStore().each(function (a) {
            a.set("favorite", false)
        });
        this.onFavoriteGroupsGridSelectionChange()
    },
    saveFavoriteGroups: function () {
        var e = this, d = [], c = e.getController("Uni.controller.history.Router"), b = e.getFavoriteDeviceGroupsGrid().getStore(), a = b.queryBy(function (f) {
            return f.get("favorite") === true
        });
        a.each(function (f) {
            d.push(f.get("id"))
        });
        e.getFavoriteDeviceGroups().setLoading(true);
        Ext.Ajax.request({
            url: b.proxy.url, method: "PUT", jsonData: {ids: d}, success: function () {
                c.getRoute("dashboard").forward()
            }, callback: function () {
                e.getFavoriteDeviceGroups().setLoading(false)
            }
        })
    },
    showOverview: function () {
        var a = this.getController("Uni.controller.history.Router");
        this.getApplication().fireEvent("changecontentevent", Ext.widget("operator-dashboard", {router: a}));
        this.loadData()
    },
    loadData: function () {
        var e = this, c = e.getDashboard(), g = c.down("#last-updated-field");
        if (Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication", "privilege.view.device", "privilege.view.issue", "privilege.comment.issue", "privilege.close.issue", "privilege.assign.issue", "privilege.action.issue"])) {
            var a = e.getModel("Dsh.model.connection.OverviewDashboard"), d = e.getModel("Dsh.model.communication.OverviewDashboard"), f = e.getModel("Dsh.model.opendatacollectionissues.Overview"), h = e.getIssuesWidget(), b = this.getController("Uni.controller.history.Router");
            if (Uni.Auth.hasAnyPrivilege(["privilege.view.device"])) {
                e.getFlaggedDevices().reload()
            }
            d.getProxy().url = "/api/dsr/communicationoverview/widget";
            a.getProxy().url = "/api/dsr/connectionoverview/widget";
            if (Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication"])) {
                a.setFilter(b.filter);
                d.setFilter(b.filter);
                c.setLoading();
                e.getCommunicationServers().reload();
                a.load(null, {
                    success: function (i) {
                        if (e.getConnectionSummary()) {
                            e.getConnectionSummary().setRecord(i.getSummary())
                        }
                    }, callback: function () {
                        d.load(null, {
                            success: function (i) {
                                if (e.getCommunicationSummary()) {
                                    e.getCommunicationSummary().setRecord(i.getSummary())
                                }
                            }, callback: function () {
                                if (g) {
                                    g.update("Last updated at " + Uni.DateTime.formatTimeShort(new Date()))
                                }
                                c.setLoading(false)
                            }
                        })
                    }
                })
            }
            if (Uni.Auth.hasAnyPrivilege(["privilege.view.issue", "privilege.comment.issue", "privilege.close.issue", "privilege.assign.issue", "privilege.action.issue"])) {
                h.setLoading();
                f.load(null, {
                    success: function (i) {
                        h.setRecord(i);
                        h.setLoading(false)
                    }
                })
            }
        }
    }
});
Ext.define("Dsh.model.OverviewFilter", {
    extend: "Ext.data.Model",
    requires: ["Uni.data.proxy.QueryStringProxy"],
    proxy: {type: "querystring", root: "filter"},
    fields: [{name: "id", persist: false}, {name: "deviceGroup", type: "auto"}]
});
Ext.define("Dsh.controller.history.Workspace", {
    extend: "Uni.controller.history.Converter",
    rootToken: "workspace",
    previousPath: "",
    currentPath: null,
    requires: ["Dsh.model.OverviewFilter", "Dsh.model.Filter"],
    routeConfig: {
        workspace: {
            title: "Workspace",
            route: "workspace",
            disabled: true,
            items: {
                connections: {
                    title: Uni.I18n.translate("title.connections.overview", "DSH", "Connections overview"),
                    route: "connections",
                    controller: "Dsh.controller.ConnectionOverview",
                    privileges: ["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication", "privilege.view.device"],
                    action: "showOverview",
                    filter: "Dsh.model.OverviewFilter",
                    items: {
                        details: {
                            title: Uni.I18n.translate("title.connections", "DSH", "Connections"),
                            route: "details",
                            controller: "Dsh.controller.Connections",
                            privileges: ["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication", "privilege.view.device"],
                            action: "showOverview",
                            filter: "Dsh.model.Filter"
                        }
                    }
                },
                communications: {
                    title: Uni.I18n.translate("title.communications.overview", "DSH", "Communications overview"),
                    route: "communications",
                    controller: "Dsh.controller.CommunicationOverview",
                    privileges: ["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication", "privilege.view.device"],
                    action: "showOverview",
                    filter: "Dsh.model.OverviewFilter",
                    items: {
                        details: {
                            title: Uni.I18n.translate("title.communications", "DSH", "Communications"),
                            route: "details",
                            controller: "Dsh.controller.Communications",
                            privileges: ["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication", "privilege.view.device"],
                            action: "showOverview",
                            filter: "Dsh.model.Filter"
                        }
                    }
                }
            }
        },
        dashboard: {
            title: Uni.I18n.translate("title.dashboard", "DSH", "Dashboard"),
            route: "dashboard",
            controller: "Dsh.controller.OperatorDashboard",
            action: "showOverview",
            filter: "Dsh.model.OverviewFilter",
            items: {
                selectfavoritedevicegroups: {
                    title: Uni.I18n.translate("title.selectFavoriteDeviceGroups", "DSH", "Select favorite device groups"),
                    route: "selectfavoritedevicegroups",
                    controller: "Dsh.controller.OperatorDashboard",
                    action: "showMyFavorieDeviceGroups"
                }
            }
        }
    }
});
Ext.define("Dsh.store.filter.CompletionCodes", {
    extend: "Ext.data.Store",
    fields: ["localizedValue", "completionCode"],
    proxy: {type: "rest", url: "/api/dsr/field/completioncodes", reader: {type: "json", root: "completionCodes"}}
});
Ext.define("Dsh.store.filter.DeviceGroup", {
    extend: "Ext.data.Store",
    requires: ["Dsh.model.DeviceGroup"],
    model: "Dsh.model.DeviceGroup",
    proxy: {
        type: "rest",
        url: "/api/ddr/devicegroups",
        pageParam: undefined,
        limitParam: undefined,
        startParam: undefined,
        reader: {type: "json", root: "devicegroups"}
    }
});
Ext.define("Dsh.store.CommunicationResultsStore", {
    extend: "Uni.data.store.Filterable",
    model: "Dsh.model.ConnectionResults",
    autoLoad: false,
    remoteFilter: true,
    proxy: {
        type: "ajax",
        url: "/api/dsr/communicationheatmap",
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {type: "json", root: "heatMap"}
    }
});
Ext.define("Dsh.controller.Main", {
    extend: "Ext.app.Controller",
    requires: ["Ext.window.Window", "Uni.controller.Navigation", "Uni.controller.Configuration", "Uni.controller.history.EventBus", "Uni.model.PortalItem", "Uni.store.PortalItems", "Uni.store.MenuItems", "Dsh.util.FilterStoreHydrator", "Dsh.model.Filterable", "Dsh.model.Kpi", "Dsh.model.Series", "Dsh.controller.OperatorDashboard"],
    controllers: ["Dsh.controller.history.Workspace", "Dsh.controller.BaseController", "Dsh.controller.CommunicationOverview", "Dsh.controller.ConnectionOverview", "Dsh.controller.OperatorDashboard", "Dsh.controller.Connections", "Dsh.controller.Communications"],
    stores: ["Dsh.store.ConnectionTasks", "Dsh.store.CommunicationTasks", "Dsh.store.filter.CurrentState", "Dsh.store.filter.LatestStatus", "Dsh.store.filter.LatestResult", "Dsh.store.filter.CommPortPool", "Dsh.store.filter.ConnectionType", "Dsh.store.filter.DeviceType", "Dsh.store.filter.CompletionCodes", "Dsh.store.filter.DeviceGroup", "Dsh.store.ConnectionResultsStore", "Dsh.store.CommunicationResultsStore", "Dsh.store.CombineStore"],
    init: function () {
        this.initMenu()
    },
    initMenu: function () {
        var c = this, b = c.getController("Uni.controller.history.Router"), d = c.getController("Dsh.controller.history.Workspace");
        var a = b.getRoute("dashboard");
        Uni.store.MenuItems.add(Ext.create("Uni.model.MenuItem", {
            text: a.title,
            glyph: "home",
            portal: "dashboard",
            index: 0
        }));
        if (Uni.Auth.hasAnyPrivilege(["privilege.administrate.deviceCommunication", "privilege.operate.deviceCommunication", "privilege.view.device"])) {
            Uni.store.MenuItems.add(Ext.create("Uni.model.MenuItem", {
                text: "Workspace",
                glyph: "workspace",
                portal: "workspace",
                index: 30
            }));
            Uni.store.PortalItems.add(Ext.create("Uni.model.PortalItem", {
                title: "Data communication",
                portal: "workspace",
                route: "datacommunication",
                items: [{
                    text: Uni.I18n.translate("title.connections", "DSH", "Connections"),
                    href: b.getRoute("workspace/connections/details").buildUrl()
                }, {
                    text: Uni.I18n.translate("title.connections.overview", "DSH", "Connections overview"),
                    href: b.getRoute("workspace/connections").buildUrl()
                }, {
                    text: Uni.I18n.translate("title.communications", "DSH", "Communications"),
                    href: b.getRoute("workspace/communications/details").buildUrl()
                }, {
                    text: Uni.I18n.translate("title.communications.overview", "DSH", "Communications overview"),
                    href: b.getRoute("workspace/communications").buildUrl()
                }]
            }))
        }
    }
});
Ext.define("Dsh.model.Configuration", {
    extend: "Ext.data.Model",
    fields: [{name: "id", type: "string"}, {name: "title", type: "string"}]
});
Ext.define("Dsh.model.Result", {
    extend: "Ext.data.Model",
    fields: [{name: "id", type: "string"}, {name: "displayName", type: "string"}, {name: "count", type: "int"}]
});
Ext.define("Dsh.model.TimeInfo", {
    extend: "Ext.data.Model",
    fields: [{name: "count", type: "int"}, {name: "timeUnit", type: "int"}]
});