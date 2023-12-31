/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

(function (c) {
    typeof module === "object" && module.exports ? module.exports = c : c(Highcharts)
})(function (c) {
    function w(a, b, d, g, f) {
        for (var f = f || 0, g = g || x, O = f + g, c = !0; c && f < O && f < a.length;)c = b(a[f], f), f += 1;
        c && (f < a.length ? setTimeout(function () {
            w(a, b, d, g, f)
        }) : d && d())
    }

    var r = c.win.document, P = function () {
    }, Q = c.Color, l = c.Series, e = c.seriesTypes, k = c.each, n = c.extend, R = c.addEvent, S = c.fireEvent, T = c.merge, U = c.pick, j = c.wrap, p = c.getOptions().plotOptions, x = 5E4, y;
    k(["area", "arearange", "column", "line", "scatter"], function (a) {
        if (p[a])p[a].boostThreshold =
            5E3
    });
    k(["translate", "generatePoints", "drawTracker", "drawPoints", "render"], function (a) {
        function b(b) {
            var g = this.options.stacking && (a === "translate" || a === "generatePoints");
            if ((this.processedXData || this.options.data).length < (this.options.boostThreshold || Number.MAX_VALUE) || g) {
                if (a === "render" && this.image)this.image.attr({href: ""}), this.animate = null;
                b.call(this)
            } else if (this[a + "Canvas"])this[a + "Canvas"]()
        }

        j(l.prototype, a, b);
        a === "translate" && (e.column && j(e.column.prototype, a, b), e.arearange && j(e.arearange.prototype,
            a, b))
    });
    j(l.prototype, "getExtremes", function (a) {
        this.hasExtremes() || a.apply(this, Array.prototype.slice.call(arguments, 1))
    });
    j(l.prototype, "setData", function (a) {
        this.hasExtremes(!0) || a.apply(this, Array.prototype.slice.call(arguments, 1))
    });
    j(l.prototype, "processData", function (a) {
        this.hasExtremes(!0) || a.apply(this, Array.prototype.slice.call(arguments, 1))
    });
    c.extend(l.prototype, {
        pointRange: 0, hasExtremes: function (a) {
            var b = this.options, d = this.xAxis && this.xAxis.options, g = this.yAxis && this.yAxis.options;
            return b.data.length >
                (b.boostThreshold || Number.MAX_VALUE) && typeof g.min === "number" && typeof g.max === "number" && (!a || typeof d.min === "number" && typeof d.max === "number")
        }, destroyGraphics: function () {
            var a = this, b = this.points, d, g;
            if (b)for (g = 0; g < b.length; g += 1)if ((d = b[g]) && d.graphic)d.graphic = d.graphic.destroy();
            k(["graph", "area", "tracker"], function (b) {
                a[b] && (a[b] = a[b].destroy())
            })
        }, getContext: function () {
            var a = this.chart, b = a.plotWidth, d = a.plotHeight, g = this.ctx, f = function (a, b, d, g, f, c, e) {
                a.call(this, d, b, g, f, c, e)
            };
            this.canvas ? g.clearRect(0,
                0, b, d) : (this.canvas = r.createElement("canvas"), this.image = a.renderer.image("", 0, 0, b, d).add(this.group), this.ctx = g = this.canvas.getContext("2d"), a.inverted && k(["moveTo", "lineTo", "rect", "arc"], function (a) {
                j(g, a, f)
            }));
            this.canvas.setAttribute("width", b);
            this.canvas.setAttribute("height", d);
            this.image.attr({width: b, height: d});
            return g
        }, canvasToSVG: function () {
            this.image.attr({href: this.canvas.toDataURL("image/png")})
        }, cvsLineTo: function (a, b, d) {
            a.lineTo(b, d)
        }, renderCanvas: function () {
            var a = this, b = a.options,
                d = a.chart, g = this.xAxis, f = this.yAxis, c, e = 0, j = a.processedXData, l = a.processedYData, k = b.data, i = g.getExtremes(), p = i.min, r = i.max, i = f.getExtremes(), V = i.min, W = i.max, z = {}, s, X = !!a.sampling, A, B = b.marker && b.marker.radius, C = this.cvsDrawPoint, D = b.lineWidth ? this.cvsLineTo : !1, E = B <= 1 ? this.cvsMarkerSquare : this.cvsMarkerCircle, Y = b.enableMouseTracking !== !1, F, i = b.threshold, m = f.getThreshold(i), G = typeof i === "number", H = m, Z = this.fill, I = a.pointArrayMap && a.pointArrayMap.join(",") === "low,high", J = !!b.stacking, $ = a.cropStart ||
                    0, i = d.options.loading, aa = a.requireSorting, K, ba = b.connectNulls, L = !j, t, u, o, q, ca = a.fillOpacity ? (new Q(a.color)).setOpacity(U(b.fillOpacity, 0.75)).get() : a.color, M = function () {
                    Z ? (c.fillStyle = ca, c.fill()) : (c.strokeStyle = a.color, c.lineWidth = b.lineWidth, c.stroke())
                }, N = function (a, b, d) {
                    e === 0 && c.beginPath();
                    K ? c.moveTo(a, b) : C ? C(c, a, b, d, F) : D ? D(c, a, b) : E && E(c, a, b, B);
                    e += 1;
                    e === 1E3 && (M(), e = 0);
                    F = {clientX: a, plotY: b, yBottom: d}
                }, v = function (a, b, c) {
                    Y && !z[a + "," + b] && (z[a + "," + b] = !0, d.inverted && (a = g.len - a, b = f.len - b), A.push({
                        clientX: a,
                        plotX: a, plotY: b, i: $ + c
                    }))
                };
            (this.points || this.graph) && this.destroyGraphics();
            a.plotGroup("group", "series", a.visible ? "visible" : "hidden", b.zIndex, d.seriesGroup);
            a.getAttribs();
            a.markerGroup = a.group;
            R(a, "destroy", function () {
                a.markerGroup = null
            });
            A = this.points = [];
            c = this.getContext();
            a.buildKDTree = P;
            if (k.length > 99999)d.options.loading = T(i, {
                labelStyle: {
                    backgroundColor: "rgba(255,255,255,0.75)",
                    padding: "1em",
                    borderRadius: "0.5em"
                }, style: {backgroundColor: "none", opacity: 1}
            }), clearTimeout(y), d.showLoading("Drawing..."),
                d.options.loading = i;
            w(J ? a.data : j || k, function (b, c) {
                var e, h, j, i, k = typeof d.index === "undefined", n = !0;
                if (!k) {
                    L ? (e = b[0], h = b[1]) : (e = b, h = l[c]);
                    if (I)L && (h = b.slice(1, 3)), i = h[0], h = h[1]; else if (J)e = b.x, h = b.stackY, i = h - b.y;
                    j = h === null;
                    aa || (n = h >= V && h <= W);
                    if (!j && e >= p && e <= r && n)if (e = Math.round(g.toPixels(e, !0)), X) {
                        if (o === void 0 || e === s) {
                            I || (i = h);
                            if (q === void 0 || h > u)u = h, q = c;
                            if (o === void 0 || i < t)t = i, o = c
                        }
                        e !== s && (o !== void 0 && (h = f.toPixels(u, !0), m = f.toPixels(t, !0), N(e, G ? Math.min(h, H) : h, G ? Math.max(m, H) : m), v(e, h, q), m !== h && v(e,
                            m, o)), o = q = void 0, s = e)
                    } else h = Math.round(f.toPixels(h, !0)), N(e, h, m), v(e, h, c);
                    K = j && !ba;
                    c % x === 0 && a.canvasToSVG()
                }
                return !k
            }, function () {
                var b = d.loadingDiv, c = d.loadingShown;
                M();
                a.canvasToSVG();
                S(a, "renderedCanvas");
                if (c)n(b.style, {
                    transition: "opacity 250ms",
                    opacity: 0
                }), d.loadingShown = !1, y = setTimeout(function () {
                    b.parentNode && b.parentNode.removeChild(b);
                    d.loadingDiv = d.loadingSpan = null
                }, 250);
                a.directTouch = !1;
                a.options.stickyTracking = !0;
                delete a.buildKDTree;
                a.buildKDTree()
            }, d.renderer.forExport ? Number.MAX_VALUE :
                void 0)
        }
    });
    e.scatter.prototype.cvsMarkerCircle = function (a, b, d, c) {
        a.moveTo(b, d);
        a.arc(b, d, c, 0, 2 * Math.PI, !1)
    };
    e.scatter.prototype.cvsMarkerSquare = function (a, b, d, c) {
        a.moveTo(b, d);
        a.rect(b - c, d - c, c * 2, c * 2)
    };
    e.scatter.prototype.fill = !0;
    n(e.area.prototype, {
        cvsDrawPoint: function (a, b, c, e, f) {
            f && b !== f.clientX && (a.moveTo(f.clientX, f.yBottom), a.lineTo(f.clientX, f.plotY), a.lineTo(b, c), a.lineTo(b, e))
        }, fill: !0, fillOpacity: !0, sampling: !0
    });
    n(e.column.prototype, {
        cvsDrawPoint: function (a, b, c, e) {
            a.rect(b - 1, c, 1, e - c)
        }, fill: !0,
        sampling: !0
    });
    l.prototype.getPoint = function (a) {
        var b = a;
        if (a && !(a instanceof this.pointClass))b = (new this.pointClass).init(this, this.options.data[a.i]), b.category = b.x, b.dist = a.dist, b.distX = a.distX, b.plotX = a.plotX, b.plotY = a.plotY;
        return b
    };
    j(l.prototype, "searchPoint", function (a) {
        return this.getPoint(a.apply(this, [].slice.call(arguments, 1)))
    })
});
