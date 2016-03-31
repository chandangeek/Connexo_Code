(function (g) {
    function y(c, a, b) {
        var e, c = c.rgba, a = a.rgba;
        e = a[3] !== 1 || c[3] !== 1;
        (!a.length || !c.length) && Highcharts.error(23);
        return (e ? "rgba(" : "rgb(") + Math.round(a[0] + (c[0] - a[0]) * (1 - b)) + "," + Math.round(a[1] + (c[1] - a[1]) * (1 - b)) + "," + Math.round(a[2] + (c[2] - a[2]) * (1 - b)) + (e ? "," + (a[3] + (c[3] - a[3]) * (1 - b)) : "") + ")"
    }

    var r = function () {
    }, n = g.getOptions(), j = g.each, k = g.extend, z = g.format, s = g.pick, o = g.wrap, h = g.Chart, m = g.seriesTypes, t = m.pie, l = m.column, u = HighchartsAdapter.fireEvent, v = HighchartsAdapter.inArray, p = [], w = 1;
    j(["fill",
        "stroke"], function (c) {
        HighchartsAdapter.addAnimSetter(c, function (a) {
            a.elem.attr(c, y(g.Color(a.start), g.Color(a.end), a.pos))
        })
    });
    k(n.lang, {drillUpText: "◁ Back to {series.name}"});
    n.drilldown = {
        activeAxisLabelStyle: {
            cursor: "pointer",
            color: "#0d233a",
            fontWeight: "bold",
            textDecoration: "underline"
        },
        activeDataLabelStyle: {cursor: "pointer", color: "#0d233a", fontWeight: "bold", textDecoration: "underline"},
        animation: {duration: 500},
        drillUpButton: {position: {align: "right", x: -10, y: 10}}
    };
    g.SVGRenderer.prototype.Element.prototype.fadeIn =
        function (c) {
            this.attr({
                opacity: 0.1,
                visibility: "inherit"
            }).animate({opacity: s(this.newOpacity, 1)}, c || {duration: 250})
        };
    h.prototype.addSeriesAsDrilldown = function (c, a) {
        this.addSingleSeriesAsDrilldown(c, a);
        this.applyDrilldown()
    };
    h.prototype.addSingleSeriesAsDrilldown = function (c, a) {
        var b = c.series, e = b.xAxis, f = b.yAxis, d;
        d = c.color || b.color;
        var i, g = [], x = [], h;
        h = b.options._levelNumber || 0;
        a = k({color: d, _ddSeriesId: w++}, a);
        i = v(c, b.points);
        j(b.chart.series, function (a) {
            if (a.xAxis === e)g.push(a), a.options._ddSeriesId =
                a.options._ddSeriesId || w++, a.options._colorIndex = a.userOptions._colorIndex, x.push(a.options), a.options._levelNumber = a.options._levelNumber || h
        });
        d = {
            levelNumber: h,
            seriesOptions: b.options,
            levelSeriesOptions: x,
            levelSeries: g,
            shapeArgs: c.shapeArgs,
            bBox: c.graphic ? c.graphic.getBBox() : {},
            color: d,
            lowerSeriesOptions: a,
            pointOptions: b.options.data[i],
            pointIndex: i,
            oldExtremes: {xMin: e && e.userMin, xMax: e && e.userMax, yMin: f && f.userMin, yMax: f && f.userMax}
        };
        if (!this.drilldownLevels)this.drilldownLevels = [];
        this.drilldownLevels.push(d);
        d = d.lowerSeries = this.addSeries(a, !1);
        d.options._levelNumber = h + 1;
        if (e)e.oldPos = e.pos, e.userMin = e.userMax = null, f.userMin = f.userMax = null;
        if (b.type === d.type)d.animate = d.animateDrilldown || r, d.options.animation = !0
    };
    h.prototype.applyDrilldown = function () {
        var c = this.drilldownLevels, a;
        if (c && c.length > 0)a = c[c.length - 1].levelNumber, j(this.drilldownLevels, function (b) {
            b.levelNumber === a && j(b.levelSeries, function (b) {
                b.options && b.options._levelNumber === a && b.remove(!1)
            })
        });
        this.redraw();
        this.showDrillUpButton()
    };
    h.prototype.getDrilldownBackText =
        function () {
            var c = this.drilldownLevels;
            if (c && c.length > 0)return c = c[c.length - 1], c.series = c.seriesOptions, z(this.options.lang.drillUpText, c)
        };
    h.prototype.showDrillUpButton = function () {
        var c = this, a = this.getDrilldownBackText(), b = c.options.drilldown.drillUpButton, e, f;
        this.drillUpButton ? this.drillUpButton.attr({text: a}).align() : (f = (e = b.theme) && e.states, this.drillUpButton = this.renderer.button(a, null, null, function () {
            c.drillUp()
        }, e, f && f.hover, f && f.select).attr({align: b.position.align, zIndex: 9}).add().align(b.position,
            !1, b.relativeTo || "plotBox"))
    };
    h.prototype.drillUp = function () {
        for (var c = this, a = c.drilldownLevels, b = a[a.length - 1].levelNumber, e = a.length, f = c.series, d, i, g, h, k = function (a) {
            var b;
            j(f, function (c) {
                c.options._ddSeriesId === a._ddSeriesId && (b = c)
            });
            b = b || c.addSeries(a, !1);
            if (b.type === g.type && b.animateDrillupTo)b.animate = b.animateDrillupTo;
            a === i.seriesOptions && (h = b)
        }; e--;)if (i = a[e], i.levelNumber === b) {
            a.pop();
            g = i.lowerSeries;
            if (!g.chart)for (d = f.length; d--;)if (f[d].options.id === i.lowerSeriesOptions.id) {
                g = f[d];
                break
            }
            g.xData =
                [];
            j(i.levelSeriesOptions, k);
            u(c, "drillup", {seriesOptions: i.seriesOptions});
            if (h.type === g.type)h.drilldownLevel = i, h.options.animation = c.options.drilldown.animation, g.animateDrillupFrom && g.chart && g.animateDrillupFrom(i);
            h.options._levelNumber = b;
            g.remove(!1);
            if (h.xAxis)d = i.oldExtremes, h.xAxis.setExtremes(d.xMin, d.xMax, !1), h.yAxis.setExtremes(d.yMin, d.yMax, !1)
        }
        this.redraw();
        this.drilldownLevels.length === 0 ? this.drillUpButton = this.drillUpButton.destroy() : this.drillUpButton.attr({text: this.getDrilldownBackText()}).align();
        p.length = []
    };
    l.prototype.supportsDrilldown = !0;
    l.prototype.animateDrillupTo = function (c) {
        if (!c) {
            var a = this, b = a.drilldownLevel;
            j(this.points, function (a) {
                a.graphic && a.graphic.hide();
                a.dataLabel && a.dataLabel.hide();
                a.connector && a.connector.hide()
            });
            setTimeout(function () {
                a.points && j(a.points, function (a, c) {
                    var d = c === (b && b.pointIndex) ? "show" : "fadeIn", i = d === "show" ? !0 : void 0;
                    if (a.graphic)a.graphic[d](i);
                    if (a.dataLabel)a.dataLabel[d](i);
                    if (a.connector)a.connector[d](i)
                })
            }, Math.max(this.chart.options.drilldown.animation.duration -
                50, 0));
            this.animate = r
        }
    };
    l.prototype.animateDrilldown = function (c) {
        var a = this, b = this.chart.drilldownLevels, e, f = this.chart.options.drilldown.animation, d = this.xAxis;
        if (!c)j(b, function (b) {
            if (a.options._ddSeriesId === b.lowerSeriesOptions._ddSeriesId)e = b.shapeArgs, e.fill = b.color
        }), e.x += s(d.oldPos, d.pos) - d.pos, j(this.points, function (a) {
            a.graphic && a.graphic.attr(e).animate(k(a.shapeArgs, {fill: a.color}), f);
            a.dataLabel && a.dataLabel.fadeIn(f)
        }), this.animate = null
    };
    l.prototype.animateDrillupFrom = function (c) {
        var a =
            this.chart.options.drilldown.animation, b = this.group, e = this;
        j(e.trackerGroups, function (a) {
            if (e[a])e[a].on("mouseover")
        });
        delete this.group;
        j(this.points, function (e) {
            var d = e.graphic, i = function () {
                d.destroy();
                b && (b = b.destroy())
            };
            d && (delete e.graphic, a ? d.animate(k(c.shapeArgs, {fill: c.color}), g.merge(a, {complete: i})) : (d.attr(c.shapeArgs), i()))
        })
    };
    t && k(t.prototype, {
        supportsDrilldown: !0,
        animateDrillupTo: l.prototype.animateDrillupTo,
        animateDrillupFrom: l.prototype.animateDrillupFrom,
        animateDrilldown: function (c) {
            var a =
                this.chart.drilldownLevels[this.chart.drilldownLevels.length - 1], b = this.chart.options.drilldown.animation, e = a.shapeArgs, f = e.start, d = (e.end - f) / this.points.length;
            if (!c)j(this.points, function (c, h) {
                c.graphic.attr(g.merge(e, {
                    start: f + h * d,
                    end: f + (h + 1) * d,
                    fill: a.color
                }))[b ? "animate" : "attr"](k(c.shapeArgs, {fill: c.color}), b)
            }), this.animate = null
        }
    });
    g.Point.prototype.doDrilldown = function (c, a) {
        for (var b = this.series.chart, e = b.options.drilldown, f = (e.series || []).length, d; f-- && !d;)e.series[f].id === this.drilldown && v(this.drilldown,
            p) === -1 && (d = e.series[f], p.push(this.drilldown));
        u(b, "drilldown", {
            point: this,
            seriesOptions: d,
            category: a,
            points: a !== void 0 && this.series.xAxis.ticks[a].label.ddPoints.slice(0)
        });
        d && (c ? b.addSingleSeriesAsDrilldown(this, d) : b.addSeriesAsDrilldown(this, d))
    };
    g.Axis.prototype.drilldownCategory = function (c) {
        j(this.ticks[c].label.ddPoints, function (a) {
            a.series && a.series.visible && a.doDrilldown && a.doDrilldown(!0, c)
        });
        this.chart.applyDrilldown()
    };
    o(g.Point.prototype, "init", function (c, a, b, e) {
        var f = c.call(this, a, b,
            e), c = a.chart;
        if (b = (b = a.xAxis && a.xAxis.ticks[e]) && b.label) {
            if (!b.ddPoints)b.ddPoints = [];
            if (b.levelNumber !== a.options._levelNumber)b.ddPoints.length = 0
        }
        if (f.drilldown) {
            if (g.addEvent(f, "click", function () {
                    f.doDrilldown()
                }), b) {
                if (!b.basicStyles)b.basicStyles = g.merge(b.styles);
                b.addClass("highcharts-drilldown-axis-label").css(c.options.drilldown.activeAxisLabelStyle).on("click", function () {
                    a.xAxis.drilldownCategory(e)
                });
                b.ddPoints.push(f);
                b.levelNumber = a.options._levelNumber
            }
        } else if (b && b.basicStyles && b.levelNumber !==
            a.options._levelNumber)b.styles = {}, b.css(b.basicStyles), b.on("click", null);
        return f
    });
    o(g.Series.prototype, "drawDataLabels", function (c) {
        var a = this.chart.options.drilldown.activeDataLabelStyle;
        c.call(this);
        j(this.points, function (b) {
            if (b.drilldown && b.dataLabel)b.dataLabel.attr({"class": "highcharts-drilldown-data-label"}).css(a).on("click", function () {
                b.doDrilldown()
            })
        })
    });
    var q, n = function (c) {
        c.call(this);
        j(this.points, function (a) {
            a.drilldown && a.graphic && a.graphic.attr({"class": "highcharts-drilldown-point"}).css({cursor: "pointer"})
        })
    };
    for (q in m)m[q].prototype.supportsDrilldown && o(m[q].prototype, "drawTracker", n)
})(Highcharts);
