/*
 Highcharts JS v4.1.3 (2015-02-27)

 Standalone Highcharts Framework

 License: MIT License
 */
var HighchartsAdapter = function () {
    function p(c) {
        function b(b, a, d) {
            b.removeEventListener(a, d, !1)
        }

        function d(b, a, d) {
            d = b.HCProxiedMethods[d.toString()];
            b.detachEvent("on" + a, d)
        }

        function a(a, c) {
            var f = a.HCEvents, i, g, k, j;
            if (a.removeEventListener)i = b; else if (a.attachEvent)i = d; else return;
            c ? (g = {}, g[c] = !0) : g = f;
            for (j in g)if (f[j])for (k = f[j].length; k--;)i(a, j, f[j][k])
        }

        c.HCExtended || Highcharts.extend(c, {
            HCExtended: !0, HCEvents: {}, bind: function (b, a) {
                var d = this, c = this.HCEvents, g;
                if (d.addEventListener)d.addEventListener(b,
                    a, !1); else if (d.attachEvent) {
                    g = function (b) {
                        b.target = b.srcElement || window;
                        a.call(d, b)
                    };
                    if (!d.HCProxiedMethods)d.HCProxiedMethods = {};
                    d.HCProxiedMethods[a.toString()] = g;
                    d.attachEvent("on" + b, g)
                }
                c[b] === s && (c[b] = []);
                c[b].push(a)
            }, unbind: function (c, h) {
                var f, i;
                c ? (f = this.HCEvents[c] || [], h ? (i = HighchartsAdapter.inArray(h, f), i > -1 && (f.splice(i, 1), this.HCEvents[c] = f), this.removeEventListener ? b(this, c, h) : this.attachEvent && d(this, c, h)) : (a(this, c), this.HCEvents[c] = [])) : (a(this), this.HCEvents = {})
            }, trigger: function (b,
                                  a) {
                var d = this.HCEvents[b] || [], c = d.length, g, k, j;
                k = function () {
                    a.defaultPrevented = !0
                };
                for (g = 0; g < c; g++) {
                    j = d[g];
                    if (a.stopped)break;
                    a.preventDefault = k;
                    a.target = this;
                    if (!a.type)a.type = b;
                    j.call(this, a) === !1 && a.preventDefault()
                }
            }
        });
        return c
    }

    var s, l = document, q = [], m = [], r, n = {}, o;
    Math.easeInOutSine = function (c, b, d, a) {
        return -d / 2 * (Math.cos(Math.PI * c / a) - 1) + b
    };
    return {
        init: function (c) {
            if (!l.defaultView)this._getStyle = function (b, d) {
                var a;
                return b.style[d] ? b.style[d] : (d === "opacity" && (d = "filter"), a = b.currentStyle[d.replace(/\-(\w)/g,
                    function (b, a) {
                        return a.toUpperCase()
                    })], d === "filter" && (a = a.replace(/alpha\(opacity=([0-9]+)\)/, function (b, a) {
                    return a / 100
                })), a === "" ? 1 : a)
            }, this.adapterRun = function (b, d) {
                var a = {width: "clientWidth", height: "clientHeight"}[d];
                if (a)return b.style.zoom = 1, b[a] - 2 * parseInt(HighchartsAdapter._getStyle(b, "padding"), 10)
            };
            if (!Array.prototype.forEach)this.each = function (b, d) {
                for (var a = 0, c = b.length; a < c; a++)if (d.call(b[a], b[a], a, b) === !1)return a
            };
            if (!Array.prototype.indexOf)this.inArray = function (b, d) {
                var a, c = 0;
                if (d)for (a =
                               d.length; c < a; c++)if (d[c] === b)return c;
                return -1
            };
            if (!Array.prototype.filter)this.grep = function (b, d) {
                for (var a = [], c = 0, h = b.length; c < h; c++)d(b[c], c) && a.push(b[c]);
                return a
            };
            o = function (b, c, a) {
                this.options = c;
                this.elem = b;
                this.prop = a
            };
            o.prototype = {
                update: function () {
                    var b;
                    b = this.paths;
                    var d = this.elem, a = d.element;
                    if (n[this.prop])n[this.prop](this); else b && a ? d.attr("d", c.step(b[0], b[1], this.now, this.toD)) : d.attr ? a && d.attr(this.prop, this.now) : (b = {}, b[this.prop] = this.now + this.unit, Highcharts.css(d, b));
                    this.options.step &&
                    this.options.step.call(this.elem, this.now, this)
                }, custom: function (b, c, a) {
                    var e = this, h = function (a) {
                        return e.step(a)
                    }, f;
                    this.startTime = +new Date;
                    this.start = b;
                    this.end = c;
                    this.unit = a;
                    this.now = this.start;
                    this.pos = this.state = 0;
                    h.elem = this.elem;
                    h() && m.push(h) === 1 && (r = setInterval(function () {
                        for (f = 0; f < m.length; f++)m[f]() || m.splice(f--, 1);
                        m.length || clearInterval(r)
                    }, 13))
                }, step: function (b) {
                    var c = +new Date, a;
                    a = this.options;
                    var e = this.elem, h;
                    if (e.stopAnimation || e.attr && !e.element)a = !1; else if (b || c >= a.duration +
                        this.startTime) {
                        this.now = this.end;
                        this.pos = this.state = 1;
                        this.update();
                        b = this.options.curAnim[this.prop] = !0;
                        for (h in a.curAnim)a.curAnim[h] !== !0 && (b = !1);
                        b && a.complete && a.complete.call(e);
                        a = !1
                    } else e = c - this.startTime, this.state = e / a.duration, this.pos = a.easing(e, 0, 1, a.duration), this.now = this.start + (this.end - this.start) * this.pos, this.update(), a = !0;
                    return a
                }
            };
            this.animate = function (b, d, a) {
                var e, h = "", f, i, g;
                b.stopAnimation = !1;
                if (typeof a !== "object" || a === null)e = arguments, a = {
                    duration: e[2],
                    easing: e[3],
                    complete: e[4]
                };
                if (typeof a.duration !== "number")a.duration = 400;
                a.easing = Math[a.easing] || Math.easeInOutSine;
                a.curAnim = Highcharts.extend({}, d);
                for (g in d)i = new o(b, a, g), f = null, g === "d" ? (i.paths = c.init(b, b.d, d.d), i.toD = d.d, e = 0, f = 1) : b.attr ? e = b.attr(g) : (e = parseFloat(HighchartsAdapter._getStyle(b, g)) || 0, g !== "opacity" && (h = "px")), f || (f = d[g]), i.custom(e, f, h)
            }
        }, _getStyle: function (c, b) {
            return window.getComputedStyle(c, void 0).getPropertyValue(b)
        }, addAnimSetter: function (c, b) {
            n[c] = b
        }, getScript: function (c, b) {
            var d = l.getElementsByTagName("head")[0],
                a = l.createElement("script");
            a.type = "text/javascript";
            a.src = c;
            a.onload = b;
            d.appendChild(a)
        }, inArray: function (c, b) {
            return b.indexOf ? b.indexOf(c) : q.indexOf.call(b, c)
        }, adapterRun: function (c, b) {
            return parseInt(HighchartsAdapter._getStyle(c, b), 10)
        }, grep: function (c, b) {
            return q.filter.call(c, b)
        }, map: function (c, b) {
            for (var d = [], a = 0, e = c.length; a < e; a++)d[a] = b.call(c[a], c[a], a, c);
            return d
        }, offset: function (c) {
            var b = document.documentElement, c = c.getBoundingClientRect();
            return {
                top: c.top + (window.pageYOffset || b.scrollTop) -
                (b.clientTop || 0), left: c.left + (window.pageXOffset || b.scrollLeft) - (b.clientLeft || 0)
            }
        }, addEvent: function (c, b, d) {
            p(c).bind(b, d)
        }, removeEvent: function (c, b, d) {
            p(c).unbind(b, d)
        }, fireEvent: function (c, b, d, a) {
            var e;
            l.createEvent && (c.dispatchEvent || c.fireEvent) ? (e = l.createEvent("Events"), e.initEvent(b, !0, !0), e.target = c, Highcharts.extend(e, d), c.dispatchEvent ? c.dispatchEvent(e) : c.fireEvent(b, e)) : c.HCExtended === !0 && (d = d || {}, c.trigger(b, d));
            d && d.defaultPrevented && (a = null);
            a && a(d)
        }, washMouseEvent: function (c) {
            return c
        },
        stop: function (c) {
            c.stopAnimation = !0
        }, each: function (c, b) {
            return Array.prototype.forEach.call(c, b)
        }
    }
}();
/*
 Highstock JS v2.1.2 (2015-02-27)

 (c) 2009-2014 Torstein Honsi

 License: www.highcharts.com/license
 */
(function () {
    function y() {
        var a, b = arguments, c, d = {}, e = function (a, b) {
            var c, d;
            typeof a !== "object" && (a = {});
            for (d in b)b.hasOwnProperty(d) && (c = b[d], a[d] = c && typeof c === "object" && Object.prototype.toString.call(c) !== "[object Array]" && d !== "renderTo" && typeof c.nodeType !== "number" ? e(a[d] || {}, c) : b[d]);
            return a
        };
        b[0] === !0 && (d = b[1], b = Array.prototype.slice.call(b, 2));
        c = b.length;
        for (a = 0; a < c; a++)d = e(d, b[a]);
        return d
    }

    function C(a, b) {
        return parseInt(a, b || 10)
    }

    function Ja(a) {
        return typeof a === "string"
    }

    function ia(a) {
        return a &&
            typeof a === "object"
    }

    function Ka(a) {
        return Object.prototype.toString.call(a) === "[object Array]"
    }

    function sa(a) {
        return typeof a === "number"
    }

    function La(a) {
        return Y.log(a) / Y.LN10
    }

    function ta(a) {
        return Y.pow(10, a)
    }

    function ua(a, b) {
        for (var c = a.length; c--;)if (a[c] === b) {
            a.splice(c, 1);
            break
        }
    }

    function s(a) {
        return a !== r && a !== null
    }

    function W(a, b, c) {
        var d, e;
        if (Ja(b))s(c) ? a.setAttribute(b, c) : a && a.getAttribute && (e = a.getAttribute(b)); else if (s(b) && ia(b))for (d in b)a.setAttribute(d, b[d]);
        return e
    }

    function pa(a) {
        return Ka(a) ?
            a : [a]
    }

    function M(a, b) {
        if (Da && !da && b && b.opacity !== r)b.filter = "alpha(opacity=" + b.opacity * 100 + ")";
        x(a.style, b)
    }

    function aa(a, b, c, d, e) {
        a = E.createElement(a);
        b && x(a, b);
        e && M(a, {padding: 0, border: Z, margin: 0});
        c && M(a, c);
        d && d.appendChild(a);
        return a
    }

    function ja(a, b) {
        var c = function () {
            return r
        };
        c.prototype = new a;
        x(c.prototype, b);
        return c
    }

    function Ra(a, b) {
        return Array((b || 2) + 1 - String(a).length).join(0) + a
    }

    function ab(a) {
        return (jb && jb(a) || ub || 0) * 6E4
    }

    function Ma(a, b) {
        for (var c = "{", d = !1, e, f, g, h, i, j = []; (c = a.indexOf(c)) !== -1;) {
            e = a.slice(0, c);
            if (d) {
                f = e.split(":");
                g = f.shift().split(".");
                i = g.length;
                e = b;
                for (h = 0; h < i; h++)e = e[g[h]];
                if (f.length)f = f.join(":"), g = /\.([0-9])/, h = F.lang, i = void 0, /f$/.test(f) ? (i = (i = f.match(g)) ? i[1] : -1, e !== null && (e = z.numberFormat(e, i, h.decimalPoint, f.indexOf(",") > -1 ? h.thousandsSep : ""))) : e = ka(f, e)
            }
            j.push(e);
            a = a.slice(c + 1);
            c = (d = !d) ? "}" : "{"
        }
        j.push(a);
        return j.join("")
    }

    function vb(a) {
        return Y.pow(10, X(Y.log(a) / Y.LN10))
    }

    function wb(a, b, c, d, e) {
        var f, g = a, c = p(c, 1);
        f = a / c;
        b || (b = [1, 2, 2.5, 5, 10], d === !1 && (c ===
        1 ? b = [1, 2, 5, 10] : c <= 0.1 && (b = [1 / c])));
        for (d = 0; d < b.length; d++)if (g = b[d], e && g * c >= a || !e && f <= (b[d] + (b[d + 1] || b[d])) / 2)break;
        g *= c;
        return g
    }

    function xb(a, b) {
        var c = a.length, d, e;
        for (e = 0; e < c; e++)a[e].ss_i = e;
        a.sort(function (a, c) {
            d = b(a, c);
            return d === 0 ? a.ss_i - c.ss_i : d
        });
        for (e = 0; e < c; e++)delete a[e].ss_i
    }

    function Sa(a) {
        for (var b = a.length, c = a[0]; b--;)a[b] < c && (c = a[b]);
        return c
    }

    function Ea(a) {
        for (var b = a.length, c = a[0]; b--;)a[b] > c && (c = a[b]);
        return c
    }

    function Na(a, b) {
        for (var c in a)a[c] && a[c] !== b && a[c].destroy && a[c].destroy(),
            delete a[c]
    }

    function Ta(a) {
        kb || (kb = aa(Ua));
        a && kb.appendChild(a);
        kb.innerHTML = ""
    }

    function qa(a, b) {
        var c = "Highcharts error #" + a + ": www.highcharts.com/errors/" + a;
        if (b)throw c;
        T.console && console.log(c)
    }

    function la(a) {
        return parseFloat(a.toPrecision(14))
    }

    function Ya(a, b) {
        Fa = p(a, b.animation)
    }

    function Nb() {
        var a = F.global, b = a.useUTC, c = b ? "getUTC" : "get", d = b ? "setUTC" : "set";
        ea = a.Date || window.Date;
        ub = b && a.timezoneOffset;
        jb = b && a.getTimezoneOffset;
        lb = function (a, c, d, h, i, j) {
            var k;
            b ? (k = ea.UTC.apply(0, arguments), k +=
                ab(k)) : k = (new ea(a, c, p(d, 1), p(h, 0), p(i, 0), p(j, 0))).getTime();
            return k
        };
        yb = c + "Minutes";
        zb = c + "Hours";
        Ab = c + "Day";
        bb = c + "Date";
        cb = c + "Month";
        db = c + "FullYear";
        Ob = d + "Minutes";
        Pb = d + "Hours";
        Bb = d + "Date";
        Cb = d + "Month";
        Db = d + "FullYear"
    }

    function $() {
    }

    function Za(a, b, c, d) {
        this.axis = a;
        this.pos = b;
        this.type = c || "";
        this.isNew = !0;
        !c && !d && this.addLabel()
    }

    function Qb(a, b, c, d, e) {
        var f = a.chart.inverted;
        this.axis = a;
        this.isNegative = c;
        this.options = b;
        this.x = d;
        this.total = null;
        this.points = {};
        this.stack = e;
        this.alignOptions = {
            align: b.align ||
            (f ? c ? "left" : "right" : "center"),
            verticalAlign: b.verticalAlign || (f ? "middle" : c ? "bottom" : "top"),
            y: p(b.y, f ? 4 : c ? 14 : -6),
            x: p(b.x, f ? c ? -6 : 6 : 0)
        };
        this.textAlign = b.textAlign || (f ? c ? "right" : "left" : "center")
    }

    function Eb(a) {
        var b = a.options, c = b.navigator, d = c.enabled, b = b.scrollbar, e = b.enabled, f = d ? c.height : 0, g = e ? b.height : 0;
        this.handles = [];
        this.scrollbarButtons = [];
        this.elementsToDestroy = [];
        this.chart = a;
        this.setBaseSeries();
        this.height = f;
        this.scrollbarHeight = g;
        this.scrollbarEnabled = e;
        this.navigatorEnabled = d;
        this.navigatorOptions =
            c;
        this.scrollbarOptions = b;
        this.outlineHeight = f + g;
        this.init()
    }

    function Fb(a) {
        this.init(a)
    }

    var r, E = document, T = window, Y = Math, w = Y.round, X = Y.floor, ya = Y.ceil, v = Y.max, B = Y.min, R = Y.abs, ba = Y.cos, fa = Y.sin, va = Y.PI, ra = va * 2 / 360, Ga = navigator.userAgent, Rb = T.opera, Da = /(msie|trident)/i.test(Ga) && !Rb, mb = E.documentMode === 8, Gb = /AppleWebKit/.test(Ga), Va = /Firefox/.test(Ga), eb = /(Mobile|Android|Windows Phone)/.test(Ga), Ha = "http://www.w3.org/2000/svg", da = !!E.createElementNS && !!E.createElementNS(Ha, "svg").createSVGRect, Wb =
        Va && parseInt(Ga.split("Firefox/")[1], 10) < 4, ma = !da && !Da && !!E.createElement("canvas").getContext, Wa, $a, Sb = {}, Hb = 0, kb, F, ka, Fa, Ib, H, ga = function () {
        return r
    }, ha = [], fb = 0, Ua = "div", Z = "none", Xb = /^[0-9]+$/, nb = ["plotTop", "marginRight", "marginBottom", "plotLeft"], Yb = "stroke-width", ea, lb, ub, jb, yb, zb, Ab, bb, cb, db, Ob, Pb, Bb, Cb, Db, I = {}, z;
    z = T.Highcharts = T.Highcharts ? qa(16, !0) : {};
    z.seriesTypes = I;
    var x = z.extend = function (a, b) {
        var c;
        a || (a = {});
        for (c in b)a[c] = b[c];
        return a
    }, p = z.pick = function () {
        var a = arguments, b, c, d = a.length;
        for (b = 0; b < d; b++)if (c = a[b], c !== r && c !== null)return c
    }, S = z.wrap = function (a, b, c) {
        var d = a[b];
        a[b] = function () {
            var a = Array.prototype.slice.call(arguments);
            a.unshift(d);
            return c.apply(this, a)
        }
    };
    ka = function (a, b, c) {
        if (!s(b) || isNaN(b))return "Invalid date";
        var a = p(a, "%Y-%m-%d %H:%M:%S"), d = new ea(b - ab(b)), e, f = d[zb](), g = d[Ab](), h = d[bb](), i = d[cb](), j = d[db](), k = F.lang, l = k.weekdays, d = x({
            a: l[g].substr(0, 3),
            A: l[g],
            d: Ra(h),
            e: h,
            w: g,
            b: k.shortMonths[i],
            B: k.months[i],
            m: Ra(i + 1),
            y: j.toString().substr(2, 2),
            Y: j,
            H: Ra(f),
            I: Ra(f %
                12 || 12),
            l: f % 12 || 12,
            M: Ra(d[yb]()),
            p: f < 12 ? "AM" : "PM",
            P: f < 12 ? "am" : "pm",
            S: Ra(d.getSeconds()),
            L: Ra(w(b % 1E3), 3)
        }, z.dateFormats);
        for (e in d)for (; a.indexOf("%" + e) !== -1;)a = a.replace("%" + e, typeof d[e] === "function" ? d[e](b) : d[e]);
        return c ? a.substr(0, 1).toUpperCase() + a.substr(1) : a
    };
    H = {
        millisecond: 1,
        second: 1E3,
        minute: 6E4,
        hour: 36E5,
        day: 864E5,
        week: 6048E5,
        month: 24192E5,
        year: 314496E5
    };
    z.numberFormat = function (a, b, c, d) {
        var e = F.lang, a = +a || 0, f = b === -1 ? B((a.toString().split(".")[1] || "").length, 20) : isNaN(b = R(b)) ? 2 : b, b = c === void 0 ? e.decimalPoint : c, d = d === void 0 ? e.thousandsSep : d, e = a < 0 ? "-" : "", c = String(C(a = R(a).toFixed(f))), g = c.length > 3 ? c.length % 3 : 0;
        return e + (g ? c.substr(0, g) + d : "") + c.substr(g).replace(/(\d{3})(?=\d)/g, "$1" + d) + (f ? b + R(a - c).toFixed(f).slice(2) : "")
    };
    Ib = {
        init: function (a, b, c) {
            var b = b || "", d = a.shift, e = b.indexOf("C") > -1, f = e ? 7 : 3, g, b = b.split(" "), c = [].concat(c), h, i, j = function (a) {
                for (g = a.length; g--;)a[g] === "M" && a.splice(g + 1, 0, a[g + 1], a[g + 2], a[g + 1], a[g + 2])
            };
            e && (j(b), j(c));
            a.isArea && (h = b.splice(b.length - 6, 6), i = c.splice(c.length -
                6, 6));
            if (d <= c.length / f && b.length === c.length)for (; d--;)c = [].concat(c).splice(0, f).concat(c);
            a.shift = 0;
            if (b.length)for (a = c.length; b.length < a;)d = [].concat(b).splice(b.length - f, f), e && (d[f - 6] = d[f - 2], d[f - 5] = d[f - 1]), b = b.concat(d);
            h && (b = b.concat(h), c = c.concat(i));
            return [b, c]
        }, step: function (a, b, c, d) {
            var e = [], f = a.length;
            if (c === 1)e = d; else if (f === b.length && c < 1)for (; f--;)d = parseFloat(a[f]), e[f] = isNaN(d) ? a[f] : c * parseFloat(b[f] - d) + d; else e = b;
            return e
        }
    };
    (function (a) {
        T.HighchartsAdapter = T.HighchartsAdapter || a && {
                init: function (b) {
                    var c =
                        a.fx;
                    a.extend(a.easing, {
                        easeOutQuad: function (a, b, c, g, h) {
                            return -g * (b /= h) * (b - 2) + c
                        }
                    });
                    a.each(["cur", "_default", "width", "height", "opacity"], function (b, e) {
                        var f = c.step, g;
                        e === "cur" ? f = c.prototype : e === "_default" && a.Tween && (f = a.Tween.propHooks[e], e = "set");
                        (g = f[e]) && (f[e] = function (a) {
                            var c, a = b ? a : this;
                            if (a.prop !== "align")return c = a.elem, c.attr ? c.attr(a.prop, e === "cur" ? r : a.now) : g.apply(this, arguments)
                        })
                    });
                    S(a.cssHooks.opacity, "get", function (a, b, c) {
                        return b.attr ? b.opacity || 0 : a.call(this, b, c)
                    });
                    this.addAnimSetter("d",
                        function (a) {
                            var c = a.elem, f;
                            if (!a.started)f = b.init(c, c.d, c.toD), a.start = f[0], a.end = f[1], a.started = !0;
                            c.attr("d", b.step(a.start, a.end, a.pos, c.toD))
                        });
                    this.each = Array.prototype.forEach ? function (a, b) {
                        return Array.prototype.forEach.call(a, b)
                    } : function (a, b) {
                        var c, g = a.length;
                        for (c = 0; c < g; c++)if (b.call(a[c], a[c], c, a) === !1)return c
                    };
                    a.fn.highcharts = function () {
                        var a = "Chart", b = arguments, c, g;
                        if (this[0]) {
                            Ja(b[0]) && (a = b[0], b = Array.prototype.slice.call(b, 1));
                            c = b[0];
                            if (c !== r)c.chart = c.chart || {}, c.chart.renderTo = this[0],
                                new z[a](c, b[1]), g = this;
                            c === r && (g = ha[W(this[0], "data-highcharts-chart")])
                        }
                        return g
                    }
                }, addAnimSetter: function (b, c) {
                    a.Tween ? a.Tween.propHooks[b] = {set: c} : a.fx.step[b] = c
                }, getScript: a.getScript, inArray: a.inArray, adapterRun: function (b, c) {
                    return a(b)[c]()
                }, grep: a.grep, map: function (a, c) {
                    for (var d = [], e = 0, f = a.length; e < f; e++)d[e] = c.call(a[e], a[e], e, a);
                    return d
                }, offset: function (b) {
                    return a(b).offset()
                }, addEvent: function (b, c, d) {
                    a(b).bind(c, d)
                }, removeEvent: function (b, c, d) {
                    var e = E.removeEventListener ? "removeEventListener" :
                        "detachEvent";
                    E[e] && b && !b[e] && (b[e] = function () {
                    });
                    a(b).unbind(c, d)
                }, fireEvent: function (b, c, d, e) {
                    var f = a.Event(c), g = "detached" + c, h;
                    !Da && d && (delete d.layerX, delete d.layerY, delete d.returnValue);
                    x(f, d);
                    b[c] && (b[g] = b[c], b[c] = null);
                    a.each(["preventDefault", "stopPropagation"], function (a, b) {
                        var c = f[b];
                        f[b] = function () {
                            try {
                                c.call(f)
                            } catch (a) {
                                b === "preventDefault" && (h = !0)
                            }
                        }
                    });
                    a(b).trigger(f);
                    b[g] && (b[c] = b[g], b[g] = null);
                    e && !f.isDefaultPrevented() && !h && e(f)
                }, washMouseEvent: function (a) {
                    var c = a.originalEvent ||
                        a;
                    if (c.pageX === r)c.pageX = a.pageX, c.pageY = a.pageY;
                    return c
                }, animate: function (b, c, d) {
                    var e = a(b);
                    if (!b.style)b.style = {};
                    if (c.d)b.toD = c.d, c.d = 1;
                    e.stop();
                    c.opacity !== r && b.attr && (c.opacity += "px");
                    b.hasAnim = 1;
                    e.animate(c, d)
                }, stop: function (b) {
                    b.hasAnim && a(b).stop()
                }
            }
    })(T.jQuery);
    var Q = T.HighchartsAdapter, G = Q || {};
    Q && Q.init.call(Q, Ib);
    var ob = G.adapterRun, Zb = G.getScript, Oa = G.inArray, n = z.each = G.each, pb = G.grep, $b = G.offset, za = G.map, D = G.addEvent, U = G.removeEvent, K = G.fireEvent, ac = G.washMouseEvent, qb = G.animate, gb =
        G.stop;
    F = {
        colors: "#7cb5ec,#434348,#90ed7d,#f7a35c,#8085e9,#f15c80,#e4d354,#2b908f,#f45b5b,#91e8e1".split(","),
        symbols: ["circle", "diamond", "square", "triangle", "triangle-down"],
        lang: {
            loading: "Loading...",
            months: "January,February,March,April,May,June,July,August,September,October,November,December".split(","),
            shortMonths: "Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec".split(","),
            weekdays: "Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday".split(","),
            decimalPoint: ".",
            numericSymbols: "k,M,G,T,P,E".split(","),
            resetZoom: "Reset zoom",
            resetZoomTitle: "Reset zoom level 1:1",
            thousandsSep: " "
        },
        global: {
            useUTC: !0,
            canvasToolsURL: "http://code.highcharts.com/stock/2.1.2/modules/canvas-tools.js",
            VMLRadialGradientURL: "http://code.highcharts.com/stock/2.1.2/gfx/vml-radial-gradient.png"
        },
        chart: {
            borderColor: "#4572A7",
            borderRadius: 0,
            defaultSeriesType: "line",
            ignoreHiddenSeries: !0,
            spacing: [10, 10, 15, 10],
            backgroundColor: "#FFFFFF",
            plotBorderColor: "#C0C0C0",
            resetZoomButton: {theme: {zIndex: 20}, position: {align: "right", x: -10, y: 10}}
        },
        title: {text: "Chart title", align: "center", margin: 15, style: {color: "#333333", fontSize: "18px"}},
        subtitle: {text: "", align: "center", style: {color: "#555555"}},
        plotOptions: {
            line: {
                allowPointSelect: !1,
                showCheckbox: !1,
                animation: {duration: 1E3},
                events: {},
                lineWidth: 2,
                marker: {
                    lineWidth: 0,
                    radius: 4,
                    lineColor: "#FFFFFF",
                    states: {
                        hover: {enabled: !0, lineWidthPlus: 1, radiusPlus: 2},
                        select: {fillColor: "#FFFFFF", lineColor: "#000000", lineWidth: 2}
                    }
                },
                point: {events: {}},
                dataLabels: {
                    align: "center",
                    formatter: function () {
                        return this.y ===
                        null ? "" : z.numberFormat(this.y, -1)
                    },
                    style: {
                        color: "contrast",
                        fontSize: "11px",
                        fontWeight: "bold",
                        textShadow: "0 0 6px contrast, 0 0 3px contrast"
                    },
                    verticalAlign: "bottom",
                    x: 0,
                    y: 0,
                    padding: 5
                },
                cropThreshold: 300,
                pointRange: 0,
                states: {hover: {lineWidthPlus: 1, marker: {}, halo: {size: 10, opacity: 0.25}}, select: {marker: {}}},
                stickyTracking: !0,
                turboThreshold: 1E3
            }
        },
        labels: {style: {position: "absolute", color: "#3E576F"}},
        legend: {
            enabled: !0,
            align: "center",
            layout: "horizontal",
            labelFormatter: function () {
                return this.name
            },
            borderColor: "#909090",
            borderRadius: 0,
            navigation: {activeColor: "#274b6d", inactiveColor: "#CCC"},
            shadow: !1,
            itemStyle: {color: "#333333", fontSize: "12px", fontWeight: "bold"},
            itemHoverStyle: {color: "#000"},
            itemHiddenStyle: {color: "#CCC"},
            itemCheckboxStyle: {position: "absolute", width: "13px", height: "13px"},
            symbolPadding: 5,
            verticalAlign: "bottom",
            x: 0,
            y: 0,
            title: {style: {fontWeight: "bold"}}
        },
        loading: {
            labelStyle: {fontWeight: "bold", position: "relative", top: "45%"},
            style: {position: "absolute", backgroundColor: "white", opacity: 0.5, textAlign: "center"}
        },
        tooltip: {
            enabled: !0,
            animation: da,
            backgroundColor: "rgba(249, 249, 249, .85)",
            borderWidth: 1,
            borderRadius: 3,
            dateTimeLabelFormats: {
                millisecond: "%A, %b %e, %H:%M:%S.%L",
                second: "%A, %b %e, %H:%M:%S",
                minute: "%A, %b %e, %H:%M",
                hour: "%A, %b %e, %H:%M",
                day: "%A, %b %e, %Y",
                week: "Week from %A, %b %e, %Y",
                month: "%B %Y",
                year: "%Y"
            },
            footerFormat: "",
            headerFormat: '<span style="font-size: 10px">{point.key}</span><br/>',
            pointFormat: '<span style="color:{point.color}">●</span> {series.name}: <b>{point.y}</b><br/>',
            shadow: !0,
            snap: eb ? 25 : 10,
            style: {color: "#333333", cursor: "default", fontSize: "12px", padding: "8px", whiteSpace: "nowrap"}
        },
        credits: {
            enabled: !0,
            text: "Highcharts.com",
            href: "http://www.highcharts.com",
            position: {align: "right", x: -10, verticalAlign: "bottom", y: -5},
            style: {cursor: "pointer", color: "#909090", fontSize: "9px"}
        }
    };
    var V = F.plotOptions, Q = V.line;
    Nb();
    var bc = /rgba\(\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]?(?:\.[0-9]+)?)\s*\)/, cc = /#([a-fA-F0-9]{2})([a-fA-F0-9]{2})([a-fA-F0-9]{2})/, dc = /rgb\(\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*\)/,
        wa = function (a) {
            var b = [], c, d;
            (function (a) {
                a && a.stops ? d = za(a.stops, function (a) {
                    return wa(a[1])
                }) : (c = bc.exec(a)) ? b = [C(c[1]), C(c[2]), C(c[3]), parseFloat(c[4], 10)] : (c = cc.exec(a)) ? b = [C(c[1], 16), C(c[2], 16), C(c[3], 16), 1] : (c = dc.exec(a)) && (b = [C(c[1]), C(c[2]), C(c[3]), 1])
            })(a);
            return {
                get: function (c) {
                    var f;
                    d ? (f = y(a), f.stops = [].concat(f.stops), n(d, function (a, b) {
                        f.stops[b] = [f.stops[b][0], a.get(c)]
                    })) : f = b && !isNaN(b[0]) ? c === "rgb" ? "rgb(" + b[0] + "," + b[1] + "," + b[2] + ")" : c === "a" ? b[3] : "rgba(" + b.join(",") + ")" : a;
                    return f
                }, brighten: function (a) {
                    if (d)n(d,
                        function (b) {
                            b.brighten(a)
                        }); else if (sa(a) && a !== 0) {
                        var c;
                        for (c = 0; c < 3; c++)b[c] += C(a * 255), b[c] < 0 && (b[c] = 0), b[c] > 255 && (b[c] = 255)
                    }
                    return this
                }, rgba: b, setOpacity: function (a) {
                    b[3] = a;
                    return this
                }
            }
        };
    $.prototype = {
        opacity: 1,
        textProps: "fontSize,fontWeight,fontFamily,color,lineHeight,width,textDecoration,textShadow".split(","),
        init: function (a, b) {
            this.element = b === "span" ? aa(b) : E.createElementNS(Ha, b);
            this.renderer = a
        },
        animate: function (a, b, c) {
            b = p(b, Fa, !0);
            gb(this);
            if (b) {
                b = y(b, {});
                if (c)b.complete = c;
                qb(this, a, b)
            } else this.attr(a),
            c && c();
            return this
        },
        colorGradient: function (a, b, c) {
            var d = this.renderer, e, f, g, h, i, j, k, l, m, o, q = [];
            a.linearGradient ? f = "linearGradient" : a.radialGradient && (f = "radialGradient");
            if (f) {
                g = a[f];
                h = d.gradients;
                j = a.stops;
                m = c.radialReference;
                Ka(g) && (a[f] = g = {x1: g[0], y1: g[1], x2: g[2], y2: g[3], gradientUnits: "userSpaceOnUse"});
                f === "radialGradient" && m && !s(g.gradientUnits) && (g = y(g, {
                    cx: m[0] - m[2] / 2 + g.cx * m[2],
                    cy: m[1] - m[2] / 2 + g.cy * m[2],
                    r: g.r * m[2],
                    gradientUnits: "userSpaceOnUse"
                }));
                for (o in g)o !== "id" && q.push(o, g[o]);
                for (o in j)q.push(j[o]);
                q = q.join(",");
                h[q] ? a = h[q].attr("id") : (g.id = a = "highcharts-" + Hb++, h[q] = i = d.createElement(f).attr(g).add(d.defs), i.stops = [], n(j, function (a) {
                    a[1].indexOf("rgba") === 0 ? (e = wa(a[1]), k = e.get("rgb"), l = e.get("a")) : (k = a[1], l = 1);
                    a = d.createElement("stop").attr({offset: a[0], "stop-color": k, "stop-opacity": l}).add(i);
                    i.stops.push(a)
                }));
                c.setAttribute(b, "url(" + d.url + "#" + a + ")")
            }
        },
        applyTextShadow: function (a) {
            var b = this.element, c, d = a.indexOf("contrast") !== -1, e = this.renderer.forExport || b.style.textShadow !== r && !Da;
            d && (a =
                a.replace(/contrast/g, this.renderer.getContrast(b.style.fill)));
            e ? d && M(b, {textShadow: a}) : (this.fakeTS = !0, this.ySetter = this.xSetter, c = [].slice.call(b.getElementsByTagName("tspan")), n(a.split(/\s?,\s?/g), function (a) {
                var d = b.firstChild, e, i, a = a.split(" ");
                e = a[a.length - 1];
                (i = a[a.length - 2]) && n(c, function (a, c) {
                    var f;
                    c === 0 && (a.setAttribute("x", b.getAttribute("x")), c = b.getAttribute("y"), a.setAttribute("y", c || 0), c === null && b.setAttribute("y", 0));
                    f = a.cloneNode(1);
                    W(f, {
                        "class": "highcharts-text-shadow", fill: e,
                        stroke: e, "stroke-opacity": 1 / v(C(i), 3), "stroke-width": i, "stroke-linejoin": "round"
                    });
                    b.insertBefore(f, d)
                })
            }))
        },
        attr: function (a, b) {
            var c, d, e = this.element, f, g = this, h;
            typeof a === "string" && b !== r && (c = a, a = {}, a[c] = b);
            if (typeof a === "string")g = (this[a + "Getter"] || this._defaultGetter).call(this, a, e); else {
                for (c in a) {
                    d = a[c];
                    h = !1;
                    this.symbolName && /^(x|y|width|height|r|start|end|innerR|anchorX|anchorY)/.test(c) && (f || (this.symbolAttr(a), f = !0), h = !0);
                    if (this.rotation && (c === "x" || c === "y"))this.doTransform = !0;
                    h || (this[c +
                    "Setter"] || this._defaultSetter).call(this, d, c, e);
                    this.shadows && /^(width|height|visibility|x|y|d|transform|cx|cy|r)$/.test(c) && this.updateShadows(c, d)
                }
                if (this.doTransform)this.updateTransform(), this.doTransform = !1
            }
            return g
        },
        updateShadows: function (a, b) {
            for (var c = this.shadows, d = c.length; d--;)c[d].setAttribute(a, a === "height" ? v(b - (c[d].cutHeight || 0), 0) : a === "d" ? this.d : b)
        },
        addClass: function (a) {
            var b = this.element, c = W(b, "class") || "";
            c.indexOf(a) === -1 && W(b, "class", c + " " + a);
            return this
        },
        symbolAttr: function (a) {
            var b =
                this;
            n("x,y,r,start,end,width,height,innerR,anchorX,anchorY".split(","), function (c) {
                b[c] = p(a[c], b[c])
            });
            b.attr({d: b.renderer.symbols[b.symbolName](b.x, b.y, b.width, b.height, b)})
        },
        clip: function (a) {
            return this.attr("clip-path", a ? "url(" + this.renderer.url + "#" + a.id + ")" : Z)
        },
        crisp: function (a) {
            var b, c = {}, d, e = a.strokeWidth || this.strokeWidth || 0;
            d = w(e) % 2 / 2;
            a.x = X(a.x || this.x || 0) + d;
            a.y = X(a.y || this.y || 0) + d;
            a.width = X((a.width || this.width || 0) - 2 * d);
            a.height = X((a.height || this.height || 0) - 2 * d);
            a.strokeWidth = e;
            for (b in a)this[b] !==
            a[b] && (this[b] = c[b] = a[b]);
            return c
        },
        css: function (a) {
            var b = this.styles, c = {}, d = this.element, e, f, g = "";
            e = !b;
            if (a && a.color)a.fill = a.color;
            if (b)for (f in a)a[f] !== b[f] && (c[f] = a[f], e = !0);
            if (e) {
                e = this.textWidth = a && a.width && d.nodeName.toLowerCase() === "text" && C(a.width) || this.textWidth;
                b && (a = x(b, c));
                this.styles = a;
                e && (ma || !da && this.renderer.forExport) && delete a.width;
                if (Da && !da)M(this.element, a); else {
                    b = function (a, b) {
                        return "-" + b.toLowerCase()
                    };
                    for (f in a)g += f.replace(/([A-Z])/g, b) + ":" + a[f] + ";";
                    W(d, "style", g)
                }
                e &&
                this.added && this.renderer.buildText(this)
            }
            return this
        },
        on: function (a, b) {
            var c = this, d = c.element;
            $a && a === "click" ? (d.ontouchstart = function (a) {
                c.touchEventFired = ea.now();
                a.preventDefault();
                b.call(d, a)
            }, d.onclick = function (a) {
                (Ga.indexOf("Android") === -1 || ea.now() - (c.touchEventFired || 0) > 1100) && b.call(d, a)
            }) : d["on" + a] = b;
            return this
        },
        setRadialReference: function (a) {
            this.element.radialReference = a;
            return this
        },
        translate: function (a, b) {
            return this.attr({translateX: a, translateY: b})
        },
        invert: function () {
            this.inverted = !0;
            this.updateTransform();
            return this
        },
        updateTransform: function () {
            var a = this.translateX || 0, b = this.translateY || 0, c = this.scaleX, d = this.scaleY, e = this.inverted, f = this.rotation, g = this.element;
            e && (a += this.attr("width"), b += this.attr("height"));
            a = ["translate(" + a + "," + b + ")"];
            e ? a.push("rotate(90) scale(-1,1)") : f && a.push("rotate(" + f + " " + (g.getAttribute("x") || 0) + " " + (g.getAttribute("y") || 0) + ")");
            (s(c) || s(d)) && a.push("scale(" + p(c, 1) + " " + p(d, 1) + ")");
            a.length && g.setAttribute("transform", a.join(" "))
        },
        toFront: function () {
            var a =
                this.element;
            a.parentNode.appendChild(a);
            return this
        },
        align: function (a, b, c) {
            var d, e, f, g, h = {};
            e = this.renderer;
            f = e.alignedObjects;
            if (a) {
                if (this.alignOptions = a, this.alignByTranslate = b, !c || Ja(c))this.alignTo = d = c || "renderer", ua(f, this), f.push(this), c = null
            } else a = this.alignOptions, b = this.alignByTranslate, d = this.alignTo;
            c = p(c, e[d], e);
            d = a.align;
            e = a.verticalAlign;
            f = (c.x || 0) + (a.x || 0);
            g = (c.y || 0) + (a.y || 0);
            if (d === "right" || d === "center")f += (c.width - (a.width || 0)) / {right: 1, center: 2}[d];
            h[b ? "translateX" : "x"] = w(f);
            if (e === "bottom" || e === "middle")g += (c.height - (a.height || 0)) / ({bottom: 1, middle: 2}[e] || 1);
            h[b ? "translateY" : "y"] = w(g);
            this[this.placed ? "animate" : "attr"](h);
            this.placed = !0;
            this.alignAttr = h;
            return this
        },
        getBBox: function (a) {
            var b, c = this.renderer, d, e = this.rotation, f = this.element, g = this.styles, h = e * ra;
            d = this.textStr;
            var i, j = f.style, k, l;
            d !== r && (l = ["", e || 0, g && g.fontSize, f.style.width].join(","), l = d === "" || Xb.test(d) ? "num:" + d.toString().length + l : d + l);
            l && !a && (b = c.cache[l]);
            if (!b) {
                if (f.namespaceURI === Ha || c.forExport) {
                    try {
                        k =
                            this.fakeTS && function (a) {
                                n(f.querySelectorAll(".highcharts-text-shadow"), function (b) {
                                    b.style.display = a
                                })
                            }, Va && j.textShadow ? (i = j.textShadow, j.textShadow = "") : k && k(Z), b = f.getBBox ? x({}, f.getBBox()) : {
                            width: f.offsetWidth,
                            height: f.offsetHeight
                        }, i ? j.textShadow = i : k && k("")
                    } catch (m) {
                    }
                    if (!b || b.width < 0)b = {width: 0, height: 0}
                } else b = this.htmlGetBBox();
                if (c.isSVG) {
                    a = b.width;
                    d = b.height;
                    if (Da && g && g.fontSize === "11px" && d.toPrecision(3) === "16.9")b.height = d = 14;
                    if (e)b.width = R(d * fa(h)) + R(a * ba(h)), b.height = R(d * ba(h)) + R(a *
                            fa(h))
                }
                c.cache[l] = b
            }
            return b
        },
        show: function (a) {
            a && this.element.namespaceURI === Ha ? this.element.removeAttribute("visibility") : this.attr({visibility: a ? "inherit" : "visible"});
            return this
        },
        hide: function () {
            return this.attr({visibility: "hidden"})
        },
        fadeOut: function (a) {
            var b = this;
            b.animate({opacity: 0}, {
                duration: a || 150, complete: function () {
                    b.attr({y: -9999})
                }
            })
        },
        add: function (a) {
            var b = this.renderer, c = this.element, d;
            if (a)this.parentGroup = a;
            this.parentInverted = a && a.inverted;
            this.textStr !== void 0 && b.buildText(this);
            this.added = !0;
            if (!a || a.handleZ || this.zIndex)d = this.zIndexSetter();
            d || (a ? a.element : b.box).appendChild(c);
            if (this.onAdd)this.onAdd();
            return this
        },
        safeRemoveChild: function (a) {
            var b = a.parentNode;
            b && b.removeChild(a)
        },
        destroy: function () {
            var a = this, b = a.element || {}, c = a.shadows, d = a.renderer.isSVG && b.nodeName === "SPAN" && a.parentGroup, e, f;
            b.onclick = b.onmouseout = b.onmouseover = b.onmousemove = b.point = null;
            gb(a);
            if (a.clipPath)a.clipPath = a.clipPath.destroy();
            if (a.stops) {
                for (f = 0; f < a.stops.length; f++)a.stops[f] = a.stops[f].destroy();
                a.stops = null
            }
            a.safeRemoveChild(b);
            for (c && n(c, function (b) {
                a.safeRemoveChild(b)
            }); d && d.div && d.div.childNodes.length === 0;)b = d.parentGroup, a.safeRemoveChild(d.div), delete d.div, d = b;
            a.alignTo && ua(a.renderer.alignedObjects, a);
            for (e in a)delete a[e];
            return null
        },
        shadow: function (a, b, c) {
            var d = [], e, f, g = this.element, h, i, j, k;
            if (a) {
                i = p(a.width, 3);
                j = (a.opacity || 0.15) / i;
                k = this.parentInverted ? "(-1,-1)" : "(" + p(a.offsetX, 1) + ", " + p(a.offsetY, 1) + ")";
                for (e = 1; e <= i; e++) {
                    f = g.cloneNode(0);
                    h = i * 2 + 1 - 2 * e;
                    W(f, {
                        isShadow: "true",
                        stroke: a.color || "black",
                        "stroke-opacity": j * e,
                        "stroke-width": h,
                        transform: "translate" + k,
                        fill: Z
                    });
                    if (c)W(f, "height", v(W(f, "height") - h, 0)), f.cutHeight = h;
                    b ? b.element.appendChild(f) : g.parentNode.insertBefore(f, g);
                    d.push(f)
                }
                this.shadows = d
            }
            return this
        },
        xGetter: function (a) {
            this.element.nodeName === "circle" && (a = {x: "cx", y: "cy"}[a] || a);
            return this._defaultGetter(a)
        },
        _defaultGetter: function (a) {
            a = p(this[a], this.element ? this.element.getAttribute(a) : null, 0);
            /^[\-0-9\.]+$/.test(a) && (a = parseFloat(a));
            return a
        },
        dSetter: function (a,
                           b, c) {
            a && a.join && (a = a.join(" "));
            /(NaN| {2}|^$)/.test(a) && (a = "M 0 0");
            c.setAttribute(b, a);
            this[b] = a
        },
        dashstyleSetter: function (a) {
            var b;
            if (a = a && a.toLowerCase()) {
                a = a.replace("shortdashdotdot", "3,1,1,1,1,1,").replace("shortdashdot", "3,1,1,1").replace("shortdot", "1,1,").replace("shortdash", "3,1,").replace("longdash", "8,3,").replace(/dot/g, "1,3,").replace("dash", "4,3,").replace(/,$/, "").split(",");
                for (b = a.length; b--;)a[b] = C(a[b]) * this["stroke-width"];
                a = a.join(",").replace("NaN", "none");
                this.element.setAttribute("stroke-dasharray",
                    a)
            }
        },
        alignSetter: function (a) {
            this.element.setAttribute("text-anchor", {left: "start", center: "middle", right: "end"}[a])
        },
        opacitySetter: function (a, b, c) {
            this[b] = a;
            c.setAttribute(b, a)
        },
        titleSetter: function (a) {
            var b = this.element.getElementsByTagName("title")[0];
            b || (b = E.createElementNS(Ha, "title"), this.element.appendChild(b));
            b.textContent = String(p(a), "").replace(/<[^>]*>/g, "")
        },
        textSetter: function (a) {
            if (a !== this.textStr)delete this.bBox, this.textStr = a, this.added && this.renderer.buildText(this)
        },
        fillSetter: function (a,
                              b, c) {
            typeof a === "string" ? c.setAttribute(b, a) : a && this.colorGradient(a, b, c)
        },
        zIndexSetter: function (a, b) {
            var c = this.renderer, d = this.parentGroup, c = (d || c).element || c.box, e, f, g = this.element, h, i;
            s(a) && (g.setAttribute(b, a), this[b] = +a);
            if (this.added) {
                if ((a = this.zIndex) && d)d.handleZ = !0;
                d = c.childNodes;
                for (i = 0; i < d.length && !h; i++)if (e = d[i], f = W(e, "zIndex"), e !== g && (C(f) > a || !s(a) && s(f)))c.insertBefore(g, e), h = !0;
                h || c.appendChild(g)
            }
            return h
        },
        _defaultSetter: function (a, b, c) {
            c.setAttribute(b, a)
        }
    };
    $.prototype.yGetter =
        $.prototype.xGetter;
    $.prototype.translateXSetter = $.prototype.translateYSetter = $.prototype.rotationSetter = $.prototype.verticalAlignSetter = $.prototype.scaleXSetter = $.prototype.scaleYSetter = function (a, b) {
        this[b] = a;
        this.doTransform = !0
    };
    $.prototype["stroke-widthSetter"] = $.prototype.strokeSetter = function (a, b, c) {
        this[b] = a;
        if (this.stroke && this["stroke-width"])this.strokeWidth = this["stroke-width"], $.prototype.fillSetter.call(this, this.stroke, "stroke", c), c.setAttribute("stroke-width", this["stroke-width"]),
            this.hasStroke = !0; else if (b === "stroke-width" && a === 0 && this.hasStroke)c.removeAttribute("stroke"), this.hasStroke = !1
    };
    var na = function () {
        this.init.apply(this, arguments)
    };
    na.prototype = {
        Element: $, init: function (a, b, c, d, e) {
            var f = location, g, d = this.createElement("svg").attr({version: "1.1"}).css(this.getStyle(d));
            g = d.element;
            a.appendChild(g);
            a.innerHTML.indexOf("xmlns") === -1 && W(g, "xmlns", Ha);
            this.isSVG = !0;
            this.box = g;
            this.boxWrapper = d;
            this.alignedObjects = [];
            this.url = (Va || Gb) && E.getElementsByTagName("base").length ?
                f.href.replace(/#.*?$/, "").replace(/([\('\)])/g, "\\$1").replace(/ /g, "%20") : "";
            this.createElement("desc").add().element.appendChild(E.createTextNode("Created with Highstock 2.1.2"));
            this.defs = this.createElement("defs").add();
            this.forExport = e;
            this.gradients = {};
            this.cache = {};
            this.setSize(b, c, !1);
            var h;
            if (Va && a.getBoundingClientRect)this.subPixelFix = b = function () {
                M(a, {left: 0, top: 0});
                h = a.getBoundingClientRect();
                M(a, {left: ya(h.left) - h.left + "px", top: ya(h.top) - h.top + "px"})
            }, b(), D(T, "resize", b)
        }, getStyle: function (a) {
            return this.style =
                x({
                    fontFamily: '"Lucida Grande", "Lucida Sans Unicode", Arial, Helvetica, sans-serif',
                    fontSize: "12px"
                }, a)
        }, isHidden: function () {
            return !this.boxWrapper.getBBox().width
        }, destroy: function () {
            var a = this.defs;
            this.box = null;
            this.boxWrapper = this.boxWrapper.destroy();
            Na(this.gradients || {});
            this.gradients = null;
            if (a)this.defs = a.destroy();
            this.subPixelFix && U(T, "resize", this.subPixelFix);
            return this.alignedObjects = null
        }, createElement: function (a) {
            var b = new this.Element;
            b.init(this, a);
            return b
        }, draw: function () {
        },
        buildText: function (a) {
            for (var b = a.element, c = this, d = c.forExport, e = p(a.textStr, "").toString(), f = e.indexOf("<") !== -1, g = b.childNodes, h, i, j = W(b, "x"), k = a.styles, l = a.textWidth, m = k && k.lineHeight, o = k && k.textShadow, q = k && k.textOverflow === "ellipsis", t = g.length, J = l && !a.added && this.box, N = function (a) {
                return m ? C(m) : c.fontMetrics(/(px|em)$/.test(a && a.style.fontSize) ? a.style.fontSize : k && k.fontSize || c.style.fontSize || 12, a).h
            }, u = function (a) {
                return a.replace(/&lt;/g, "<").replace(/&gt;/g, ">")
            }; t--;)b.removeChild(g[t]);
            !f && !o && !q && e.indexOf(" ") === -1 ? b.appendChild(E.createTextNode(u(e))) : (h = /<.*style="([^"]+)".*>/, i = /<.*href="(http[^"]+)".*>/, J && J.appendChild(b), e = f ? e.replace(/<(b|strong)>/g, '<span style="font-weight:bold">').replace(/<(i|em)>/g, '<span style="font-style:italic">').replace(/<a/g, "<span").replace(/<\/(b|strong|i|em|a)>/g, "</span>").split(/<br.*?>/g) : [e], e[e.length - 1] === "" && e.pop(), n(e, function (e, f) {
                var g, m = 0, e = e.replace(/<span/g, "|||<span").replace(/<\/span>/g, "</span>|||");
                g = e.split("|||");
                n(g, function (e) {
                    if (e !==
                        "" || g.length === 1) {
                        var o = {}, t = E.createElementNS(Ha, "tspan"), J;
                        h.test(e) && (J = e.match(h)[1].replace(/(;| |^)color([ :])/, "$1fill$2"), W(t, "style", J));
                        i.test(e) && !d && (W(t, "onclick", 'location.href="' + e.match(i)[1] + '"'), M(t, {cursor: "pointer"}));
                        e = u(e.replace(/<(.|\n)*?>/g, "") || " ");
                        if (e !== " ") {
                            t.appendChild(E.createTextNode(e));
                            if (m)o.dx = 0; else if (f && j !== null)o.x = j;
                            W(t, o);
                            b.appendChild(t);
                            !m && f && (!da && d && M(t, {display: "block"}), W(t, "dy", N(t)));
                            if (l) {
                                for (var o = e.replace(/([^\^])-/g, "$1- ").split(" "), n = g.length >
                                    1 || f || o.length > 1 && k.whiteSpace !== "nowrap", p, A, r, s = [], w = N(t), v = 1, x = a.rotation, y = e, B = y.length; (n || q) && (o.length || s.length);)a.rotation = 0, p = a.getBBox(!0), r = p.width, !da && c.forExport && (r = c.measureSpanWidth(t.firstChild.data, a.styles)), p = r > l, A === void 0 && (A = p), q && A ? (B /= 2, y === "" || !p && B < 0.5 ? o = [] : (p && (A = !0), y = e.substring(0, y.length + (p ? -1 : 1) * ya(B)), o = [y + "…"], t.removeChild(t.firstChild))) : !p || o.length === 1 ? (o = s, s = [], o.length && (v++, t = E.createElementNS(Ha, "tspan"), W(t, {
                                    dy: w,
                                    x: j
                                }), J && W(t, "style", J), b.appendChild(t)),
                                r > l && (l = r)) : (t.removeChild(t.firstChild), s.unshift(o.pop())), o.length && t.appendChild(E.createTextNode(o.join(" ").replace(/- /g, "-")));
                                A && a.attr("title", a.textStr);
                                a.rotation = x
                            }
                            m++
                        }
                    }
                })
            }), J && J.removeChild(b), o && a.applyTextShadow && a.applyTextShadow(o))
        }, getContrast: function (a) {
            a = wa(a).rgba;
            return a[0] + a[1] + a[2] > 384 ? "#000" : "#FFF"
        }, button: function (a, b, c, d, e, f, g, h, i) {
            var j = this.label(a, b, c, i, null, null, null, null, "button"), k = 0, l, m, o, q, t, J, a = {
                x1: 0,
                y1: 0,
                x2: 0,
                y2: 1
            }, e = y({
                "stroke-width": 1, stroke: "#CCCCCC", fill: {
                    linearGradient: a,
                    stops: [[0, "#FEFEFE"], [1, "#F6F6F6"]]
                }, r: 2, padding: 5, style: {color: "black"}
            }, e);
            o = e.style;
            delete e.style;
            f = y(e, {stroke: "#68A", fill: {linearGradient: a, stops: [[0, "#FFF"], [1, "#ACF"]]}}, f);
            q = f.style;
            delete f.style;
            g = y(e, {stroke: "#68A", fill: {linearGradient: a, stops: [[0, "#9BD"], [1, "#CDF"]]}}, g);
            t = g.style;
            delete g.style;
            h = y(e, {style: {color: "#CCC"}}, h);
            J = h.style;
            delete h.style;
            D(j.element, Da ? "mouseover" : "mouseenter", function () {
                k !== 3 && j.attr(f).css(q)
            });
            D(j.element, Da ? "mouseout" : "mouseleave", function () {
                k !== 3 &&
                (l = [e, f, g][k], m = [o, q, t][k], j.attr(l).css(m))
            });
            j.setState = function (a) {
                (j.state = k = a) ? a === 2 ? j.attr(g).css(t) : a === 3 && j.attr(h).css(J) : j.attr(e).css(o)
            };
            return j.on("click", function () {
                k !== 3 && d.call(j)
            }).attr(e).css(x({cursor: "default"}, o))
        }, crispLine: function (a, b) {
            a[1] === a[4] && (a[1] = a[4] = w(a[1]) - b % 2 / 2);
            a[2] === a[5] && (a[2] = a[5] = w(a[2]) + b % 2 / 2);
            return a
        }, path: function (a) {
            var b = {fill: Z};
            Ka(a) ? b.d = a : ia(a) && x(b, a);
            return this.createElement("path").attr(b)
        }, circle: function (a, b, c) {
            a = ia(a) ? a : {x: a, y: b, r: c};
            b = this.createElement("circle");
            b.xSetter = function (a) {
                this.element.setAttribute("cx", a)
            };
            b.ySetter = function (a) {
                this.element.setAttribute("cy", a)
            };
            return b.attr(a)
        }, arc: function (a, b, c, d, e, f) {
            if (ia(a))b = a.y, c = a.r, d = a.innerR, e = a.start, f = a.end, a = a.x;
            a = this.symbol("arc", a || 0, b || 0, c || 0, c || 0, {innerR: d || 0, start: e || 0, end: f || 0});
            a.r = c;
            return a
        }, rect: function (a, b, c, d, e, f) {
            var e = ia(a) ? a.r : e, g = this.createElement("rect"), a = ia(a) ? a : a === r ? {} : {
                x: a,
                y: b,
                width: v(c, 0),
                height: v(d, 0)
            };
            if (f !== r)a.strokeWidth = f, a = g.crisp(a);
            if (e)a.r = e;
            g.rSetter = function (a) {
                W(this.element,
                    {rx: a, ry: a})
            };
            return g.attr(a)
        }, setSize: function (a, b, c) {
            var d = this.alignedObjects, e = d.length;
            this.width = a;
            this.height = b;
            for (this.boxWrapper[p(c, !0) ? "animate" : "attr"]({width: a, height: b}); e--;)d[e].align()
        }, g: function (a) {
            var b = this.createElement("g");
            return s(a) ? b.attr({"class": "highcharts-" + a}) : b
        }, image: function (a, b, c, d, e) {
            var f = {preserveAspectRatio: Z};
            arguments.length > 1 && x(f, {x: b, y: c, width: d, height: e});
            f = this.createElement("image").attr(f);
            f.element.setAttributeNS ? f.element.setAttributeNS("http://www.w3.org/1999/xlink",
                "href", a) : f.element.setAttribute("hc-svg-href", a);
            return f
        }, symbol: function (a, b, c, d, e, f) {
            var g, h = this.symbols[a], h = h && h(w(b), w(c), d, e, f), i = /^url\((.*?)\)$/, j, k;
            if (h)g = this.path(h), x(g, {
                symbolName: a,
                x: b,
                y: c,
                width: d,
                height: e
            }), f && x(g, f); else if (i.test(a))k = function (a, b) {
                a.element && (a.attr({
                    width: b[0],
                    height: b[1]
                }), a.alignByTranslate || a.translate(w((d - b[0]) / 2), w((e - b[1]) / 2)))
            }, j = a.match(i)[1], a = Sb[j] || f && f.width && f.height && [f.width, f.height], g = this.image(j).attr({
                x: b,
                y: c
            }), g.isImg = !0, a ? k(g, a) : (g.attr({
                width: 0,
                height: 0
            }), aa("img", {
                onload: function () {
                    k(g, Sb[j] = [this.width, this.height])
                }, src: j
            }));
            return g
        }, symbols: {
            circle: function (a, b, c, d) {
                var e = 0.166 * c;
                return ["M", a + c / 2, b, "C", a + c + e, b, a + c + e, b + d, a + c / 2, b + d, "C", a - e, b + d, a - e, b, a + c / 2, b, "Z"]
            }, square: function (a, b, c, d) {
                return ["M", a, b, "L", a + c, b, a + c, b + d, a, b + d, "Z"]
            }, triangle: function (a, b, c, d) {
                return ["M", a + c / 2, b, "L", a + c, b + d, a, b + d, "Z"]
            }, "triangle-down": function (a, b, c, d) {
                return ["M", a, b, "L", a + c, b, a + c / 2, b + d, "Z"]
            }, diamond: function (a, b, c, d) {
                return ["M", a + c / 2, b, "L", a + c, b + d / 2, a +
                c / 2, b + d, a, b + d / 2, "Z"]
            }, arc: function (a, b, c, d, e) {
                var f = e.start, c = e.r || c || d, g = e.end - 0.001, d = e.innerR, h = e.open, i = ba(f), j = fa(f), k = ba(g), g = fa(g), e = e.end - f < va ? 0 : 1;
                return ["M", a + c * i, b + c * j, "A", c, c, 0, e, 1, a + c * k, b + c * g, h ? "M" : "L", a + d * k, b + d * g, "A", d, d, 0, e, 0, a + d * i, b + d * j, h ? "" : "Z"]
            }, callout: function (a, b, c, d, e) {
                var f = B(e && e.r || 0, c, d), g = f + 6, h = e && e.anchorX, i = e && e.anchorY, e = w(e.strokeWidth || 0) % 2 / 2;
                a += e;
                b += e;
                e = ["M", a + f, b, "L", a + c - f, b, "C", a + c, b, a + c, b, a + c, b + f, "L", a + c, b + d - f, "C", a + c, b + d, a + c, b + d, a + c - f, b + d, "L", a + f, b + d, "C", a, b + d,
                    a, b + d, a, b + d - f, "L", a, b + f, "C", a, b, a, b, a + f, b];
                h && h > c && i > b + g && i < b + d - g ? e.splice(13, 3, "L", a + c, i - 6, a + c + 6, i, a + c, i + 6, a + c, b + d - f) : h && h < 0 && i > b + g && i < b + d - g ? e.splice(33, 3, "L", a, i + 6, a - 6, i, a, i - 6, a, b + f) : i && i > d && h > a + g && h < a + c - g ? e.splice(23, 3, "L", h + 6, b + d, h, b + d + 6, h - 6, b + d, a + f, b + d) : i && i < 0 && h > a + g && h < a + c - g && e.splice(3, 3, "L", h - 6, b, h, b - 6, h + 6, b, c - f, b);
                return e
            }
        }, clipRect: function (a, b, c, d) {
            var e = "highcharts-" + Hb++, f = this.createElement("clipPath").attr({id: e}).add(this.defs), a = this.rect(a, b, c, d, 0).add(f);
            a.id = e;
            a.clipPath = f;
            a.count =
                0;
            return a
        }, text: function (a, b, c, d) {
            var e = ma || !da && this.forExport, f = {};
            if (d && !this.forExport)return this.html(a, b, c);
            f.x = Math.round(b || 0);
            if (c)f.y = Math.round(c);
            if (a || a === 0)f.text = a;
            a = this.createElement("text").attr(f);
            e && a.css({position: "absolute"});
            if (!d)a.xSetter = function (a, b, c) {
                var d = c.getElementsByTagName("tspan"), e, f = c.getAttribute(b), m;
                for (m = 0; m < d.length; m++)e = d[m], e.getAttribute(b) === f && e.setAttribute(b, a);
                c.setAttribute(b, a)
            };
            return a
        }, fontMetrics: function (a, b) {
            a = a || this.style.fontSize;
            if (b &&
                T.getComputedStyle)b = b.element || b, a = T.getComputedStyle(b, "").fontSize;
            var a = /px/.test(a) ? C(a) : /em/.test(a) ? parseFloat(a) * 12 : 12, c = a < 24 ? a + 3 : w(a * 1.2), d = w(c * 0.8);
            return {h: c, b: d, f: a}
        }, rotCorr: function (a, b, c) {
            var d = a;
            b && c && (d = v(d * ba(b * ra), 4));
            return {x: -a / 3 * fa(b * ra), y: d}
        }, label: function (a, b, c, d, e, f, g, h, i) {
            function j() {
                var a, b;
                a = q.element.style;
                J = (oa === void 0 || v === void 0 || o.styles.textAlign) && s(q.textStr) && q.getBBox();
                o.width = (oa || J.width || 0) + 2 * u + A;
                o.height = (v || J.height || 0) + 2 * u;
                D = u + m.fontMetrics(a && a.fontSize,
                        q).b;
                if (E) {
                    if (!t)a = w(-p * u), b = h ? -D : 0, o.box = t = d ? m.symbol(d, a, b, o.width, o.height, L) : m.rect(a, b, o.width, o.height, 0, L[Yb]), t.attr("fill", Z).add(o);
                    t.isImg || t.attr(x({width: w(o.width), height: w(o.height)}, L));
                    L = null
                }
            }

            function k() {
                var a = o.styles, a = a && a.textAlign, b = A + u * (1 - p), c;
                c = h ? 0 : D;
                if (s(oa) && J && (a === "center" || a === "right"))b += {center: 0.5, right: 1}[a] * (oa - J.width);
                if (b !== q.x || c !== q.y)q.attr("x", b), c !== r && q.attr("y", c);
                q.x = b;
                q.y = c
            }

            function l(a, b) {
                t ? t.attr(a, b) : L[a] = b
            }

            var m = this, o = m.g(i), q = m.text("", 0, 0, g).attr({zIndex: 1}),
                t, J, p = 0, u = 3, A = 0, oa, v, B, Jb, z = 0, L = {}, D, E;
            o.onAdd = function () {
                q.add(o);
                o.attr({text: a || a === 0 ? a : "", x: b, y: c});
                t && s(e) && o.attr({anchorX: e, anchorY: f})
            };
            o.widthSetter = function (a) {
                oa = a
            };
            o.heightSetter = function (a) {
                v = a
            };
            o.paddingSetter = function (a) {
                if (s(a) && a !== u)u = o.padding = a, k()
            };
            o.paddingLeftSetter = function (a) {
                s(a) && a !== A && (A = a, k())
            };
            o.alignSetter = function (a) {
                p = {left: 0, center: 0.5, right: 1}[a]
            };
            o.textSetter = function (a) {
                a !== r && q.textSetter(a);
                j();
                k()
            };
            o["stroke-widthSetter"] = function (a, b) {
                a && (E = !0);
                z = a % 2 / 2;
                l(b,
                    a)
            };
            o.strokeSetter = o.fillSetter = o.rSetter = function (a, b) {
                b === "fill" && a && (E = !0);
                l(b, a)
            };
            o.anchorXSetter = function (a, b) {
                e = a;
                l(b, a + z - B)
            };
            o.anchorYSetter = function (a, b) {
                f = a;
                l(b, a - Jb)
            };
            o.xSetter = function (a) {
                o.x = a;
                p && (a -= p * ((oa || J.width) + u));
                B = w(a);
                o.attr("translateX", B)
            };
            o.ySetter = function (a) {
                Jb = o.y = w(a);
                o.attr("translateY", Jb)
            };
            var C = o.css;
            return x(o, {
                css: function (a) {
                    if (a) {
                        var b = {}, a = y(a);
                        n(o.textProps, function (c) {
                            a[c] !== r && (b[c] = a[c], delete a[c])
                        });
                        q.css(b)
                    }
                    return C.call(o, a)
                }, getBBox: function () {
                    return {
                        width: J.width +
                        2 * u, height: J.height + 2 * u, x: J.x - u, y: J.y - u
                    }
                }, shadow: function (a) {
                    t && t.shadow(a);
                    return o
                }, destroy: function () {
                    U(o.element, "mouseenter");
                    U(o.element, "mouseleave");
                    q && (q = q.destroy());
                    t && (t = t.destroy());
                    $.prototype.destroy.call(o);
                    o = m = j = k = l = null
                }
            })
        }
    };
    Wa = na;
    x($.prototype, {
        htmlCss: function (a) {
            var b = this.element;
            if (b = a && b.tagName === "SPAN" && a.width)delete a.width, this.textWidth = b, this.updateTransform();
            if (a && a.textOverflow === "ellipsis")a.whiteSpace = "nowrap", a.overflow = "hidden";
            this.styles = x(this.styles, a);
            M(this.element,
                a);
            return this
        }, htmlGetBBox: function () {
            var a = this.element;
            if (a.nodeName === "text")a.style.position = "absolute";
            return {x: a.offsetLeft, y: a.offsetTop, width: a.offsetWidth, height: a.offsetHeight}
        }, htmlUpdateTransform: function () {
            if (this.added) {
                var a = this.renderer, b = this.element, c = this.translateX || 0, d = this.translateY || 0, e = this.x || 0, f = this.y || 0, g = this.textAlign || "left", h = {
                    left: 0,
                    center: 0.5,
                    right: 1
                }[g], i = this.shadows, j = this.styles;
                M(b, {marginLeft: c, marginTop: d});
                i && n(i, function (a) {
                    M(a, {
                        marginLeft: c + 1, marginTop: d +
                        1
                    })
                });
                this.inverted && n(b.childNodes, function (c) {
                    a.invertChild(c, b)
                });
                if (b.tagName === "SPAN") {
                    var k = this.rotation, l, m = C(this.textWidth), o = [k, g, b.innerHTML, this.textWidth].join(",");
                    if (o !== this.cTT) {
                        l = a.fontMetrics(b.style.fontSize).b;
                        s(k) && this.setSpanRotation(k, h, l);
                        i = p(this.elemWidth, b.offsetWidth);
                        if (i > m && /[ \-]/.test(b.textContent || b.innerText))M(b, {
                            width: m + "px",
                            display: "block",
                            whiteSpace: j && j.whiteSpace || "normal"
                        }), i = m;
                        this.getSpanCorrection(i, l, h, k, g)
                    }
                    M(b, {
                        left: e + (this.xCorr || 0) + "px", top: f + (this.yCorr ||
                        0) + "px"
                    });
                    if (Gb)l = b.offsetHeight;
                    this.cTT = o
                }
            } else this.alignOnAdd = !0
        }, setSpanRotation: function (a, b, c) {
            var d = {}, e = Da ? "-ms-transform" : Gb ? "-webkit-transform" : Va ? "MozTransform" : Rb ? "-o-transform" : "";
            d[e] = d.transform = "rotate(" + a + "deg)";
            d[e + (Va ? "Origin" : "-origin")] = d.transformOrigin = b * 100 + "% " + c + "px";
            M(this.element, d)
        }, getSpanCorrection: function (a, b, c) {
            this.xCorr = -a * c;
            this.yCorr = -b
        }
    });
    x(na.prototype, {
        html: function (a, b, c) {
            var d = this.createElement("span"), e = d.element, f = d.renderer;
            d.textSetter = function (a) {
                a !==
                e.innerHTML && delete this.bBox;
                e.innerHTML = this.textStr = a
            };
            d.xSetter = d.ySetter = d.alignSetter = d.rotationSetter = function (a, b) {
                b === "align" && (b = "textAlign");
                d[b] = a;
                d.htmlUpdateTransform()
            };
            d.attr({text: a, x: w(b), y: w(c)}).css({
                position: "absolute",
                fontFamily: this.style.fontFamily,
                fontSize: this.style.fontSize
            });
            e.style.whiteSpace = "nowrap";
            d.css = d.htmlCss;
            if (f.isSVG)d.add = function (a) {
                var b, c = f.box.parentNode, j = [];
                if (this.parentGroup = a) {
                    if (b = a.div, !b) {
                        for (; a;)j.push(a), a = a.parentGroup;
                        n(j.reverse(), function (a) {
                            var d;
                            b = a.div = a.div || aa(Ua, {className: W(a.element, "class")}, {
                                    position: "absolute",
                                    left: (a.translateX || 0) + "px",
                                    top: (a.translateY || 0) + "px"
                                }, b || c);
                            d = b.style;
                            x(a, {
                                translateXSetter: function (b, c) {
                                    d.left = b + "px";
                                    a[c] = b;
                                    a.doTransform = !0
                                }, translateYSetter: function (b, c) {
                                    d.top = b + "px";
                                    a[c] = b;
                                    a.doTransform = !0
                                }, visibilitySetter: function (a, b) {
                                    d[b] = a
                                }
                            })
                        })
                    }
                } else b = c;
                b.appendChild(e);
                d.added = !0;
                d.alignOnAdd && d.htmlUpdateTransform();
                return d
            };
            return d
        }
    });
    var hb;
    if (!da && !ma)G = {
        init: function (a, b) {
            var c = ["<", b, ' filled="f" stroked="f"'],
                d = ["position: ", "absolute", ";"], e = b === Ua;
            (b === "shape" || e) && d.push("left:0;top:0;width:1px;height:1px;");
            d.push("visibility: ", e ? "hidden" : "visible");
            c.push(' style="', d.join(""), '"/>');
            if (b)c = e || b === "span" || b === "img" ? c.join("") : a.prepVML(c), this.element = aa(c);
            this.renderer = a
        }, add: function (a) {
            var b = this.renderer, c = this.element, d = b.box, d = a ? a.element || a : d;
            a && a.inverted && b.invertChild(c, d);
            d.appendChild(c);
            this.added = !0;
            this.alignOnAdd && !this.deferUpdateTransform && this.updateTransform();
            if (this.onAdd)this.onAdd();
            return this
        }, updateTransform: $.prototype.htmlUpdateTransform, setSpanRotation: function () {
            var a = this.rotation, b = ba(a * ra), c = fa(a * ra);
            M(this.element, {filter: a ? ["progid:DXImageTransform.Microsoft.Matrix(M11=", b, ", M12=", -c, ", M21=", c, ", M22=", b, ", sizingMethod='auto expand')"].join("") : Z})
        }, getSpanCorrection: function (a, b, c, d, e) {
            var f = d ? ba(d * ra) : 1, g = d ? fa(d * ra) : 0, h = p(this.elemHeight, this.element.offsetHeight), i;
            this.xCorr = f < 0 && -a;
            this.yCorr = g < 0 && -h;
            i = f * g < 0;
            this.xCorr += g * b * (i ? 1 - c : c);
            this.yCorr -= f * b * (d ? i ?
                    c : 1 - c : 1);
            e && e !== "left" && (this.xCorr -= a * c * (f < 0 ? -1 : 1), d && (this.yCorr -= h * c * (g < 0 ? -1 : 1)), M(this.element, {textAlign: e}))
        }, pathToVML: function (a) {
            for (var b = a.length, c = []; b--;)if (sa(a[b]))c[b] = w(a[b] * 10) - 5; else if (a[b] === "Z")c[b] = "x"; else if (c[b] = a[b], a.isArc && (a[b] === "wa" || a[b] === "at"))c[b + 5] === c[b + 7] && (c[b + 7] += a[b + 7] > a[b + 5] ? 1 : -1), c[b + 6] === c[b + 8] && (c[b + 8] += a[b + 8] > a[b + 6] ? 1 : -1);
            return c.join(" ") || "x"
        }, clip: function (a) {
            var b = this, c;
            a ? (c = a.members, ua(c, b), c.push(b), b.destroyClip = function () {
                ua(c, b)
            }, a = a.getCSS(b)) :
                (b.destroyClip && b.destroyClip(), a = {clip: mb ? "inherit" : "rect(auto)"});
            return b.css(a)
        }, css: $.prototype.htmlCss, safeRemoveChild: function (a) {
            a.parentNode && Ta(a)
        }, destroy: function () {
            this.destroyClip && this.destroyClip();
            return $.prototype.destroy.apply(this)
        }, on: function (a, b) {
            this.element["on" + a] = function () {
                var a = T.event;
                a.target = a.srcElement;
                b(a)
            };
            return this
        }, cutOffPath: function (a, b) {
            var c, a = a.split(/[ ,]/);
            c = a.length;
            if (c === 9 || c === 11)a[c - 4] = a[c - 2] = C(a[c - 2]) - 10 * b;
            return a.join(" ")
        }, shadow: function (a,
                             b, c) {
            var d = [], e, f = this.element, g = this.renderer, h, i = f.style, j, k = f.path, l, m, o, q;
            k && typeof k.value !== "string" && (k = "x");
            m = k;
            if (a) {
                o = p(a.width, 3);
                q = (a.opacity || 0.15) / o;
                for (e = 1; e <= 3; e++) {
                    l = o * 2 + 1 - 2 * e;
                    c && (m = this.cutOffPath(k.value, l + 0.5));
                    j = ['<shape isShadow="true" strokeweight="', l, '" filled="false" path="', m, '" coordsize="10 10" style="', f.style.cssText, '" />'];
                    h = aa(g.prepVML(j), null, {left: C(i.left) + p(a.offsetX, 1), top: C(i.top) + p(a.offsetY, 1)});
                    if (c)h.cutOff = l + 1;
                    j = ['<stroke color="', a.color || "black", '" opacity="',
                        q * e, '"/>'];
                    aa(g.prepVML(j), null, null, h);
                    b ? b.element.appendChild(h) : f.parentNode.insertBefore(h, f);
                    d.push(h)
                }
                this.shadows = d
            }
            return this
        }, updateShadows: ga, setAttr: function (a, b) {
            mb ? this.element[a] = b : this.element.setAttribute(a, b)
        }, classSetter: function (a) {
            this.element.className = a
        }, dashstyleSetter: function (a, b, c) {
            (c.getElementsByTagName("stroke")[0] || aa(this.renderer.prepVML(["<stroke/>"]), null, null, c))[b] = a || "solid";
            this[b] = a
        }, dSetter: function (a, b, c) {
            var d = this.shadows, a = a || [];
            this.d = a.join && a.join(" ");
            c.path = a = this.pathToVML(a);
            if (d)for (c = d.length; c--;)d[c].path = d[c].cutOff ? this.cutOffPath(a, d[c].cutOff) : a;
            this.setAttr(b, a)
        }, fillSetter: function (a, b, c) {
            var d = c.nodeName;
            if (d === "SPAN")c.style.color = a; else if (d !== "IMG")c.filled = a !== Z, this.setAttr("fillcolor", this.renderer.color(a, c, b, this))
        }, opacitySetter: ga, rotationSetter: function (a, b, c) {
            c = c.style;
            this[b] = c[b] = a;
            c.left = -w(fa(a * ra) + 1) + "px";
            c.top = w(ba(a * ra)) + "px"
        }, strokeSetter: function (a, b, c) {
            this.setAttr("strokecolor", this.renderer.color(a, c, b))
        },
        "stroke-widthSetter": function (a, b, c) {
            c.stroked = !!a;
            this[b] = a;
            sa(a) && (a += "px");
            this.setAttr("strokeweight", a)
        }, titleSetter: function (a, b) {
            this.setAttr(b, a)
        }, visibilitySetter: function (a, b, c) {
            a === "inherit" && (a = "visible");
            this.shadows && n(this.shadows, function (c) {
                c.style[b] = a
            });
            c.nodeName === "DIV" && (a = a === "hidden" ? "-999em" : 0, mb || (c.style[b] = a ? "visible" : "hidden"), b = "top");
            c.style[b] = a
        }, xSetter: function (a, b, c) {
            this[b] = a;
            b === "x" ? b = "left" : b === "y" && (b = "top");
            this.updateClipping ? (this[b] = a, this.updateClipping()) :
                c.style[b] = a
        }, zIndexSetter: function (a, b, c) {
            c.style[b] = a
        }
    }, z.VMLElement = G = ja($, G), G.prototype.ySetter = G.prototype.widthSetter = G.prototype.heightSetter = G.prototype.xSetter, G = {
        Element: G, isIE8: Ga.indexOf("MSIE 8.0") > -1, init: function (a, b, c, d) {
            var e;
            this.alignedObjects = [];
            d = this.createElement(Ua).css(x(this.getStyle(d), {position: "relative"}));
            e = d.element;
            a.appendChild(d.element);
            this.isVML = !0;
            this.box = e;
            this.boxWrapper = d;
            this.cache = {};
            this.setSize(b, c, !1);
            if (!E.namespaces.hcv) {
                E.namespaces.add("hcv", "urn:schemas-microsoft-com:vml");
                try {
                    E.createStyleSheet().cssText = "hcv\\:fill, hcv\\:path, hcv\\:shape, hcv\\:stroke{ behavior:url(#default#VML); display: inline-block; } "
                } catch (f) {
                    E.styleSheets[0].cssText += "hcv\\:fill, hcv\\:path, hcv\\:shape, hcv\\:stroke{ behavior:url(#default#VML); display: inline-block; } "
                }
            }
        }, isHidden: function () {
            return !this.box.offsetWidth
        }, clipRect: function (a, b, c, d) {
            var e = this.createElement(), f = ia(a);
            return x(e, {
                members: [],
                count: 0,
                left: (f ? a.x : a) + 1,
                top: (f ? a.y : b) + 1,
                width: (f ? a.width : c) - 1,
                height: (f ? a.height :
                    d) - 1,
                getCSS: function (a) {
                    var b = a.element, c = b.nodeName, a = a.inverted, d = this.top - (c === "shape" ? b.offsetTop : 0), e = this.left, b = e + this.width, f = d + this.height, d = {clip: "rect(" + w(a ? e : d) + "px," + w(a ? f : b) + "px," + w(a ? b : f) + "px," + w(a ? d : e) + "px)"};
                    !a && mb && c === "DIV" && x(d, {width: b + "px", height: f + "px"});
                    return d
                },
                updateClipping: function () {
                    n(e.members, function (a) {
                        a.element && a.css(e.getCSS(a))
                    })
                }
            })
        }, color: function (a, b, c, d) {
            var e = this, f, g = /^rgba/, h, i, j = Z;
            a && a.linearGradient ? i = "gradient" : a && a.radialGradient && (i = "pattern");
            if (i) {
                var k,
                    l, m = a.linearGradient || a.radialGradient, o, q, t, J, p, u = "", a = a.stops, A, oa = [], r = function () {
                        h = ['<fill colors="' + oa.join(",") + '" opacity="', t, '" o:opacity2="', q, '" type="', i, '" ', u, 'focus="100%" method="any" />'];
                        aa(e.prepVML(h), null, null, b)
                    };
                o = a[0];
                A = a[a.length - 1];
                o[0] > 0 && a.unshift([0, o[1]]);
                A[0] < 1 && a.push([1, A[1]]);
                n(a, function (a, b) {
                    g.test(a[1]) ? (f = wa(a[1]), k = f.get("rgb"), l = f.get("a")) : (k = a[1], l = 1);
                    oa.push(a[0] * 100 + "% " + k);
                    b ? (t = l, J = k) : (q = l, p = k)
                });
                if (c === "fill")if (i === "gradient")c = m.x1 || m[0] || 0, a = m.y1 ||
                    m[1] || 0, o = m.x2 || m[2] || 0, m = m.y2 || m[3] || 0, u = 'angle="' + (90 - Y.atan((m - a) / (o - c)) * 180 / va) + '"', r(); else {
                    var j = m.r, s = j * 2, w = j * 2, v = m.cx, x = m.cy, y = b.radialReference, B, j = function () {
                        y && (B = d.getBBox(), v += (y[0] - B.x) / B.width - 0.5, x += (y[1] - B.y) / B.height - 0.5, s *= y[2] / B.width, w *= y[2] / B.height);
                        u = 'src="' + F.global.VMLRadialGradientURL + '" size="' + s + "," + w + '" origin="0.5,0.5" position="' + v + "," + x + '" color2="' + p + '" ';
                        r()
                    };
                    d.added ? j() : d.onAdd = j;
                    j = J
                } else j = k
            } else if (g.test(a) && b.tagName !== "IMG")f = wa(a), h = ["<", c, ' opacity="', f.get("a"),
                '"/>'], aa(this.prepVML(h), null, null, b), j = f.get("rgb"); else {
                j = b.getElementsByTagName(c);
                if (j.length)j[0].opacity = 1, j[0].type = "solid";
                j = a
            }
            return j
        }, prepVML: function (a) {
            var b = this.isIE8, a = a.join("");
            b ? (a = a.replace("/>", ' xmlns="urn:schemas-microsoft-com:vml" />'), a = a.indexOf('style="') === -1 ? a.replace("/>", ' style="display:inline-block;behavior:url(#default#VML);" />') : a.replace('style="', 'style="display:inline-block;behavior:url(#default#VML);')) : a = a.replace("<", "<hcv:");
            return a
        }, text: na.prototype.html,
        path: function (a) {
            var b = {coordsize: "10 10"};
            Ka(a) ? b.d = a : ia(a) && x(b, a);
            return this.createElement("shape").attr(b)
        }, circle: function (a, b, c) {
            var d = this.symbol("circle");
            if (ia(a))c = a.r, b = a.y, a = a.x;
            d.isCircle = !0;
            d.r = c;
            return d.attr({x: a, y: b})
        }, g: function (a) {
            var b;
            a && (b = {className: "highcharts-" + a, "class": "highcharts-" + a});
            return this.createElement(Ua).attr(b)
        }, image: function (a, b, c, d, e) {
            var f = this.createElement("img").attr({src: a});
            arguments.length > 1 && f.attr({x: b, y: c, width: d, height: e});
            return f
        }, createElement: function (a) {
            return a ===
            "rect" ? this.symbol(a) : na.prototype.createElement.call(this, a)
        }, invertChild: function (a, b) {
            var c = this, d = b.style, e = a.tagName === "IMG" && a.style;
            M(a, {
                flip: "x",
                left: C(d.width) - (e ? C(e.top) : 1),
                top: C(d.height) - (e ? C(e.left) : 1),
                rotation: -90
            });
            n(a.childNodes, function (b) {
                c.invertChild(b, a)
            })
        }, symbols: {
            arc: function (a, b, c, d, e) {
                var f = e.start, g = e.end, h = e.r || c || d, c = e.innerR, d = ba(f), i = fa(f), j = ba(g), k = fa(g);
                if (g - f === 0)return ["x"];
                f = ["wa", a - h, b - h, a + h, b + h, a + h * d, b + h * i, a + h * j, b + h * k];
                e.open && !c && f.push("e", "M", a, b);
                f.push("at",
                    a - c, b - c, a + c, b + c, a + c * j, b + c * k, a + c * d, b + c * i, "x", "e");
                f.isArc = !0;
                return f
            }, circle: function (a, b, c, d, e) {
                e && (c = d = 2 * e.r);
                e && e.isCircle && (a -= c / 2, b -= d / 2);
                return ["wa", a, b, a + c, b + d, a + c, b + d / 2, a + c, b + d / 2, "e"]
            }, rect: function (a, b, c, d, e) {
                return na.prototype.symbols[!s(e) || !e.r ? "square" : "callout"].call(0, a, b, c, d, e)
            }
        }
    }, z.VMLRenderer = hb = function () {
        this.init.apply(this, arguments)
    }, hb.prototype = y(na.prototype, G), Wa = hb;
    na.prototype.measureSpanWidth = function (a, b) {
        var c = E.createElement("span"), d;
        d = E.createTextNode(a);
        c.appendChild(d);
        M(c, b);
        this.box.appendChild(c);
        d = c.offsetWidth;
        Ta(c);
        return d
    };
    var Tb;
    if (ma)z.CanVGRenderer = G = function () {
        Ha = "http://www.w3.org/1999/xhtml"
    }, G.prototype.symbols = {}, Tb = function () {
        function a() {
            var a = b.length, d;
            for (d = 0; d < a; d++)b[d]();
            b = []
        }

        var b = [];
        return {
            push: function (c, d) {
                b.length === 0 && Zb(d, a);
                b.push(c)
            }
        }
    }(), Wa = G;
    Za.prototype = {
        addLabel: function () {
            var a = this.axis, b = a.options, c = a.chart, d = a.categories, e = a.names, f = this.pos, g = b.labels, h = a.tickPositions, i = f === h[0], j = f === h[h.length - 1], e = d ? p(d[f], e[f], f) : f,
                d = this.label, h = h.info, k;
            a.isDatetimeAxis && h && (k = b.dateTimeLabelFormats[h.higherRanks[f] || h.unitName]);
            this.isFirst = i;
            this.isLast = j;
            b = a.labelFormatter.call({
                axis: a,
                chart: c,
                isFirst: i,
                isLast: j,
                dateTimeLabelFormat: k,
                value: a.isLog ? la(ta(e)) : e
            });
            s(d) ? d && d.attr({text: b}) : (this.labelLength = (this.label = d = s(b) && g.enabled ? c.renderer.text(b, 0, 0, g.useHTML).css(y(g.style)).add(a.labelGroup) : null) && d.getBBox().width, this.rotation = 0)
        }, getLabelSize: function () {
            return this.label ? this.label.getBBox()[this.axis.horiz ?
                "height" : "width"] : 0
        }, handleOverflow: function (a) {
            var b = this.axis, c = a.x, d = b.chart.chartWidth, e = b.chart.spacing, f = p(b.labelLeft, e[3]), e = p(b.labelRight, d - e[1]), g = this.label, h = this.rotation, i = {
                left: 0,
                center: 0.5,
                right: 1
            }[b.labelAlign], j = g.getBBox().width, b = b.slotWidth, k;
            if (h)h < 0 && c - i * j < f ? k = w(c / ba(h * ra) - f) : h > 0 && c + i * j > e && (k = w((d - c) / ba(h * ra))); else {
                d = c - i * j;
                c += i * j;
                if (d < f)b -= f - d, a.x = f, g.attr({align: "left"}); else if (c > e)b -= c - e, a.x = e, g.attr({align: "right"});
                j > b && (k = b)
            }
            k && g.css({width: k, textOverflow: "ellipsis"})
        },
        getPosition: function (a, b, c, d) {
            var e = this.axis, f = e.chart, g = d && f.oldChartHeight || f.chartHeight;
            return {
                x: a ? e.translate(b + c, null, null, d) + e.transB : e.left + e.offset + (e.opposite ? (d && f.oldChartWidth || f.chartWidth) - e.right - e.left : 0),
                y: a ? g - e.bottom + e.offset - (e.opposite ? e.height : 0) : g - e.translate(b + c, null, null, d) - e.transB
            }
        }, getLabelPosition: function (a, b, c, d, e, f, g, h) {
            var i = this.axis, j = i.transA, k = i.reversed, l = i.staggerLines, m = i.tickRotCorr || {
                    x: 0,
                    y: 0
                }, c = p(e.y, m.y + (i.side === 2 ? 8 : -(c.getBBox().height / 2))), a = a + e.x + m.x -
                (f && d ? f * j * (k ? -1 : 1) : 0), b = b + c - (f && !d ? f * j * (k ? 1 : -1) : 0);
            l && (b += g / (h || 1) % l * (i.labelOffset / l));
            return {x: a, y: w(b)}
        }, getMarkPath: function (a, b, c, d, e, f) {
            return f.crispLine(["M", a, b, "L", a + (e ? 0 : -c), b + (e ? c : 0)], d)
        }, render: function (a, b, c) {
            var d = this.axis, e = d.options, f = d.chart.renderer, g = d.horiz, h = this.type, i = this.label, j = this.pos, k = e.labels, l = this.gridLine, m = h ? h + "Grid" : "grid", o = h ? h + "Tick" : "tick", q = e[m + "LineWidth"], t = e[m + "LineColor"], J = e[m + "LineDashStyle"], n = e[o + "Length"], m = e[o + "Width"] || 0, u = e[o + "Color"], A = e[o + "Position"],
                o = this.mark, oa = k.step, s = !0, w = d.tickmarkOffset, v = this.getPosition(g, j, w, b), x = v.x, v = v.y, y = g && x === d.pos + d.len || !g && v === d.pos ? -1 : 1, c = p(c, 1);
            this.isActive = !0;
            if (q) {
                j = d.getPlotLinePath(j + w, q * y, b, !0);
                if (l === r) {
                    l = {stroke: t, "stroke-width": q};
                    if (J)l.dashstyle = J;
                    if (!h)l.zIndex = 1;
                    if (b)l.opacity = 0;
                    this.gridLine = l = q ? f.path(j).attr(l).add(d.gridGroup) : null
                }
                if (!b && l && j)l[this.isNew ? "attr" : "animate"]({d: j, opacity: c})
            }
            if (m && n)A === "inside" && (n = -n), d.opposite && (n = -n), h = this.getMarkPath(x, v, n, m * y, g, f), o ? o.animate({
                d: h,
                opacity: c
            }) : this.mark = f.path(h).attr({stroke: u, "stroke-width": m, opacity: c}).add(d.axisGroup);
            if (i && !isNaN(x))i.xy = v = this.getLabelPosition(x, v, i, g, k, w, a, oa), this.isFirst && !this.isLast && !p(e.showFirstLabel, 1) || this.isLast && !this.isFirst && !p(e.showLastLabel, 1) ? s = !1 : g && !d.isRadial && !k.step && !k.rotation && !b && c !== 0 && this.handleOverflow(v), oa && a % oa && (s = !1), s && !isNaN(v.y) ? (v.opacity = c, i[this.isNew ? "attr" : "animate"](v), this.isNew = !1) : i.attr("y", -9999)
        }, destroy: function () {
            Na(this, this.axis)
        }
    };
    z.PlotLineOrBand =
        function (a, b) {
            this.axis = a;
            if (b)this.options = b, this.id = b.id
        };
    z.PlotLineOrBand.prototype = {
        render: function () {
            var a = this, b = a.axis, c = b.horiz, d = a.options, e = d.label, f = a.label, g = d.width, h = d.to, i = d.from, j = s(i) && s(h), k = d.value, l = d.dashStyle, m = a.svgElem, o = [], q, t = d.color, p = d.zIndex, n = d.events, u = {}, A = b.chart.renderer;
            b.isLog && (i = La(i), h = La(h), k = La(k));
            if (g) {
                if (o = b.getPlotLinePath(k, g), u = {stroke: t, "stroke-width": g}, l)u.dashstyle = l
            } else if (j) {
                o = b.getPlotBandPath(i, h, d);
                if (t)u.fill = t;
                if (d.borderWidth)u.stroke = d.borderColor,
                    u["stroke-width"] = d.borderWidth
            } else return;
            if (s(p))u.zIndex = p;
            if (m)if (o)m.animate({d: o}, null, m.onGetPath); else {
                if (m.hide(), m.onGetPath = function () {
                        m.show()
                    }, f)a.label = f = f.destroy()
            } else if (o && o.length && (a.svgElem = m = A.path(o).attr(u).add(), n))for (q in d = function (b) {
                m.on(b, function (c) {
                    n[b].apply(a, [c])
                })
            }, n)d(q);
            if (e && s(e.text) && o && o.length && b.width > 0 && b.height > 0) {
                e = y({
                    align: c && j && "center",
                    x: c ? !j && 4 : 10,
                    verticalAlign: !c && j && "middle",
                    y: c ? j ? 16 : 10 : j ? 6 : -4,
                    rotation: c && !j && 90
                }, e);
                if (!f) {
                    u = {
                        align: e.textAlign ||
                        e.align, rotation: e.rotation
                    };
                    if (s(p))u.zIndex = p;
                    a.label = f = A.text(e.text, 0, 0, e.useHTML).attr(u).css(e.style).add()
                }
                b = [o[1], o[4], j ? o[6] : o[1]];
                j = [o[2], o[5], j ? o[7] : o[2]];
                o = Sa(b);
                c = Sa(j);
                f.align(e, !1, {x: o, y: c, width: Ea(b) - o, height: Ea(j) - c});
                f.show()
            } else f && f.hide();
            return a
        }, destroy: function () {
            ua(this.axis.plotLinesAndBands, this);
            delete this.axis;
            Na(this)
        }
    };
    var O = z.Axis = function () {
        this.init.apply(this, arguments)
    };
    O.prototype = {
        defaultOptions: {
            dateTimeLabelFormats: {
                millisecond: "%H:%M:%S.%L", second: "%H:%M:%S",
                minute: "%H:%M", hour: "%H:%M", day: "%e. %b", week: "%e. %b", month: "%b '%y", year: "%Y"
            },
            endOnTick: !1,
            gridLineColor: "#D8D8D8",
            labels: {enabled: !0, style: {color: "#606060", cursor: "default", fontSize: "11px"}, x: 0, y: 15},
            lineColor: "#C0D0E0",
            lineWidth: 1,
            minPadding: 0.01,
            maxPadding: 0.01,
            minorGridLineColor: "#E0E0E0",
            minorGridLineWidth: 1,
            minorTickColor: "#A0A0A0",
            minorTickLength: 2,
            minorTickPosition: "outside",
            startOfWeek: 1,
            startOnTick: !1,
            tickColor: "#C0D0E0",
            tickLength: 10,
            tickmarkPlacement: "between",
            tickPixelInterval: 100,
            tickPosition: "outside",
            tickWidth: 1,
            title: {align: "middle", style: {color: "#707070"}},
            type: "linear"
        },
        defaultYAxisOptions: {
            endOnTick: !0,
            gridLineWidth: 1,
            tickPixelInterval: 72,
            showLastLabel: !0,
            labels: {x: -8, y: 3},
            lineWidth: 0,
            maxPadding: 0.05,
            minPadding: 0.05,
            startOnTick: !0,
            tickWidth: 0,
            title: {rotation: 270, text: "Values"},
            stackLabels: {
                enabled: !1, formatter: function () {
                    return z.numberFormat(this.total, -1)
                }, style: y(V.line.dataLabels.style, {color: "#000000"})
            }
        },
        defaultLeftAxisOptions: {labels: {x: -15, y: null}, title: {rotation: 270}},
        defaultRightAxisOptions: {labels: {x: 15, y: null}, title: {rotation: 90}},
        defaultBottomAxisOptions: {labels: {autoRotation: [-45], x: 0, y: null}, title: {rotation: 0}},
        defaultTopAxisOptions: {labels: {autoRotation: [-45], x: 0, y: -15}, title: {rotation: 0}},
        init: function (a, b) {
            var c = b.isX;
            this.horiz = a.inverted ? !c : c;
            this.coll = (this.isXAxis = c) ? "xAxis" : "yAxis";
            this.opposite = b.opposite;
            this.side = b.side || (this.horiz ? this.opposite ? 0 : 2 : this.opposite ? 1 : 3);
            this.setOptions(b);
            var d = this.options, e = d.type;
            this.labelFormatter = d.labels.formatter ||
                this.defaultLabelFormatter;
            this.userOptions = b;
            this.minPixelPadding = 0;
            this.chart = a;
            this.reversed = d.reversed;
            this.zoomEnabled = d.zoomEnabled !== !1;
            this.categories = d.categories || e === "category";
            this.names = this.names || [];
            this.isLog = e === "logarithmic";
            this.isDatetimeAxis = e === "datetime";
            this.isLinked = s(d.linkedTo);
            this.ticks = {};
            this.labelEdge = [];
            this.minorTicks = {};
            this.plotLinesAndBands = [];
            this.alternateBands = {};
            this.len = 0;
            this.minRange = this.userMinRange = d.minRange || d.maxZoom;
            this.range = d.range;
            this.offset =
                d.offset || 0;
            this.stacks = {};
            this.oldStacks = {};
            this.min = this.max = null;
            this.crosshair = p(d.crosshair, pa(a.options.tooltip.crosshairs)[c ? 0 : 1], !1);
            var f, d = this.options.events;
            Oa(this, a.axes) === -1 && (c && !this.isColorAxis ? a.axes.splice(a.xAxis.length, 0, this) : a.axes.push(this), a[this.coll].push(this));
            this.series = this.series || [];
            if (a.inverted && c && this.reversed === r)this.reversed = !0;
            this.removePlotLine = this.removePlotBand = this.removePlotBandOrLine;
            for (f in d)D(this, f, d[f]);
            if (this.isLog)this.val2lin = La, this.lin2val =
                ta
        },
        setOptions: function (a) {
            this.options = y(this.defaultOptions, this.isXAxis ? {} : this.defaultYAxisOptions, [this.defaultTopAxisOptions, this.defaultRightAxisOptions, this.defaultBottomAxisOptions, this.defaultLeftAxisOptions][this.side], y(F[this.coll], a))
        },
        defaultLabelFormatter: function () {
            var a = this.axis, b = this.value, c = a.categories, d = this.dateTimeLabelFormat, e = F.lang.numericSymbols, f = e && e.length, g, h = a.options.labels.format, a = a.isLog ? b : a.tickInterval;
            if (h)g = Ma(h, this); else if (c)g = b; else if (d)g = ka(d, b); else if (f &&
                a >= 1E3)for (; f-- && g === r;)c = Math.pow(1E3, f + 1), a >= c && e[f] !== null && (g = z.numberFormat(b / c, -1) + e[f]);
            g === r && (g = R(b) >= 1E4 ? z.numberFormat(b, 0) : z.numberFormat(b, -1, r, ""));
            return g
        },
        getSeriesExtremes: function () {
            var a = this, b = a.chart;
            a.hasVisibleSeries = !1;
            a.dataMin = a.dataMax = a.ignoreMinPadding = a.ignoreMaxPadding = null;
            a.buildStacks && a.buildStacks();
            n(a.series, function (c) {
                if (c.visible || !b.options.chart.ignoreHiddenSeries) {
                    var d;
                    d = c.options.threshold;
                    var e;
                    a.hasVisibleSeries = !0;
                    a.isLog && d <= 0 && (d = null);
                    if (a.isXAxis) {
                        if (d =
                                c.xData, d.length)a.dataMin = B(p(a.dataMin, d[0]), Sa(d)), a.dataMax = v(p(a.dataMax, d[0]), Ea(d))
                    } else {
                        c.getExtremes();
                        e = c.dataMax;
                        c = c.dataMin;
                        if (s(c) && s(e))a.dataMin = B(p(a.dataMin, c), c), a.dataMax = v(p(a.dataMax, e), e);
                        if (s(d))if (a.dataMin >= d)a.dataMin = d, a.ignoreMinPadding = !0; else if (a.dataMax < d)a.dataMax = d, a.ignoreMaxPadding = !0
                    }
                }
            })
        },
        translate: function (a, b, c, d, e, f) {
            var g = 1, h = 0, i = d ? this.oldTransA : this.transA, d = d ? this.oldMin : this.min, j = this.minPixelPadding, e = (this.doPostTranslate || this.isLog && e) && this.lin2val;
            if (!i)i = this.transA;
            if (c)g *= -1, h = this.len;
            this.reversed && (g *= -1, h -= g * (this.sector || this.len));
            b ? (a = a * g + h, a -= j, a = a / i + d, e && (a = this.lin2val(a))) : (e && (a = this.val2lin(a)), f === "between" && (f = 0.5), a = g * (a - d) * i + h + g * j + (sa(f) ? i * f * this.pointRange : 0));
            return a
        },
        toPixels: function (a, b) {
            return this.translate(a, !1, !this.horiz, null, !0) + (b ? 0 : this.pos)
        },
        toValue: function (a, b) {
            return this.translate(a - (b ? 0 : this.pos), !0, !this.horiz, null, !0)
        },
        getPlotLinePath: function (a, b, c, d, e) {
            var f = this.chart, g = this.left, h = this.top, i, j,
                k = c && f.oldChartHeight || f.chartHeight, l = c && f.oldChartWidth || f.chartWidth, m;
            i = this.transB;
            var o = function (a, b, c) {
                if (a < b || a > c)d ? a = B(v(b, a), c) : m = !0;
                return a
            }, e = p(e, this.translate(a, null, null, c)), a = c = w(e + i);
            i = j = w(k - e - i);
            isNaN(e) ? m = !0 : this.horiz ? (i = h, j = k - this.bottom, a = c = o(a, g, g + this.width)) : (a = g, c = l - this.right, i = j = o(i, h, h + this.height));
            return m && !d ? null : f.renderer.crispLine(["M", a, i, "L", c, j], b || 1)
        },
        getLinearTickPositions: function (a, b, c) {
            var d, e = la(X(b / a) * a), f = la(ya(c / a) * a), g = [];
            if (b === c && sa(b))return [b];
            for (b = e; b <= f;) {
                g.push(b);
                b = la(b + a);
                if (b === d)break;
                d = b
            }
            return g
        },
        getMinorTickPositions: function () {
            var a = this.options, b = this.tickPositions, c = this.minorTickInterval, d = [], e, f = this.min;
            e = this.max;
            var g = e - f;
            if (g && g / c < this.len / 3)if (this.isLog) {
                a = b.length;
                for (e = 1; e < a; e++)d = d.concat(this.getLogTickPositions(c, b[e - 1], b[e], !0))
            } else if (this.isDatetimeAxis && a.minorTickInterval === "auto")d = d.concat(this.getTimeTicks(this.normalizeTimeTickInterval(c), f, e, a.startOfWeek)); else for (b = f + (b[0] - f) % c; b <= e; b += c)d.push(b);
            this.trimTicks(d);
            return d
        },
        adjustForMinRange: function () {
            var a = this.options, b = this.min, c = this.max, d, e = this.dataMax - this.dataMin >= this.minRange, f, g, h, i, j;
            if (this.isXAxis && this.minRange === r && !this.isLog)s(a.min) || s(a.max) ? this.minRange = null : (n(this.series, function (a) {
                i = a.xData;
                for (g = j = a.xIncrement ? 1 : i.length - 1; g > 0; g--)if (h = i[g] - i[g - 1], f === r || h < f)f = h
            }), this.minRange = B(f * 5, this.dataMax - this.dataMin));
            if (c - b < this.minRange) {
                var k = this.minRange;
                d = (k - c + b) / 2;
                d = [b - d, p(a.min, b - d)];
                if (e)d[2] = this.dataMin;
                b = Ea(d);
                c = [b + k, p(a.max, b + k)];
                if (e)c[2] = this.dataMax;
                c = Sa(c);
                c - b < k && (d[0] = c - k, d[1] = p(a.min, c - k), b = Ea(d))
            }
            this.min = b;
            this.max = c
        },
        setAxisTranslation: function (a) {
            var b = this, c = b.max - b.min, d = b.axisPointRange || 0, e, f = 0, g = 0, h = b.linkedParent, i = !!b.categories, j = b.transA;
            if (b.isXAxis || i || d)h ? (f = h.minPointOffset, g = h.pointRangePadding) : n(b.series, function (a) {
                var h = i ? 1 : b.isXAxis ? a.pointRange : b.axisPointRange || 0, j = a.options.pointPlacement, o = a.closestPointRange;
                h > c && (h = 0);
                d = v(d, h);
                b.single || (f = v(f, Ja(j) ? 0 : h / 2), g = v(g, j ===
                "on" ? 0 : h));
                !a.noSharedTooltip && s(o) && (e = s(e) ? B(e, o) : o)
            }), h = b.ordinalSlope && e ? b.ordinalSlope / e : 1, b.minPointOffset = f *= h, b.pointRangePadding = g *= h, b.pointRange = B(d, c), b.closestPointRange = e;
            if (a)b.oldTransA = j;
            b.translationSlope = b.transA = j = b.len / (c + g || 1);
            b.transB = b.horiz ? b.left : b.bottom;
            b.minPixelPadding = j * f
        },
        setTickInterval: function (a) {
            var b = this, c = b.chart, d = b.options, e = b.isLog, f = b.isDatetimeAxis, g = b.isXAxis, h = b.isLinked, i = d.maxPadding, j = d.minPadding, k = d.tickInterval, l = d.tickPixelInterval, m = b.categories;
            !f && !m && !h && this.getTickAmount();
            h ? (b.linkedParent = c[b.coll][d.linkedTo], c = b.linkedParent.getExtremes(), b.min = p(c.min, c.dataMin), b.max = p(c.max, c.dataMax), d.type !== b.linkedParent.options.type && qa(11, 1)) : (b.min = p(b.userMin, d.min, b.dataMin), b.max = p(b.userMax, d.max, b.dataMax));
            if (e)!a && B(b.min, p(b.dataMin, b.min)) <= 0 && qa(10, 1), b.min = la(La(b.min)), b.max = la(La(b.max));
            if (b.range && s(b.max))b.userMin = b.min = v(b.min, b.max - b.range), b.userMax = b.max, b.range = null;
            b.beforePadding && b.beforePadding();
            b.adjustForMinRange();
            if (!m && !b.axisPointRange && !b.usePercentage && !h && s(b.min) && s(b.max) && (c = b.max - b.min)) {
                if (!s(d.min) && !s(b.userMin) && j && (b.dataMin < 0 || !b.ignoreMinPadding))b.min -= c * j;
                if (!s(d.max) && !s(b.userMax) && i && (b.dataMax > 0 || !b.ignoreMaxPadding))b.max += c * i
            }
            if (sa(d.floor))b.min = v(b.min, d.floor);
            if (sa(d.ceiling))b.max = B(b.max, d.ceiling);
            b.tickInterval = b.min === b.max || b.min === void 0 || b.max === void 0 ? 1 : h && !k && l === b.linkedParent.options.tickPixelInterval ? b.linkedParent.tickInterval : p(k, this.tickAmount ? (b.max - b.min) / v(this.tickAmount -
                1, 1) : void 0, m ? 1 : (b.max - b.min) * l / v(b.len, l));
            g && !a && n(b.series, function (a) {
                a.processData(b.min !== b.oldMin || b.max !== b.oldMax)
            });
            b.setAxisTranslation(!0);
            b.beforeSetTickPositions && b.beforeSetTickPositions();
            if (b.postProcessTickInterval)b.tickInterval = b.postProcessTickInterval(b.tickInterval);
            if (b.pointRange)b.tickInterval = v(b.pointRange, b.tickInterval);
            a = p(d.minTickInterval, b.isDatetimeAxis && b.closestPointRange);
            if (!k && b.tickInterval < a)b.tickInterval = a;
            if (!f && !e && !k)b.tickInterval = wb(b.tickInterval,
                null, vb(b.tickInterval), p(d.allowDecimals, !(b.tickInterval > 0.5 && b.tickInterval < 5 && b.max > 1E3 && b.max < 9999)), !!this.tickAmount);
            if (!this.tickAmount && this.len)b.tickInterval = b.unsquish();
            this.setTickPositions()
        },
        setTickPositions: function () {
            var a = this.options, b, c = a.tickPositions, d = a.tickPositioner, e = a.startOnTick, f = a.endOnTick, g;
            this.tickmarkOffset = this.categories && a.tickmarkPlacement === "between" && this.tickInterval === 1 ? 0.5 : 0;
            this.minorTickInterval = a.minorTickInterval === "auto" && this.tickInterval ? this.tickInterval /
            5 : a.minorTickInterval;
            this.tickPositions = b = a.tickPositions && a.tickPositions.slice();
            if (!b && (this.tickPositions = b = this.isDatetimeAxis ? this.getTimeTicks(this.normalizeTimeTickInterval(this.tickInterval, a.units), this.min, this.max, a.startOfWeek, this.ordinalPositions, this.closestPointRange, !0) : this.isLog ? this.getLogTickPositions(this.tickInterval, this.min, this.max) : this.getLinearTickPositions(this.tickInterval, this.min, this.max), d && (d = d.apply(this, [this.min, this.max]))))this.tickPositions = b = d;
            if (!this.isLinked)this.trimTicks(b,
                e, f), this.min === this.max && s(this.min) && !this.tickAmount && (g = !0, this.min -= 0.5, this.max += 0.5), this.single = g, !c && !d && this.adjustTickAmount()
        },
        trimTicks: function (a, b, c) {
            var d = a[0], e = a[a.length - 1], f = this.minPointOffset || 0;
            b ? this.min = d : this.min - f > d && a.shift();
            c ? this.max = e : this.max + f < e && a.pop();
            a.length === 0 && s(d) && a.push((e + d) / 2)
        },
        getTickAmount: function () {
            var a = {}, b, c = this.options, d = c.tickAmount, e = c.tickPixelInterval;
            !s(c.tickInterval) && this.len < e && !this.isRadial && !this.isLog && c.startOnTick && c.endOnTick &&
            (d = 2);
            !d && this.chart.options.chart.alignTicks !== !1 && c.alignTicks !== !1 && (n(this.chart[this.coll], function (c) {
                var d = c.options, c = c.horiz, d = [c ? d.left : d.top, c ? d.width : d.height, d.pane].join(",");
                a[d] ? b = !0 : a[d] = 1
            }), b && (d = ya(this.len / e) + 1));
            if (d < 4)this.finalTickAmt = d, d = 5;
            this.tickAmount = d
        },
        adjustTickAmount: function () {
            var a = this.tickInterval, b = this.tickPositions, c = this.tickAmount, d = this.finalTickAmt, e = b && b.length;
            if (e < c) {
                for (; b.length < c;)b.push(la(b[b.length - 1] + a));
                this.transA *= (e - 1) / (c - 1);
                this.max = b[b.length -
                1]
            } else e > c && (this.tickInterval *= 2, this.setTickPositions());
            if (s(d)) {
                for (a = c = b.length; a--;)(d === 3 && a % 2 === 1 || d <= 2 && a > 0 && a < c - 1) && b.splice(a, 1);
                this.finalTickAmt = r
            }
        },
        setScale: function () {
            var a = this.stacks, b, c, d, e;
            this.oldMin = this.min;
            this.oldMax = this.max;
            this.oldAxisLength = this.len;
            this.setAxisSize();
            e = this.len !== this.oldAxisLength;
            n(this.series, function (a) {
                if (a.isDirtyData || a.isDirty || a.xAxis.isDirty)d = !0
            });
            if (e || d || this.isLinked || this.forceRedraw || this.userMin !== this.oldUserMin || this.userMax !== this.oldUserMax) {
                if (!this.isXAxis)for (b in a)for (c in a[b])a[b][c].total =
                    null, a[b][c].cum = 0;
                this.forceRedraw = !1;
                this.getSeriesExtremes();
                this.setTickInterval();
                this.oldUserMin = this.userMin;
                this.oldUserMax = this.userMax;
                if (!this.isDirty)this.isDirty = e || this.min !== this.oldMin || this.max !== this.oldMax
            } else if (!this.isXAxis) {
                if (this.oldStacks)a = this.stacks = this.oldStacks;
                for (b in a)for (c in a[b])a[b][c].cum = a[b][c].total
            }
        },
        setExtremes: function (a, b, c, d, e) {
            var f = this, g = f.chart, c = p(c, !0);
            n(f.series, function (a) {
                delete a.kdTree
            });
            e = x(e, {min: a, max: b});
            K(f, "setExtremes", e, function () {
                f.userMin =
                    a;
                f.userMax = b;
                f.eventArgs = e;
                f.isDirtyExtremes = !0;
                c && g.redraw(d)
            })
        },
        zoom: function (a, b) {
            var c = this.dataMin, d = this.dataMax, e = this.options;
            this.allowZoomOutside || (s(c) && a <= B(c, p(e.min, c)) && (a = r), s(d) && b >= v(d, p(e.max, d)) && (b = r));
            this.displayBtn = a !== r || b !== r;
            this.setExtremes(a, b, !1, r, {trigger: "zoom"});
            return !0
        },
        setAxisSize: function () {
            var a = this.chart, b = this.options, c = b.offsetLeft || 0, d = this.horiz, e = p(b.width, a.plotWidth - c + (b.offsetRight || 0)), f = p(b.height, a.plotHeight), g = p(b.top, a.plotTop), b = p(b.left, a.plotLeft +
                c), c = /%$/;
            c.test(f) && (f = parseFloat(f) / 100 * a.plotHeight);
            c.test(g) && (g = parseFloat(g) / 100 * a.plotHeight + a.plotTop);
            this.left = b;
            this.top = g;
            this.width = e;
            this.height = f;
            this.bottom = a.chartHeight - f - g;
            this.right = a.chartWidth - e - b;
            this.len = v(d ? e : f, 0);
            this.pos = d ? b : g
        },
        getExtremes: function () {
            var a = this.isLog;
            return {
                min: a ? la(ta(this.min)) : this.min,
                max: a ? la(ta(this.max)) : this.max,
                dataMin: this.dataMin,
                dataMax: this.dataMax,
                userMin: this.userMin,
                userMax: this.userMax
            }
        },
        getThreshold: function (a) {
            var b = this.isLog, c = b ? ta(this.min) :
                this.min, b = b ? ta(this.max) : this.max;
            c > a || a === null ? a = c : b < a && (a = b);
            return this.translate(a, 0, 1, 0, 1)
        },
        autoLabelAlign: function (a) {
            a = (p(a, 0) - this.side * 90 + 720) % 360;
            return a > 15 && a < 165 ? "right" : a > 195 && a < 345 ? "left" : "center"
        },
        unsquish: function () {
            var a = this.ticks, b = this.options.labels, c = this.horiz, d = this.tickInterval, e = d, f = this.len / (((this.categories ? 1 : 0) + this.max - this.min) / d), g, h = b.rotation, i = this.chart.renderer.fontMetrics(b.style.fontSize, a[0] && a[0].label), j, k = Number.MAX_VALUE, l, m = function (a) {
                a /= f || 1;
                a = a > 1 ?
                    ya(a) : 1;
                return a * d
            };
            c ? (l = s(h) ? [h] : f < 80 && !b.staggerLines && !b.step && b.autoRotation) && n(l, function (a) {
                var b;
                a && a >= -90 && a <= 90 && (j = m(R(i.h / fa(ra * a))), b = j + R(a / 360), b < k && (k = b, g = a, e = j))
            }) : e = m(i.h);
            this.autoRotation = l;
            this.labelRotation = g;
            return e
        },
        renderUnsquish: function () {
            var a = this.chart, b = a.renderer, c = this.tickPositions, d = this.ticks, e = this.options.labels, f = this.horiz, g = a.margin, h = this.slotWidth = f && !e.step && !e.rotation && (this.staggerLines || 1) * a.plotWidth / c.length || !f && (g[3] && g[3] - a.spacing[3] || a.chartWidth *
                0.33), i = v(1, w(h - 2 * (e.padding || 5))), j = {}, g = b.fontMetrics(e.style.fontSize, d[0] && d[0].label), k, l = 0;
            if (!Ja(e.rotation))j.rotation = e.rotation;
            if (this.autoRotation)n(c, function (a) {
                if ((a = d[a]) && a.labelLength > l)l = a.labelLength
            }), l > i && l > g.h ? j.rotation = this.labelRotation : this.labelRotation = 0; else if (h) {
                k = {width: i + "px", textOverflow: "clip"};
                for (h = c.length; !f && h--;)if (i = c[h], (i = d[i].label) && this.len / c.length - 4 < i.getBBox().height)i.specCss = {textOverflow: "ellipsis"}
            }
            j.rotation && (k = {
                width: (l > a.chartHeight * 0.5 ? a.chartHeight *
                0.33 : a.chartHeight) + "px", textOverflow: "ellipsis"
            });
            this.labelAlign = j.align = e.align || this.autoLabelAlign(this.labelRotation);
            n(c, function (a) {
                var b = (a = d[a]) && a.label;
                if (b)k && b.css(y(k, b.specCss)), delete b.specCss, b.attr(j), a.rotation = j.rotation
            });
            this.tickRotCorr = b.rotCorr(g.b, this.labelRotation || 0, this.side === 2)
        },
        getOffset: function () {
            var a = this, b = a.chart, c = b.renderer, d = a.options, e = a.tickPositions, f = a.ticks, g = a.horiz, h = a.side, i = b.inverted ? [1, 0, 3, 2][h] : h, j, k, l = 0, m, o = 0, q = d.title, t = d.labels, J = 0, N = b.axisOffset,
                b = b.clipOffset, u = [-1, 1, 1, -1][h], A;
            a.hasData = j = a.hasVisibleSeries || s(a.min) && s(a.max) && !!e;
            a.showAxis = k = j || p(d.showEmpty, !0);
            a.staggerLines = a.horiz && t.staggerLines;
            if (!a.axisGroup)a.gridGroup = c.g("grid").attr({zIndex: d.gridZIndex || 1}).add(), a.axisGroup = c.g("axis").attr({zIndex: d.zIndex || 2}).add(), a.labelGroup = c.g("axis-labels").attr({zIndex: t.zIndex || 7}).addClass("highcharts-" + a.coll.toLowerCase() + "-labels").add();
            if (j || a.isLinked) {
                if (n(e, function (b) {
                        f[b] ? f[b].addLabel() : f[b] = new Za(a, b)
                    }), a.renderUnsquish(),
                        n(e, function (b) {
                            if (h === 0 || h === 2 || {
                                    1: "left",
                                    3: "right"
                                }[h] === a.labelAlign)J = v(f[b].getLabelSize(), J)
                        }), a.staggerLines)J *= a.staggerLines, a.labelOffset = J
            } else for (A in f)f[A].destroy(), delete f[A];
            if (q && q.text && q.enabled !== !1) {
                if (!a.axisTitle)a.axisTitle = c.text(q.text, 0, 0, q.useHTML).attr({
                    zIndex: 7,
                    rotation: q.rotation || 0,
                    align: q.textAlign || {low: "left", middle: "center", high: "right"}[q.align]
                }).addClass("highcharts-" + this.coll.toLowerCase() + "-title").css(q.style).add(a.axisGroup), a.axisTitle.isNew = !0;
                if (k)l =
                    a.axisTitle.getBBox()[g ? "height" : "width"], m = q.offset, o = s(m) ? 0 : p(q.margin, g ? 5 : 10);
                a.axisTitle[k ? "show" : "hide"]()
            }
            a.offset = u * p(d.offset, N[h]);
            a.tickRotCorr = a.tickRotCorr || {x: 0, y: 0};
            c = h === 2 ? a.tickRotCorr.y : 0;
            g = J + o + (J && u * (g ? p(t.y, a.tickRotCorr.y + 8) : t.x) - c);
            a.axisTitleMargin = p(m, g);
            N[h] = v(N[h], a.axisTitleMargin + l + u * a.offset, g);
            b[i] = v(b[i], X(d.lineWidth / 2) * 2)
        },
        getLinePath: function (a) {
            var b = this.chart, c = this.opposite, d = this.offset, e = this.horiz, f = this.left + (c ? this.width : 0) + d, d = b.chartHeight - this.bottom - (c ?
                    this.height : 0) + d;
            c && (a *= -1);
            return b.renderer.crispLine(["M", e ? this.left : f, e ? d : this.top, "L", e ? b.chartWidth - this.right : f, e ? d : b.chartHeight - this.bottom], a)
        },
        getTitlePosition: function () {
            var a = this.horiz, b = this.left, c = this.top, d = this.len, e = this.options.title, f = a ? b : c, g = this.opposite, h = this.offset, i = C(e.style.fontSize || 12), d = {
                low: f + (a ? 0 : d),
                middle: f + d / 2,
                high: f + (a ? d : 0)
            }[e.align], b = (a ? c + this.height : b) + (a ? 1 : -1) * (g ? -1 : 1) * this.axisTitleMargin + (this.side === 2 ? i : 0);
            return {
                x: a ? d : b + (g ? this.width : 0) + h + (e.x || 0), y: a ?
                b - (g ? this.height : 0) + h : d + (e.y || 0)
            }
        },
        render: function () {
            var a = this, b = a.chart, c = b.renderer, d = a.options, e = a.isLog, f = a.isLinked, g = a.tickPositions, h = a.axisTitle, i = a.ticks, j = a.minorTicks, k = a.alternateBands, l = d.stackLabels, m = d.alternateGridColor, o = a.tickmarkOffset, q = d.lineWidth, t, p = b.hasRendered && s(a.oldMin) && !isNaN(a.oldMin);
            t = a.hasData;
            var N = a.showAxis, u, A;
            a.labelEdge.length = 0;
            a.overlap = !1;
            n([i, j, k], function (a) {
                for (var b in a)a[b].isActive = !1
            });
            if (t || f) {
                a.minorTickInterval && !a.categories && n(a.getMinorTickPositions(),
                    function (b) {
                        j[b] || (j[b] = new Za(a, b, "minor"));
                        p && j[b].isNew && j[b].render(null, !0);
                        j[b].render(null, !1, 1)
                    });
                if (g.length && (n(g, function (b, c) {
                        if (!f || b >= a.min && b <= a.max)i[b] || (i[b] = new Za(a, b)), p && i[b].isNew && i[b].render(c, !0, 0.1), i[b].render(c)
                    }), o && (a.min === 0 || a.single)))i[-1] || (i[-1] = new Za(a, -1, null, !0)), i[-1].render(-1);
                m && n(g, function (b, c) {
                    if (c % 2 === 0 && b < a.max)k[b] || (k[b] = new z.PlotLineOrBand(a)), u = b + o, A = g[c + 1] !== r ? g[c + 1] + o : a.max, k[b].options = {
                        from: e ? ta(u) : u,
                        to: e ? ta(A) : A,
                        color: m
                    }, k[b].render(), k[b].isActive = !0
                });
                if (!a._addedPlotLB)n((d.plotLines || []).concat(d.plotBands || []), function (b) {
                    a.addPlotBandOrLine(b)
                }), a._addedPlotLB = !0
            }
            n([i, j, k], function (a) {
                var c, d, e = [], f = Fa ? Fa.duration || 500 : 0, g = function () {
                    for (d = e.length; d--;)a[e[d]] && !a[e[d]].isActive && (a[e[d]].destroy(), delete a[e[d]])
                };
                for (c in a)if (!a[c].isActive)a[c].render(c, !1, 0), a[c].isActive = !1, e.push(c);
                a === k || !b.hasRendered || !f ? g() : f && setTimeout(g, f)
            });
            if (q)t = a.getLinePath(q), a.axisLine ? a.axisLine.animate({d: t}) : a.axisLine = c.path(t).attr({
                stroke: d.lineColor,
                "stroke-width": q, zIndex: 7
            }).add(a.axisGroup), a.axisLine[N ? "show" : "hide"]();
            if (h && N)h[h.isNew ? "attr" : "animate"](a.getTitlePosition()), h.isNew = !1;
            l && l.enabled && a.renderStackTotals();
            a.isDirty = !1
        },
        redraw: function () {
            this.render();
            n(this.plotLinesAndBands, function (a) {
                a.render()
            });
            n(this.series, function (a) {
                a.isDirty = !0
            })
        },
        destroy: function (a) {
            var b = this, c = b.stacks, d, e = b.plotLinesAndBands;
            a || U(b);
            for (d in c)Na(c[d]), c[d] = null;
            n([b.ticks, b.minorTicks, b.alternateBands], function (a) {
                Na(a)
            });
            for (a = e.length; a--;)e[a].destroy();
            n("stackTotalGroup,axisLine,axisTitle,axisGroup,cross,gridGroup,labelGroup".split(","), function (a) {
                b[a] && (b[a] = b[a].destroy())
            });
            this.cross && this.cross.destroy()
        },
        drawCrosshair: function (a, b) {
            var c, d = this.crosshair, e = d.animation;
            if (!this.crosshair || (s(b) || !p(this.crosshair.snap, !0)) === !1)this.hideCrosshair(); else if (p(d.snap, !0) ? s(b) && (c = this.isXAxis ? b.plotX : this.len - b.plotY) : c = this.horiz ? a.chartX - this.pos : this.len - a.chartY + this.pos, c = this.isRadial ? this.getPlotLinePath(this.isXAxis ? b.x : p(b.stackY,
                    b.y)) || null : this.getPlotLinePath(null, null, null, null, c) || null, c === null)this.hideCrosshair(); else if (this.cross)this.cross.attr({visibility: "visible"})[e ? "animate" : "attr"]({d: c}, e); else {
                e = this.categories && !this.isRadial;
                e = {
                    "stroke-width": d.width || (e ? this.transA : 1),
                    stroke: d.color || (e ? "rgba(155,200,255,0.2)" : "#C0C0C0"),
                    zIndex: d.zIndex || 2
                };
                if (d.dashStyle)e.dashstyle = d.dashStyle;
                this.cross = this.chart.renderer.path(c).attr(e).add()
            }
        },
        hideCrosshair: function () {
            this.cross && this.cross.hide()
        }
    };
    x(O.prototype,
        {
            getPlotBandPath: function (a, b) {
                var c = this.getPlotLinePath(b, null, null, !0), d = this.getPlotLinePath(a, null, null, !0);
                d && c && d.toString() !== c.toString() ? d.push(c[4], c[5], c[1], c[2]) : d = null;
                return d
            }, addPlotBand: function (a) {
            return this.addPlotBandOrLine(a, "plotBands")
        }, addPlotLine: function (a) {
            return this.addPlotBandOrLine(a, "plotLines")
        }, addPlotBandOrLine: function (a, b) {
            var c = (new z.PlotLineOrBand(this, a)).render(), d = this.userOptions;
            c && (b && (d[b] = d[b] || [], d[b].push(a)), this.plotLinesAndBands.push(c));
            return c
        },
            removePlotBandOrLine: function (a) {
                for (var b = this.plotLinesAndBands, c = this.options, d = this.userOptions, e = b.length; e--;)b[e].id === a && b[e].destroy();
                n([c.plotLines || [], d.plotLines || [], c.plotBands || [], d.plotBands || []], function (b) {
                    for (e = b.length; e--;)b[e].id === a && ua(b, b[e])
                })
            }
        });
    O.prototype.getTimeTicks = function (a, b, c, d) {
        var e = [], f = {}, g = F.global.useUTC, h, i = new ea(b - ab(b)), j = a.unitRange, k = a.count;
        if (s(b)) {
            i.setMilliseconds(j >= H.second ? 0 : k * X(i.getMilliseconds() / k));
            j >= H.second && i.setSeconds(j >= H.minute ? 0 :
            k * X(i.getSeconds() / k));
            if (j >= H.minute)i[Ob](j >= H.hour ? 0 : k * X(i[yb]() / k));
            if (j >= H.hour)i[Pb](j >= H.day ? 0 : k * X(i[zb]() / k));
            if (j >= H.day)i[Bb](j >= H.month ? 1 : k * X(i[bb]() / k));
            j >= H.month && (i[Cb](j >= H.year ? 0 : k * X(i[cb]() / k)), h = i[db]());
            j >= H.year && (h -= h % k, i[Db](h));
            if (j === H.week)i[Bb](i[bb]() - i[Ab]() + p(d, 1));
            b = 1;
            if (ub || jb)i = i.getTime(), i = new ea(i + ab(i));
            h = i[db]();
            for (var d = i.getTime(), l = i[cb](), m = i[bb](), o = (H.day + (g ? ab(i) : i.getTimezoneOffset() * 6E4)) % H.day; d < c;)e.push(d), j === H.year ? d = lb(h + b * k, 0) : j === H.month ? d = lb(h,
                l + b * k) : !g && (j === H.day || j === H.week) ? d = lb(h, l, m + b * k * (j === H.day ? 1 : 7)) : d += j * k, b++;
            e.push(d);
            n(pb(e, function (a) {
                return j <= H.hour && a % H.day === o
            }), function (a) {
                f[a] = "day"
            })
        }
        e.info = x(a, {higherRanks: f, totalRange: j * k});
        return e
    };
    O.prototype.normalizeTimeTickInterval = function (a, b) {
        var c = b || [["millisecond", [1, 2, 5, 10, 20, 25, 50, 100, 200, 500]], ["second", [1, 2, 5, 10, 15, 30]], ["minute", [1, 2, 5, 10, 15, 30]], ["hour", [1, 2, 3, 4, 6, 8, 12]], ["day", [1, 2]], ["week", [1, 2]], ["month", [1, 2, 3, 4, 6]], ["year", null]], d = c[c.length - 1], e = H[d[0]],
            f = d[1], g;
        for (g = 0; g < c.length; g++)if (d = c[g], e = H[d[0]], f = d[1], c[g + 1] && a <= (e * f[f.length - 1] + H[c[g + 1][0]]) / 2)break;
        e === H.year && a < 5 * e && (f = [1, 2, 5]);
        c = wb(a / e, f, d[0] === "year" ? v(vb(a / e), 1) : 1);
        return {unitRange: e, count: c, unitName: d[0]}
    };
    O.prototype.getLogTickPositions = function (a, b, c, d) {
        var e = this.options, f = this.len, g = [];
        if (!d)this._minorAutoInterval = null;
        if (a >= 0.5)a = w(a), g = this.getLinearTickPositions(a, b, c); else if (a >= 0.08)for (var f = X(b), h, i, j, k, l, e = a > 0.3 ? [1, 2, 4] : a > 0.15 ? [1, 2, 4, 6, 8] : [1, 2, 3, 4, 5, 6, 7, 8, 9]; f < c + 1 && !l; f++) {
            i = e.length;
            for (h = 0; h < i && !l; h++)j = La(ta(f) * e[h]), j > b && (!d || k <= c) && k !== r && g.push(k), k > c && (l = !0), k = j
        } else if (b = ta(b), c = ta(c), a = e[d ? "minorTickInterval" : "tickInterval"], a = p(a === "auto" ? null : a, this._minorAutoInterval, (c - b) * (e.tickPixelInterval / (d ? 5 : 1)) / ((d ? f / this.tickPositions.length : f) || 1)), a = wb(a, null, vb(a)), g = za(this.getLinearTickPositions(a, b, c), La), !d)this._minorAutoInterval = a / 5;
        if (!d)this.tickInterval = a;
        return g
    };
    var Kb = z.Tooltip = function () {
        this.init.apply(this, arguments)
    };
    Kb.prototype = {
        init: function (a,
                        b) {
            var c = b.borderWidth, d = b.style, e = C(d.padding);
            this.chart = a;
            this.options = b;
            this.crosshairs = [];
            this.now = {x: 0, y: 0};
            this.isHidden = !0;
            this.label = a.renderer.label("", 0, 0, b.shape || "callout", null, null, b.useHTML, null, "tooltip").attr({
                padding: e,
                fill: b.backgroundColor,
                "stroke-width": c,
                r: b.borderRadius,
                zIndex: 8
            }).css(d).css({padding: 0}).add().attr({y: -9999});
            ma || this.label.shadow(b.shadow);
            this.shared = b.shared
        }, destroy: function () {
            if (this.label)this.label = this.label.destroy();
            clearTimeout(this.hideTimer);
            clearTimeout(this.tooltipTimeout)
        },
        move: function (a, b, c, d) {
            var e = this, f = e.now, g = e.options.animation !== !1 && !e.isHidden && (R(a - f.x) > 1 || R(b - f.y) > 1), h = e.followPointer || e.len > 1;
            x(f, {
                x: g ? (2 * f.x + a) / 3 : a,
                y: g ? (f.y + b) / 2 : b,
                anchorX: h ? r : g ? (2 * f.anchorX + c) / 3 : c,
                anchorY: h ? r : g ? (f.anchorY + d) / 2 : d
            });
            e.label.attr(f);
            if (g)clearTimeout(this.tooltipTimeout), this.tooltipTimeout = setTimeout(function () {
                e && e.move(a, b, c, d)
            }, 32)
        }, hide: function (a) {
            var b = this, c;
            clearTimeout(this.hideTimer);
            if (!this.isHidden)c = this.chart.hoverPoints, this.hideTimer = setTimeout(function () {
                b.label.fadeOut();
                b.isHidden = !0
            }, p(a, this.options.hideDelay, 500)), c && n(c, function (a) {
                a.setState()
            }), this.chart.hoverPoints = null, this.chart.hoverSeries = null
        }, getAnchor: function (a, b) {
            var c, d = this.chart, e = d.inverted, f = d.plotTop, g = d.plotLeft, h = 0, i = 0, j, k, a = pa(a);
            c = a[0].tooltipPos;
            this.followPointer && b && (b.chartX === r && (b = d.pointer.normalize(b)), c = [b.chartX - d.plotLeft, b.chartY - f]);
            c || (n(a, function (a) {
                j = a.series.yAxis;
                k = a.series.xAxis;
                h += a.plotX + (!e && k ? k.left - g : 0);
                i += (a.plotLow ? (a.plotLow + a.plotHigh) / 2 : a.plotY) + (!e && j ? j.top -
                    f : 0)
            }), h /= a.length, i /= a.length, c = [e ? d.plotWidth - i : h, this.shared && !e && a.length > 1 && b ? b.chartY - f : e ? d.plotHeight - h : i]);
            return za(c, w)
        }, getPosition: function (a, b, c) {
            var d = this.chart, e = this.distance, f = {}, g, h = ["y", d.chartHeight, b, c.plotY + d.plotTop], i = ["x", d.chartWidth, a, c.plotX + d.plotLeft], j = p(c.ttBelow, d.inverted && !c.negative || !d.inverted && c.negative), k = function (a, b, c, d) {
                var g = c < d - e, b = d + e + c < b, c = d - e - c;
                d += e;
                if (j && b)f[a] = d; else if (!j && g)f[a] = c; else if (g)f[a] = c; else if (b)f[a] = d; else return !1
            }, l = function (a, b,
                             c, d) {
                if (d < e || d > b - e)return !1; else f[a] = d < c / 2 ? 1 : d > b - c / 2 ? b - c - 2 : d - c / 2
            }, m = function (a) {
                var b = h;
                h = i;
                i = b;
                g = a
            }, o = function () {
                k.apply(0, h) !== !1 ? l.apply(0, i) === !1 && !g && (m(!0), o()) : g ? f.x = f.y = 0 : (m(!0), o())
            };
            (d.inverted || this.len > 1) && m();
            o();
            return f
        }, defaultFormatter: function (a) {
            var b = this.points || pa(this), c;
            c = [a.tooltipFooterHeaderFormatter(b[0])];
            c = c.concat(a.bodyFormatter(b));
            c.push(a.tooltipFooterHeaderFormatter(b[0], !0));
            return c.join("")
        }, refresh: function (a, b) {
            var c = this.chart, d = this.label, e = this.options,
                f, g, h = {}, i, j = [];
            i = e.formatter || this.defaultFormatter;
            var h = c.hoverPoints, k, l = this.shared;
            clearTimeout(this.hideTimer);
            this.followPointer = pa(a)[0].series.tooltipOptions.followPointer;
            g = this.getAnchor(a, b);
            f = g[0];
            g = g[1];
            l && (!a.series || !a.series.noSharedTooltip) ? (c.hoverPoints = a, h && n(h, function (a) {
                a.setState()
            }), n(a, function (a) {
                a.setState("hover");
                j.push(a.getLabelConfig())
            }), h = {
                x: a[0].category,
                y: a[0].y
            }, h.points = j, this.len = j.length, a = a[0]) : h = a.getLabelConfig();
            i = i.call(h, this);
            h = a.series;
            this.distance =
                p(h.tooltipOptions.distance, 16);
            i === !1 ? this.hide() : (this.isHidden && (gb(d), d.attr("opacity", 1).show()), d.attr({text: i}), k = e.borderColor || a.color || h.color || "#606060", d.attr({stroke: k}), this.updatePosition({
                plotX: f,
                plotY: g,
                negative: a.negative,
                ttBelow: a.ttBelow
            }), this.isHidden = !1);
            K(c, "tooltipRefresh", {text: i, x: f + c.plotLeft, y: g + c.plotTop, borderColor: k})
        }, updatePosition: function (a) {
            var b = this.chart, c = this.label, c = (this.options.positioner || this.getPosition).call(this, c.width, c.height, a);
            this.move(w(c.x),
                w(c.y), a.plotX + b.plotLeft, a.plotY + b.plotTop)
        }, getXDateFormat: function (a, b, c) {
            var d, b = b.dateTimeLabelFormats, e = c && c.closestPointRange, f, g = {
                millisecond: 15,
                second: 12,
                minute: 9,
                hour: 6,
                day: 3
            }, h, i;
            if (e) {
                h = ka("%m-%d %H:%M:%S.%L", a.x);
                for (f in H) {
                    if (e === H.week && +ka("%w", a.x) === c.options.startOfWeek && h.substr(6) === "00:00:00.000") {
                        f = "week";
                        break
                    } else if (H[f] > e) {
                        f = i;
                        break
                    } else if (g[f] && h.substr(g[f]) !== "01-01 00:00:00.000".substr(g[f]))break;
                    f !== "week" && (i = f)
                }
                f && (d = b[f])
            } else d = b.day;
            return d || b.year
        }, tooltipFooterHeaderFormatter: function (a,
                                                   b) {
            var c = b ? "footer" : "header", d = a.series, e = d.tooltipOptions, f = e.xDateFormat, g = d.xAxis, h = g && g.options.type === "datetime" && sa(a.key), c = e[c + "Format"];
            h && !f && (f = this.getXDateFormat(a, e, g));
            h && f && (c = c.replace("{point.key}", "{point.key:" + f + "}"));
            return Ma(c, {point: a, series: d})
        }, bodyFormatter: function (a) {
            return za(a, function (a) {
                var c = a.series.tooltipOptions;
                return (c.pointFormatter || a.point.tooltipFormatter).call(a.point, c.pointFormat)
            })
        }
    };
    var Ia;
    $a = E.documentElement.ontouchstart !== r;
    var Xa = z.Pointer = function (a,
                                   b) {
        this.init(a, b)
    };
    Xa.prototype = {
        init: function (a, b) {
            var c = b.chart, d = c.events, e = ma ? "" : c.zoomType, c = a.inverted, f;
            this.options = b;
            this.chart = a;
            this.zoomX = f = /x/.test(e);
            this.zoomY = e = /y/.test(e);
            this.zoomHor = f && !c || e && c;
            this.zoomVert = e && !c || f && c;
            this.hasZoom = f || e;
            this.runChartClick = d && !!d.click;
            this.pinchDown = [];
            this.lastValidTouch = {};
            if (z.Tooltip && b.tooltip.enabled)a.tooltip = new Kb(a, b.tooltip), this.followTouchMove = p(b.tooltip.followTouchMove, !0);
            this.setDOMEvents()
        }, normalize: function (a, b) {
            var c, d, a =
                a || window.event, a = ac(a);
            if (!a.target)a.target = a.srcElement;
            d = a.touches ? a.touches.length ? a.touches.item(0) : a.changedTouches[0] : a;
            if (!b)this.chartPosition = b = $b(this.chart.container);
            d.pageX === r ? (c = v(a.x, a.clientX - b.left), d = a.y) : (c = d.pageX - b.left, d = d.pageY - b.top);
            return x(a, {chartX: w(c), chartY: w(d)})
        }, getCoordinates: function (a) {
            var b = {xAxis: [], yAxis: []};
            n(this.chart.axes, function (c) {
                b[c.isXAxis ? "xAxis" : "yAxis"].push({axis: c, value: c.toValue(a[c.horiz ? "chartX" : "chartY"])})
            });
            return b
        }, runPointActions: function (a) {
            var b =
                this, c = b.chart, d = c.series, e = c.tooltip, f = e ? e.shared : !1, g, h = c.hoverPoint, i = c.hoverSeries, j = c.chartWidth, k = c.chartWidth, l, m = [], o, q;
            if (!f && !i)for (g = 0; g < d.length; g++)if (d[g].directTouch || !d[g].options.stickyTracking)d = [];
            (!i || !i.noSharedTooltip) && (f || !i) ? (n(d, function (b) {
                l = b.noSharedTooltip && f;
                b.visible && !l && p(b.options.enableMouseTracking, !0) && (q = b.searchPoint(a)) && m.push(q)
            }), n(m, function (a) {
                if (a && s(a.plotX) && s(a.plotY) && (a.dist.distX < j || (a.dist.distX === j || a.series.kdDimensions > 1) && a.dist.distR < k))j =
                    a.dist.distX, k = a.dist.distR, o = a
            })) : o = i ? i.searchPoint(a) : r;
            if (o && o !== h)if (f && !o.series.noSharedTooltip) {
                g = m.length;
                for (d = o.clientX; g--;)i = m[g].clientX, (m[g].x !== o.x || i !== d || !s(m[g].y) || m[g].series.noSharedTooltip) && m.splice(g, 1);
                e && e.refresh(m, a);
                n(m, function (b) {
                    b.onMouseOver(a)
                })
            } else e && e.refresh(o, a), o.onMouseOver(a); else g = i && i.tooltipOptions.followPointer, e && g && !e.isHidden && (g = e.getAnchor([{}], a), e.updatePosition({
                plotX: g[0],
                plotY: g[1]
            }));
            if (e && !b._onDocumentMouseMove)b._onDocumentMouseMove = function (a) {
                b.onDocumentMouseMove(a)
            },
                D(E, "mousemove", b._onDocumentMouseMove);
            n(c.axes, function (b) {
                b.drawCrosshair(a, p(o, h))
            })
        }, reset: function (a, b) {
            var c = this.chart, d = c.hoverSeries, e = c.hoverPoint, f = c.tooltip, g = f && f.shared ? c.hoverPoints : e;
            (a = a && f && g) && pa(g)[0].plotX === r && (a = !1);
            if (a)f.refresh(g), e && (e.setState(e.state, !0), n(c.axes, function (b) {
                p(b.options.crosshair && b.options.crosshair.snap, !0) ? b.drawCrosshair(null, a) : b.hideCrosshair()
            })); else {
                if (e)e.onMouseOut();
                if (d)d.onMouseOut();
                f && f.hide(b);
                if (this._onDocumentMouseMove)U(E, "mousemove",
                    this._onDocumentMouseMove), this._onDocumentMouseMove = null;
                n(c.axes, function (a) {
                    a.hideCrosshair()
                });
                this.hoverX = null
            }
        }, scaleGroups: function (a, b) {
            var c = this.chart, d;
            n(c.series, function (e) {
                d = a || e.getPlotBox();
                e.xAxis && e.xAxis.zoomEnabled && (e.group.attr(d), e.markerGroup && (e.markerGroup.attr(d), e.markerGroup.clip(b ? c.clipRect : null)), e.dataLabelsGroup && e.dataLabelsGroup.attr(d))
            });
            c.clipRect.attr(b || c.clipBox)
        }, dragStart: function (a) {
            var b = this.chart;
            b.mouseIsDown = a.type;
            b.cancelClick = !1;
            b.mouseDownX = this.mouseDownX =
                a.chartX;
            b.mouseDownY = this.mouseDownY = a.chartY
        }, drag: function (a) {
            var b = this.chart, c = b.options.chart, d = a.chartX, e = a.chartY, f = this.zoomHor, g = this.zoomVert, h = b.plotLeft, i = b.plotTop, j = b.plotWidth, k = b.plotHeight, l, m = this.mouseDownX, o = this.mouseDownY, q = c.panKey && a[c.panKey + "Key"];
            d < h ? d = h : d > h + j && (d = h + j);
            e < i ? e = i : e > i + k && (e = i + k);
            this.hasDragged = Math.sqrt(Math.pow(m - d, 2) + Math.pow(o - e, 2));
            if (this.hasDragged > 10) {
                l = b.isInsidePlot(m - h, o - i);
                if (b.hasCartesianSeries && (this.zoomX || this.zoomY) && l && !q && !this.selectionMarker)this.selectionMarker =
                    b.renderer.rect(h, i, f ? 1 : j, g ? 1 : k, 0).attr({
                        fill: c.selectionMarkerFill || "rgba(69,114,167,0.25)",
                        zIndex: 7
                    }).add();
                this.selectionMarker && f && (d -= m, this.selectionMarker.attr({width: R(d), x: (d > 0 ? 0 : d) + m}));
                this.selectionMarker && g && (d = e - o, this.selectionMarker.attr({
                    height: R(d),
                    y: (d > 0 ? 0 : d) + o
                }));
                l && !this.selectionMarker && c.panning && b.pan(a, c.panning)
            }
        }, drop: function (a) {
            var b = this, c = this.chart, d = this.hasPinched;
            if (this.selectionMarker) {
                var e = {xAxis: [], yAxis: [], originalEvent: a.originalEvent || a}, f = this.selectionMarker,
                    g = f.attr ? f.attr("x") : f.x, h = f.attr ? f.attr("y") : f.y, i = f.attr ? f.attr("width") : f.width, j = f.attr ? f.attr("height") : f.height, k;
                if (this.hasDragged || d)n(c.axes, function (c) {
                    if (c.zoomEnabled && s(c.min) && (d || b[{xAxis: "zoomX", yAxis: "zoomY"}[c.coll]])) {
                        var f = c.horiz, o = a.type === "touchend" ? c.minPixelPadding : 0, q = c.toValue((f ? g : h) + o), f = c.toValue((f ? g + i : h + j) - o);
                        e[c.coll].push({axis: c, min: B(q, f), max: v(q, f)});
                        k = !0
                    }
                }), k && K(c, "selection", e, function (a) {
                    c.zoom(x(a, d ? {animation: !1} : null))
                });
                this.selectionMarker = this.selectionMarker.destroy();
                d && this.scaleGroups()
            }
            if (c)M(c.container, {cursor: c._cursor}), c.cancelClick = this.hasDragged > 10, c.mouseIsDown = this.hasDragged = this.hasPinched = !1, this.pinchDown = []
        }, onContainerMouseDown: function (a) {
            a = this.normalize(a);
            a.preventDefault && a.preventDefault();
            this.dragStart(a)
        }, onDocumentMouseUp: function (a) {
            ha[Ia] && ha[Ia].pointer.drop(a)
        }, onDocumentMouseMove: function (a) {
            var b = this.chart, c = this.chartPosition, a = this.normalize(a, c);
            c && !this.inClass(a.target, "highcharts-tracker") && !b.isInsidePlot(a.chartX - b.plotLeft,
                a.chartY - b.plotTop) && this.reset()
        }, onContainerMouseLeave: function () {
            var a = ha[Ia];
            if (a)a.pointer.reset(), a.pointer.chartPosition = null
        }, onContainerMouseMove: function (a) {
            var b = this.chart;
            Ia = b.index;
            a = this.normalize(a);
            a.returnValue = !1;
            b.mouseIsDown === "mousedown" && this.drag(a);
            (this.inClass(a.target, "highcharts-tracker") || b.isInsidePlot(a.chartX - b.plotLeft, a.chartY - b.plotTop)) && !b.openMenu && this.runPointActions(a)
        }, inClass: function (a, b) {
            for (var c; a;) {
                if (c = W(a, "class"))if (c.indexOf(b) !== -1)return !0; else if (c.indexOf("highcharts-container") !== -1)return !1;
                a = a.parentNode
            }
        }, onTrackerMouseOut: function (a) {
            var b = this.chart.hoverSeries, c = (a = a.relatedTarget || a.toElement) && a.point && a.point.series;
            if (b && !b.options.stickyTracking && !this.inClass(a, "highcharts-tooltip") && c !== b)b.onMouseOut()
        }, onContainerClick: function (a) {
            var b = this.chart, c = b.hoverPoint, d = b.plotLeft, e = b.plotTop, a = this.normalize(a);
            a.cancelBubble = !0;
            b.cancelClick || (c && this.inClass(a.target, "highcharts-tracker") ? (K(c.series, "click", x(a, {point: c})), b.hoverPoint && c.firePointEvent("click",
                a)) : (x(a, this.getCoordinates(a)), b.isInsidePlot(a.chartX - d, a.chartY - e) && K(b, "click", a)))
        }, setDOMEvents: function () {
            var a = this, b = a.chart.container;
            b.onmousedown = function (b) {
                a.onContainerMouseDown(b)
            };
            b.onmousemove = function (b) {
                a.onContainerMouseMove(b)
            };
            b.onclick = function (b) {
                a.onContainerClick(b)
            };
            D(b, "mouseleave", a.onContainerMouseLeave);
            fb === 1 && D(E, "mouseup", a.onDocumentMouseUp);
            if ($a)b.ontouchstart = function (b) {
                a.onContainerTouchStart(b)
            }, b.ontouchmove = function (b) {
                a.onContainerTouchMove(b)
            }, fb === 1 &&
            D(E, "touchend", a.onDocumentTouchEnd)
        }, destroy: function () {
            var a;
            U(this.chart.container, "mouseleave", this.onContainerMouseLeave);
            fb || (U(E, "mouseup", this.onDocumentMouseUp), U(E, "touchend", this.onDocumentTouchEnd));
            clearInterval(this.tooltipTimeout);
            for (a in this)this[a] = null
        }
    };
    x(z.Pointer.prototype, {
        pinchTranslate: function (a, b, c, d, e, f) {
            (this.zoomHor || this.pinchHor) && this.pinchTranslateDirection(!0, a, b, c, d, e, f);
            (this.zoomVert || this.pinchVert) && this.pinchTranslateDirection(!1, a, b, c, d, e, f)
        }, pinchTranslateDirection: function (a,
                                              b, c, d, e, f, g, h) {
            var i = this.chart, j = a ? "x" : "y", k = a ? "X" : "Y", l = "chart" + k, m = a ? "width" : "height", o = i["plot" + (a ? "Left" : "Top")], q, t, p = h || 1, n = i.inverted, u = i.bounds[a ? "h" : "v"], A = b.length === 1, r = b[0][l], s = c[0][l], v = !A && b[1][l], w = !A && c[1][l], x, c = function () {
                !A && R(r - v) > 20 && (p = h || R(s - w) / R(r - v));
                t = (o - s) / p + r;
                q = i["plot" + (a ? "Width" : "Height")] / p
            };
            c();
            b = t;
            b < u.min ? (b = u.min, x = !0) : b + q > u.max && (b = u.max - q, x = !0);
            x ? (s -= 0.8 * (s - g[j][0]), A || (w -= 0.8 * (w - g[j][1])), c()) : g[j] = [s, w];
            n || (f[j] = t - o, f[m] = q);
            f = n ? 1 / p : p;
            e[m] = q;
            e[j] = b;
            d[n ? a ? "scaleY" :
                "scaleX" : "scale" + k] = p;
            d["translate" + k] = f * o + (s - f * r)
        }, pinch: function (a) {
            var b = this, c = b.chart, d = b.pinchDown, e = a.touches, f = e.length, g = b.lastValidTouch, h = b.hasZoom, i = b.selectionMarker, j = {}, k = f === 1 && (b.inClass(a.target, "highcharts-tracker") && c.runTrackerClick || b.runChartClick), l = {};
            h && !k && a.preventDefault();
            za(e, function (a) {
                return b.normalize(a)
            });
            if (a.type === "touchstart")n(e, function (a, b) {
                d[b] = {chartX: a.chartX, chartY: a.chartY}
            }), g.x = [d[0].chartX, d[1] && d[1].chartX], g.y = [d[0].chartY, d[1] && d[1].chartY], n(c.axes,
                function (a) {
                    if (a.zoomEnabled) {
                        var b = c.bounds[a.horiz ? "h" : "v"], d = a.minPixelPadding, e = a.toPixels(p(a.options.min, a.dataMin)), f = a.toPixels(p(a.options.max, a.dataMax)), g = B(e, f), e = v(e, f);
                        b.min = B(a.pos, g - d);
                        b.max = v(a.pos + a.len, e + d)
                    }
                }), b.res = !0; else if (d.length) {
                if (!i)b.selectionMarker = i = x({destroy: ga}, c.plotBox);
                b.pinchTranslate(d, e, j, i, l, g);
                b.hasPinched = h;
                b.scaleGroups(j, l);
                if (!h && b.followTouchMove && f === 1)this.runPointActions(b.normalize(a)); else if (b.res)b.res = !1, this.reset(!1, 0)
            }
        }, onContainerTouchStart: function (a) {
            var b =
                this.chart;
            Ia = b.index;
            a.touches.length === 1 ? (a = this.normalize(a), b.isInsidePlot(a.chartX - b.plotLeft, a.chartY - b.plotTop) && !b.openMenu ? (this.runPointActions(a), this.pinch(a)) : this.reset()) : a.touches.length === 2 && this.pinch(a)
        }, onContainerTouchMove: function (a) {
            (a.touches.length === 1 || a.touches.length === 2) && this.pinch(a)
        }, onDocumentTouchEnd: function (a) {
            ha[Ia] && ha[Ia].pointer.drop(a)
        }
    });
    if (T.PointerEvent || T.MSPointerEvent) {
        var Aa = {}, Lb = !!T.PointerEvent, ec = function () {
            var a, b = [];
            b.item = function (a) {
                return this[a]
            };
            for (a in Aa)Aa.hasOwnProperty(a) && b.push({pageX: Aa[a].pageX, pageY: Aa[a].pageY, target: Aa[a].target});
            return b
        }, Mb = function (a, b, c, d) {
            a = a.originalEvent || a;
            if ((a.pointerType === "touch" || a.pointerType === a.MSPOINTER_TYPE_TOUCH) && ha[Ia])d(a), d = ha[Ia].pointer, d[b]({
                type: c,
                target: a.currentTarget,
                preventDefault: ga,
                touches: ec()
            })
        };
        x(Xa.prototype, {
            onContainerPointerDown: function (a) {
                Mb(a, "onContainerTouchStart", "touchstart", function (a) {
                    Aa[a.pointerId] = {pageX: a.pageX, pageY: a.pageY, target: a.currentTarget}
                })
            }, onContainerPointerMove: function (a) {
                Mb(a,
                    "onContainerTouchMove", "touchmove", function (a) {
                        Aa[a.pointerId] = {pageX: a.pageX, pageY: a.pageY};
                        if (!Aa[a.pointerId].target)Aa[a.pointerId].target = a.currentTarget
                    })
            }, onDocumentPointerUp: function (a) {
                Mb(a, "onContainerTouchEnd", "touchend", function (a) {
                    delete Aa[a.pointerId]
                })
            }, batchMSEvents: function (a) {
                a(this.chart.container, Lb ? "pointerdown" : "MSPointerDown", this.onContainerPointerDown);
                a(this.chart.container, Lb ? "pointermove" : "MSPointerMove", this.onContainerPointerMove);
                a(E, Lb ? "pointerup" : "MSPointerUp", this.onDocumentPointerUp)
            }
        });
        S(Xa.prototype, "init", function (a, b, c) {
            a.call(this, b, c);
            (this.hasZoom || this.followTouchMove) && M(b.container, {"-ms-touch-action": Z, "touch-action": Z})
        });
        S(Xa.prototype, "setDOMEvents", function (a) {
            a.apply(this);
            (this.hasZoom || this.followTouchMove) && this.batchMSEvents(D)
        });
        S(Xa.prototype, "destroy", function (a) {
            this.batchMSEvents(U);
            a.call(this)
        })
    }
    var rb = z.Legend = function (a, b) {
        this.init(a, b)
    };
    rb.prototype = {
        init: function (a, b) {
            var c = this, d = b.itemStyle, e = b.itemMarginTop || 0;
            this.options = b;
            if (b.enabled)c.itemStyle =
                d, c.itemHiddenStyle = y(d, b.itemHiddenStyle), c.itemMarginTop = e, c.padding = d = p(b.padding, 8), c.initialItemX = d, c.initialItemY = d - 5, c.maxItemWidth = 0, c.chart = a, c.itemHeight = 0, c.symbolWidth = p(b.symbolWidth, 16), c.pages = [], c.render(), D(c.chart, "endResize", function () {
                c.positionCheckboxes()
            })
        }, colorizeItem: function (a, b) {
            var c = this.options, d = a.legendItem, e = a.legendLine, f = a.legendSymbol, g = this.itemHiddenStyle.color, c = b ? c.itemStyle.color : g, h = b ? a.legendColor || a.color || "#CCC" : g, g = a.options && a.options.marker, i = {fill: h},
                j;
            d && d.css({fill: c, color: c});
            e && e.attr({stroke: h});
            if (f) {
                if (g && f.isMarker)for (j in i.stroke = h, g = a.convertAttribs(g), g)d = g[j], d !== r && (i[j] = d);
                f.attr(i)
            }
        }, positionItem: function (a) {
            var b = this.options, c = b.symbolPadding, b = !b.rtl, d = a._legendItemPos, e = d[0], d = d[1], f = a.checkbox;
            a.legendGroup && a.legendGroup.translate(b ? e : this.legendWidth - e - 2 * c - 4, d);
            if (f)f.x = e, f.y = d
        }, destroyItem: function (a) {
            var b = a.checkbox;
            n(["legendItem", "legendLine", "legendSymbol", "legendGroup"], function (b) {
                a[b] && (a[b] = a[b].destroy())
            });
            b && Ta(a.checkbox)
        }, clearItems: function () {
            var a = this;
            n(a.getAllItems(), function (b) {
                a.destroyItem(b)
            })
        }, destroy: function () {
            var a = this.group, b = this.box;
            if (b)this.box = b.destroy();
            if (a)this.group = a.destroy()
        }, positionCheckboxes: function (a) {
            var b = this.group.alignAttr, c, d = this.clipHeight || this.legendHeight;
            if (b)c = b.translateY, n(this.allItems, function (e) {
                var f = e.checkbox, g;
                f && (g = c + f.y + (a || 0) + 3, M(f, {
                    left: b.translateX + e.checkboxOffset + f.x - 20 + "px",
                    top: g + "px",
                    display: g > c - 6 && g < c + d - 6 ? "" : Z
                }))
            })
        }, renderTitle: function () {
            var a =
                this.padding, b = this.options.title, c = 0;
            if (b.text) {
                if (!this.title)this.title = this.chart.renderer.label(b.text, a - 3, a - 4, null, null, null, null, null, "legend-title").attr({zIndex: 1}).css(b.style).add(this.group);
                a = this.title.getBBox();
                c = a.height;
                this.offsetWidth = a.width;
                this.contentGroup.attr({translateY: c})
            }
            this.titleHeight = c
        }, renderItem: function (a) {
            var b = this.chart, c = b.renderer, d = this.options, e = d.layout === "horizontal", f = this.symbolWidth, g = d.symbolPadding, h = this.itemStyle, i = this.itemHiddenStyle, j = this.padding,
                k = e ? p(d.itemDistance, 20) : 0, l = !d.rtl, m = d.width, o = d.itemMarginBottom || 0, q = this.itemMarginTop, t = this.initialItemX, n = a.legendItem, N = a.series && a.series.drawLegendSymbol ? a.series : a, u = N.options, u = this.createCheckboxForItem && u && u.showCheckbox, A = d.useHTML;
            if (!n) {
                a.legendGroup = c.g("legend-item").attr({zIndex: 1}).add(this.scrollGroup);
                a.legendItem = n = c.text(d.labelFormat ? Ma(d.labelFormat, a) : d.labelFormatter.call(a), l ? f + g : -g, this.baseline || 0, A).css(y(a.visible ? h : i)).attr({
                    align: l ? "left" : "right",
                    zIndex: 2
                }).add(a.legendGroup);
                if (!this.baseline)this.baseline = c.fontMetrics(h.fontSize, n).f + 3 + q, n.attr("y", this.baseline);
                N.drawLegendSymbol(this, a);
                this.setItemEvents && this.setItemEvents(a, n, A, h, i);
                this.colorizeItem(a, a.visible);
                u && this.createCheckboxForItem(a)
            }
            c = n.getBBox();
            f = a.checkboxOffset = d.itemWidth || a.legendItemWidth || f + g + c.width + k + (u ? 20 : 0);
            this.itemHeight = g = w(a.legendItemHeight || c.height);
            if (e && this.itemX - t + f > (m || b.chartWidth - 2 * j - t - d.x))this.itemX = t, this.itemY += q + this.lastLineHeight + o;
            this.maxItemWidth = v(this.maxItemWidth,
                f);
            this.lastItemY = q + this.itemY + o;
            this.lastLineHeight = v(g, this.lastLineHeight);
            a._legendItemPos = [this.itemX, this.itemY];
            e ? this.itemX += f : (this.itemY += q + g + o, this.lastLineHeight = g);
            this.offsetWidth = m || v((e ? this.itemX - t - k : f) + j, this.offsetWidth)
        }, getAllItems: function () {
            var a = [];
            n(this.chart.series, function (b) {
                var c = b.options;
                if (p(c.showInLegend, !s(c.linkedTo) ? r : !1, !0))a = a.concat(b.legendItems || (c.legendType === "point" ? b.data : b))
            });
            return a
        }, adjustMargins: function (a, b) {
            var c = this.chart, d = this.options, e = d.align[0] +
                d.verticalAlign[0] + d.layout[0];
            this.display && !d.floating && n([/(lth|ct|rth)/, /(rtv|rm|rbv)/, /(rbh|cb|lbh)/, /(lbv|lm|ltv)/], function (f, g) {
                f.test(e) && !s(a[g]) && (c[nb[g]] = v(c[nb[g]], c.legend[(g + 1) % 2 ? "legendHeight" : "legendWidth"] + [1, -1, -1, 1][g] * d[g % 2 ? "x" : "y"] + p(d.margin, 12) + b[g]))
            })
        }, render: function () {
            var a = this, b = a.chart, c = b.renderer, d = a.group, e, f, g, h, i = a.box, j = a.options, k = a.padding, l = j.borderWidth, m = j.backgroundColor;
            a.itemX = a.initialItemX;
            a.itemY = a.initialItemY;
            a.offsetWidth = 0;
            a.lastItemY = 0;
            if (!d)a.group =
                d = c.g("legend").attr({zIndex: 7}).add(), a.contentGroup = c.g().attr({zIndex: 1}).add(d), a.scrollGroup = c.g().add(a.contentGroup);
            a.renderTitle();
            e = a.getAllItems();
            xb(e, function (a, b) {
                return (a.options && a.options.legendIndex || 0) - (b.options && b.options.legendIndex || 0)
            });
            j.reversed && e.reverse();
            a.allItems = e;
            a.display = f = !!e.length;
            a.lastLineHeight = 0;
            n(e, function (b) {
                a.renderItem(b)
            });
            g = (j.width || a.offsetWidth) + k;
            h = a.lastItemY + a.lastLineHeight + a.titleHeight;
            h = a.handleOverflow(h);
            h += k;
            if (l || m) {
                if (i) {
                    if (g > 0 && h > 0)i[i.isNew ?
                        "attr" : "animate"](i.crisp({width: g, height: h})), i.isNew = !1
                } else a.box = i = c.rect(0, 0, g, h, j.borderRadius, l || 0).attr({
                    stroke: j.borderColor,
                    "stroke-width": l || 0,
                    fill: m || Z
                }).add(d).shadow(j.shadow), i.isNew = !0;
                i[f ? "show" : "hide"]()
            }
            a.legendWidth = g;
            a.legendHeight = h;
            n(e, function (b) {
                a.positionItem(b)
            });
            f && d.align(x({width: g, height: h}, j), !0, "spacingBox");
            b.isResizing || this.positionCheckboxes()
        }, handleOverflow: function (a) {
            var b = this, c = this.chart, d = c.renderer, e = this.options, f = e.y, f = c.spacingBox.height + (e.verticalAlign ===
                "top" ? -f : f) - this.padding, g = e.maxHeight, h, i = this.clipRect, j = e.navigation, k = p(j.animation, !0), l = j.arrowSize || 12, m = this.nav, o = this.pages, q, t = this.allItems;
            e.layout === "horizontal" && (f /= 2);
            g && (f = B(f, g));
            o.length = 0;
            if (a > f && !e.useHTML) {
                this.clipHeight = h = v(f - 20 - this.titleHeight - this.padding, 0);
                this.currentPage = p(this.currentPage, 1);
                this.fullHeight = a;
                n(t, function (a, b) {
                    var c = a._legendItemPos[1], d = w(a.legendItem.getBBox().height), e = o.length;
                    if (!e || c - o[e - 1] > h && (q || c) !== o[e - 1])o.push(q || c), e++;
                    b === t.length - 1 &&
                    c + d - o[e - 1] > h && o.push(c);
                    c !== q && (q = c)
                });
                if (!i)i = b.clipRect = d.clipRect(0, this.padding, 9999, 0), b.contentGroup.clip(i);
                i.attr({height: h});
                if (!m)this.nav = m = d.g().attr({zIndex: 1}).add(this.group), this.up = d.symbol("triangle", 0, 0, l, l).on("click", function () {
                    b.scroll(-1, k)
                }).add(m), this.pager = d.text("", 15, 10).css(j.style).add(m), this.down = d.symbol("triangle-down", 0, 0, l, l).on("click", function () {
                    b.scroll(1, k)
                }).add(m);
                b.scroll(0);
                a = f
            } else if (m)i.attr({height: c.chartHeight}), m.hide(), this.scrollGroup.attr({translateY: 1}),
                this.clipHeight = 0;
            return a
        }, scroll: function (a, b) {
            var c = this.pages, d = c.length, e = this.currentPage + a, f = this.clipHeight, g = this.options.navigation, h = g.activeColor, g = g.inactiveColor, i = this.pager, j = this.padding;
            e > d && (e = d);
            if (e > 0)b !== r && Ya(b, this.chart), this.nav.attr({
                translateX: j,
                translateY: f + this.padding + 7 + this.titleHeight,
                visibility: "visible"
            }), this.up.attr({fill: e === 1 ? g : h}).css({cursor: e === 1 ? "default" : "pointer"}), i.attr({text: e + "/" + d}), this.down.attr({
                x: 18 + this.pager.getBBox().width,
                fill: e === d ? g : h
            }).css({
                cursor: e ===
                d ? "default" : "pointer"
            }), c = -c[e - 1] + this.initialItemY, this.scrollGroup.animate({translateY: c}), this.currentPage = e, this.positionCheckboxes(c)
        }
    };
    G = z.LegendSymbolMixin = {
        drawRectangle: function (a, b) {
            var c = a.options.symbolHeight || 12;
            b.legendSymbol = this.chart.renderer.rect(0, a.baseline - 5 - c / 2, a.symbolWidth, c, a.options.symbolRadius || 0).attr({zIndex: 3}).add(b.legendGroup)
        }, drawLineMarker: function (a) {
            var b = this.options, c = b.marker, d;
            d = a.symbolWidth;
            var e = this.chart.renderer, f = this.legendGroup, a = a.baseline - w(e.fontMetrics(a.options.itemStyle.fontSize,
                        this.legendItem).b * 0.3), g;
            if (b.lineWidth) {
                g = {"stroke-width": b.lineWidth};
                if (b.dashStyle)g.dashstyle = b.dashStyle;
                this.legendLine = e.path(["M", 0, a, "L", d, a]).attr(g).add(f)
            }
            if (c && c.enabled !== !1)b = c.radius, this.legendSymbol = d = e.symbol(this.symbol, d / 2 - b, a - b, 2 * b, 2 * b).add(f), d.isMarker = !0
        }
    };
    (/Trident\/7\.0/.test(Ga) || Va) && S(rb.prototype, "positionItem", function (a, b) {
        var c = this, d = function () {
            b._legendItemPos && a.call(c, b)
        };
        d();
        setTimeout(d)
    });
    var Pa = z.Chart = function () {
        this.init.apply(this, arguments)
    };
    Pa.prototype =
    {
        callbacks: [], init: function (a, b) {
        var c, d = a.series;
        a.series = null;
        c = y(F, a);
        c.series = a.series = d;
        this.userOptions = a;
        d = c.chart;
        this.margin = this.splashArray("margin", d);
        this.spacing = this.splashArray("spacing", d);
        var e = d.events;
        this.bounds = {h: {}, v: {}};
        this.callback = b;
        this.isResizing = 0;
        this.options = c;
        this.axes = [];
        this.series = [];
        this.hasCartesianSeries = d.showAxes;
        var f = this, g;
        f.index = ha.length;
        ha.push(f);
        fb++;
        d.reflow !== !1 && D(f, "load", function () {
            f.initReflow()
        });
        if (e)for (g in e)D(f, g, e[g]);
        f.xAxis = [];
        f.yAxis =
            [];
        f.animation = ma ? !1 : p(d.animation, !0);
        f.pointCount = f.colorCounter = f.symbolCounter = 0;
        f.firstRender()
    }, initSeries: function (a) {
        var b = this.options.chart;
        (b = I[a.type || b.type || b.defaultSeriesType]) || qa(17, !0);
        b = new b;
        b.init(this, a);
        return b
    }, isInsidePlot: function (a, b, c) {
        var d = c ? b : a, a = c ? a : b;
        return d >= 0 && d <= this.plotWidth && a >= 0 && a <= this.plotHeight
    }, redraw: function (a) {
        var b = this.axes, c = this.series, d = this.pointer, e = this.legend, f = this.isDirtyLegend, g, h, i = this.hasCartesianSeries, j = this.isDirtyBox, k = c.length,
            l = k, m = this.renderer, o = m.isHidden(), q = [];
        Ya(a, this);
        o && this.cloneRenderTo();
        for (this.layOutTitles(); l--;)if (a = c[l], a.options.stacking && (g = !0, a.isDirty)) {
            h = !0;
            break
        }
        if (h)for (l = k; l--;)if (a = c[l], a.options.stacking)a.isDirty = !0;
        n(c, function (a) {
            a.isDirty && a.options.legendType === "point" && (f = !0)
        });
        if (f && e.options.enabled)e.render(), this.isDirtyLegend = !1;
        g && this.getStacks();
        if (i && !this.isResizing)this.maxTicks = null, n(b, function (a) {
            a.setScale()
        });
        this.getMargins();
        i && (n(b, function (a) {
            a.isDirty && (j = !0)
        }), n(b,
            function (a) {
                if (a.isDirtyExtremes)a.isDirtyExtremes = !1, q.push(function () {
                    K(a, "afterSetExtremes", x(a.eventArgs, a.getExtremes()));
                    delete a.eventArgs
                });
                (j || g) && a.redraw()
            }));
        j && this.drawChartBox();
        n(c, function (a) {
            a.isDirty && a.visible && (!a.isCartesian || a.xAxis) && a.redraw()
        });
        d && d.reset(!0);
        m.draw();
        K(this, "redraw");
        o && this.cloneRenderTo(!0);
        n(q, function (a) {
            a.call()
        })
    }, get: function (a) {
        var b = this.axes, c = this.series, d, e;
        for (d = 0; d < b.length; d++)if (b[d].options.id === a)return b[d];
        for (d = 0; d < c.length; d++)if (c[d].options.id ===
            a)return c[d];
        for (d = 0; d < c.length; d++) {
            e = c[d].points || [];
            for (b = 0; b < e.length; b++)if (e[b].id === a)return e[b]
        }
        return null
    }, getAxes: function () {
        var a = this, b = this.options, c = b.xAxis = pa(b.xAxis || {}), b = b.yAxis = pa(b.yAxis || {});
        n(c, function (a, b) {
            a.index = b;
            a.isX = !0
        });
        n(b, function (a, b) {
            a.index = b
        });
        c = c.concat(b);
        n(c, function (b) {
            new O(a, b)
        })
    }, getSelectedPoints: function () {
        var a = [];
        n(this.series, function (b) {
            a = a.concat(pb(b.points || [], function (a) {
                return a.selected
            }))
        });
        return a
    }, getSelectedSeries: function () {
        return pb(this.series,
            function (a) {
                return a.selected
            })
    }, getStacks: function () {
        var a = this;
        n(a.yAxis, function (a) {
            if (a.stacks && a.hasVisibleSeries)a.oldStacks = a.stacks
        });
        n(a.series, function (b) {
            if (b.options.stacking && (b.visible === !0 || a.options.chart.ignoreHiddenSeries === !1))b.stackKey = b.type + p(b.options.stack, "")
        })
    }, setTitle: function (a, b, c) {
        var g;
        var d = this, e = d.options, f;
        f = e.title = y(e.title, a);
        g = e.subtitle = y(e.subtitle, b), e = g;
        n([["title", a, f], ["subtitle", b, e]], function (a) {
            var b = a[0], c = d[b], e = a[1], a = a[2];
            c && e && (d[b] = c = c.destroy());
            a && a.text && !c && (d[b] = d.renderer.text(a.text, 0, 0, a.useHTML).attr({
                align: a.align,
                "class": "highcharts-" + b,
                zIndex: a.zIndex || 4
            }).css(a.style).add())
        });
        d.layOutTitles(c)
    }, layOutTitles: function (a) {
        var b = 0, c = this.title, d = this.subtitle, e = this.options, f = e.title, e = e.subtitle, g = this.renderer, h = this.spacingBox.width - 44;
        if (c && (c.css({width: (f.width || h) + "px"}).align(x({y: g.fontMetrics(f.style.fontSize, c).b - 3}, f), !1, "spacingBox"), !f.floating && !f.verticalAlign))b = c.getBBox().height;
        d && (d.css({width: (e.width || h) + "px"}).align(x({
            y: b +
            (f.margin - 13) + g.fontMetrics(f.style.fontSize, d).b
        }, e), !1, "spacingBox"), !e.floating && !e.verticalAlign && (b = ya(b + d.getBBox().height)));
        c = this.titleOffset !== b;
        this.titleOffset = b;
        if (!this.isDirtyBox && c)this.isDirtyBox = c, this.hasRendered && p(a, !0) && this.isDirtyBox && this.redraw()
    }, getChartSize: function () {
        var a = this.options.chart, b = a.width, a = a.height, c = this.renderToClone || this.renderTo;
        if (!s(b))this.containerWidth = ob(c, "width");
        if (!s(a))this.containerHeight = ob(c, "height");
        this.chartWidth = v(0, b || this.containerWidth ||
            600);
        this.chartHeight = v(0, p(a, this.containerHeight > 19 ? this.containerHeight : 400))
    }, cloneRenderTo: function (a) {
        var b = this.renderToClone, c = this.container;
        a ? b && (this.renderTo.appendChild(c), Ta(b), delete this.renderToClone) : (c && c.parentNode === this.renderTo && this.renderTo.removeChild(c), this.renderToClone = b = this.renderTo.cloneNode(0), M(b, {
            position: "absolute",
            top: "-9999px",
            display: "block"
        }), b.style.setProperty && b.style.setProperty("display", "block", "important"), E.body.appendChild(b), c && b.appendChild(c))
    },
        getContainer: function () {
            var a, b = this.options.chart, c, d, e;
            this.renderTo = a = b.renderTo;
            e = "highcharts-" + Hb++;
            if (Ja(a))this.renderTo = a = E.getElementById(a);
            a || qa(13, !0);
            c = C(W(a, "data-highcharts-chart"));
            !isNaN(c) && ha[c] && ha[c].hasRendered && ha[c].destroy();
            W(a, "data-highcharts-chart", this.index);
            a.innerHTML = "";
            !b.skipClone && !a.offsetWidth && this.cloneRenderTo();
            this.getChartSize();
            c = this.chartWidth;
            d = this.chartHeight;
            this.container = a = aa(Ua, {
                className: "highcharts-container" + (b.className ? " " + b.className : ""),
                id: e
            }, x({
                position: "relative",
                overflow: "hidden",
                width: c + "px",
                height: d + "px",
                textAlign: "left",
                lineHeight: "normal",
                zIndex: 0,
                "-webkit-tap-highlight-color": "rgba(0,0,0,0)"
            }, b.style), this.renderToClone || a);
            this._cursor = a.style.cursor;
            this.renderer = b.forExport ? new na(a, c, d, b.style, !0) : new Wa(a, c, d, b.style);
            ma && this.renderer.create(this, a, c, d);
            this.renderer.chartIndex = this.index
        }, getMargins: function (a) {
        var b = this.spacing, c = this.margin, d = this.titleOffset;
        this.resetMargins();
        if (d && !s(c[0]))this.plotTop = v(this.plotTop,
            d + this.options.title.margin + b[0]);
        this.legend.adjustMargins(c, b);
        this.extraBottomMargin && (this.marginBottom += this.extraBottomMargin);
        this.extraTopMargin && (this.plotTop += this.extraTopMargin);
        a || this.getAxisMargins()
    }, getAxisMargins: function () {
        var a = this, b = a.axisOffset = [0, 0, 0, 0], c = a.margin;
        a.hasCartesianSeries && n(a.axes, function (a) {
            a.getOffset()
        });
        n(nb, function (d, e) {
            s(c[e]) || (a[d] += b[e])
        });
        a.setChartSize()
    }, reflow: function (a) {
        var b = this, c = b.options.chart, d = b.renderTo, e = c.width || ob(d, "width"), f = c.height ||
            ob(d, "height"), c = a ? a.target : T, d = function () {
            if (b.container)b.setSize(e, f, !1), b.hasUserSize = null
        };
        if (!b.hasUserSize && e && f && (c === T || c === E)) {
            if (e !== b.containerWidth || f !== b.containerHeight)clearTimeout(b.reflowTimeout), a ? b.reflowTimeout = setTimeout(d, 100) : d();
            b.containerWidth = e;
            b.containerHeight = f
        }
    }, initReflow: function () {
        var a = this, b = function (b) {
            a.reflow(b)
        };
        D(T, "resize", b);
        D(a, "destroy", function () {
            U(T, "resize", b)
        })
    }, setSize: function (a, b, c) {
        var d = this, e, f, g;
        d.isResizing += 1;
        g = function () {
            d && K(d, "endResize",
                null, function () {
                    d.isResizing -= 1
                })
        };
        Ya(c, d);
        d.oldChartHeight = d.chartHeight;
        d.oldChartWidth = d.chartWidth;
        if (s(a))d.chartWidth = e = v(0, w(a)), d.hasUserSize = !!e;
        if (s(b))d.chartHeight = f = v(0, w(b));
        (Fa ? qb : M)(d.container, {width: e + "px", height: f + "px"}, Fa);
        d.setChartSize(!0);
        d.renderer.setSize(e, f, c);
        d.maxTicks = null;
        n(d.axes, function (a) {
            a.isDirty = !0;
            a.setScale()
        });
        n(d.series, function (a) {
            a.isDirty = !0
        });
        d.isDirtyLegend = !0;
        d.isDirtyBox = !0;
        d.layOutTitles();
        d.getMargins();
        d.redraw(c);
        d.oldChartHeight = null;
        K(d, "resize");
        Fa === !1 ? g() : setTimeout(g, Fa && Fa.duration || 500)
    }, setChartSize: function (a) {
        var b = this.inverted, c = this.renderer, d = this.chartWidth, e = this.chartHeight, f = this.options.chart, g = this.spacing, h = this.clipOffset, i, j, k, l;
        this.plotLeft = i = w(this.plotLeft);
        this.plotTop = j = w(this.plotTop);
        this.plotWidth = k = v(0, w(d - i - this.marginRight));
        this.plotHeight = l = v(0, w(e - j - this.marginBottom));
        this.plotSizeX = b ? l : k;
        this.plotSizeY = b ? k : l;
        this.plotBorderWidth = f.plotBorderWidth || 0;
        this.spacingBox = c.spacingBox = {
            x: g[3], y: g[0], width: d -
            g[3] - g[1], height: e - g[0] - g[2]
        };
        this.plotBox = c.plotBox = {x: i, y: j, width: k, height: l};
        d = 2 * X(this.plotBorderWidth / 2);
        b = ya(v(d, h[3]) / 2);
        c = ya(v(d, h[0]) / 2);
        this.clipBox = {
            x: b,
            y: c,
            width: X(this.plotSizeX - v(d, h[1]) / 2 - b),
            height: v(0, X(this.plotSizeY - v(d, h[2]) / 2 - c))
        };
        a || n(this.axes, function (a) {
            a.setAxisSize();
            a.setAxisTranslation()
        })
    }, resetMargins: function () {
        var a = this;
        n(nb, function (b, c) {
            a[b] = p(a.margin[c], a.spacing[c])
        });
        a.axisOffset = [0, 0, 0, 0];
        a.clipOffset = [0, 0, 0, 0]
    }, drawChartBox: function () {
        var a = this.options.chart,
            b = this.renderer, c = this.chartWidth, d = this.chartHeight, e = this.chartBackground, f = this.plotBackground, g = this.plotBorder, h = this.plotBGImage, i = a.borderWidth || 0, j = a.backgroundColor, k = a.plotBackgroundColor, l = a.plotBackgroundImage, m = a.plotBorderWidth || 0, o, q = this.plotLeft, t = this.plotTop, p = this.plotWidth, n = this.plotHeight, u = this.plotBox, r = this.clipRect, s = this.clipBox;
        o = i + (a.shadow ? 8 : 0);
        if (i || j)if (e)e.animate(e.crisp({width: c - o, height: d - o})); else {
            e = {fill: j || Z};
            if (i)e.stroke = a.borderColor, e["stroke-width"] = i;
            this.chartBackground = b.rect(o / 2, o / 2, c - o, d - o, a.borderRadius, i).attr(e).addClass("highcharts-background").add().shadow(a.shadow)
        }
        if (k)f ? f.animate(u) : this.plotBackground = b.rect(q, t, p, n, 0).attr({fill: k}).add().shadow(a.plotShadow);
        if (l)h ? h.animate(u) : this.plotBGImage = b.image(l, q, t, p, n).add();
        r ? r.animate({width: s.width, height: s.height}) : this.clipRect = b.clipRect(s);
        if (m)g ? g.animate(g.crisp({
            x: q,
            y: t,
            width: p,
            height: n,
            strokeWidth: -m
        })) : this.plotBorder = b.rect(q, t, p, n, 0, -m).attr({
            stroke: a.plotBorderColor, "stroke-width": m,
            fill: Z, zIndex: 1
        }).add();
        this.isDirtyBox = !1
    }, propFromSeries: function () {
        var a = this, b = a.options.chart, c, d = a.options.series, e, f;
        n(["inverted", "angular", "polar"], function (g) {
            c = I[b.type || b.defaultSeriesType];
            f = a[g] || b[g] || c && c.prototype[g];
            for (e = d && d.length; !f && e--;)(c = I[d[e].type]) && c.prototype[g] && (f = !0);
            a[g] = f
        })
    }, linkSeries: function () {
        var a = this, b = a.series;
        n(b, function (a) {
            a.linkedSeries.length = 0
        });
        n(b, function (b) {
            var d = b.options.linkedTo;
            if (Ja(d) && (d = d === ":previous" ? a.series[b.index - 1] : a.get(d)))d.linkedSeries.push(b),
                b.linkedParent = d
        })
    }, renderSeries: function () {
        n(this.series, function (a) {
            a.translate();
            a.render()
        })
    }, renderLabels: function () {
        var a = this, b = a.options.labels;
        b.items && n(b.items, function (c) {
            var d = x(b.style, c.style), e = C(d.left) + a.plotLeft, f = C(d.top) + a.plotTop + 12;
            delete d.left;
            delete d.top;
            a.renderer.text(c.html, e, f).attr({zIndex: 2}).css(d).add()
        })
    }, render: function () {
        var a = this.axes, b = this.renderer, c = this.options, d, e, f, g;
        this.setTitle();
        this.legend = new rb(this, c.legend);
        this.getStacks();
        this.getMargins(!0);
        this.setChartSize();
        d = this.plotWidth;
        e = this.plotHeight -= 13;
        n(a, function (a) {
            a.setScale()
        });
        this.getAxisMargins();
        f = d / this.plotWidth > 1.2;
        g = e / this.plotHeight > 1.1;
        if (f || g)this.maxTicks = null, n(a, function (a) {
            (a.horiz && f || !a.horiz && g) && a.setTickInterval(!0)
        }), this.getMargins();
        this.drawChartBox();
        this.hasCartesianSeries && n(a, function (a) {
            a.render()
        });
        if (!this.seriesGroup)this.seriesGroup = b.g("series-group").attr({zIndex: 3}).add();
        this.renderSeries();
        this.renderLabels();
        this.showCredits(c.credits);
        this.hasRendered = !0
    }, showCredits: function (a) {
        if (a.enabled && !this.credits)this.credits = this.renderer.text(a.text, 0, 0).on("click", function () {
            if (a.href)location.href = a.href
        }).attr({align: a.position.align, zIndex: 8}).css(a.style).add().align(a.position)
    }, destroy: function () {
        var a = this, b = a.axes, c = a.series, d = a.container, e, f = d && d.parentNode;
        K(a, "destroy");
        ha[a.index] = r;
        fb--;
        a.renderTo.removeAttribute("data-highcharts-chart");
        U(a);
        for (e = b.length; e--;)b[e] = b[e].destroy();
        for (e = c.length; e--;)c[e] = c[e].destroy();
        n("title,subtitle,chartBackground,plotBackground,plotBGImage,plotBorder,seriesGroup,clipRect,credits,pointer,scroller,rangeSelector,legend,resetZoomButton,tooltip,renderer".split(","),
            function (b) {
                var c = a[b];
                c && c.destroy && (a[b] = c.destroy())
            });
        if (d)d.innerHTML = "", U(d), f && Ta(d);
        for (e in a)delete a[e]
    }, isReadyToRender: function () {
        var a = this;
        return !da && T == T.top && E.readyState !== "complete" || ma && !T.canvg ? (ma ? Tb.push(function () {
            a.firstRender()
        }, a.options.global.canvasToolsURL) : E.attachEvent("onreadystatechange", function () {
            E.detachEvent("onreadystatechange", a.firstRender);
            E.readyState === "complete" && a.firstRender()
        }), !1) : !0
    }, firstRender: function () {
        var a = this, b = a.options, c = a.callback;
        if (a.isReadyToRender()) {
            a.getContainer();
            K(a, "init");
            a.resetMargins();
            a.setChartSize();
            a.propFromSeries();
            a.getAxes();
            n(b.series || [], function (b) {
                a.initSeries(b)
            });
            a.linkSeries();
            K(a, "beforeRender");
            if (z.Pointer)a.pointer = new Xa(a, b);
            a.render();
            a.renderer.draw();
            c && c.apply(a, [a]);
            n(a.callbacks, function (b) {
                a.index !== r && b.apply(a, [a])
            });
            K(a, "load");
            a.cloneRenderTo(!0)
        }
    }, splashArray: function (a, b) {
        var c = b[a], c = ia(c) ? c : [c, c, c, c];
        return [p(b[a + "Top"], c[0]), p(b[a + "Right"], c[1]), p(b[a + "Bottom"], c[2]), p(b[a + "Left"], c[3])]
    }
    };
    var fc = z.CenteredSeriesMixin =
    {
        getCenter: function () {
            var a = this.options, b = this.chart, c = 2 * (a.slicedOffset || 0), d = b.plotWidth - 2 * c, b = b.plotHeight - 2 * c, e = a.center, e = [p(e[0], "50%"), p(e[1], "50%"), a.size || "100%", a.innerSize || 0], f = B(d, b), g, h, i;
            for (h = 0; h < 4; ++h)i = e[h], g = /%$/.test(i), a = h < 2 || h === 2 && g, e[h] = (g ? [d, b, f, e[2]][h] * C(i) / 100 : C(i)) + (a ? c : 0);
            return e
        }
    }, Ba = function () {
    };
    Ba.prototype = {
        init: function (a, b, c) {
            this.series = a;
            this.color = a.color;
            this.applyOptions(b, c);
            this.pointAttr = {};
            if (a.options.colorByPoint && (b = a.options.colors || a.chart.options.colors,
                    this.color = this.color || b[a.colorCounter++], a.colorCounter === b.length))a.colorCounter = 0;
            a.chart.pointCount++;
            return this
        }, applyOptions: function (a, b) {
            var c = this.series, d = c.options.pointValKey || c.pointValKey, a = Ba.prototype.optionsToObject.call(this, a);
            x(this, a);
            this.options = this.options ? x(this.options, a) : a;
            if (d)this.y = this[d];
            if (this.x === r && c)this.x = b === r ? c.autoIncrement() : b;
            return this
        }, optionsToObject: function (a) {
            var b = {}, c = this.series, d = c.pointArrayMap || ["y"], e = d.length, f = 0, g = 0;
            if (typeof a === "number" ||
                a === null)b[d[0]] = a; else if (Ka(a)) {
                if (a.length > e) {
                    c = typeof a[0];
                    if (c === "string")b.name = a[0]; else if (c === "number")b.x = a[0];
                    f++
                }
                for (; g < e;)b[d[g++]] = a[f++]
            } else if (typeof a === "object") {
                b = a;
                if (a.dataLabels)c._hasPointLabels = !0;
                if (a.marker)c._hasPointMarkers = !0
            }
            return b
        }, destroy: function () {
            var a = this.series.chart, b = a.hoverPoints, c;
            a.pointCount--;
            if (b && (this.setState(), ua(b, this), !b.length))a.hoverPoints = null;
            if (this === a.hoverPoint)this.onMouseOut();
            if (this.graphic || this.dataLabel)U(this), this.destroyElements();
            this.legendItem && a.legend.destroyItem(this);
            for (c in this)this[c] = null
        }, destroyElements: function () {
            for (var a = "graphic,dataLabel,dataLabelUpper,group,connector,shadowGroup".split(","), b, c = 6; c--;)b = a[c], this[b] && (this[b] = this[b].destroy())
        }, getLabelConfig: function () {
            return {
                x: this.category,
                y: this.y,
                key: this.name || this.category,
                series: this.series,
                point: this,
                percentage: this.percentage,
                total: this.total || this.stackTotal
            }
        }, tooltipFormatter: function (a) {
            var b = this.series, c = b.tooltipOptions, d = p(c.valueDecimals,
                ""), e = c.valuePrefix || "", f = c.valueSuffix || "";
            n(b.pointArrayMap || ["y"], function (b) {
                b = "{point." + b;
                if (e || f)a = a.replace(b + "}", e + b + "}" + f);
                a = a.replace(b + "}", b + ":,." + d + "f}")
            });
            return Ma(a, {point: this, series: this.series})
        }, firePointEvent: function (a, b, c) {
            var d = this, e = this.series.options;
            (e.point.events[a] || d.options && d.options.events && d.options.events[a]) && this.importEvents();
            a === "click" && e.allowPointSelect && (c = function (a) {
                d.select(null, a.ctrlKey || a.metaKey || a.shiftKey)
            });
            K(this, a, b, c)
        }
    };
    var P = z.Series = function () {
    };
    P.prototype = {
        isCartesian: !0,
        type: "line",
        pointClass: Ba,
        sorted: !0,
        requireSorting: !0,
        pointAttrToOptions: {stroke: "lineColor", "stroke-width": "lineWidth", fill: "fillColor", r: "radius"},
        axisTypes: ["xAxis", "yAxis"],
        colorCounter: 0,
        parallelArrays: ["x", "y"],
        init: function (a, b) {
            var c = this, d, e, f = a.series, g = function (a, b) {
                return p(a.options.index, a._i) - p(b.options.index, b._i)
            };
            c.chart = a;
            c.options = b = c.setOptions(b);
            c.linkedSeries = [];
            c.bindAxes();
            x(c, {
                name: b.name, state: "", pointAttr: {}, visible: b.visible !== !1, selected: b.selected === !0
            });
            if (ma)b.animation = !1;
            e = b.events;
            for (d in e)D(c, d, e[d]);
            if (e && e.click || b.point && b.point.events && b.point.events.click || b.allowPointSelect)a.runTrackerClick = !0;
            c.getColor();
            c.getSymbol();
            n(c.parallelArrays, function (a) {
                c[a + "Data"] = []
            });
            c.setData(b.data, !1);
            if (c.isCartesian)a.hasCartesianSeries = !0;
            f.push(c);
            c._i = f.length - 1;
            xb(f, g);
            this.yAxis && xb(this.yAxis.series, g);
            n(f, function (a, b) {
                a.index = b;
                a.name = a.name || "Series " + (b + 1)
            })
        },
        bindAxes: function () {
            var a = this, b = a.options, c = a.chart, d;
            n(a.axisTypes ||
                [], function (e) {
                n(c[e], function (c) {
                    d = c.options;
                    if (b[e] === d.index || b[e] !== r && b[e] === d.id || b[e] === r && d.index === 0)c.series.push(a), a[e] = c, c.isDirty = !0
                });
                !a[e] && a.optionalAxis !== e && qa(18, !0)
            })
        },
        updateParallelArrays: function (a, b) {
            var c = a.series, d = arguments;
            n(c.parallelArrays, typeof b === "number" ? function (d) {
                var f = d === "y" && c.toYData ? c.toYData(a) : a[d];
                c[d + "Data"][b] = f
            } : function (a) {
                Array.prototype[b].apply(c[a + "Data"], Array.prototype.slice.call(d, 2))
            })
        },
        autoIncrement: function () {
            var a = this.options, b = this.xIncrement,
                c, d = a.pointIntervalUnit, b = p(b, a.pointStart, 0);
            this.pointInterval = c = p(this.pointInterval, a.pointInterval, 1);
            if (d === "month" || d === "year")a = new ea(b), a = d === "month" ? +a[Cb](a[cb]() + c) : +a[Db](a[db]() + c), c = a - b;
            this.xIncrement = b + c;
            return b
        },
        getSegments: function () {
            var a = -1, b = [], c, d = this.points, e = d.length;
            if (e)if (this.options.connectNulls) {
                for (c = e; c--;)d[c].y === null && d.splice(c, 1);
                d.length && (b = [d])
            } else n(d, function (c, g) {
                c.y === null ? (g > a + 1 && b.push(d.slice(a + 1, g)), a = g) : g === e - 1 && b.push(d.slice(a + 1, g + 1))
            });
            this.segments =
                b
        },
        setOptions: function (a) {
            var b = this.chart, c = b.options.plotOptions, b = b.userOptions || {}, d = b.plotOptions || {}, e = c[this.type];
            this.userOptions = a;
            c = y(e, c.series, a);
            this.tooltipOptions = y(F.tooltip, F.plotOptions[this.type].tooltip, b.tooltip, d.series && d.series.tooltip, d[this.type] && d[this.type].tooltip, a.tooltip);
            e.marker === null && delete c.marker;
            this.zoneAxis = c.zoneAxis;
            a = this.zones = (c.zones || []).slice();
            if ((c.negativeColor || c.negativeFillColor) && !c.zones)a.push({
                value: c[this.zoneAxis + "Threshold"] || c.threshold ||
                0, color: c.negativeColor, fillColor: c.negativeFillColor
            });
            a.length && s(a[a.length - 1].value) && a.push({color: this.color, fillColor: this.fillColor});
            return c
        },
        getCyclic: function (a, b, c) {
            var d = this.userOptions, e = "_" + a + "Index", f = a + "Counter";
            b || (s(d[e]) ? b = d[e] : (d[e] = b = this.chart[f] % c.length, this.chart[f] += 1), b = c[b]);
            this[a] = b
        },
        getColor: function () {
            this.options.colorByPoint || this.getCyclic("color", this.options.color || V[this.type].color, this.chart.options.colors)
        },
        getSymbol: function () {
            var a = this.options.marker;
            this.getCyclic("symbol", a.symbol, this.chart.options.symbols);
            if (/^url/.test(this.symbol))a.radius = 0
        },
        drawLegendSymbol: G.drawLineMarker,
        setData: function (a, b, c, d) {
            var e = this, f = e.points, g = f && f.length || 0, h, i = e.options, j = e.chart, k = null, l = e.xAxis, m = l && !!l.categories, o = i.turboThreshold, q = this.xData, t = this.yData, J = (h = e.pointArrayMap) && h.length, a = a || [];
            h = a.length;
            b = p(b, !0);
            if (d !== !1 && h && g === h && !e.cropped && !e.hasGroupedData && e.visible)n(a, function (a, b) {
                f[b].update(a, !1, null, !1)
            }); else {
                e.xIncrement = null;
                e.pointRange =
                    m ? 1 : i.pointRange;
                e.colorCounter = 0;
                n(this.parallelArrays, function (a) {
                    e[a + "Data"].length = 0
                });
                if (o && h > o) {
                    for (c = 0; k === null && c < h;)k = a[c], c++;
                    if (sa(k)) {
                        m = p(i.pointStart, 0);
                        i = p(i.pointInterval, 1);
                        for (c = 0; c < h; c++)q[c] = m, t[c] = a[c], m += i;
                        e.xIncrement = m
                    } else if (Ka(k))if (J)for (c = 0; c < h; c++)i = a[c], q[c] = i[0], t[c] = i.slice(1, J + 1); else for (c = 0; c < h; c++)i = a[c], q[c] = i[0], t[c] = i[1]; else qa(12)
                } else for (c = 0; c < h; c++)if (a[c] !== r && (i = {series: e}, e.pointClass.prototype.applyOptions.apply(i, [a[c]]), e.updateParallelArrays(i, c),
                    m && i.name))l.names[i.x] = i.name;
                Ja(t[0]) && qa(14, !0);
                e.data = [];
                e.options.data = a;
                for (c = g; c--;)f[c] && f[c].destroy && f[c].destroy();
                if (l)l.minRange = l.userMinRange;
                e.isDirty = e.isDirtyData = j.isDirtyBox = !0;
                c = !1
            }
            b && j.redraw(c)
        },
        processData: function (a) {
            var b = this.xData, c = this.yData, d = b.length, e;
            e = 0;
            var f, g, h = this.xAxis, i, j = this.options;
            i = j.cropThreshold;
            var k = this.isCartesian, l, m;
            if (k && !this.isDirty && !h.isDirty && !this.yAxis.isDirty && !a)return !1;
            if (h)a = h.getExtremes(), l = a.min, m = a.max;
            if (k && this.sorted && (!i ||
                d > i || this.forceCrop))if (b[d - 1] < l || b[0] > m)b = [], c = []; else if (b[0] < l || b[d - 1] > m)e = this.cropData(this.xData, this.yData, l, m), b = e.xData, c = e.yData, e = e.start, f = !0;
            for (i = b.length - 1; i >= 0; i--)d = b[i] - b[i - 1], d > 0 && (g === r || d < g) ? g = d : d < 0 && this.requireSorting && qa(15);
            this.cropped = f;
            this.cropStart = e;
            this.processedXData = b;
            this.processedYData = c;
            if (j.pointRange === null)this.pointRange = g || 1;
            this.closestPointRange = g
        },
        cropData: function (a, b, c, d) {
            var e = a.length, f = 0, g = e, h = p(this.cropShoulder, 1), i;
            for (i = 0; i < e; i++)if (a[i] >= c) {
                f =
                    v(0, i - h);
                break
            }
            for (; i < e; i++)if (a[i] > d) {
                g = i + h;
                break
            }
            return {xData: a.slice(f, g), yData: b.slice(f, g), start: f, end: g}
        },
        generatePoints: function () {
            var a = this.options.data, b = this.data, c, d = this.processedXData, e = this.processedYData, f = this.pointClass, g = d.length, h = this.cropStart || 0, i, j = this.hasGroupedData, k, l = [], m;
            if (!b && !j)b = [], b.length = a.length, b = this.data = b;
            for (m = 0; m < g; m++)i = h + m, j ? l[m] = (new f).init(this, [d[m]].concat(pa(e[m]))) : (b[i] ? k = b[i] : a[i] !== r && (b[i] = k = (new f).init(this, a[i], d[m])), l[m] = k), l[m].index =
                i;
            if (b && (g !== (c = b.length) || j))for (m = 0; m < c; m++)if (m === h && !j && (m += g), b[m])b[m].destroyElements(), b[m].plotX = r;
            this.data = b;
            this.points = l
        },
        getExtremes: function (a) {
            var b = this.yAxis, c = this.processedXData, d, e = [], f = 0;
            d = this.xAxis.getExtremes();
            var g = d.min, h = d.max, i, j, k, l, a = a || this.stackedYData || this.processedYData;
            d = a.length;
            for (l = 0; l < d; l++)if (j = c[l], k = a[l], i = k !== null && k !== r && (!b.isLog || k.length || k > 0), j = this.getExtremesFromAll || this.cropped || (c[l + 1] || j) >= g && (c[l - 1] || j) <= h, i && j)if (i = k.length)for (; i--;)k[i] !==
            null && (e[f++] = k[i]); else e[f++] = k;
            this.dataMin = p(void 0, Sa(e));
            this.dataMax = p(void 0, Ea(e))
        },
        translate: function () {
            this.processedXData || this.processData();
            this.generatePoints();
            for (var a = this.options, b = a.stacking, c = this.xAxis, d = c.categories, e = this.yAxis, f = this.points, g = f.length, h = !!this.modifyValue, i = a.pointPlacement, j = i === "between" || sa(i), k = a.threshold, l, m, o, q = Number.MAX_VALUE, a = 0; a < g; a++) {
                var t = f[a], n = t.x, N = t.y;
                m = t.low;
                var u = b && e.stacks[(this.negStacks && N < k ? "-" : "") + this.stackKey];
                if (e.isLog && N !==
                    null && N <= 0)t.y = N = null, qa(10);
                t.plotX = l = c.translate(n, 0, 0, 0, 1, i, this.type === "flags");
                if (b && this.visible && u && u[n])u = u[n], N = u.points[this.index + "," + a], m = N[0], N = N[1], m === 0 && (m = p(k, e.min)), e.isLog && m <= 0 && (m = null), t.total = t.stackTotal = u.total, t.percentage = u.total && t.y / u.total * 100, t.stackY = N, u.setOffset(this.pointXOffset || 0, this.barW || 0);
                t.yBottom = s(m) ? e.translate(m, 0, 1, 0, 1) : null;
                h && (N = this.modifyValue(N, t));
                t.plotY = m = typeof N === "number" && N !== Infinity ? B(v(-1E5, e.translate(N, 0, 1, 0, 1)), 1E5) : r;
                t.isInside = m !==
                    r && m >= 0 && m <= e.len && l >= 0 && l <= c.len;
                t.clientX = j ? c.translate(n, 0, 0, 0, 1) : l;
                t.negative = t.y < (k || 0);
                t.category = d && d[t.x] !== r ? d[t.x] : t.x;
                a && (q = B(q, R(l - o)));
                o = l
            }
            this.closestPointRangePx = q;
            this.getSegments()
        },
        setClip: function (a) {
            var b = this.chart, c = b.renderer, d = b.inverted, e = this.clipBox, f = e || b.clipBox, g = this.sharedClipKey || ["_sharedClip", a && a.duration, a && a.easing, f.height].join(","), h = b[g], i = b[g + "m"];
            if (!h) {
                if (a)f.width = 0, b[g + "m"] = i = c.clipRect(-99, d ? -b.plotLeft : -b.plotTop, 99, d ? b.chartWidth : b.chartHeight);
                b[g] =
                    h = c.clipRect(f)
            }
            a && (h.count += 1);
            if (this.options.clip !== !1)this.group.clip(a || e ? h : b.clipRect), this.markerGroup.clip(i), this.sharedClipKey = g;
            a || (h.count -= 1, h.count <= 0 && g && b[g] && (e || (b[g] = b[g].destroy()), b[g + "m"] && (b[g + "m"] = b[g + "m"].destroy())))
        },
        animate: function (a) {
            var b = this.chart, c = this.options.animation, d;
            if (c && !ia(c))c = V[this.type].animation;
            a ? this.setClip(c) : (d = this.sharedClipKey, (a = b[d]) && a.animate({width: b.plotSizeX}, c), b[d + "m"] && b[d + "m"].animate({width: b.plotSizeX + 99}, c), this.animate = null)
        },
        afterAnimate: function () {
            this.setClip();
            K(this, "afterAnimate")
        },
        drawPoints: function () {
            var a, b = this.points, c = this.chart, d, e, f, g, h, i, j, k, l = this.options.marker, m = this.pointAttr[""], o, q, t, n = this.markerGroup, N = p(l.enabled, this.xAxis.isRadial, this.closestPointRangePx > 2 * l.radius);
            if (l.enabled !== !1 || this._hasPointMarkers)for (f = b.length; f--;)if (g = b[f], d = X(g.plotX), e = g.plotY, k = g.graphic, o = g.marker || {}, q = !!g.marker, a = N && o.enabled === r || o.enabled, t = g.isInside, a && e !== r && !isNaN(e) && g.y !== null)if (a = g.pointAttr[g.selected ?
                        "select" : ""] || m, h = a.r, i = p(o.symbol, this.symbol), j = i.indexOf("url") === 0, k)k[t ? "show" : "hide"](!0).animate(x({
                x: d - h,
                y: e - h
            }, k.symbolName ? {width: 2 * h, height: 2 * h} : {})); else {
                if (t && (h > 0 || j))g.graphic = c.renderer.symbol(i, d - h, e - h, 2 * h, 2 * h, q ? o : l).attr(a).add(n)
            } else if (k)g.graphic = k.destroy()
        },
        convertAttribs: function (a, b, c, d) {
            var e = this.pointAttrToOptions, f, g, h = {}, a = a || {}, b = b || {}, c = c || {}, d = d || {};
            for (f in e)g = e[f], h[f] = p(a[g], b[f], c[f], d[f]);
            return h
        },
        getAttribs: function () {
            var a = this, b = a.options, c = V[a.type].marker ?
                b.marker : b, d = c.states, e = d.hover, f, g = a.color, h = a.options.negativeColor;
            f = {stroke: g, fill: g};
            var i = a.points || [], j, k = [], l, m = a.pointAttrToOptions;
            l = a.hasPointSpecificOptions;
            var o = c.lineColor, q = c.fillColor;
            j = b.turboThreshold;
            var t = a.zones, p = a.zoneAxis || "y", N;
            b.marker ? (e.radius = e.radius || c.radius + e.radiusPlus, e.lineWidth = e.lineWidth || c.lineWidth + e.lineWidthPlus) : (e.color = e.color || wa(e.color || g).brighten(e.brightness).get(), e.negativeColor = e.negativeColor || wa(e.negativeColor || h).brighten(e.brightness).get());
            k[""] = a.convertAttribs(c, f);
            n(["hover", "select"], function (b) {
                k[b] = a.convertAttribs(d[b], k[""])
            });
            a.pointAttr = k;
            g = i.length;
            if (!j || g < j || l)for (; g--;) {
                j = i[g];
                if ((c = j.options && j.options.marker || j.options) && c.enabled === !1)c.radius = 0;
                if (t.length) {
                    l = 0;
                    for (f = t[l]; j[p] >= f.value;)f = t[++l];
                    j.color = j.fillColor = f.color
                }
                l = b.colorByPoint || j.color;
                if (j.options)for (N in m)s(c[m[N]]) && (l = !0);
                if (l) {
                    c = c || {};
                    l = [];
                    d = c.states || {};
                    f = d.hover = d.hover || {};
                    if (!b.marker)f.color = f.color || !j.options.color && e[j.negative && h ? "negativeColor" :
                            "color"] || wa(j.color).brighten(f.brightness || e.brightness).get();
                    f = {color: j.color};
                    if (!q)f.fillColor = j.color;
                    if (!o)f.lineColor = j.color;
                    l[""] = a.convertAttribs(x(f, c), k[""]);
                    l.hover = a.convertAttribs(d.hover, k.hover, l[""]);
                    l.select = a.convertAttribs(d.select, k.select, l[""])
                } else l = k;
                j.pointAttr = l
            }
        },
        destroy: function () {
            var a = this, b = a.chart, c = /AppleWebKit\/533/.test(Ga), d, e, f = a.data || [], g, h, i;
            K(a, "destroy");
            U(a);
            n(a.axisTypes || [], function (b) {
                if (i = a[b])ua(i.series, a), i.isDirty = i.forceRedraw = !0
            });
            a.legendItem &&
            a.chart.legend.destroyItem(a);
            for (e = f.length; e--;)(g = f[e]) && g.destroy && g.destroy();
            a.points = null;
            clearTimeout(a.animationTimeout);
            n("area,graph,dataLabelsGroup,group,markerGroup,tracker,graphNeg,areaNeg,posClip,negClip".split(","), function (b) {
                a[b] && (d = c && b === "group" ? "hide" : "destroy", a[b][d]())
            });
            if (b.hoverSeries === a)b.hoverSeries = null;
            ua(b.series, a);
            for (h in a)delete a[h]
        },
        getSegmentPath: function (a) {
            var b = this, c = [], d = b.options.step;
            n(a, function (e, f) {
                var g = e.plotX, h = e.plotY, i;
                b.getPointSpline ? c.push.apply(c,
                    b.getPointSpline(a, e, f)) : (c.push(f ? "L" : "M"), d && f && (i = a[f - 1], d === "right" ? c.push(i.plotX, h) : d === "center" ? c.push((i.plotX + g) / 2, i.plotY, (i.plotX + g) / 2, h) : c.push(g, i.plotY)), c.push(e.plotX, e.plotY))
            });
            return c
        },
        getGraphPath: function () {
            var a = this, b = [], c, d = [];
            n(a.segments, function (e) {
                c = a.getSegmentPath(e);
                e.length > 1 ? b = b.concat(c) : d.push(e[0])
            });
            a.singlePoints = d;
            return a.graphPath = b
        },
        drawGraph: function () {
            var a = this, b = this.options, c = [["graph", b.lineColor || this.color, b.dashStyle]], d = b.lineWidth, e = b.linecap !==
                "square", f = this.getGraphPath(), g = this.fillGraph && this.color || Z;
            n(this.zones, function (d, e) {
                c.push(["colorGraph" + e, d.color || a.color, d.dashStyle || b.dashStyle])
            });
            n(c, function (c, i) {
                var j = c[0], k = a[j];
                if (k)gb(k), k.animate({d: f}); else if ((d || g) && f.length)k = {
                    stroke: c[1],
                    "stroke-width": d,
                    fill: g,
                    zIndex: 1
                }, c[2] ? k.dashstyle = c[2] : e && (k["stroke-linecap"] = k["stroke-linejoin"] = "round"), a[j] = a.chart.renderer.path(f).attr(k).add(a.group).shadow(!i && b.shadow)
            })
        },
        applyZones: function () {
            var a = this, b = this.chart, c = b.renderer,
                d = this.zones, e, f, g = this.clips || [], h, i = this.graph, j = this.area, k = v(b.chartWidth, b.chartHeight), l = this[(this.zoneAxis || "y") + "Axis"], m = l.reversed, o = l.horiz, q = !1;
            if (d.length && (i || j))i.hide(), j && j.hide(), n(d, function (d, i) {
                e = p(f, m ? o ? b.plotWidth : 0 : o ? 0 : l.toPixels(l.min));
                f = w(l.toPixels(p(d.value, l.max), !0));
                q && (e = f = l.toPixels(l.max));
                if (l.isXAxis) {
                    if (h = {x: m ? f : e, y: 0, width: Math.abs(e - f), height: k}, !o)h.x = b.plotHeight - h.x
                } else if (h = {x: 0, y: m ? e : f, width: k, height: Math.abs(e - f)}, o)h.y = b.plotWidth - h.y;
                b.inverted && c.isVML &&
                (h = l.isXAxis ? {
                    x: 0,
                    y: m ? e : f,
                    height: h.width,
                    width: b.chartWidth
                } : {x: h.y - b.plotLeft - b.spacingBox.x, y: 0, width: h.height, height: b.chartHeight});
                g[i] ? g[i].animate(h) : (g[i] = c.clipRect(h), a["colorGraph" + i].clip(g[i]), j && a["colorArea" + i].clip(g[i]));
                q = d.value > l.max
            }), this.clips = g
        },
        invertGroups: function () {
            function a() {
                var a = {width: b.yAxis.len, height: b.xAxis.len};
                n(["group", "markerGroup"], function (c) {
                    b[c] && b[c].attr(a).invert()
                })
            }

            var b = this, c = b.chart;
            if (b.xAxis)D(c, "resize", a), D(b, "destroy", function () {
                U(c, "resize",
                    a)
            }), a(), b.invertGroups = a
        },
        plotGroup: function (a, b, c, d, e) {
            var f = this[a], g = !f;
            g && (this[a] = f = this.chart.renderer.g(b).attr({visibility: c, zIndex: d || 0.1}).add(e));
            f[g ? "attr" : "animate"](this.getPlotBox());
            return f
        },
        getPlotBox: function () {
            var a = this.chart, b = this.xAxis, c = this.yAxis;
            if (a.inverted)b = c, c = this.xAxis;
            return {translateX: b ? b.left : a.plotLeft, translateY: c ? c.top : a.plotTop, scaleX: 1, scaleY: 1}
        },
        render: function () {
            var a = this, b = a.chart, c, d = a.options, e = (c = d.animation) && !!a.animate && b.renderer.isSVG && p(c.duration,
                    500) || 0, f = a.visible ? "visible" : "hidden", g = d.zIndex, h = a.hasRendered, i = b.seriesGroup;
            c = a.plotGroup("group", "series", f, g, i);
            a.markerGroup = a.plotGroup("markerGroup", "markers", f, g, i);
            e && a.animate(!0);
            a.getAttribs();
            c.inverted = a.isCartesian ? b.inverted : !1;
            a.drawGraph && (a.drawGraph(), a.applyZones());
            n(a.points, function (a) {
                a.redraw && a.redraw()
            });
            a.drawDataLabels && a.drawDataLabels();
            a.visible && a.drawPoints();
            a.drawTracker && a.options.enableMouseTracking !== !1 && a.drawTracker();
            b.inverted && a.invertGroups();
            d.clip !== !1 && !a.sharedClipKey && !h && c.clip(b.clipRect);
            e && a.animate();
            if (!h)e ? a.animationTimeout = setTimeout(function () {
                a.afterAnimate()
            }, e) : a.afterAnimate();
            a.isDirty = a.isDirtyData = !1;
            a.hasRendered = !0
        },
        redraw: function () {
            var a = this.chart, b = this.isDirtyData, c = this.group, d = this.xAxis, e = this.yAxis;
            c && (a.inverted && c.attr({
                width: a.plotWidth,
                height: a.plotHeight
            }), c.animate({translateX: p(d && d.left, a.plotLeft), translateY: p(e && e.top, a.plotTop)}));
            this.translate();
            this.render();
            b && (delete this.kdTree, K(this, "updatedData"))
        },
        kdDimensions: 1,
        kdTree: null,
        kdAxisArray: ["plotX", "plotY"],
        kdComparer: "distX",
        searchPoint: function (a) {
            var b = this.xAxis, c = this.yAxis, d = this.chart.inverted;
            a.plotX = d ? b.len - a.chartY + b.pos : a.chartX - b.pos;
            a.plotY = d ? c.len - a.chartX + c.pos : a.chartY - c.pos;
            return this.searchKDTree(a)
        },
        buildKDTree: function () {
            function a(b, d, g) {
                var h, i;
                if (i = b && b.length)return h = c.kdAxisArray[d % g], b.sort(function (a, b) {
                    return a[h] - b[h]
                }), i = Math.floor(i / 2), {
                    point: b[i],
                    left: a(b.slice(0, i), d + 1, g),
                    right: a(b.slice(i + 1), d + 1, g)
                }
            }

            function b() {
                c.kdTree =
                    a(c.points.slice(), d, d)
            }

            var c = this, d = c.kdDimensions;
            delete c.kdTree;
            c.options.kdSync ? b() : setTimeout(b)
        },
        searchKDTree: function (a) {
            function b(a, h, i, j) {
                var k = h.point, l = c.kdAxisArray[i % j], m, o = k;
                m = s(a[e]) && s(k[e]) ? Math.pow(a[e] - k[e], 2) : null;
                var q = s(a[f]) && s(k[f]) ? Math.pow(a[f] - k[f], 2) : null, t = (m || 0) + (q || 0);
                m = {
                    distX: s(m) ? Math.sqrt(m) : Number.MAX_VALUE,
                    distY: s(q) ? Math.sqrt(q) : Number.MAX_VALUE,
                    distR: s(t) ? Math.sqrt(t) : Number.MAX_VALUE
                };
                k.dist = m;
                l = a[l] - k[l];
                m = l < 0 ? "left" : "right";
                h[m] && (m = b(a, h[m], i + 1, j), o = m.dist[d] <
                o.dist[d] ? m : k, k = l < 0 ? "right" : "left", h[k] && Math.sqrt(l * l) < o.dist[d] && (a = b(a, h[k], i + 1, j), o = a.dist[d] < o.dist[d] ? a : o));
                return o
            }

            var c = this, d = this.kdComparer, e = this.kdAxisArray[0], f = this.kdAxisArray[1];
            this.kdTree || this.buildKDTree();
            if (this.kdTree)return b(a, this.kdTree, this.kdDimensions, this.kdDimensions)
        }
    };
    Qb.prototype = {
        destroy: function () {
            Na(this, this.axis)
        }, render: function (a) {
            var b = this.options, c = b.format, c = c ? Ma(c, this) : b.formatter.call(this);
            this.label ? this.label.attr({text: c, visibility: "hidden"}) :
                this.label = this.axis.chart.renderer.text(c, null, null, b.useHTML).css(b.style).attr({
                    align: this.textAlign,
                    rotation: b.rotation,
                    visibility: "hidden"
                }).add(a)
        }, setOffset: function (a, b) {
            var c = this.axis, d = c.chart, e = d.inverted, f = this.isNegative, g = c.translate(c.usePercentage ? 100 : this.total, 0, 0, 0, 1), c = c.translate(0), c = R(g - c), h = d.xAxis[0].translate(this.x) + a, i = d.plotHeight, f = {
                x: e ? f ? g : g - c : h,
                y: e ? i - h - b : f ? i - g - c : i - g,
                width: e ? c : b,
                height: e ? b : c
            };
            if (e = this.label)e.align(this.alignOptions, null, f), f = e.alignAttr, e[this.options.crop === !1 || d.isInsidePlot(f.x, f.y) ? "show" : "hide"](!0)
        }
    };
    O.prototype.buildStacks = function () {
        var a = this.series, b = p(this.options.reversedStacks, !0), c = a.length;
        if (!this.isXAxis) {
            for (this.usePercentage = !1; c--;)a[b ? c : a.length - c - 1].setStackedPoints();
            if (this.usePercentage)for (c = 0; c < a.length; c++)a[c].setPercentStacks()
        }
    };
    O.prototype.renderStackTotals = function () {
        var a = this.chart, b = a.renderer, c = this.stacks, d, e, f = this.stackTotalGroup;
        if (!f)this.stackTotalGroup = f = b.g("stack-labels").attr({visibility: "visible", zIndex: 6}).add();
        f.translate(a.plotLeft, a.plotTop);
        for (d in c)for (e in a = c[d], a)a[e].render(f)
    };
    P.prototype.setStackedPoints = function () {
        if (this.options.stacking && !(this.visible !== !0 && this.chart.options.chart.ignoreHiddenSeries !== !1)) {
            var a = this.processedXData, b = this.processedYData, c = [], d = b.length, e = this.options, f = e.threshold, g = e.stack, e = e.stacking, h = this.stackKey, i = "-" + h, j = this.negStacks, k = this.yAxis, l = k.stacks, m = k.oldStacks, o, q, t, p, n, u;
            for (p = 0; p < d; p++) {
                n = a[p];
                u = b[p];
                t = this.index + "," + p;
                q = (o = j && u < f) ? i : h;
                l[q] || (l[q] =
                {});
                if (!l[q][n])m[q] && m[q][n] ? (l[q][n] = m[q][n], l[q][n].total = null) : l[q][n] = new Qb(k, k.options.stackLabels, o, n, g);
                q = l[q][n];
                q.points[t] = [q.cum || 0];
                e === "percent" ? (o = o ? h : i, j && l[o] && l[o][n] ? (o = l[o][n], q.total = o.total = v(o.total, q.total) + R(u) || 0) : q.total = la(q.total + (R(u) || 0))) : q.total = la(q.total + (u || 0));
                q.cum = (q.cum || 0) + (u || 0);
                q.points[t].push(q.cum);
                c[p] = q.cum
            }
            if (e === "percent")k.usePercentage = !0;
            this.stackedYData = c;
            k.oldStacks = {}
        }
    };
    P.prototype.setPercentStacks = function () {
        var a = this, b = a.stackKey, c = a.yAxis.stacks,
            d = a.processedXData;
        n([b, "-" + b], function (b) {
            var e;
            for (var f = d.length, g, h; f--;)if (g = d[f], e = (h = c[b] && c[b][g]) && h.points[a.index + "," + f], g = e)h = h.total ? 100 / h.total : 0, g[0] = la(g[0] * h), g[1] = la(g[1] * h), a.stackedYData[f] = g[1]
        })
    };
    x(Pa.prototype, {
        addSeries: function (a, b, c) {
            var d, e = this;
            a && (b = p(b, !0), K(e, "addSeries", {options: a}, function () {
                d = e.initSeries(a);
                e.isDirtyLegend = !0;
                e.linkSeries();
                b && e.redraw(c)
            }));
            return d
        }, addAxis: function (a, b, c, d) {
            var e = b ? "xAxis" : "yAxis", f = this.options;
            new O(this, y(a, {
                index: this[e].length,
                isX: b
            }));
            f[e] = pa(f[e] || {});
            f[e].push(a);
            p(c, !0) && this.redraw(d)
        }, showLoading: function (a) {
            var b = this, c = b.options, d = b.loadingDiv, e = c.loading, f = function () {
                d && M(d, {
                    left: b.plotLeft + "px",
                    top: b.plotTop + "px",
                    width: b.plotWidth + "px",
                    height: b.plotHeight + "px"
                })
            };
            if (!d)b.loadingDiv = d = aa(Ua, {className: "highcharts-loading"}, x(e.style, {
                zIndex: 10,
                display: Z
            }), b.container), b.loadingSpan = aa("span", null, e.labelStyle, d), D(b, "redraw", f);
            b.loadingSpan.innerHTML = a || c.lang.loading;
            if (!b.loadingShown)M(d, {opacity: 0, display: ""}),
                qb(d, {opacity: e.style.opacity}, {duration: e.showDuration || 0}), b.loadingShown = !0;
            f()
        }, hideLoading: function () {
            var a = this.options, b = this.loadingDiv;
            b && qb(b, {opacity: 0}, {
                duration: a.loading.hideDuration || 100, complete: function () {
                    M(b, {display: Z})
                }
            });
            this.loadingShown = !1
        }
    });
    x(Ba.prototype, {
        update: function (a, b, c, d) {
            function e() {
                f.applyOptions(a);
                if (ia(a) && !Ka(a))f.redraw = function () {
                    if (h)a && a.marker && a.marker.symbol ? f.graphic = h.destroy() : h.attr(f.pointAttr[f.state || ""]);
                    if (a && a.dataLabels && f.dataLabel)f.dataLabel =
                        f.dataLabel.destroy();
                    f.redraw = null
                };
                i = f.index;
                g.updateParallelArrays(f, i);
                if (l && f.name)l[f.x] = f.name;
                k.data[i] = f.options;
                g.isDirty = g.isDirtyData = !0;
                if (!g.fixedBox && g.hasCartesianSeries)j.isDirtyBox = !0;
                k.legendType === "point" && (g.updateTotals(), j.legend.clearItems());
                b && j.redraw(c)
            }

            var f = this, g = f.series, h = f.graphic, i, j = g.chart, k = g.options, l = g.xAxis && g.xAxis.names, b = p(b, !0);
            d === !1 ? e() : f.firePointEvent("update", {options: a}, e)
        }, remove: function (a, b) {
            this.series.removePoint(Oa(this, this.series.data), a,
                b)
        }
    });
    x(P.prototype, {
        addPoint: function (a, b, c, d) {
            var e = this.options, f = this.data, g = this.graph, h = this.area, i = this.chart, j = this.xAxis && this.xAxis.names, k = g && g.shift || 0, l = e.data, m, o = this.xData;
            Ya(d, i);
            c && n([g, h, this.graphNeg, this.areaNeg], function (a) {
                if (a)a.shift = k + 1
            });
            if (h)h.isArea = !0;
            b = p(b, !0);
            d = {series: this};
            this.pointClass.prototype.applyOptions.apply(d, [a]);
            g = d.x;
            h = o.length;
            if (this.requireSorting && g < o[h - 1])for (m = !0; h && o[h - 1] > g;)h--;
            this.updateParallelArrays(d, "splice", h, 0, 0);
            this.updateParallelArrays(d,
                h);
            if (j && d.name)j[g] = d.name;
            l.splice(h, 0, a);
            m && (this.data.splice(h, 0, null), this.processData());
            e.legendType === "point" && this.generatePoints();
            c && (f[0] && f[0].remove ? f[0].remove(!1) : (f.shift(), this.updateParallelArrays(d, "shift"), l.shift()));
            this.isDirtyData = this.isDirty = !0;
            b && (this.getAttribs(), i.redraw())
        }, removePoint: function (a, b, c) {
            var d = this, e = d.data, f = e[a], g = d.points, h = d.chart, i = function () {
                e.length === g.length && g.splice(a, 1);
                e.splice(a, 1);
                d.options.data.splice(a, 1);
                d.updateParallelArrays(f || {series: d},
                    "splice", a, 1);
                f && f.destroy();
                d.isDirty = !0;
                d.isDirtyData = !0;
                b && h.redraw()
            };
            Ya(c, h);
            b = p(b, !0);
            f ? f.firePointEvent("remove", null, i) : i()
        }, remove: function (a, b) {
            var c = this, d = c.chart, a = p(a, !0);
            if (!c.isRemoving)c.isRemoving = !0, K(c, "remove", null, function () {
                c.destroy();
                d.isDirtyLegend = d.isDirtyBox = !0;
                d.linkSeries();
                a && d.redraw(b)
            });
            c.isRemoving = !1
        }, update: function (a, b) {
            var c = this, d = this.chart, e = this.userOptions, f = this.type, g = I[f].prototype, h = ["group", "markerGroup", "dataLabelsGroup"], i;
            if (a.type && a.type !== f ||
                a.zIndex !== void 0)h.length = 0;
            n(h, function (a) {
                h[a] = c[a];
                delete c[a]
            });
            a = y(e, {animation: !1, index: this.index, pointStart: this.xData[0]}, {data: this.options.data}, a);
            this.remove(!1);
            for (i in g)this[i] = r;
            x(this, I[a.type || f].prototype);
            n(h, function (a) {
                c[a] = h[a]
            });
            this.init(d, a);
            d.linkSeries();
            p(b, !0) && d.redraw(!1)
        }
    });
    x(O.prototype, {
        update: function (a, b) {
            var c = this.chart, a = c.options[this.coll][this.options.index] = y(this.userOptions, a);
            this.destroy(!0);
            this._addedPlotLB = r;
            this.init(c, x(a, {events: r}));
            c.isDirtyBox = !0;
            p(b, !0) && c.redraw()
        }, remove: function (a) {
            for (var b = this.chart, c = this.coll, d = this.series, e = d.length; e--;)d[e] && d[e].remove(!1);
            ua(b.axes, this);
            ua(b[c], this);
            b.options[c].splice(this.options.index, 1);
            n(b[c], function (a, b) {
                a.options.index = b
            });
            this.destroy();
            b.isDirtyBox = !0;
            p(a, !0) && b.redraw()
        }, setTitle: function (a, b) {
            this.update({title: a}, b)
        }, setCategories: function (a, b) {
            this.update({categories: a}, b)
        }
    });
    var Ca = ja(P);
    I.line = Ca;
    V.area = y(Q, {threshold: 0});
    var xa = ja(P, {
        type: "area", getSegments: function () {
            var a =
                this, b = [], c = [], d = [], e = this.xAxis, f = this.yAxis, g = f.stacks[this.stackKey], h = {}, i, j, k = this.points, l = this.options.connectNulls, m, o;
            if (this.options.stacking && !this.cropped) {
                for (m = 0; m < k.length; m++)h[k[m].x] = k[m];
                for (o in g)g[o].total !== null && d.push(+o);
                d.sort(function (a, b) {
                    return a - b
                });
                n(d, function (b) {
                    var d = 0, k;
                    if (!l || h[b] && h[b].y !== null)if (h[b])c.push(h[b]); else {
                        for (m = a.index; m <= f.series.length; m++)if (k = g[b].points[m + "," + b]) {
                            d = k[1];
                            break
                        }
                        i = e.translate(b);
                        j = f.toPixels(d, !0);
                        c.push({
                            y: null, plotX: i, clientX: i,
                            plotY: j, yBottom: j, onMouseOver: ga
                        })
                    }
                });
                c.length && b.push(c)
            } else P.prototype.getSegments.call(this), b = this.segments;
            this.segments = b
        }, getSegmentPath: function (a) {
            var b = P.prototype.getSegmentPath.call(this, a), c = [].concat(b), d, e = this.options;
            d = b.length;
            var f = this.yAxis.getThreshold(e.threshold), g;
            d === 3 && c.push("L", b[1], b[2]);
            if (e.stacking && !this.closedStacks)for (d = a.length - 1; d >= 0; d--)g = p(a[d].yBottom, f), d < a.length - 1 && e.step && c.push(a[d + 1].plotX, g), c.push(a[d].plotX, g); else this.closeSegment(c, a, f);
            this.areaPath =
                this.areaPath.concat(c);
            return b
        }, closeSegment: function (a, b, c) {
            a.push("L", b[b.length - 1].plotX, c, "L", b[0].plotX, c)
        }, drawGraph: function () {
            this.areaPath = [];
            P.prototype.drawGraph.apply(this);
            var a = this, b = this.areaPath, c = this.options, d = [["area", this.color, c.fillColor]];
            n(this.zones, function (b, f) {
                d.push(["colorArea" + f, b.color || a.color, b.fillColor || c.fillColor])
            });
            n(d, function (d) {
                var f = d[0], g = a[f];
                g ? g.animate({d: b}) : a[f] = a.chart.renderer.path(b).attr({
                    fill: p(d[2], wa(d[1]).setOpacity(p(c.fillOpacity, 0.75)).get()),
                    zIndex: 0
                }).add(a.group)
            })
        }, drawLegendSymbol: G.drawRectangle
    });
    I.area = xa;
    V.spline = y(Q);
    Ca = ja(P, {
        type: "spline", getPointSpline: function (a, b, c) {
            var d = b.plotX, e = b.plotY, f = a[c - 1], g = a[c + 1], h, i, j, k;
            if (f && g) {
                a = f.plotY;
                j = g.plotX;
                var g = g.plotY, l;
                h = (1.5 * d + f.plotX) / 2.5;
                i = (1.5 * e + a) / 2.5;
                j = (1.5 * d + j) / 2.5;
                k = (1.5 * e + g) / 2.5;
                l = (k - i) * (j - d) / (j - h) + e - k;
                i += l;
                k += l;
                i > a && i > e ? (i = v(a, e), k = 2 * e - i) : i < a && i < e && (i = B(a, e), k = 2 * e - i);
                k > g && k > e ? (k = v(g, e), i = 2 * e - k) : k < g && k < e && (k = B(g, e), i = 2 * e - k);
                b.rightContX = j;
                b.rightContY = k
            }
            c ? (b = ["C", f.rightContX ||
            f.plotX, f.rightContY || f.plotY, h || d, i || e, d, e], f.rightContX = f.rightContY = null) : b = ["M", d, e];
            return b
        }
    });
    I.spline = Ca;
    V.areaspline = y(V.area);
    xa = xa.prototype;
    Ca = ja(Ca, {
        type: "areaspline",
        closedStacks: !0,
        getSegmentPath: xa.getSegmentPath,
        closeSegment: xa.closeSegment,
        drawGraph: xa.drawGraph,
        drawLegendSymbol: G.drawRectangle
    });
    I.areaspline = Ca;
    V.column = y(Q, {
        borderColor: "#FFFFFF",
        borderRadius: 0,
        groupPadding: 0.2,
        marker: null,
        pointPadding: 0.1,
        minPointLength: 0,
        cropThreshold: 50,
        pointRange: null,
        states: {
            hover: {
                brightness: 0.1,
                shadow: !1, halo: !1
            }, select: {color: "#C0C0C0", borderColor: "#000000", shadow: !1}
        },
        dataLabels: {align: null, verticalAlign: null, y: null},
        stickyTracking: !1,
        tooltip: {distance: 6},
        threshold: 0
    });
    Ca = ja(P, {
        type: "column",
        pointAttrToOptions: {stroke: "borderColor", fill: "color", r: "borderRadius"},
        cropShoulder: 0,
        directTouch: !0,
        trackerGroups: ["group", "dataLabelsGroup"],
        negStacks: !0,
        init: function () {
            P.prototype.init.apply(this, arguments);
            var a = this, b = a.chart;
            b.hasRendered && n(b.series, function (b) {
                if (b.type === a.type)b.isDirty = !0
            })
        },
        getColumnMetrics: function () {
            var a = this, b = a.options, c = a.xAxis, d = a.yAxis, e = c.reversed, f, g = {}, h, i = 0;
            b.grouping === !1 ? i = 1 : n(a.chart.series, function (b) {
                var c = b.options, e = b.yAxis;
                if (b.type === a.type && b.visible && d.len === e.len && d.pos === e.pos)c.stacking ? (f = b.stackKey, g[f] === r && (g[f] = i++), h = g[f]) : c.grouping !== !1 && (h = i++), b.columnIndex = h
            });
            var c = B(R(c.transA) * (c.ordinalSlope || b.pointRange || c.closestPointRange || c.tickInterval || 1), c.len), j = c * b.groupPadding, k = (c - 2 * j) / i, l = b.pointWidth, b = s(l) ? (k - l) / 2 : k * b.pointPadding,
                l = p(l, k - 2 * b);
            return a.columnMetrics = {
                width: l,
                offset: b + (j + ((e ? i - (a.columnIndex || 0) : a.columnIndex) || 0) * k - c / 2) * (e ? -1 : 1)
            }
        },
        translate: function () {
            var a = this, b = a.chart, c = a.options, d = a.borderWidth = p(c.borderWidth, a.closestPointRange * a.xAxis.transA < 2 ? 0 : 1), e = a.yAxis, f = a.translatedThreshold = e.getThreshold(c.threshold), g = p(c.minPointLength, 5), h = a.getColumnMetrics(), i = h.width, j = a.barW = v(i, 1 + 2 * d), k = a.pointXOffset = h.offset, l = -(d % 2 ? 0.5 : 0), m = d % 2 ? 0.5 : 1;
            b.renderer.isVML && b.inverted && (m += 1);
            c.pointPadding && (j = ya(j));
            P.prototype.translate.apply(a);
            n(a.points, function (c) {
                var d = p(c.yBottom, f), h = B(v(-999 - d, c.plotY), e.len + 999 + d), n = c.plotX + k, r = j, u = B(h, d), s;
                s = v(h, d) - u;
                R(s) < g && g && (s = g, u = w(R(u - f) > g ? d - g : f - (e.translate(c.y, 0, 1, 0, 1) <= f ? g : 0)));
                c.barX = n;
                c.pointWidth = i;
                c.tooltipPos = b.inverted ? [e.len + e.pos - b.plotLeft - h, a.xAxis.len - n - r / 2] : [n + r / 2, h + e.pos - b.plotTop];
                r = w(n + r) + l;
                n = w(n) + l;
                r -= n;
                d = R(u) < 0.5;
                s = B(w(u + s) + m, 9E4);
                u = w(u) + m;
                s -= u;
                d && (u -= 1, s += 1);
                c.shapeType = "rect";
                c.shapeArgs = {x: n, y: u, width: r, height: s}
            })
        },
        getSymbol: ga,
        drawLegendSymbol: G.drawRectangle,
        drawGraph: ga,
        drawPoints: function () {
            var a = this, b = this.chart, c = a.options, d = b.renderer, e = c.animationLimit || 250, f, g;
            n(a.points, function (h) {
                var i = h.plotY, j = h.graphic;
                if (i !== r && !isNaN(i) && h.y !== null)f = h.shapeArgs, i = s(a.borderWidth) ? {"stroke-width": a.borderWidth} : {}, g = h.pointAttr[h.selected ? "select" : ""] || a.pointAttr[""], j ? (gb(j), j.attr(i)[b.pointCount < e ? "animate" : "attr"](y(f))) : h.graphic = d[h.shapeType](f).attr(i).attr(g).add(a.group).shadow(c.shadow, null, c.stacking && !c.borderRadius); else if (j)h.graphic =
                    j.destroy()
            })
        },
        animate: function (a) {
            var b = this.yAxis, c = this.options, d = this.chart.inverted, e = {};
            if (da)a ? (e.scaleY = 0.001, a = B(b.pos + b.len, v(b.pos, b.toPixels(c.threshold))), d ? e.translateX = a - b.len : e.translateY = a, this.group.attr(e)) : (e.scaleY = 1, e[d ? "translateX" : "translateY"] = b.pos, this.group.animate(e, this.options.animation), this.animate = null)
        },
        remove: function () {
            var a = this, b = a.chart;
            b.hasRendered && n(b.series, function (b) {
                if (b.type === a.type)b.isDirty = !0
            });
            P.prototype.remove.apply(a, arguments)
        }
    });
    I.column =
        Ca;
    V.bar = y(V.column);
    xa = ja(Ca, {type: "bar", inverted: !0});
    I.bar = xa;
    V.scatter = y(Q, {
        lineWidth: 0,
        marker: {enabled: !0},
        tooltip: {
            headerFormat: '<span style="color:{series.color}">●</span> <span style="font-size: 10px;"> {series.name}</span><br/>',
            pointFormat: "x: <b>{point.x}</b><br/>y: <b>{point.y}</b><br/>"
        }
    });
    xa = ja(P, {
        type: "scatter",
        sorted: !1,
        requireSorting: !1,
        noSharedTooltip: !0,
        trackerGroups: ["group", "markerGroup", "dataLabelsGroup"],
        takeOrdinalPosition: !1,
        kdDimensions: 2,
        kdComparer: "distR",
        drawGraph: function () {
            this.options.lineWidth &&
            P.prototype.drawGraph.call(this)
        }
    });
    I.scatter = xa;
    V.pie = y(Q, {
        borderColor: "#FFFFFF",
        borderWidth: 1,
        center: [null, null],
        clip: !1,
        colorByPoint: !0,
        dataLabels: {
            distance: 30, enabled: !0, formatter: function () {
                return this.point.name
            }, x: 0
        },
        ignoreHiddenPoint: !0,
        legendType: "point",
        marker: null,
        size: null,
        showInLegend: !1,
        slicedOffset: 10,
        states: {hover: {brightness: 0.1, shadow: !1}},
        stickyTracking: !1,
        tooltip: {followPointer: !0}
    });
    Q = {
        type: "pie",
        isCartesian: !1,
        pointClass: ja(Ba, {
            init: function () {
                Ba.prototype.init.apply(this, arguments);
                var a = this, b;
                x(a, {visible: a.visible !== !1, name: p(a.name, "Slice")});
                b = function (b) {
                    a.slice(b.type === "select")
                };
                D(a, "select", b);
                D(a, "unselect", b);
                return a
            }, setVisible: function (a) {
                var b = this, c = b.series, d = c.chart, e = !c.isDirty && c.options.ignoreHiddenPoint;
                b.visible = b.options.visible = a = a === r ? !b.visible : a;
                c.options.data[Oa(b, c.data)] = b.options;
                n(["graphic", "dataLabel", "connector", "shadowGroup"], function (c) {
                    if (b[c])b[c][a ? "show" : "hide"](!0)
                });
                b.legendItem && (d.hasRendered && (c.updateTotals(), d.legend.clearItems(),
                e || d.legend.render()), d.legend.colorizeItem(b, a));
                if (e)c.isDirty = !0, d.redraw()
            }, slice: function (a, b, c) {
                var d = this.series;
                Ya(c, d.chart);
                p(b, !0);
                this.sliced = this.options.sliced = a = s(a) ? a : !this.sliced;
                d.options.data[Oa(this, d.data)] = this.options;
                a = a ? this.slicedTranslation : {translateX: 0, translateY: 0};
                this.graphic.animate(a);
                this.shadowGroup && this.shadowGroup.animate(a)
            }, haloPath: function (a) {
                var b = this.shapeArgs, c = this.series.chart;
                return this.sliced || !this.visible ? [] : this.series.chart.renderer.symbols.arc(c.plotLeft +
                    b.x, c.plotTop + b.y, b.r + a, b.r + a, {innerR: this.shapeArgs.r, start: b.start, end: b.end})
            }
        }),
        requireSorting: !1,
        noSharedTooltip: !0,
        trackerGroups: ["group", "dataLabelsGroup"],
        axisTypes: [],
        pointAttrToOptions: {stroke: "borderColor", "stroke-width": "borderWidth", fill: "color"},
        getColor: ga,
        animate: function (a) {
            var b = this, c = b.points, d = b.startAngleRad;
            if (!a)n(c, function (a) {
                var c = a.graphic, a = a.shapeArgs;
                c && (c.attr({r: b.center[3] / 2, start: d, end: d}), c.animate({
                    r: a.r,
                    start: a.start,
                    end: a.end
                }, b.options.animation))
            }), b.animate =
                null
        },
        setData: function (a, b, c, d) {
            P.prototype.setData.call(this, a, !1, c, d);
            this.processData();
            this.generatePoints();
            p(b, !0) && this.chart.redraw(c)
        },
        updateTotals: function () {
            var a, b = 0, c, d, e, f = this.options.ignoreHiddenPoint;
            c = this.points;
            d = c.length;
            for (a = 0; a < d; a++) {
                e = c[a];
                if (e.y < 0)e.y = null;
                b += f && !e.visible ? 0 : e.y
            }
            this.total = b;
            for (a = 0; a < d; a++)e = c[a], e.percentage = b > 0 && (e.visible || !f) ? e.y / b * 100 : 0, e.total = b
        },
        generatePoints: function () {
            P.prototype.generatePoints.call(this);
            this.updateTotals()
        },
        translate: function (a) {
            this.generatePoints();
            var b = 0, c = this.options, d = c.slicedOffset, e = d + c.borderWidth, f, g, h, i = c.startAngle || 0, j = this.startAngleRad = va / 180 * (i - 90), i = (this.endAngleRad = va / 180 * (p(c.endAngle, i + 360) - 90)) - j, k = this.points, l = c.dataLabels.distance, c = c.ignoreHiddenPoint, m, o = k.length, q;
            if (!a)this.center = a = this.getCenter();
            this.getX = function (b, c) {
                h = Y.asin(B((b - a[1]) / (a[2] / 2 + l), 1));
                return a[0] + (c ? -1 : 1) * ba(h) * (a[2] / 2 + l)
            };
            for (m = 0; m < o; m++) {
                q = k[m];
                f = j + b * i;
                if (!c || q.visible)b += q.percentage / 100;
                g = j + b * i;
                q.shapeType = "arc";
                q.shapeArgs = {
                    x: a[0], y: a[1],
                    r: a[2] / 2, innerR: a[3] / 2, start: w(f * 1E3) / 1E3, end: w(g * 1E3) / 1E3
                };
                h = (g + f) / 2;
                h > 1.5 * va ? h -= 2 * va : h < -va / 2 && (h += 2 * va);
                q.slicedTranslation = {translateX: w(ba(h) * d), translateY: w(fa(h) * d)};
                f = ba(h) * a[2] / 2;
                g = fa(h) * a[2] / 2;
                q.tooltipPos = [a[0] + f * 0.7, a[1] + g * 0.7];
                q.half = h < -va / 2 || h > va / 2 ? 1 : 0;
                q.angle = h;
                e = B(e, l / 2);
                q.labelPos = [a[0] + f + ba(h) * l, a[1] + g + fa(h) * l, a[0] + f + ba(h) * e, a[1] + g + fa(h) * e, a[0] + f, a[1] + g, l < 0 ? "center" : q.half ? "right" : "left", h]
            }
        },
        drawGraph: null,
        drawPoints: function () {
            var a = this, b = a.chart.renderer, c, d, e = a.options.shadow,
                f, g;
            if (e && !a.shadowGroup)a.shadowGroup = b.g("shadow").add(a.group);
            n(a.points, function (h) {
                d = h.graphic;
                g = h.shapeArgs;
                f = h.shadowGroup;
                if (e && !f)f = h.shadowGroup = b.g("shadow").add(a.shadowGroup);
                c = h.sliced ? h.slicedTranslation : {translateX: 0, translateY: 0};
                f && f.attr(c);
                d ? d.animate(x(g, c)) : h.graphic = d = b[h.shapeType](g).setRadialReference(a.center).attr(h.pointAttr[h.selected ? "select" : ""]).attr({"stroke-linejoin": "round"}).attr(c).add(a.group).shadow(e, f);
                h.visible !== void 0 && h.setVisible(h.visible)
            })
        },
        searchPoint: ga,
        sortByAngle: function (a, b) {
            a.sort(function (a, d) {
                return a.angle !== void 0 && (d.angle - a.angle) * b
            })
        },
        drawLegendSymbol: G.drawRectangle,
        getCenter: fc.getCenter,
        getSymbol: ga
    };
    Q = ja(P, Q);
    I.pie = Q;
    P.prototype.drawDataLabels = function () {
        var a = this, b = a.options, c = b.cursor, d = b.dataLabels, e = a.points, f, g, h = a.hasRendered || 0, i, j, k = a.chart.renderer;
        if (d.enabled || a._hasPointLabels)a.dlProcessOptions && a.dlProcessOptions(d), j = a.plotGroup("dataLabelsGroup", "data-labels", d.defer ? "hidden" : "visible", d.zIndex || 6), p(d.defer, !0) &&
        (j.attr({opacity: +h}), h || D(a, "afterAnimate", function () {
            a.visible && j.show();
            j[b.animation ? "animate" : "attr"]({opacity: 1}, {duration: 200})
        })), g = d, n(e, function (e) {
            var h, o = e.dataLabel, q, t, n = e.connector, N = !0, u, v = {};
            f = e.dlOptions || e.options && e.options.dataLabels;
            h = p(f && f.enabled, g.enabled);
            if (o && !h)e.dataLabel = o.destroy(); else if (h) {
                d = y(g, f);
                u = d.style;
                h = d.rotation;
                q = e.getLabelConfig();
                i = d.format ? Ma(d.format, q) : d.formatter.call(q, d);
                u.color = p(d.color, u.color, a.color, "black");
                if (o)if (s(i))o.attr({text: i}),
                    N = !1; else {
                    if (e.dataLabel = o = o.destroy(), n)e.connector = n.destroy()
                } else if (s(i)) {
                    o = {
                        fill: d.backgroundColor,
                        stroke: d.borderColor,
                        "stroke-width": d.borderWidth,
                        r: d.borderRadius || 0,
                        rotation: h,
                        padding: d.padding,
                        zIndex: 1
                    };
                    if (u.color === "contrast")v.color = d.inside || d.distance < 0 || b.stacking ? k.getContrast(e.color || a.color) : "#000000";
                    if (c)v.cursor = c;
                    for (t in o)o[t] === r && delete o[t];
                    o = e.dataLabel = k[h ? "text" : "label"](i, 0, -999, d.shape, null, null, d.useHTML).attr(o).css(x(u, v)).add(j).shadow(d.shadow)
                }
                o && a.alignDataLabel(e,
                    o, d, null, N)
            }
        })
    };
    P.prototype.alignDataLabel = function (a, b, c, d, e) {
        var f = this.chart, g = f.inverted, h = p(a.plotX, -999), i = p(a.plotY, -999), j = b.getBBox(), k = f.renderer.fontMetrics(c.style.fontSize).b, l = this.visible && (a.series.forceDL || f.isInsidePlot(h, w(i), g) || d && f.isInsidePlot(h, g ? d.x + 1 : d.y + d.height - 1, g));
        if (l)d = x({
            x: g ? f.plotWidth - i : h,
            y: w(g ? f.plotHeight - h : i),
            width: 0,
            height: 0
        }, d), x(c, {
            width: j.width,
            height: j.height
        }), c.rotation ? (a = f.renderer.rotCorr(k, c.rotation), b[e ? "attr" : "animate"]({
            x: d.x + c.x + d.width / 2 + a.x,
            y: d.y + c.y + d.height / 2
        }).attr({align: c.align})) : (b.align(c, null, d), g = b.alignAttr, p(c.overflow, "justify") === "justify" ? this.justifyDataLabel(b, c, g, j, d, e) : p(c.crop, !0) && (l = f.isInsidePlot(g.x, g.y) && f.isInsidePlot(g.x + j.width, g.y + j.height)), c.shape && b.attr({
            anchorX: a.plotX,
            anchorY: a.plotY
        }));
        if (!l)b.attr({y: -999}), b.placed = !1
    };
    P.prototype.justifyDataLabel = function (a, b, c, d, e, f) {
        var g = this.chart, h = b.align, i = b.verticalAlign, j, k, l = a.box ? 0 : a.padding || 0;
        j = c.x + l;
        if (j < 0)h === "right" ? b.align = "left" : b.x = -j, k = !0;
        j =
            c.x + d.width - l;
        if (j > g.plotWidth)h === "left" ? b.align = "right" : b.x = g.plotWidth - j, k = !0;
        j = c.y + l;
        if (j < 0)i === "bottom" ? b.verticalAlign = "top" : b.y = -j, k = !0;
        j = c.y + d.height - l;
        if (j > g.plotHeight)i === "top" ? b.verticalAlign = "bottom" : b.y = g.plotHeight - j, k = !0;
        if (k)a.placed = !f, a.align(b, null, e)
    };
    if (I.pie)I.pie.prototype.drawDataLabels = function () {
        var a = this, b = a.data, c, d = a.chart, e = a.options.dataLabels, f = p(e.connectorPadding, 10), g = p(e.connectorWidth, 1), h = d.plotWidth, i = d.plotHeight, j, k, l = p(e.softConnector, !0), m = e.distance, o =
            a.center, q = o[2] / 2, t = o[1], s = m > 0, r, u, A, x = [[], []], y, z, D, E, L, C = [0, 0, 0, 0], I = function (a, b) {
            return b.y - a.y
        };
        if (a.visible && (e.enabled || a._hasPointLabels)) {
            P.prototype.drawDataLabels.apply(a);
            n(b, function (a) {
                a.dataLabel && a.visible && x[a.half].push(a)
            });
            for (E = 2; E--;) {
                var G = [], M = [], H = x[E], K = H.length, F;
                if (K) {
                    a.sortByAngle(H, E - 0.5);
                    for (L = b = 0; !b && H[L];)b = H[L] && H[L].dataLabel && (H[L].dataLabel.getBBox().height || 21), L++;
                    if (m > 0) {
                        u = B(t + q + m, d.plotHeight);
                        for (L = v(0, t - q - m); L <= u; L += b)G.push(L);
                        u = G.length;
                        if (K > u) {
                            c = [].concat(H);
                            c.sort(I);
                            for (L = K; L--;)c[L].rank = L;
                            for (L = K; L--;)H[L].rank >= u && H.splice(L, 1);
                            K = H.length
                        }
                        for (L = 0; L < K; L++) {
                            c = H[L];
                            A = c.labelPos;
                            c = 9999;
                            var Q, O;
                            for (O = 0; O < u; O++)Q = R(G[O] - A[1]), Q < c && (c = Q, F = O);
                            if (F < L && G[L] !== null)F = L; else for (u < K - L + F && G[L] !== null && (F = u - K + L); G[F] === null;)F++;
                            M.push({i: F, y: G[F]});
                            G[F] = null
                        }
                        M.sort(I)
                    }
                    for (L = 0; L < K; L++) {
                        c = H[L];
                        A = c.labelPos;
                        r = c.dataLabel;
                        D = c.visible === !1 ? "hidden" : "visible";
                        c = A[1];
                        if (m > 0) {
                            if (u = M.pop(), F = u.i, z = u.y, c > z && G[F + 1] !== null || c < z && G[F - 1] !== null)z = B(v(0, c), d.plotHeight)
                        } else z =
                            c;
                        y = e.justify ? o[0] + (E ? -1 : 1) * (q + m) : a.getX(z === t - q - m || z === t + q + m ? c : z, E);
                        r._attr = {visibility: D, align: A[6]};
                        r._pos = {x: y + e.x + ({left: f, right: -f}[A[6]] || 0), y: z + e.y - 10};
                        r.connX = y;
                        r.connY = z;
                        if (this.options.size === null)u = r.width, y - u < f ? C[3] = v(w(u - y + f), C[3]) : y + u > h - f && (C[1] = v(w(y + u - h + f), C[1])), z - b / 2 < 0 ? C[0] = v(w(-z + b / 2), C[0]) : z + b / 2 > i && (C[2] = v(w(z + b / 2 - i), C[2]))
                    }
                }
            }
            if (Ea(C) === 0 || this.verifyDataLabelOverflow(C))this.placeDataLabels(), s && g && n(this.points, function (b) {
                j = b.connector;
                A = b.labelPos;
                if ((r = b.dataLabel) && r._pos)D =
                    r._attr.visibility, y = r.connX, z = r.connY, k = l ? ["M", y + (A[6] === "left" ? 5 : -5), z, "C", y, z, 2 * A[2] - A[4], 2 * A[3] - A[5], A[2], A[3], "L", A[4], A[5]] : ["M", y + (A[6] === "left" ? 5 : -5), z, "L", A[2], A[3], "L", A[4], A[5]], j ? (j.animate({d: k}), j.attr("visibility", D)) : b.connector = j = a.chart.renderer.path(k).attr({
                    "stroke-width": g,
                    stroke: e.connectorColor || b.color || "#606060",
                    visibility: D
                }).add(a.dataLabelsGroup); else if (j)b.connector = j.destroy()
            })
        }
    }, I.pie.prototype.placeDataLabels = function () {
        n(this.points, function (a) {
            var a = a.dataLabel,
                b;
            if (a)(b = a._pos) ? (a.attr(a._attr), a[a.moved ? "animate" : "attr"](b), a.moved = !0) : a && a.attr({y: -999})
        })
    }, I.pie.prototype.alignDataLabel = ga, I.pie.prototype.verifyDataLabelOverflow = function (a) {
        var b = this.center, c = this.options, d = c.center, e = c = c.minSize || 80, f;
        d[0] !== null ? e = v(b[2] - v(a[1], a[3]), c) : (e = v(b[2] - a[1] - a[3], c), b[0] += (a[3] - a[1]) / 2);
        d[1] !== null ? e = v(B(e, b[2] - v(a[0], a[2])), c) : (e = v(B(e, b[2] - a[0] - a[2]), c), b[1] += (a[0] - a[2]) / 2);
        e < b[2] ? (b[2] = e, this.translate(b), n(this.points, function (a) {
            if (a.dataLabel)a.dataLabel._pos =
                null
        }), this.drawDataLabels && this.drawDataLabels()) : f = !0;
        return f
    };
    if (I.column)I.column.prototype.alignDataLabel = function (a, b, c, d, e) {
        var f = this.chart.inverted, g = a.series, h = a.dlBox || a.shapeArgs, i = a.below || a.plotY > p(this.translatedThreshold, g.yAxis.len), j = p(c.inside, !!this.options.stacking);
        if (h && (d = y(h), f && (d = {
                x: g.yAxis.len - d.y - d.height,
                y: g.xAxis.len - d.x - d.width,
                width: d.height,
                height: d.width
            }), !j))f ? (d.x += i ? 0 : d.width, d.width = 0) : (d.y += i ? d.height : 0, d.height = 0);
        c.align = p(c.align, !f || j ? "center" : i ? "right" :
            "left");
        c.verticalAlign = p(c.verticalAlign, f || j ? "middle" : i ? "top" : "bottom");
        P.prototype.alignDataLabel.call(this, a, b, c, d, e)
    };
    (function (a) {
        var b = a.Chart, c = a.each, d = HighchartsAdapter.addEvent;
        b.prototype.callbacks.push(function (a) {
            function b() {
                var d = [];
                c(a.series, function (a) {
                    var b = a.options.dataLabels;
                    (b.enabled || a._hasPointLabels) && !b.allowOverlap && a.visible && c(a.points, function (a) {
                        if (a.dataLabel)a.dataLabel.labelrank = a.labelrank, d.push(a.dataLabel)
                    })
                });
                a.hideOverlappingLabels(d)
            }

            b();
            d(a, "redraw", b)
        });
        b.prototype.hideOverlappingLabels = function (a) {
            var b = a.length, c, d, i, j;
            for (d = 0; d < b; d++)if (c = a[d])c.oldOpacity = c.opacity, c.newOpacity = 1;
            for (d = 0; d < b; d++) {
                i = a[d];
                for (c = d + 1; c < b; ++c)if (j = a[c], i && j && i.placed && j.placed && i.newOpacity !== 0 && j.newOpacity !== 0 && !(j.alignAttr.x > i.alignAttr.x + i.width || j.alignAttr.x + j.width < i.alignAttr.x || j.alignAttr.y > i.alignAttr.y + i.height || j.alignAttr.y + j.height < i.alignAttr.y))(i.labelrank < j.labelrank ? i : j).newOpacity = 0
            }
            for (d = 0; d < b; d++)if (c = a[d]) {
                if (c.oldOpacity !== c.newOpacity &&
                    c.placed)c.alignAttr.opacity = c.newOpacity, c[c.isOld && c.newOpacity ? "animate" : "attr"](c.alignAttr);
                c.isOld = !0
            }
        }
    })(z);
    var ib = z.TrackerMixin = {
        drawTrackerPoint: function () {
            var a = this, b = a.chart, c = b.pointer, d = a.options.cursor, e = d && {cursor: d}, f = function (c) {
                var d = c.target, e;
                if (b.hoverSeries !== a)a.onMouseOver();
                for (; d && !e;)e = d.point, d = d.parentNode;
                if (e !== r && e !== b.hoverPoint)e.onMouseOver(c)
            };
            n(a.points, function (a) {
                if (a.graphic)a.graphic.element.point = a;
                if (a.dataLabel)a.dataLabel.element.point = a
            });
            if (!a._hasTracking)n(a.trackerGroups,
                function (b) {
                    if (a[b] && (a[b].addClass("highcharts-tracker").on("mouseover", f).on("mouseout", function (a) {
                            c.onTrackerMouseOut(a)
                        }).css(e), $a))a[b].on("touchstart", f)
                }), a._hasTracking = !0
        }, drawTrackerGraph: function () {
            var a = this, b = a.options, c = b.trackByArea, d = [].concat(c ? a.areaPath : a.graphPath), e = d.length, f = a.chart, g = f.pointer, h = f.renderer, i = f.options.tooltip.snap, j = a.tracker, k = b.cursor, l = k && {cursor: k}, k = a.singlePoints, m, o = function () {
                if (f.hoverSeries !== a)a.onMouseOver()
            }, q = "rgba(192,192,192," + (da ? 1.0E-4 :
                    0.002) + ")";
            if (e && !c)for (m = e + 1; m--;)d[m] === "M" && d.splice(m + 1, 0, d[m + 1] - i, d[m + 2], "L"), (m && d[m] === "M" || m === e) && d.splice(m, 0, "L", d[m - 2] + i, d[m - 1]);
            for (m = 0; m < k.length; m++)e = k[m], d.push("M", e.plotX - i, e.plotY, "L", e.plotX + i, e.plotY);
            j ? j.attr({d: d}) : (a.tracker = h.path(d).attr({
                "stroke-linejoin": "round",
                visibility: a.visible ? "visible" : "hidden",
                stroke: q,
                fill: c ? q : Z,
                "stroke-width": b.lineWidth + (c ? 0 : 2 * i),
                zIndex: 2
            }).add(a.group), n([a.tracker, a.markerGroup], function (a) {
                a.addClass("highcharts-tracker").on("mouseover",
                    o).on("mouseout", function (a) {
                    g.onTrackerMouseOut(a)
                }).css(l);
                if ($a)a.on("touchstart", o)
            }))
        }
    };
    if (I.column)Ca.prototype.drawTracker = ib.drawTrackerPoint;
    if (I.pie)I.pie.prototype.drawTracker = ib.drawTrackerPoint;
    if (I.scatter)xa.prototype.drawTracker = ib.drawTrackerPoint;
    x(rb.prototype, {
        setItemEvents: function (a, b, c, d, e) {
            var f = this;
            (c ? b : a.legendGroup).on("mouseover", function () {
                a.setState("hover");
                b.css(f.options.itemHoverStyle)
            }).on("mouseout", function () {
                b.css(a.visible ? d : e);
                a.setState()
            }).on("click", function (b) {
                var c =
                    function () {
                        a.setVisible()
                    }, b = {browserEvent: b};
                a.firePointEvent ? a.firePointEvent("legendItemClick", b, c) : K(a, "legendItemClick", b, c)
            })
        }, createCheckboxForItem: function (a) {
            a.checkbox = aa("input", {
                type: "checkbox",
                checked: a.selected,
                defaultChecked: a.selected
            }, this.options.itemCheckboxStyle, this.chart.container);
            D(a.checkbox, "click", function (b) {
                K(a.series || a, "checkboxClick", {checked: b.target.checked, item: a}, function () {
                    a.select()
                })
            })
        }
    });
    F.legend.itemStyle.cursor = "pointer";
    x(Pa.prototype, {
        showResetZoom: function () {
            var a =
                this, b = F.lang, c = a.options.chart.resetZoomButton, d = c.theme, e = d.states, f = c.relativeTo === "chart" ? null : "plotBox";
            this.resetZoomButton = a.renderer.button(b.resetZoom, null, null, function () {
                a.zoomOut()
            }, d, e && e.hover).attr({align: c.position.align, title: b.resetZoomTitle}).add().align(c.position, !1, f)
        }, zoomOut: function () {
            var a = this;
            K(a, "selection", {resetSelection: !0}, function () {
                a.zoom()
            })
        }, zoom: function (a) {
            var b, c = this.pointer, d = !1, e;
            !a || a.resetSelection ? n(this.axes, function (a) {
                b = a.zoom()
            }) : n(a.xAxis.concat(a.yAxis),
                function (a) {
                    var e = a.axis, h = e.isXAxis;
                    if (c[h ? "zoomX" : "zoomY"] || c[h ? "pinchX" : "pinchY"])b = e.zoom(a.min, a.max), e.displayBtn && (d = !0)
                });
            e = this.resetZoomButton;
            if (d && !e)this.showResetZoom(); else if (!d && ia(e))this.resetZoomButton = e.destroy();
            b && this.redraw(p(this.options.chart.animation, a && a.animation, this.pointCount < 100))
        }, pan: function (a, b) {
            var c = this, d = c.hoverPoints, e;
            d && n(d, function (a) {
                a.setState()
            });
            n(b === "xy" ? [1, 0] : [1], function (b) {
                var d = a[b ? "chartX" : "chartY"], h = c[b ? "xAxis" : "yAxis"][0], i = c[b ? "mouseDownX" :
                    "mouseDownY"], j = (h.pointRange || 0) / 2, k = h.getExtremes(), l = h.toValue(i - d, !0) + j, j = h.toValue(i + c[b ? "plotWidth" : "plotHeight"] - d, !0) - j, i = i > d;
                if (h.series.length && (i || l > B(k.dataMin, k.min)) && (!i || j < v(k.dataMax, k.max)))h.setExtremes(l, j, !1, !1, {trigger: "pan"}), e = !0;
                c[b ? "mouseDownX" : "mouseDownY"] = d
            });
            e && c.redraw(!1);
            M(c.container, {cursor: "move"})
        }
    });
    x(Ba.prototype, {
        select: function (a, b) {
            var c = this, d = c.series, e = d.chart, a = p(a, !c.selected);
            c.firePointEvent(a ? "select" : "unselect", {accumulate: b}, function () {
                c.selected =
                    c.options.selected = a;
                d.options.data[Oa(c, d.data)] = c.options;
                c.setState(a && "select");
                b || n(e.getSelectedPoints(), function (a) {
                    if (a.selected && a !== c)a.selected = a.options.selected = !1, d.options.data[Oa(a, d.data)] = a.options, a.setState(""), a.firePointEvent("unselect")
                })
            })
        }, onMouseOver: function (a) {
            var b = this.series, c = b.chart, d = c.tooltip, e = c.hoverPoint;
            if (e && e !== this)e.onMouseOut();
            this.firePointEvent("mouseOver");
            d && (!d.shared || b.noSharedTooltip) && d.refresh(this, a);
            this.setState("hover");
            c.hoverPoint = this
        },
        onMouseOut: function () {
            var a = this.series.chart, b = a.hoverPoints;
            this.firePointEvent("mouseOut");
            if (!b || Oa(this, b) === -1)this.setState(), a.hoverPoint = null
        }, importEvents: function () {
            if (!this.hasImportedEvents) {
                var a = y(this.series.options.point, this.options).events, b;
                this.events = a;
                for (b in a)D(this, b, a[b]);
                this.hasImportedEvents = !0
            }
        }, setState: function (a, b) {
            var c = this.plotX, d = this.plotY, e = this.series, f = e.options.states, g = V[e.type].marker && e.options.marker, h = g && !g.enabled, i = g && g.states[a], j = i && i.enabled === !1, k = e.stateMarkerGraphic, l = this.marker || {}, m = e.chart, o = e.halo, q, a = a || "";
            q = this.pointAttr[a] || e.pointAttr[a];
            if (!(a === this.state && !b || this.selected && a !== "select" || f[a] && f[a].enabled === !1 || a && (j || h && i.enabled === !1) || a && l.states && l.states[a] && l.states[a].enabled === !1)) {
                if (this.graphic)g = g && this.graphic.symbolName && q.r, this.graphic.attr(y(q, g ? {
                    x: c - g,
                    y: d - g,
                    width: 2 * g,
                    height: 2 * g
                } : {})), k && k.hide(); else {
                    if (a && i)if (g = i.radius, l = l.symbol || e.symbol, k && k.currentSymbol !== l && (k = k.destroy()), k)k[b ? "animate" : "attr"]({
                        x: c -
                        g, y: d - g
                    }); else if (l)e.stateMarkerGraphic = k = m.renderer.symbol(l, c - g, d - g, 2 * g, 2 * g).attr(q).add(e.markerGroup), k.currentSymbol = l;
                    if (k)k[a && m.isInsidePlot(c, d, m.inverted) ? "show" : "hide"]()
                }
                if ((c = f[a] && f[a].halo) && c.size) {
                    if (!o)e.halo = o = m.renderer.path().add(m.seriesGroup);
                    o.attr(x({fill: wa(this.color || e.color).setOpacity(c.opacity).get()}, c.attributes))[b ? "animate" : "attr"]({d: this.haloPath(c.size)})
                } else o && o.attr({d: []});
                this.state = a
            }
        }, haloPath: function (a) {
            var b = this.series, c = b.chart, d = b.getPlotBox(),
                e = c.inverted;
            return c.renderer.symbols.circle(d.translateX + (e ? b.yAxis.len - this.plotY : this.plotX) - a, d.translateY + (e ? b.xAxis.len - this.plotX : this.plotY) - a, a * 2, a * 2)
        }
    });
    x(P.prototype, {
        onMouseOver: function () {
            var a = this.chart, b = a.hoverSeries;
            if (b && b !== this)b.onMouseOut();
            this.options.events.mouseOver && K(this, "mouseOver");
            this.setState("hover");
            a.hoverSeries = this
        }, onMouseOut: function () {
            var a = this.options, b = this.chart, c = b.tooltip, d = b.hoverPoint;
            if (d)d.onMouseOut();
            this && a.events.mouseOut && K(this, "mouseOut");
            c && !a.stickyTracking && (!c.shared || this.noSharedTooltip) && c.hide();
            this.setState();
            b.hoverSeries = null
        }, setState: function (a) {
            var b = this.options, c = this.graph, d = this.graphNeg, e = b.states, b = b.lineWidth, a = a || "";
            if (this.state !== a)this.state = a, e[a] && e[a].enabled === !1 || (a && (b = (e[a].lineWidth || b) + (e[a].lineWidthPlus || 0)), c && !c.dashstyle && (a = {"stroke-width": b}, c.attr(a), d && d.attr(a)))
        }, setVisible: function (a, b) {
            var c = this, d = c.chart, e = c.legendItem, f, g = d.options.chart.ignoreHiddenSeries, h = c.visible;
            f = (c.visible =
                a = c.userOptions.visible = a === r ? !h : a) ? "show" : "hide";
            n(["group", "dataLabelsGroup", "markerGroup", "tracker"], function (a) {
                if (c[a])c[a][f]()
            });
            if (d.hoverSeries === c || (d.hoverPoint && d.hoverPoint.series) === c)c.onMouseOut();
            e && d.legend.colorizeItem(c, a);
            c.isDirty = !0;
            c.options.stacking && n(d.series, function (a) {
                if (a.options.stacking && a.visible)a.isDirty = !0
            });
            n(c.linkedSeries, function (b) {
                b.setVisible(a, !1)
            });
            if (g)d.isDirtyBox = !0;
            b !== !1 && d.redraw();
            K(c, f)
        }, show: function () {
            this.setVisible(!0)
        }, hide: function () {
            this.setVisible(!1)
        },
        select: function (a) {
            this.selected = a = a === r ? !this.selected : a;
            if (this.checkbox)this.checkbox.checked = a;
            K(this, a ? "select" : "unselect")
        }, drawTracker: ib.drawTrackerGraph
    });
    S(P.prototype, "init", function (a) {
        var b;
        a.apply(this, Array.prototype.slice.call(arguments, 1));
        (b = this.xAxis) && b.options.ordinal && D(this, "updatedData", function () {
            delete b.ordinalIndex
        })
    });
    S(O.prototype, "getTimeTicks", function (a, b, c, d, e, f, g, h) {
        var i = 0, j = 0, k, l = {}, m, o, q, t = [], n = -Number.MAX_VALUE, p = this.options.tickPixelInterval;
        if (!this.options.ordinal && !this.options.breaks || !f || f.length < 3 || c === r)return a.call(this, b, c, d, e);
        for (o = f.length; j < o; j++) {
            q = j && f[j - 1] > d;
            f[j] < c && (i = j);
            if (j === o - 1 || f[j + 1] - f[j] > g * 5 || q) {
                if (f[j] > n) {
                    for (k = a.call(this, b, f[i], f[j], e); k.length && k[0] <= n;)k.shift();
                    k.length && (n = k[k.length - 1]);
                    t = t.concat(k)
                }
                i = j + 1
            }
            if (q)break
        }
        a = k.info;
        if (h && a.unitRange <= H.hour) {
            j = t.length - 1;
            for (i = 1; i < j; i++)ka("%d", t[i]) !== ka("%d", t[i - 1]) && (l[t[i]] = "day", m = !0);
            m && (l[t[0]] = "day");
            a.higherRanks = l
        }
        t.info = a;
        if (h && s(p)) {
            var h = a = t.length, j = [], u;
            for (m = []; h--;)i =
                this.translate(t[h]), u && (m[h] = u - i), j[h] = u = i;
            m.sort();
            m = m[X(m.length / 2)];
            m < p * 0.6 && (m = null);
            h = t[a - 1] > d ? a - 1 : a;
            for (u = void 0; h--;)i = j[h], d = u - i, u && d < p * 0.8 && (m === null || d < m * 0.8) ? (l[t[h]] && !l[t[h + 1]] ? (d = h + 1, u = i) : d = h, t.splice(d, 1)) : u = i
        }
        return t
    });
    x(O.prototype, {
        beforeSetTickPositions: function () {
            var a = this, b, c = [], d = !1, e, f = a.getExtremes(), g = f.min, f = f.max, h;
            if (a.options.ordinal || a.options.breaks) {
                n(a.series, function (d, e) {
                    if (d.visible !== !1 && (d.takeOrdinalPosition !== !1 || a.options.breaks))if (c = c.concat(d.processedXData),
                            b = c.length, c.sort(function (a, b) {
                            return a - b
                        }), b)for (e = b - 1; e--;)c[e] === c[e + 1] && c.splice(e, 1)
                });
                b = c.length;
                if (b > 2) {
                    e = c[1] - c[0];
                    for (h = b - 1; h-- && !d;)c[h + 1] - c[h] !== e && (d = !0);
                    if (!a.options.keepOrdinalPadding && (c[0] - g > e || f - c[c.length - 1] > e))d = !0
                }
                d ? (a.ordinalPositions = c, e = a.val2lin(v(g, c[0]), !0), h = v(a.val2lin(B(f, c[c.length - 1]), !0), 1), a.ordinalSlope = f = (f - g) / (h - e), a.ordinalOffset = g - e * f) : a.ordinalPositions = a.ordinalSlope = a.ordinalOffset = r;
                if (a.options.ordinal)a.doPostTranslate = d
            }
            a.groupIntervalFactor = null
        }, val2lin: function (a,
                              b) {
            var c = this.ordinalPositions;
            if (c) {
                var d = c.length, e, f;
                for (e = d; e--;)if (c[e] === a) {
                    f = e;
                    break
                }
                for (e = d - 1; e--;)if (a > c[e] || e === 0) {
                    c = (a - c[e]) / (c[e + 1] - c[e]);
                    f = e + c;
                    break
                }
                return b ? f : this.ordinalSlope * (f || 0) + this.ordinalOffset
            } else return a
        }, lin2val: function (a, b) {
            var c = this.ordinalPositions;
            if (c) {
                var d = this.ordinalSlope, e = this.ordinalOffset, f = c.length - 1, g, h;
                if (b)a < 0 ? a = c[0] : a > f ? a = c[f] : (f = X(a), h = a - f); else for (; f--;)if (g = d * f + e, a >= g) {
                    d = d * (f + 1) + e;
                    h = (a - g) / (d - g);
                    break
                }
                return h !== r && c[f] !== r ? c[f] + (h ? h * (c[f + 1] - c[f]) :
                    0) : a
            } else return a
        }, getExtendedPositions: function () {
            var a = this.chart, b = this.series[0].currentDataGrouping, c = this.ordinalIndex, d = b ? b.count + b.unitName : "raw", e = this.getExtremes(), f, g;
            if (!c)c = this.ordinalIndex = {};
            if (!c[d])f = {
                series: [], getExtremes: function () {
                    return {min: e.dataMin, max: e.dataMax}
                }, options: {ordinal: !0}, val2lin: O.prototype.val2lin
            }, n(this.series, function (c) {
                g = {xAxis: f, xData: c.xData, chart: a, destroyGroupedData: ga};
                g.options = {
                    dataGrouping: b ? {
                        enabled: !0, forced: !0, approximation: "open", units: [[b.unitName,
                            [b.count]]]
                    } : {enabled: !1}
                };
                c.processData.apply(g);
                f.series.push(g)
            }), this.beforeSetTickPositions.apply(f), c[d] = f.ordinalPositions;
            return c[d]
        }, getGroupIntervalFactor: function (a, b, c) {
            var d = 0, c = c.processedXData, e = c.length, f = [], g = this.groupIntervalFactor;
            if (!g) {
                for (; d < e - 1; d++)f[d] = c[d + 1] - c[d];
                f.sort(function (a, b) {
                    return a - b
                });
                d = f[X(e / 2)];
                a = v(a, c[0]);
                b = B(b, c[e - 1]);
                this.groupIntervalFactor = g = e * d / (b - a)
            }
            return g
        }, postProcessTickInterval: function (a) {
            var b = this.ordinalSlope;
            return b ? this.options.breaks ? this.closestPointRange :
            a / (b / this.closestPointRange) : a
        }
    });
    S(Pa.prototype, "pan", function (a, b) {
        var c = this.xAxis[0], d = b.chartX, e = !1;
        if (c.options.ordinal && c.series.length) {
            var f = this.mouseDownX, g = c.getExtremes(), h = g.dataMax, i = g.min, j = g.max, k = this.hoverPoints, l = c.closestPointRange, f = (f - d) / (c.translationSlope * (c.ordinalSlope || l)), m = {ordinalPositions: c.getExtendedPositions()}, l = c.lin2val, o = c.val2lin, q;
            if (m.ordinalPositions) {
                if (R(f) > 1)k && n(k, function (a) {
                    a.setState()
                }), f < 0 ? (k = m, q = c.ordinalPositions ? c : m) : (k = c.ordinalPositions ? c : m,
                    q = m), m = q.ordinalPositions, h > m[m.length - 1] && m.push(h), this.fixedRange = j - i, f = c.toFixedRange(null, null, l.apply(k, [o.apply(k, [i, !0]) + f, !0]), l.apply(q, [o.apply(q, [j, !0]) + f, !0])), f.min >= B(g.dataMin, i) && f.max <= v(h, j) && c.setExtremes(f.min, f.max, !0, !1, {trigger: "pan"}), this.mouseDownX = d, M(this.container, {cursor: "move"})
            } else e = !0
        } else e = !0;
        e && a.apply(this, Array.prototype.slice.call(arguments, 1))
    });
    S(P.prototype, "getSegments", function (a) {
        var b, c = this.options.gapSize, d = this.xAxis;
        a.apply(this, Array.prototype.slice.call(arguments,
            1));
        if (c)b = this.segments, n(b, function (a, f) {
            for (var g = a.length - 1; g--;)a[g + 1].x - a[g].x > d.closestPointRange * c && b.splice(f + 1, 0, a.splice(g + 1, a.length - g))
        })
    });
    (function (a) {
        function b() {
            return Array.prototype.slice.call(arguments, 1)
        }

        var c = a.pick, d = a.wrap, e = a.extend, f = HighchartsAdapter.fireEvent, g = a.Axis, h = a.Series;
        e(g.prototype, {
            isInBreak: function (a, b) {
                var c = a.repeat || Infinity, d = a.from, e = a.to - a.from, c = b >= d ? (b - d) % c : c - (d - b) % c;
                return a.inclusive ? c <= e : c < e && c !== 0
            }, isInAnyBreak: function (a, b) {
                if (!this.options.breaks)return !1;
                for (var d = this.options.breaks, e = d.length, f = !1, g = !1; e--;)this.isInBreak(d[e], a) && (f = !0, g || (g = c(d[e].showPoints, this.isXAxis ? !1 : !0)));
                return f && b ? f && !g : f
            }
        });
        d(g.prototype, "setTickPositions", function (a) {
            a.apply(this, Array.prototype.slice.call(arguments, 1));
            if (this.options.breaks) {
                var b = this.tickPositions, c = this.tickPositions.info, d = [], e;
                if (!(c && c.totalRange >= this.closestPointRange)) {
                    for (e = 0; e < b.length; e++)this.isInAnyBreak(b[e]) || d.push(b[e]);
                    this.tickPositions = d;
                    this.tickPositions.info = c
                }
            }
        });
        d(g.prototype,
            "init", function (a, b, c) {
                if (c.breaks && c.breaks.length)c.ordinal = !1;
                a.call(this, b, c);
                if (this.options.breaks) {
                    var d = this;
                    d.doPostTranslate = !0;
                    this.val2lin = function (a) {
                        var b = a, c, e;
                        for (e = 0; e < d.breakArray.length; e++)if (c = d.breakArray[e], c.to <= a)b -= c.len; else if (c.from >= a)break; else if (d.isInBreak(c, a)) {
                            b -= a - c.from;
                            break
                        }
                        return b
                    };
                    this.lin2val = function (a) {
                        var b, c;
                        for (c = 0; c < d.breakArray.length; c++)if (b = d.breakArray[c], b.from >= a)break; else b.to < a ? a += b.to - b.from : d.isInBreak(b, a) && (a += b.to - b.from);
                        return a
                    };
                    this.setExtremes = function (a, b, c, d, e) {
                        for (; this.isInAnyBreak(a);)a -= this.closestPointRange;
                        for (; this.isInAnyBreak(b);)b -= this.closestPointRange;
                        g.prototype.setExtremes.call(this, a, b, c, d, e)
                    };
                    this.setAxisTranslation = function (a) {
                        g.prototype.setAxisTranslation.call(this, a);
                        var b = d.options.breaks, a = [], c = [], e = 0, h, i, j = d.userMin || d.min, k = d.userMax || d.max, n, p;
                        for (p in b)i = b[p], d.isInBreak(i, j) && (j += i.to % i.repeat - j % i.repeat), d.isInBreak(i, k) && (k -= k % i.repeat - i.from % i.repeat);
                        for (p in b) {
                            i = b[p];
                            n = i.from;
                            for (h =
                                     i.repeat || Infinity; n - h > j;)n -= h;
                            for (; n < j;)n += h;
                            for (; n < k; n += h)a.push({value: n, move: "in"}), a.push({
                                value: n + (i.to - i.from),
                                move: "out",
                                size: i.breakSize
                            })
                        }
                        a.sort(function (a, b) {
                            return a.value === b.value ? (a.move === "in" ? 0 : 1) - (b.move === "in" ? 0 : 1) : a.value - b.value
                        });
                        b = 0;
                        n = j;
                        for (p in a) {
                            i = a[p];
                            b += i.move === "in" ? 1 : -1;
                            if (b === 1 && i.move === "in")n = i.value;
                            b === 0 && (c.push({
                                from: n,
                                to: i.value,
                                len: i.value - n - (i.size || 0)
                            }), e += i.value - n - (i.size || 0))
                        }
                        d.breakArray = c;
                        f(d, "afterBreaks");
                        d.transA *= (k - d.min) / (k - j - e);
                        d.min = j;
                        d.max = k
                    }
                }
            });
        d(h.prototype, "generatePoints", function (a) {
            a.apply(this, b(arguments));
            var c = this.xAxis, d = this.yAxis, e = this.points, f, g = e.length;
            if (c && d && (c.options.breaks || d.options.breaks))for (; g--;)if (f = e[g], c.isInAnyBreak(f.x, !0) || d.isInAnyBreak(f.y, !0))e.splice(g, 1), this.data[g].destroyElements()
        });
        d(a.seriesTypes.column.prototype, "drawPoints", function (a) {
            a.apply(this);
            var a = this.points, b = this.yAxis, c = b.breakArray || [], d, e, g, h, n;
            for (g = 0; g < a.length; g++) {
                d = a[g];
                n = d.stackY || d.y;
                for (h = 0; h < c.length; h++)if (e = c[h], n <
                    e.from)break; else n > e.to ? f(b, "pointBreak", {
                    point: d,
                    brk: e
                }) : f(b, "pointInBreak", {point: d, brk: e})
            }
        })
    })(z);
    var ca = P.prototype, Q = Kb.prototype, gc = ca.processData, hc = ca.generatePoints, ic = ca.destroy, jc = Q.tooltipFooterHeaderFormatter, kc = {
            approximation: "average", groupPixelWidth: 2, dateTimeLabelFormats: {
                millisecond: ["%A, %b %e, %H:%M:%S.%L", "%A, %b %e, %H:%M:%S.%L", "-%H:%M:%S.%L"],
                second: ["%A, %b %e, %H:%M:%S", "%A, %b %e, %H:%M:%S", "-%H:%M:%S"],
                minute: ["%A, %b %e, %H:%M", "%A, %b %e, %H:%M", "-%H:%M"],
                hour: ["%A, %b %e, %H:%M",
                    "%A, %b %e, %H:%M", "-%H:%M"],
                day: ["%A, %b %e, %Y", "%A, %b %e", "-%A, %b %e, %Y"],
                week: ["Week from %A, %b %e, %Y", "%A, %b %e", "-%A, %b %e, %Y"],
                month: ["%B %Y", "%B", "-%B %Y"],
                year: ["%Y", "%Y", "-%Y"]
            }
        }, Ub = {
            line: {},
            spline: {},
            area: {},
            areaspline: {},
            column: {approximation: "sum", groupPixelWidth: 10},
            arearange: {approximation: "range"},
            areasplinerange: {approximation: "range"},
            columnrange: {approximation: "range", groupPixelWidth: 10},
            candlestick: {approximation: "ohlc", groupPixelWidth: 10},
            ohlc: {approximation: "ohlc", groupPixelWidth: 5}
        },
        Vb = [["millisecond", [1, 2, 5, 10, 20, 25, 50, 100, 200, 500]], ["second", [1, 2, 5, 10, 15, 30]], ["minute", [1, 2, 5, 10, 15, 30]], ["hour", [1, 2, 3, 4, 6, 8, 12]], ["day", [1]], ["week", [1]], ["month", [1, 3, 6]], ["year", null]], Qa = {
            sum: function (a) {
                var b = a.length, c;
                if (!b && a.hasNulls)c = null; else if (b)for (c = 0; b--;)c += a[b];
                return c
            }, average: function (a) {
                var b = a.length, a = Qa.sum(a);
                typeof a === "number" && b && (a /= b);
                return a
            }, open: function (a) {
                return a.length ? a[0] : a.hasNulls ? null : r
            }, high: function (a) {
                return a.length ? Ea(a) : a.hasNulls ? null : r
            }, low: function (a) {
                return a.length ?
                    Sa(a) : a.hasNulls ? null : r
            }, close: function (a) {
                return a.length ? a[a.length - 1] : a.hasNulls ? null : r
            }, ohlc: function (a, b, c, d) {
                a = Qa.open(a);
                b = Qa.high(b);
                c = Qa.low(c);
                d = Qa.close(d);
                if (typeof a === "number" || typeof b === "number" || typeof c === "number" || typeof d === "number")return [a, b, c, d]
            }, range: function (a, b) {
                a = Qa.low(a);
                b = Qa.high(b);
                if (typeof a === "number" || typeof b === "number")return [a, b]
            }
        };
    ca.groupData = function (a, b, c, d) {
        var e = this.data, f = this.options.data, g = [], h = [], i = a.length, j, k, l = !!b, m = [[], [], [], []], d = typeof d === "function" ?
            d : Qa[d], o = this.pointArrayMap, q = o && o.length, n;
        for (n = 0; n <= i; n++)if (a[n] >= c[0])break;
        for (; n <= i; n++) {
            for (; c[1] !== r && a[n] >= c[1] || n === i;)if (j = c.shift(), k = d.apply(0, m), k !== r && (g.push(j), h.push(k)), m[0] = [], m[1] = [], m[2] = [], m[3] = [], n === i)break;
            if (n === i)break;
            if (o) {
                j = this.cropStart + n;
                j = e && e[j] || this.pointClass.prototype.applyOptions.apply({series: this}, [f[j]]);
                var p;
                for (k = 0; k < q; k++)if (p = j[o[k]], typeof p === "number")m[k].push(p); else if (p === null)m[k].hasNulls = !0
            } else if (j = l ? b[n] : null, typeof j === "number")m[0].push(j);
            else if (j === null)m[0].hasNulls = !0
        }
        return [g, h]
    };
    ca.processData = function () {
        var a = this.chart, b = this.options, c = b.dataGrouping, d = this.allowDG !== !1 && c && p(c.enabled, a.options._stock), e;
        this.forceCrop = d;
        this.groupPixelWidth = null;
        this.hasProcessed = !0;
        if (gc.apply(this, arguments) !== !1 && d) {
            this.destroyGroupedData();
            var f = this.processedXData, g = this.processedYData, h = a.plotSizeX, a = this.xAxis, i = a.options.ordinal, j = this.groupPixelWidth = a.getGroupPixelWidth && a.getGroupPixelWidth(), d = this.pointRange;
            if (j) {
                e = !0;
                this.points =
                    null;
                var k = a.getExtremes(), d = k.min, k = k.max, i = i && a.getGroupIntervalFactor(d, k, this) || 1, h = j * (k - d) / h * i, j = a.getTimeTicks(a.normalizeTimeTickInterval(h, c.units || Vb), d, k, a.options.startOfWeek, f, this.closestPointRange), g = ca.groupData.apply(this, [f, g, j, c.approximation]), f = g[0], g = g[1];
                if (c.smoothed) {
                    c = f.length - 1;
                    for (f[c] = k; c-- && c > 0;)f[c] += h / 2;
                    f[0] = d
                }
                this.currentDataGrouping = j.info;
                if (b.pointRange === null)this.pointRange = j.info.totalRange;
                this.closestPointRange = j.info.totalRange;
                if (s(f[0]) && f[0] < a.dataMin) {
                    if (a.min ===
                        a.dataMin)a.min = f[0];
                    a.dataMin = f[0]
                }
                this.processedXData = f;
                this.processedYData = g
            } else this.currentDataGrouping = null, this.pointRange = d;
            this.hasGroupedData = e
        }
    };
    ca.destroyGroupedData = function () {
        var a = this.groupedData;
        n(a || [], function (b, c) {
            b && (a[c] = b.destroy ? b.destroy() : null)
        });
        this.groupedData = null
    };
    ca.generatePoints = function () {
        hc.apply(this);
        this.destroyGroupedData();
        this.groupedData = this.hasGroupedData ? this.points : null
    };
    Q.tooltipFooterHeaderFormatter = function (a, b) {
        var c = a.series, d = c.tooltipOptions,
            e = c.options.dataGrouping, f = d.xDateFormat, g, h = c.xAxis;
        h && h.options.type === "datetime" && e && sa(a.key) ? (c = c.currentDataGrouping, e = e.dateTimeLabelFormats, c ? (h = e[c.unitName], c.count === 1 ? f = h[0] : (f = h[1], g = h[2])) : !f && e && (f = this.getXDateFormat(a, d, h)), f = ka(f, a.key), g && (f += ka(g, a.key + c.totalRange - 1)), d = d[(b ? "footer" : "header") + "Format"].replace("{point.key}", f)) : d = jc.call(this, a, b);
        return d
    };
    ca.destroy = function () {
        for (var a = this.groupedData || [], b = a.length; b--;)a[b] && a[b].destroy();
        ic.apply(this)
    };
    S(ca, "setOptions",
        function (a, b) {
            var c = a.call(this, b), d = this.type, e = this.chart.options.plotOptions, f = V[d].dataGrouping;
            if (Ub[d])f || (f = y(kc, Ub[d])), c.dataGrouping = y(f, e.series && e.series.dataGrouping, e[d].dataGrouping, b.dataGrouping);
            if (this.chart.options._stock)this.requireSorting = !0;
            return c
        });
    S(O.prototype, "setScale", function (a) {
        a.call(this);
        n(this.series, function (a) {
            a.hasProcessed = !1
        })
    });
    O.prototype.getGroupPixelWidth = function () {
        var a = this.series, b = a.length, c, d = 0, e = !1, f;
        for (c = b; c--;)(f = a[c].options.dataGrouping) &&
        (d = v(d, f.groupPixelWidth));
        for (c = b; c--;)if ((f = a[c].options.dataGrouping) && a[c].hasProcessed)if (b = (a[c].processedXData || a[c].data).length, a[c].groupPixelWidth || b > this.chart.plotSizeX / d || b && f.forced)e = !0;
        return e ? d : 0
    };
    V.ohlc = y(V.column, {
        lineWidth: 1,
        tooltip: {pointFormat: '<span style="color:{point.color}">●</span> <b> {series.name}</b><br/>Open: {point.open}<br/>High: {point.high}<br/>Low: {point.low}<br/>Close: {point.close}<br/>'},
        states: {hover: {lineWidth: 3}},
        threshold: null
    });
    Q = ja(I.column, {
        type: "ohlc",
        pointArrayMap: ["open", "high", "low", "close"],
        toYData: function (a) {
            return [a.open, a.high, a.low, a.close]
        },
        pointValKey: "high",
        pointAttrToOptions: {stroke: "color", "stroke-width": "lineWidth"},
        upColorProp: "stroke",
        getAttribs: function () {
            I.column.prototype.getAttribs.apply(this, arguments);
            var a = this.options, b = a.states, a = a.upColor || this.color, c = y(this.pointAttr), d = this.upColorProp;
            c[""][d] = a;
            c.hover[d] = b.hover.upColor || a;
            c.select[d] = b.select.upColor || a;
            n(this.points, function (a) {
                if (a.open < a.close && !a.options.color)a.pointAttr =
                    c
            })
        },
        translate: function () {
            var a = this.yAxis;
            I.column.prototype.translate.apply(this);
            n(this.points, function (b) {
                if (b.open !== null)b.plotOpen = a.translate(b.open, 0, 1, 0, 1);
                if (b.close !== null)b.plotClose = a.translate(b.close, 0, 1, 0, 1)
            })
        },
        drawPoints: function () {
            var a = this, b = a.chart, c, d, e, f, g, h, i, j;
            n(a.points, function (k) {
                if (k.plotY !== r)i = k.graphic, c = k.pointAttr[k.selected ? "selected" : ""] || a.pointAttr[""], f = c["stroke-width"] % 2 / 2, j = w(k.plotX) - f, g = w(k.shapeArgs.width / 2), h = ["M", j, w(k.yBottom), "L", j, w(k.plotY)], k.open !==
                null && (d = w(k.plotOpen) + f, h.push("M", j, d, "L", j - g, d)), k.close !== null && (e = w(k.plotClose) + f, h.push("M", j, e, "L", j + g, e)), i ? i.animate({d: h}) : k.graphic = b.renderer.path(h).attr(c).add(a.group)
            })
        },
        animate: null
    });
    I.ohlc = Q;
    V.candlestick = y(V.column, {
        lineColor: "black",
        lineWidth: 1,
        states: {hover: {lineWidth: 2}},
        tooltip: V.ohlc.tooltip,
        threshold: null,
        upColor: "white"
    });
    Q = ja(Q, {
        type: "candlestick",
        pointAttrToOptions: {fill: "color", stroke: "lineColor", "stroke-width": "lineWidth"},
        upColorProp: "fill",
        getAttribs: function () {
            I.ohlc.prototype.getAttribs.apply(this,
                arguments);
            var a = this.options, b = a.states, c = a.upLineColor || a.lineColor, d = b.hover.upLineColor || c, e = b.select.upLineColor || c;
            n(this.points, function (a) {
                if (a.open < a.close)a.pointAttr[""].stroke = c, a.pointAttr.hover.stroke = d, a.pointAttr.select.stroke = e
            })
        },
        drawPoints: function () {
            var a = this, b = a.chart, c, d = a.pointAttr[""], e, f, g, h, i, j, k, l, m, o, q;
            n(a.points, function (n) {
                m = n.graphic;
                if (n.plotY !== r)c = n.pointAttr[n.selected ? "selected" : ""] || d, k = c["stroke-width"] % 2 / 2, l = w(n.plotX) - k, e = n.plotOpen, f = n.plotClose, g = Y.min(e,
                    f), h = Y.max(e, f), q = w(n.shapeArgs.width / 2), i = w(g) !== w(n.plotY), j = h !== n.yBottom, g = w(g) + k, h = w(h) + k, o = ["M", l - q, h, "L", l - q, g, "L", l + q, g, "L", l + q, h, "Z", "M", l, g, "L", l, i ? w(n.plotY) : g, "M", l, h, "L", l, j ? w(n.yBottom) : h], m ? m.animate({d: o}) : n.graphic = b.renderer.path(o).attr(c).add(a.group).shadow(a.options.shadow)
            })
        }
    });
    I.candlestick = Q;
    var sb = na.prototype.symbols;
    V.flags = y(V.column, {
        fillColor: "white",
        lineWidth: 1,
        pointRange: 0,
        shape: "flag",
        stackDistance: 12,
        states: {hover: {lineColor: "black", fillColor: "#FCFFC5"}},
        style: {
            fontSize: "11px",
            fontWeight: "bold", textAlign: "center"
        },
        tooltip: {pointFormat: "{point.text}<br/>"},
        threshold: null,
        y: -30
    });
    I.flags = ja(I.column, {
        type: "flags",
        sorted: !1,
        noSharedTooltip: !0,
        allowDG: !1,
        takeOrdinalPosition: !1,
        trackerGroups: ["markerGroup"],
        forceCrop: !0,
        init: P.prototype.init,
        pointAttrToOptions: {fill: "fillColor", stroke: "color", "stroke-width": "lineWidth", r: "radius"},
        translate: function () {
            I.column.prototype.translate.apply(this);
            var a = this.chart, b = this.points, c = b.length - 1, d, e, f = this.options.onSeries, f = (d = f && a.get(f)) &&
                d.options.step, g = d && d.points, h = g && g.length, i = this.xAxis, j = i.getExtremes(), k, l, m;
            if (d && d.visible && h) {
                d = d.currentDataGrouping;
                l = g[h - 1].x + (d ? d.totalRange : 0);
                for (b.sort(function (a, b) {
                    return a.x - b.x
                }); h-- && b[c];)if (d = b[c], k = g[h], k.x <= d.x && k.plotY !== r) {
                    if (d.x <= l)d.plotY = k.plotY, k.x < d.x && !f && (m = g[h + 1]) && m.plotY !== r && (d.plotY += (d.x - k.x) / (m.x - k.x) * (m.plotY - k.plotY));
                    c--;
                    h++;
                    if (c < 0)break
                }
            }
            n(b, function (c, d) {
                var f;
                if (c.plotY === r)c.x >= j.min && c.x <= j.max ? c.plotY = a.chartHeight - i.bottom - (i.opposite ? i.height : 0) + i.offset -
                    a.plotTop : c.shapeArgs = {};
                if ((e = b[d - 1]) && e.plotX === c.plotX) {
                    if (e.stackIndex === r)e.stackIndex = 0;
                    f = e.stackIndex + 1
                }
                c.stackIndex = f
            })
        },
        drawPoints: function () {
            var a, b = this.pointAttr[""], c = this.points, d = this.chart.renderer, e, f, g = this.options, h = g.y, i, j, k, l, m = g.lineWidth % 2 / 2, o, n;
            for (j = c.length; j--;)if (k = c[j], a = k.plotX > this.xAxis.len, e = k.plotX + (a ? m : -m), l = k.stackIndex, i = k.options.shape || g.shape, f = k.plotY, f !== r && (f = k.plotY + h + m - (l !== r && l * g.stackDistance)), o = l ? r : k.plotX + m, n = l ? r : k.plotY, l = k.graphic, f !== r && e >= 0 && !a)a =
                k.pointAttr[k.selected ? "select" : ""] || b, l ? l.attr({
                x: e,
                y: f,
                r: a.r,
                anchorX: o,
                anchorY: n
            }) : k.graphic = d.label(k.options.title || g.title || "A", e, f, i, o, n, g.useHTML).css(y(g.style, k.style)).attr(a).attr({
                align: i === "flag" ? "left" : "center",
                width: g.width,
                height: g.height
            }).add(this.markerGroup).shadow(g.shadow), k.tooltipPos = [e, f]; else if (l)k.graphic = l.destroy()
        },
        drawTracker: function () {
            var a = this.points;
            ib.drawTrackerPoint.apply(this);
            n(a, function (b) {
                var c = b.graphic;
                c && D(c.element, "mouseover", function () {
                    if (b.stackIndex >
                        0 && !b.raised)b._y = c.y, c.attr({y: b._y - 8}), b.raised = !0;
                    n(a, function (a) {
                        if (a !== b && a.raised && a.graphic)a.graphic.attr({y: a._y}), a.raised = !1
                    })
                })
            })
        },
        animate: ga,
        buildKDTree: ga,
        setClip: ga
    });
    sb.flag = function (a, b, c, d, e) {
        var f = e && e.anchorX || a, e = e && e.anchorY || b;
        return ["M", f, e, "L", a, b + d, a, b, a + c, b, a + c, b + d, a, b + d, "M", f, e, "Z"]
    };
    n(["circle", "square"], function (a) {
        sb[a + "pin"] = function (b, c, d, e, f) {
            var g = f && f.anchorX, f = f && f.anchorY, b = sb[a](b, c, d, e);
            g && f && b.push("M", g, c > f ? c : c + e, "L", g, f);
            return b
        }
    });
    Wa === z.VMLRenderer && n(["flag",
        "circlepin", "squarepin"], function (a) {
        hb.prototype.symbols[a] = sb[a]
    });
    var Q = [].concat(Vb), tb = function (a) {
        var b = pb(arguments, function (a) {
            return typeof a === "number"
        });
        if (b.length)return Math[a].apply(0, b)
    };
    Q[4] = ["day", [1, 2, 3, 4]];
    Q[5] = ["week", [1, 2, 3]];
    x(F, {
        navigator: {
            handles: {backgroundColor: "#ebe7e8", borderColor: "#b2b1b6"},
            height: 40,
            margin: 25,
            maskFill: "rgba(128,179,236,0.3)",
            maskInside: !0,
            outlineColor: "#b2b1b6",
            outlineWidth: 1,
            series: {
                type: I.areaspline === r ? "line" : "areaspline",
                color: "#4572A7",
                compare: null,
                fillOpacity: 0.05,
                dataGrouping: {approximation: "average", enabled: !0, groupPixelWidth: 2, smoothed: !0, units: Q},
                dataLabels: {enabled: !1, zIndex: 2},
                id: "highcharts-navigator-series",
                lineColor: "#4572A7",
                lineWidth: 1,
                marker: {enabled: !1},
                pointRange: 0,
                shadow: !1,
                threshold: null
            },
            xAxis: {
                tickWidth: 0,
                lineWidth: 0,
                gridLineColor: "#EEE",
                gridLineWidth: 1,
                tickPixelInterval: 200,
                labels: {align: "left", style: {color: "#888"}, x: 3, y: -4},
                crosshair: !1
            },
            yAxis: {
                gridLineWidth: 0,
                startOnTick: !1,
                endOnTick: !1,
                minPadding: 0.1,
                maxPadding: 0.1,
                labels: {enabled: !1},
                crosshair: !1,
                title: {text: null},
                tickWidth: 0
            }
        },
        scrollbar: {
            height: eb ? 20 : 14,
            barBackgroundColor: "#bfc8d1",
            barBorderRadius: 0,
            barBorderWidth: 1,
            barBorderColor: "#bfc8d1",
            buttonArrowColor: "#666",
            buttonBackgroundColor: "#ebe7e8",
            buttonBorderColor: "#bbb",
            buttonBorderRadius: 0,
            buttonBorderWidth: 1,
            minWidth: 6,
            rifleColor: "#666",
            trackBackgroundColor: "#eeeeee",
            trackBorderColor: "#eeeeee",
            trackBorderWidth: 1,
            liveRedraw: da && !eb
        }
    });
    Eb.prototype = {
        drawHandle: function (a, b) {
            var c = this.chart, d = c.renderer, e = this.elementsToDestroy,
                f = this.handles, g = this.navigatorOptions.handles, g = {
                    fill: g.backgroundColor,
                    stroke: g.borderColor,
                    "stroke-width": 1
                }, h;
            this.rendered || (f[b] = d.g("navigator-handle-" + ["left", "right"][b]).css({cursor: "ew-resize"}).attr({zIndex: 4 - b}).add(), h = d.rect(-4.5, 0, 9, 16, 0, 1).attr(g).add(f[b]), e.push(h), h = d.path(["M", -1.5, 4, "L", -1.5, 12, "M", 0.5, 4, "L", 0.5, 12]).attr(g).add(f[b]), e.push(h));
            f[b][c.isResizing ? "animate" : "attr"]({
                translateX: this.scrollerLeft + this.scrollbarHeight + parseInt(a, 10),
                translateY: this.top + this.height /
                2 - 8
            })
        }, drawScrollbarButton: function (a) {
            var b = this.chart.renderer, c = this.elementsToDestroy, d = this.scrollbarButtons, e = this.scrollbarHeight, f = this.scrollbarOptions, g;
            this.rendered || (d[a] = b.g().add(this.scrollbarGroup), g = b.rect(-0.5, -0.5, e + 1, e + 1, f.buttonBorderRadius, f.buttonBorderWidth).attr({
                stroke: f.buttonBorderColor,
                "stroke-width": f.buttonBorderWidth,
                fill: f.buttonBackgroundColor
            }).add(d[a]), c.push(g), g = b.path(["M", e / 2 + (a ? -1 : 1), e / 2 - 3, "L", e / 2 + (a ? -1 : 1), e / 2 + 3, e / 2 + (a ? 2 : -2), e / 2]).attr({fill: f.buttonArrowColor}).add(d[a]),
                c.push(g));
            a && d[a].attr({translateX: this.scrollerWidth - e})
        }, render: function (a, b, c, d) {
            var e = this.chart, f = e.renderer, g, h, i, j, k = this.scrollbarGroup, l = this.navigatorGroup, m = this.scrollbar, l = this.xAxis, o = this.scrollbarTrack, n = this.scrollbarHeight, t = this.scrollbarEnabled, r = this.navigatorOptions, s = this.scrollbarOptions, u = s.minWidth, A = this.height, x = this.top, y = this.navigatorEnabled, z = r.outlineWidth, C = z / 2, D = 0, E = this.outlineHeight, H = s.barBorderRadius, G = s.barBorderWidth, F = x + C, I;
            if (!isNaN(a)) {
                this.navigatorLeft =
                    g = p(l.left, e.plotLeft + n);
                this.navigatorWidth = h = p(l.len, e.plotWidth - 2 * n);
                this.scrollerLeft = i = g - n;
                this.scrollerWidth = j = j = h + 2 * n;
                l.getExtremes && (I = this.getUnionExtremes(!0)) && (I.dataMin !== l.min || I.dataMax !== l.max) && l.setExtremes(I.dataMin, I.dataMax, !0, !1);
                c = p(c, l.translate(a));
                d = p(d, l.translate(b));
                if (isNaN(c) || R(c) === Infinity)c = 0, d = j;
                if (!(l.translate(d, !0) - l.translate(c, !0) < e.xAxis[0].minRange)) {
                    this.zoomedMax = B(v(c, d), h);
                    this.zoomedMin = v(this.fixedWidth ? this.zoomedMax - this.fixedWidth : B(c, d), 0);
                    this.range =
                        this.zoomedMax - this.zoomedMin;
                    c = w(this.zoomedMax);
                    b = w(this.zoomedMin);
                    a = c - b;
                    if (!this.rendered) {
                        if (y)this.navigatorGroup = l = f.g("navigator").attr({zIndex: 3}).add(), this.leftShade = f.rect().attr({fill: r.maskFill}).add(l), r.maskInside ? this.leftShade.css({cursor: "ew-resize "}) : this.rightShade = f.rect().attr({fill: r.maskFill}).add(l), this.outline = f.path().attr({
                            "stroke-width": z,
                            stroke: r.outlineColor
                        }).add(l);
                        if (t)this.scrollbarGroup = k = f.g("scrollbar").add(), m = s.trackBorderWidth, this.scrollbarTrack = o = f.rect().attr({
                            x: 0,
                            y: -m % 2 / 2,
                            fill: s.trackBackgroundColor,
                            stroke: s.trackBorderColor,
                            "stroke-width": m,
                            r: s.trackBorderRadius || 0,
                            height: n
                        }).add(k), this.scrollbar = m = f.rect().attr({
                            y: -G % 2 / 2,
                            height: n,
                            fill: s.barBackgroundColor,
                            stroke: s.barBorderColor,
                            "stroke-width": G,
                            r: H
                        }).add(k), this.scrollbarRifles = f.path().attr({
                            stroke: s.rifleColor,
                            "stroke-width": 1
                        }).add(k)
                    }
                    e = e.isResizing ? "animate" : "attr";
                    if (y) {
                        this.leftShade[e](r.maskInside ? {x: g + b, y: x, width: c - b, height: A} : {
                            x: g,
                            y: x,
                            width: b,
                            height: A
                        });
                        if (this.rightShade)this.rightShade[e]({
                            x: g +
                            c, y: x, width: h - c, height: A
                        });
                        this.outline[e]({d: ["M", i, F, "L", g + b - C, F, g + b - C, F + E, "L", g + c - C, F + E, "L", g + c - C, F, i + j, F].concat(r.maskInside ? ["M", g + b + C, F, "L", g + c - C, F] : [])});
                        this.drawHandle(b + C, 0);
                        this.drawHandle(c + C, 1)
                    }
                    if (t && k)this.drawScrollbarButton(0), this.drawScrollbarButton(1), k[e]({
                        translateX: i,
                        translateY: w(F + A)
                    }), o[e]({width: j}), g = n + b, h = a - G, h < u && (D = (u - h) / 2, h = u, g -= D), this.scrollbarPad = D, m[e]({
                        x: X(g) + G % 2 / 2,
                        width: h
                    }), u = n + b + a / 2 - 0.5, this.scrollbarRifles.attr({visibility: a > 12 ? "visible" : "hidden"})[e]({
                        d: ["M",
                            u - 3, n / 4, "L", u - 3, 2 * n / 3, "M", u, n / 4, "L", u, 2 * n / 3, "M", u + 3, n / 4, "L", u + 3, 2 * n / 3]
                    });
                    this.scrollbarPad = D;
                    this.rendered = !0
                }
            }
        }, addEvents: function () {
            var a = this.chart.container, b = this.mouseDownHandler, c = this.mouseMoveHandler, d = this.mouseUpHandler, e;
            e = [[a, "mousedown", b], [a, "mousemove", c], [document, "mouseup", d]];
            $a && e.push([a, "touchstart", b], [a, "touchmove", c], [document, "touchend", d]);
            n(e, function (a) {
                D.apply(null, a)
            });
            this._events = e
        }, removeEvents: function () {
            n(this._events, function (a) {
                U.apply(null, a)
            });
            this._events =
                r;
            this.navigatorEnabled && this.baseSeries && U(this.baseSeries, "updatedData", this.updatedDataHandler)
        }, init: function () {
            var a = this, b = a.chart, c, d, e = a.scrollbarHeight, f = a.navigatorOptions, g = a.height, h = a.top, i, j, k = a.baseSeries;
            a.mouseDownHandler = function (d) {
                var d = b.pointer.normalize(d), e = a.zoomedMin, f = a.zoomedMax, h = a.top, j = a.scrollbarHeight, k = a.scrollerLeft, l = a.scrollerWidth, m = a.navigatorLeft, n = a.navigatorWidth, p = a.scrollbarPad, r = a.range, s = d.chartX, v = d.chartY, d = b.xAxis[0], w, x = eb ? 10 : 7;
                if (v > h && v < h + g + j)if ((h =
                        !a.scrollbarEnabled || v < h + g) && Y.abs(s - e - m) < x)a.grabbedLeft = !0, a.otherHandlePos = f, a.fixedExtreme = d.max, b.fixedRange = null; else if (h && Y.abs(s - f - m) < x)a.grabbedRight = !0, a.otherHandlePos = e, a.fixedExtreme = d.min, b.fixedRange = null; else if (s > m + e - p && s < m + f + p)a.grabbedCenter = s, a.fixedWidth = r, i = s - e; else if (s > k && s < k + l) {
                    f = h ? s - m - r / 2 : s < m ? e - r * 0.2 : s > k + l - j ? e + r * 0.2 : s < m + e ? e - r : f;
                    if (f < 0)f = 0; else if (f + r >= n)f = n - r, w = a.getUnionExtremes().dataMax;
                    if (f !== e)a.fixedWidth = r, e = c.toFixedRange(f, f + r, null, w), d.setExtremes(e.min, e.max,
                        !0, !1, {trigger: "navigator"})
                }
            };
            a.mouseMoveHandler = function (c) {
                var d = a.scrollbarHeight, e = a.navigatorLeft, f = a.navigatorWidth, g = a.scrollerLeft, h = a.scrollerWidth, k = a.range, l;
                if (c.pageX !== 0)c = b.pointer.normalize(c), l = c.chartX, l < e ? l = e : l > g + h - d && (l = g + h - d), a.grabbedLeft ? (j = !0, a.render(0, 0, l - e, a.otherHandlePos)) : a.grabbedRight ? (j = !0, a.render(0, 0, a.otherHandlePos, l - e)) : a.grabbedCenter && (j = !0, l < i ? l = i : l > f + i - k && (l = f + i - k), a.render(0, 0, l - i, l - i + k)), j && a.scrollbarOptions.liveRedraw && setTimeout(function () {
                        a.mouseUpHandler(c)
                    },
                    0)
            };
            a.mouseUpHandler = function (d) {
                var e, f;
                if (j) {
                    if (a.zoomedMin === a.otherHandlePos)e = a.fixedExtreme; else if (a.zoomedMax === a.otherHandlePos)f = a.fixedExtreme;
                    e = c.toFixedRange(a.zoomedMin, a.zoomedMax, e, f);
                    b.xAxis[0].setExtremes(e.min, e.max, !0, !1, {
                        trigger: "navigator",
                        triggerOp: "navigator-drag",
                        DOMEvent: d
                    })
                }
                if (d.type !== "mousemove")a.grabbedLeft = a.grabbedRight = a.grabbedCenter = a.fixedWidth = a.fixedExtreme = a.otherHandlePos = j = i = null
            };
            var l = b.xAxis.length, m = b.yAxis.length;
            b.extraBottomMargin = a.outlineHeight + f.margin;
            a.navigatorEnabled ? (a.xAxis = c = new O(b, y({
                breaks: k && k.xAxis.options.breaks,
                ordinal: k && k.xAxis.options.ordinal
            }, f.xAxis, {
                id: "navigator-x-axis",
                isX: !0,
                type: "datetime",
                index: l,
                height: g,
                offset: 0,
                offsetLeft: e,
                offsetRight: -e,
                keepOrdinalPadding: !0,
                startOnTick: !1,
                endOnTick: !1,
                minPadding: 0,
                maxPadding: 0,
                zoomEnabled: !1
            })), a.yAxis = d = new O(b, y(f.yAxis, {
                id: "navigator-y-axis",
                alignTicks: !1,
                height: g,
                offset: 0,
                index: m,
                zoomEnabled: !1
            })), k || f.series.data ? a.addBaseSeries() : b.series.length === 0 && S(b, "redraw", function (c,
                                                                                                            d) {
                if (b.series.length > 0 && !a.series)a.setBaseSeries(), b.redraw = c;
                c.call(b, d)
            })) : a.xAxis = c = {
                translate: function (a, c) {
                    var d = b.xAxis[0], f = d.getExtremes(), g = b.plotWidth - 2 * e, h = tb("min", d.options.min, f.dataMin), d = tb("max", d.options.max, f.dataMax) - h;
                    return c ? a * d / g + h : g * (a - h) / d
                }, toFixedRange: O.prototype.toFixedRange
            };
            S(b, "getMargins", function (b) {
                var e = this.legend, f = e.options;
                b.apply(this, [].slice.call(arguments, 1));
                a.top = h = a.navigatorOptions.top || this.chartHeight - a.height - a.scrollbarHeight - this.spacing[2] - (f.verticalAlign ===
                    "bottom" && f.enabled && !f.floating ? e.legendHeight + p(f.margin, 10) : 0);
                if (c && d)c.options.top = d.options.top = h, c.setAxisSize(), d.setAxisSize()
            });
            a.addEvents()
        }, getUnionExtremes: function (a) {
            var b = this.chart.xAxis[0], c = this.xAxis, d = c.options, e = b.options;
            if (!a || b.dataMin !== null)return {
                dataMin: tb("min", d && d.min, e.min, b.dataMin, c.dataMin),
                dataMax: tb("max", d && d.max, e.max, b.dataMax, c.dataMax)
            }
        }, setBaseSeries: function (a) {
            var b = this.chart, a = a || b.options.navigator.baseSeries;
            this.series && this.series.remove();
            this.baseSeries =
                b.series[a] || typeof a === "string" && b.get(a) || b.series[0];
            this.xAxis && this.addBaseSeries()
        }, addBaseSeries: function () {
            var a = this.baseSeries, b = a ? a.options : {}, c = b.data, d = this.navigatorOptions.series, e;
            e = d.data;
            this.hasNavigatorData = !!e;
            b = y(b, d, {
                enableMouseTracking: !1,
                group: "nav",
                padXAxis: !1,
                xAxis: "navigator-x-axis",
                yAxis: "navigator-y-axis",
                name: "Navigator",
                showInLegend: !1,
                isInternal: !0,
                visible: !0
            });
            b.data = e || c;
            this.series = this.chart.initSeries(b);
            if (a && this.navigatorOptions.adaptToUpdatedData !== !1)D(a,
                "updatedData", this.updatedDataHandler), a.userOptions.events = x(a.userOptions.event, {updatedData: this.updatedDataHandler})
        }, updatedDataHandler: function () {
            var a = this.chart.scroller, b = a.baseSeries, c = b.xAxis, d = c.getExtremes(), e = d.min, f = d.max, g = d.dataMin, d = d.dataMax, h = f - e, i, j, k, l, m, o = a.series;
            i = o.xData;
            var n = !!c.setExtremes;
            j = f >= i[i.length - 1] - (this.closestPointRange || 0);
            i = e <= g;
            if (!a.hasNavigatorData)o.options.pointStart = b.xData[0], o.setData(b.options.data, !1), m = !0;
            i && (l = g, k = l + h);
            j && (k = d, i || (l = v(k - h, o.xData[0])));
            n && (i || j) ? isNaN(l) || c.setExtremes(l, k, !0, !1, {trigger: "updatedData"}) : (m && this.chart.redraw(!1), a.render(v(e, g), B(f, d)))
        }, destroy: function () {
            this.removeEvents();
            n([this.xAxis, this.yAxis, this.leftShade, this.rightShade, this.outline, this.scrollbarTrack, this.scrollbarRifles, this.scrollbarGroup, this.scrollbar], function (a) {
                a && a.destroy && a.destroy()
            });
            this.xAxis = this.yAxis = this.leftShade = this.rightShade = this.outline = this.scrollbarTrack = this.scrollbarRifles = this.scrollbarGroup = this.scrollbar = null;
            n([this.scrollbarButtons,
                this.handles, this.elementsToDestroy], function (a) {
                Na(a)
            })
        }
    };
    z.Scroller = Eb;
    S(O.prototype, "zoom", function (a, b, c) {
        var d = this.chart, e = d.options, f = e.chart.zoomType, g = e.navigator, e = e.rangeSelector, h;
        if (this.isXAxis && (g && g.enabled || e && e.enabled))if (f === "x")d.resetZoomButton = "blocked"; else if (f === "y")h = !1; else if (f === "xy")d = this.previousZoom, s(b) ? this.previousZoom = [this.min, this.max] : d && (b = d[0], c = d[1], delete this.previousZoom);
        return h !== r ? h : a.call(this, b, c)
    });
    S(Pa.prototype, "init", function (a, b, c) {
        D(this,
            "beforeRender", function () {
                var a = this.options;
                if (a.navigator.enabled || a.scrollbar.enabled)this.scroller = new Eb(this)
            });
        a.call(this, b, c)
    });
    S(P.prototype, "addPoint", function (a, b, c, d, e) {
        var f = this.options.turboThreshold;
        f && this.xData.length > f && ia(b) && !Ka(b) && this.chart.scroller && qa(20, !0);
        a.call(this, b, c, d, e)
    });
    x(F, {
        rangeSelector: {
            buttonTheme: {
                width: 28,
                height: 18,
                fill: "#f7f7f7",
                padding: 2,
                r: 0,
                "stroke-width": 0,
                style: {color: "#444", cursor: "pointer", fontWeight: "normal"},
                zIndex: 7,
                states: {
                    hover: {fill: "#e7e7e7"},
                    select: {fill: "#e7f0f9", style: {color: "black", fontWeight: "bold"}}
                }
            }, inputPosition: {align: "right"}, labelStyle: {color: "#666"}
        }
    });
    F.lang = y(F.lang, {rangeSelectorZoom: "Zoom", rangeSelectorFrom: "From", rangeSelectorTo: "To"});
    Fb.prototype = {
        clickButton: function (a, b) {
            var c = this, d = c.selected, e = c.chart, f = c.buttons, g = c.buttonOptions[a], h = e.xAxis[0], i = e.scroller && e.scroller.getUnionExtremes() || h || {}, j = i.dataMin, k = i.dataMax, l, m = h && w(B(h.max, p(k, h.max))), o = new ea(m), q = g.type, t = g.count, i = g._range, s;
            if (!(j === null || k ===
                null || a === c.selected)) {
                if (q === "month" || q === "year")l = {
                    month: "Month",
                    year: "FullYear"
                }[q], o["set" + l](o["get" + l]() - t), l = o.getTime(), j = p(j, Number.MIN_VALUE), isNaN(l) || l < j ? (l = j, m = B(l + i, k)) : i = m - l; else if (i)l = v(m - i, j), m = B(l + i, k); else if (q === "ytd")if (h) {
                    if (k === r)j = Number.MAX_VALUE, k = Number.MIN_VALUE, n(e.series, function (a) {
                        a = a.xData;
                        j = B(a[0], j);
                        k = v(a[a.length - 1], k)
                    }), b = !1;
                    m = new ea(k);
                    s = m.getFullYear();
                    l = s = v(j || 0, ea.UTC(s, 0, 1));
                    m = m.getTime();
                    m = B(k || m, m)
                } else {
                    D(e, "beforeRender", function () {
                        c.clickButton(a)
                    });
                    return
                } else q === "all" && h && (l = j, m = k);
                f[d] && f[d].setState(0);
                f[a] && f[a].setState(2);
                e.fixedRange = i;
                h ? h.setExtremes(l, m, p(b, 1), 0, {
                    trigger: "rangeSelectorButton",
                    rangeSelectorButton: g
                }) : (d = e.options.xAxis, d[0] = y(d[0], {range: i, min: s}));
                c.setSelected(a)
            }
        },
        setSelected: function (a) {
            this.selected = this.options.selected = a
        },
        defaultButtons: [{type: "month", count: 1, text: "1m"}, {type: "month", count: 3, text: "3m"}, {
            type: "month",
            count: 6,
            text: "6m"
        }, {type: "ytd", text: "YTD"}, {type: "year", count: 1, text: "1y"}, {type: "all", text: "All"}],
        init: function (a) {
            var b = this, c = a.options.rangeSelector, d = c.buttons || [].concat(b.defaultButtons), e = c.selected, f = b.blurInputs = function () {
                var a = b.minInput, c = b.maxInput;
                a && a.blur && K(a, "blur");
                c && c.blur && K(c, "blur")
            };
            b.chart = a;
            b.options = c;
            b.buttons = [];
            a.extraTopMargin = 35;
            b.buttonOptions = d;
            D(a.container, "mousedown", f);
            D(a, "resize", f);
            n(d, b.computeButtonRange);
            e !== r && d[e] && this.clickButton(e, !1);
            D(a, "load", function () {
                D(a.xAxis[0], "afterSetExtremes", function () {
                    b.updateButtonStates(!0)
                })
            })
        },
        updateButtonStates: function (a) {
            var b =
                this, c = this.chart, d = c.xAxis[0], e = c.scroller && c.scroller.getUnionExtremes() || d, f = e.dataMin, g = e.dataMax, h = b.selected, i = b.options.allButtonsEnabled, j = b.buttons;
            a && c.fixedRange !== w(d.max - d.min) && (j[h] && j[h].setState(0), b.setSelected(null));
            n(b.buttonOptions, function (a, c) {
                var e = a._range, o = e > g - f, n = e < d.minRange, p = a.type === "all" && d.max - d.min >= g - f && j[c].state !== 2, s = a.type === "ytd" && ka("%Y", f) === ka("%Y", g);
                e === w(d.max - d.min) && c !== h ? (b.setSelected(c), j[c].setState(2)) : !i && (o || n || p || s) ? j[c].setState(3) : j[c].state ===
                3 && j[c].setState(0)
            })
        },
        computeButtonRange: function (a) {
            var b = a.type, c = a.count || 1, d = {
                millisecond: 1,
                second: 1E3,
                minute: 6E4,
                hour: 36E5,
                day: 864E5,
                week: 6048E5
            };
            if (d[b])a._range = d[b] * c; else if (b === "month" || b === "year")a._range = {
                    month: 30,
                    year: 365
                }[b] * 864E5 * c
        },
        setInputValue: function (a, b) {
            var c = this.chart.options.rangeSelector;
            if (s(b))this[a + "Input"].HCTime = b;
            this[a + "Input"].value = ka(c.inputEditDateFormat || "%Y-%m-%d", this[a + "Input"].HCTime);
            this[a + "DateBox"].attr({
                text: ka(c.inputDateFormat || "%b %e, %Y", this[a +
                "Input"].HCTime)
            })
        },
        drawInput: function (a) {
            var b = this, c = b.chart, d = c.renderer.style, e = c.renderer, f = c.options.rangeSelector, g = b.div, h = a === "min", i, j, k, l = this.inputGroup;
            this[a + "Label"] = j = e.label(F.lang[h ? "rangeSelectorFrom" : "rangeSelectorTo"], this.inputGroup.offset).attr({padding: 2}).css(y(d, f.labelStyle)).add(l);
            l.offset += j.width + 5;
            this[a + "DateBox"] = k = e.label("", l.offset).attr({
                padding: 2,
                width: f.inputBoxWidth || 90,
                height: f.inputBoxHeight || 17,
                stroke: f.inputBoxBorderColor || "silver",
                "stroke-width": 1
            }).css(y({
                textAlign: "center",
                color: "#444"
            }, d, f.inputStyle)).on("click", function () {
                b[a + "Input"].focus()
            }).add(l);
            l.offset += k.width + (h ? 10 : 0);
            this[a + "Input"] = i = aa("input", {
                name: a,
                className: "highcharts-range-selector",
                type: "text"
            }, x({
                position: "absolute",
                border: 0,
                width: "1px",
                height: "1px",
                padding: 0,
                textAlign: "center",
                fontSize: d.fontSize,
                fontFamily: d.fontFamily,
                top: c.plotTop + "px"
            }, f.inputStyle), g);
            i.onfocus = function () {
                M(this, {
                    left: l.translateX + k.x + "px",
                    top: l.translateY + "px",
                    width: k.width - 2 + "px",
                    height: k.height - 2 + "px",
                    border: "2px solid silver"
                })
            };
            i.onblur = function () {
                M(this, {border: 0, width: "1px", height: "1px"});
                b.setInputValue(a)
            };
            i.onchange = function () {
                var a = i.value, d = (f.inputDateParser || ea.parse)(a), e = c.xAxis[0], g = e.dataMin, j = e.dataMax;
                isNaN(d) && (d = a.split("-"), d = ea.UTC(C(d[0]), C(d[1]) - 1, C(d[2])));
                isNaN(d) || (F.global.useUTC || (d += (new ea).getTimezoneOffset() * 6E4), h ? d > b.maxInput.HCTime ? d = r : d < g && (d = g) : d < b.minInput.HCTime ? d = r : d > j && (d = j), d !== r && c.xAxis[0].setExtremes(h ? d : e.min, h ? e.max : d, r, r, {trigger: "rangeSelectorInput"}))
            }
        },
        render: function (a, b) {
            var c =
                this, d = c.chart, e = d.renderer, f = d.container, g = d.options, h = g.exporting && g.navigation && g.navigation.buttonOptions, i = g.rangeSelector, j = c.buttons, k = F.lang, g = c.div, g = c.inputGroup, l = i.buttonTheme, m = i.buttonPosition || {}, o = i.inputEnabled, q = l && l.states, t = d.plotLeft, r, v, u = c.group;
            if (!c.rendered && (c.group = u = e.g("range-selector-buttons").add(), c.zoomText = e.text(k.rangeSelectorZoom, p(m.x, t), p(m.y, d.plotTop - 35) + 15).css(i.labelStyle).add(u), r = p(m.x, t) + c.zoomText.getBBox().width + 5, v = p(m.y, d.plotTop - 35), n(c.buttonOptions,
                    function (a, b) {
                        j[b] = e.button(a.text, r, v, function () {
                            c.clickButton(b);
                            c.isActive = !0
                        }, l, q && q.hover, q && q.select, q && q.disabled).css({textAlign: "center"}).add(u);
                        r += j[b].width + p(i.buttonSpacing, 5);
                        c.selected === b && j[b].setState(2)
                    }), c.updateButtonStates(), o !== !1))c.div = g = aa("div", null, {
                position: "relative",
                height: 0,
                zIndex: 1
            }), f.parentNode.insertBefore(g, f), c.inputGroup = g = e.g("input-group").add(), g.offset = 0, c.drawInput("min"), c.drawInput("max");
            o !== !1 && (f = d.plotTop - 45, g.align(x({
                y: f, width: g.offset, x: h && f < (h.y ||
                0) + h.height - d.spacing[0] ? -40 : 0
            }, i.inputPosition), !0, d.spacingBox), s(o) || (d = u.getBBox(), g[g.translateX < d.x + d.width + 10 ? "hide" : "show"]()), c.setInputValue("min", a), c.setInputValue("max", b));
            c.rendered = !0
        },
        destroy: function () {
            var a = this.minInput, b = this.maxInput, c = this.chart, d = this.blurInputs, e;
            U(c.container, "mousedown", d);
            U(c, "resize", d);
            Na(this.buttons);
            if (a)a.onfocus = a.onblur = a.onchange = null;
            if (b)b.onfocus = b.onblur = b.onchange = null;
            for (e in this)this[e] && e !== "chart" && (this[e].destroy ? this[e].destroy() :
            this[e].nodeType && Ta(this[e])), this[e] = null
        }
    };
    O.prototype.toFixedRange = function (a, b, c, d) {
        var e = this.chart && this.chart.fixedRange, a = p(c, this.translate(a, !0)), b = p(d, this.translate(b, !0)), c = e && (b - a) / e;
        c > 0.7 && c < 1.3 && (d ? a = b - e : b = a + e);
        return {min: a, max: b}
    };
    S(Pa.prototype, "init", function (a, b, c) {
        D(this, "init", function () {
            if (this.options.rangeSelector.enabled)this.rangeSelector = new Fb(this)
        });
        a.call(this, b, c)
    });
    z.RangeSelector = Fb;
    Pa.prototype.callbacks.push(function (a) {
        function b() {
            f = a.xAxis[0].getExtremes();
            g.render(f.min, f.max)
        }

        function c() {
            f = a.xAxis[0].getExtremes();
            isNaN(f.min) || h.render(f.min, f.max)
        }

        function d(a) {
            a.triggerOp !== "navigator-drag" && g.render(a.min, a.max)
        }

        function e(a) {
            h.render(a.min, a.max)
        }

        var f, g = a.scroller, h = a.rangeSelector;
        g && (D(a.xAxis[0], "afterSetExtremes", d), S(a, "drawChartBox", function (a) {
            var c = this.isDirtyBox;
            a.call(this);
            c && b()
        }), b());
        h && (D(a.xAxis[0], "afterSetExtremes", e), D(a, "resize", c), c());
        D(a, "destroy", function () {
            g && U(a.xAxis[0], "afterSetExtremes", d);
            h && (U(a, "resize", c),
                U(a.xAxis[0], "afterSetExtremes", e))
        })
    });
    z.StockChart = function (a, b) {
        var c = a.series, d, e = p(a.navigator && a.navigator.enabled, !0) ? {
            startOnTick: !1,
            endOnTick: !1
        } : null, f = {marker: {enabled: !1, radius: 2}, states: {hover: {lineWidth: 2}}}, g = {
            shadow: !1,
            borderWidth: 0
        };
        a.xAxis = za(pa(a.xAxis || {}), function (a) {
            return y({
                minPadding: 0,
                maxPadding: 0,
                ordinal: !0,
                title: {text: null},
                labels: {overflow: "justify"},
                showLastLabel: !0
            }, a, {type: "datetime", categories: null}, e)
        });
        a.yAxis = za(pa(a.yAxis || {}), function (a) {
            d = p(a.opposite, !0);
            return y({
                labels: {y: -2},
                opposite: d, showLastLabel: !1, title: {text: null}
            }, a)
        });
        a.series = null;
        a = y({
            chart: {panning: !0, pinchType: "x"},
            navigator: {enabled: !0},
            scrollbar: {enabled: !0},
            rangeSelector: {enabled: !0},
            title: {text: null, style: {fontSize: "16px"}},
            tooltip: {shared: !0, crosshairs: !0},
            legend: {enabled: !1},
            plotOptions: {
                line: f,
                spline: f,
                area: f,
                areaspline: f,
                arearange: f,
                areasplinerange: f,
                column: g,
                columnrange: g,
                candlestick: g,
                ohlc: g
            }
        }, a, {_stock: !0, chart: {inverted: !1}});
        a.series = c;
        return new Pa(a, b)
    };
    S(Xa.prototype, "init", function (a, b, c) {
        var d =
            c.chart.pinchType || "";
        a.call(this, b, c);
        this.pinchX = this.pinchHor = d.indexOf("x") !== -1;
        this.pinchY = this.pinchVert = d.indexOf("y") !== -1;
        this.hasZoom = this.hasZoom || this.pinchHor || this.pinchVert
    });
    S(O.prototype, "autoLabelAlign", function (a) {
        var b = this.chart, c = this.options, b = b._labelPanes = b._labelPanes || {}, d = this.options.labels;
        if (this.chart.options._stock && this.coll === "yAxis" && (c = c.top + "," + c.height, !b[c] && d.enabled)) {
            if (d.x === 15)d.x = 0;
            if (d.align === void 0)d.align = "right";
            b[c] = 1;
            return "right"
        }
        return a.call(this,
            [].slice.call(arguments, 1))
    });
    S(O.prototype, "getPlotLinePath", function (a, b, c, d, e, f) {
        var g = this, h = this.isLinked && !this.series ? this.linkedParent.series : this.series, i = g.chart, j = i.renderer, k = g.left, l = g.top, m, o, q, r, x = [], y = [], u;
        if (g.coll === "colorAxis")return a.apply(this, [].slice.call(arguments, 1));
        y = g.isXAxis ? s(g.options.yAxis) ? [i.yAxis[g.options.yAxis]] : za(h, function (a) {
            return a.yAxis
        }) : s(g.options.xAxis) ? [i.xAxis[g.options.xAxis]] : za(h, function (a) {
            return a.xAxis
        });
        n(g.isXAxis ? i.yAxis : i.xAxis, function (a) {
            if (s(a.options.id) ?
                a.options.id.indexOf("navigator") === -1 : 1) {
                var b = a.isXAxis ? "yAxis" : "xAxis", b = s(a.options[b]) ? i[b][a.options[b]] : i[b][0];
                g === b && y.push(a)
            }
        });
        u = y.length ? [] : [g.isXAxis ? i.yAxis[0] : i.xAxis[0]];
        n(y, function (a) {
            Oa(a, u) === -1 && u.push(a)
        });
        f = p(f, g.translate(b, null, null, d));
        isNaN(f) || (g.horiz ? n(u, function (a) {
            var b;
            o = a.pos;
            r = o + a.len;
            m = q = w(f + g.transB);
            if (m < k || m > k + g.width)e ? m = q = B(v(k, m), k + g.width) : b = !0;
            b || x.push("M", m, o, "L", q, r)
        }) : n(u, function (a) {
            var b;
            m = a.pos;
            q = m + a.len;
            o = r = w(l + g.height - f);
            if (o < l || o > l + g.height)e ?
                o = r = B(v(l, o), g.top + g.height) : b = !0;
            b || x.push("M", m, o, "L", q, r)
        }));
        return x.length > 0 ? j.crispPolyLine(x, c || 1) : null
    });
    O.prototype.getPlotBandPath = function (a, b) {
        var c = this.getPlotLinePath(b, null, null, !0), d = this.getPlotLinePath(a, null, null, !0), e = [], f;
        if (d && c)for (f = 0; f < d.length; f += 6)e.push("M", d[f + 1], d[f + 2], "L", d[f + 4], d[f + 5], c[f + 4], c[f + 5], c[f + 1], c[f + 2]); else e = null;
        return e
    };
    na.prototype.crispPolyLine = function (a, b) {
        var c;
        for (c = 0; c < a.length; c += 6)a[c + 1] === a[c + 4] && (a[c + 1] = a[c + 4] = w(a[c + 1]) - b % 2 / 2), a[c + 2] === a[c +
        5] && (a[c + 2] = a[c + 5] = w(a[c + 2]) + b % 2 / 2);
        return a
    };
    if (Wa === z.VMLRenderer)hb.prototype.crispPolyLine = na.prototype.crispPolyLine;
    S(O.prototype, "hideCrosshair", function (a, b) {
        a.call(this, b);
        s(this.crossLabelArray) && (s(b) ? this.crossLabelArray[b] && this.crossLabelArray[b].hide() : n(this.crossLabelArray, function (a) {
            a.hide()
        }))
    });
    S(O.prototype, "drawCrosshair", function (a, b, c) {
        var d, e;
        a.call(this, b, c);
        if (s(this.crosshair.label) && this.crosshair.label.enabled && s(c)) {
            var a = this.chart, f = this.options.crosshair.label, g =
                this.isXAxis ? "x" : "y", b = this.horiz, h = this.opposite, i = this.left, j = this.top, k = this.crossLabel, l, m, n = f.format, q = "";
            if (!k)k = this.crossLabel = a.renderer.label().attr({
                align: f.align || (b ? "center" : h ? this.labelAlign === "right" ? "right" : "left" : this.labelAlign === "left" ? "left" : "center"),
                zIndex: 12,
                height: b ? 16 : r,
                fill: f.backgroundColor || this.series[0] && this.series[0].color || "gray",
                padding: p(f.padding, 2),
                stroke: f.borderColor || null,
                "stroke-width": f.borderWidth || 0
            }).css(x({
                color: "white", fontWeight: "normal", fontSize: "11px",
                textAlign: "center"
            }, f.style)).add();
            b ? (l = c.plotX + i, m = j + (h ? 0 : this.height)) : (l = h ? this.width + i : 0, m = c.plotY + j);
            if (m < j || m > j + this.height)this.hideCrosshair(); else {
                !n && !f.formatter && (this.isDatetimeAxis && (q = "%b %d, %Y"), n = "{value" + (q ? ":" + q : "") + "}");
                k.attr({
                    text: n ? Ma(n, {value: c[g]}) : f.formatter.call(this, c[g]),
                    x: l,
                    y: m,
                    visibility: "visible"
                });
                c = k.getBBox();
                if (b) {
                    if (this.options.tickPosition === "inside" && !h || this.options.tickPosition !== "inside" && h)m = k.y - c.height
                } else m = k.y - c.height / 2;
                b ? (d = i - c.x, e = i + this.width -
                    c.x) : (d = this.labelAlign === "left" ? i : 0, e = this.labelAlign === "right" ? i + this.width : a.chartWidth);
                k.translateX < d && (l += d - k.translateX);
                k.translateX + c.width >= e && (l -= k.translateX + c.width - e);
                k.attr({x: l, y: m, visibility: "visible"})
            }
        }
    });
    var lc = ca.init, mc = ca.processData, nc = Ba.prototype.tooltipFormatter;
    ca.init = function () {
        lc.apply(this, arguments);
        this.setCompare(this.options.compare)
    };
    ca.setCompare = function (a) {
        this.modifyValue = a === "value" || a === "percent" ? function (b, c) {
            var d = this.compareValue;
            if (b !== r && (b = a === "value" ?
                b - d : b = 100 * (b / d) - 100, c))c.change = b;
            return b
        } : null;
        if (this.chart.hasRendered)this.isDirty = !0
    };
    ca.processData = function () {
        var a = 0, b, c, d;
        mc.apply(this, arguments);
        if (this.xAxis && this.processedYData) {
            b = this.processedXData;
            c = this.processedYData;
            for (d = c.length; a < d; a++)if (typeof c[a] === "number" && b[a] >= this.xAxis.min) {
                this.compareValue = c[a];
                break
            }
        }
    };
    S(ca, "getExtremes", function (a) {
        a.apply(this, [].slice.call(arguments, 1));
        if (this.modifyValue)this.dataMax = this.modifyValue(this.dataMax), this.dataMin = this.modifyValue(this.dataMin)
    });
    O.prototype.setCompare = function (a, b) {
        this.isXAxis || (n(this.series, function (b) {
            b.setCompare(a)
        }), p(b, !0) && this.chart.redraw())
    };
    Ba.prototype.tooltipFormatter = function (a) {
        a = a.replace("{point.change}", (this.change > 0 ? "+" : "") + z.numberFormat(this.change, p(this.series.tooltipOptions.changeDecimals, 2)));
        return nc.apply(this, [a])
    };
    S(P.prototype, "render", function (a) {
        if (this.chart.options._stock)!this.clipBox && this.animate && this.animate.toString().indexOf("sharedClip") !== -1 ? (this.clipBox = y(this.chart.clipBox),
            this.clipBox.width = this.xAxis.len, this.clipBox.height = this.yAxis.len) : this.chart[this.sharedClipKey] && this.chart[this.sharedClipKey].attr({
            width: this.xAxis.len,
            height: this.yAxis.len
        });
        a.call(this)
    });
    x(z, {
        Color: wa,
        Point: Ba,
        Tick: Za,
        Renderer: Wa,
        SVGElement: $,
        SVGRenderer: na,
        arrayMin: Sa,
        arrayMax: Ea,
        charts: ha,
        dateFormat: ka,
        error: qa,
        format: Ma,
        pathAnim: Ib,
        getOptions: function () {
            return F
        },
        hasBidiBug: Wb,
        isTouchDevice: eb,
        setOptions: function (a) {
            F = y(!0, F, a);
            Nb();
            return F
        },
        addEvent: D,
        removeEvent: U,
        createElement: aa,
        discardElement: Ta,
        css: M,
        each: n,
        map: za,
        merge: y,
        splat: pa,
        extendClass: ja,
        pInt: C,
        svg: da,
        canvas: ma,
        vml: !da && !ma,
        product: "Highstock",
        version: "2.1.2"
    })
})();
/*
 Highcharts JS v4.1.3 (2015-02-27)

 (c) 2009-2014 Torstein Honsi

 License: www.highcharts.com/license
 */
(function (k, D) {
    function K(a, b, c) {
        this.init.call(this, a, b, c)
    }

    var P = k.arrayMin, Q = k.arrayMax, u = k.each, H = k.extend, o = k.merge, R = k.map, q = k.pick, x = k.pInt, p = k.getOptions().plotOptions, g = k.seriesTypes, v = k.extendClass, L = k.splat, r = k.wrap, M = k.Axis, y = k.Tick, I = k.Point, S = k.Pointer, T = k.CenteredSeriesMixin, z = k.TrackerMixin, s = k.Series, w = Math, E = w.round, B = w.floor, N = w.max, U = k.Color, t = function () {
    };
    H(K.prototype, {
        init: function (a, b, c) {
            var d = this, e = d.defaultOptions;
            d.chart = b;
            if (b.angular)e.background = {};
            d.options = a = o(e, a);
            (a = a.background) && u([].concat(L(a)).reverse(), function (a) {
                var h = a.backgroundColor, b = c.userOptions, a = o(d.defaultBackgroundOptions, a);
                if (h)a.backgroundColor = h;
                a.color = a.backgroundColor;
                c.options.plotBands.unshift(a);
                b.plotBands = b.plotBands || [];
                b.plotBands.unshift(a)
            })
        }, defaultOptions: {center: ["50%", "50%"], size: "85%", startAngle: 0}, defaultBackgroundOptions: {
            shape: "circle",
            borderWidth: 1,
            borderColor: "silver",
            backgroundColor: {linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1}, stops: [[0, "#FFF"], [1, "#DDD"]]},
            from: -Number.MAX_VALUE,
            innerRadius: 0,
            to: Number.MAX_VALUE,
            outerRadius: "105%"
        }
    });
    var G = M.prototype, y = y.prototype, V = {
        getOffset: t, redraw: function () {
            this.isDirty = !1
        }, render: function () {
            this.isDirty = !1
        }, setScale: t, setCategories: t, setTitle: t
    }, O = {
        isRadial: !0,
        defaultRadialGaugeOptions: {
            labels: {align: "center", x: 0, y: null},
            minorGridLineWidth: 0,
            minorTickInterval: "auto",
            minorTickLength: 10,
            minorTickPosition: "inside",
            minorTickWidth: 1,
            tickLength: 10,
            tickPosition: "inside",
            tickWidth: 2,
            title: {rotation: 0},
            zIndex: 2
        },
        defaultRadialXOptions: {
            gridLineWidth: 1,
            labels: {align: null, distance: 15, x: 0, y: null},
            maxPadding: 0,
            minPadding: 0,
            showLastLabel: !1,
            tickLength: 0
        },
        defaultRadialYOptions: {
            gridLineInterpolation: "circle",
            labels: {align: "right", x: -3, y: -2},
            showLastLabel: !1,
            title: {x: 4, text: null, rotation: 90}
        },
        setOptions: function (a) {
            a = this.options = o(this.defaultOptions, this.defaultRadialOptions, a);
            if (!a.plotBands)a.plotBands = []
        },
        getOffset: function () {
            G.getOffset.call(this);
            this.chart.axisOffset[this.side] = 0;
            this.center = this.pane.center = T.getCenter.call(this.pane)
        },
        getLinePath: function (a,
                               b) {
            var c = this.center, b = q(b, c[2] / 2 - this.offset);
            return this.chart.renderer.symbols.arc(this.left + c[0], this.top + c[1], b, b, {
                start: this.startAngleRad,
                end: this.endAngleRad,
                open: !0,
                innerR: 0
            })
        },
        setAxisTranslation: function () {
            G.setAxisTranslation.call(this);
            if (this.center)this.transA = this.isCircular ? (this.endAngleRad - this.startAngleRad) / (this.max - this.min || 1) : this.center[2] / 2 / (this.max - this.min || 1), this.minPixelPadding = this.isXAxis ? this.transA * this.minPointOffset : 0
        },
        beforeSetTickPositions: function () {
            this.autoConnect &&
            (this.max += this.categories && 1 || this.pointRange || this.closestPointRange || 0)
        },
        setAxisSize: function () {
            G.setAxisSize.call(this);
            if (this.isRadial) {
                this.center = this.pane.center = k.CenteredSeriesMixin.getCenter.call(this.pane);
                if (this.isCircular)this.sector = this.endAngleRad - this.startAngleRad;
                this.len = this.width = this.height = this.center[2] * q(this.sector, 1) / 2
            }
        },
        getPosition: function (a, b) {
            return this.postTranslate(this.isCircular ? this.translate(a) : 0, q(this.isCircular ? b : this.translate(a), this.center[2] / 2) - this.offset)
        },
        postTranslate: function (a, b) {
            var c = this.chart, d = this.center, a = this.startAngleRad + a;
            return {x: c.plotLeft + d[0] + Math.cos(a) * b, y: c.plotTop + d[1] + Math.sin(a) * b}
        },
        getPlotBandPath: function (a, b, c) {
            var d = this.center, e = this.startAngleRad, f = d[2] / 2, h = [q(c.outerRadius, "100%"), c.innerRadius, q(c.thickness, 10)], i = /%$/, l, m = this.isCircular;
            this.options.gridLineInterpolation === "polygon" ? d = this.getPlotLinePath(a).concat(this.getPlotLinePath(b, !0)) : (a = Math.max(a, this.min), b = Math.min(b, this.max), m || (h[0] = this.translate(a),
                h[1] = this.translate(b)), h = R(h, function (a) {
                i.test(a) && (a = x(a, 10) * f / 100);
                return a
            }), c.shape === "circle" || !m ? (a = -Math.PI / 2, b = Math.PI * 1.5, l = !0) : (a = e + this.translate(a), b = e + this.translate(b)), d = this.chart.renderer.symbols.arc(this.left + d[0], this.top + d[1], h[0], h[0], {
                start: Math.min(a, b),
                end: Math.max(a, b),
                innerR: q(h[1], h[0] - h[2]),
                open: l
            }));
            return d
        },
        getPlotLinePath: function (a, b) {
            var c = this, d = c.center, e = c.chart, f = c.getPosition(a), h, i, l;
            c.isCircular ? l = ["M", d[0] + e.plotLeft, d[1] + e.plotTop, "L", f.x, f.y] : c.options.gridLineInterpolation ===
            "circle" ? (a = c.translate(a)) && (l = c.getLinePath(0, a)) : (u(e.xAxis, function (a) {
                a.pane === c.pane && (h = a)
            }), l = [], a = c.translate(a), d = h.tickPositions, h.autoConnect && (d = d.concat([d[0]])), b && (d = [].concat(d).reverse()), u(d, function (f, c) {
                i = h.getPosition(f, a);
                l.push(c ? "L" : "M", i.x, i.y)
            }));
            return l
        },
        getTitlePosition: function () {
            var a = this.center, b = this.chart, c = this.options.title;
            return {
                x: b.plotLeft + a[0] + (c.x || 0),
                y: b.plotTop + a[1] - {high: 0.5, middle: 0.25, low: 0}[c.align] * a[2] + (c.y || 0)
            }
        }
    };
    r(G, "init", function (a, b, c) {
        var j;
        var d = b.angular, e = b.polar, f = c.isX, h = d && f, i, l;
        l = b.options;
        var m = c.pane || 0;
        if (d) {
            if (H(this, h ? V : O), i = !f)this.defaultRadialOptions = this.defaultRadialGaugeOptions
        } else if (e)H(this, O), this.defaultRadialOptions = (i = f) ? this.defaultRadialXOptions : o(this.defaultYAxisOptions, this.defaultRadialYOptions);
        a.call(this, b, c);
        if (!h && (d || e)) {
            a = this.options;
            if (!b.panes)b.panes = [];
            this.pane = (j = b.panes[m] = b.panes[m] || new K(L(l.pane)[m], b, this), m = j);
            m = m.options;
            b.inverted = !1;
            l.chart.zoomType = null;
            this.startAngleRad = b = (m.startAngle -
                90) * Math.PI / 180;
            this.endAngleRad = l = (q(m.endAngle, m.startAngle + 360) - 90) * Math.PI / 180;
            this.offset = a.offset || 0;
            if ((this.isCircular = i) && c.max === D && l - b === 2 * Math.PI)this.autoConnect = !0
        }
    });
    r(y, "getPosition", function (a, b, c, d, e) {
        var f = this.axis;
        return f.getPosition ? f.getPosition(c) : a.call(this, b, c, d, e)
    });
    r(y, "getLabelPosition", function (a, b, c, d, e, f, h, i, l) {
        var m = this.axis, j = f.y, n = 20, g = f.align, A = (m.translate(this.pos) + m.startAngleRad + Math.PI / 2) / Math.PI * 180 % 360;
        m.isRadial ? (a = m.getPosition(this.pos, m.center[2] /
            2 + q(f.distance, -25)), f.rotation === "auto" ? d.attr({rotation: A}) : j === null && (j = m.chart.renderer.fontMetrics(d.styles.fontSize).b - d.getBBox().height / 2), g === null && (m.isCircular ? (this.label.getBBox().width > m.len * m.tickInterval / (m.max - m.min) && (n = 0), g = A > n && A < 180 - n ? "left" : A > 180 + n && A < 360 - n ? "right" : "center") : g = "center", d.attr({align: g})), a.x += f.x, a.y += j) : a = a.call(this, b, c, d, e, f, h, i, l);
        return a
    });
    r(y, "getMarkPath", function (a, b, c, d, e, f, h) {
        var i = this.axis;
        i.isRadial ? (a = i.getPosition(this.pos, i.center[2] / 2 + d), b =
            ["M", b, c, "L", a.x, a.y]) : b = a.call(this, b, c, d, e, f, h);
        return b
    });
    p.arearange = o(p.area, {
        lineWidth: 1,
        marker: null,
        threshold: null,
        tooltip: {pointFormat: '<span style="color:{series.color}">●</span> {series.name}: <b>{point.low}</b> - <b>{point.high}</b><br/>'},
        trackByArea: !0,
        dataLabels: {align: null, verticalAlign: null, xLow: 0, xHigh: 0, yLow: 0, yHigh: 0},
        states: {hover: {halo: !1}}
    });
    g.arearange = v(g.area, {
        type: "arearange", pointArrayMap: ["low", "high"], toYData: function (a) {
            return [a.low, a.high]
        }, pointValKey: "low", deferTranslatePolar: !0,
        highToXY: function (a) {
            var b = this.chart, c = this.xAxis.postTranslate(a.rectPlotX, this.yAxis.len - a.plotHigh);
            a.plotHighX = c.x - b.plotLeft;
            a.plotHigh = c.y - b.plotTop
        }, getSegments: function () {
            var a = this;
            u(a.points, function (b) {
                if (!a.options.connectNulls && (b.low === null || b.high === null))b.y = null; else if (b.low === null && b.high !== null)b.y = b.high
            });
            s.prototype.getSegments.call(this)
        }, translate: function () {
            var a = this, b = a.yAxis;
            g.area.prototype.translate.apply(a);
            u(a.points, function (a) {
                var d = a.low, e = a.high, f = a.plotY;
                e ===
                null && d === null ? a.y = null : d === null ? (a.plotLow = a.plotY = null, a.plotHigh = b.translate(e, 0, 1, 0, 1)) : e === null ? (a.plotLow = f, a.plotHigh = null) : (a.plotLow = f, a.plotHigh = b.translate(e, 0, 1, 0, 1))
            });
            this.chart.polar && u(this.points, function (c) {
                a.highToXY(c)
            })
        }, getSegmentPath: function (a) {
            var b, c = [], d = a.length, e = s.prototype.getSegmentPath, f, h;
            h = this.options;
            var i = h.step;
            for (b = HighchartsAdapter.grep(a, function (a) {
                return a.plotLow !== null
            }); d--;)f = a[d], f.plotHigh !== null && c.push({plotX: f.plotHighX || f.plotX, plotY: f.plotHigh});
            a = e.call(this, b);
            if (i)i === !0 && (i = "left"), h.step = {left: "right", center: "center", right: "left"}[i];
            c = e.call(this, c);
            h.step = i;
            h = [].concat(a, c);
            this.chart.polar || (c[0] = "L");
            this.areaPath = this.areaPath.concat(a, c);
            return h
        }, drawDataLabels: function () {
            var a = this.data, b = a.length, c, d = [], e = s.prototype, f = this.options.dataLabels, h = f.align, i, l = this.chart.inverted;
            if (f.enabled || this._hasPointLabels) {
                for (c = b; c--;)if (i = a[c], i.y = i.high, i._plotY = i.plotY, i.plotY = i.plotHigh, d[c] = i.dataLabel, i.dataLabel = i.dataLabelUpper,
                        i.below = !1, l) {
                    if (!h)f.align = "left";
                    f.x = f.xHigh
                } else f.y = f.yHigh;
                e.drawDataLabels && e.drawDataLabels.apply(this, arguments);
                for (c = b; c--;)if (i = a[c], i.dataLabelUpper = i.dataLabel, i.dataLabel = d[c], i.y = i.low, i.plotY = i._plotY, i.below = !0, l) {
                    if (!h)f.align = "right";
                    f.x = f.xLow
                } else f.y = f.yLow;
                e.drawDataLabels && e.drawDataLabels.apply(this, arguments)
            }
            f.align = h
        }, alignDataLabel: function () {
            g.column.prototype.alignDataLabel.apply(this, arguments)
        }, setStackedPoints: t, getSymbol: t, drawPoints: t
    });
    p.areasplinerange = o(p.arearange);
    g.areasplinerange = v(g.arearange, {type: "areasplinerange", getPointSpline: g.spline.prototype.getPointSpline});
    (function () {
        var a = g.column.prototype;
        p.columnrange = o(p.column, p.arearange, {lineWidth: 1, pointRange: null});
        g.columnrange = v(g.arearange, {
            type: "columnrange",
            translate: function () {
                var b = this, c = b.yAxis, d;
                a.translate.apply(b);
                u(b.points, function (a) {
                    var f = a.shapeArgs, h = b.options.minPointLength, i;
                    a.tooltipPos = null;
                    a.plotHigh = d = c.translate(a.high, 0, 1, 0, 1);
                    a.plotLow = a.plotY;
                    i = d;
                    a = a.plotY - d;
                    a < h && (h -= a, a +=
                        h, i -= h / 2);
                    f.height = a;
                    f.y = i
                })
            },
            trackerGroups: ["group", "dataLabelsGroup"],
            drawGraph: t,
            pointAttrToOptions: a.pointAttrToOptions,
            drawPoints: a.drawPoints,
            drawTracker: a.drawTracker,
            animate: a.animate,
            getColumnMetrics: a.getColumnMetrics
        })
    })();
    p.gauge = o(p.line, {
        dataLabels: {
            enabled: !0,
            defer: !1,
            y: 15,
            borderWidth: 1,
            borderColor: "silver",
            borderRadius: 3,
            crop: !1,
            verticalAlign: "top",
            zIndex: 2
        }, dial: {}, pivot: {}, tooltip: {headerFormat: ""}, showInLegend: !1
    });
    z = {
        type: "gauge",
        pointClass: v(I, {
            setState: function (a) {
                this.state =
                    a
            }
        }),
        angular: !0,
        drawGraph: t,
        fixedBox: !0,
        forceDL: !0,
        trackerGroups: ["group", "dataLabelsGroup"],
        translate: function () {
            var a = this.yAxis, b = this.options, c = a.center;
            this.generatePoints();
            u(this.points, function (d) {
                var e = o(b.dial, d.dial), f = x(q(e.radius, 80)) * c[2] / 200, h = x(q(e.baseLength, 70)) * f / 100, i = x(q(e.rearLength, 10)) * f / 100, l = e.baseWidth || 3, m = e.topWidth || 1, j = b.overshoot, n = a.startAngleRad + a.translate(d.y, null, null, null, !0);
                j && typeof j === "number" ? (j = j / 180 * Math.PI, n = Math.max(a.startAngleRad - j, Math.min(a.endAngleRad +
                    j, n))) : b.wrap === !1 && (n = Math.max(a.startAngleRad, Math.min(a.endAngleRad, n)));
                n = n * 180 / Math.PI;
                d.shapeType = "path";
                d.shapeArgs = {
                    d: e.path || ["M", -i, -l / 2, "L", h, -l / 2, f, -m / 2, f, m / 2, h, l / 2, -i, l / 2, "z"],
                    translateX: c[0],
                    translateY: c[1],
                    rotation: n
                };
                d.plotX = c[0];
                d.plotY = c[1]
            })
        },
        drawPoints: function () {
            var a = this, b = a.yAxis.center, c = a.pivot, d = a.options, e = d.pivot, f = a.chart.renderer;
            u(a.points, function (c) {
                var b = c.graphic, e = c.shapeArgs, m = e.d, j = o(d.dial, c.dial);
                b ? (b.animate(e), e.d = m) : c.graphic = f[c.shapeType](e).attr({
                    stroke: j.borderColor ||
                    "none", "stroke-width": j.borderWidth || 0, fill: j.backgroundColor || "black", rotation: e.rotation
                }).add(a.group)
            });
            c ? c.animate({
                translateX: b[0],
                translateY: b[1]
            }) : a.pivot = f.circle(0, 0, q(e.radius, 5)).attr({
                "stroke-width": e.borderWidth || 0,
                stroke: e.borderColor || "silver",
                fill: e.backgroundColor || "black"
            }).translate(b[0], b[1]).add(a.group)
        },
        animate: function (a) {
            var b = this;
            if (!a)u(b.points, function (a) {
                var d = a.graphic;
                d && (d.attr({rotation: b.yAxis.startAngleRad * 180 / Math.PI}), d.animate({rotation: a.shapeArgs.rotation},
                    b.options.animation))
            }), b.animate = null
        },
        render: function () {
            this.group = this.plotGroup("group", "series", this.visible ? "visible" : "hidden", this.options.zIndex, this.chart.seriesGroup);
            s.prototype.render.call(this);
            this.group.clip(this.chart.clipRect)
        },
        setData: function (a, b) {
            s.prototype.setData.call(this, a, !1);
            this.processData();
            this.generatePoints();
            q(b, !0) && this.chart.redraw()
        },
        drawTracker: z && z.drawTrackerPoint
    };
    g.gauge = v(g.line, z);
    p.boxplot = o(p.column, {
        fillColor: "#FFFFFF",
        lineWidth: 1,
        medianWidth: 2,
        states: {hover: {brightness: -0.3}},
        threshold: null,
        tooltip: {pointFormat: '<span style="color:{point.color}">●</span> <b> {series.name}</b><br/>Maximum: {point.high}<br/>Upper quartile: {point.q3}<br/>Median: {point.median}<br/>Lower quartile: {point.q1}<br/>Minimum: {point.low}<br/>'},
        whiskerLength: "50%",
        whiskerWidth: 2
    });
    g.boxplot = v(g.column, {
        type: "boxplot", pointArrayMap: ["low", "q1", "median", "q3", "high"], toYData: function (a) {
            return [a.low, a.q1, a.median, a.q3, a.high]
        }, pointValKey: "high", pointAttrToOptions: {
            fill: "fillColor", stroke: "color",
            "stroke-width": "lineWidth"
        }, drawDataLabels: t, translate: function () {
            var a = this.yAxis, b = this.pointArrayMap;
            g.column.prototype.translate.apply(this);
            u(this.points, function (c) {
                u(b, function (b) {
                    c[b] !== null && (c[b + "Plot"] = a.translate(c[b], 0, 1, 0, 1))
                })
            })
        }, drawPoints: function () {
            var a = this, b = a.points, c = a.options, d = a.chart.renderer, e, f, h, i, l, m, j, n, g, A, k, J, p, o, r, t, v, s, w, x, z, y, F = a.doQuartiles !== !1, C = parseInt(a.options.whiskerLength, 10) / 100;
            u(b, function (b) {
                g = b.graphic;
                z = b.shapeArgs;
                k = {};
                o = {};
                t = {};
                y = b.color || a.color;
                if (b.plotY !== D)if (e = b.pointAttr[b.selected ? "selected" : ""], v = z.width, s = B(z.x), w = s + v, x = E(v / 2), f = B(F ? b.q1Plot : b.lowPlot), h = B(F ? b.q3Plot : b.lowPlot), i = B(b.highPlot), l = B(b.lowPlot), k.stroke = b.stemColor || c.stemColor || y, k["stroke-width"] = q(b.stemWidth, c.stemWidth, c.lineWidth), k.dashstyle = b.stemDashStyle || c.stemDashStyle, o.stroke = b.whiskerColor || c.whiskerColor || y, o["stroke-width"] = q(b.whiskerWidth, c.whiskerWidth, c.lineWidth), t.stroke = b.medianColor || c.medianColor || y, t["stroke-width"] = q(b.medianWidth, c.medianWidth,
                        c.lineWidth), j = k["stroke-width"] % 2 / 2, n = s + x + j, A = ["M", n, h, "L", n, i, "M", n, f, "L", n, l], F && (j = e["stroke-width"] % 2 / 2, n = B(n) + j, f = B(f) + j, h = B(h) + j, s += j, w += j, J = ["M", s, h, "L", s, f, "L", w, f, "L", w, h, "L", s, h, "z"]), C && (j = o["stroke-width"] % 2 / 2, i += j, l += j, p = ["M", n - x * C, i, "L", n + x * C, i, "M", n - x * C, l, "L", n + x * C, l]), j = t["stroke-width"] % 2 / 2, m = E(b.medianPlot) + j, r = ["M", s, m, "L", w, m], g)b.stem.animate({d: A}), C && b.whiskers.animate({d: p}), F && b.box.animate({d: J}), b.medianShape.animate({d: r}); else {
                    b.graphic = g = d.g().add(a.group);
                    b.stem = d.path(A).attr(k).add(g);
                    if (C)b.whiskers = d.path(p).attr(o).add(g);
                    if (F)b.box = d.path(J).attr(e).add(g);
                    b.medianShape = d.path(r).attr(t).add(g)
                }
            })
        }
    });
    p.errorbar = o(p.boxplot, {
        color: "#000000",
        grouping: !1,
        linkedTo: ":previous",
        tooltip: {pointFormat: '<span style="color:{point.color}">●</span> {series.name}: <b>{point.low}</b> - <b>{point.high}</b><br/>'},
        whiskerWidth: null
    });
    g.errorbar = v(g.boxplot, {
        type: "errorbar", pointArrayMap: ["low", "high"], toYData: function (a) {
            return [a.low, a.high]
        }, pointValKey: "high", doQuartiles: !1, drawDataLabels: g.arearange ?
            g.arearange.prototype.drawDataLabels : t, getColumnMetrics: function () {
            return this.linkedParent && this.linkedParent.columnMetrics || g.column.prototype.getColumnMetrics.call(this)
        }
    });
    p.waterfall = o(p.column, {
        lineWidth: 1,
        lineColor: "#333",
        dashStyle: "dot",
        borderColor: "#333",
        dataLabels: {inside: !0},
        states: {hover: {lineWidthPlus: 0}}
    });
    g.waterfall = v(g.column, {
        type: "waterfall", upColorProp: "fill", pointArrayMap: ["low", "y"], pointValKey: "y", translate: function () {
            var a = this.options, b = this.yAxis, c, d, e, f, h, i, l, m, j, n = a.threshold,
                k = a.stacking;
            g.column.prototype.translate.apply(this);
            l = m = n;
            d = this.points;
            for (c = 0, a = d.length; c < a; c++) {
                e = d[c];
                i = this.processedYData[c];
                f = e.shapeArgs;
                j = (h = k && b.stacks[(this.negStacks && i < n ? "-" : "") + this.stackKey]) ? h[e.x].points[this.index + "," + c] : [0, i];
                if (e.isSum)e.y = i; else if (e.isIntermediateSum)e.y = i - m;
                h = N(l, l + e.y) + j[0];
                f.y = b.translate(h, 0, 1);
                if (e.isSum)f.y = b.translate(j[1], 0, 1), f.height = b.translate(j[0], 0, 1) - f.y; else if (e.isIntermediateSum)f.y = b.translate(j[1], 0, 1), f.height = b.translate(m, 0, 1) - f.y, m =
                    j[1]; else {
                    if (l !== 0)f.height = i > 0 ? b.translate(l, 0, 1) - f.y : b.translate(l, 0, 1) - b.translate(l - i, 0, 1);
                    l += i
                }
                e.plotY = f.y = E(f.y) - this.borderWidth % 2 / 2;
                f.height = N(E(f.height), 0.001);
                e.yBottom = f.y + f.height;
                f = e.plotY + (e.negative ? f.height : 0);
                this.chart.inverted ? e.tooltipPos[0] = b.len - f : e.tooltipPos[1] = f
            }
        }, processData: function (a) {
            var b = this.yData, c = this.options.data, d, e = b.length, f, h, i, l, m, j;
            h = f = i = l = this.options.threshold || 0;
            for (j = 0; j < e; j++)m = b[j], d = c && c[j] ? c[j] : {}, m === "sum" || d.isSum ? b[j] = h : m === "intermediateSum" ||
            d.isIntermediateSum ? b[j] = f : (h += m, f += m), i = Math.min(h, i), l = Math.max(h, l);
            s.prototype.processData.call(this, a);
            this.dataMin = i;
            this.dataMax = l
        }, toYData: function (a) {
            if (a.isSum)return a.x === 0 ? null : "sum"; else if (a.isIntermediateSum)return a.x === 0 ? null : "intermediateSum";
            return a.y
        }, getAttribs: function () {
            g.column.prototype.getAttribs.apply(this, arguments);
            var a = this, b = a.options, c = b.states, d = b.upColor || a.color, b = k.Color(d).brighten(0.1).get(), e = o(a.pointAttr), f = a.upColorProp;
            e[""][f] = d;
            e.hover[f] = c.hover.upColor ||
                b;
            e.select[f] = c.select.upColor || d;
            u(a.points, function (b) {
                if (!b.options.color)b.y > 0 ? (b.pointAttr = e, b.color = d) : b.pointAttr = a.pointAttr
            })
        }, getGraphPath: function () {
            var a = this.data, b = a.length, c = E(this.options.lineWidth + this.borderWidth) % 2 / 2, d = [], e, f, h;
            for (h = 1; h < b; h++)f = a[h].shapeArgs, e = a[h - 1].shapeArgs, f = ["M", e.x + e.width, e.y + c, "L", f.x, e.y + c], a[h - 1].y < 0 && (f[2] += e.height, f[5] += e.height), d = d.concat(f);
            return d
        }, getExtremes: t, drawGraph: s.prototype.drawGraph
    });
    p.polygon = o(p.scatter, {marker: {enabled: !1}});
    g.polygon =
        v(g.scatter, {
            type: "polygon", fillGraph: !0, getSegmentPath: function (a) {
                return s.prototype.getSegmentPath.call(this, a).concat("z")
            }, drawGraph: s.prototype.drawGraph, drawLegendSymbol: k.LegendSymbolMixin.drawRectangle
        });
    p.bubble = o(p.scatter, {
        dataLabels: {
            formatter: function () {
                return this.point.z
            }, inside: !0, verticalAlign: "middle"
        },
        marker: {lineColor: null, lineWidth: 1},
        minSize: 8,
        maxSize: "20%",
        states: {hover: {halo: {size: 5}}},
        tooltip: {pointFormat: "({point.x}, {point.y}), Size: {point.z}"},
        turboThreshold: 0,
        zThreshold: 0,
        zoneAxis: "z"
    });
    z = v(I, {
        haloPath: function () {
            return I.prototype.haloPath.call(this, this.shapeArgs.r + this.series.options.states.hover.halo.size)
        }, ttBelow: !1
    });
    g.bubble = v(g.scatter, {
        type: "bubble",
        pointClass: z,
        pointArrayMap: ["y", "z"],
        parallelArrays: ["x", "y", "z"],
        trackerGroups: ["group", "dataLabelsGroup"],
        bubblePadding: !0,
        zoneAxis: "z",
        pointAttrToOptions: {stroke: "lineColor", "stroke-width": "lineWidth", fill: "fillColor"},
        applyOpacity: function (a) {
            var b = this.options.marker, c = q(b.fillOpacity, 0.5), a = a || b.fillColor ||
                this.color;
            c !== 1 && (a = U(a).setOpacity(c).get("rgba"));
            return a
        },
        convertAttribs: function () {
            var a = s.prototype.convertAttribs.apply(this, arguments);
            a.fill = this.applyOpacity(a.fill);
            return a
        },
        getRadii: function (a, b, c, d) {
            var e, f, h, i = this.zData, l = [], m = this.options.sizeBy !== "width";
            for (f = 0, e = i.length; f < e; f++)h = b - a, h = h > 0 ? (i[f] - a) / (b - a) : 0.5, m && h >= 0 && (h = Math.sqrt(h)), l.push(w.ceil(c + h * (d - c)) / 2);
            this.radii = l
        },
        animate: function (a) {
            var b = this.options.animation;
            if (!a)u(this.points, function (a) {
                var d = a.graphic, a = a.shapeArgs;
                d && a && (d.attr("r", 1), d.animate({r: a.r}, b))
            }), this.animate = null
        },
        translate: function () {
            var a, b = this.data, c, d, e = this.radii;
            g.scatter.prototype.translate.call(this);
            for (a = b.length; a--;)c = b[a], d = e ? e[a] : 0, d >= this.minPxSize / 2 ? (c.shapeType = "circle", c.shapeArgs = {
                x: c.plotX,
                y: c.plotY,
                r: d
            }, c.dlBox = {
                x: c.plotX - d,
                y: c.plotY - d,
                width: 2 * d,
                height: 2 * d
            }) : c.shapeArgs = c.plotY = c.dlBox = D
        },
        drawLegendSymbol: function (a, b) {
            var c = x(a.itemStyle.fontSize) / 2;
            b.legendSymbol = this.chart.renderer.circle(c, a.baseline - c, c).attr({zIndex: 3}).add(b.legendGroup);
            b.legendSymbol.isMarker = !0
        },
        drawPoints: g.column.prototype.drawPoints,
        alignDataLabel: g.column.prototype.alignDataLabel,
        buildKDTree: t,
        applyZones: t
    });
    M.prototype.beforePadding = function () {
        var a = this, b = this.len, c = this.chart, d = 0, e = b, f = this.isXAxis, h = f ? "xData" : "yData", i = this.min, l = {}, m = w.min(c.plotWidth, c.plotHeight), j = Number.MAX_VALUE, n = -Number.MAX_VALUE, g = this.max - i, k = b / g, p = [];
        u(this.series, function (b) {
            var h = b.options;
            if (b.bubblePadding && (b.visible || !c.options.chart.ignoreHiddenSeries))if (a.allowZoomOutside = !0, p.push(b), f)u(["minSize", "maxSize"], function (a) {
                var b = h[a], f = /%$/.test(b), b = x(b);
                l[a] = f ? m * b / 100 : b
            }), b.minPxSize = l.minSize, b = b.zData, b.length && (j = q(h.zMin, w.min(j, w.max(P(b), h.displayNegative === !1 ? h.zThreshold : -Number.MAX_VALUE))), n = q(h.zMax, w.max(n, Q(b))))
        });
        u(p, function (a) {
            var b = a[h], c = b.length, m;
            f && a.getRadii(j, n, l.minSize, l.maxSize);
            if (g > 0)for (; c--;)typeof b[c] === "number" && (m = a.radii[c], d = Math.min((b[c] - i) * k - m, d), e = Math.max((b[c] - i) * k + m, e))
        });
        p.length && g > 0 && q(this.options.min, this.userMin) ===
        D && q(this.options.max, this.userMax) === D && (e -= b, k *= (b + d - e) / b, this.min += d / k, this.max += e / k)
    };
    (function () {
        function a(a, b, c) {
            a.call(this, b, c);
            if (this.chart.polar)this.closeSegment = function (a) {
                var b = this.xAxis.center;
                a.push("L", b[0], b[1])
            }, this.closedStacks = !0
        }

        function b(a, b) {
            var c = this.chart, d = this.options.animation, e = this.group, j = this.markerGroup, n = this.xAxis.center, g = c.plotLeft, k = c.plotTop;
            if (c.polar) {
                if (c.renderer.isSVG)d === !0 && (d = {}), b ? (c = {
                    translateX: n[0] + g,
                    translateY: n[1] + k,
                    scaleX: 0.001,
                    scaleY: 0.001
                },
                    e.attr(c), j && j.attr(c)) : (c = {
                    translateX: g,
                    translateY: k,
                    scaleX: 1,
                    scaleY: 1
                }, e.animate(c, d), j && j.animate(c, d), this.animate = null)
            } else a.call(this, b)
        }

        var c = s.prototype, d = S.prototype, e;
        c.searchPolarPoint = function (a) {
            var b = this.chart, c = this.xAxis.pane.center, d = a.chartX - c[0] - b.plotLeft, a = a.chartY - c[1] - b.plotTop;
            this.kdAxisArray = ["clientX"];
            a = {clientX: 180 + Math.atan2(d, a) * (-180 / Math.PI)};
            return this.searchKDTree(a)
        };
        r(c, "buildKDTree", function (a) {
            if (this.chart.polar)this.kdAxisArray = ["clientX"];
            a.apply(this)
        });
        r(c, "searchPoint", function (a, b) {
            return this.chart.polar ? this.searchPolarPoint(b) : a.call(this, b)
        });
        c.toXY = function (a) {
            var b, c = this.chart, d = a.plotX;
            b = a.plotY;
            a.rectPlotX = d;
            a.rectPlotY = b;
            d = (d / Math.PI * 180 + this.xAxis.pane.options.startAngle) % 360;
            d < 0 && (d += 360);
            a.clientX = d;
            b = this.xAxis.postTranslate(a.plotX, this.yAxis.len - b);
            a.plotX = a.polarPlotX = b.x - c.plotLeft;
            a.plotY = a.polarPlotY = b.y - c.plotTop
        };
        g.area && r(g.area.prototype, "init", a);
        g.areaspline && r(g.areaspline.prototype, "init", a);
        g.spline && r(g.spline.prototype,
            "getPointSpline", function (a, b, c, d) {
                var e, j, n, g, k, p, o;
                if (this.chart.polar) {
                    e = c.plotX;
                    j = c.plotY;
                    a = b[d - 1];
                    n = b[d + 1];
                    this.connectEnds && (a || (a = b[b.length - 2]), n || (n = b[1]));
                    if (a && n)g = a.plotX, k = a.plotY, b = n.plotX, p = n.plotY, g = (1.5 * e + g) / 2.5, k = (1.5 * j + k) / 2.5, n = (1.5 * e + b) / 2.5, o = (1.5 * j + p) / 2.5, b = Math.sqrt(Math.pow(g - e, 2) + Math.pow(k - j, 2)), p = Math.sqrt(Math.pow(n - e, 2) + Math.pow(o - j, 2)), g = Math.atan2(k - j, g - e), k = Math.atan2(o - j, n - e), o = Math.PI / 2 + (g + k) / 2, Math.abs(g - o) > Math.PI / 2 && (o -= Math.PI), g = e + Math.cos(o) * b, k = j + Math.sin(o) *
                        b, n = e + Math.cos(Math.PI + o) * p, o = j + Math.sin(Math.PI + o) * p, c.rightContX = n, c.rightContY = o;
                    d ? (c = ["C", a.rightContX || a.plotX, a.rightContY || a.plotY, g || e, k || j, e, j], a.rightContX = a.rightContY = null) : c = ["M", e, j]
                } else c = a.call(this, b, c, d);
                return c
            });
        r(c, "translate", function (a) {
            a.call(this);
            if (this.chart.polar && !this.preventPostTranslate)for (var a = this.points, b = a.length; b--;)this.toXY(a[b])
        });
        r(c, "getSegmentPath", function (a, b) {
            var c = this.points;
            if (this.chart.polar && this.options.connectEnds !== !1 && b[b.length - 1] === c[c.length -
                1] && c[0].y !== null)this.connectEnds = !0, b = [].concat(b, [c[0]]);
            return a.call(this, b)
        });
        r(c, "animate", b);
        if (g.column)e = g.column.prototype, r(e, "animate", b), r(e, "translate", function (a) {
            var b = this.xAxis, c = this.yAxis.len, d = b.center, e = b.startAngleRad, j = this.chart.renderer, g, k;
            this.preventPostTranslate = !0;
            a.call(this);
            if (b.isRadial) {
                b = this.points;
                for (k = b.length; k--;)g = b[k], a = g.barX + e, g.shapeType = "path", g.shapeArgs = {
                    d: j.symbols.arc(d[0], d[1], c - g.plotY, null, {
                        start: a,
                        end: a + g.pointWidth,
                        innerR: c - q(g.yBottom, c)
                    })
                },
                    this.toXY(g), g.tooltipPos = [g.plotX, g.plotY], g.ttBelow = g.plotY > d[1]
            }
        }), r(e, "alignDataLabel", function (a, b, d, e, g, j) {
            if (this.chart.polar) {
                a = b.rectPlotX / Math.PI * 180;
                if (e.align === null)e.align = a > 20 && a < 160 ? "left" : a > 200 && a < 340 ? "right" : "center";
                if (e.verticalAlign === null)e.verticalAlign = a < 45 || a > 315 ? "bottom" : a > 135 && a < 225 ? "top" : "middle";
                c.alignDataLabel.call(this, b, d, e, g, j)
            } else a.call(this, b, d, e, g, j)
        });
        r(d, "getCoordinates", function (a, b) {
            var c = this.chart, d = {xAxis: [], yAxis: []};
            c.polar ? u(c.axes, function (a) {
                var e =
                    a.isXAxis, f = a.center, g = b.chartX - f[0] - c.plotLeft, f = b.chartY - f[1] - c.plotTop;
                d[e ? "xAxis" : "yAxis"].push({
                    axis: a,
                    value: a.translate(e ? Math.PI - Math.atan2(g, f) : Math.sqrt(Math.pow(g, 2) + Math.pow(f, 2)), !0)
                })
            }) : d = a.call(this, b);
            return d
        })
    })()
})(Highcharts);
/*
 Highcharts JS v4.1.3 (2015-02-27)

 (c) 2009-2013 Torstein Hønsi

 License: www.highcharts.com/license
 */
(function (c) {
    function n(d, a, b) {
        var e, f, g = a.options.chart.options3d, h = !1;
        b ? (h = a.inverted, b = a.plotWidth / 2, a = a.plotHeight / 2, e = g.depth / 2, f = A(g.depth, 1) * A(g.viewDistance, 0)) : (b = a.plotLeft + a.plotWidth / 2, a = a.plotTop + a.plotHeight / 2, e = g.depth / 2, f = A(g.depth, 1) * A(g.viewDistance, 0));
        var j = [], i = b, k = a, v = e, p = f, b = y * (h ? g.beta : -g.beta), g = y * (h ? -g.alpha : g.alpha), o = l(b), s = m(b), t = l(g), u = m(g), w, x, r, n, q, z;
        c.each(d, function (a) {
            w = (h ? a.y : a.x) - i;
            x = (h ? a.x : a.y) - k;
            r = (a.z || 0) - v;
            n = s * w - o * r;
            q = -o * t * w - s * t * r + u * x;
            z = o * u * w + s * u * r + t * x;
            p > 0 &&
            p < Number.POSITIVE_INFINITY && (n *= p / (z + v + p), q *= p / (z + v + p));
            n += i;
            q += k;
            z += v;
            j.push({x: h ? q : n, y: h ? n : q, z: z})
        });
        return j
    }

    function q(d) {
        return d !== void 0 && d !== null
    }

    function E(d) {
        var a = 0, b, e;
        for (b = 0; b < d.length; b++)e = (b + 1) % d.length, a += d[b].x * d[e].y - d[e].x * d[b].y;
        return a / 2
    }

    function D(d) {
        var a = 0, b;
        for (b = 0; b < d.length; b++)a += d[b].z;
        return d.length ? a / d.length : 0
    }

    function r(d, a, b, e, f, g, c, j) {
        var i = [];
        return g > f && g - f > o / 2 + 1.0E-4 ? (i = i.concat(r(d, a, b, e, f, f + o / 2, c, j)), i = i.concat(r(d, a, b, e, f + o / 2, g, c, j))) : g < f && f - g > o / 2 + 1.0E-4 ?
            (i = i.concat(r(d, a, b, e, f, f - o / 2, c, j)), i = i.concat(r(d, a, b, e, f - o / 2, g, c, j))) : (i = g - f, ["C", d + b * m(f) - b * B * i * l(f) + c, a + e * l(f) + e * B * i * m(f) + j, d + b * m(g) + b * B * i * l(g) + c, a + e * l(g) - e * B * i * m(g) + j, d + b * m(g) + c, a + e * l(g) + j])
    }

    function F(d) {
        if (this.chart.is3d()) {
            var a = this.chart.options.plotOptions.column.grouping;
            a !== void 0 && !a && this.group.zIndex !== void 0 && this.group.attr({zIndex: this.group.zIndex * 10});
            var b = this.options, e = this.options.states;
            this.borderWidth = b.borderWidth = b.edgeWidth || 1;
            c.each(this.data, function (a) {
                if (a.y !==
                    null)a = a.pointAttr, this.borderColor = c.pick(b.edgeColor, a[""].fill), a[""].stroke = this.borderColor, a.hover.stroke = c.pick(e.hover.edgeColor, this.borderColor), a.select.stroke = c.pick(e.select.edgeColor, this.borderColor)
            })
        }
        d.apply(this, [].slice.call(arguments, 1))
    }

    var o = Math.PI, y = o / 180, l = Math.sin, m = Math.cos, A = c.pick, G = Math.round;
    c.perspective = n;
    var B = 4 * (Math.sqrt(2) - 1) / 3 / (o / 2);
    c.SVGRenderer.prototype.toLinePath = function (d, a) {
        var b = [];
        c.each(d, function (a) {
            b.push("L", a.x, a.y)
        });
        d.length && (b[0] = "M", a && b.push("Z"));
        return b
    };
    c.SVGRenderer.prototype.cuboid = function (d) {
        var a = this.g(), d = this.cuboidPath(d);
        a.front = this.path(d[0]).attr({zIndex: d[3], "stroke-linejoin": "round"}).add(a);
        a.top = this.path(d[1]).attr({zIndex: d[4], "stroke-linejoin": "round"}).add(a);
        a.side = this.path(d[2]).attr({zIndex: d[5], "stroke-linejoin": "round"}).add(a);
        a.fillSetter = function (a) {
            var d = c.Color(a).brighten(0.1).get(), f = c.Color(a).brighten(-0.1).get();
            this.front.attr({fill: a});
            this.top.attr({fill: d});
            this.side.attr({fill: f});
            this.color = a;
            return this
        };
        a.opacitySetter = function (a) {
            this.front.attr({opacity: a});
            this.top.attr({opacity: a});
            this.side.attr({opacity: a});
            return this
        };
        a.attr = function (a) {
            a.shapeArgs || q(a.x) ? (a = this.renderer.cuboidPath(a.shapeArgs || a), this.front.attr({
                d: a[0],
                zIndex: a[3]
            }), this.top.attr({d: a[1], zIndex: a[4]}), this.side.attr({
                d: a[2],
                zIndex: a[5]
            })) : c.SVGElement.prototype.attr.call(this, a);
            return this
        };
        a.animate = function (a, d, f) {
            q(a.x) && q(a.y) ? (a = this.renderer.cuboidPath(a), this.front.attr({zIndex: a[3]}).animate({d: a[0]},
                d, f), this.top.attr({zIndex: a[4]}).animate({d: a[1]}, d, f), this.side.attr({zIndex: a[5]}).animate({d: a[2]}, d, f)) : a.opacity ? (this.front.animate(a, d, f), this.top.animate(a, d, f), this.side.animate(a, d, f)) : c.SVGElement.prototype.animate.call(this, a, d, f);
            return this
        };
        a.destroy = function () {
            this.front.destroy();
            this.top.destroy();
            this.side.destroy();
            return null
        };
        a.attr({zIndex: -d[3]});
        return a
    };
    c.SVGRenderer.prototype.cuboidPath = function (d) {
        var a = d.x, b = d.y, e = d.z, f = d.height, g = d.width, h = d.depth, j = c.map, i = [{
            x: a, y: b,
            z: e
        }, {x: a + g, y: b, z: e}, {x: a + g, y: b + f, z: e}, {x: a, y: b + f, z: e}, {
            x: a,
            y: b + f,
            z: e + h
        }, {x: a + g, y: b + f, z: e + h}, {x: a + g, y: b, z: e + h}, {
            x: a,
            y: b,
            z: e + h
        }], i = n(i, c.charts[this.chartIndex], d.insidePlotArea), b = function (a, b) {
            a = j(a, function (a) {
                return i[a]
            });
            b = j(b, function (a) {
                return i[a]
            });
            return E(a) < 0 ? a : E(b) < 0 ? b : []
        }, d = b([3, 2, 1, 0], [7, 6, 5, 4]), a = b([1, 6, 7, 0], [4, 5, 2, 3]), b = b([1, 2, 5, 6], [0, 7, 4, 3]);
        return [this.toLinePath(d, !0), this.toLinePath(a, !0), this.toLinePath(b, !0), D(d), D(a), D(b)]
    };
    c.SVGRenderer.prototype.arc3d = function (d) {
        d.alpha *=
            y;
        d.beta *= y;
        var a = this.g(), b = this.arc3dPath(d), e = a.renderer, f = b.zTop * 100;
        a.shapeArgs = d;
        a.top = e.path(b.top).attr({zIndex: b.zTop}).add(a);
        a.side1 = e.path(b.side2).attr({zIndex: b.zSide1});
        a.side2 = e.path(b.side1).attr({zIndex: b.zSide2});
        a.inn = e.path(b.inn).attr({zIndex: b.zInn});
        a.out = e.path(b.out).attr({zIndex: b.zOut});
        a.fillSetter = function (a) {
            this.color = a;
            var b = c.Color(a).brighten(-0.1).get();
            this.side1.attr({fill: b});
            this.side2.attr({fill: b});
            this.inn.attr({fill: b});
            this.out.attr({fill: b});
            this.top.attr({fill: a});
            return this
        };
        a.translateXSetter = function (a) {
            this.out.attr({translateX: a});
            this.inn.attr({translateX: a});
            this.side1.attr({translateX: a});
            this.side2.attr({translateX: a});
            this.top.attr({translateX: a})
        };
        a.translateYSetter = function (a) {
            this.out.attr({translateY: a});
            this.inn.attr({translateY: a});
            this.side1.attr({translateY: a});
            this.side2.attr({translateY: a});
            this.top.attr({translateY: a})
        };
        a.animate = function (a, b, d) {
            q(a.end) || q(a.start) ? (this._shapeArgs = this.shapeArgs, c.SVGElement.prototype.animate.call(this,
                {_args: a}, {
                    duration: b, step: function () {
                        var a = arguments[1], b = a.elem, d = b._shapeArgs, e = a.end, a = a.pos, d = c.merge(d, {
                            x: d.x + (e.x - d.x) * a,
                            y: d.y + (e.y - d.y) * a,
                            r: d.r + (e.r - d.r) * a,
                            innerR: d.innerR + (e.innerR - d.innerR) * a,
                            start: d.start + (e.start - d.start) * a,
                            end: d.end + (e.end - d.end) * a
                        }), e = b.renderer.arc3dPath(d);
                        b.shapeArgs = d;
                        b.top.attr({d: e.top, zIndex: e.zTop});
                        b.inn.attr({d: e.inn, zIndex: e.zInn});
                        b.out.attr({d: e.out, zIndex: e.zOut});
                        b.side1.attr({d: e.side1, zIndex: e.zSide1});
                        b.side2.attr({d: e.side2, zIndex: e.zSide2})
                    }
                }, d)) :
                c.SVGElement.prototype.animate.call(this, a, b, d);
            return this
        };
        a.destroy = function () {
            this.top.destroy();
            this.out.destroy();
            this.inn.destroy();
            this.side1.destroy();
            this.side2.destroy();
            c.SVGElement.prototype.destroy.call(this)
        };
        a.hide = function () {
            this.top.hide();
            this.out.hide();
            this.inn.hide();
            this.side1.hide();
            this.side2.hide()
        };
        a.show = function () {
            this.top.show();
            this.out.show();
            this.inn.show();
            this.side1.show();
            this.side2.show()
        };
        a.zIndex = f;
        a.attr({zIndex: f});
        return a
    };
    c.SVGRenderer.prototype.arc3dPath =
        function (d) {
            var a = d.x, b = d.y, e = d.start, c = d.end - 1.0E-5, g = d.r, h = d.innerR, j = d.depth, i = d.alpha, k = d.beta, v = m(e), p = l(e), d = m(c), n = l(c), s = g * m(k), t = g * m(i), u = h * m(k);
            h *= m(i);
            var w = j * l(k), x = j * l(i), j = ["M", a + s * v, b + t * p], j = j.concat(r(a, b, s, t, e, c, 0, 0)), j = j.concat(["L", a + u * d, b + h * n]), j = j.concat(r(a, b, u, h, c, e, 0, 0)), j = j.concat(["Z"]), k = k > 0 ? o / 2 : 0, i = i > 0 ? 0 : o / 2, k = e > -k ? e : c > -k ? -k : e, q = c < o - i ? c : e < o - i ? o - i : c, i = ["M", a + s * m(k), b + t * l(k)], i = i.concat(r(a, b, s, t, k, q, 0, 0)), i = i.concat(["L", a + s * m(q) + w, b + t * l(q) + x]), i = i.concat(r(a, b, s, t, q, k,
                w, x)), i = i.concat(["Z"]), k = ["M", a + u * v, b + h * p], k = k.concat(r(a, b, u, h, e, c, 0, 0)), k = k.concat(["L", a + u * m(c) + w, b + h * l(c) + x]), k = k.concat(r(a, b, u, h, c, e, w, x)), k = k.concat(["Z"]), v = ["M", a + s * v, b + t * p, "L", a + s * v + w, b + t * p + x, "L", a + u * v + w, b + h * p + x, "L", a + u * v, b + h * p, "Z"], a = ["M", a + s * d, b + t * n, "L", a + s * d + w, b + t * n + x, "L", a + u * d + w, b + h * n + x, "L", a + u * d, b + h * n, "Z"], b = l((e + c) / 2), e = l(e), c = l(c);
            return {
                top: j,
                zTop: g,
                out: i,
                zOut: Math.max(b, e, c) * g,
                inn: k,
                zInn: Math.max(b, e, c) * g,
                side1: v,
                zSide1: e * g * 0.99,
                side2: a,
                zSide2: c * g * 0.99
            }
        };
    c.Chart.prototype.is3d =
        function () {
            return this.options.chart.options3d && this.options.chart.options3d.enabled
        };
    c.wrap(c.Chart.prototype, "isInsidePlot", function (d) {
        return this.is3d() ? !0 : d.apply(this, [].slice.call(arguments, 1))
    });
    c.getOptions().chart.options3d = {
        enabled: !1,
        alpha: 0,
        beta: 0,
        depth: 100,
        viewDistance: 25,
        frame: {
            bottom: {size: 1, color: "rgba(255,255,255,0)"},
            side: {size: 1, color: "rgba(255,255,255,0)"},
            back: {size: 1, color: "rgba(255,255,255,0)"}
        }
    };
    c.wrap(c.Chart.prototype, "init", function (d) {
        var a = [].slice.call(arguments, 1),
            b;
        if (a[0].chart.options3d && a[0].chart.options3d.enabled)b = a[0].plotOptions || {}, b = b.pie || {}, b.borderColor = c.pick(b.borderColor, void 0);
        d.apply(this, a)
    });
    c.wrap(c.Chart.prototype, "setChartSize", function (d) {
        d.apply(this, [].slice.call(arguments, 1));
        if (this.is3d()) {
            var a = this.inverted, b = this.clipBox, c = this.margin;
            b[a ? "y" : "x"] = -(c[3] || 0);
            b[a ? "x" : "y"] = -(c[0] || 0);
            b[a ? "height" : "width"] = this.chartWidth + (c[3] || 0) + (c[1] || 0);
            b[a ? "width" : "height"] = this.chartHeight + (c[0] || 0) + (c[2] || 0)
        }
    });
    c.wrap(c.Chart.prototype,
        "redraw", function (d) {
            if (this.is3d())this.isDirtyBox = !0;
            d.apply(this, [].slice.call(arguments, 1))
        });
    c.Chart.prototype.renderSeries = function () {
        for (var d, a = this.series.length; a--;)d = this.series[a], d.translate(), d.render()
    };
    c.Chart.prototype.retrieveStacks = function (d) {
        var a = this.series, b = {}, e, f = 1;
        c.each(this.series, function (c) {
            e = d ? c.options.stack || 0 : a.length - 1 - c.index;
            b[e] ? b[e].series.push(c) : (b[e] = {series: [c], position: f}, f++)
        });
        b.totalStacks = f + 1;
        return b
    };
    c.wrap(c.Axis.prototype, "init", function (d) {
        var a =
            arguments;
        if (a[1].is3d())a[2].tickWidth = c.pick(a[2].tickWidth, 0), a[2].gridLineWidth = c.pick(a[2].gridLineWidth, 1);
        d.apply(this, [].slice.call(arguments, 1))
    });
    c.wrap(c.Axis.prototype, "render", function (d) {
        d.apply(this, [].slice.call(arguments, 1));
        if (this.chart.is3d()) {
            var a = this.chart, b = a.renderer, c = a.options.chart.options3d, f = c.frame, g = f.bottom, h = f.back, f = f.side, j = c.depth, i = this.height, k = this.width, l = this.left, p = this.top;
            this.horiz ? (this.axisLine && this.axisLine.hide(), h = {
                x: l, y: p + (a.yAxis[0].reversed ?
                    -g.size : i), z: 0, width: k, height: g.size, depth: j, insidePlotArea: !1
            }, this.bottomFrame ? this.bottomFrame.animate(h) : this.bottomFrame = b.cuboid(h).attr({
                fill: g.color,
                zIndex: a.yAxis[0].reversed && c.alpha > 0 ? 4 : -1
            }).css({stroke: g.color}).add()) : (c = {
                x: l,
                y: p,
                z: j + 1,
                width: k,
                height: i + g.size,
                depth: h.size,
                insidePlotArea: !1
            }, this.backFrame ? this.backFrame.animate(c) : this.backFrame = b.cuboid(c).attr({
                fill: h.color,
                zIndex: -3
            }).css({stroke: h.color}).add(), this.axisLine && this.axisLine.hide(), a = {
                x: (a.yAxis[0].opposite ? k : 0) + l -
                f.size, y: p, z: 0, width: f.size, height: i + g.size, depth: j + h.size, insidePlotArea: !1
            }, this.sideFrame ? this.sideFrame.animate(a) : this.sideFrame = b.cuboid(a).attr({
                fill: f.color,
                zIndex: -2
            }).css({stroke: f.color}).add())
        }
    });
    c.wrap(c.Axis.prototype, "getPlotLinePath", function (c) {
        var a = c.apply(this, [].slice.call(arguments, 1));
        if (!this.chart.is3d())return a;
        if (a === null)return a;
        var b = this.chart.options.chart.options3d.depth, a = [{
            x: a[1],
            y: a[2],
            z: this.horiz || this.opposite ? b : 0
        }, {x: a[1], y: a[2], z: b}, {x: a[4], y: a[5], z: b}, {
            x: a[4],
            y: a[5], z: this.horiz || this.opposite ? 0 : b
        }], a = n(a, this.chart, !1);
        return a = this.chart.renderer.toLinePath(a, !1)
    });
    c.wrap(c.Axis.prototype, "getPlotBandPath", function (c) {
        if (this.chart.is3d()) {
            var a = arguments, b = a[1], a = this.getPlotLinePath(a[2]);
            (b = this.getPlotLinePath(b)) && a ? b.push(a[7], a[8], a[4], a[5], a[1], a[2]) : b = null;
            return b
        } else return c.apply(this, [].slice.call(arguments, 1))
    });
    c.wrap(c.Tick.prototype, "getMarkPath", function (c) {
        var a = c.apply(this, [].slice.call(arguments, 1));
        if (!this.axis.chart.is3d())return a;
        a = [{x: a[1], y: a[2], z: 0}, {x: a[4], y: a[5], z: 0}];
        a = n(a, this.axis.chart, !1);
        return a = ["M", a[0].x, a[0].y, "L", a[1].x, a[1].y]
    });
    c.wrap(c.Tick.prototype, "getLabelPosition", function (c) {
        var a = c.apply(this, [].slice.call(arguments, 1));
        if (!this.axis.chart.is3d())return a;
        a = n([{x: a.x, y: a.y, z: 0}], this.axis.chart, !1)[0];
        a.x -= !this.axis.horiz && this.axis.opposite ? this.axis.transA : 0;
        return a
    });
    c.wrap(c.Axis.prototype, "drawCrosshair", function (c) {
        var a = arguments;
        this.chart.is3d() && a[2] && (a[2] = {
            plotX: a[2].plotXold || a[2].plotX,
            plotY: a[2].plotYold || a[2].plotY
        });
        c.apply(this, [].slice.call(a, 1))
    });
    c.wrap(c.seriesTypes.column.prototype, "translate", function (d) {
        d.apply(this, [].slice.call(arguments, 1));
        if (this.chart.is3d()) {
            var a = this.chart, b = this.options, e = b.depth || 25, f = (b.stacking ? b.stack || 0 : this._i) * (e + (b.groupZPadding || 1));
            b.grouping !== !1 && (f = 0);
            f += b.groupZPadding || 1;
            c.each(this.data, function (b) {
                if (b.y !== null) {
                    var c = b.shapeArgs, d = b.tooltipPos;
                    b.shapeType = "cuboid";
                    c.z = f;
                    c.depth = e;
                    c.insidePlotArea = !0;
                    d = n([{x: d[0], y: d[1], z: f}],
                        a, !1)[0];
                    b.tooltipPos = [d.x, d.y]
                }
            })
        }
    });
    c.wrap(c.seriesTypes.column.prototype, "animate", function (d) {
        if (this.chart.is3d()) {
            var a = arguments[1], b = this.yAxis, e = this, f = this.yAxis.reversed;
            if (c.svg)a ? c.each(e.data, function (a) {
                if (a.y !== null && (a.height = a.shapeArgs.height, a.shapey = a.shapeArgs.y, a.shapeArgs.height = 1, !f))a.shapeArgs.y = a.stackY ? a.plotY + b.translate(a.stackY) : a.plotY + (a.negative ? -a.height : a.height)
            }) : (c.each(e.data, function (a) {
                if (a.y !== null)a.shapeArgs.height = a.height, a.shapeArgs.y = a.shapey, a.graphic &&
                a.graphic.animate(a.shapeArgs, e.options.animation)
            }), this.drawDataLabels(), e.animate = null)
        } else d.apply(this, [].slice.call(arguments, 1))
    });
    c.wrap(c.seriesTypes.column.prototype, "init", function (c) {
        c.apply(this, [].slice.call(arguments, 1));
        if (this.chart.is3d()) {
            var a = this.options, b = a.grouping, e = a.stacking, f = 0;
            if (b === void 0 || b) {
                b = this.chart.retrieveStacks(e);
                e = a.stack || 0;
                for (f = 0; f < b[e].series.length; f++)if (b[e].series[f] === this)break;
                f = b.totalStacks * 10 - 10 * (b.totalStacks - b[e].position) - f
            }
            a.zIndex = f
        }
    });
    c.wrap(c.Series.prototype, "alignDataLabel", function (c) {
        if (this.chart.is3d() && (this.type === "column" || this.type === "columnrange")) {
            var a = arguments[4], b = {x: a.x, y: a.y, z: 0}, b = n([b], this.chart, !0)[0];
            a.x = b.x;
            a.y = b.y
        }
        c.apply(this, [].slice.call(arguments, 1))
    });
    c.seriesTypes.columnrange && c.wrap(c.seriesTypes.columnrange.prototype, "drawPoints", F);
    c.wrap(c.seriesTypes.column.prototype, "drawPoints", F);
    var C = c.getOptions();
    C.plotOptions.cylinder = c.merge(C.plotOptions.column);
    C = c.extendClass(c.seriesTypes.column,
        {type: "cylinder"});
    c.seriesTypes.cylinder = C;
    c.wrap(c.seriesTypes.cylinder.prototype, "translate", function (d) {
        d.apply(this, [].slice.call(arguments, 1));
        if (this.chart.is3d()) {
            var a = this.chart, b = a.options, e = b.plotOptions.cylinder, b = b.chart.options3d, f = e.depth || 0, g = {
                x: a.inverted ? a.plotHeight / 2 : a.plotWidth / 2,
                y: a.inverted ? a.plotWidth / 2 : a.plotHeight / 2,
                z: b.depth,
                vd: b.viewDistance
            }, h = b.alpha, j = e.stacking ? (this.options.stack || 0) * f : this._i * f;
            j += f / 2;
            e.grouping !== !1 && (j = 0);
            c.each(this.data, function (a) {
                var b = a.shapeArgs;
                a.shapeType = "arc3d";
                b.x += f / 2;
                b.z = j;
                b.start = 0;
                b.end = 2 * o;
                b.r = f * 0.95;
                b.innerR = 0;
                b.depth = b.height * (1 / l((90 - h) * y)) - j;
                b.alpha = 90 - h;
                b.beta = 0;
                b.origin = g
            })
        }
    });
    c.wrap(c.seriesTypes.pie.prototype, "translate", function (d) {
        d.apply(this, [].slice.call(arguments, 1));
        if (this.chart.is3d()) {
            var a = this, b = a.chart, e = a.options, f = e.depth || 0, g = b.options.chart.options3d, h = {
                x: b.plotWidth / 2,
                y: b.plotHeight / 2,
                z: g.depth
            }, j = g.alpha, i = g.beta, k = e.stacking ? (e.stack || 0) * f : a._i * f;
            k += f / 2;
            e.grouping !== !1 && (k = 0);
            c.each(a.data, function (b) {
                b.shapeType =
                    "arc3d";
                var c = b.shapeArgs;
                if (b.y)c.z = k, c.depth = f * 0.75, c.origin = h, c.alpha = j, c.beta = i, c = (c.end + c.start) / 2, b.slicedTranslation = {
                    translateX: G(m(c) * a.options.slicedOffset * m(j * y)),
                    translateY: G(l(c) * a.options.slicedOffset * m(j * y))
                }
            })
        }
    });
    c.wrap(c.seriesTypes.pie.prototype.pointClass.prototype, "haloPath", function (c) {
        var a = arguments;
        return this.series.chart.is3d() ? [] : c.call(this, a[1])
    });
    c.wrap(c.seriesTypes.pie.prototype, "drawPoints", function (d) {
        if (this.chart.is3d()) {
            var a = this.options, b = this.options.states;
            this.borderWidth = a.borderWidth = a.edgeWidth || 1;
            this.borderColor = a.edgeColor = c.pick(a.edgeColor, a.borderColor, void 0);
            b.hover.borderColor = c.pick(b.hover.edgeColor, this.borderColor);
            b.hover.borderWidth = c.pick(b.hover.edgeWidth, this.borderWidth);
            b.select.borderColor = c.pick(b.select.edgeColor, this.borderColor);
            b.select.borderWidth = c.pick(b.select.edgeWidth, this.borderWidth);
            c.each(this.data, function (a) {
                var c = a.pointAttr;
                c[""].stroke = a.series.borderColor || a.color;
                c[""]["stroke-width"] = a.series.borderWidth;
                c.hover.stroke = b.hover.borderColor;
                c.hover["stroke-width"] = b.hover.borderWidth;
                c.select.stroke = b.select.borderColor;
                c.select["stroke-width"] = b.select.borderWidth
            })
        }
        d.apply(this, [].slice.call(arguments, 1));
        if (this.chart.is3d()) {
            var e = this.group;
            c.each(this.points, function (a) {
                a.graphic.out.add(e);
                a.graphic.inn.add(e);
                a.graphic.side1.add(e);
                a.graphic.side2.add(e)
            })
        }
    });
    c.wrap(c.seriesTypes.pie.prototype, "drawDataLabels", function (d) {
        if (this.chart.is3d()) {
            var a = this;
            c.each(a.data, function (b) {
                var c = b.shapeArgs,
                    d = c.r, g = c.depth, h = (c.alpha || a.chart.options.chart.options3d.alpha) * y, c = (c.start + c.end) / 2, b = b.labelPos;
                b[1] += -d * (1 - m(h)) * l(c) + (l(c) > 0 ? l(h) * g : 0);
                b[3] += -d * (1 - m(h)) * l(c) + (l(c) > 0 ? l(h) * g : 0);
                b[5] += -d * (1 - m(h)) * l(c) + (l(c) > 0 ? l(h) * g : 0)
            })
        }
        d.apply(this, [].slice.call(arguments, 1))
    });
    c.wrap(c.seriesTypes.pie.prototype, "addPoint", function (c) {
        c.apply(this, [].slice.call(arguments, 1));
        this.chart.is3d() && this.update(this.userOptions, !0)
    });
    c.wrap(c.seriesTypes.pie.prototype, "animate", function (d) {
        if (this.chart.is3d()) {
            var a =
                arguments[1], b = this.options.animation, e = this.center, f = this.group, g = this.markerGroup;
            if (c.svg)if (b === !0 && (b = {}), a) {
                if (f.oldtranslateX = f.translateX, f.oldtranslateY = f.translateY, a = {
                        translateX: e[0],
                        translateY: e[1],
                        scaleX: 0.001,
                        scaleY: 0.001
                    }, f.attr(a), g)g.attrSetters = f.attrSetters, g.attr(a)
            } else a = {
                translateX: f.oldtranslateX,
                translateY: f.oldtranslateY,
                scaleX: 1,
                scaleY: 1
            }, f.animate(a, b), g && g.animate(a, b), this.animate = null
        } else d.apply(this, [].slice.call(arguments, 1))
    });
    c.wrap(c.seriesTypes.scatter.prototype,
        "translate", function (c) {
            c.apply(this, [].slice.call(arguments, 1));
            if (this.chart.is3d()) {
                var a = this.chart, b = a.options.chart.options3d.depth, e = a.options.zAxis || {
                        min: 0,
                        max: b
                    }, f = b / (e.max - e.min), g = [], h;
                for (h = 0; h < this.data.length; h++)b = this.data[h], g.push({
                    x: b.plotX,
                    y: b.plotY,
                    z: (b.z - e.min) * f
                });
                a = n(g, a, !0);
                for (h = 0; h < this.data.length; h++)b = this.data[h], e = a[h], b.plotXold = b.plotX, b.plotYold = b.plotY, b.plotX = e.x, b.plotY = e.y, b.plotZ = e.z
            }
        });
    c.wrap(c.seriesTypes.scatter.prototype, "init", function (c) {
        var a = c.apply(this,
            [].slice.call(arguments, 1));
        if (this.chart.is3d())this.pointArrayMap = ["x", "y", "z"], this.tooltipOptions.pointFormat = this.userOptions.tooltip ? this.userOptions.tooltip.pointFormat || "x: <b>{point.x}</b><br/>y: <b>{point.y}</b><br/>z: <b>{point.z}</b><br/>" : "x: <b>{point.x}</b><br/>y: <b>{point.y}</b><br/>z: <b>{point.z}</b><br/>";
        return a
    });
    if (c.VMLRenderer)c.setOptions({animate: !1}), c.VMLRenderer.prototype.cuboid = c.SVGRenderer.prototype.cuboid, c.VMLRenderer.prototype.cuboidPath = c.SVGRenderer.prototype.cuboidPath,
        c.VMLRenderer.prototype.toLinePath = c.SVGRenderer.prototype.toLinePath, c.VMLRenderer.prototype.createElement3D = c.SVGRenderer.prototype.createElement3D, c.VMLRenderer.prototype.arc3d = function (d) {
        d = c.SVGRenderer.prototype.arc3d.call(this, d);
        d.css({zIndex: d.zIndex});
        return d
    }, c.VMLRenderer.prototype.arc3dPath = c.SVGRenderer.prototype.arc3dPath, c.wrap(c.Axis.prototype, "render", function (c) {
        c.apply(this, [].slice.call(arguments, 1));
        this.sideFrame && (this.sideFrame.css({zIndex: 0}), this.sideFrame.front.attr({fill: this.sideFrame.color}));
        this.bottomFrame && (this.bottomFrame.css({zIndex: 1}), this.bottomFrame.front.attr({fill: this.bottomFrame.color}));
        this.backFrame && (this.backFrame.css({zIndex: 0}), this.backFrame.front.attr({fill: this.backFrame.color}))
    })
})(Highcharts);
/*
 Highcharts JS v4.1.3 (2015-02-27)
 Exporting module

 (c) 2010-2014 Torstein Honsi

 License: www.highcharts.com/license
 */
(function (g) {
    var z = g.Chart, s = g.addEvent, A = g.removeEvent, B = HighchartsAdapter.fireEvent, j = g.createElement, p = g.discardElement, u = g.css, l = g.merge, m = g.each, q = g.extend, E = g.splat, F = Math.max, k = document, C = window, G = g.isTouchDevice, H = g.Renderer.prototype.symbols, r = g.getOptions(), x;
    q(r.lang, {
        printChart: "Print chart",
        downloadPNG: "Download PNG image",
        downloadJPEG: "Download JPEG image",
        downloadPDF: "Download PDF document",
        downloadSVG: "Download SVG vector image",
        contextButtonTitle: "Chart context menu"
    });
    r.navigation =
    {
        menuStyle: {border: "1px solid #A0A0A0", background: "#FFFFFF", padding: "5px 0"},
        menuItemStyle: {padding: "0 10px", background: "none", color: "#303030", fontSize: G ? "14px" : "11px"},
        menuItemHoverStyle: {background: "#4572A5", color: "#FFFFFF"},
        buttonOptions: {
            symbolFill: "#E0E0E0",
            symbolSize: 14,
            symbolStroke: "#666",
            symbolStrokeWidth: 3,
            symbolX: 12.5,
            symbolY: 10.5,
            align: "right",
            buttonSpacing: 3,
            height: 22,
            theme: {fill: "white", stroke: "none"},
            verticalAlign: "top",
            width: 24
        }
    };
    r.exporting = {
        type: "image/png", url: "http://export.highcharts.com/",
        buttons: {
            contextButton: {
                menuClassName: "highcharts-contextmenu",
                symbol: "menu",
                _titleKey: "contextButtonTitle",
                menuItems: [{
                    textKey: "printChart", onclick: function () {
                        this.print()
                    }
                }, {separator: !0}, {
                    textKey: "downloadPNG", onclick: function () {
                        this.exportChart()
                    }
                }, {
                    textKey: "downloadJPEG", onclick: function () {
                        this.exportChart({type: "image/jpeg"})
                    }
                }, {
                    textKey: "downloadPDF", onclick: function () {
                        this.exportChart({type: "application/pdf"})
                    }
                }, {
                    textKey: "downloadSVG", onclick: function () {
                        this.exportChart({type: "image/svg+xml"})
                    }
                }]
            }
        }
    };
    g.post = function (b, a, e) {
        var c, b = j("form", l({
            method: "post",
            action: b,
            enctype: "multipart/form-data"
        }, e), {display: "none"}, k.body);
        for (c in a)j("input", {type: "hidden", name: c, value: a[c]}, null, b);
        b.submit();
        p(b)
    };
    q(z.prototype, {
        sanitizeSVG: function (b) {
            return b.replace(/zIndex="[^"]+"/g, "").replace(/isShadow="[^"]+"/g, "").replace(/symbolName="[^"]+"/g, "").replace(/jQuery[0-9]+="[^"]+"/g, "").replace(/url\([^#]+#/g, "url(#").replace(/<svg /, '<svg xmlns:xlink="http://www.w3.org/1999/xlink" ').replace(/ (NS[0-9]+\:)?href=/g,
                " xlink:href=").replace(/\n/, " ").replace(/<\/svg>.*?$/, "</svg>").replace(/(fill|stroke)="rgba\(([ 0-9]+,[ 0-9]+,[ 0-9]+),([ 0-9\.]+)\)"/g, '$1="rgb($2)" $1-opacity="$3"').replace(/(text-shadow:)([^;"]+)([;"])/g, function (a, b, c, f) {
                c = c.replace(/\([^\)]+\)/g, function (a) {
                    return a.replace(/,/g, "|")
                });
                c = c.split(",")[0];
                c = c.replace(/\([^\)]+\)/g, function (a) {
                    return a.replace(/\|/g, ",")
                });
                return b + c + f
            }).replace(/&nbsp;/g, " ").replace(/&shy;/g, "­").replace(/<IMG /g, "<image ").replace(/height=([^" ]+)/g, 'height="$1"').replace(/width=([^" ]+)/g,
                'width="$1"').replace(/hc-svg-href="([^"]+)">/g, 'xlink:href="$1"/>').replace(/id=([^" >]+)/g, 'id="$1"').replace(/class=([^" >]+)/g, 'class="$1"').replace(/ transform /g, " ").replace(/:(path|rect)/g, "$1").replace(/style="([^"]+)"/g, function (a) {
                return a.toLowerCase()
            })
        }, getSVG: function (b) {
            var a = this, e, c, f, y, h, d = l(a.options, b);
            if (!k.createElementNS)k.createElementNS = function (a, b) {
                return k.createElement(b)
            };
            c = j("div", null, {
                position: "absolute", top: "-9999em", width: a.chartWidth + "px", height: a.chartHeight +
                "px"
            }, k.body);
            f = a.renderTo.style.width;
            h = a.renderTo.style.height;
            f = d.exporting.sourceWidth || d.chart.width || /px$/.test(f) && parseInt(f, 10) || 600;
            h = d.exporting.sourceHeight || d.chart.height || /px$/.test(h) && parseInt(h, 10) || 400;
            q(d.chart, {animation: !1, renderTo: c, forExport: !0, width: f, height: h});
            d.exporting.enabled = !1;
            delete d.data;
            d.series = [];
            m(a.series, function (a) {
                y = l(a.options, {animation: !1, enableMouseTracking: !1, showCheckbox: !1, visible: a.visible});
                y.isInternal || d.series.push(y)
            });
            b && m(["xAxis", "yAxis"],
                function (a) {
                    m(E(b[a]), function (b, c) {
                        d[a][c] = l(d[a][c], b)
                    })
                });
            e = new g.Chart(d, a.callback);
            m(["xAxis", "yAxis"], function (b) {
                m(a[b], function (a, c) {
                    var d = e[b][c], f = a.getExtremes(), h = f.userMin, f = f.userMax;
                    d && (h !== void 0 || f !== void 0) && d.setExtremes(h, f, !0, !1)
                })
            });
            f = e.container.innerHTML;
            d = null;
            e.destroy();
            p(c);
            f = this.sanitizeSVG(f);
            return f = f.replace(/(url\(#highcharts-[0-9]+)&quot;/g, "$1").replace(/&quot;/g, "'")
        }, getSVGForExport: function (b, a) {
            var e = this.options.exporting;
            return this.getSVG(l({chart: {borderRadius: 0}},
                e.chartOptions, a, {
                    exporting: {
                        sourceWidth: b && b.sourceWidth || e.sourceWidth,
                        sourceHeight: b && b.sourceHeight || e.sourceHeight
                    }
                }))
        }, exportChart: function (b, a) {
            var e = this.getSVGForExport(b, a), b = l(this.options.exporting, b);
            g.post(b.url, {
                filename: b.filename || "chart",
                type: b.type,
                width: b.width || 0,
                scale: b.scale || 2,
                svg: e
            }, b.formAttributes)
        }, print: function () {
            var b = this, a = b.container, e = [], c = a.parentNode, f = k.body, g = f.childNodes;
            if (!b.isPrinting)b.isPrinting = !0, B(b, "beforePrint"), m(g, function (a, b) {
                if (a.nodeType === 1)e[b] =
                    a.style.display, a.style.display = "none"
            }), f.appendChild(a), C.focus(), C.print(), setTimeout(function () {
                c.appendChild(a);
                m(g, function (a, b) {
                    if (a.nodeType === 1)a.style.display = e[b]
                });
                b.isPrinting = !1;
                B(b, "afterPrint")
            }, 1E3)
        }, contextMenu: function (b, a, e, c, f, g, h) {
            var d = this, l = d.options.navigation, D = l.menuItemStyle, n = d.chartWidth, o = d.chartHeight, k = "cache-" + b, i = d[k], t = F(f, g), v, w, p, r = function (a) {
                d.pointer.inClass(a.target, b) || w()
            };
            if (!i)d[k] = i = j("div", {className: b}, {position: "absolute", zIndex: 1E3, padding: t + "px"},
                d.container), v = j("div", null, q({
                MozBoxShadow: "3px 3px 10px #888",
                WebkitBoxShadow: "3px 3px 10px #888",
                boxShadow: "3px 3px 10px #888"
            }, l.menuStyle), i), w = function () {
                u(i, {display: "none"});
                h && h.setState(0);
                d.openMenu = !1
            }, s(i, "mouseleave", function () {
                p = setTimeout(w, 500)
            }), s(i, "mouseenter", function () {
                clearTimeout(p)
            }), s(document, "mouseup", r), s(d, "destroy", function () {
                A(document, "mouseup", r)
            }), m(a, function (a) {
                if (a) {
                    var b = a.separator ? j("hr", null, null, v) : j("div", {
                        onmouseover: function () {
                            u(this, l.menuItemHoverStyle)
                        },
                        onmouseout: function () {
                            u(this, D)
                        }, onclick: function () {
                            w();
                            a.onclick && a.onclick.apply(d, arguments)
                        }, innerHTML: a.text || d.options.lang[a.textKey]
                    }, q({cursor: "pointer"}, D), v);
                    d.exportDivElements.push(b)
                }
            }), d.exportDivElements.push(v, i), d.exportMenuWidth = i.offsetWidth, d.exportMenuHeight = i.offsetHeight;
            a = {display: "block"};
            e + d.exportMenuWidth > n ? a.right = n - e - f - t + "px" : a.left = e - t + "px";
            c + g + d.exportMenuHeight > o && h.alignOptions.verticalAlign !== "top" ? a.bottom = o - c - t + "px" : a.top = c + g - t + "px";
            u(i, a);
            d.openMenu = !0
        }, addButton: function (b) {
            var a =
                this, e = a.renderer, c = l(a.options.navigation.buttonOptions, b), f = c.onclick, k = c.menuItems, h, d, m = {
                stroke: c.symbolStroke,
                fill: c.symbolFill
            }, j = c.symbolSize || 12;
            if (!a.btnCount)a.btnCount = 0;
            if (!a.exportDivElements)a.exportDivElements = [], a.exportSVGElements = [];
            if (c.enabled !== !1) {
                var n = c.theme, o = n.states, p = o && o.hover, o = o && o.select, i;
                delete n.states;
                f ? i = function () {
                    f.apply(a, arguments)
                } : k && (i = function () {
                    a.contextMenu(d.menuClassName, k, d.translateX, d.translateY, d.width, d.height, d);
                    d.setState(2)
                });
                c.text && c.symbol ?
                    n.paddingLeft = g.pick(n.paddingLeft, 25) : c.text || q(n, {
                    width: c.width,
                    height: c.height,
                    padding: 0
                });
                d = e.button(c.text, 0, 0, i, n, p, o).attr({
                    title: a.options.lang[c._titleKey],
                    "stroke-linecap": "round"
                });
                d.menuClassName = b.menuClassName || "highcharts-menu-" + a.btnCount++;
                c.symbol && (h = e.symbol(c.symbol, c.symbolX - j / 2, c.symbolY - j / 2, j, j).attr(q(m, {
                    "stroke-width": c.symbolStrokeWidth || 1,
                    zIndex: 1
                })).add(d));
                d.add().align(q(c, {width: d.width, x: g.pick(c.x, x)}), !0, "spacingBox");
                x += (d.width + c.buttonSpacing) * (c.align === "right" ?
                        -1 : 1);
                a.exportSVGElements.push(d, h)
            }
        }, destroyExport: function (b) {
            var b = b.target, a, e;
            for (a = 0; a < b.exportSVGElements.length; a++)if (e = b.exportSVGElements[a])e.onclick = e.ontouchstart = null, b.exportSVGElements[a] = e.destroy();
            for (a = 0; a < b.exportDivElements.length; a++)e = b.exportDivElements[a], A(e, "mouseleave"), b.exportDivElements[a] = e.onmouseout = e.onmouseover = e.ontouchstart = e.onclick = null, p(e)
        }
    });
    H.menu = function (b, a, e, c) {
        return ["M", b, a + 2.5, "L", b + e, a + 2.5, "M", b, a + c / 2 + 0.5, "L", b + e, a + c / 2 + 0.5, "M", b, a + c - 1.5, "L", b +
        e, a + c - 1.5]
    };
    z.prototype.callbacks.push(function (b) {
        var a, e = b.options.exporting, c = e.buttons;
        x = 0;
        if (e.enabled !== !1) {
            for (a in c)b.addButton(c[a]);
            s(b, "destroy", b.destroyExport)
        }
    })
})(Highcharts);
/*
 Highcharts JS v4.1.3 (2015-02-27)
 Solid angular gauge module

 (c) 2010-2014 Torstein Honsi

 License: www.highcharts.com/license
 */
(function (a) {
    var q = a.getOptions().plotOptions, r = a.pInt, s = a.pick, j = a.each, o;
    q.solidgauge = a.merge(q.gauge, {colorByPoint: !0});
    o = {
        initDataClasses: function (b) {
            var c = this, f = this.chart, d, n = 0, i = this.options;
            this.dataClasses = d = [];
            j(b.dataClasses, function (e, g) {
                var p, e = a.merge(e);
                d.push(e);
                if (!e.color)i.dataClassColor === "category" ? (p = f.options.colors, e.color = p[n++], n === p.length && (n = 0)) : e.color = c.tweenColors(a.Color(i.minColor), a.Color(i.maxColor), g / (b.dataClasses.length - 1))
            })
        }, initStops: function (b) {
            this.stops =
                b.stops || [[0, this.options.minColor], [1, this.options.maxColor]];
            j(this.stops, function (b) {
                b.color = a.Color(b[1])
            })
        }, toColor: function (b, c) {
            var f, d = this.stops, a, i = this.dataClasses, e, g;
            if (i)for (g = i.length; g--;) {
                if (e = i[g], a = e.from, d = e.to, (a === void 0 || b >= a) && (d === void 0 || b <= d)) {
                    f = e.color;
                    if (c)c.dataClass = g;
                    break
                }
            } else {
                this.isLog && (b = this.val2lin(b));
                f = 1 - (this.max - b) / (this.max - this.min);
                for (g = d.length; g--;)if (f > d[g][0])break;
                a = d[g] || d[g + 1];
                d = d[g + 1] || a;
                f = 1 - (d[0] - f) / (d[0] - a[0] || 1);
                f = this.tweenColors(a.color,
                    d.color, f)
            }
            return f
        }, tweenColors: function (b, c, a) {
            var d = c.rgba[3] !== 1 || b.rgba[3] !== 1;
            return b.rgba.length === 0 || c.rgba.length === 0 ? "none" : (d ? "rgba(" : "rgb(") + Math.round(c.rgba[0] + (b.rgba[0] - c.rgba[0]) * (1 - a)) + "," + Math.round(c.rgba[1] + (b.rgba[1] - c.rgba[1]) * (1 - a)) + "," + Math.round(c.rgba[2] + (b.rgba[2] - c.rgba[2]) * (1 - a)) + (d ? "," + (c.rgba[3] + (b.rgba[3] - c.rgba[3]) * (1 - a)) : "") + ")"
        }
    };
    j(["fill", "stroke"], function (b) {
        HighchartsAdapter.addAnimSetter(b, function (c) {
            c.elem.attr(b, o.tweenColors(a.Color(c.start), a.Color(c.end),
                c.pos))
        })
    });
    a.seriesTypes.solidgauge = a.extendClass(a.seriesTypes.gauge, {
        type: "solidgauge", bindAxes: function () {
            var b;
            a.seriesTypes.gauge.prototype.bindAxes.call(this);
            b = this.yAxis;
            a.extend(b, o);
            b.options.dataClasses && b.initDataClasses(b.options);
            b.initStops(b.options)
        }, drawPoints: function () {
            var b = this, c = b.yAxis, f = c.center, d = b.options, n = b.radius = r(s(d.radius, 100)) * f[2] / 200, i = b.chart.renderer, e = d.overshoot, g = e && typeof e === "number" ? e / 180 * Math.PI : 0;
            a.each(b.points, function (a) {
                var e = a.graphic, h = c.startAngleRad +
                    c.translate(a.y, null, null, null, !0), k = r(s(d.innerRadius, 60)) * f[2] / 200, l = c.toColor(a.y, a);
                l === "none" && (l = a.color || b.color || "none");
                if (l !== "none")a.color = l;
                h = Math.max(c.startAngleRad - g, Math.min(c.endAngleRad + g, h));
                d.wrap === !1 && (h = Math.max(c.startAngleRad, Math.min(c.endAngleRad, h)));
                var h = h * 180 / Math.PI, m = h / (180 / Math.PI), j = c.startAngleRad, h = Math.min(m, j), m = Math.max(m, j);
                m - h > 2 * Math.PI && (m = h + 2 * Math.PI);
                a.shapeArgs = k = {x: f[0], y: f[1], r: n, innerR: k, start: h, end: m, fill: l};
                e ? (a = k.d, e.animate(k), k.d = a) : a.graphic =
                    i.arc(k).attr({
                        stroke: d.borderColor || "none",
                        "stroke-width": d.borderWidth || 0,
                        fill: l,
                        "sweep-flag": 0
                    }).add(b.group)
            })
        }, animate: function (b) {
            this.center = this.yAxis.center;
            this.center[3] = 2 * this.radius;
            this.startAngleRad = this.yAxis.startAngleRad;
            a.seriesTypes.pie.prototype.animate.call(this, b)
        }
    })
})(Highcharts);
/*

 Highcharts funnel module

 (c) 2010-2014 Torstein Honsi

 License: www.highcharts.com/license
 */
(function (b) {
    var q = b.getOptions(), w = q.plotOptions, r = b.seriesTypes, G = b.merge, E = function () {
    }, B = b.each, F = b.pick;
    w.funnel = G(w.pie, {
        animation: !1,
        center: ["50%", "50%"],
        width: "90%",
        neckWidth: "30%",
        height: "100%",
        neckHeight: "25%",
        reversed: !1,
        dataLabels: {connectorWidth: 1, connectorColor: "#606060"},
        size: !0,
        states: {select: {color: "#C0C0C0", borderColor: "#000000", shadow: !1}}
    });
    r.funnel = b.extendClass(r.pie, {
        type: "funnel", animate: E, translate: function () {
            var a = function (i, a) {
                return /%$/.test(i) ? a * parseInt(i, 10) / 100 : parseInt(i,
                    10)
            }, C = 0, e = this.chart, c = this.options, b = c.reversed, j = e.plotWidth, f = e.plotHeight, n = 0, e = c.center, g = a(e[0], j), q = a(e[1], f), r = a(c.width, j), k, s, d = a(c.height, f), t = a(c.neckWidth, j), u = a(c.neckHeight, f), x = d - u, a = this.data, y, z, w = c.dataLabels.position === "left" ? 1 : 0, A, l, D, p, h, v, m;
            this.getWidthAt = s = function (i) {
                return i > d - u || d === u ? t : t + (r - t) * ((d - u - i) / (d - u))
            };
            this.getX = function (i, a) {
                return g + (a ? -1 : 1) * (s(b ? f - i : i) / 2 + c.dataLabels.distance)
            };
            this.center = [g, q, d];
            this.centerX = g;
            B(a, function (a) {
                C += a.y
            });
            B(a, function (a) {
                m = null;
                z = C ? a.y / C : 0;
                l = q - d / 2 + n * d;
                h = l + z * d;
                k = s(l);
                A = g - k / 2;
                D = A + k;
                k = s(h);
                p = g - k / 2;
                v = p + k;
                l > x ? (A = p = g - t / 2, D = v = g + t / 2) : h > x && (m = h, k = s(x), p = g - k / 2, v = p + k, h = x);
                b && (l = d - l, h = d - h, m = m ? d - m : null);
                y = ["M", A, l, "L", D, l, v, h];
                m && y.push(v, m, p, m);
                y.push(p, h, "Z");
                a.shapeType = "path";
                a.shapeArgs = {d: y};
                a.percentage = z * 100;
                a.plotX = g;
                a.plotY = (l + (m || h)) / 2;
                a.tooltipPos = [g, a.plotY];
                a.slice = E;
                a.half = w;
                n += z
            })
        }, drawPoints: function () {
            var a = this, b = a.options, e = a.chart.renderer;
            B(a.data, function (c) {
                var o = c.options, j = c.graphic, f = c.shapeArgs;
                j ? j.animate(f) :
                    c.graphic = e.path(f).attr({
                        fill: c.color,
                        stroke: F(o.borderColor, b.borderColor),
                        "stroke-width": F(o.borderWidth, b.borderWidth)
                    }).add(a.group)
            })
        }, sortByAngle: function (a) {
            a.sort(function (a, b) {
                return a.plotY - b.plotY
            })
        }, drawDataLabels: function () {
            var a = this.data, b = this.options.dataLabels.distance, e, c, o, j = a.length, f, n;
            for (this.center[2] -= 2 * b; j--;)o = a[j], c = (e = o.half) ? 1 : -1, n = o.plotY, f = this.getX(n, e), o.labelPos = [0, n, f + (b - 5) * c, n, f + b * c, n, e ? "right" : "left", 0];
            r.pie.prototype.drawDataLabels.call(this)
        }
    });
    q.plotOptions.pyramid =
        b.merge(q.plotOptions.funnel, {neckWidth: "0%", neckHeight: "0%", reversed: !0});
    b.seriesTypes.pyramid = b.extendClass(b.seriesTypes.funnel, {type: "pyramid"})
})(Highcharts);
/*
 Highcharts JS v4.1.3 (2015-02-27)
 Plugin for displaying a message when there is no data visible in chart.

 (c) 2010-2014 Highsoft AS
 Author: Oystein Moseng

 License: www.highcharts.com/license
 */
(function (c) {
    function i() {
        return !!this.points.length
    }

    function e() {
        this.hasData() ? this.hideNoData() : this.showNoData()
    }

    var f = c.seriesTypes, d = c.Chart.prototype, g = c.getOptions(), h = c.extend, j = c.each;
    h(g.lang, {noData: "No data to display"});
    g.noData = {
        position: {x: 0, y: 0, align: "center", verticalAlign: "middle"},
        attr: {},
        style: {fontWeight: "bold", fontSize: "12px", color: "#60606a"}
    };
    j(["pie", "gauge", "waterfall", "bubble"], function (a) {
        if (f[a])f[a].prototype.hasData = i
    });
    c.Series.prototype.hasData = function () {
        return this.visible &&
            this.dataMax !== void 0 && this.dataMin !== void 0
    };
    d.showNoData = function (a) {
        var b = this.options, a = a || b.lang.noData, b = b.noData;
        if (!this.noDataLabel)this.noDataLabel = this.renderer.label(a, 0, 0, null, null, null, null, null, "no-data").attr(b.attr).css(b.style).add(), this.noDataLabel.align(h(this.noDataLabel.getBBox(), b.position), !1, "plotBox")
    };
    d.hideNoData = function () {
        if (this.noDataLabel)this.noDataLabel = this.noDataLabel.destroy()
    };
    d.hasData = function () {
        for (var a = this.series, b = a.length; b--;)if (a[b].hasData() && !a[b].options.isInternal)return !0;
        return !1
    };
    d.callbacks.push(function (a) {
        c.addEvent(a, "load", e);
        c.addEvent(a, "redraw", e)
    })
})(Highcharts);
(function (g) {
    var h = g.Chart, i = g.each, j = HighchartsAdapter.addEvent;
    h.prototype.callbacks.push(function (e) {
        function f() {
            var b = [];
            i(e.series, function (a) {
                var c = a.options.dataLabels;
                (c.enabled || a._hasPointLabels) && !c.allowOverlap && a.visible && i(a.points, function (a) {
                    if (a.dataLabel)a.dataLabel.labelrank = a.labelrank, b.push(a.dataLabel)
                })
            });
            e.hideOverlappingLabels(b)
        }

        f();
        j(e, "redraw", f)
    });
    h.prototype.hideOverlappingLabels = function (e) {
        var f = e.length, b, a, c, d;
        for (a = 0; a < f; a++)if (b = e[a])b.oldOpacity = b.opacity, b.newOpacity =
            1;
        for (a = 0; a < f; a++) {
            c = e[a];
            for (b = a + 1; b < f; ++b)if (d = e[b], c && d && c.placed && d.placed && c.newOpacity !== 0 && d.newOpacity !== 0 && !(d.alignAttr.x > c.alignAttr.x + c.width || d.alignAttr.x + d.width < c.alignAttr.x || d.alignAttr.y > c.alignAttr.y + c.height || d.alignAttr.y + d.height < c.alignAttr.y))(c.labelrank < d.labelrank ? c : d).newOpacity = 0
        }
        for (a = 0; a < f; a++)if (b = e[a]) {
            if (b.oldOpacity !== b.newOpacity && b.placed)b.alignAttr.opacity = b.newOpacity, b[b.isOld && b.newOpacity ? "animate" : "attr"](b.alignAttr);
            b.isOld = !0
        }
    }
})(Highcharts);
/*
 Highcharts JS v4.1.3 (2015-02-27)
 Data module

 (c) 2012-2014 Torstein Honsi

 License: www.highcharts.com/license
 */
(function (g) {
    var k = g.each, t = g.pick, r = HighchartsAdapter.inArray, u = g.splat, j, p = function (b, a) {
        this.init(b, a)
    };
    g.extend(p.prototype, {
        init: function (b, a) {
            this.options = b;
            this.chartOptions = a;
            this.columns = b.columns || this.rowsToColumns(b.rows) || [];
            this.firstRowAsNames = t(b.firstRowAsNames, !0);
            this.decimalRegex = b.decimalPoint && RegExp("^([0-9]+)" + b.decimalPoint + "([0-9]+)$");
            this.rawColumns = [];
            this.columns.length ? this.dataFound() : (this.parseCSV(), this.parseTable(), this.parseGoogleSpreadsheet())
        }, getColumnDistribution: function () {
            var b =
                this.chartOptions, a = this.options, e = [], f = function (b) {
                return (g.seriesTypes[b || "line"].prototype.pointArrayMap || [0]).length
            }, d = b && b.chart && b.chart.type, c = [], h = [], n = 0, i;
            k(b && b.series || [], function (b) {
                c.push(f(b.type || d))
            });
            k(a && a.seriesMapping || [], function (b) {
                e.push(b.x || 0)
            });
            e.length === 0 && e.push(0);
            k(a && a.seriesMapping || [], function (a) {
                var e = new j, o, q = c[n] || f(d), m = g.seriesTypes[((b && b.series || [])[n] || {}).type || d || "line"].prototype.pointArrayMap || ["y"];
                e.addColumnReader(a.x, "x");
                for (o in a)a.hasOwnProperty(o) &&
                o !== "x" && e.addColumnReader(a[o], o);
                for (i = 0; i < q; i++)e.hasReader(m[i]) || e.addColumnReader(void 0, m[i]);
                h.push(e);
                n++
            });
            a = g.seriesTypes[d || "line"].prototype.pointArrayMap;
            a === void 0 && (a = ["y"]);
            this.valueCount = {global: f(d), xColumns: e, individual: c, seriesBuilders: h, globalPointArrayMap: a}
        }, dataFound: function () {
            if (this.options.switchRowsAndColumns)this.columns = this.rowsToColumns(this.columns);
            this.getColumnDistribution();
            this.parseTypes();
            this.parsed() !== !1 && this.complete()
        }, parseCSV: function () {
            var b = this,
                a = this.options, e = a.csv, f = this.columns, d = a.startRow || 0, c = a.endRow || Number.MAX_VALUE, h = a.startColumn || 0, n = a.endColumn || Number.MAX_VALUE, i, g, s = 0;
            e && (g = e.replace(/\r\n/g, "\n").replace(/\r/g, "\n").split(a.lineDelimiter || "\n"), i = a.itemDelimiter || (e.indexOf("\t") !== -1 ? "\t" : ","), k(g, function (a, e) {
                var g = b.trim(a), v = g.indexOf("#") === 0;
                e >= d && e <= c && !v && g !== "" && (g = a.split(i), k(g, function (b, a) {
                    a >= h && a <= n && (f[a - h] || (f[a - h] = []), f[a - h][s] = b)
                }), s += 1)
            }), this.dataFound())
        }, parseTable: function () {
            var b = this.options, a =
                b.table, e = this.columns, f = b.startRow || 0, d = b.endRow || Number.MAX_VALUE, c = b.startColumn || 0, h = b.endColumn || Number.MAX_VALUE;
            a && (typeof a === "string" && (a = document.getElementById(a)), k(a.getElementsByTagName("tr"), function (b, a) {
                a >= f && a <= d && k(b.children, function (b, d) {
                    if ((b.tagName === "TD" || b.tagName === "TH") && d >= c && d <= h)e[d - c] || (e[d - c] = []), e[d - c][a - f] = b.innerHTML
                })
            }), this.dataFound())
        }, parseGoogleSpreadsheet: function () {
            var b = this, a = this.options, e = a.googleSpreadsheetKey, f = this.columns, d = a.startRow || 0, c = a.endRow ||
                Number.MAX_VALUE, h = a.startColumn || 0, n = a.endColumn || Number.MAX_VALUE, i, g;
            e && jQuery.ajax({
                dataType: "json",
                url: "https://spreadsheets.google.com/feeds/cells/" + e + "/" + (a.googleSpreadsheetWorksheet || "od6") + "/public/values?alt=json-in-script&callback=?",
                error: a.error,
                success: function (a) {
                    var a = a.feed.entry, e, k = a.length, m = 0, j = 0, l;
                    for (l = 0; l < k; l++)e = a[l], m = Math.max(m, e.gs$cell.col), j = Math.max(j, e.gs$cell.row);
                    for (l = 0; l < m; l++)if (l >= h && l <= n)f[l - h] = [], f[l - h].length = Math.min(j, c - d);
                    for (l = 0; l < k; l++)if (e = a[l], i = e.gs$cell.row -
                            1, g = e.gs$cell.col - 1, g >= h && g <= n && i >= d && i <= c)f[g - h][i - d] = e.content.$t;
                    b.dataFound()
                }
            })
        }, trim: function (b, a) {
            typeof b === "string" && (b = b.replace(/^\s+|\s+$/g, ""), a && /^[0-9\s]+$/.test(b) && (b = b.replace(/\s/g, "")), this.decimalRegex && (b = b.replace(this.decimalRegex, "$1.$2")));
            return b
        }, parseTypes: function () {
            for (var b = this.columns, a = b.length; a--;)this.parseColumn(b[a], a)
        }, parseColumn: function (b, a) {
            var e = this.rawColumns, f = this.columns, d = b.length, c, h, g, i, k = this.firstRowAsNames, j = r(a, this.valueCount.xColumns) !== -1, o = [], q = this.chartOptions, m, p = (this.options.columnTypes || [])[a], q = j && (q && q.xAxis && u(q.xAxis)[0].type === "category" || p === "string");
            for (e[a] || (e[a] = []); d--;)if (c = o[d] || b[d], g = this.trim(c), i = this.trim(c, !0), h = parseFloat(i), e[a][d] === void 0 && (e[a][d] = g), q || d === 0 && k)b[d] = g; else if (+i === h)b[d] = h, h > 31536E6 && p !== "float" ? b.isDatetime = !0 : b.isNumeric = !0, b[d + 1] !== void 0 && (m = h > b[d + 1]); else if (h = this.parseDate(c), j && typeof h === "number" && !isNaN(h) && p !== "float") {
                if (o[d] = c, b[d] = h, b.isDatetime = !0, b[d + 1] !== void 0) {
                    c =
                        h > b[d + 1];
                    if (c !== m && m !== void 0)this.alternativeFormat ? (this.dateFormat = this.alternativeFormat, d = b.length, this.alternativeFormat = this.dateFormats[this.dateFormat].alternative) : b.unsorted = !0;
                    m = c
                }
            } else if (b[d] = g === "" ? null : g, d !== 0 && (b.isDatetime || b.isNumeric))b.mixed = !0;
            j && b.mixed && (f[a] = e[a]);
            if (j && m && this.options.sort)for (a = 0; a < f.length; a++)f[a].reverse(), k && f[a].unshift(f[a].pop())
        }, dateFormats: {
            "YYYY-mm-dd": {
                regex: /^([0-9]{4})[\-\/\.]([0-9]{2})[\-\/\.]([0-9]{2})$/, parser: function (b) {
                    return Date.UTC(+b[1],
                        b[2] - 1, +b[3])
                }
            }, "dd/mm/YYYY": {
                regex: /^([0-9]{1,2})[\-\/\.]([0-9]{1,2})[\-\/\.]([0-9]{4})$/, parser: function (b) {
                    return Date.UTC(+b[3], b[2] - 1, +b[1])
                }, alternative: "mm/dd/YYYY"
            }, "mm/dd/YYYY": {
                regex: /^([0-9]{1,2})[\-\/\.]([0-9]{1,2})[\-\/\.]([0-9]{4})$/, parser: function (b) {
                    return Date.UTC(+b[3], b[1] - 1, +b[2])
                }
            }, "dd/mm/YY": {
                regex: /^([0-9]{1,2})[\-\/\.]([0-9]{1,2})[\-\/\.]([0-9]{2})$/, parser: function (b) {
                    return Date.UTC(+b[3] + 2E3, b[2] - 1, +b[1])
                }, alternative: "mm/dd/YY"
            }, "mm/dd/YY": {
                regex: /^([0-9]{1,2})[\-\/\.]([0-9]{1,2})[\-\/\.]([0-9]{2})$/,
                parser: function (b) {
                    return Date.UTC(+b[3] + 2E3, b[1] - 1, +b[2])
                }
            }
        }, parseDate: function (b) {
            var a = this.options.parseDate, e, f, d = this.options.dateFormat || this.dateFormat, c;
            a && (e = a(b));
            if (typeof b === "string") {
                if (d)a = this.dateFormats[d], (c = b.match(a.regex)) && (e = a.parser(c)); else for (f in this.dateFormats)if (a = this.dateFormats[f], c = b.match(a.regex)) {
                    this.dateFormat = f;
                    this.alternativeFormat = a.alternative;
                    e = a.parser(c);
                    break
                }
                c || (c = Date.parse(b), typeof c === "object" && c !== null && c.getTime ? e = c.getTime() - c.getTimezoneOffset() *
                    6E4 : typeof c === "number" && !isNaN(c) && (e = c - (new Date(c)).getTimezoneOffset() * 6E4))
            }
            return e
        }, rowsToColumns: function (b) {
            var a, e, f, d, c;
            if (b) {
                c = [];
                e = b.length;
                for (a = 0; a < e; a++) {
                    d = b[a].length;
                    for (f = 0; f < d; f++)c[f] || (c[f] = []), c[f][a] = b[a][f]
                }
            }
            return c
        }, parsed: function () {
            if (this.options.parsed)return this.options.parsed.call(this, this.columns)
        }, getFreeIndexes: function (b, a) {
            var e, f, d = [], c = [], h;
            for (f = 0; f < b; f += 1)d.push(!0);
            for (e = 0; e < a.length; e += 1) {
                h = a[e].getReferencedColumnIndexes();
                for (f = 0; f < h.length; f += 1)d[h[f]] = !1
            }
            for (f = 0; f < d.length; f += 1)d[f] && c.push(f);
            return c
        }, complete: function () {
            var b = this.columns, a, e = this.options, f, d, c, h, g = [], i;
            if (e.complete || e.afterComplete) {
                for (c = 0; c < b.length; c++)if (this.firstRowAsNames)b[c].name = b[c].shift();
                f = [];
                d = this.getFreeIndexes(b.length, this.valueCount.seriesBuilders);
                for (c = 0; c < this.valueCount.seriesBuilders.length; c++)i = this.valueCount.seriesBuilders[c], i.populateColumns(d) && g.push(i);
                for (; d.length > 0;) {
                    i = new j;
                    i.addColumnReader(0, "x");
                    c = r(0, d);
                    c !== -1 && d.splice(c, 1);
                    for (c =
                             0; c < this.valueCount.global; c++)i.addColumnReader(void 0, this.valueCount.globalPointArrayMap[c]);
                    i.populateColumns(d) && g.push(i)
                }
                g.length > 0 && g[0].readers.length > 0 && (i = b[g[0].readers[0].columnIndex], i !== void 0 && (i.isDatetime ? a = "datetime" : i.isNumeric || (a = "category")));
                if (a === "category")for (c = 0; c < g.length; c++) {
                    i = g[c];
                    for (d = 0; d < i.readers.length; d++)if (i.readers[d].configName === "x")i.readers[d].configName = "name"
                }
                for (c = 0; c < g.length; c++) {
                    i = g[c];
                    d = [];
                    for (h = 0; h < b[0].length; h++)d[h] = i.read(b, h);
                    f[c] = {data: d};
                    if (i.name)f[c].name = i.name;
                    if (a === "category")f[c].turboThreshold = 0
                }
                b = {series: f};
                if (a)b.xAxis = {type: a};
                e.complete && e.complete(b);
                e.afterComplete && e.afterComplete(b)
            }
        }
    });
    g.Data = p;
    g.data = function (b, a) {
        return new p(b, a)
    };
    g.wrap(g.Chart.prototype, "init", function (b, a, e) {
        var f = this;
        a && a.data ? g.data(g.extend(a.data, {
            afterComplete: function (d) {
                var c, h;
                if (a.hasOwnProperty("series"))if (typeof a.series === "object")for (c = Math.max(a.series.length, d.series.length); c--;)h = a.series[c] || {}, a.series[c] = g.merge(h, d.series[c]);
                else delete a.series;
                a = g.merge(d, a);
                b.call(f, a, e)
            }
        }), a) : b.call(f, a, e)
    });
    j = function () {
        this.readers = [];
        this.pointIsArray = !0
    };
    j.prototype.populateColumns = function (b) {
        var a = !0;
        k(this.readers, function (a) {
            if (a.columnIndex === void 0)a.columnIndex = b.shift()
        });
        k(this.readers, function (b) {
            b.columnIndex === void 0 && (a = !1)
        });
        return a
    };
    j.prototype.read = function (b, a) {
        var e = this.pointIsArray, f = e ? [] : {}, d;
        k(this.readers, function (c) {
            var d = b[c.columnIndex][a];
            e ? f.push(d) : f[c.configName] = d
        });
        if (this.name === void 0 && this.readers.length >=
            2 && (d = this.getReferencedColumnIndexes(), d.length >= 2))d.shift(), d.sort(), this.name = b[d.shift()].name;
        return f
    };
    j.prototype.addColumnReader = function (b, a) {
        this.readers.push({columnIndex: b, configName: a});
        if (!(a === "x" || a === "y" || a === void 0))this.pointIsArray = !1
    };
    j.prototype.getReferencedColumnIndexes = function () {
        var b, a = [], e;
        for (b = 0; b < this.readers.length; b += 1)e = this.readers[b], e.columnIndex !== void 0 && a.push(e.columnIndex);
        return a
    };
    j.prototype.hasReader = function (b) {
        var a, e;
        for (a = 0; a < this.readers.length; a +=
            1)if (e = this.readers[a], e.configName === b)return !0
    }
})(Highcharts);
