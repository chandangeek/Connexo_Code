Ext.define("Uni.override.ApplicationOverride", {override: "Ext.app.Application", unifyingControllers: ["Uni.controller.Acknowledgements", "Uni.controller.Configuration", "Uni.controller.Error", "Uni.controller.Navigation", "Uni.controller.Portal", "Uni.controller.Notifications", "Uni.controller.Search"], initControllers: function () {
    this.callParent(arguments);
    this.loadUnifyingControllers()
}, loadUnifyingControllers: function () {
    var c = this;
    for (var a = 0, b = c.unifyingControllers.length; a < b; a++) {
        c.getController(c.unifyingControllers[a])
    }
}});
Ext.define("Uni.override.panel.Panel", {override: "Ext.panel.Panel", beforeRender: function () {
    var a = this;
    this.callParent(arguments);
    if (a.subtitle) {
        this.setSubTitle(a.subtitle)
    }
}, setSubTitle: function (a) {
    var b = this, c = b.header;
    b.subtitle = a;
    if (c) {
        if (c.isHeader) {
            c.setSubTitle(a)
        } else {
            c.subtitle = a
        }
    } else {
        if (b.rendered) {
            b.updateHeader()
        }
    }
}});
Ext.define("Uni.override.panel.Header", {override: "Ext.panel.Header", headingTpl: ['<span id="{id}-textEl" class="{headerCls}-text {cls}-text {cls}-text-{ui}" unselectable="on"', '<tpl if="headerRole">', ' role="{headerRole}"', "</tpl>", ">{title}</span>", '<span id="{id}-subTextEl" class="{headerCls}-sub-text {cls}-sub-text {cls}-sub-text-{ui}" unselectable="on"', ">{subtitle}</span>"], initComponent: function () {
    var a = this;
    this.callParent(arguments);
    a.titleCmp.childEls.push("subTextEl")
}, setSubTitle: function (b) {
    var c = this, a = c.titleCmp;
    c.subtitle = b;
    if (a.rendered) {
        a.subTextEl.update(c.subtitle || "&#160;");
        a.updateLayout()
    } else {
        c.titleCmp.on({render: function () {
            c.setSubTitle(b)
        }, single: true})
    }
}});
Ext.define("Uni.override.ButtonOverride", {override: "Ext.button.Button", hrefTarget: "_self"});
Ext.define("Uni.override.JsonWriterOverride", {override: "Ext.data.writer.Json", getRecordData: function (a, b) {
    return a.getWriteData(true, true)
}});
Ext.define("Uni.override.StoreOverride", {override: "Ext.data.Store", pageSize: 10, getStore: function (c) {
    if (Ext.isEmpty(this.self.storeIdMap)) {
        this.self.storeIdMap = {}
    }
    var b = this.self.storeIdMap[c], a = null;
    if (b) {
        a = Ext.StoreManager.get(b)
    }
    if (!a) {
        a = Ext.create(this.getModuleClassName(c, "store"));
        this.self.storeIdMap[c] = a.storeId
    }
    return a
}});
Ext.define("Uni.override.ServerOverride", {override: "Ext.data.proxy.Server", processResponse: function (g, a, c, b, f, h) {
    var e = this, d, i;
    if (g === true) {
        d = e.getReader();
        d.applyDefaults = a.action === "read";
        i = d.read(e.extractResponseData(b));
        if (i.success !== false) {
            Ext.apply(a, {response: b, resultSet: i});
            a.commitRecords(i.records);
            a.setCompleted();
            a.setSuccessful()
        } else {
            Ext.apply(a, {response: b, resultSet: i});
            a.setException(i.message);
            e.fireEvent("exception", this, b, a)
        }
    } else {
        Ext.apply(a, {response: b, resultSet: i});
        e.setException(a, b);
        e.fireEvent("exception", this, b, a)
    }
    if (typeof f == "function") {
        f.call(h || e, a)
    }
    e.afterRequest(c, g)
}});
Ext.define("Uni.override.ModelOverride", {override: "Ext.data.Model", getWriteData: function (a, j) {
    var h = this, e = h.fields.items, i = e.length, d = {}, b, g, c;
    c = (typeof j === "undefined") ? false : j;
    for (g = 0; g < i; g++) {
        if (!c) {
            b = e[g].name;
            d[b] = h.get(b)
        } else {
            if (e[g].persist) {
                b = e[g].name;
                d[b] = h.get(b)
            }
        }
    }
    if (a === true) {
        Ext.apply(d, h.getAssociatedData(c))
    }
    return d
}, getAssociatedData: function (a) {
    return this.prepareAssociatedData({}, 1, a)
}, prepareAssociatedData: function (w, z, s) {
    var y = this, t = y.associations.items, e = t.length, x = {}, p = [], v = [], l = [], n, b, a, m, f, k, h, u, g, c, r, q, d, A;
    for (r = 0; r < e; r++) {
        c = t[r];
        u = c.associationId;
        h = w[u];
        if (h && h !== z) {
            continue
        }
        w[u] = z;
        d = c.type;
        A = c.name;
        if (d == "hasMany") {
            n = y[c.storeName];
            x[A] = [];
            if (n && n.getCount() > 0) {
                b = n.data.items;
                g = b.length;
                for (q = 0; q < g; q++) {
                    a = b[q];
                    x[A][q] = a.getWriteData(false, s);
                    p.push(a);
                    v.push(A);
                    l.push(q)
                }
            }
        } else {
            if (d == "belongsTo" || d == "hasOne") {
                a = y[c.instanceName];
                if (a !== undefined) {
                    x[A] = a.getWriteData(false, s);
                    p.push(a);
                    v.push(A);
                    l.push(-1)
                }
            }
        }
    }
    for (r = 0, g = p.length; r < g; ++r) {
        a = p[r];
        m = x[v[r]];
        f = l[r];
        k = a.prepareAssociatedData(w, z + 1, s);
        if (f === -1) {
            Ext.apply(m, k)
        } else {
            Ext.apply(m[f], k)
        }
    }
    return x
}});
Ext.define("Uni.override.RestOverride", {override: "Ext.data.proxy.Rest", buildUrl: function (g) {
    var j = this, f = g.operation, d = f.records || [], h = d[0], c = h ? h.getId() : f.id;
    if (typeof c !== "undefined") {
        c = encodeURIComponent(c);
        if (h) {
            h.setId(c)
        } else {
            f.id = c
        }
    }
    var b = j.callParent(arguments);
    var i = new Ext.Template(b), e = g.proxy.extraParams, a = i.apply(e);
    Ext.Object.each(e, function (k, m) {
        var l = new RegExp("{" + k + ".*?}")
    });
    g.url = b;
    return a
}});
Ext.define("Uni.override.FormOverride", {override: "Ext.form.Basic", hydrator: null, constructor: function (a) {
    this.callParent(arguments);
    if (a.hydrator) {
        this.setHydrator(Ext.create(a.hydrator))
    }
}, setHydrator: function (a) {
    this.hydrator = a
}, loadRecord: function (a) {
    if (!this.hydrator) {
        this.callParent(arguments)
    } else {
        this._record = a;
        return this.setValues(this.hydrator.extract(a))
    }
}, updateRecord: function (a) {
    a = a || this._record;
    if (this.hydrator) {
        var b = this.getFieldValues();
        a.beginEdit();
        this.hydrator.hydrate(b, a);
        a.endEdit();
        return this
    } else {
        return this.callParent(arguments)
    }
}});
Ext.define("Uni.model.Translation", {extend: "Ext.data.Model", fields: ["cmp", "key", "value"], idProperty: "key"});
Ext.define("Uni.store.Translations", {extend: "Ext.data.Store", model: "Uni.model.Translation", storeId: "translations", singleton: true, autoLoad: false, clearOnPageLoad: false, clearRemovedOnLoad: false, remoteFilter: false, config: {baseComponents: ["UNI"], components: []}, proxy: {type: "ajax", url: "/api/nls/thesaurus", pageParam: undefined, limitParam: undefined, startParam: undefined, reader: {type: "json", root: "translations"}, buildUrl: function (d) {
    var g = Uni.store.Translations.getBaseComponents(), c = Uni.store.Translations.getComponents();
    d.params.cmp = _.union(g, c);
    var b = this, e = b.format, a = b.getUrl(d), f = d.params.id;
    if (!a.match(/\/$/)) {
        a += "/"
    }
    if (typeof f !== "undefined") {
        a += f
    }
    if (e) {
        if (!a.match(/\.$/)) {
            a += "."
        }
        a += e
    }
    if (b.noCache) {
        a = Ext.urlAppend(a, Ext.String.format("{0}={1}", b.cacheString, Ext.Date.now()))
    }
    d.url = a;
    return a
}}});
Ext.define("Uni.I18n", {singleton: true, requires: ["Uni.store.Translations"], currencyFormatKey: "currencyFormat", decimalSeparatorKey: "decimalSeparator", thousandsSeparatorKey: "thousandsSeparator", init: function (a) {
    Uni.store.Translations.setComponents(a)
}, load: function (a) {
    a = (typeof a !== "undefined") ? a : function () {
    };
    Uni.store.Translations.load({callback: function () {
        a()
    }})
}, lookupTranslation: function (c, b) {
    var d, a;
    if (typeof b !== "undefined" && b) {
        a = Uni.store.Translations.findBy(function (e) {
            return e.data.key === c && e.data.cmp === b
        });
        d = Uni.store.Translations.getAt(a)
    } else {
        d = Uni.store.Translations.getById(c)
    }
    if (typeof d !== "undefined" && d !== null) {
        d = d.data.value
    } else {
    }
    return d
}, replaceAll: function (d, b, a) {
    var c = "{[" + b + "]}";
    return d.replace(new RegExp(c, "g"), a)
}, translate: function (d, b, f, a) {
    var e = this.lookupTranslation(d, b);
    if ((typeof e === "undefined" || e === null) && typeof f === "undefined" && f === null) {
        e = d
    }
    if ((typeof e === "undefined" || e === null) && typeof f !== "undefined" && f !== null) {
        e = f
    }
    if (typeof e !== "undefined" && e !== null && typeof a !== "undefined") {
        for (var c = 0; c < a.length; c++) {
            e = this.replaceAll(e, c, a[c])
        }
    }
    return e
}, translatePlural: function (c, b, a, f) {
    var d = c + "[" + b + "]", e = this.lookupTranslation(d, a);
    if (typeof e === "undefined") {
        e = this.lookupTranslation(c, a) || f
    }
    if (typeof b !== "undefined") {
        e = this.replaceAll(e, 0, b)
    }
    return e
}, formatDate: function (c, b, a, e) {
    b = b || new Date();
    var d = this.translate(c, a, e);
    return Ext.Date.format(b, d)
}, formatNumberWithSeparators: function (f, e, o, m) {
    var b = parseFloat(f), l = isNaN(e) ? 2 : Math.abs(e), k = o || ".", p = (typeof m === "undefined") ? "," : m, a = (b < 0) ? "-" : "", h = parseInt(b = Math.abs(b).toFixed(l)) + "", g = ((g = h.length) > 3) ? g % 3 : 0;
    return a + (g ? h.substr(0, g) + p : "") + h.substr(g).replace(/(\d{3})(?=\d)/g, "$1" + p) + (l ? k + Math.abs(b - h).toFixed(l).slice(2) : "")
}, formatNumber: function (d, b, a) {
    var c = this.translate(this.decimalSeparatorKey, b, "."), e = this.translate(this.thousandsSeparatorKey, b, ",");
    return this.formatNumberWithSeparators(d, a, c, e)
}, formatCurrency: function (d, b, a) {
    var c = this.formatNumber(d, b, a);
    return this.translate(this.currencyFormatKey, b, c, [c])
}});
Ext.define("Uni.override.MessageBoxOverride", {override: "Ext.window.MessageBox", buttonText: {ok: Uni.I18n.translate("window.messabox.ok", "UNI", "OK"), yes: Uni.I18n.translate("window.messabox.yes", "UNI", "Yes"), no: Uni.I18n.translate("window.messabox.no", "UNI", "No"), cancel: Uni.I18n.translate("window.messabox.cancel", "UNI", "Cancel")}});
Ext.define("Uni.override.window.MessageBox", {override: "Ext.window.MessageBox", shadow: false, reconfigure: function (a) {
    if (((typeof a) != "undefined") && a.ui) {
        this.ui = a.ui
    }
    this.callParent(arguments)
}, initComponent: function () {
    var a = this, b = a.title;
    a.title = null;
    this.callParent(arguments);
    this.topContainer.padding = 0;
    a.titleComponent = new Ext.panel.Header({title: b});
    a.promptContainer.insert(0, a.titleComponent)
}, setTitle: function (d) {
    var b = this, c = b.titleComponent;
    if (c) {
        var a = c.title
    }
    if (c) {
        if (c.isHeader) {
            c.setTitle(d)
        } else {
            c.title = d
        }
    } else {
        if (b.rendered) {
            b.updateHeader()
        }
    }
    b.fireEvent("titlechange", b, d, a)
}}, function () {
    Ext.MessageBox = Ext.Msg = new this()
});
Ext.define("Uni.override.form.field.Text", {override: "Ext.form.field.Text", labelAlign: "right", labelPad: 15, msgTarget: "under", blankText: "This is a required field"});
Ext.define("Uni.override.FieldBaseOverride", {override: "Ext.form.field.Base", labelSeparator: "", msgTarget: "under"});
Ext.define("Uni.override.form.field.Base", {override: "Ext.form.field.Base", labelAlign: "right", labelPad: 15, msgTarget: "under", blankText: "This is a required field", validateOnChange: false, validateOnBlur: false, getLabelCls: function () {
    var a = this.labelCls;
    if (this.required) {
        a += " uni-form-item-label-required"
    }
    return a
}, initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.override.CheckboxOverride", {override: "Ext.form.field.Checkbox", inputValue: true});
Ext.define("Uni.override.FieldContainerOverride", {override: "Ext.form.FieldContainer", labelSeparator: "", msgTarget: "side", labelAlign: "right", initComponent: function () {
    this.callParent();
    this.form = new Ext.form.Basic(this)
}, getValues: function () {
    return this.form.getValues()
}, setValues: function (a) {
    this.form.setValues(a)
}});
Ext.define("Uni.override.form.field.FieldContainer", {override: "Ext.form.FieldContainer", labelPad: 15, getLabelCls: function () {
    var a = this.labelCls;
    if (this.required) {
        a += " uni-form-item-label-required"
    }
    return a
}, initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.override.FieldSetOverride", {override: "Ext.form.FieldSet", initComponent: function () {
    this.callParent();
    this.form = new Ext.form.Basic(this);
    this.form.monitor.selector = "[isFormField]:not([excludeForm]){ownerCt.getId() === '" + this.getId() + "'}"
}, getValues: function () {
    var a = this.form.getValues();
    this.items.each(function (b) {
        if (_.isFunction(b.getValues)) {
            _.isEmpty(b.name) ? Ext.merge(a, b.getValues()) : a[b.name] = b.getValues()
        }
    });
    return a
}, setValues: function (a) {
    this.form.setValues(a);
    this.items.each(function (b) {
        if (_.isFunction(b.setValues)) {
            _.isEmpty(b.name) ? b.setValues(a) : b.setValues(a[b.name])
        }
    })
}});
Ext.define("Uni.override.form.Label", {override: "Ext.form.Label", cls: "x-form-item-label"});
Ext.define("Uni.override.form.Panel", {override: "Ext.form.Panel", buttonAlign: "left", initComponent: function () {
    var b = this;
    var a = 100;
    if (b.defaults && b.defaults.labelWidth) {
        a = b.defaults.labelWidth
    }
    if (b.defaults && b.defaults.labelAlign && b.defaults.labelAlign != "left") {
        a = 0
    }
    if (b.buttons) {
        b.buttons.splice(0, 0, {xtype: "tbspacer", width: a, cls: "x-form-item-label-right"})
    }
    b.callParent(arguments)
}});
Ext.define("Uni.override.form.field.ComboBox", {override: "Ext.form.field.ComboBox", anyMatch: true, initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.override.form.field.ComboBox", {override: "Ext.form.field.ComboBox", anyMatch: true, listeners: {change: function (a) {
    a.validate()
}}, initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.override.NumberFieldOverride", {override: "Ext.form.field.Number", fieldStyle: "text-align:right;", minText: "The minimum value is {0}", maxText: "The maximum value is {0}"});
Ext.define("Uni.override.form.field.Date", {override: "Ext.form.field.Date", format: "d/m/Y", initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.grid.plugin.ShowConditionalToolTip", {extend: "Ext.AbstractPlugin", requires: ["Ext.util.Format", "Ext.tip.ToolTip"], alias: "plugin.showConditionalToolTip", init: function (a) {
    var b = a.getView();
    b.on("refresh", this.setTooltip);
    b.on("resize", this.setTooltip);
    b.on("beforerefresh", this.destroyTooltips);
    b.on("beforedestroy", this.destroyTooltips, this, {single: true});
    a.on("beforedestroy", this.destroyHeaderTooltips, this, {single: true})
}, setTooltip: function (b) {
    var a = b.up("gridpanel");
    Ext.Array.each(a.columns, function (c) {
        var d = Ext.get(a.getEl().query("#" + c.id + "-titleEl")[0]);
        d.tooltip && d.tooltip.destroy();
        if (c.text && (d.getWidth(true) < d.getTextWidth())) {
            d.tooltip = Ext.create("Ext.tip.ToolTip", {target: d, html: c.text})
        }
        if (c.$className === "Ext.grid.column.Column" || c.$className === "Ext.grid.column.Date") {
            Ext.Array.each(b.getEl().query(".x-grid-cell-headerId-" + c.id), function (g) {
                var e = Ext.get(g), f = e.down(".x-grid-cell-inner"), h = f ? Ext.util.Format.stripTags(f.getHTML()) : false;
                e.tooltip && e.tooltip.destroy();
                if (h && (e.getWidth(true) < e.getTextWidth())) {
                    e.tooltip = Ext.create("Ext.tip.ToolTip", {target: e, html: h})
                }
            })
        }
    })
}, destroyHeaderTooltips: function (a) {
    Ext.Array.each(a.columns, function (b) {
        var c = Ext.get(a.getEl().query("#" + b.id + "-titleEl")[0]);
        c.tooltip && c.tooltip.destroy()
    })
}, destroyTooltips: function (a) {
    Ext.Array.each(a.getEl().query(".x-grid-cell"), function (c) {
        var b = Ext.get(c);
        b.tooltip && b.tooltip.destroy()
    })
}});
Ext.define("Uni.override.GridPanelOverride", {override: "Ext.grid.Panel", requires: ["Uni.grid.plugin.ShowConditionalToolTip"], plugins: ["showConditionalToolTip"], listeners: {cellclick: function (e, d, c, a) {
    var b = e.getHeaderCt().getHeaderAtIndex(c).getXType();
    if (b === "actioncolumn") {
        return true
    }
}}});
Ext.define("Uni.override.grid.Panel", {override: "Ext.grid.Panel", bodyBorder: true, enableColumnHide: false, enableColumnMove: false, enableColumnResize: false, sortableColumns: false, collapsible: false, overflowY: "auto", selModel: {mode: "SINGLE"}});
Ext.define("Uni.override.view.Table", {override: "Ext.view.Table", bodyBorder: true});
Ext.define("Uni.override.grid.plugin.BufferedRenderer", {override: "Ext.grid.plugin.BufferedRenderer", rowHeight: 29, init: function (a) {
    this.callParent(arguments);
    a.on("boxready", function () {
        a.view.refresh()
    })
}, bindStore: function (a) {
    var b = this;
    b.trailingBufferZone = 0;
    b.leadingBufferZone = a.pageSize;
    this.callParent(arguments)
}});
Ext.define("Uni.override.menu.Item", {override: "Ext.menu.Item", setHref: function (a, b) {
    this.href = !Ext.isDefined(a) ? "#" : a;
    this.hrefTarget = !Ext.isDefined(b) ? "_self" : b || this.hrefTarget;
    if (Ext.isDefined(this.itemEl)) {
        this.itemEl.set({href: this.href, hrefTarget: this.hrefTarget})
    }
}});
Ext.define("Uni.About", {singleton: true, version: "1.0.0", startup: new Date(), baseCssPrefix: "uni-"});
Ext.define("Uni.model.Privilege", {extend: "Ext.data.Model", fields: ["name"], idProperty: "name", proxy: {type: "rest", url: "/api/usr/users/privileges", reader: {type: "json", root: "privileges"}}});
Ext.define("Uni.store.Privileges", {extend: "Ext.data.Store", model: "Uni.model.Privilege", storeId: "userPrivileges", singleton: true, autoLoad: false, clearOnPageLoad: false, clearRemovedOnLoad: false, remoteFilter: false});
Ext.define("Uni.Auth", {singleton: true, requires: ["Uni.store.Privileges"], load: function (a) {
    a = (typeof a !== "undefined") ? a : function () {
    };
    Uni.store.Privileges.load({callback: function () {
        a()
    }})
}, hasPrivilege: function (a) {
    for (var b = 0; b < Uni.store.Privileges.getCount(); b++) {
        if (a === Uni.store.Privileges.getAt(b).get("name")) {
            return true
        }
    }
    return false
}, hasNoPrivilege: function (a) {
    return !this.hasPrivilege(a)
}, hasAnyPrivilege: function (c) {
    if (Ext.isArray(c)) {
        for (var b = 0; b < c.length; b++) {
            var a = c[b];
            if (this.hasPrivilege(a)) {
                return true
            }
        }
    }
    return false
}});
Ext.define("Uni.view.window.Acknowledgement", {extend: "Ext.window.Window", xtype: "acknowledgement-window", autoShow: true, resizable: false, bodyBorder: false, shadow: false, animCollapse: true, border: false, header: false, cls: Uni.About.baseCssPrefix + "window-acknowledgement", layout: {type: "hbox", align: "center"}, setMessage: function (b) {
    var a = this.down("#msgmessage");
    a.removeAll();
    a.add({xtype: "label", html: b})
}, initComponent: function () {
    var a = this;
    a.items = [
        {xtype: "component", cls: "icon"},
        {xtype: "panel", itemId: "msgmessage", cls: "message", layout: {type: "vbox", align: "left"}},
        {xtype: "component", html: "&nbsp;", flex: 1},
        {xtype: "button", iconCls: "close", ui: "close", width: 28, height: 28, handler: function () {
            a.close()
        }}
    ];
    a.callParent(arguments)
}});
Ext.define("Uni.controller.Acknowledgements", {extend: "Ext.app.Controller", requires: ["Uni.view.window.Acknowledgement"], init: function () {
    this.getApplication().on("acknowledge", this.showAcknowledgement)
}, showAcknowledgement: function (c) {
    var b = Ext.widget("acknowledgement-window"), a = new Ext.util.DelayedTask(function () {
        b.close()
    });
    b.setMessage(c);
    b.center();
    b.setPosition(b.x, 116, false);
    a.delay(5000)
}});
Ext.define("Uni.controller.Configuration", {extend: "Ext.app.Controller", refs: [
    {ref: "logo", selector: "navigationLogo"},
    {ref: "appSwitcher", selector: "navigationAppSwitcher"}
], init: function () {
    this.getApplication().on("changeapptitleevent", this.changeAppTitle, this);
    this.getApplication().on("changeappglyphevent", this.changeAppGlyph, this)
}, changeAppTitle: function (b) {
    var a = this.getLogo();
    a.setLogoTitle(b)
}, changeAppGlyph: function (a) {
    var b = this.getLogo();
    b.setLogoGlyph(a)
}});
Ext.define("Uni.view.error.Window", {extend: "Ext.window.Window", alias: "widget.errorWindow", requires: [], width: 600, height: 350, layout: "fit", modal: true, constrain: true, closeAction: "hide", title: "Error message", items: [
    {xtype: "textareafield", itemId: "messagefield", margin: 10}
], initComponent: function () {
    this.buttons = [
        {text: "Report issue", action: "report", disabled: true},
        {text: "Close", scope: this, handler: this.close}
    ];
    this.callParent(arguments)
}, setErrorMessage: function (a) {
    var b = this.down("#messagefield");
    b.setValue(a)
}});
Ext.define("Ext.ux.window.Notification", {extend: "Ext.window.Window", alias: "widget.uxNotification", cls: "ux-notification-window", autoClose: true, autoHeight: true, plain: false, draggable: false, shadow: false, focus: Ext.emptyFn, manager: null, useXAxis: false, position: "br", spacing: 6, paddingX: 30, paddingY: 10, slideInAnimation: "easeIn", slideBackAnimation: "bounceOut", slideInDuration: 1500, slideBackDuration: 1000, hideDuration: 500, autoCloseDelay: 7000, stickOnClick: true, stickWhileHover: true, isHiding: false, isFading: false, destroyAfterHide: false, closeOnMouseOut: false, xPos: 0, yPos: 0, statics: {defaultManager: {el: null}}, initComponent: function () {
    var a = this;
    if (Ext.isDefined(a.corner)) {
        a.position = a.corner
    }
    if (Ext.isDefined(a.slideDownAnimation)) {
        a.slideBackAnimation = a.slideDownAnimation
    }
    if (Ext.isDefined(a.autoDestroyDelay)) {
        a.autoCloseDelay = a.autoDestroyDelay
    }
    if (Ext.isDefined(a.autoHideDelay)) {
        a.autoCloseDelay = a.autoHideDelay
    }
    if (Ext.isDefined(a.autoHide)) {
        a.autoClose = a.autoHide
    }
    if (Ext.isDefined(a.slideInDelay)) {
        a.slideInDuration = a.slideInDelay
    }
    if (Ext.isDefined(a.slideDownDelay)) {
        a.slideBackDuration = a.slideDownDelay
    }
    if (Ext.isDefined(a.fadeDelay)) {
        a.hideDuration = a.fadeDelay
    }
    a.position = a.position.replace(/c/, "");
    a.updateAlignment(a.position);
    a.setManager(a.manager);
    a.callParent(arguments)
}, onRender: function () {
    var a = this;
    a.callParent(arguments);
    a.el.hover(function () {
        a.mouseIsOver = true
    }, function () {
        a.mouseIsOver = false;
        if (a.closeOnMouseOut) {
            a.closeOnMouseOut = false;
            a.close()
        }
    }, a)
}, updateAlignment: function (a) {
    var b = this;
    switch (a) {
        case"br":
            b.paddingFactorX = -1;
            b.paddingFactorY = -1;
            b.siblingAlignment = "br-br";
            if (b.useXAxis) {
                b.managerAlignment = "bl-br"
            } else {
                b.managerAlignment = "tr-br"
            }
            break;
        case"bl":
            b.paddingFactorX = 1;
            b.paddingFactorY = -1;
            b.siblingAlignment = "bl-bl";
            if (b.useXAxis) {
                b.managerAlignment = "br-bl"
            } else {
                b.managerAlignment = "tl-bl"
            }
            break;
        case"tr":
            b.paddingFactorX = -1;
            b.paddingFactorY = 1;
            b.siblingAlignment = "tr-tr";
            if (b.useXAxis) {
                b.managerAlignment = "tl-tr"
            } else {
                b.managerAlignment = "br-tr"
            }
            break;
        case"tl":
            b.paddingFactorX = 1;
            b.paddingFactorY = 1;
            b.siblingAlignment = "tl-tl";
            if (b.useXAxis) {
                b.managerAlignment = "tr-tl"
            } else {
                b.managerAlignment = "bl-tl"
            }
            break;
        case"b":
            b.paddingFactorX = 0;
            b.paddingFactorY = -1;
            b.siblingAlignment = "b-b";
            b.useXAxis = 0;
            b.managerAlignment = "t-b";
            break;
        case"t":
            b.paddingFactorX = 0;
            b.paddingFactorY = 1;
            b.siblingAlignment = "t-t";
            b.useXAxis = 0;
            b.managerAlignment = "b-t";
            break;
        case"l":
            b.paddingFactorX = 1;
            b.paddingFactorY = 0;
            b.siblingAlignment = "l-l";
            b.useXAxis = 1;
            b.managerAlignment = "r-l";
            break;
        case"r":
            b.paddingFactorX = -1;
            b.paddingFactorY = 0;
            b.siblingAlignment = "r-r";
            b.useXAxis = 1;
            b.managerAlignment = "l-r";
            break
    }
}, getXposAlignedToManager: function () {
    var a = this;
    var b = 0;
    if (a.manager && a.manager.el && a.manager.el.dom) {
        if (!a.useXAxis) {
            return a.el.getLeft()
        } else {
            if (a.position == "br" || a.position == "tr" || a.position == "r") {
                b += a.manager.el.getAnchorXY("r")[0];
                b -= (a.el.getWidth() + a.paddingX)
            } else {
                b += a.manager.el.getAnchorXY("l")[0];
                b += a.paddingX
            }
        }
    }
    return b
}, getYposAlignedToManager: function () {
    var b = this;
    var a = 0;
    if (b.manager && b.manager.el && b.manager.el.dom) {
        if (b.useXAxis) {
            return b.el.getTop()
        } else {
            if (b.position == "br" || b.position == "bl" || b.position == "b") {
                a += b.manager.el.getAnchorXY("b")[1];
                a -= (b.el.getHeight() + b.paddingY)
            } else {
                a += b.manager.el.getAnchorXY("t")[1];
                a += b.paddingY
            }
        }
    }
    return a
}, getXposAlignedToSibling: function (a) {
    var b = this;
    if (b.useXAxis) {
        if (b.position == "tl" || b.position == "bl" || b.position == "l") {
            return(a.xPos + a.el.getWidth() + a.spacing)
        } else {
            return(a.xPos - b.el.getWidth() - b.spacing)
        }
    } else {
        return b.el.getLeft()
    }
}, getYposAlignedToSibling: function (a) {
    var b = this;
    if (b.useXAxis) {
        return b.el.getTop()
    } else {
        if (b.position == "tr" || b.position == "tl" || b.position == "t") {
            return(a.yPos + a.el.getHeight() + a.spacing)
        } else {
            return(a.yPos - b.el.getHeight() - a.spacing)
        }
    }
}, getNotifications: function (b) {
    var a = this;
    if (!a.manager.notifications[b]) {
        a.manager.notifications[b] = []
    }
    return a.manager.notifications[b]
}, setManager: function (a) {
    var b = this;
    b.manager = a;
    if (typeof b.manager == "string") {
        b.manager = Ext.ComponentQuery.query(b.manager)[0]
    }
    if (!b.manager) {
        b.manager = b.statics().defaultManager;
        if (!b.manager.el) {
            b.manager.el = Ext.getBody()
        }
    }
    if (typeof b.manager.notifications == "undefined") {
        b.manager.notifications = {}
    }
}, beforeShow: function () {
    var a = this;
    if (a.stickOnClick) {
        if (a.body && a.body.dom) {
            Ext.fly(a.body.dom).on("click", function () {
                a.cancelAutoClose();
                a.addCls("notification-fixed")
            }, a)
        }
    }
    if (a.autoClose) {
        a.task = new Ext.util.DelayedTask(a.doAutoClose, a);
        a.task.delay(a.autoCloseDelay)
    }
    a.el.setX(-10000);
    a.el.setOpacity(0.9)
}, afterShow: function () {
    var b = this;
    b.callParent(arguments);
    var a = b.getNotifications(b.managerAlignment);
    if (a.length) {
        b.el.alignTo(a[a.length - 1].el, b.siblingAlignment, [0, 0]);
        b.xPos = b.getXposAlignedToSibling(a[a.length - 1]);
        b.yPos = b.getYposAlignedToSibling(a[a.length - 1])
    } else {
        b.el.alignTo(b.manager.el, b.managerAlignment, [(b.paddingX * b.paddingFactorX), (b.paddingY * b.paddingFactorY)], false);
        b.xPos = b.getXposAlignedToManager();
        b.yPos = b.getYposAlignedToManager()
    }
    Ext.Array.include(a, b);
    b.el.animate({from: {x: b.el.getX(), y: b.el.getY()}, to: {x: b.xPos, y: b.yPos, opacity: 0.9}, easing: b.slideInAnimation, duration: b.slideInDuration, dynamic: true})
}, slideBack: function () {
    var c = this;
    var b = c.getNotifications(c.managerAlignment);
    var a = Ext.Array.indexOf(b, c);
    if (!c.isHiding && c.el && c.manager && c.manager.el && c.manager.el.dom && c.manager.el.isVisible()) {
        if (a) {
            c.xPos = c.getXposAlignedToSibling(b[a - 1]);
            c.yPos = c.getYposAlignedToSibling(b[a - 1])
        } else {
            c.xPos = c.getXposAlignedToManager();
            c.yPos = c.getYposAlignedToManager()
        }
        c.stopAnimation();
        c.el.animate({to: {x: c.xPos, y: c.yPos}, easing: c.slideBackAnimation, duration: c.slideBackDuration, dynamic: true})
    }
}, cancelAutoClose: function () {
    var a = this;
    if (a.autoClose) {
        a.task.cancel()
    }
}, doAutoClose: function () {
    var a = this;
    if (!(a.stickWhileHover && a.mouseIsOver)) {
        a.close()
    } else {
        a.closeOnMouseOut = true
    }
}, removeFromManager: function () {
    var c = this;
    if (c.manager) {
        var b = c.getNotifications(c.managerAlignment);
        var a = Ext.Array.indexOf(b, c);
        if (a != -1) {
            Ext.Array.erase(b, a, 1);
            for (; a < b.length; a++) {
                b[a].slideBack()
            }
        }
    }
}, hide: function () {
    var a = this;
    if (a.isHiding) {
        if (!a.isFading) {
            a.callParent(arguments);
            a.isHiding = false
        }
    } else {
        a.isHiding = true;
        a.isFading = true;
        a.cancelAutoClose();
        if (a.el) {
            a.el.fadeOut({opacity: 0, easing: "easeIn", duration: a.hideDuration, remove: a.destroyAfterHide, listeners: {afteranimate: function () {
                a.isFading = false;
                a.removeCls("notification-fixed");
                a.removeFromManager();
                a.hide(a.animateTarget, a.doClose, a)
            }}})
        }
    }
    return a
}, destroy: function () {
    var a = this;
    if (!a.hidden) {
        a.destroyAfterHide = true;
        a.hide(a.animateTarget, a.doClose, a)
    } else {
        a.callParent(arguments)
    }
}});
Ext.define("Uni.controller.Error", {extend: "Ext.app.Controller", requires: ["Uni.view.error.Window", "Ext.ux.window.Notification"], config: {window: null}, refs: [
    {ref: "contentPanel", selector: "viewport > #contentPanel"}
], init: function () {
    var a = this;
    Ext.Error.handle = a.handleGenericError;
    Ext.Ajax.on("requestexception", a.handleRequestError, a)
}, handleGenericError: function (a) {
    var b = Uni.I18n.translate("error.requestFailed", "UNI", "Request failed");
    this.showError(b, a)
}, handleRequestError: function (f, b, c) {
    var g = Uni.I18n.translate("error.requestFailed", "UNI", "Request failed"), e = b.responseText || b.statusText, a = Ext.decode(e, true);
    if (Ext.isDefined(a) && a !== null) {
        if (!Ext.isEmpty(a.message)) {
            e = a.message
        } else {
            if (Ext.isDefined(a.errors) && Ext.isArray(a.errors)) {
                if (1 === a.errors.length) {
                    e = a.errors[0].msg
                } else {
                    if (1 < a.errors.length) {
                        e = "<ul>";
                        for (var d = 0; d < a.errors.length; d++) {
                            e += "<li>" + a.errors[d].msg + "</li>"
                        }
                        e += "</ul>"
                    } else {
                        e = Uni.I18n.translate("error.unknownErrorOccurred", "UNI", "An unknown error occurred.")
                    }
                }
            }
        }
    }
    if (Ext.isEmpty(e)) {
        g = Uni.I18n.translate("error.connectionProblemsTitle", "UNI", "Unexpected connection problems");
        e = Uni.I18n.translate("error.connectionProblemsMessage", "UNI", "Unexpected connection problems. Please check that server is available.")
    }
    switch (b.status) {
        case 400:
            if (a && a.message) {
                g = Uni.I18n.translate("error.requestFailed", "UNI", "Request failed");
                this.showError(g, e)
            }
            break;
        case 500:
            g = Uni.I18n.translate("error.internalServerError", "UNI", "Internal server error");
            e = Uni.I18n.translate("error.internalServerErrorMessage", "UNI", "Please contact your system administrator.");
            this.showError(g, e);
            break;
        case 404:
            g = Uni.I18n.translate("error.requestFailed", "UNI", "Request failed");
            e = Uni.I18n.translate("error.notFoundErrorMessage", "UNI", "Please contact your system administrator.");
            this.showError(g, e);
            break;
        case 401:
            this.getApplication().fireEvent("sessionexpired");
            break;
        case 403:
        case 418:
        default:
            this.showError(g, e);
            break
    }
}, showError: function (d, c, a) {
    a = a ? a : {};
    Ext.apply(a, {title: d, msg: c, modal: false, ui: "message-error", icon: Ext.MessageBox.ERROR});
    var b = Ext.create("Ext.window.MessageBox", {buttons: [
        {xtype: "button", text: Uni.I18n.translate("general.close", "UNI", "Close"), action: "close", name: "close", ui: "action", handler: function () {
            b.close()
        }}
    ]});
    b.show(a)
}});
Ext.define("Uni.controller.history.EventBus", {extend: "Ext.app.Controller", requires: ["Ext.util.History"], config: {defaultToken: "", previousPath: null, currentPath: null}, onLaunch: function () {
    this.initHistory()
}, initHistory: function () {
    var a = this;
    Ext.util.History.init(function () {
        Ext.util.History.addListener("change", function (b) {
            a.onHistoryChange(b)
        });
        a.checkHistoryState()
    })
}, checkHistoryState: function () {
    var b = this, a = Ext.util.History.getToken();
    if (a === null || a === "") {
        a = b.getDefaultToken();
        Ext.util.History.add(a)
    }
    b.onHistoryChange(a)
}, onHistoryChange: function (b) {
    var a = b.indexOf("?");
    if (a > 0) {
        b = b.substring(0, a)
    }
    if (this.getCurrentPath() !== null) {
        this.setPreviousPath(this.getCurrentPath())
    }
    this.setCurrentPath(b);
    crossroads.parse(b)
}});
Ext.define("Uni.model.MenuItem", {extend: "Ext.data.Model", fields: ["text", "portal", "href", "glyph", "index", "hidden"], proxy: {type: "memory"}});
Ext.define("Uni.store.MenuItems", {extend: "Ext.data.Store", model: "Uni.model.MenuItem", storeId: "menuItems", singleton: true, autoLoad: false, clearOnPageLoad: false, clearRemovedOnLoad: false, proxy: {type: "memory", reader: {type: "json", root: "items"}}, sorters: [
    {property: "index", direction: "DESC"}
]});
Ext.define("Uni.model.AppItem", {extend: "Ext.data.Model", fields: ["name", "basePath", "startPage", "icon", "mainController", "scripts", "translationComponents", "styleSheets", "dependencies"]});
Ext.define("Uni.store.AppItems", {extend: "Ext.data.Store", model: "Uni.model.AppItem", storeId: "appitems", singleton: true, autoLoad: true, proxy: {type: "ajax", url: "/api/apps/pages", reader: {type: "json", root: ""}}});
Ext.define("Uni.model.App", {extend: "Ext.data.Model", fields: ["name", {name: "url", convert: function (b, a) {
    if (b.indexOf("#") === -1 && b.indexOf("http") === -1) {
        b += "#"
    }
    return b
}}, "icon", {name: "isActive", persist: false, convert: function (d, a) {
    var b = window.location.href, e = window.location.pathname, c = e + window.location.hash;
    return b.indexOf(a.data.url, 0) === 0 || c.indexOf(a.data.url, 0) === 0
}}, {name: "isExternal", persist: false, convert: function (c, a) {
    var b = a.get("url");
    return b.indexOf("http") === 0
}}]});
Ext.define("Uni.store.Apps", {extend: "Ext.data.Store", model: "Uni.model.App", storeId: "apps", singleton: true, autoLoad: false, proxy: {type: "ajax", url: "/api/apps/apps", reader: {type: "json", root: ""}}});
Ext.define("Uni.view.container.ContentContainer", {extend: "Ext.container.Container", alias: "widget.contentcontainer", ui: "contentcontainer", requires: [], layout: "border", side: null, content: null, items: [
    {region: "west", xtype: "container", itemId: "westContainer", cls: "west"},
    {region: "center", xtype: "container", itemId: "centerContainer", cls: "center", overflowY: "auto", layout: {type: "vbox", align: "stretch"}, items: []}
], initComponent: function () {
    var a = this.side, b = this.content;
    if (!(a instanceof Ext.Component)) {
        a = Ext.clone(a)
    }
    this.items[0].items = a;
    if (!(b instanceof Ext.Component)) {
        b = Ext.clone(b)
    }
    this.items[1].items = b;
    this.callParent(arguments)
}, getNorthContainer: function () {
    return this.down("#northContainer")
}, getWestContainer: function () {
    return this.down("#westContainer")
}, getCenterContainer: function () {
    return this.down("#centerContainer")
}});
Ext.define("Uni.controller.history.Router", {extend: "Ext.app.Controller", config: {}, routes: {}, defaultAction: "showOverview", currentRoute: null, arguments: {}, queryParams: {}, filter: null, addConfig: function (a) {
    _.extend(this.config, a);
    var b = this;
    _.each(a, function (d, c) {
        b.initRoute(c, d)
    })
}, getQueryString: function () {
    var b = Ext.util.History.getToken() || document.location.href.split("?")[1], a = b.indexOf("?");
    return a < 0 ? "" : b.substring(a + 1)
}, getQueryStringValues: function () {
    var a = this.getQueryString();
    if (typeof a !== "undefined") {
        return Ext.Object.fromQueryString(this.getQueryString())
    }
    return{}
}, queryParamsToString: function (a) {
    return Ext.urlEncode(_.object(_.keys(a), _.map(a, function (b) {
        return _.isString(b) ? b : Ext.JSON.encodeValue(b)
    })))
}, initRoute: function (c, b, f) {
    var d = this;
    f = typeof f !== "undefined" ? f : "";
    var a = f + "/" + b.route;
    var e = typeof b.action !== "undefined" ? b.action : d.defaultAction;
    var g = typeof b.params !== "undefined" ? b.params : {};
    d.routes[c] = _.extend(b, {path: a, getTitle: function () {
        var h = this;
        return _.isFunction(this.title) ? this.title.apply(d, [h]) : this.title
    }, setTitle: function (h) {
        this.title = h;
        d.fireEvent("routechange", this)
    }, buildUrl: function (j, i) {
        j = Ext.applyIf(j || {}, d.arguments);
        i = Ext.applyIf(i || {}, d.queryParams);
        var h = this.crossroad ? "#" + this.crossroad.interpolate(j) : "#" + this.path;
        return _.isEmpty(i) ? h : h + "?" + d.queryParamsToString(i)
    }, forward: function (i, h) {
        window.location.href = this.buildUrl(i, h)
    }});
    if (d.routes[c].callback) {
        d.routes[c].callback.apply(d, [d.routes[c]])
    }
    if (!b.disabled) {
        d.routes[c].crossroad = crossroads.addRoute(a, function () {
            d.currentRoute = c;
            d.queryParams = Ext.Object.fromQueryString(d.getQueryString());
            d.arguments = _.object(d.routes[c].crossroad._paramsIds, arguments);
            var k = _.values(_.extend(d.arguments, g));
            if (Ext.isDefined(b.redirect)) {
                if (Ext.isObject(b.redirect)) {
                    var j = _.extend(d.arguments, b.redirect.params);
                    d.getRoute(b.redirect.route).forward(j)
                } else {
                    if (Ext.isString(b.redirect)) {
                        d.getRoute(b.redirect).forward(d.arguments)
                    } else {
                        throw"config redirect must be a string or an object"
                    }
                }
            } else {
                var h = d.getController(b.controller);
                var i = function () {
                    d.fireEvent("routematch", d);
                    h[e].apply(h, k)
                };
                if (b.filter) {
                    Ext.ModelManager.getModel(b.filter).load(null, {callback: function (l) {
                        d.filter = l || Ext.create(b.filter);
                        i()
                    }})
                } else {
                    i()
                }
            }
        })
    }
    if (b.items) {
        _.each(b.items, function (h, j) {
            if (Ext.isArray(h.privileges) && !Uni.Auth.hasAnyPrivilege(h.privileges)) {
                return
            }
            var i = c + "/" + j;
            d.initRoute(i, h, a)
        })
    }
}, buildBreadcrumbs: function (d) {
    var c = this;
    d = typeof d === "undefined" ? c.currentRoute.split("/") : d.split("/");
    var b = [];
    do {
        var a = c.getRoute(d.join("/"));
        b.push(a);
        d.pop()
    } while (d.length);
    return b
}, getRoute: function (b) {
    var a = this;
    if (!Ext.isDefined(b)) {
        b = a.currentRoute
    }
    return a.routes[b]
}, getRouteConfig: function (c) {
    var a = me.routeConfig;
    c = c.split("/");
    do {
        var b = c.shift();
        a = a[b];
        if (b !== "items" && c.length) {
            c.splice(0, 0, "items")
        }
    } while (c.length);
    return a
}});
Ext.define("Uni.controller.Navigation", {extend: "Ext.app.Controller", requires: ["Uni.controller.history.EventBus", "Uni.store.MenuItems", "Uni.store.AppItems", "Uni.store.Apps", "Uni.view.container.ContentContainer", "Uni.controller.history.Router"], views: [], refs: [
    {ref: "navigationMenu", selector: "navigationMenu"},
    {ref: "contentWrapper", selector: "viewport > #contentPanel"},
    {ref: "breadcrumbs", selector: "breadcrumbTrail"},
    {ref: "searchButton", selector: "navigationHeader #globalSearch"}
], applicationTitle: "Connexo Multi Sense", applicationTitleSeparator: "-", searchEnabled: true, init: function () {
    var a = this;
    Ext.util.History.addListener("change", function () {
        a.selectMenuItemByActiveToken()
    });
    this.initApps();
    this.initMenuItems();
    this.control({navigationMenu: {afterrender: this.onAfterRenderNavigationMenu}, navigationAppSwitcher: {afterrender: this.resetAppSwitcherState}, "navigationHeader #globalSearch": {afterrender: this.initSearch}});
    this.getApplication().on("changemaincontentevent", this.showContent, this);
    this.getApplication().on("changemainbreadcrumbevent", this.initTitle, this);
    this.getApplication().on("changemainbreadcrumbevent", this.setBreadcrumb, this);
    this.getController("Uni.controller.history.Router").on("routematch", this.initBreadcrumbs, this);
    this.getController("Uni.controller.history.Router").on("routechange", this.initBreadcrumbs, this)
}, initApps: function () {
    Uni.store.Apps.load()
}, initTitle: function (a) {
    var b = this, c = "";
    if (Ext.isObject(a)) {
        c = a.get("text");
        while (Ext.isDefined(a.getAssociatedData()["Uni.model.BreadcrumbItem"])) {
            a = a.getChild();
            c = a.get("text")
        }
    }
    if (!Ext.isEmpty(c)) {
        Ext.getDoc().dom.title = b.applicationTitle + " " + b.applicationTitleSeparator + " " + c
    } else {
        Ext.getDoc().dom.title = b.applicationTitle
    }
}, initBreadcrumbs: function () {
    var c = this;
    var b = c.getController("Uni.controller.history.Router");
    var a = c.getBreadcrumbs();
    var e, d;
    a.removeAll();
    _.map(b.buildBreadcrumbs(), function (f) {
        var g = f.getTitle();
        d = Ext.create("Uni.model.BreadcrumbItem", {text: Ext.isString(g) ? g : "", href: f.buildUrl(), relative: false});
        if (e) {
            d.setChild(e)
        }
        e = d
    });
    c.initTitle(d);
    a.setBreadcrumbItem(d)
}, initSearch: function () {
    var a = this;
    a.getSearchButton().setVisible(a.searchEnabled)
}, onAfterRenderNavigationMenu: function () {
    this.refreshNavigationMenu();
    this.selectMenuItemByActiveToken()
}, initMenuItems: function () {
    Uni.store.MenuItems.on({add: this.refreshNavigationMenu, load: this.refreshNavigationMenu, update: this.refreshNavigationMenu, remove: this.refreshNavigationMenu, bulkremove: this.refreshNavigationMenu, scope: this})
}, initAppItems: function () {
    Uni.store.AppItems.on({add: this.resetAppSwitcherState, load: this.resetAppSwitcherState, update: this.resetAppSwitcherState, remove: this.resetAppSwitcherState, bulkremove: this.resetAppSwitcherState, scope: this});
    Uni.store.AppItems.load()
}, resetAppSwitcherState: function () {
    var a = Uni.store.AppItems.getCount();
    if (a > 0) {
        this.getAppSwitcher().enable()
    } else {
        this.getAppSwitcher().disable()
    }
}, refreshNavigationMenu: function () {
    var b = this.getNavigationMenu(), a = Uni.store.MenuItems;
    this.removeDuplicatesFromStore(a);
    if (b !== undefined) {
        if (b.rendered) {
            Ext.suspendLayouts()
        }
        b.removeAllMenuItems();
        a.each(function (c) {
            b.addMenuItem(c)
        });
        if (b.rendered) {
            Ext.resumeLayouts(true)
        }
    }
}, removeDuplicatesFromStore: function (b) {
    var a = [], c = [];
    b.each(function (e) {
        var f = e.get("text"), d = e.get("portal");
        if (a[f + d]) {
            c.push(e)
        } else {
            a[f + d] = true
        }
    });
    b.remove(c)
}, addMenuItem: function (d, a, c) {
    a = portal ? "#/" + portal : a;
    var b = {text: d, tooltip: d, href: a, glyph: c};
    this.getNavigationMenu().addMenuItem(b)
}, selectMenuItemByActiveToken: function () {
    var b = this, a = Ext.util.History.getToken(), c = b.stripAndSplitToken(a);
    b.getNavigationMenu().deselectAllMenuItems();
    Uni.store.MenuItems.each(function (d) {
        modelTokens = b.stripAndSplitToken(d.get("href"));
        if (c[0] === modelTokens[0] || c[0] === d.get("portal")) {
            b.getNavigationMenu().selectMenuItem(d);
            return
        }
    })
}, stripAndSplitToken: function (a) {
    if (a) {
        a = a.indexOf(Uni.controller.history.Settings.tokenDelimiter) === 0 ? a.substring(1) : a;
        a = a.replace(/#\/|#/g, "");
        return a.split(Uni.controller.history.Settings.tokenDelimiter)
    } else {
        return[]
    }
}, showContent: function (c, a) {
    this.getContentWrapper().removeAll();
    if (c instanceof Uni.view.container.ContentContainer) {
        a = c.side;
        c = c.content
    }
    var b = new Ext.widget("contentcontainer", {content: c, side: a});
    this.getContentWrapper().add(b);
    this.getContentWrapper().doComponentLayout()
}, setBreadcrumb: function (b) {
    var a = this.getBreadcrumbs();
    a.setBreadcrumbItem(b)
}});
Ext.define("Uni.view.notifications.Anchor", {extend: "Ext.button.Button", alias: "widget.notificationsAnchor", text: "", action: "preview", glyph: "xe012@icomoon", scale: "small", cls: "notifications-anchor", disabled: true, menu: [
    {xtype: "dataview", tpl: ['<tpl for=".">', '<div class="notification-item">', "<p>{message}</p>", "</div>", "</tpl>"], itemSelector: "div.notification-item", store: "notifications"}
]});
Ext.define("Uni.model.Notification", {extend: "Ext.data.Model", fields: ["message", "type", "timeadded", "timeseen", "callback"], constructor: function () {
    var a = arguments[0] || {};
    if (!a.timeadded) {
        a.timeadded = new Date()
    }
    if (arguments.length === 0) {
        this.callParent([a])
    } else {
        this.callParent(arguments)
    }
}, proxy: {type: "memory"}});
Ext.define("Uni.store.Notifications", {extend: "Ext.data.Store", model: "Uni.model.Notification", storeId: "notifications", singleton: true, autoLoad: false, clearOnPageLoad: false, clearRemovedOnLoad: false, proxy: {type: "memory"}});
Ext.define("Uni.controller.Notifications", {extend: "Ext.app.Controller", requires: ["Uni.view.notifications.Anchor", "Uni.store.Notifications"], refs: [
    {ref: "anchor", selector: "notificationsAnchor"}
], init: function () {
    this.getApplication().on("addnotificationevent", this.addNotification, this);
    Uni.store.Notifications.on({add: this.resetAnchorCount, load: this.resetAnchorCount, update: this.resetAnchorCount, remove: this.resetAnchorCount, bulk: this.resetAnchorCount, scope: this});
    this.control({notificationsAnchor: {afterrender: this.resetAnchorCount}})
}, addNotification: function (a) {
    Uni.store.Notifications.add(a)
}, resetAnchorCount: function () {
    var a = 0;
    Uni.store.Notifications.each(function (b) {
        if (!b.data.timeseen) {
            a++
        }
    });
    if (a > 0) {
        this.getAnchor().enable()
    } else {
        this.getAnchor().disable()
    }
    this.getAnchor().setText(this.getUnseenText(a))
}, getUnseenText: function (b) {
    var a = "";
    if (b > 10) {
        a = "10+"
    } else {
        if (b > 0) {
            a = b
        }
    }
    return a
}});
Ext.define("Uni.model.PortalItem", {extend: "Ext.data.Model", fields: ["title", "portal", "index", "items"], proxy: {type: "memory"}});
Ext.define("Uni.store.PortalItems", {extend: "Ext.data.Store", model: "Uni.model.PortalItem", storeId: "portalItems", singleton: true, autoLoad: false, clearOnPageLoad: false, clearRemovedOnLoad: false, proxy: {type: "memory", reader: {type: "json", root: "items"}}});
Ext.define("Uni.view.container.PortalContainer", {extend: "Ext.panel.Panel", xtype: "portal-container", ui: "large", padding: "16px 0 0 0 ", layout: "column", columnCount: 3, addPortalItem: function (c) {
    var e = this, b = this.createPortalWidgetFromItem(c), a = c.get("index");
    if (a === "" || a === null || typeof a === "undefined") {
        this.add(b)
    } else {
        this.insert(a, b)
    }
    var d = this.items.items.length, f = d % e.columnCount;
    switch (f) {
        case 1:
            b.addCls("first");
            break;
        case 2:
            b.addCls("middle");
            break;
        default:
            b.addCls("last");
            break
    }
}, createPortalWidgetFromItem: function (b) {
    var c = this, e = b.get("title"), a = b.get("items"), d;
    if (typeof a === "undefined") {
        return d
    }
    d = Ext.create("Ext.panel.Panel", {title: e, ui: "tile", columnWidth: 1 / c.columnCount, height: 256, items: [
        {xtype: "menu", ui: "tilemenu", floating: false, items: a}
    ]});
    return d
}});
Ext.define("Uni.controller.Portal", {extend: "Ext.app.Controller", requires: ["Uni.store.MenuItems", "Uni.store.PortalItems", "Uni.view.container.PortalContainer"], portalViews: [], init: function () {
    this.initMenuItems();
    this.refreshPortals();
    this.addEvents("changemaincontentevent", "changemainbreadcrumbevent")
}, initMenuItems: function () {
    Uni.store.MenuItems.on({add: this.refreshPortals, load: this.refreshPortals, update: this.refreshPortals, remove: this.refreshPortals, bulkremove: this.refreshPortals, scope: this})
}, refreshPortals: function () {
    var b = this, a = Uni.store.MenuItems;
    a.each(function (d) {
        var c = d.get("portal"), e = d.get("text");
        if (!Ext.isEmpty(c)) {
            crossroads.addRoute("/" + c, function () {
                b.showPortalOverview(c, e)
            })
        }
    })
}, showPortalOverview: function (a, h) {
    var b = Uni.store.PortalItems, e = this.portalViews[a];
    if (Ext.isDefined(e)) {
        e.removeAll()
    }
    this.portalViews[a] = Ext.create("Uni.view.container.PortalContainer", {title: h});
    e = this.portalViews[a];
    b.clearFilter();
    b.filter("portal", a);
    var g = {};
    b.each(function (i) {
        if (g.hasOwnProperty(i.get("title"))) {
            Ext.each(i.get("items"), function (j) {
                g[i.get("title")].get("items").push(j)
            });
            b.remove(i)
        } else {
            g[i.get("title")] = i
        }
        g[i.get("title")].get("items").sort(function (k, j) {
            if (k.text < j.text) {
                return -1
            } else {
                if (k.text > j.text) {
                    return 1
                } else {
                    return 0
                }
            }
        })
    });
    var f = [];
    for (portalItemToDisplay in g) {
        if (g.hasOwnProperty(portalItemToDisplay)) {
            f.push(portalItemToDisplay)
        }
    }
    f.sort();
    for (var d = 0; d < f.length; d++) {
        e.addPortalItem(g[f[d]])
    }
    this.getApplication().fireEvent("changemaincontentevent", e);
    var c = Ext.create("Uni.model.BreadcrumbItem", {text: h});
    this.getApplication().fireEvent("changemainbreadcrumbevent", c)
}});
Ext.define("Uni.view.search.Quick", {extend: "Ext.container.Container", alias: "widget.searchQuick", cls: "search-quick", layout: {type: "hbox", align: "stretch", pack: "end"}, items: [
    {xtype: "container", layout: {type: "hbox", align: "middle"}, items: [
        {xtype: "textfield", itemId: "searchField", cls: "search-field", emptyText: "Search"}
    ]},
    {xtype: "button", itemId: "searchButton", cls: "search-button", glyph: "xe021@icomoon", scale: "small"}
], initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.controller.Search", {extend: "Ext.app.Controller", requires: ["Uni.view.search.Quick"], refs: [
    {ref: "searchField", selector: "searchQuick #searchField"}
], init: function () {
    this.control({"searchQuick #searchButton": {click: this.onClickSearchButton}, searchButton: {click: this.onClickBasicSearchButton}, "searchQuick #searchField": {specialkey: this.onEnterSearchField}})
}, onClickBasicSearchButton: function () {
    this.getApplication().fireEvent("showadvancedsearchevent")
}, onClickSearchButton: function () {
    this.validateInputAndFireEvent()
}, onEnterSearchField: function (b, a) {
    if (a.getKey() === a.ENTER) {
        this.validateInputAndFireEvent()
    }
}, validateInputAndFireEvent: function () {
    var a = this.getSearchField().getValue().trim();
    if (a.length > 0) {
        this.fireSearchQueryEvent(a)
    }
}, fireSearchQueryEvent: function (a) {
    this.getApplication().fireEvent("searchqueryevent", a)
}});
Ext.define("Uni.view.form.field.Vtypes", {requires: ["Ext.form.field.VTypes"], hexstringRegex: /^[a-f_A-F_0-9]*$/, init: function () {
    this.validateNonEmptyString();
    this.validateHexString();
    this.validateEan13String();
    this.validateEan18String();
    this.validateReadingtype()
}, validateReadingtype: function () {
    var b = this;
    var a = null;
    Ext.apply(Ext.form.field.VTypes, {readingtype: function (c) {
        return/^\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+\.\d+$/.test(c)
    }, readingtypeText: "Invalid reading type syntax", readingtypeMask: /[\d\.]/i})
}, validateNonEmptyString: function () {
    var b = this;
    var a = null;
    Ext.apply(Ext.form.field.VTypes, {nonemptystring: function (c) {
        a = null;
        if ((c == null || c == undefined || c == "")) {
            return false
        }
        if (c.trim().length == 0) {
            return false
        }
    }, nonemptystringText: "This field is required"})
}, validateHexString: function () {
    var a = this;
    Ext.apply(Ext.form.field.VTypes, {hexstring: function (b) {
        return a.hexstringRegex.test(b)
    }, hexstringText: "Wrong Hexadecimal number!"})
}, validateEan13String: function () {
    var a = this;
    Ext.apply(Ext.form.field.VTypes, {ean13: function (b) {
        if (b.length != 13) {
            return false
        } else {
            if (a.validateNumeric(b) === false) {
                return false
            } else {
                if (b.substr(12) !== a.validateCheckDigit(b.substring(0, 12))) {
                    return false
                } else {
                    return true
                }
            }
        }
    }, ean13stringText: "Wrong Ean13!"})
}, numericregex: /^[0-9]$/, validateEan18String: function () {
    var a = this;
    Ext.apply(Ext.form.field.VTypes, {ean18: function (b) {
        if (b.length !== 18) {
            return false
        } else {
            if (a.validateNumeric(b) === false) {
                return false
            } else {
                if (b.substr(17) !== a.validateCheckDigit(b.substring(0, 17))) {
                    return false
                } else {
                    return true
                }
            }
        }
    }, ean18stringText: "Wrong Ean18!"})
}, validateNumeric: function (a) {
    return this.numericregex.test(a)
}, validateCheckDigit: function (c) {
    var f = 3;
    var b = 0;
    for (var a = c.length - 1; a >= 0; a--) {
        var e = c.substring(a, a + 1);
        b += e * f;
        f = (f === 3) ? 1 : 3
    }
    var d = (((b - 1) / 10) + 1) * 10;
    return d - b
}});
Ext.define("Uni.view.panel.FilterToolbar", {extend: "Ext.panel.Panel", alias: "widget.filter-toolbar", titlePosition: "left", layout: {type: "hbox"}, header: false, ui: "filter-toolbar", showClearButton: true, items: [
    {xtype: "container", itemId: "itemsContainer", defaults: {margin: "0 8 0 0"}, items: []},
    {xtype: "label", itemId: "emptyLabel", hidden: true},
    {xtype: "component", flex: 1, html: "&nbsp;"},
    {xtype: "container", itemId: "toolsContainer", layout: {type: "hbox", align: "stretch"}, dock: "left"}
], dockedItems: [
    {xtype: "header", dock: "left"},
    {xtype: "container", dock: "right", minHeight: 150, items: {itemId: "Reset", xtype: "button", text: "Clear all", action: "clear"}}
], updateContainer: function (a) {
    var b = a.items.getCount() ? true : false;
    if (!this.emptyText) {
        this.setVisible(b)
    } else {
        this.getEmptyLabel().setVisible(!b);
        this.getClearButton().setDisabled(!b)
    }
}, initComponent: function () {
    var a = this;
    this.dockedItems[0].title = a.title;
    this.items[0].items = a.content;
    this.items[1].text = a.emptyText;
    this.items[3].items = a.tools;
    this.callParent(arguments);
    this.getClearButton().on("click", function () {
        a.fireEvent("clearAllFilters")
    });
    if (!this.showClearButton) {
        this.getClearButton().hide()
    }
    this.getContainer().on("afterlayout", "updateContainer", this)
}, getContainer: function () {
    return this.down("#itemsContainer")
}, getTools: function () {
    return this.down("#toolsContainer")
}, getClearButton: function () {
    return this.down('button[action="clear"]')
}, getEmptyLabel: function () {
    return this.down("#emptyLabel")
}});
Ext.define("Uni.component.filter.view.FilterTopPanel", {extend: "Uni.view.panel.FilterToolbar", alias: "widget.filter-top-panel", title: "Filters", setFilter: function (d, b, f, g) {
    var e = this, a = e.getContainer(), c = a.down("button[name=" + d + "]");
    if (!_.isEmpty(c)) {
        c.setText(b + ": " + f)
    } else {
        if (!g) {
            a.add(Ext.create("Uni.view.button.TagButton", {text: b + ": " + f, name: d, listeners: {closeclick: function () {
                e.fireEvent("removeFilter", d)
            }}}))
        } else {
            a.add(Ext.create("Ext.button.Button", {text: b + ": " + f, name: d, ui: "tag"}))
        }
    }
    this.updateContainer(this.getContainer())
}});
Ext.define("Uni.form.NestedForm", {extend: "Ext.form.Panel", alias: "widget.nested-form", initComponent: function () {
    this.callParent();
    this.getForm().monitor.selector = "[isFormField]:not([excludeForm]){ownerCt.getId() === '" + this.getId() + "'}"
}, getValues: function () {
    var a = this.callParent();
    this.items.each(function (b) {
        if (_.isFunction(b.getValues)) {
            a[b.name] = b.getValues()
        }
    });
    return a
}, setValues: function (a) {
    this.form.setValues(a);
    this.items.each(function (b) {
        if (!_.isEmpty(b.name) && _.has(a, b.name)) {
            if (_.isFunction(b.setValues) && _.isObject(a[b.name])) {
                b.setValues(a[b.name])
            }
        }
    })
}, loadRecord: function (a) {
    this.form._record = a;
    var b = this.form.hydrator ? this.form.hydrator.extract(a) : a.getData();
    return this.setValues(b)
}, updateRecord: function (a) {
    a = a || this.getRecord();
    var b = this.getValues();
    a.beginEdit();
    this.form.hydrator ? this.form.hydrator.hydrate(b, a) : a.set(b);
    a.endEdit();
    return this
}});
Ext.define("Uni.override.ux.window.Notification", {override: "Ext.ux.window.Notification", title: false, position: "t", stickOnClick: false, closable: false, ui: "notification"});
Ext.define("Uni.Loader", {scriptLoadingCount: 0, requires: ["Ext.tip.QuickTipManager", "Ext.layout.container.Absolute", "Ext.data.proxy.Rest", "Ext.state.CookieProvider", "Uni.About", "Uni.I18n", "Uni.Auth", "Uni.controller.Acknowledgements", "Uni.controller.Configuration", "Uni.controller.Error", "Uni.controller.Navigation", "Uni.controller.Notifications", "Uni.controller.Portal", "Uni.controller.Search", "Uni.view.form.field.Vtypes", "Uni.component.filter.view.FilterTopPanel", "Uni.form.NestedForm", "Uni.override.ServerOverride", "Uni.override.ApplicationOverride", "Uni.override.ButtonOverride", "Uni.override.CheckboxOverride", "Uni.override.FieldBaseOverride", "Uni.override.FieldContainerOverride", "Uni.override.NumberFieldOverride", "Uni.override.JsonWriterOverride", "Uni.override.RestOverride", "Uni.override.StoreOverride", "Uni.override.GridPanelOverride", "Uni.override.FormOverride", "Uni.override.form.field.ComboBox", "Uni.override.ModelOverride", "Uni.override.FieldSetOverride", "Uni.override.form.field.Base", "Uni.override.form.field.ComboBox", "Uni.override.form.field.Date", "Uni.override.form.field.FieldContainer", "Uni.override.form.Label", "Uni.override.form.Panel", "Uni.override.grid.plugin.BufferedRenderer", "Uni.override.grid.Panel", "Uni.override.menu.Item", "Uni.override.panel.Header", "Uni.override.panel.Panel", "Uni.override.ux.window.Notification", "Uni.override.view.Table", "Uni.override.window.MessageBox"], initI18n: function (a) {
    Uni.I18n.init(a)
}, onReady: function (b) {
    var a = this;
    a.loadFont();
    a.loadTooltips();
    a.loadStateManager();
    a.loadStores();
    a.loadVtypes();
    Uni.Auth.load(function () {
        Uni.I18n.load(function () {
            b()
        })
    })
}, loadFont: function () {
    Ext.setGlyphFontFamily("icomoon")
}, loadTooltips: function () {
    Ext.tip.QuickTipManager.init()
}, loadStateManager: function () {
    Ext.state.Manager.setProvider(Ext.create("Ext.state.CookieProvider"))
}, loadStores: function () {
    Ext.require("Uni.store.Apps");
    Ext.require("Uni.store.AppItems");
    Ext.require("Uni.store.Notifications");
    Ext.require("Uni.store.Translations");
    Ext.require("Uni.store.Privileges")
}, loadVtypes: function () {
    Ext.create("Uni.view.form.field.Vtypes").init()
}, loadScript: function (d, e) {
    var c = this, a = document.createElement("script"), b;
    a.setAttribute("src", d);
    if (e) {
        c.scriptLoadingCount++;
        a.onreadystatechange = a.onload = function () {
            if (!b) {
                c.scriptLoadingCount--;
                if (c.scriptLoadingCount === 0) {
                    e()
                }
            }
            b = true
        }
    }
    document.getElementsByTagName("head")[0].appendChild(a)
}, loadStyleSheet: function (a) {
    var b = document.createElement("link");
    b.setAttribute("rel", "stylesheet");
    b.setAttribute("type", "text/css");
    b.setAttribute("href", a);
    document.getElementsByTagName("head")[0].appendChild(b)
}});
Ext.define("Uni.component.filter.model.Filter", {extend: "Ext.data.Model", getFields: function () {
    var a = [];
    this.fields.each(function (b) {
        a.push(b.name)
    });
    this.associations.each(function (b) {
        a.push(b.name)
    });
    return a
}, getPlainData: function () {
    var a = this, b = this.getData(true);
    this.associations.each(function (c) {
        switch (c.type) {
            case"hasOne":
                b[c.name] = a.extractHasOne(a[c.getterName](), c);
                break;
            case"hasMany":
                b[c.name] = a.extractHasMany(a[c.name](), c);
                break
        }
    });
    _.each(b, function (d, c) {
        if (!d) {
            delete b[c]
        }
    });
    return b
}, extractHasOne: function (a) {
    return a ? a.getId() : false
}, extractHasMany: function (b) {
    var a = [];
    b.each(function (c) {
        a.push(c.getId())
    });
    return a
}, removeFilterParam: function (b, d) {
    if (d) {
        var a = this[b]();
        var c = a.getById(d);
        if (c) {
            a.remove(c)
        }
    } else {
        if (!_.isUndefined(this.data[b])) {
            delete this.data[b]
        }
    }
}});
Ext.define("Uni.component.filter.store.Filterable", {proxyFilter: null, setProxyFilter: function (a) {
    this.proxyFilter = a;
    this.updateProxyFilter()
}, getProxyFilter: function () {
    return this.proxyFilter
}, updateProxyFilter: function () {
    this.load();
    this.fireEvent("updateProxyFilter", this.proxyFilter)
}, getFilterParams: function () {
    return this.proxyFilter.getPlainData()
}});
Ext.define("Uni.component.filter.view.Filter", {extend: "Ext.form.Panel", alias: "widget.filter-form", applyKey: 13, loadRecord: function (a) {
    var b = this, c = a.getData(true);
    this.callParent([a]);
    a.associations.each(function (d) {
        switch (d.type) {
            case"hasOne":
                c[d.name] = b.extractHasOne(a[d.getterName].call(a));
                break;
            case"hasMany":
                c[d.name] = b.extractHasMany(a[d.name]());
                break
        }
    });
    return this.getForm().setValues(c)
}, extractHasOne: function (a) {
    return a ? a.getId() : null
}, extractHasMany: function (b) {
    var a = [];
    b.each(function (c) {
        a.push(c.getId())
    });
    return a
}, updateRecord: function (a) {
    this.callParent([a]);
    var c = this, b = this.getValues();
    a = a || this._record;
    a.associations.each(function (d) {
        switch (d.type) {
            case"hasOne":
                c.hydrateHasOne(a, d, b);
                break;
            case"hasMany":
                c.hydrateHasMany(a, d, b);
                break
        }
    });
    return this
}, hydrateHasOne: function (b, a, d) {
    var e = a.name, f = this.down('[name="' + e + '"]');
    if (!d[e]) {
        b[a.setterName](Ext.create(a.model))
    } else {
        if (f && f.mixins.bindable) {
            var c = f.getStore();
            var g = c.getById(d[e]);
            b[a.setterName](g)
        }
    }
    return this
}, hydrateHasMany: function (b, a, e) {
    var g = a.name, d = b[g](), h = this.down('[name="' + g + '"]');
    if (!e[g]) {
        d.removeAll()
    } else {
        if (h && h.mixins.bindable && e[g]) {
            var f = h.getStore();
            if (!_.isArray(e[g])) {
                e[g] = [e[g]]
            }
            var c = _.map(e[g], function (i) {
                return f.getById(i)
            });
            d.loadRecords(c, {})
        }
    }
    return this
}, initComponent: function () {
    var a = this;
    a.callParent(arguments);
    a.on("afterrender", function (c) {
        var b = c.getEl();
        b.on("keypress", function (f, d) {
            (f.getKey() == a.applyKey) && (a.fireEvent("applyfilter", {me: a, key: a.applyKey, t: d}))
        })
    })
}});
Ext.define("Uni.component.sort.model.Sort", {extend: "Ext.data.Model", inheritableStatics: {ASC: "asc", DESC: "desc"}, defaultOrder: "ASC", key: "sort", getFields: function () {
    return[this.key]
}, addSortParam: function (b, a) {
    a = a || this.statics()[this.defaultOrder];
    var c = this.fields.getByKey(b);
    if (c) {
        this.set(b, a)
    }
}, toggleSortParam: function (b) {
    var c = this.fields.getByKey(b);
    if (c) {
        var a = this.get(b) == this.statics().ASC ? this.statics().DESC : this.statics().ASC;
        this.set(b, a)
    }
}, removeSortParam: function (a) {
    delete this.data[a]
}, getPlainData: function () {
    var b = this.getData(), c = {};
    c[this.statics().ASC] = "";
    c[this.statics().DESC] = "-";
    var d = [];
    _.each(b, function (f, e) {
        if (_.contains(_.keys(c), f)) {
            d.push(c[f] + e)
        }
    });
    var a = {};
    a[this.key] = d;
    return a
}});
Ext.define("Uni.component.sort.store.Sortable", {proxySort: null, setProxySort: function (a) {
    this.proxySort = a;
    this.updateProxySort()
}, getProxySort: function () {
    return this.proxySort
}, updateProxySort: function () {
    this.load();
    this.fireEvent("updateProxySort", this.proxySort)
}, getSortParams: function () {
    return this.proxySort.getPlainData()
}});
Ext.define("Uni.controller.AppController", {extend: "Ext.app.Controller", requires: [], refs: [
    {ref: "viewport", selector: "viewport"},
    {ref: "contentPanel", selector: "viewport > #contentPanel"},
    {ref: "logo", selector: "viewport uni-nav-logo"}
], applicationTitle: "Connexo", defaultToken: "", searchEnabled: true, init: function () {
    var a = this;
    a.initCrossroads();
    a.getController("Uni.controller.Navigation").applicationTitle = a.applicationTitle;
    a.getController("Uni.controller.Navigation").searchEnabled = a.searchEnabled;
    a.getController("Uni.controller.history.EventBus").setDefaultToken(a.defaultToken);
    a.getApplication().on("changecontentevent", a.showContent, a);
    a.getApplication().on("sessionexpired", a.redirectToLogin, a);
    a.loadControllers();
    a.callParent(arguments)
}, initCrossroads: function () {
    crossroads.ignoreState = true
}, onLaunch: function () {
    var a = this, b = a.getLogo();
    if (b.rendered) {
        b.setText(a.applicationTitle)
    } else {
        b.text = a.applicationTitle
    }
    a.callParent(arguments)
}, showContent: function (a) {
    this.getContentPanel().removeAll();
    this.getContentPanel().add(a);
    this.getContentPanel().doComponentLayout()
}, redirectToLogin: function () {
    window.location = "/apps/login/index.html?expired&page=" + window.location.pathname + window.location.hash
}, loadControllers: function () {
    for (var c = 0; c < this.controllers.length; c++) {
        var a = this.controllers[c];
        try {
            this.getController(a)
        } catch (b) {
            console.error("Could not load the '" + a + "' controller.")
        }
    }
}});
Ext.define("Uni.controller.history.Converter", {extend: "Ext.app.Controller", requires: ["Uni.controller.history.EventBus"], rootToken: null, init: function () {
    var a = this.getController("Uni.controller.history.Router");
    a.addConfig(this.routeConfig);
    this.callParent(arguments)
}, tokenize: function (d, e) {
    e = e !== undefined ? e : true;
    var c = "", a = Uni.controller.history.Settings.tokenDelimiter;
    for (var b = 0; b < d.length; b++) {
        c += a + d[b]
    }
    if (e) {
        c = "#" + c
    }
    return c
}, tokenizePath: function (a, b) {
    b = b !== undefined ? b : true;
    if (b) {
        a = "#" + a
    }
    return a
}, tokenizeShowOverview: function () {
    return this.tokenize([this.rootToken])
}});
Ext.define("Uni.controller.history.Settings", {statics: {tokenDelimiter: "/"}});
Ext.define("Uni.data.model.Filter", {extend: "Ext.data.Model"});
Ext.define("Uni.util.Application", {singleton: true, appPath: "app", getAppNamespace: function () {
    var b = Ext.Loader.getConfig().paths;
    for (var a in b) {
        if (b.hasOwnProperty(a) && b[a] === this.appPath) {
            return a
        }
    }
    return undefined
}});
Ext.define("Uni.util.History", {singleton: true, routerController: "Uni.controller.history.Router", requires: ["Uni.util.Application"], suspendEventsForNextCall: function () {
    var a = location.href;
    Ext.util.History.suspendEvents();
    new Ext.util.DelayedTask(function () {
        if (location.href !== a) {
            Ext.util.History.resumeEvents();
            this.stopped = true
        }
    }).delay(100)
}, getRouterController: function () {
    var me = this, appPath = Ext.String.htmlEncode(Uni.util.Application.appPath), namespace = Ext.String.htmlEncode(Uni.util.Application.getAppNamespace()), evalCode = namespace + "." + appPath + ".getController('" + me.routerController + "')";
    if (typeof namespace !== "undefined") {
        try {
            return eval(evalCode + ";")
        } catch (ex) {
            return evalCode
        }
    }
    return Ext.create(me.routerController)
}});
Ext.define("Uni.data.proxy.QueryStringProxy", {extend: "Ext.data.proxy.Proxy", alias: "proxy.querystring", root: "", router: null, requires: ["Uni.util.History"], writer: {type: "json", writeRecordId: false}, constructor: function (a) {
    a = a || {};
    this.callParent(arguments);
    if (a.hydrator) {
        this.hydrator = Ext.create(a.hydrator)
    }
    this.router = a.router || Uni.util.History.getRouterController()
}, create: function () {
    this.setQueryParams.apply(this, arguments)
}, update: function () {
    this.setQueryParams.apply(this, arguments)
}, read: function (c, h, d) {
    var e = this, b = e.router, g = e.model;
    c.setStarted();
    if (!_.isUndefined(b.queryParams[e.root])) {
        var f = Ext.decode(b.queryParams[e.root], true);
        if (this.hydrator) {
            var a = Ext.create(g);
            this.hydrator.hydrate(f, a);
            c.resultSet = Ext.create("Ext.data.ResultSet", {records: [a], total: 1, loaded: true, success: true})
        } else {
            c.resultSet = e.reader.read(f)
        }
        c.setSuccessful()
    }
    c.setCompleted();
    if (!c.wasSuccessful()) {
        e.fireEvent("exception", e, null, c)
    }
    Ext.callback(h, d || e, [c])
}, destroy: function () {
    var a = this.router;
    delete a.queryParams[this.root];
    a.getRoute().forward()
}, setQueryParams: function (b, f, c) {
    var a = this.router, d = {};
    b.setStarted();
    var e = this.hydrator ? this.hydrator.extract(c) : this.writer.getRecordData(c);
    c.commit();
    b.setCompleted();
    b.setSuccessful();
    d[this.root] = Ext.encode(e);
    a.getRoute().forward(null, d)
}});
Ext.define("Uni.data.store.Filterable", {extend: "Ext.data.Store", remoteFilter: true, hydrator: null, constructor: function (b) {
    var c = this;
    this.callParent(arguments);
    var a = this.router = b.router || Uni.util.History.getRouterController();
    if (c.hydrator && Ext.isString(c.hydrator)) {
        c.hydrator = Ext.create(c.hydrator)
    }
    a.on("routematch", function () {
        if (a.filter) {
            c.setFilterModel(a.filter)
        }
    })
}, setFilterModel: function (a) {
    var c = this, d = c.hydrator ? c.hydrator.extract(a) : a.getData(), b = [];
    _.map(d, function (f, e) {
        if (f) {
            b.push({property: e, value: f})
        }
    });
    c.clearFilter(true);
    c.addFilter(b, false)
}});
Ext.define("Uni.form.field.DateTime", {extend: "Ext.form.FieldContainer", mixins: {field: "Ext.form.field.Field"}, alias: "widget.date-time", layout: "vbox", requires: ["Ext.form.field.Date", "Ext.form.field.Number", "Ext.container.Container"], dateConfig: null, hoursConfig: null, separatorConfig: null, minutesConfig: null, initComponent: function () {
    var e = this, d = {xtype: "datefield", itemId: "date-time-field-date", submitValue: false, width: "100%", listeners: {change: {fn: e.onItemChange, scope: e}}}, b = {itemId: "date-time-field-hours", flex: 1, maxValue: 23, minValue: 0}, c = {itemId: "date-time-field-minutes", flex: 1, maxValue: 59, minValue: 0}, f = {xtype: "component", html: ":", margin: "0 5 0 5"}, a = {xtype: "container", width: "100%", layout: {type: "hbox", align: "middle"}, defaults: {xtype: "numberfield", allowDecimals: false, submitValue: false, value: 0, valueToRaw: e.formatDisplayOfTime, listeners: {change: {fn: e.onItemChange, scope: e}, blur: e.numberFieldValidation}}};
    if (e.layout === "hbox") {
        delete a.width;
        d.width = 130;
        b.width = 80;
        c.width = 80
    }
    Ext.apply(d, e.dateConfig);
    Ext.apply(b, e.hoursConfig);
    Ext.apply(c, e.minutesConfig);
    Ext.apply(f, e.separatorConfig);
    a.items = [b, f, c];
    e.items = [d, a];
    e.callParent(arguments)
}, formatDisplayOfTime: function (b) {
    var a = "00";
    if (b) {
        if (b < 10 && b > 0) {
            a = "0" + b
        } else {
            if (b >= 10) {
                a = b
            }
        }
    }
    return a
}, numberFieldValidation: function (b) {
    var a = b.getValue();
    if (Ext.isEmpty(a) || a < b.minValue) {
        b.setValue(b.minValue)
    } else {
        if (a > b.maxValue) {
            b.setValue(b.maxValue)
        }
    }
}, setValue: function (e) {
    var d = this, c = d.down("#date-time-field-date"), a = d.down("#date-time-field-hours"), b = d.down("#date-time-field-minutes");
    if (Ext.isDate(e) || moment(new Date(e)).isValid()) {
        d.eachItem(function (f) {
            f.suspendEvent("change")
        });
        c.setValue(moment(e).startOf("day").toDate());
        a.setValue(moment(e).hours());
        b.setValue(moment(e).minutes());
        d.fireEvent("change", d, e);
        d.eachItem(function (f) {
            f.resumeEvent("change")
        })
    } else {
        if (e === undefined || e === null) {
            c.reset();
            a.reset();
            b.reset()
        } else {
        }
    }
}, getValue: function () {
    var d = this, b = d.down("#date-time-field-date").getValue(), a = d.down("#date-time-field-hours").getValue(), c = d.down("#date-time-field-minutes").getValue();
    if (b) {
        b = b.getTime();
        if (a) {
            b += a * 3600000
        }
        if (c) {
            b += c * 60000
        }
    }
    b = new Date(b);
    return d.submitFormat ? Ext.Date.format(b, d.submitFormat) : b
}, markInvalid: function (a) {
    this.eachItem(function (b) {
        b.markInvalid("")
    });
    this.items.items[0].markInvalid(a)
}, eachItem: function (b, a) {
    if (this.items && this.items.each) {
        this.items.each(b, a || this)
    }
}, onItemChange: function () {
    this.fireEvent("change", this, this.getValue())
}});
Ext.define("Uni.form.field.DisplayFieldWithInfoIcon", {extend: "Ext.form.field.Display", xtype: "displayfield-with-info-icon", emptyText: "", infoTooltip: null, beforeRenderer: null, requires: ["Ext.button.Button"], deferredRenderer: function (b, c, a) {
    if (!c.isDestroyed) {
        new Ext.button.Button({renderTo: c.getEl().down(".x-form-display-field"), tooltip: a, iconCls: "icon-info-small", cls: "uni-btn-transparent", style: {display: "inline-block", "text-decoration": "none !important"}});
        c.updateLayout()
    }
}, renderer: function (b, c) {
    var a = this;
    if (Ext.isEmpty(b)) {
        return a.emptyText
    }
    if (Ext.isFunction(a.beforeRenderer)) {
        b = a.beforeRenderer(b, c)
    }
    a.infoTooltip && Ext.defer(this.deferredRenderer, 1, this, [b, c, a.infoTooltip]);
    return'<span style="display: inline-block; float: left; margin-right: 10px;">' + b + "</span>"
}});
Ext.define("Uni.form.field.EditedDisplay", {extend: "Ext.form.field.Display", xtype: "edited-displayfield", name: "editedDate", deferredRenderer: function (b, a) {
    b.getEl().down(".x-form-display-field").appendChild(a);
    b.updateLayout()
}, renderer: function (b, c) {
    var a;
    if (b) {
        b = Ext.isDate(b) ? b : new Date(b);
        a = document.createElement("span");
        a.className = "icon-edit";
        Ext.create("Ext.tip.ToolTip", {target: a, html: Uni.I18n.formatDate("editedDate.format", b, "MDC", "\\E\\d\\i\\t\\e\\d \\o\\n F d, Y \\a\\t H:i")});
        Ext.defer(this.deferredRenderer, 1, this, [c, a])
    }
    return""
}});
Ext.define("Uni.form.field.FilterDisplay", {extend: "Ext.form.FieldContainer", requires: ["Ext.form.field.Display", "Ext.button.Button"], xtype: "filter-display", emptyText: "", layout: "hbox", initComponent: function () {
    var b = this, a = b.name;
    b.items = [
        {xtype: "displayfield", name: a, renderer: function (e, f) {
            var d = f.nextSibling("#filter-display-button"), c = e;
            if (Ext.isFunction(b.renderer)) {
                c = b.renderer(e, f)
            }
            d.filterValue = e;
            d.setVisible(c ? true : false);
            return c ? c : b.emptyText
        }},
        {xtype: "button", itemId: "filter-display-button", filterBy: b.name, cls: "uni-btn-transparent", iconCls: "icon-filter", ui: "blank", shadow: false, hidden: true, margin: "5 0 0 10", width: 16}
    ];
    b.callParent(arguments)
}});
Ext.define("Uni.form.field.IntervalFlagsDisplay", {extend: "Ext.form.field.Display", xtype: "interval-flags-displayfield", name: "intervalFlags", fieldLabel: Uni.I18n.translate("intervalFlags.label", "UNI", "Interval flags"), emptyText: "", deferredRenderer: function (b, a) {
    b.getEl().down(".x-form-display-field").appendChild(a);
    b.updateLayout()
}, renderer: function (c, d) {
    var a, b = "";
    if (!Ext.isArray(c) || !c.length) {
        return this.emptyText
    }
    a = document.createElement("span");
    a.className = "icon-info-small";
    a.setAttribute("style", "width: 16px; height: 16px");
    Ext.Array.each(c, function (f, e) {
        e++;
        b += Uni.I18n.translate("intervalFlags.Flag", "UNI", "Flag") + " " + e + ": " + f + "<br>"
    });
    Ext.create("Ext.tip.ToolTip", {target: a, html: b});
    Ext.defer(this.deferredRenderer, 1, this, [d, a]);
    return'<span style="display: inline-block; width: 20px; float: left;">' + c.length + "</span>"
}});
Ext.define("Uni.form.field.LastEventDateDisplay", {extend: "Ext.form.field.Display", xtype: "last-event-date-displayfield", name: "lastEventDate", fieldLabel: Uni.I18n.translate("lastEventDate.label", "UNI", "Last event date"), emptyText: "", requires: ["Ext.button.Button"], deferredRenderer: function (b, c, a) {
    if (!c.isDestroyed) {
        new Ext.button.Button({renderTo: c.getEl().down(".x-form-display-field"), tooltip: a, iconCls: "icon-info-small", cls: "uni-btn-transparent", style: {display: "inline-block", "text-decoration": "none !important"}});
        c.updateLayout()
    }
}, renderer: function (c, d) {
    var a = Uni.I18n.formatDate("lastEventDate.dateFormat", Ext.isDate(c) ? c : new Date(c), "UNI", "F d, Y H:i:s"), b = Uni.I18n.translate("lastEventDate.tooltip", "UNI", "Date and time of last received event");
    if (!c) {
        return this.emptyText
    }
    Ext.defer(this.deferredRenderer, 1, this, [a, d, b]);
    return'<span style="display: inline-block; float: left; margin-right: 10px;">' + a + "</span>"
}});
Ext.define("Uni.form.field.LastEventTypeDisplay", {extend: "Ext.form.field.Display", xtype: "last-event-type-displayfield", name: "lastEventType", fieldLabel: Uni.I18n.translate("lastEventType.label", "UNI", "Last event type"), emptyText: "", requires: ["Ext.button.Button"], deferredRenderer: function (b, c, a) {
    if (!c.isDestroyed) {
        new Ext.button.Button({renderTo: c.getEl().down(".x-form-display-field"), tooltip: a, iconCls: "icon-info-small", cls: "uni-btn-transparent", style: {display: "inline-block", "text-decoration": "none !important"}});
        c.updateLayout()
    }
}, renderer: function (c, d) {
    var a = "", b = "<table>";
    if (!c) {
        return this.emptyText
    }
    Ext.Object.each(c, function (e, f) {
        if (e === "code") {
            a = f
        } else {
            b += '<tr><td style="text-align: right;border: none"><b>' + Uni.I18n.translate("lastEventType." + e, "UNI", e) + ":</b></td><td>&nbsp;&nbsp;&nbsp;" + f.name + " (" + f.id + ")</td></tr>"
        }
    });
    b += "</table>";
    Ext.defer(this.deferredRenderer, 1, this, [a, d, b]);
    return'<span style="display: inline-block; width: 115px; float: left;">' + a + "</span>"
}});
Ext.define("Uni.form.field.LastReadingDisplay", {extend: "Ext.form.field.Display", xtype: "last-reading-displayfield", name: "lastReading", fieldLabel: Uni.I18n.translate("lastReading.label", "UNI", "Last reading"), emptyText: "", requires: ["Ext.button.Button"], deferredRenderer: function (b, c, a) {
    if (!c.isDestroyed) {
        new Ext.button.Button({renderTo: c.getEl().down(".x-form-display-field"), tooltip: a, iconCls: "icon-info-small", cls: "uni-btn-transparent", style: {display: "inline-block", "text-decoration": "none !important"}});
        c.updateLayout()
    }
}, renderer: function (c, d) {
    var a = Uni.I18n.formatDate("lastReading.dateFormat", Ext.isDate(c) ? c : new Date(c), "UNI", "F d, Y H:i:s"), b = Uni.I18n.translate("lastReading.tooltip", "UNI", "The moment when the data was read out for the last time");
    if (!c) {
        return this.emptyText
    }
    Ext.defer(this.deferredRenderer, 1, this, [a, d, b]);
    return'<span style="display: inline-block; float: left; margin-right: 10px;">' + a + "</span>"
}});
Ext.define("Uni.form.field.Obis", {extend: "Ext.form.field.Text", requires: ["Ext.form.VTypes"], xtype: "obis-field", name: "obisCode", cls: "obisCode", msgTarget: "under", fieldLabel: Uni.I18n.translate("obis.label", "UNI", "OBIS code"), emptyText: Uni.I18n.translate("obis.mask", "UNI", "x.x.x.x.x.x"), afterSubTpl: '<div class="x-form-display-field"><i>' + Uni.I18n.translate("obis.info", "UNI", 'Provide the values for the 6 attributes of the Obis code, separated by a "."') + "</i></div>", maskRe: /[\d.]+/, vtype: "obisCode", required: true, initComponent: function () {
    Ext.apply(Ext.form.VTypes, {obisCode: function (c, b) {
        var a = /^(0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.0*([0-9]{1,2}|1[0-9]{2}|2[0-4][0-9]|25[0-5]))$/;
        return a.test(c)
    }, obisCodeText: Uni.I18n.translate("obis.error", "UNI", "OBIS code is wrong")});
    this.callParent(this)
}});
Ext.define("Uni.form.field.ObisDisplay", {extend: "Ext.form.field.Display", xtype: "obis-displayfield", name: "obisCode", cls: "obisCode", fieldLabel: Uni.I18n.translate("obis.label", "UNI", "OBIS code"), emptyText: ""});
Ext.define("Uni.form.field.Password", {extend: "Ext.form.FieldContainer", xtype: "password-field", fieldLabel: Uni.I18n.translate("form.password", "UNI", "Password"), layout: {type: "vbox", align: "stretch"}, handler: function (c, b) {
    var d = this.down("textfield");
    var a = d.getEl().down("input");
    a.dom.type = b ? "text" : "password"
}, items: [
    {xtype: "textfield", required: true, allowBlank: false, inputType: "password", name: this.name},
    {xtype: "checkbox", boxLabel: Uni.I18n.translate("comServerComPorts.form.showChar", "MDC", "Show characters")}
], initComponent: function () {
    this.items[0].name = this.name;
    this.items[1].handler = this.handler;
    this.items[1].scope = this;
    this.callParent(arguments)
}});
Ext.define("Uni.form.field.ReadingTypeDisplay", {extend: "Ext.form.field.Display", xtype: "reading-type-displayfield", name: "readingType", fieldLabel: Uni.I18n.translate("readingType.label", "UNI", "Reading type"), emptyText: "", requires: ["Ext.button.Button"], deferredRenderer: function (b, c) {
    var a = this;
    new Ext.button.Button({renderTo: c.getEl().down(".x-form-display-field"), tooltip: Uni.I18n.translate("readingType.tooltip", "UNI", "Reading type info"), iconCls: "icon-info-small", cls: "uni-btn-transparent", style: {display: "inline-block", "text-decoration": "none !important"}, handler: function () {
        a.handler(b)
    }});
    c.updateLayout()
}, handler: function (b) {
    var a = Ext.widget("readingTypeDetails");
    a.down("form").getForm().setValues(b);
    a.show()
}, renderer: function (a, b) {
    if (!a) {
        return this.emptyText
    }
    Ext.defer(this.deferredRenderer, 1, this, [a, b]);
    return'<span style="display: inline-block; width: 230px; float: left;">' + (a.mrid || a) + "</span>"
}});
Ext.define("Uni.grid.column.Action", {extend: "Ext.grid.column.Action", alias: "widget.uni-actioncolumn", header: "Actions", width: 100, align: "left", iconCls: "x-uni-action-icon", menu: {defaultAlign: "tr-br?", plain: true, items: []}, constructor: function (b) {
    var c = this, a = Ext.apply({}, b);
    c.menu.items = [];
    if (_.isString(a.items)) {
        var d = Ext.ClassManager.get(a.items);
        Ext.apply(c.menu.items, d.prototype.items)
    } else {
        Ext.apply(c.menu.items, a.items)
    }
    a.items = null;
    c.callParent([a]);
    this.initMenu()
}, initMenu: function () {
    var b = this, a = b.menu.xtype;
    a == null ? a = "menu" : null;
    b.menu = Ext.widget(a, b.menu);
    b.menu.on("click", function (g, d, f, c) {
        b.fireEvent("menuclick", g, d, f, c);
        if (d.action) {
            b.fireEvent(d.action, g.record)
        }
    })
}, handler: function (d, f, c) {
    var e = this;
    var b = d.getStore().getAt(f);
    var a = d.getCellByPosition({row: f, column: c});
    if (e.menu.cell === a) {
        e.menu.hide();
        e.menu.cell = null
    } else {
        a.addCls("active");
        e.menu.record = b;
        e.menu.showBy(a);
        e.menu.cell = a
    }
    e.menu.on("hide", function () {
        var g = d.getEl().query("." + e.iconCls + ":hover");
        if (!g.length) {
            e.menu.cell = null
        }
        a.removeCls("active")
    })
}});
Ext.define("Uni.grid.column.Default", {extend: "Ext.grid.column.Column", xtype: "uni-default-column", header: Uni.I18n.translate("general.default", "UNI", "Default"), minWidth: 120, align: "left", renderer: function (b, a) {
    if (b) {
        return'<div class="' + Uni.About.baseCssPrefix + 'default-column-icon default">&nbsp;</div>'
    } else {
        return""
    }
}});
Ext.define("Uni.grid.column.Edited", {extend: "Ext.grid.column.Column", xtype: "edited-column", header: Uni.I18n.translate("editedDate.header", "UNI", "Edited"), minWidth: 100, align: "left", requires: ["Uni.form.field.EditedDisplay"], deferredRenderer: function (f, b, a) {
    try {
        var d = this, c = a.getCell(b, d).down(".x-grid-cell-inner"), h = new Uni.form.field.EditedDisplay({fieldLabel: false});
        c.setHTML("");
        h.setValue(f);
        h.render(c)
    } catch (g) {
    }
}, renderer: function (g, c, b, h, e, d, a) {
    var f = c.column;
    Ext.defer(f.deferredRenderer, 1, f, [g, b, a])
}});
Ext.define("Uni.grid.column.IntervalFlags", {extend: "Ext.grid.column.Column", xtype: "interval-flags-column", header: Uni.I18n.translate("intervalFlags.label", "UNI", "Interval flags"), minWidth: 60, align: "left", requires: ["Uni.form.field.IntervalFlagsDisplay"], deferredRenderer: function (g, c, b) {
    var f = this, a;
    try {
        a = b.getCell(c, f)
    } catch (e) {
        return false
    }
    var d = a.down(".x-grid-cell-inner");
    var h = new Uni.form.field.IntervalFlagsDisplay({fieldLabel: false});
    d.setHTML("");
    h.setValue(g);
    h.render(d)
}, renderer: function (g, c, b, h, e, d, a) {
    var f = c.column;
    Ext.defer(f.deferredRenderer, 1, f, [g, b, a])
}});
Ext.define("Uni.grid.column.LastEventType", {extend: "Ext.grid.column.Column", xtype: "last-event-type-column", header: Uni.I18n.translate("lastEventType.label", "UNI", "Last event type"), minWidth: 150, align: "left", requires: ["Uni.form.field.LastEventTypeDisplay"], deferredRenderer: function (e, b, a) {
    var d = this;
    var c = a.getCell(b, d).down(".x-grid-cell-inner");
    var f = new Uni.form.field.LastEventTypeDisplay({fieldLabel: false});
    c.setHTML("");
    f.setValue(e);
    f.render(c);
    Ext.defer(a.updateLayout, 10, a)
}, renderer: function (g, c, b, h, e, d, a) {
    var f = c.column;
    Ext.defer(f.deferredRenderer, 1, f, [g, b, a])
}});
Ext.define("Uni.grid.column.Obis", {extend: "Ext.grid.column.Column", xtype: "obis-column", header: Uni.I18n.translate("obis.label", "UNI", "OBIS code"), minWidth: 120, align: "left"});
Ext.define("Uni.view.window.ReadingTypeDetails", {extend: "Ext.window.Window", xtype: "readingTypeDetails", closable: true, width: 700, height: 500, constrain: true, autoShow: true, modal: true, layout: "fit", closeAction: "destroy", floating: true, title: Uni.I18n.translate("readingType.readingTypeDetails", "UNI", "Reading type details"), items: {xtype: "form", border: false, itemId: "readingTypeDetailsForm", layout: "column", defaults: {columnWidth: 0.5, layout: "form"}, items: [
    {defaults: {xtype: "displayfield", labelWidth: 150}, items: [
        {name: "timePeriodOfInterest", fieldLabel: Uni.I18n.translate("readingType.timePeriodOfInterest", "UNI", "Time-period of interest")},
        {name: "dataQualifier", fieldLabel: Uni.I18n.translate("readingType.dataQualifier", "UNI", "Data qualifier")},
        {name: "timeAttributeEnumerations", fieldLabel: Uni.I18n.translate("readingType.timeAttributeEnumerations", "UNI", "Time attribute enumerations")},
        {name: "accumulationBehaviour", fieldLabel: Uni.I18n.translate("readingType.accumulationBehaviour", "UNI", "Accumulation behavior")},
        {name: "directionOfFlow", fieldLabel: Uni.I18n.translate("readingType.directionOfFlow", "UNI", "Direction of flow")},
        {name: "commodity", fieldLabel: Uni.I18n.translate("readingType.commodity", "UNI", "Commodity")},
        {name: "measurementKind", fieldLabel: Uni.I18n.translate("readingType.measurementKind", "UNI", "Kind")},
        {name: "interharmonics", fieldLabel: Uni.I18n.translate("readingType.interharmonics", "UNI", "(Compound) Interharmonics")},
        {name: "argumentReference", fieldLabel: Uni.I18n.translate("readingType.argumentReference", "UNI", "(Compound) Numerator and Denominator Argument Reference")}
    ]},
    {defaults: {xtype: "displayfield", labelWidth: 150}, items: [
        {name: "timeOfUse", fieldLabel: Uni.I18n.translate("readingType.timeOfUse", "UNI", "Time of use")},
        {name: "criticalPeakPeriod", fieldLabel: Uni.I18n.translate("readingType.criticalPeakPeriod", "UNI", "Critical peak period")},
        {name: "consumptionTier", fieldLabel: Uni.I18n.translate("readingType.comsumptionTier", "UNI", "Consumption tier")},
        {name: "phase", fieldLabel: Uni.I18n.translate("readingType.phase", "UNI", "Phase")},
        {name: "powerOfTenMultiplier", fieldLabel: Uni.I18n.translate("readingType.powerOfTenMultiplier", "UNI", "Power of ten multiplier")},
        {name: "unitOfMeasure", fieldLabel: Uni.I18n.translate("readingType.unitOfMeasure", "UNI", "Unit of measure")},
        {name: "currency", fieldLabel: Uni.I18n.translate("readingType.currency", "UNI", "Currency")}
    ]}
]}});
Ext.define("Uni.grid.column.ReadingType", {extend: "Ext.grid.column.Column", xtype: "reading-type-column", header: Uni.I18n.translate("readingType.label", "UNI", "Reading type"), minWidth: 280, align: "left", requires: ["Ext.panel.Tool", "Ext.util.Point", "Uni.view.window.ReadingTypeDetails", "Uni.form.field.ReadingTypeDisplay"], deferredRenderer: function (e, b, a) {
    var d = this;
    var c = a.getCell(b, d).down(".x-grid-cell-inner");
    var f = new Uni.form.field.ReadingTypeDisplay({fieldLabel: false});
    c.setHTML("");
    f.setValue(e);
    f.render(c);
    Ext.defer(a.updateLayout, 10, a)
}, renderer: function (g, c, b, h, e, d, a) {
    var f = c.column;
    Ext.defer(f.deferredRenderer, 1, f, [g, b, a])
}});
Ext.define("Uni.grid.column.ValidationFlag", {extend: "Ext.grid.column.Column", xtype: "validation-flag-column", header: Uni.I18n.translate("device.registerData.value", "MDC", "Value"), renderer: function (c, b, a) {
    switch (a.get("validationResult")) {
        case"validationStatus.notValidated":
            return'<span class="validation-column-align"><span class="icon-validation icon-validation-black"></span>';
            break;
        case"validationStatus.ok":
            return'<span class="validation-column-align"><span class="icon-validation"></span>';
            break;
        case"validationStatus.suspect":
            return'<span class="validation-column-align"><span class="icon-validation icon-validation-red"></span>';
            break;
        default:
            return"";
            break
    }
}});
Ext.define("Uni.grid.plugin.DragDropWithoutIndication", {extend: "Ext.grid.plugin.DragDrop", alias: "plugin.gridviewdragdropwithoutindication", onViewRender: function (a) {
    var b = this, c;
    if (b.enableDrag) {
        if (b.containerScroll) {
            c = a.getEl()
        }
        b.dragZone = new Ext.view.DragZone({view: a, ddGroup: b.dragGroup || b.ddGroup, dragText: b.dragText, containerScroll: b.containerScroll, scrollEl: c})
    }
    if (b.enableDrop) {
        b.dropZone = new Ext.grid.ViewDropZone({indicatorHtml: "", indicatorCls: "", view: a, ddGroup: b.dropGroup || b.ddGroup})
    }
}});
Ext.define("Uni.model.BreadcrumbItem", {extend: "Ext.data.Model", fields: ["text", "href", {name: "relative", type: "boolean", defaultValue: true}], associations: [
    {type: "hasOne", model: "Uni.model.BreadcrumbItem", associationKey: "child", getterName: "getChild", setterName: "doSetChild"}
], proxy: {type: "memory"}, setChild: function (c, a, b) {
    this.doSetChild(c, a, b);
    return c
}});
Ext.define("Uni.model.Script", {extend: "Ext.data.Model", fields: ["name", "path"]});
Ext.define("Uni.property.view.DefaultButton", {extend: "Ext.button.Button", xtype: "uni-default-button", border: 0, icon: "../sky/build/resources/images/form/restore.png", height: 28, width: 28, scale: "small", action: "delete", margin: "0 0 5 5", hidden: true});
Ext.define("Uni.property.view.property.Base", {extend: "Ext.form.FieldContainer", requires: ["Uni.property.view.DefaultButton"], width: 320, resetButtonHidden: false, translationKey: "UNI", layout: "hbox", fieldLabel: "", required: false, items: [
    {xtype: "uni-default-button"}
], isEdit: true, property: null, key: null, getName: function (a) {
    a = a ? a : this.key;
    return"properties." + a
}, setKey: function (b) {
    this.key = b;
    var a = Uni.I18n.translate(b, this.translationKey, b);
    this.setFieldLabel(a)
}, getResetButton: function () {
    return this.down("uni-default-button")
}, initProperty: function (a) {
    this.property = a;
    if (a) {
        this.key = a.get("key");
        this.itemId = this.key;
        if (this.isEdit) {
            this.required = a.get("required")
        }
    }
}, setProperty: function (a) {
    this.property = a;
    if (a) {
        this.setKey(a.get("key"));
        this.setValue(this.getProperty().get("value"));
        this.updateResetButton()
    }
}, updateResetButton: function () {
    var b = this.resetButtonHidden;
    var a = this.getResetButton();
    if (!b && this.isEdit) {
        a.setTooltip(Uni.I18n.translate("general.restoreDefaultValue", this.translationKey, "Restore to default value") + " &quot; " + this.getProperty().get("default") + "&quot;");
        a.setVisible(!this.getProperty().get("isInheritedOrDefaultValue"))
    }
    this.fireEvent("enableRestoreAll", this)
}, showPopupEnteredValueEqualsInheritedValue: function (c, b) {
    var a = this;
    Ext.create("Uni.view.window.Confirmation", {confirmText: Uni.I18n.translate("general.yes", "UNI", "Yes"), cancelText: Uni.I18n.translate("general.no", "UNI", "No")}).show({msg: Ext.String.format(Uni.I18n.translate("property.valueSameAsInherited", "UNI", "The value of '{0}' is the same as the default value.  Do you want to link the value to the default value?"), b.get("key")), title: Ext.String.format(Uni.I18n.translate("property.valueSameAs", "MDC", "Set '{0}' to its default value?"), b.get("key")), config: {property: a, field: c}, fn: a.setPropertyValue})
}, setPropertyValue: function (b, d, a) {
    if (b === "confirm") {
        var c = a.config.property;
        c.restoreDefault()
    }
}, getProperty: function () {
    return this.property
}, getEditCmp: function () {
    throw"getDisplayCmp is not implemented"
}, getDisplayCmp: function () {
    return{xtype: "displayfield", name: this.getName(), itemId: this.key + "displayfield"}
}, setValue: function (a) {
    this.isEdit ? this.getField().setValue(a) : this.getDisplayField().setValue(a)
}, getValue: function (a) {
    return a
}, getField: function () {
    return null
}, getDisplayField: function () {
    return this.down("displayfield")
}, initComponent: function () {
    var c = this;
    var a = Ext.apply({items: []}, c.config);
    Ext.apply(a.items, c.items);
    c.initProperty(c.property);
    var b = c.isEdit ? c.getEditCmp() : c.getDisplayCmp();
    if (Ext.isArray(b)) {
        var d = [0, 0];
        d.push.apply(d, b);
        a.items.splice.apply(a.items, d)
    } else {
        if (Ext.isObject(b)) {
            a.items.splice(0, 0, b)
        }
    }
    Ext.apply(c, a);
    c.callParent(d);
    c.setProperty(c.property);
    c.initListeners()
}, initListeners: function () {
    var a = this;
    var b = a.getField();
    if (b) {
        b.on("change", function () {
            a.getProperty().set("isInheritedOrDefaultValue", false);
            a.updateResetButton()
        });
        b.on("blur", function () {
            if (b.getValue() !== "" && !a.getProperty().get("isInheritedOrDefaultValue") && b.getValue() === a.getProperty().get("default")) {
                a.showPopupEnteredValueEqualsInheritedValue(b, a.getProperty())
            }
        })
    }
    this.on("afterrender", function () {
        a.fireEvent("enableRestoreAll", this)
    });
    this.getResetButton().setHandler(this.restoreDefault, this)
}, restoreDefault: function () {
    var b = this.getProperty();
    var a = b.get("default");
    this.setValue(a);
    b.set("isInheritedOrDefaultValue", true);
    this.updateResetButton()
}, useInheritedValue: function () {
    this.getProperty().initInheritedValues();
    this.updateResetButton()
}});
Ext.define("Uni.property.view.property.BaseCombo", {extend: "Uni.property.view.property.Base", getEditCmp: function () {
    return this.isCombo() ? this.getComboCmp() : this.getNormalCmp()
}, isCombo: function () {
    return this.getProperty().getSelectionMode() === "COMBOBOX"
}, getNormalCmp: function () {
    throw"getNormalCmp is not implemented"
}, getComboCmp: function () {
    var a = this;
    return{xtype: "combobox", itemId: a.key + "combobox", name: this.getName(), store: a.getProperty().getPossibleValues(), queryMode: "local", displayField: "value", valueField: "key", value: a.getProperty().get("value"), width: a.width, forceSelection: a.getProperty().getExhaustive()}
}, setValue: function (a) {
    if (this.isEdit) {
        this.isCombo() ? this.getComboField().setValue(a) : this.callParent(arguments)
    } else {
        this.callParent(arguments)
    }
}, getComboField: function () {
    return this.down("combobox")
}, initListeners: function () {
    var a = this;
    this.callParent(arguments);
    var b = a.getComboField();
    if (b) {
        b.on("change", function () {
            a.getProperty().set("isInheritedOrDefaultValue", false);
            a.updateResetButton()
        })
    }
}});
Ext.define("Uni.property.view.property.Text", {extend: "Uni.property.view.property.BaseCombo", getNormalCmp: function () {
    var a = this;
    return{xtype: "textfield", name: this.getName(), itemId: a.key + "textfield", width: a.width, msgTarget: "under"}
}, getField: function () {
    return this.down("textfield")
}});
Ext.define("Uni.property.view.property.Combobox", {extend: "Uni.property.view.property.Base", getEditCmp: function () {
    var a = this;
    return{xtype: "combobox", itemId: a.key + "combobox", name: this.getName(), store: a.getProperty().getPossibleValues(), queryMode: "local", displayField: "value", valueField: "key", width: a.width, forceSelection: a.getProperty().getExhaustive()}
}, getField: function () {
    return this.down("combobox")
}});
Ext.define("Uni.property.view.property.Textarea", {extend: "Uni.property.view.property.Base", getEditCmp: function () {
    var a = this;
    return{xtype: "textareafield", name: this.getName(), itemId: a.key + "textareafield", width: a.width, grow: true, msgTarget: "under"}
}, getField: function () {
    return this.down("textareafield")
}});
Ext.define("Uni.property.view.property.Password", {extend: "Uni.property.view.property.Text", getNormalCmp: function () {
    var a = this.callParent(arguments);
    a.inputType = "password";
    return a
}});
Ext.define("Uni.property.view.property.Hexstring", {extend: "Uni.property.view.property.Text", getNormalCmp: function () {
    var a = this.callParent(arguments);
    a.vtype = "hexstring";
    return a
}});
Ext.define("Uni.property.view.property.Boolean", {extend: "Uni.property.view.property.Base", getEditCmp: function () {
    var a = this;
    return{xtype: "checkbox", name: this.getName(), itemId: a.key + "checkbox", width: a.width, cls: "check", msgTarget: "under"}
}, getField: function () {
    return this.down("checkbox")
}, setValue: function (a) {
    if (!this.isEdit) {
        a = a ? "Yes" : "No"
    }
    this.callParent([a])
}, getDisplayCmp: function () {
    var a = this;
    return{xtype: "displayfield", name: this.getName(), itemId: a.key + "displayfield", width: a.width, msgTarget: "under"}
}, getDisplayField: function () {
    return this.down("displayfield")
}});
Ext.define("Uni.property.view.property.Number", {extend: "Uni.property.view.property.BaseCombo", getNormalCmp: function () {
    var b = this;
    var c = null;
    var e = null;
    var a = true;
    var d = b.getProperty().getValidationRule();
    if (d != null) {
        c = d.get("minimumValue");
        e = d.get("maximumValue");
        a = d.get("allowDecimals")
    }
    return{xtype: "numberfield", name: this.getName(), itemId: b.key + "numberfield", width: b.width, hideTrigger: true, keyNavEnabled: false, mouseWheelEnabled: false, minValue: c, maxValue: e, allowDecimals: a, msgTarget: "under"}
}, getComboCmp: function () {
    var a = this.callParent(arguments);
    a.fieldStyle = "text-align:right;";
    return a
}, getField: function () {
    return this.down("numberfield")
}});
Ext.define("Uni.property.view.property.NullableBoolean", {extend: "Uni.property.view.property.Base", getEditCmp: function () {
    var a = this;
    return{xtype: "radiogroup", itemId: a.key + "radiogroup", name: this.getName(), allowBlank: false, vertical: true, columns: 1, items: [
        {boxLabel: Uni.I18n.translate("true", a.translationKey, "True"), name: "rb", itemId: "rb_1_" + a.key, inputValue: true},
        {boxLabel: Uni.I18n.translate("false", a.translationKey, "False"), name: "rb", itemId: "rb_2_" + a.key, inputValue: false},
        {boxLabel: Uni.I18n.translate("none", a.translationKey, "None"), name: "rb", itemId: "rb_3_" + a.key, inputValue: null}
    ]}
}, getField: function () {
    return this.down("radiogroup")
}, setValue: function (b) {
    var a = {rb: null};
    if (Ext.isBoolean(b)) {
        a.rb = b
    }
    if (!this.isEdit) {
        if (b === true) {
            a = Uni.I18n.translate("yes", this.translationKey, "Yes")
        } else {
            if (b === false) {
                a = Uni.I18n.translate("no", this.translationKey, "No")
            } else {
                a = Uni.I18n.translate("na", this.translationKey, "N/A")
            }
        }
    }
    this.callParent([a])
}});
Ext.define("Uni.property.view.property.Date", {extend: "Uni.property.view.property.Base", format: "d/m/Y", formats: ["d.m.Y", "d m Y"], getEditCmp: function () {
    var a = this;
    return{xtype: "datefield", name: this.getName(), itemId: a.key + "datefield", format: a.format, altFormats: a.formats.join("|"), width: a.width, required: a.required}
}, getField: function () {
    return this.down("datefield")
}, setValue: function (a) {
    if (a !== null && a !== "") {
        a = new Date(a);
        if (!this.isEdit) {
            a = a.toLocaleDateString()
        }
    }
    this.callParent([a])
}, getValue: function (b) {
    if (b !== null && b !== "") {
        var a = new Date(b.getFullYear(), b.getMonth(), b.getDate(), 0, 0, 0, 0);
        return a.getTime()
    } else {
        return b
    }
}});
Ext.define("Uni.property.view.property.DateTime", {extend: "Uni.property.view.property.Date", timeFormat: "H:i:s", getEditCmp: function () {
    var b = this, a = [];
    a[0] = this.callParent(arguments);
    a[1] = {xtype: "timefield", name: b.getName() + ".time", margin: "0 0 0 16", itemId: b.key + "timefield", format: b.timeFormat, width: b.width, required: b.required};
    return a
}, getTimeField: function () {
    return this.down("timefield")
}, getDateField: function () {
    return this.down("datefield")
}, setValue: function (d) {
    var b = null, c = null;
    if (d !== null && d !== "") {
        var a = new Date(d);
        b = new Date(a.getFullYear(), a.getMonth(), a.getDate(), 0, 0, 0, 0);
        c = new Date(1970, 0, 1, a.getHours(), a.getMinutes(), a.getSeconds(), 0)
    }
    if (!this.isEdit) {
        this.callParent([a.toLocaleString()])
    } else {
        this.callParent([b]);
        this.getTimeField().setValue(c)
    }
}, getValue: function (d) {
    var c = this.getTimeField().getValue(), b = this.getDateField().getValue();
    if (c !== null && c !== "" && b !== null && b !== "") {
        var a = new Date(b.getFullYear(), b.getMonth(), b.getDate(), c.getHours(), c.getMinutes(), c.getSeconds(), 0);
        return a.getTime()
    }
    return null
}});
Ext.define("Uni.property.model.field.TimeUnit", {extend: "Ext.data.Model", fields: ["timeUnit"]});
Ext.define("Uni.property.store.TimeUnits", {extend: "Ext.data.Store", requires: ["Uni.property.model.field.TimeUnit"], model: "Uni.property.model.field.TimeUnit", autoLoad: true, proxy: {type: "rest", url: "../../api/mdc/field/timeUnit", reader: {type: "json", root: "timeUnits"}}});
Ext.define("Uni.property.view.property.Period", {extend: "Uni.property.view.property.BaseCombo", requires: ["Uni.property.store.TimeUnits"], getNormalCmp: function () {
    var a = this;
    return[
        {xtype: "numberfield", itemId: a.key + "numberfield", name: this.getName() + ".numberfield", width: a.width, required: a.required},
        {xtype: "combobox", margin: "0 0 0 16", itemId: a.key + "combobox", name: this.getName() + ".combobox", store: "Uni.property.store.TimeUnits", queryMode: "local", displayField: "timeUnit", valueField: "timeUnit", width: a.width, forceSelection: false, required: a.required}
    ]
}, getComboCmp: function () {
    var b = Ext.create("Ext.data.Store", {fields: [
        {name: "key", type: "string"},
        {name: "value", type: "string"}
    ]});
    b.loadData([], false);
    this.getProperty().getPossibleValues().each(function (c) {
        var d = c.get("count") + " " + c.get("timeUnit");
        b.add({key: d, value: d})
    });
    var a = this.callParent(arguments);
    a.store = b;
    return a
}, getField: function () {
    return this.down("numberfield")
}, setValue: function (c) {
    var b = null, a = null, d = null;
    if (c != null) {
        b = c.timeUnit;
        a = c.count;
        d = a + " " + b
    }
    if (this.isEdit) {
        if (this.isCombo()) {
            this.getComboField().setValue(d)
        } else {
            this.getField().setValue(a);
            this.getComboField().setValue(b)
        }
    } else {
        this.callParent([d])
    }
}, getValue: function (b) {
    if (!this.isCombo()) {
        var a = {};
        a.count = b.numberfield;
        a.timeUnit = b.combobox;
        return a
    } else {
        return b
    }
}});
Ext.define("Uni.property.view.property.Time", {extend: "Uni.property.view.property.Base", timeFormat: "H:i:s", getEditCmp: function () {
    var a = this;
    return{xtype: "timefield", name: this.getName(), itemId: a.key + "timefield", format: a.timeFormat, width: a.width, required: a.required}
}, getField: function () {
    return this.down("timefield")
}, setValue: function (a) {
    if (a !== null && a !== "") {
        a = new Date(a * 1000);
        if (!this.isEdit) {
            a = a.toLocaleTimeString()
        }
    }
    this.callParent([a])
}, getValue: function (b) {
    if (b != null && b != "") {
        var a = new Date(1970, 0, 1, b.getHours(), b.getMinutes(), b.getSeconds(), 0);
        return a.getTime() / 1000
    } else {
        return b
    }
}});
Ext.define("Uni.property.view.property.CodeTable", {extend: "Uni.property.view.property.Base", getEditCmp: function () {
    var a = this;
    return[
        {xtype: "textfield", name: this.getName(), itemId: a.key + "codetable", width: a.width, readOnly: true},
        {xtype: "button", text: "...", scale: "small", action: "showCodeTable"}
    ]
}, getField: function () {
    return this.down("textfield")
}});
Ext.define("Uni.property.view.property.Reference", {extend: "Uni.property.view.property.BaseCombo"});
Ext.define("Ext.ux.form.MultiSelect", {extend: "Ext.form.FieldContainer", mixins: {bindable: "Ext.util.Bindable", field: "Ext.form.field.Field"}, alternateClassName: "Ext.ux.Multiselect", alias: ["widget.multiselectfield", "widget.multiselect"], requires: ["Ext.panel.Panel", "Ext.view.BoundList", "Ext.layout.container.Fit"], uses: ["Ext.view.DragZone", "Ext.view.DropZone"], layout: "anchor", ddReorder: false, appendOnly: false, displayField: "text", allowBlank: true, minSelections: 0, maxSelections: Number.MAX_VALUE, blankText: "This is a required field", minSelectionsText: "Minimum {0} item(s) required", maxSelectionsText: "Maximum {0} item(s) required", delimiter: ",", dragText: "{0} Item{1}", ignoreSelectChange: 0, initComponent: function () {
    var a = this;
    a.bindStore(a.store, true);
    if (a.store.autoCreated) {
        a.valueField = a.displayField = "field1";
        if (!a.store.expanded) {
            a.displayField = "field2"
        }
    }
    if (!Ext.isDefined(a.valueField)) {
        a.valueField = a.displayField
    }
    a.items = a.setupItems();
    a.callParent();
    a.initField();
    a.addEvents("drop")
}, setupItems: function () {
    var a = this;
    a.boundList = Ext.create("Ext.view.BoundList", Ext.apply({anchor: "none 100%", deferInitialRefresh: false, border: 1, multiSelect: true, store: a.store, displayField: a.displayField, disabled: a.disabled}, a.listConfig));
    a.boundList.getSelectionModel().on("selectionchange", a.onSelectChange, a);
    if (!a.title) {
        return a.boundList
    }
    a.boundList.border = false;
    return{border: true, anchor: "none 100%", layout: "anchor", title: a.title, tbar: a.tbar, items: a.boundList}
}, onSelectChange: function (a, b) {
    if (!this.ignoreSelectChange) {
        this.setValue(b)
    }
}, getSelected: function () {
    return this.boundList.getSelectionModel().getSelection()
}, isEqual: function (e, d) {
    var b = Ext.Array.from, c = 0, a;
    e = b(e);
    d = b(d);
    a = e.length;
    if (a !== d.length) {
        return false
    }
    for (; c < a; c++) {
        if (d[c] !== e[c]) {
            return false
        }
    }
    return true
}, afterRender: function () {
    var b = this, a;
    b.callParent();
    if (b.selectOnRender) {
        a = b.getRecordsForValue(b.value);
        if (a.length) {
            ++b.ignoreSelectChange;
            b.boundList.getSelectionModel().select(a);
            --b.ignoreSelectChange
        }
        delete b.toSelect
    }
    if (b.ddReorder && !b.dragGroup && !b.dropGroup) {
        b.dragGroup = b.dropGroup = "MultiselectDD-" + Ext.id()
    }
    if (b.draggable || b.dragGroup) {
        b.dragZone = Ext.create("Ext.view.DragZone", {view: b.boundList, ddGroup: b.dragGroup, dragText: b.dragText})
    }
    if (b.droppable || b.dropGroup) {
        b.dropZone = Ext.create("Ext.view.DropZone", {view: b.boundList, ddGroup: b.dropGroup, handleNodeDrop: function (i, h, c) {
            var d = this.view, f = d.getStore(), e = i.records, g;
            i.view.store.remove(e);
            g = f.indexOf(h);
            if (c === "after") {
                g++
            }
            f.insert(g, e);
            d.getSelectionModel().select(e);
            b.fireEvent("drop", b, e)
        }})
    }
}, isValid: function () {
    var b = this, a = b.disabled, c = b.forceValidation || !a;
    return c ? b.validateValue(b.value) : a
}, validateValue: function (b) {
    var a = this, d = a.getErrors(b), c = Ext.isEmpty(d);
    if (!a.preventMark) {
        if (c) {
            a.clearInvalid();
            a.down("boundlist").removeCls("x-form-invalid-field")
        } else {
            a.markInvalid(d);
            a.down("boundlist").addCls("x-form-invalid-field")
        }
        a.fireEvent("fieldvaliditychange", a, c)
    }
    return c
}, markInvalid: function (c) {
    var b = this, a = b.getActiveError();
    b.setActiveErrors(Ext.Array.from(c));
    if (a !== b.getActiveError()) {
        b.updateLayout()
    }
}, clearInvalid: function () {
    var b = this, a = b.hasActiveError();
    b.unsetActiveError();
    if (a) {
        b.updateLayout()
    }
}, getSubmitData: function () {
    var a = this, b = null, c;
    if (!a.disabled && a.submitValue && !a.isFileUpload()) {
        c = a.getSubmitValue();
        if (c !== null) {
            b = {};
            b[a.getName()] = c
        }
    }
    return b
}, getSubmitValue: function () {
    var b = this, a = b.delimiter, c = b.getValue();
    return Ext.isString(a) ? c.join(a) : c
}, getValue: function () {
    return this.value || []
}, getRecordsForValue: function (g) {
    var f = this, a = [], h = f.store.getRange(), l = f.valueField, d = 0, k = h.length, b, c, e;
    for (e = g.length; d < e; ++d) {
        for (c = 0; c < k; ++c) {
            b = h[c];
            if (b.get(l) == g[d]) {
                a.push(b)
            }
        }
    }
    return a
}, setupValue: function (g) {
    var b = this.delimiter, d = this.valueField, e = 0, c, a, f;
    if (Ext.isDefined(g)) {
        if (b && Ext.isString(g)) {
            g = g.split(b)
        } else {
            if (!Ext.isArray(g)) {
                g = [g]
            }
        }
        for (a = g.length; e < a; ++e) {
            f = g[e];
            if (f && f.isModel) {
                g[e] = f.get(d)
            }
        }
        c = Ext.Array.unique(g)
    } else {
        c = []
    }
    return c
}, setValue: function (d) {
    var c = this, b = c.boundList.getSelectionModel(), a = c.store;
    if (!a.getCount()) {
        a.on({load: Ext.Function.bind(c.setValue, c, [d]), single: true});
        return
    }
    d = c.setupValue(d);
    c.mixins.field.setValue.call(c, d);
    if (c.rendered) {
        ++c.ignoreSelectChange;
        b.deselectAll();
        b.select(c.getRecordsForValue(d));
        --c.ignoreSelectChange
    } else {
        c.selectOnRender = true
    }
}, clearValue: function () {
    this.setValue([])
}, onEnable: function () {
    var a = this.boundList;
    this.callParent();
    if (a) {
        a.enable()
    }
}, onDisable: function () {
    var a = this.boundList;
    this.callParent();
    if (a) {
        a.disable()
    }
}, getErrors: function (b) {
    var a = this, c = Ext.String.format, e = [], d;
    b = Ext.Array.from(b || a.getValue());
    d = b.length;
    if (!a.allowBlank && d < 1) {
        e.push(a.blankText)
    }
    if (d < a.minSelections) {
        e.push(c(a.minSelectionsText, a.minSelections))
    }
    if (d > a.maxSelections) {
        e.push(c(a.maxSelectionsText, a.maxSelections))
    }
    return e
}, onDestroy: function () {
    var a = this;
    a.bindStore(null);
    Ext.destroy(a.dragZone, a.dropZone);
    a.callParent()
}, onBindStore: function (a) {
    var b = this.boundList;
    if (b) {
        b.bindStore(a)
    }
}});
Ext.define("Uni.property.view.property.Multiselect", {extend: "Uni.property.view.property.Base", requires: ["Ext.ux.form.MultiSelect"], getEditCmp: function () {
    var a = this;
    return[
        {items: [
            {xtype: "multiselect", itemId: a.key + "multiselect", name: a.getName(), allowBlank: !a.getProperty().get("required"), store: a.getProperty().getPredefinedPropertyValues().possibleValues(), displayField: "name", valueField: "id", width: a.width, height: 194, msgTarget: "multiselect-invalid-id-" + a.id, validateOnChange: false, listeners: {change: function (d, c) {
                var b = c.length;
                d.nextSibling("#multiselectSelectedItemsInfo").update(Ext.String.format(Uni.I18n.translatePlural("multiselect.selected", b, "UNI", "{0} items selected"), b))
            }, fieldvaliditychange: function (c, b) {
                c.nextSibling("#multiselectError").setVisible(!b)
            }}},
            {xtype: "component", itemId: "multiselectSelectedItemsInfo", html: Ext.String.format(Uni.I18n.translatePlural("multiselect.selected", 0, "UNI", "{0} items selected"), 0)},
            {xtype: "component", itemId: "multiselectError", cls: "x-form-invalid-under", hidden: true, height: 36, html: '<div id="multiselect-invalid-id-' + a.id + '"></div>'}
        ]}
    ]
}, getDisplayCmp: function () {
    var b = this, a = b.getProperty().getPredefinedPropertyValues().possibleValues();
    return{xtype: "displayfield", name: b.getName(), itemId: b.key + "displayfield", renderer: function (d) {
        var c = "";
        Ext.isArray(d) && Ext.Array.each(d, function (f) {
            var e = a.getById(f);
            e && (c += e.get("name") + "<br>")
        });
        return c
    }}
}, getField: function () {
    return this.down("multiselect")
}, setValue: function (a) {
    var b = this.getField();
    if (this.isEdit) {
        Ext.isArray(a) ? b.setValue(a) : b.reset()
    } else {
        this.getDisplayField().setValue(Ext.isArray(a) ? a : [])
    }
}});
Ext.define("Uni.property.controller.Registry", {extend: "Ext.app.Controller", singleton: true, requires: ["Uni.property.view.property.Text", "Uni.property.view.property.Combobox", "Uni.property.view.property.Textarea", "Uni.property.view.property.Password", "Uni.property.view.property.Hexstring", "Uni.property.view.property.Boolean", "Uni.property.view.property.Number", "Uni.property.view.property.NullableBoolean", "Uni.property.view.property.Date", "Uni.property.view.property.DateTime", "Uni.property.view.property.Period", "Uni.property.view.property.Time", "Uni.property.view.property.CodeTable", "Uni.property.view.property.Reference", "Uni.property.view.property.Multiselect"], propertiesMap: {TEXT: "Uni.property.view.property.Text", COMBOBOX: "Uni.property.view.property.Combobox", TEXTAREA: "Uni.property.view.property.Textarea", PASSWORD: "Uni.property.view.property.Password", HEXSTRING: "Uni.property.view.property.Hexstring", BOOLEAN: "Uni.property.view.property.Boolean", NUMBER: "Uni.property.view.property.Number", NULLABLE_BOOLEAN: "Uni.property.view.property.NullableBoolean", DATE: "Uni.property.view.property.Date", CLOCK: "Uni.property.view.property.DateTime", TIMEDURATION: "Uni.property.view.property.Period", TIMEOFDAY: "Uni.property.view.property.Time", CODETABLE: "Uni.property.view.property.CodeTable", REFERENCE: "Uni.property.view.property.Reference", EAN13: "Uni.property.view.property.Text", EAN18: "Uni.property.view.property.Text", ENCRYPTED_STRING: "Uni.property.view.property.Text", UNKNOWN: "Uni.property.view.property.Text", LISTVALUE: "Uni.property.view.property.Multiselect"}, stores: ["Uni.property.store.TimeUnits"], addProperty: function (b, a) {
    if (!Ext.isString(b)) {
        throw"!Ext.isString(key)"
    }
    if (!this.getProperty(b)) {
        this.propertiesMap[b] = a
    }
}, addProperties: function (a) {
    Ext.apply(this.propertiesMap, a)
}, getProperty: function (a) {
    return this.propertiesMap[a] || null
}});
Ext.define("Uni.property.form.PropertyHydrator", {extract: function (a) {
    return a.getData(true)
}, falseAndZeroChecker: function (a) {
    if (null != a) {
        if (a.toString() == "false") {
            return false
        }
        if (a.toString() == "0") {
            return 0
        }
    }
    return a || null
}, hydrate: function (d, a) {
    var b = d;
    var c = this;
    if (typeof a === "undefined" || !a.properties()) {
        return false
    }
    a.properties().each(function (g) {
        if (g.get("isInheritedOrDefaultValue") === true) {
            if (g.get("required") === true && g.get("hasDefaultValue")) {
                var f = c.falseAndZeroChecker(b[g.get("key")]);
                e = Ext.create("Uni.property.model.PropertyValue");
                g.setPropertyValue(e);
                e.set("value", f)
            } else {
                g.setPropertyValue(null)
            }
        } else {
            var f = c.falseAndZeroChecker(b[g.get("key")]);
            if (!g.raw.propertyValueInfo) {
                e = Ext.create("Uni.property.model.PropertyValue");
                g.setPropertyValue(e)
            }
            var e = g.getPropertyValue();
            e.set("value", f)
        }
    })
}});
Ext.define("Uni.property.form.Property", {extend: "Ext.form.Panel", alias: "widget.property-form", hydrator: "Uni.property.form.PropertyHydrator", border: 0, requires: ["Uni.property.controller.Registry", "Uni.property.form.PropertyHydrator"], defaults: {labelWidth: 250, resetButtonHidden: false}, layout: {type: "vbox", align: "stretch"}, initialised: false, isEdit: true, inheritedValues: false, loadRecord: function (a) {
    this.initProperties(a.properties());
    this.callParent(arguments)
}, loadRecordAsNotRequired: function (a) {
    var b = a.properties();
    _.each(b.data.items, function (c) {
        c.set("required", false)
    });
    this.loadRecord(a)
}, initProperties: function (b) {
    var c = this;
    var a = Uni.property.controller.Registry;
    c.removeAll();
    b.each(function (f) {
        if (!(f instanceof Uni.property.model.Property)) {
            throw"!(entry instanceof Uni.property.model.Property)"
        }
        c.inheritedValues ? f.initInheritedValues() : f.initValues();
        b.commitChanges();
        var e = f.getType();
        var d = a.getProperty(e);
        if (d) {
            var g = Ext.create(d, Ext.apply(c.defaults, {property: f, isEdit: c.isEdit}));
            c.add(g)
        }
    });
    this.initialised = true
}, useInheritedValues: function () {
    this.items.each(function (a) {
        a.useInheritedValue()
    });
    this.inheritedValues = true
}, getFieldValues: function (a) {
    var b = this.getValues(false, a, false, true);
    return this.unFlattenObj(b)
}, updateRecord: function () {
    var c = this;
    var b = c.getFieldValues();
    var a = {};
    _.each(b.properties || [], function (e, d) {
        var f = c.getPropertyField(d);
        a[d] = f.getValue(e)
    });
    this.getForm().hydrator.hydrate(a, c.getRecord())
}, unFlattenObj: function (a) {
    return _(a).inject(function (b, f, d) {
        var g = b, e = d.split("."), c = e.length - 1;
        _(e).each(function (i, h) {
            g = g[i] = (h == c ? f : (g[i] || {}))
        });
        return b
    }, {})
}, setProperties: function (a) {
    var b = this;
    a.each(function (c) {
        if (!(c instanceof Uni.property.model.Property)) {
            throw"!(entry instanceof Uni.property.model.Property)"
        }
        var d = b.getPropertyField(c.get("key"));
        if (d) {
            d.setProperty(c)
        }
    })
}, restoreAll: function () {
    this.items.each(function (a) {
        a.restoreDefault()
    })
}, getPropertyField: function (a) {
    return this.getComponent(a)
}});
Ext.define("Uni.property.model.PossibleValue", {extend: "Ext.data.Model", fields: ["id", "name"]});
Ext.define("Uni.property.model.PredefinedPropertyValue", {extend: "Ext.data.Model", requires: ["Uni.property.model.PossibleValue"], fields: [
    {name: "exhaustive", type: "boolean"},
    {name: "selectionMode", type: "string"},
    {name: "possibleValues", type: "auto"}
], associations: [
    {name: "possibleValues", type: "hasMany", model: "Uni.property.model.PossibleValue", associationKey: "possibleValues"}
]});
Ext.define("Uni.property.model.PropertyValue", {extend: "Ext.data.Model", fields: [
    {name: "value"},
    {name: "defaultValue"},
    {name: "inheritedValue"}
]});
Ext.define("Uni.property.model.PropertyValidationRule", {extend: "Ext.data.Model", fields: [
    {name: "allowDecimals"},
    {name: "minimumValue"},
    {name: "maximumValue"}
]});
Ext.define("Uni.property.model.PropertyType", {extend: "Ext.data.Model", fields: [
    {name: "simplePropertyType"}
], requires: ["Uni.property.model.PredefinedPropertyValue", "Uni.property.model.PropertyValidationRule"], associations: [
    {name: "predefinedPropertyValuesInfo", type: "hasOne", model: "Uni.property.model.PredefinedPropertyValue", associationKey: "predefinedPropertyValuesInfo", getterName: "getPredefinedPropertyValue", setterName: "setPredefinedPropertyValue", foreignKey: "predefinedPropertyValuesInfo"},
    {name: "propertyValidationRule", type: "hasOne", model: "Uni.property.model.PropertyValidationRule", associationKey: "propertyValidationRule", getterName: "getPropertyValidationRule", setterName: "setPropertyValidationRule", foreignKey: "propertyValidationRule"}
]});
Ext.define("Uni.property.model.Property", {extend: "Ext.data.Model", fields: [
    {name: "key", type: "string"},
    {name: "required", type: "boolean"},
    {name: "value", persist: false},
    {name: "default", persist: false},
    {name: "hasDefault", persist: false},
    {name: "isInheritedOrDefaultValue", type: "boolean", defaultValue: true, persist: false}
], requires: ["Uni.property.model.PropertyValue", "Uni.property.model.PropertyType"], associations: [
    {name: "propertyValueInfo", type: "hasOne", model: "Uni.property.model.PropertyValue", associationKey: "propertyValueInfo", getterName: "getPropertyValue", setterName: "setPropertyValue", foreignKey: "propertyValueInfo"},
    {name: "propertyTypeInfo", type: "hasOne", model: "Uni.property.model.PropertyType", associationKey: "propertyTypeInfo", getterName: "getPropertyType", setterName: "setPropertyType", foreignKey: "propertyTypeInfo"}
], initValues: function () {
    var e = this;
    var f = null;
    var c = "";
    var b = true;
    var d = false;
    if (e.raw.propertyValueInfo) {
        var a = e.getPropertyValue() || null;
        if (null !== a) {
            f = a.get("value");
            b = false;
            if (f === a.get("defaultValue")) {
                b = true
            }
            if (a.get("inheritedValue") !== "") {
                c = a.get("inheritedValue")
            } else {
                c = a.get("defaultValue");
                if (typeof a.get("defaultValue") !== "undefined" && typeof a.get("defaultValue") !== "") {
                    d = true
                }
            }
            if (f === "") {
                f = c;
                b = true
            }
        }
    }
    e.set("isInheritedOrDefaultValue", b);
    e.set("value", f);
    e.set("default", c);
    e.set("hasDefaultValue", d)
}, initInheritedValues: function () {
    var d = this;
    var e = null;
    var c = false;
    var a = false;
    if (d.raw.propertyValueInfo) {
        var b = d.getPropertyValue() || null;
        if (null !== b) {
            e = b.get("value");
            if (e === b.get("defaultValue")) {
                a = true
            }
            if (!e) {
                e = b.get("defaultValue");
                c = true
            }
            b.set("inheritedValue", e);
            b.set("value", "")
        }
    }
    if (a || (typeof d.raw.propertyValueInfo === "undefined")) {
        d.set("isInheritedOrDefaultValue", true)
    } else {
        d.set("isInheritedOrDefaultValue", false)
    }
    d.set("value", e);
    d.set("default", e);
    d.set("hasDefaultValue", c)
}, getType: function () {
    return this.getPropertyType().get("simplePropertyType")
}, getValidationRule: function () {
    var a = this.getPropertyType();
    if (a.raw.propertyValidationRule) {
        return a.getPropertyValidationRule()
    } else {
        return null
    }
}, getPredefinedPropertyValues: function () {
    var a = this.getPropertyType();
    if (a.raw.predefinedPropertyValuesInfo) {
        return a.getPredefinedPropertyValue()
    } else {
        return null
    }
}, getPossibleValues: function () {
    var a = this.getPredefinedPropertyValues();
    return a ? a.get("possibleValues") : null
}, getSelectionMode: function () {
    var a = this.getPredefinedPropertyValues();
    return a ? a.get("selectionMode") : null
}, getExhaustive: function () {
    var a = this.getPredefinedPropertyValues();
    return a ? a.get("exhaustive") : null
}});
Ext.define("Uni.util.Common", {singleton: true, loadNecessaryStores: function (c, g, e) {
    var d = this, b, f, a;
    if (Ext.isString(c)) {
        c = [c]
    }
    b = c.length;
    if (e !== false) {
        f = setTimeout(function () {
            b = 0;
            g()
        }, e || 30000)
    }
    a = function () {
        b--;
        if (b === 0) {
            clearTimeout(f);
            g()
        }
    };
    Ext.Array.each(c, function (h) {
        try {
            var j = Ext.getStore(h), i = j.isLoading();
            if (!i && j.getCount()) {
                a()
            } else {
                if (i) {
                    j.on("load", a, d, {single: true})
                } else {
                    j.load(function () {
                        a()
                    })
                }
            }
        } catch (k) {
            a()
        }
    })
}});
Ext.define("Uni.util.FormErrorMessage", {extend: "Ext.panel.Panel", alias: "widget.uni-form-error-message", ui: "form-error-framed", text: null, defaultText: "There are errors on this page that require your attention.", layout: {type: "hbox", align: "middle"}, errorIcon: null, defaultErrorIcon: "x-uni-form-error-msg-icon", margin: "7 0 32 0", beforeRender: function () {
    var a = this;
    if (!a.text) {
        a.text = a.defaultText
    }
    if (!a.errorIcon) {
        a.errorIcon = a.defaultErrorIcon
    }
    a.renew();
    a.callParent(arguments)
}, renew: function () {
    var a = this;
    a.removeAll(true);
    a.add([
        {xtype: "box", height: 22, width: 26, cls: a.errorIcon},
        {ui: "form-error", name: "errormsgpanel", html: a.text}
    ])
}, setText: function (b) {
    var a = this;
    a.text = b;
    a.renew()
}});
Ext.define("Uni.util.FormInfoMessage", {extend: "Ext.panel.Panel", alias: "widget.uni-form-info-message", cls: Uni.About.baseCssPrefix + "panel-no-items-found", ui: "small", framed: true, text: null, layout: {type: "hbox", align: "middle"}, margin: "7 0 32 0", beforeRender: function () {
    var a = this;
    a.renew();
    a.callParent(arguments)
}, renew: function () {
    var a = this;
    a.removeAll(true);
    a.add([
        {ui: "form-error", name: "errormsgpanel", html: a.text}
    ])
}, setText: function (b) {
    var a = this;
    a.text = b;
    a.renew()
}});
Ext.define("Uni.util.Hydrator", {extract: function (a) {
    var b = this, c = a.getData();
    a.associations.each(function (d) {
        switch (d.type) {
            case"hasOne":
                c[d.name] = b.extractHasOne(a.get(d.name));
                break;
            case"hasMany":
                c[d.name] = b.extractHasMany(a[d.name]());
                break
        }
    });
    return c
}, extractHasOne: function (a) {
    return a ? a.getId() : null
}, extractHasMany: function (b) {
    var a = [];
    b.each(function (c) {
        a.push(c.getId())
    });
    return a
}, Promise: function () {
    return{callback: null, callbacks: [], when: function (a) {
        this.callbacks = a;
        return this
    }, then: function (a) {
        this.callbacks.length ? this.callback = a : a();
        return this
    }, resolve: function (b) {
        var a = _.indexOf(this.callbacks, b);
        this.callbacks.splice(a, 1);
        if (!this.callbacks.length) {
            this.callback.call()
        }
        return this
    }}
}, hydrate: function (f, b) {
    var e = this, a = _.pick(f, b.fields.keys), c = _.pick(f, b.associations.keys);
    _.each(a, function (i, h) {
        b.set(h, i)
    });
    var g = new this.Promise();
    var d = [];
    _.each(c, function (k, j) {
        var i = b.associations.get(j);
        var l = function () {
            g.resolve(l)
        };
        d.push(l);
        switch (i.type) {
            case"hasOne":
                b.set(i.foreignKey, k);
                var h = i.createGetter();
                h.call(b, l);
                break;
            case"hasMany":
                e.hydrateHasMany(k, b[j]()).then(l);
                break
        }
    });
    return g.when(d)
}, hydrateHasMany: function (c, a) {
    a.removeAll();
    if (!c) {
        return this
    }
    if (!_.isArray(c)) {
        c = [c]
    }
    var d = new this.Promise();
    var b = [];
    _.map(c, function (f) {
        var e = function (g) {
            a.add(g);
            d.resolve(e)
        };
        b.push(e);
        a.model.load(f, {callback: e})
    });
    return d.when(b)
}});
Ext.define("Uni.util.QueryString", {singleton: true, buildQueryString: function (a) {
    var b = this, d = b.getQueryString(), c = Ext.Object.fromQueryString(d);
    Ext.apply(c, a || {});
    c = b.cleanQueryObject(c);
    return Ext.Object.toQueryString(c)
}, cleanQueryObject: function (c) {
    var b = Ext.clone(c || {});
    for (var a in c) {
        if (c.hasOwnProperty(a) && !Ext.isDefined(c[a])) {
            delete b[a]
        }
    }
    return b
}, buildHrefWithQueryString: function (b) {
    var c = this, a = location.href.split("?")[0], d = c.buildQueryString(b);
    return a + "?" + d
}, getQueryString: function () {
    var b = Ext.util.History.getToken() || document.location.href.split("?")[1], a = b.indexOf("?");
    return a < 0 ? "" : b.substring(a + 1)
}, getQueryStringValues: function () {
    return Ext.Object.fromQueryString(this.getQueryString())
}});
Ext.define("Uni.util.When", {success: null, failure: null, callback: null, toExecute: [], context: [], args: [], results: [], simple: [], count: null, failed: false, constructor: function () {
    var a = this;
    this.init();
    a.callParent(arguments)
}, init: function () {
    this.success = null;
    this.failure = null;
    this.callback = null;
    this.toExecute = [];
    this.context = [];
    this.args = [];
    this.results = [];
    this.simple = [];
    this.count = null;
    this.failed = false
}, when: function (a) {
    this.init();
    for (var b in a) {
        this.toExecute.push(a[b].action);
        this.context.push(a[b].context);
        this.args.push(a[b].args);
        this.simple.push(a[b].simple);
        this.count++
    }
    return this
}, then: function (d) {
    this.success = d.success;
    this.failure = d.failure;
    this.callback = d.callback;
    var g = this;
    for (var e in this.toExecute) {
        var c = [];
        var b = function (h) {
            return function () {
                g.count--;
                g.results[h] = arguments;
                if (g.count === 0) {
                    if (g.failed === false) {
                        g.resolveSuccess(g.success)
                    } else {
                        g.resolveFailure(g.failure)
                    }
                }
            }
        };
        var a = function (h) {
            return function () {
                g.count--;
                g.results[h] = arguments;
                g.failed = true;
                if (g.count === 0) {
                    g.resolveFailure(g.failure)
                }
            }
        };
        var f = function (h) {
            return function () {
                g.count--;
                g.results[h] = arguments;
                if (g.count === 0) {
                    if (g.failed === false) {
                        g.resolveSuccess(g.callback)
                    } else {
                        g.resolveFailure(g.callback)
                    }
                }
            }
        };
        if (typeof this.args[e] != "undefined") {
            c = this.args[e]
        }
        if (typeof this.callback === "undefined") {
            if (this.simple[e] === false) {
                c.push({success: b(e), failure: a(e)})
            } else {
                c.push({callback: b(e)})
            }
        } else {
            if (this.simple[e] === false) {
                c.push({success: f(e), failure: f(e)})
            } else {
                c.push({callback: f(e)})
            }
        }
        this.toExecute[e].apply(this.context[e], c)
    }
}, resolveSuccess: function (a) {
    a(this.results)
}, resolveFailure: function (a) {
    a()
}});
Ext.define("Uni.view.navigation.AppCenter", {extend: "Ext.button.Button", xtype: "uni-nav-appcenter", text: "", iconCls: "icon-appcenter", cls: Uni.About.baseCssPrefix + "nav-appcenter", menu: {xtype: "menu", plain: true, showSeparator: false, forceLayout: true, cls: Uni.About.baseCssPrefix + "nav-appcenter-menu", items: [
    {xtype: "dataview", cls: Uni.About.baseCssPrefix + "nav-appcenter-dataview", tpl: ['<div class="handlebar"></div>', '<tpl for=".">', '<a href="{url}"', '<tpl if="isExternal"> target="_blank"</tpl>', ">", '<div class="app-item', '<tpl if="isActive"> x-pressed</tpl>', '">', '<div class="icon icon-{icon}">&nbsp;</div>', '<span class="name">{name}</span>', "</div>", "</a>", "</tpl>"], itemSelector: "div.app-item", store: "apps"}
]}, initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.view.navigation.Logo", {extend: "Ext.button.Button", xtype: "uni-nav-logo", ui: "navigationlogo", text: "Connexo", action: "home", scale: "medium", href: "#", hrefTarget: "_self", initComponent: function () {
    this.callParent(arguments)
}, setLogoTitle: function (a) {
    if (this.rendered) {
        this.setText(a)
    } else {
        this.text = a
    }
}, setLogoGlyph: function (a) {
    if (this.rendered) {
        this.setGlyph(a)
    } else {
        this.glyph = a
    }
}});
Ext.define("Uni.view.search.Basic", {extend: "Ext.button.Button", alias: "widget.searchBasic", itemId: "searchButton", cls: "search-button", glyph: "xe021@icomoon", scale: "small"});
Ext.define("Uni.view.navigation.Help", {extend: "Ext.button.Button", alias: "widget.navigationHelp", action: "help", glyph: "xe009@icomoon", scale: "small", cls: "nav-help", initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.view.user.Menu", {extend: "Ext.button.Button", xtype: "userMenu", scale: "small", cls: "user-menu", iconCls: "icon-user", menu: [
    {text: "Logout", action: "logout", href: "/apps/login/index.html?logout=true"}
], initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.view.navigation.Header", {extend: "Ext.container.Container", alias: "widget.navigationHeader", ui: "navigationheader", requires: ["Uni.view.navigation.AppCenter", "Uni.view.navigation.Logo", "Uni.view.search.Basic", "Uni.view.search.Quick", "Uni.view.notifications.Anchor", "Uni.view.navigation.Help", "Uni.view.user.Menu"], layout: {type: "hbox", align: "stretch"}, height: 48, items: [
    {xtype: "uni-nav-appcenter"},
    {xtype: "uni-nav-logo"},
    {xtype: "component", flex: 1},
    {xtype: "button", itemId: "globalSearch", text: Uni.I18n.translate("navigation.header.search", "UNI", "Search"), cls: "search-button", iconCls: "icon-search", scale: "small", action: "search", href: "#/search", hidden: true},
    {xtype: "userMenu"}
], initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.view.navigation.Footer", {extend: "Ext.container.Container", alias: "widget.navigationFooter", cls: "nav-footer", height: 30, layout: {type: "hbox", align: "middle"}, items: [
    {xtype: "component", cls: "powered-by", html: 'Powered by <a href="http://www.energyict.com/en/smart-grid" target="_blank">Elster EnergyICT Jupiter 1.0.0</a>, <a href="http://www.energyict.com/en/smart-grid" target="_blank">Smart data management</a>'}
], initComponent: function () {
    this.callParent(arguments)
}});
Ext.define("Uni.view.navigation.Menu", {extend: "Ext.container.Container", alias: "widget.navigationMenu", ui: "navigationmenu", layout: {type: "vbox", align: "stretch"}, defaults: {xtype: "button", ui: "menuitem", hrefTarget: "_self", toggleGroup: "menu-items", action: "menu-main", enableToggle: true, allowDepress: false, cls: "menu-item", tooltipType: "title", scale: "large"}, removeAllMenuItems: function () {
    this.removeAll()
}, addMenuItem: function (a) {
    var c = this, b = c.createMenuItemFromModel(a);
    if (a.data.index === "" || a.data.index === null || a.data.index === undefined) {
        this.add(b)
    } else {
        this.insert(a.data.index, b)
    }
}, createMenuItemFromModel: function (b) {
    var c = b.data.glyph ? "uni-icon-" + b.data.glyph : "uni-icon-none", a = b.data.portal ? "#/" + b.data.portal : b.data.href;
    return{tooltip: b.data.text, text: b.data.text, href: a, data: b, iconCls: c, hidden: b.data.hidden}
}, selectMenuItem: function (a) {
    var b = this, c = a.id;
    this.items.items.forEach(function (d) {
        if (c === d.data.id) {
            b.deselectAllMenuItems();
            d.toggle(true, false)
        }
    })
}, deselectAllMenuItems: function () {
    this.items.items.forEach(function (a) {
        a.toggle(false, false)
    })
}});
Ext.define("Uni.view.breadcrumb.Link", {extend: "Ext.Component", alias: "widget.breadcrumbLink", ui: "link", text: "", href: "", beforeRender: function () {
    var a = this;
    a.callParent();
    Ext.applyIf(a.renderData, {text: a.text || "&#160;", href: a.href})
}, renderTpl: ['<tpl if="href">', '<a href="{href}">', "</tpl>", "{text}", '<tpl if="href"></a></tpl>']});
Ext.define("Uni.view.breadcrumb.Separator", {extend: "Ext.Component", alias: "widget.breadcrumbSeparator", ui: "linkseparator", html: "&nbsp;"});
Ext.define("Uni.view.breadcrumb.Trail", {extend: "Ext.container.Container", alias: "widget.breadcrumbTrail", ui: "breadcrumbtrail", requires: ["Uni.view.breadcrumb.Link", "Uni.view.breadcrumb.Separator", "Uni.controller.history.Settings"], layout: {type: "hbox", align: "middle"}, setBreadcrumbItem: function (b) {
    var a = this;
    if (a.rendered) {
        Ext.suspendLayouts()
    }
    a.removeAll();
    a.addBreadcrumbItem(b);
    if (a.rendered) {
        Ext.resumeLayouts(true)
    }
}, addBreadcrumbItem: function (e, c) {
    c = c || "";
    if (e.data.relative && c.length > 0) {
        c += Uni.controller.history.Settings.tokenDelimiter
    }
    var f, a = e.data.href, d = Ext.widget("breadcrumbLink", {text: e.data.text});
    try {
        f = e.getChild()
    } catch (b) {
    }
    if (f !== undefined && f.rendered) {
        d.setHref(c + a)
    } else {
        if (f !== undefined && !f.rendered) {
            d.href = c + a
        }
    }
    this.addBreadcrumbComponent(d);
    if (f !== undefined && f !== null) {
        if (e.data.relative) {
            c += a
        }
        this.addBreadcrumbItem(f, c)
    }
}, addBreadcrumbComponent: function (a) {
    var b = this.items.getCount();
    if (b % 2 === 1) {
        this.add(Ext.widget("breadcrumbSeparator"))
    }
    this.add(a)
}});
Ext.define("Uni.view.Viewport", {extend: "Ext.container.Viewport", requires: ["Ext.layout.container.Border", "Uni.view.navigation.Header", "Uni.view.navigation.Footer", "Uni.view.navigation.Menu", "Uni.view.container.ContentContainer", "Uni.view.breadcrumb.Trail"], layout: "border", items: [
    {xtype: "navigationHeader", region: "north", weight: 30},
    {xtype: "container", ui: "navigationwrapper", region: "west", layout: "absolute", width: 48, items: [
        {xtype: "navigationMenu"}
    ], weight: 10},
    {xtype: "container", region: "center", itemId: "contentPanel", layout: "fit"},
    {region: "north", xtype: "container", itemId: "northContainer", cls: "north", layout: "hbox", ui: "breadcrumbtrailcontainer", height: 48, items: [
        {xtype: "breadcrumbTrail", itemId: "breadcrumbTrail"}
    ], weight: 20}
]});
Ext.define("Uni.view.button.TagButton", {extend: "Ext.button.Split", alias: "widget.tag-button", split: true, menu: {}, ui: "tag", arrowCls: null, afterRender: function () {
    var a = this, c = a.getEl().first(), d = c.first().first(), b = c.createChild({tag: "span", cls: "x-btn-tag-right"}), e = c.getById(b.id);
    d.addCls(a.iconCls ? "x-btn-tag-text" : "x-btn-tag-text-noicon");
    e.on("click", function () {
        a.fireEvent("closeclick", a);
        a.destroy()
    });
    this.callParent(arguments)
}});
Ext.define("Uni.view.button.SortItemButton", {extend: "Uni.view.button.TagButton", alias: "widget.sort-item-btn", name: "sortitembtn", iconCls: "x-btn-sort-item-asc", sortOrder: "asc"});
Ext.define("Uni.view.button.StepButton", {extend: "Ext.button.Button", alias: "widget.step-button", ui: "step-active"});
Ext.define("Uni.view.container.EmptyGridContainer", {extend: "Ext.container.Container", xtype: "emptygridcontainer", layout: "card", activeItem: 1, grid: null, emptyComponent: null, mixins: {bindable: "Ext.util.Bindable"}, items: [
    {xtype: "container", itemId: "emptyContainer"},
    {xtype: "container", itemId: "gridContainer"}
], initComponent: function () {
    var c = this, b = c.grid, a = c.emptyComponent;
    if (!(b instanceof Ext.Component)) {
        b = Ext.clone(b)
    }
    c.items[1].items = b;
    if (!(a instanceof Ext.Component)) {
        a = Ext.clone(a)
    }
    c.items[0].items = a;
    this.callParent(arguments);
    c.grid = c.getGridCt().items.items[0];
    c.bindStore(c.grid.store || "ext-empty-store", true);
    this.on("beforedestroy", this.onBeforeDestroy, this)
}, getStoreListeners: function () {
    return{beforeload: this.onBeforeLoad, load: this.onLoad}
}, onBeforeDestroy: function () {
    this.bindStore("ext-empty-store")
}, onBeforeLoad: function () {
    var a = this;
    a.getLayout().setActiveItem(a.getGridCt())
}, onLoad: function () {
    var b = this, a = b.grid.store.getCount(), c = a === 0;
    b.getLayout().setActiveItem(c ? b.getEmptyCt() : b.getGridCt())
}, getGridCt: function () {
    return this.down("#gridContainer")
}, getEmptyCt: function () {
    return this.down("#emptyContainer")
}});
Ext.define("Uni.view.container.PreviewContainer", {extend: "Ext.container.Container", xtype: "preview-container", itemId: "preview-container", layout: "card", activeItem: 1, grid: null, emptyComponent: null, previewComponent: null, selectByDefault: true, mixins: {bindable: "Ext.util.Bindable"}, items: [
    {xtype: "container"},
    {xtype: "container", itemId: "wrapper-container", items: []}
], initComponent: function () {
    var d = this, b = d.grid, a = d.emptyComponent, c = d.previewComponent;
    if (!(a instanceof Ext.Component)) {
        a = Ext.clone(a)
    }
    d.items[0] = a;
    d.items[1].items = [];
    if (!(b instanceof Ext.Component)) {
        b = Ext.clone(b)
    }
    b.maxHeight = 450;
    d.items[1].items.push(b);
    if (!(c instanceof Ext.Component)) {
        c = Ext.clone(c)
    }
    d.items[1].items.push(c);
    d.callParent(arguments);
    d.grid = d.getWrapperCt().items.items[0];
    d.previewComponent = d.getWrapperCt().items.items[1];
    d.bindStore(d.grid.store || "ext-empty-store", true);
    d.initChildPagingBottom();
    d.initGridListeners();
    d.on("beforedestroy", d.onBeforeDestroy, d)
}, doChildPagingOperation: function (d, a) {
    var b = this, c;
    if (Ext.isDefined(b.previewComponent)) {
        c = b.previewComponent.down(d)
    } else {
        return
    }
    if (c !== null && Ext.isDefined(c) && c.getXType() === d) {
        c.updatePagingParams = false;
        a(c)
    }
}, resetChildPagingTop: function () {
    var b = this, a = "pagingtoolbartop";
    b.doChildPagingOperation(a, function (c) {
        c.resetPaging()
    })
}, initChildPagingBottom: function () {
    var a = this, b = "pagingtoolbarbottom";
    a.doChildPagingOperation(b, function (c) {
        c.updatePagingParams = false
    })
}, resetChildPagingBottom: function () {
    var a = this, b = "pagingtoolbarbottom";
    a.doChildPagingOperation(b, function (c) {
        c.resetPaging()
    })
}, initGridListeners: function () {
    var a = this;
    a.grid.on("selectionchange", a.onGridSelectionChange, a)
}, onGridSelectionChange: function () {
    var b = this, a = b.grid.view.getSelectionModel().getSelection();
    if (b.rendered) {
        Ext.suspendLayouts()
    }
    if (b.previewComponent) {
        b.previewComponent.setVisible(a.length === 1)
    }
    b.resetChildPagingTop();
    b.resetChildPagingBottom();
    b.grid.getView().focusRow(a[0]);
    if (b.rendered) {
        Ext.resumeLayouts(true)
    }
}, getStoreListeners: function () {
    return{beforeload: this.onBeforeLoad, bulkremove: this.onLoad, remove: this.onLoad, clear: this.onLoad, load: this.onLoad}
}, onBeforeDestroy: function () {
    this.bindStore("ext-empty-store")
}, onBeforeLoad: function () {
    var c = this, a = c.items.indexOf(c.getLayout().getActiveItem());
    try {
        c.grid.getView().getSelectionModel().deselectAll(true)
    } catch (b) {
    }
    if (a !== 1) {
        c.getLayout().setActiveItem(1)
    }
}, onLoad: function () {
    var c = this, b = c.grid.store.getCount(), d = b === 0, a = c.items.indexOf(c.getLayout().getActiveItem());
    if (d && a !== 0) {
        c.getLayout().setActiveItem(0)
    } else {
        if (!d && a !== 1) {
            c.getLayout().setActiveItem(1)
        }
    }
    if (c.selectByDefault && !d) {
        c.grid.getView().getSelectionModel().preventFocus = true;
        c.grid.getView().getSelectionModel().select(0)
    }
}, getWrapperCt: function () {
    return this.down("#wrapper-container")
}});
Ext.define("Uni.view.form.CheckboxGroup", {extend: "Ext.form.CheckboxGroup", alias: "widget.checkboxstore", mixins: {bindable: "Ext.util.Bindable"}, displayField: "name", valueField: "id", initComponent: function () {
    var a = this;
    a.bindStore(a.store || "ext-empty-store", true);
    this.callParent(arguments)
}, refresh: function () {
    var a = this;
    a.removeAll();
    a.store.each(function (b) {
        a.add({xtype: "checkbox", boxLabel: b.get(a.displayField), inputValue: b.get(a.valueField), name: a.name, getModelData: function () {
            return null
        }})
    })
}, getModelData: function () {
    var c = this, a = [], b = {};
    Ext.Array.each(c.query("checkbox"), function (d) {
        if (d.getValue()) {
            c.store.each(function (e) {
                if (e.get(c.valueField) === d.inputValue) {
                    a.push(e.raw)
                }
            })
        }
    });
    b[c.name] = a;
    return b
}, setValue: function (b) {
    var a = {};
    a[this.name] = b;
    this.callParent([a])
}, getStoreListeners: function () {
    return{load: this.refresh}
}});
Ext.define("Uni.view.grid.SelectionGrid", {extend: "Ext.grid.Panel", xtype: "selection-grid", requires: ["Ext.grid.plugin.BufferedRenderer"], bottomToolbarHeight: 27, selType: "checkboxmodel", selModel: {mode: "MULTI", showHeaderCheckbox: false}, overflowY: "auto", maxHeight: 450, plugins: [
    {ptype: "bufferedrenderer", trailingBufferZone: 5, leadingBufferZone: 5, scrollToLoadBuffer: 10, onViewResize: function (d, f, b, c, h) {
        if (!h || b !== h) {
            var g = this, e, a;
            if (d.all.getCount()) {
                delete g.rowHeight
            }
            a = g.getScrollHeight();
            e = 18;
            g.viewSize = g.setViewSize(e);
            g.stretchView(d, a)
        }
    }}
], counterTextFn: function (a) {
    return Uni.I18n.translatePlural("grid.BulkSelection.counterText", a, "UNI", "{0} items selected")
}, uncheckText: Uni.I18n.translate("general.uncheckAll", "UNI", "Uncheck all"), initComponent: function () {
    var a = this;
    a.dockedItems = [
        {xtype: "toolbar", dock: "top", itemId: "topToolbarContainer", layout: {type: "hbox", align: "middle"}, items: [
            {xtype: "text", itemId: "selectionCounter", text: a.counterTextFn(0), margin: "0 8 0 0"},
            {xtype: "button", itemId: "uncheckAllButton", text: a.uncheckText, action: "uncheckAll", margin: "0 0 0 8", disabled: true}
        ]}
    ];
    a.callParent(arguments);
    a.getUncheckAllButton().on("click", a.onClickUncheckAllButton, a);
    a.on("selectionchange", a.onSelectionChange, a)
}, onClickUncheckAllButton: function (a) {
    var b = this;
    b.view.getSelectionModel().deselectAll();
    a.setDisabled(true)
}, onSelectionChange: function () {
    var b = this, a = b.view.getSelectionModel().getSelection();
    b.getSelectionCounter().setText(b.counterTextFn(a.length));
    b.getUncheckAllButton().setDisabled(a.length === 0);
    b.doLayout()
}, getSelectionCounter: function () {
    return this.down("#selectionCounter")
}, getUncheckAllButton: function () {
    return this.down("#uncheckAllButton")
}, getTopToolbarContainer: function () {
    return this.down("#topToolbarContainer")
}});
Ext.define("Uni.view.grid.BulkSelection", {extend: "Uni.view.grid.SelectionGrid", xtype: "bulk-selection-grid", maxHeight: 600, allLabel: Uni.I18n.translate("grid.BulkSelection.allLabel", "UNI", "All items"), allDescription: Uni.I18n.translate("grid.BulkSelection.allDescription", "UNI", "Select all items"), selectedLabel: Uni.I18n.translate("grid.BulkSelection.selectedLabel", "UNI", "Selected items"), selectedDescription: Uni.I18n.translate("grid.BulkSelection.selectedDescription", "UNI", "Select items in table"), addText: Uni.I18n.translate("general.add", "UNI", "Add"), cancelText: Uni.I18n.translate("general.cancel", "UNI", "Cancel"), cancelHref: window.location.href, allChosenByDefault: true, allInputValue: "allItems", selectedInputValue: "selectedItems", radioGroupName: "selectedGroupType-" + new Date().getTime() * Math.random(), bottomToolbarHidden: false, gridHeight: 0, gridHeaderHeight: 0, initComponent: function () {
    var a = this;
    a.addEvents("allitemsadd", "selecteditemsadd");
    a.callParent(arguments);
    a.addDocked({xtype: "radiogroup", dock: "top", itemId: "itemradiogroup", columns: 1, vertical: true, submitValue: false, defaults: {padding: "0 0 16 0"}, items: [
        {name: a.radioGroupName, boxLabel: "<b>" + a.allLabel + "</b>", afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + a.allDescription + "</span>", inputValue: a.allInputValue, checked: a.allChosenByDefault},
        {name: a.radioGroupName, boxLabel: "<b>" + a.selectedLabel + "</b>", afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + a.selectedDescription + "</span>", inputValue: a.selectedInputValue, checked: !a.allChosenByDefault}
    ]}, 0);
    a.addDocked({xtype: "toolbar", dock: "bottom", itemId: "bottomToolbar", layout: "hbox", items: [
        {xtype: "button", itemId: "addButton", text: a.addText, action: "add", ui: "action"},
        {xtype: "button", itemId: "cancelButton", text: a.cancelText, action: "cancel", ui: "link", href: a.cancelHref}
    ]});
    a.getSelectionGroupType().on("change", a.onChangeSelectionGroupType, a);
    a.getAddButton().on("click", a.onClickAddButton, a);
    a.on("selectionchange", a.onBulkSelectionChange, a);
    if (a.bottomToolbarHidden) {
        a.hideBottomToolbar()
    }
    a.store.on("afterrender", a.onChangeSelectionGroupType, a, {single: true});
    a.store.on("load", a.onSelectDefaultGroupType, a, {single: true})
}, onSelectDefaultGroupType: function () {
    var a = this, b = {};
    if (a.rendered) {
        b[a.radioGroupName] = a.allChosenByDefault ? a.allInputValue : a.selectedInputValue;
        a.getSelectionGroupType().setValue(b);
        a.getSelectionGroupType().fireEvent("change")
    }
    a.onChangeSelectionGroupType()
}, onChangeSelectionGroupType: function (d, c) {
    var b = this, a = b.view.getSelectionModel().getSelection();
    b.getAddButton().setDisabled(!b.isAllSelected() && a.length === 0);
    b.setGridVisible(!b.isAllSelected())
}, setGridVisible: function (c) {
    var i = this, b = i.gridHeight, a = i.gridHeaderHeight, f, h, d = "force-no-border";
    i.getTopToolbarContainer().setVisible(c);
    if (i.rendered) {
        f = i.getView().height;
        h = i.headerCt.height;
        if (!c) {
            b = 0;
            a = 0;
            i.addCls(d)
        } else {
            i.removeCls(d)
        }
        if (f !== 0 && h !== 0) {
            i.gridHeight = f;
            i.gridHeaderHeight = h
        }
        if (typeof b === "undefined") {
            var j = i.getView().getNode(0), e = Ext.get(j);
            if (e !== null) {
                var g = i.store.getCount() > 10 ? 10 : i.store.getCount();
                b = g * e.getHeight()
            }
        }
        i.getView().height = b;
        i.headerCt.height = a;
        i.doLayout()
    }
}, onClickAddButton: function () {
    var b = this, a = b.view.getSelectionModel().getSelection();
    if (b.isAllSelected()) {
        b.fireEvent("allitemsadd")
    } else {
        if (a.length > 0) {
            b.fireEvent("selecteditemsadd", a)
        }
    }
}, onBulkSelectionChange: function () {
    var b = this, a = b.view.getSelectionModel().getSelection();
    b.getAddButton().setDisabled(!b.isAllSelected() && a.length === 0)
}, isAllSelected: function () {
    var b = this, a = b.getSelectionGroupType().getValue();
    return a[b.radioGroupName] === b.allInputValue
}, getSelectionGroupType: function () {
    return this.down("radiogroup")
}, getAddButton: function () {
    return this.down("#addButton")
}, getCancelButton: function () {
    return this.down("#cancelButton")
}, getBottomToolbar: function () {
    return this.down("#bottomToolbar")
}, hideBottomToolbar: function () {
    this.getBottomToolbar().setVisible(false)
}});
Ext.define("Uni.view.grid.ConnectedGrid", {extend: "Ext.container.Container", xtype: "connected-grid", requires: ["Uni.grid.plugin.DragDropWithoutIndication"], layout: {type: "hbox"}, allItemsTitle: null, allItemsStoreName: null, selectedItemsTitle: null, selectedItemsStoreName: null, displayedColumn: null, disableIndication: false, enableSorting: false, initComponent: function () {
    var c = this, b = c.id + "allItemsGrid", d = c.id + "selectedItemsGrid", a = "gridviewdragdrop";
    if (c.disableIndication) {
        a = "gridviewdragdropwithoutindication"
    }
    if (Ext.isEmpty(c.displayedColumn)) {
        c.displayedColumn = "name"
    }
    c.items = [
        {xtype: "gridpanel", itemId: "allItemsGrid", store: c.allItemsStoreName, title: c.allItemsTitle, hideHeaders: true, selModel: {mode: "MULTI"}, columns: [
            {dataIndex: c.displayedColumn, flex: 1}
        ], viewConfig: {plugins: {ptype: a, dragGroup: b, dropGroup: d}, listeners: {drop: function (f, g, h, e) {
            c.enableSorting && c.getAllItemsStore().sort(c.displayedColumn, "ASC")
        }}}, height: 400, width: 200},
        {xtype: "container", margin: "0 10", layout: {type: "vbox", align: "center", pack: "center"}, defaults: {margin: "5"}, items: [
            {xtype: "container", height: 100},
            {xtype: "button", itemId: "selectAllItems", width: 50, text: ">>", handler: function () {
                c.selectAllItems()
            }},
            {xtype: "button", itemId: "selectItems", width: 50, text: ">", handler: function () {
                c.selectItems()
            }},
            {xtype: "button", itemId: "deselectItems", width: 50, text: "<", handler: function () {
                c.deselectItems()
            }},
            {xtype: "button", itemId: "deselectAllItems", width: 50, text: "<<", handler: function () {
                c.deselectAllItems()
            }}
        ]},
        {xtype: "gridpanel", itemId: "selectedItemsGrid", store: c.selectedItemsStoreName, title: c.selectedItemsTitle, hideHeaders: true, selModel: {mode: "MULTI"}, columns: [
            {dataIndex: c.displayedColumn, flex: 1}
        ], viewConfig: {plugins: {ptype: a, dragGroup: d, dropGroup: b}, listeners: {drop: function (f, g, h, e) {
            c.enableSorting && c.getSelectedItemsStore().sort(c.displayedColumn, "ASC")
        }}}, height: 400, width: 200}
    ];
    c.callParent(arguments)
}, getAllItemsGrid: function () {
    return this.down("#allItemsGrid")
}, getSelectedItemsGrid: function () {
    return this.down("#selectedItemsGrid")
}, getAllItemsStore: function () {
    var a = this.getAllItemsGrid();
    if (a) {
        return a.getStore()
    } else {
        return null
    }
}, getSelectedItemsStore: function () {
    var a = this.getSelectedItemsGrid();
    if (a) {
        return a.getStore()
    } else {
        return null
    }
}, selectAllItems: function () {
    var b = this.getAllItemsStore(), a = this.getSelectedItemsStore();
    if (b && a) {
        b.each(function (c) {
            a.add(c)
        });
        b.removeAll()
    }
    this.enableSorting && a.sort(this.displayedColumn, "ASC")
}, selectItems: function () {
    var a = this.getAllItemsGrid(), d = a.getSelectionModel().getSelection(), c = this.getAllItemsStore(), b = this.getSelectedItemsStore();
    if (c && b) {
        Ext.Array.each(d, function (e) {
            c.remove(e);
            b.add(e)
        })
    }
    this.enableSorting && b.sort(this.displayedColumn, "ASC")
}, deselectItems: function () {
    var a = this.getSelectedItemsGrid(), d = a.getSelectionModel().getSelection(), c = this.getAllItemsStore(), b = this.getSelectedItemsStore();
    if (c && b) {
        Ext.Array.each(d, function (e) {
            c.add(e);
            b.remove(e)
        })
    }
    this.enableSorting && c.sort(this.displayedColumn, "ASC")
}, deselectAllItems: function () {
    var b = this.getAllItemsStore(), a = this.getSelectedItemsStore();
    if (b && a) {
        a.each(function (c) {
            b.add(c)
        });
        a.removeAll()
    }
    this.enableSorting && b.sort(this.displayedColumn, "ASC")
}});
Ext.define("Uni.view.menu.NavigationItem", {extend: "Ext.menu.Item", alias: "widget.navigation-item", arrowCls: null, renderTpl: ['<tpl if="plain">', "{text}", "<tpl else>", '<a id="{id}-itemEl"', ' class="' + Ext.baseCSSPrefix + 'menu-item-link{childElCls}"', ' href="{href}"', '<tpl if="hrefTarget"> target="{hrefTarget}"</tpl>', ' hidefocus="true"', ' unselectable="on"', '<tpl if="tabIndex">', ' tabIndex="{tabIndex}"', "</tpl>", ">", '<div role="img" id="{id}-iconEl" class="' + Ext.baseCSSPrefix + "menu-item-icon {iconCls}", '{childElCls} {glyphCls}" style="<tpl if="icon">background-image:url({icon});</tpl>', '<tpl if="glyph && glyphFontFamily">font-family:{glyphFontFamily};</tpl>">', '<tpl if="glyph">&#{glyph};</tpl>', "</div>", '<span class="navigation-item-number">{index}</span>', '<span id="{id}-textEl" class="' + Ext.baseCSSPrefix + 'menu-item-text" unselectable="on">{text}</span>', "</a>", "</tpl>"]});
Ext.define("Uni.view.menu.NavigationMenu", {extend: "Ext.menu.Menu", alias: "widget.navigation-menu", cls: "x-navigation-menu", requires: ["Uni.view.menu.NavigationItem"], defaults: {xtype: "navigation-item"}, floating: false, hidden: false, activeStep: 1, jumpBack: true, jumpForward: false, listeners: {add: function (c, b, a) {
    b.renderData.index = b.index = ++a;
    this.updateItemCls(a)
}, click: function (b, a) {
    a.index < b.activeStep ? (b.jumpBack ? b.moveTo(a.index) : null) : (b.jumpForward ? b.moveTo(a.index) : null)
}}, updateItemCls: function (a) {
    var c = this, b = c.items.getAt(a - 1);
    b.removeCls(["step-completed", "step-active", "step-non-completed"]);
    a < c.activeStep ? b.addCls("step-completed") : (a > c.activeStep ? b.addCls("step-non-completed") : b.addCls("step-active"))
}, moveTo: function (b) {
    var a = this;
    a.moveToStep(b);
    a.fireEvent("movetostep", a.activeStep)
}, moveToStep: function (b) {
    var a = this, c = a.items.getCount();
    if (1 < b < c) {
        a.activeStep = b;
        a.items.each(function (e) {
            var d = e.index;
            a.updateItemCls(d)
        })
    }
}, getActiveStep: function () {
    return this.activeStep
}, moveNextStep: function () {
    this.moveToStep(this.activeStep + 1)
}, movePrevStep: function () {
    this.moveToStep(this.activeStep - 1)
}});
Ext.define("Uni.view.navigation.SubMenu", {extend: "Ext.menu.Menu", alias: "widget.navigationSubMenu", floating: false, ui: "side-menu", plain: true, width: 256, defaults: {xtype: "menuitem", hrefTarget: "_self"}, selectedCls: "current", initComponent: function () {
    var a = this;
    Ext.util.History.addListener("change", function (b) {
        a.checkNavigation(b)
    });
    this.callParent(this)
}, toggleMenuItem: function (b) {
    var a = this.selectedCls;
    var c = this.items.getAt(b);
    if (c.hasCls(a)) {
        c.removeCls(a)
    } else {
        c.addCls(a)
    }
}, cleanSelection: function () {
    var a = this.selectedCls;
    this.items.each(function (b) {
        b.removeCls(a)
    })
}, checkNavigation: function (a) {
    var b = this;
    b.items.each(function (d, c) {
        if ((d.href != null) && (Ext.String.endsWith(d.href, a))) {
            b.cleanSelection();
            b.toggleMenuItem(c)
        }
    })
}});
Ext.define("Uni.view.notifications.NoItemsFoundPanel", {extend: "Ext.container.Container", xtype: "no-items-found-panel", title: Uni.I18n.translate("notifications.NoItemsFoundPanel.title", "UNI", "No items found"), reasonsText: Uni.I18n.translate("notifications.NoItemsFoundPanel.reasonsText", "UNI", "This could be because:"), reasons: [], stepsText: Uni.I18n.translate("notifications.NoItemsFoundPanel.stepsText", "UNI", "Possible steps:"), stepItems: [], layout: {type: "vbox"}, items: [
    {xtype: "panel", itemId: "wrapper", cls: Uni.About.baseCssPrefix + "panel-no-items-found", ui: "medium", framed: true, layout: {type: "vbox", align: "stretch"}}
], initComponent: function () {
    var a = this;
    a.callParent(arguments);
    var c = a.down("#wrapper");
    c.setTitle(a.title);
    if (Ext.isArray(a.reasons) || Ext.isString(a.reasons)) {
        var b = a.formatReasons(a.reasons);
        c.add({xtype: "component", html: b})
    }
    if (!Ext.isEmpty(a.stepItems) && Ext.isArray(a.stepItems) || Ext.isObject(a.stepItems)) {
        c.add({xtype: "component", html: '<span class="steps-text">' + a.stepsText + "</span>"});
        c.add(a.createSteps(a.stepItems))
    }
}, formatReasons: function (d) {
    var b = this, a = '<span class="reasons-text">' + b.reasonsText + "</span>", c = "";
    if (Ext.isArray(d)) {
        Ext.Array.each(d, function (e) {
            c += b.formatReason(e)
        })
    } else {
        if (Ext.isString(d)) {
            c += b.formatReason(d)
        }
    }
    return a + "<ul>" + c + "</ul>"
}, formatReason: function (a) {
    return"<li>" + a + "</li>"
}, createSteps: function (b) {
    var a = Ext.create("Ext.container.Container", {cls: "steps", layout: {type: "hbox"}, defaults: {xtype: "button", hrefTarget: "_self", margin: "0 8px 0 0"}});
    if (Ext.isArray(b)) {
        Ext.Array.each(b, function (c) {
            a.add(Ext.clone(c))
        })
    } else {
        if (Ext.isString(b)) {
            a.add(Ext.clone(b))
        }
    }
    return a
}});
Ext.define("Uni.view.panel.StepPanel", {extend: "Ext.panel.Panel", alias: "widget.step-panel", text: "Some step text", indexText: "12", index: null, isLastItem: null, isFirstItem: null, isMiddleItem: null, isOneItem: null, isActiveStep: null, isCompletedStep: null, isNonCompletedStep: null, state: "noncompleted", layout: {type: "vbox", align: "left"}, states: {active: ["step-active", "step-label-active"], completed: ["step-completed", "step-label-completed"], noncompleted: ["step-non-completed", "step-label-non-completed"]}, items: [], handler: function () {
}, getStepDots: function () {
    return{layout: {type: "vbox", align: "left"}, cls: "x-panel-step-dots", items: [
        {xtype: "box", name: "bottomdots", cls: "x-image-step-dots"}
    ]}
}, getStepLabel: function () {
    var a = this;
    return{name: "step-label-side", layout: {type: "hbox", align: "middle"}, items: [
        {xtype: "button", name: "steppanellabel", text: a.text, cls: "x-label-step", ui: "step-label-active", handler: a.handler}
    ]}
}, getStepPanelLayout: function () {
    var a = this;
    return{name: "basepanel", layout: {type: "hbox", align: "middle"}, items: [
        {name: "steppanelbutton", xtype: "step-button", ui: "step-active", text: a.indexText, handler: a.handler},
        a.getStepLabel()
    ]}
}, doStepLayout: function () {
    var b = this, a = null;
    b.isFirstItem && (a = [b.getStepPanelLayout(), b.getStepDots()]);
    b.isLastItem && (a = [b.getStepDots(), b.getStepPanelLayout()]);
    b.isMiddleItem && (a = [b.getStepDots(), b.getStepPanelLayout(), b.getStepDots()]);
    b.isOneItem && (a = [b.getStepPanelLayout()]);
    b.items = a
}, afterRender: function (a) {
    a.stepButton = this.down("panel[name=basepanel]");
    a.stepLabel = this.down();
    console.log(this.stepButton, this.stepLabel)
}, setState: function (a) {
    !a && (this.state = a);
    console.log(this, this.stepButton, this.stepLabel);
    this.stepButton.setUI(this.states[this.state][0]);
    this.stepLabel.setUI(this.states[this.state][1])
}, getState: function () {
    return this.state
}, initComponent: function () {
    var a = this;
    a.doStepLayout();
    a.callParent(arguments)
}});
Ext.define("Uni.view.toolbar.PagingBottom", {extend: "Ext.toolbar.Paging", xtype: "pagingtoolbarbottom", ui: "pagingtoolbarbottom", defaultButtonUI: "default", requires: ["Uni.util.QueryString", "Uni.util.History"], params: {}, defaultPageSize: 10, totalCount: 0, totalPages: 0, isFullTotalCount: false, isSecondPagination: false, pageSizeParam: "limit", pageStartParam: "start", deferLoading: false, updatePagingParams: true, itemsPerPageMsg: Uni.I18n.translate("general.itemsPerPage", "UNI", "Items per page"), firstText: Uni.I18n.translate("general.firstPage", "UNI", "First page"), prevText: Uni.I18n.translate("general.previousPage", "UNI", "Previous page"), nextText: Uni.I18n.translate("general.nextPage", "UNI", "Next page"), lastText: Uni.I18n.translate("general.lastPage", "UNI", "Last page"), pageSizeStore: Ext.create("Ext.data.Store", {fields: ["value"], data: [
    {value: "10"},
    {value: "20"},
    {value: "50"},
    {value: "100"}
]}), pageNavItemTpl: new Ext.XTemplate('<a href="{1}">{0}</a>'), currentPageNavItemTpl: new Ext.XTemplate("<span>{0}</span>"), initComponent: function () {
    this.callParent(arguments);
    this.initPageSizeAndStartFromQueryString();
    var a = this.child("#pagingCombo");
    a.setRawValue("" + this.store.pageSize)
}, initPageSizeAndStartFromQueryString: function () {
    var c = Uni.util.QueryString.getQueryStringValues(), a = c[this.pageSizeParam], b = c[this.pageStartParam];
    if (this.isSecondPagination) {
        b = (this.store.currentPage - 1) * this.store.pageSize;
        a = this.store.pageSize
    } else {
        b = parseInt(b, this.defaultPageSize) || 0;
        a = parseInt(a, this.defaultPageSize) || this.store.pageSize
    }
    this.initPageSizeAndStart(a, b)
}, initPageSizeAndStart: function (a, d) {
    var b = this, c = Math.max(Math.ceil((d + 1) / a), 1);
    if (this.store.currentPage !== c) {
        this.store.currentPage = c
    }
    a = this.adjustPageSize(a);
    if (this.store.pageSize !== a) {
        this.store.pageSize = a
    }
    this.initExtraParams();
    if (!b.deferLoading) {
        this.store.load({params: b.params, callback: function (e) {
            if (e !== null && e.length === 0 && c > 1) {
                b.initPageSizeAndStart(a, d - a)
            }
        }})
    }
}, adjustPageSize: function (b) {
    var c = b, a;
    this.pageSizeStore.each(function (d) {
        var e = parseInt(d.data.value, 10), f = Math.abs(b - e);
        if (f < a || typeof a === "undefined") {
            a = f;
            c = e
        }
    });
    return c
}, onPageSizeChange: function (b, d) {
    var c = this, a = parseInt(d, 10);
    c.resetPageSize(a);
    c.updateQueryString()
}, resetPageSize: function (a) {
    var c = this, b = Math.max(Math.ceil((c.getPageStartValue() + 1) / a), 1);
    c.store.currentPage = b;
    c.store.pageSize = a;
    c.totalPages = 0;
    this.initExtraParams();
    c.store.load({params: c.params, callback: function (d) {
        if (d !== null && d.length === 0 && b > 1) {
            c.initPageSizeAndStart(a, pageStart - a)
        }
    }})
}, updateQueryString: function (b) {
    var a = this;
    a.updateHrefIfNecessary(a.buildQueryString(b))
}, resetQueryString: function () {
    var a = this;
    var b = {};
    b[a.pageSizeParam] = undefined;
    b[a.pageStartParam] = undefined;
    a.updateHrefIfNecessary(Uni.util.QueryString.buildHrefWithQueryString(b))
}, updateHrefIfNecessary: function (a) {
    if (this.updatePagingParams && location.href !== a) {
        Uni.util.History.suspendEventsForNextCall();
        location.href = a
    }
}, resetPaging: function () {
    var b = this, a = b.child("#pageNavItem");
    b.totalCount = 0;
    b.totalPages = 0;
    b.isFullTotalCount = false;
    b.store.currentPage = 1;
    b.initPageNavItems(a, 1, b.totalPages);
    b.resetQueryString()
}, buildQueryString: function (c) {
    var a = this;
    if (typeof c === "undefined") {
        c = a.getPageStartValue()
    }
    var b = {};
    b[a.pageSizeParam] = a.store.pageSize;
    b[a.pageStartParam] = c;
    return Uni.util.QueryString.buildHrefWithQueryString(b)
}, getPageStartValue: function (b) {
    var c = this, a = c.getPageData(), d = Math.max(a.fromRecord - 1, 0);
    b = b || 0;
    return d + c.store.pageSize * b
}, getPagingItems: function () {
    var a = this;
    return[
        {xtype: "tbtext", text: a.itemsPerPageMsg},
        {xtype: "combobox", itemId: "pagingCombo", store: a.pageSizeStore, width: 64, queryMode: "local", displayField: "value", valueField: "value", enableKeyEvents: true, keyNavEnabled: false, submitValue: false, isFormField: false, allowBlank: false, forceSelection: true, editable: false, scope: a, listeners: {change: a.onPageSizeChange, scope: a}},
        {xtype: "component", html: "&nbsp;", flex: 1},
        {itemId: "first", ui: "gridnav", tooltip: a.firstText, overflowText: a.firstText, iconCls: Ext.baseCSSPrefix + "tbar-page-first", disabled: true, handler: a.moveFirst, scope: a},
        {itemId: "prev", ui: "gridnav", tooltip: a.prevText, overflowText: a.prevText, iconCls: Ext.baseCSSPrefix + "tbar-page-prev", disabled: true, handler: a.movePrevious, scope: a},
        {xtype: "container", itemId: "pageNavItem", cls: "pagenav", layout: "hbox"},
        {itemId: "next", ui: "gridnav", tooltip: a.nextText, overflowText: a.nextText, iconCls: Ext.baseCSSPrefix + "tbar-page-next", disabled: true, handler: a.moveNext, scope: a},
        {itemId: "last", ui: "gridnav", tooltip: a.lastText, overflowText: a.lastText, iconCls: Ext.baseCSSPrefix + "tbar-page-last", disabled: true, handler: a.moveLast, scope: a}
    ]
}, onLoad: function () {
    var f = this, c, a, b, e, g, d;
    e = f.store.getCount();
    g = e === 0;
    if (!g) {
        c = f.getPageData();
        a = c.currentPage;
        b = c.pageCount;
        if (f.isSecondPagination) {
            f.totalCount = f.store.getTotalCount()
        } else {
            f.totalCount = f.totalCount < f.store.getTotalCount() ? f.store.getTotalCount() : f.totalCount
        }
        f.totalPages = Math.ceil(f.totalCount / f.store.pageSize)
    } else {
        a = 0;
        b = 0
    }
    Ext.suspendLayouts();
    d = f.child("#pageNavItem");
    f.initPageNavItems(d, a, f.totalPages);
    f.setChildDisabled("#first", a === 1 || g);
    f.setChildDisabled("#prev", a === 1 || g);
    f.setChildDisabled("#next", a === b || g);
    if (f.isFullTotalCount || (typeof c !== "undefined" && f.store.pageSize * c.currentPage >= f.totalCount)) {
        f.setChildDisabled("#last", typeof c === "undefined" || f.totalPages === c.currentPage);
        f.isFullTotalCount = true
    }
    f.updateInfo();
    Ext.resumeLayouts(true);
    f.fireEvent("change", f, c)
}, moveLast: function () {
    var c = this, a = c.getPageData().pageCount, b = a < c.totalPages ? c.totalPages : a;
    if (c.fireEvent("beforechange", c, b) !== false) {
        c.initExtraParams();
        c.store.loadPage(b, {params: c.params});
        c.updateQueryString();
        return true
    }
    return false
}, moveFirst: function () {
    var a = this;
    if (this.fireEvent("beforechange", this, 1) !== false) {
        a.initExtraParams();
        a.store.loadPage(1, {params: a.params});
        a.updateQueryString();
        return true
    }
    return false
}, movePrevious: function () {
    var c = this, a = c.store, b = a.currentPage - 1;
    if (b > 0) {
        if (c.fireEvent("beforechange", c, b) !== false) {
            c.initExtraParams();
            a.loadPage(a.currentPage - 1, {params: c.params});
            c.updateQueryString();
            return true
        }
    }
    return false
}, moveNext: function () {
    var d = this, a = d.store, c = d.getPageData().pageCount, b = a.currentPage + 1;
    if (b <= c) {
        if (d.fireEvent("beforechange", d, b) !== false) {
            d.initExtraParams();
            a.loadPage(a.currentPage + 1, {params: d.params});
            d.updateQueryString();
            return true
        }
    }
    return false
}, initPageNavItems: function (a, h, c) {
    var f = this, j = 10, e = Math.max(1, h - 5), k = Math.min(e + j - 1, c), g, b;
    if (k - e < j - 1) {
        e = Math.max(1, k - j + 1)
    }
    e = e < 1 ? 1 : e;
    k = k > c ? c : k;
    if (a.rendered) {
        Ext.suspendLayouts()
    }
    a.removeAll();
    for (var d = e; d <= k; d++) {
        g = d - h;
        b = f.getPageStartValue(g);
        a.add(f.createPageNavItem(d, b, g === 0))
    }
    if (a.rendered) {
        Ext.resumeLayouts(true)
    }
}, createPageNavItem: function (e, f, b) {
    var d = this, a = d.formatSinglePageNavItem(e, f, b), c = Ext.create("Ext.Component", {baseCls: Ext.baseCSSPrefix + "toolbar-text", cls: b ? "active" : "", html: a});
    if (!b) {
        c.on("afterrender", function () {
            d.addNavItemClickHandler(d, e, c)
        })
    }
    return c
}, addNavItemClickHandler: function (b, c, a) {
    a.getEl().on("click", function () {
        Ext.History.suspendEvents();
        b.initExtraParams();
        b.store.loadPage(c, {params: b.params, callback: function () {
            var d = Ext.create("Ext.util.DelayedTask", function () {
                Ext.History.resumeEvents()
            });
            d.delay(250)
        }})
    })
}, formatSinglePageNavItem: function (e, f, a) {
    var d = this, c = a ? d.currentPageNavItemTpl : d.pageNavItemTpl, b = d.buildQueryString(f);
    return c.apply([e, b])
}, initExtraParams: function () {
    var a = this;
    if (Ext.isArray(a.params)) {
        a.params.forEach(function (c) {
            var b = Object.keys(c)[0];
            var d = c[b];
            a.store.getProxy().setExtraParam(b, d)
        })
    }
}});
Ext.define("Uni.view.toolbar.PagingTop", {extend: "Ext.toolbar.Paging", xtype: "pagingtoolbartop", ui: "pagingtoolbartop", displayInfo: false, usesExactCount: false, displayMsg: Uni.I18n.translate("general.displayMsgItems", "UNI", "{0} - {1} of {2} items"), displayMoreMsg: Uni.I18n.translate("general.displayMsgMoreItems", "UNI", "{0} - {1} of more than {2} items"), emptyMsg: Uni.I18n.translate("general.noItemsToDisplay", "UNI", "There are no items to display"), isFullTotalCount: false, totalCount: -1, defaultButtonUI: "default", initComponent: function () {
    this.callParent(arguments)
}, getPagingItems: function () {
    return[
        {xtype: "tbtext", itemId: "displayItem"},
        "->"
    ]
}, updateInfo: function () {
    var e = this, d = e.child("#displayItem"), b = e.store, c = e.getPageData(), a, f;
    if (d) {
        if (e.usesExactCount) {
            e.totalCount = b.getTotalCount()
        } else {
            e.totalCount = e.totalCount < b.getTotalCount() ? b.getTotalCount() : e.totalCount
        }
        if (b.getCount() === 0) {
            e.totalCount = -1;
            f = e.emptyMsg
        } else {
            a = e.totalCount - 1;
            f = e.displayMoreMsg;
            if (e.isFullTotalCount || b.pageSize * c.currentPage >= e.totalCount || e.usesExactCount) {
                e.isFullTotalCount = true;
                a = e.totalCount;
                f = e.displayMsg
            }
            f = Ext.String.format(f, c.fromRecord, c.toRecord, a)
        }
        d.setText(f)
    }
}, resetPaging: function () {
    var a = this;
    a.onLoad();
    a.totalCount = -1;
    a.isFullTotalCount = false
}, onLoad: function () {
    Ext.suspendLayouts();
    this.updateInfo();
    Ext.resumeLayouts(true);
    this.fireEvent("change", this, this.getPageData())
}});
Ext.define("Uni.view.window.Confirmation", {extend: "Ext.window.MessageBox", xtype: "confirmation-window", cls: Uni.About.baseCssPrefix + "confirmation-window", confirmText: Uni.I18n.translate("general.remove", "UNI", "Remove"), cancelText: Uni.I18n.translate("general.cancel", "UNI", "Cancel"), confirmation: function () {
    var a = this.header.child("[type=close]");
    a.itemId = "confirm";
    this.btnCallback(a);
    delete a.itemId;
    this.hide()
}, cancellation: function () {
    this.close()
}, initComponent: function () {
    var a = this;
    a.buttons = [
        {xtype: "button", action: "confirm", name: "confirm", scope: a, text: a.confirmText, ui: "remove", handler: a.confirmation},
        {xtype: "button", action: "cancel", name: "cancel", scope: a, text: a.cancelText, ui: "link", handler: a.cancellation}
    ];
    a.callParent(arguments)
}, show: function (a) {
    Ext.apply(a, {icon: Ext.MessageBox.ERROR});
    this.callParent(arguments)
}});
Ext.define("Uni.view.window.Wizard", {extend: "Ext.window.Window", constrain: true, requires: ["Ext.layout.container.Card", "Uni.view.navigation.SubMenu"], layout: {type: "vbox", align: "stretch"}, minWidth: 400, minHeight: 200, steps: null, title: "", description: {xtype: "component", html: ""}, items: [
    {xtype: "container", layout: {type: "vbox", align: "stretch"}, items: [
        {xtype: "component", itemId: "wizardTitle", html: ""},
        {xtype: "container", itemId: "wizardDescription", html: ""}
    ]},
    {layout: {type: "hbox", align: "stretch"}, items: [
        {xtype: "container", layout: {type: "vbox", align: "stretchmax"}, items: [
            {xtype: "component", html: "<h3>" + Uni.I18n.translate("window.wizard.menu.title", "UNI", "Steps") + "</h3>"},
            {xtype: "navigationSubMenu", itemId: "stepsMenu"}
        ]},
        {xtype: "container", layout: {type: "vbox", align: "stretch"}, items: [
            {xtype: "component", itemId: "stepsTitle", html: "&nbsp;"},
            {xtype: "container", itemId: "stepsContainer", layout: "card", flex: 1, items: []}
        ]}
    ]}
], bbar: [
    {xtype: "component", flex: 1},
    {text: Uni.I18n.translate("window.wizard.tools.previous", "UNI", "&laquo; Previous"), action: "prev", scope: this, handler: this.prevStep, disabled: true},
    {text: Uni.I18n.translate("window.wizard.tools.next", "UNI", "Next &raquo;"), action: "next", scope: this, handler: this.nextStep, disabled: true},
    {text: Uni.I18n.translate("window.wizard.tools.finish", "UNI", "Finish"), action: "finish"},
    {text: Uni.I18n.translate("window.wizard.tools.cancel", "UNI", "Cancel"), action: "cancel"}
], initComponent: function () {
    var a = this.steps;
    if (a) {
        if (!(a instanceof Ext.Component)) {
            a = Ext.clone(a)
        }
        this.items[1].items[1].items[1].items = a
    }
    this.callParent(arguments);
    this.addCls(Uni.About.baseCssPrefix + "window-wizard");
    this.setTitle(this.title);
    this.setDescription(this.description);
    if (a) {
        this.initStepsMenu(a)
    }
    this.initNavigation()
}, initStepsMenu: function (b) {
    var e = this, a = this.getStepsMenuCmp();
    for (var c = 0; c < b.length; c++) {
        var d = b[c];
        var f = a.add({text: d.title, pressed: c === 0, stepIndex: c});
        f.on("click", function () {
            e.goToStep(this.stepIndex)
        })
    }
    e.checkNavigationState()
}, initNavigation: function () {
    var b = this, a = this.down("button[action=prev]"), d = this.down("button[action=next]"), c = this.down("button[action=cancel]");
    a.on("click", b.prevStep, b);
    d.on("click", b.nextStep, b);
    c.on("click", b.close, b)
}, goToStep: function (b) {
    var a = this.getStepsContainerCmp();
    a.getLayout().setActiveItem(b);
    this.checkNavigationState()
}, prevStep: function () {
    var a = this.getStepsContainerCmp().getLayout(), b = a.getPrev();
    if (b) {
        a.setActiveItem(b)
    }
    this.checkNavigationState()
}, nextStep: function () {
    var b = this.getStepsContainerCmp().getLayout(), a = b.getNext();
    if (a) {
        b.setActiveItem(a)
    }
    this.checkNavigationState()
}, initStepsTitle: function () {
    var b = this.getStepsContainerCmp(), a = b.getLayout().getActiveItem(), c = this.getStepsTitleCmp();
    if (typeof a !== "undefined" && a.hasOwnProperty("title")) {
        c.update("<h3>" + a.title + "</h3>")
    }
}, checkNavigationState: function () {
    var a = this.getStepsMenuCmp(), d = this.getStepsContainerCmp().getLayout(), b = d.getActiveItem(), g = d.getPrev(), f = this.down("button[action=prev]"), e = d.getNext(), j = this.down("button[action=next]");
    for (var c = 0; c < this.getStepsContainerCmp().items.length; c++) {
        var h = this.getStepsContainerCmp().items.items[c];
        if (h.getId() === b.getId()) {
            a.toggleMenuItem(c);
            break
        }
    }
    this.initStepsTitle();
    f.setDisabled(!g);
    j.setDisabled(!e)
}, setTitle: function (a) {
    this.callParent(arguments);
    this.getTitleCmp().update("<h2>" + a + "</h2>")
}, setDescription: function (a) {
    this.getDescriptionCmp().removeAll();
    if (!(a instanceof Ext.Component)) {
        a = Ext.clone(a)
    }
    this.getDescriptionCmp().add(a)
}, getTitleCmp: function () {
    return this.down("#wizardTitle")
}, getDescriptionCmp: function () {
    return this.down("#wizardDescription")
}, getStepsMenuCmp: function () {
    return this.down("#stepsMenu")
}, getStepsTitleCmp: function () {
    return this.down("#stepsTitle")
}, getStepsContainerCmp: function () {
    return this.down("#stepsContainer")
}});
Ext.define("Uni.view.window.ReadingTypeWizard", {extend: "Uni.view.window.Wizard", requires: ["Ext.form.RadioGroup"], width: 800, height: 600, title: Uni.I18n.translate("window.readingtypewizard.title", "UNI", "Select a reading type"), description: {xtype: "container", layout: "vbox", items: [
    {xtype: "component", html: Uni.I18n.translate("window.readingtypewizard.description", "UNI", "Use the steps below to define a value for the different attributes of a reading type")},
    {xtype: "component", html: "TODO"}
]}, initComponent: function () {
    var h = Ext.create("Ext.data.Store", {fields: ["text", "value"]}), e = Ext.create("Ext.data.Store", {fields: ["text", "value"]}), c = Ext.create("Ext.data.Store", {fields: ["text", "value"]}), g = Ext.create("Ext.data.Store", {fields: ["text", "value"]});
    this.initMeasuringPeriodForm(h, e, c, g);
    var f = Ext.create("Ext.data.Store", {fields: ["text", "value"]}), d = Ext.create("Ext.data.Store", {fields: ["text", "value"]}), i = Ext.create("Ext.data.Store", {fields: ["text", "value"]}), b = Ext.create("Ext.data.Store", {fields: ["text", "value"]}), a = Ext.create("Ext.data.Store", {fields: ["text", "value"]});
    this.initCommodityForm(f, d, i, b, a);
    this.steps = [
        {title: Uni.I18n.translate("window.readingtypewizard.introduction", "UNI", "Introduction"), xtype: "container", layout: {type: "vbox", align: "stretchmax"}, items: [
            {xtype: "component", html: Uni.I18n.translate("window.readingtypewizard.introduction.content", "UNI", "<p>A reading type provides a detailed description of a reading value. It is described in terms of 18 key attributes.</p><p>Every attribute that has a value of zero is not applicable to the description.</p><p>Step through this wizard to define a value for each attribute or compound attribute of the reading type. You can skip steps or jump to a specific step by using the navigation on the left.</p>")}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.macroperiod", "UNI", "Macro period"), xtype: "container", layout: {type: "anchor"}, items: [
            {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.macroperiod.description", "UNI", "Reflects how the data is viewed or captured over a long period of time.") + "</p>"},
            {xtype: "radiogroup", itemId: "macroPeriod", columns: 1, anchor: "100%", items: [
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.notapplicable", "UNI", "Not applicable (0)"), name: "macroPeriod", inputValue: 0, checked: true},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.macroperiod.daily", "UNI", "Daily (11)"), name: "macroPeriod", inputValue: 11},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.macroperiod.weekly", "UNI", "Weekly (24)"), name: "macroPeriod", inputValue: 24},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.macroperiod.monthly", "UNI", "Monthly (13)"), name: "macroPeriod", inputValue: 13},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.macroperiod.seasonal", "UNI", "Seasonal (22)"), name: "macroPeriod", inputValue: 22},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.macroperiod.billingperiod", "UNI", "Billing period (8)"), name: "macroPeriod", inputValue: 8},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.macroperiod.specifiedperiod", "UNI", "Specified period (32)"), name: "macroPeriod", inputValue: 32}
            ]}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.dataaggregation", "UNI", "Data aggregation"), xtype: "container", layout: {type: "anchor"}, items: [
            {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.dataaggregation.description", "UNI", "DMay be used to define a mathematical operation carried out over the time period (#1).") + "</p>"},
            {xtype: "radiogroup", itemId: "dataAggregation", columns: 1, anchor: "100%", items: [
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.notapplicable", "UNI", "Not applicable (0)"), name: "dataAggregation", inputValue: 0, checked: true},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.normal", "UNI", "Normal (12)"), name: "dataAggregation", inputValue: 12},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.nominal", "UNI", "Nominal (11)"), name: "dataAggregation", inputValue: 11},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.average", "UNI", "Average (2)"), name: "dataAggregation", inputValue: 2},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.sum", "UNI", "Sum (26)"), name: "dataAggregation", inputValue: 26},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.excess", "UNI", "Excess (4)"), name: "dataAggregation", inputValue: 4},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.lowthreshold", "UNI", "Low threshold (7)"), name: "dataAggregation", inputValue: 7},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.highthreshold", "UNI", "High threshold (5)"), name: "dataAggregation", inputValue: 5},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.low", "UNI", "Low (28)"), name: "dataAggregation", inputValue: 28},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.minimum", "UNI", "Minimum (28)"), name: "dataAggregation", inputValue: 9},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.secondminimum", "UNI", "Second minimum (17)"), name: "dataAggregation", inputValue: 17},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.maximum", "UNI", "Maximum (16)"), name: "dataAggregation", inputValue: 8},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.secondmaximum", "UNI", "Second maximum (16)"), name: "dataAggregation", inputValue: 16},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.thirdmaximum", "UNI", "Third maximum (23)"), name: "dataAggregation", inputValue: 23},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.fourthmaximum", "UNI", "Fourth maximum (24)"), name: "dataAggregation", inputValue: 24},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.fifthmaximum", "UNI", "Fifth maximum (25)"), name: "dataAggregation", inputValue: 25},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.high", "UNI", "High (27)"), name: "dataAggregation", inputValue: 27}
            ]}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.measuringperiod", "UNI", "Measuring period"), xtype: "container", layout: {type: "anchor"}, items: [
            {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.measuringperiod.description", "UNI", "Describes the way the value was originally measured. This doesn't represent the frequency at which it is reported or presented.") + "</p>"},
            {xtype: "radiogroup", itemId: "measuringPeriod", columns: 1, anchor: "100%", items: [
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.notapplicable", "UNI", "Not applicable (0)"), name: "measuringPeriod", inputValue: 0, checked: true},
                {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.measuringperiod.interval", "UNI", "Interval") + "</p>"},
                {xtype: "container", layout: "hbox", items: [
                    {xtype: "radiofield", name: "measuringPeriod", inputValue: "intervalMinute"},
                    {xtype: "combobox", itemId: "intervalMinute", displayField: "text", valueField: "value", queryMode: "local", store: h}
                ]},
                {xtype: "container", layout: "hbox", items: [
                    {xtype: "radiofield", name: "measuringPeriod", inputValue: "intervalHour"},
                    {xtype: "combobox", itemId: "intervalHour", displayField: "text", valueField: "value", queryMode: "local", store: e}
                ]},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaggregation.specifiedinterval", "UNI", "Specified interval (100)"), name: "measuringPeriod", inputValue: 100},
                {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.measuringperiod.fixedblock", "UNI", "Fixed block") + "</p>"},
                {xtype: "container", layout: "hbox", items: [
                    {xtype: "radiofield", name: "measuringPeriod", inputValue: "fixedBlock"},
                    {xtype: "combobox", itemId: "fixedBlock", displayField: "text", valueField: "value", queryMode: "local", store: c}
                ]},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.measuringperiod.specifiedfixedblock", "UNI", "Specified fixed block (101)"), name: "measuringPeriod", inputValue: 101},
                {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.measuringperiod.fixedblock", "UNI", "Fixed block") + "</p>"},
                {xtype: "container", layout: "hbox", items: [
                    {xtype: "radiofield", name: "measuringPeriod", inputValue: "rollingBlock"},
                    {xtype: "combobox", itemId: "rollingBlock", displayField: "text", valueField: "value", queryMode: "local", store: g}
                ]},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.measuringperiod.specifiedrollingblock", "UNI", "Specified rolling block (102)"), name: "measuringPeriod", inputValue: 102}
            ]}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.dataaccumulation", "UNI", "Data accumulation"), xtype: "container", layout: {type: "anchor"}, items: [
            {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.dataaccumulation.description", "UNI", "Indicates how the value is represented to accumulate over time.") + "</p>"},
            {xtype: "radiogroup", itemId: "dataAccumulation", columns: 1, anchor: "100%", items: [
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.notapplicable", "UNI", "Not applicable (0)"), name: "dataAccumulation", inputValue: 0, checked: true},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.bulkquantity", "UNI", "Bulk quantity (1)"), name: "dataAccumulation", inputValue: 1},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.deltadata", "UNI", "Delta data (4)"), name: "dataAccumulation", inputValue: 4},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.cumulative", "UNI", "Cumulative (3)"), name: "dataAccumulation", inputValue: 3},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.continiouscumulative", "UNI", "Continious cumulative (2)"), name: "dataAccumulation", inputValue: 2},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.indicating", "UNI", "Indicating (6)"), name: "dataAccumulation", inputValue: 6},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.summation", "UNI", "Summation (9)"), name: "dataAccumulation", inputValue: 9},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.timedelay", "UNI", "Time delay (10)"), name: "dataAccumulation", inputValue: 10},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.instantaneous", "UNI", "Instantaneous (12)"), name: "dataAccumulation", inputValue: 12},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.latchingquantity", "UNI", "Latching quantity (13)"), name: "dataAccumulation", inputValue: 13},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.dataaccumulation.boundedquantity", "UNI", "Bounded quantity (14)"), name: "dataAccumulation", inputValue: 14}
            ]}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.flowdirection", "UNI", "Flow direction"), xtype: "container", layout: {type: "anchor"}, items: [
            {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.flowdirection.description", "UNI", "Indicates how the value is represented to accumulate over time.") + "</p>"},
            {xtype: "radiogroup", itemId: "flowDirection", columns: 1, anchor: "100%", items: [
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.notapplicable", "UNI", "Not applicable (0)"), name: "flowDirection", inputValue: 0, checked: true},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.forward", "UNI", "Forward (1)"), name: "flowDirection", inputValue: 1},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.reverse", "UNI", "Reverse (19)"), name: "flowDirection", inputValue: 19},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.lagging", "UNI", "Lagging (2)"), name: "flowDirection", inputValue: 2},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.leading", "UNI", "Leading (3)"), name: "flowDirection", inputValue: 3},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.net", "UNI", "Net (4)"), name: "flowDirection", inputValue: 4},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.total", "UNI", "Total (20)"), name: "flowDirection", inputValue: 20},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.totalbyphase", "UNI", "Total by phase (21)"), name: "flowDirection", inputValue: 21},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrant1", "UNI", "Quadrant 1 (15)"), name: "flowDirection", inputValue: 15},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants1and2", "UNI", "Quadrants 1 and 2 (5)"), name: "flowDirection", inputValue: 5},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants1and3", "UNI", "Quadrants 1 and 3 (7)"), name: "flowDirection", inputValue: 7},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants1and4", "UNI", "Quadrants 1 and 4 (8)"), name: "flowDirection", inputValue: 8},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants1minus4", "UNI", "Quadrants 1 minus 4 (9)"), name: "flowDirection", inputValue: 9},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrant2", "UNI", "Quadrant 2 (16)"), name: "flowDirection", inputValue: 16},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants2and3", "UNI", "Quadrant 2 and 3 (10)"), name: "flowDirection", inputValue: 10},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants2and4", "UNI", "Quadrant 2 and 4 (11)"), name: "flowDirection", inputValue: 11},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants2minus3", "UNI", "Quadrant 2 minus 3 (12)"), name: "flowDirection", inputValue: 12},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrant3", "UNI", "Quadrant 3 (17)"), name: "flowDirection", inputValue: 17},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants3and4", "UNI", "Quadrants 3 and 4 (13)"), name: "flowDirection", inputValue: 13},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrants3minus2", "UNI", "Quadrants 3 minus 2 (14)"), name: "flowDirection", inputValue: 14},
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.flowdirection.quadrant4", "UNI", "Bounded quantity (18)"), name: "flowDirection", inputValue: 18}
            ]}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.commodity", "UNI", "Commodity"), xtype: "container", layout: {type: "anchor"}, items: [
            {xtype: "component", html: "<p>" + Uni.I18n.translate("window.readingtypewizard.commodity.description", "UNI", "Some description.") + "</p>"},
            {xtype: "radiogroup", itemId: "commodity", columns: 1, anchor: "100%", items: [
                {boxLabel: Uni.I18n.translate("window.readingtypewizard.notapplicable", "UNI", "Not applicable (0)"), name: "commodity", inputValue: 0, checked: true}
            ]}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.measurementkind", "UNI", "Measurement kind"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Measurement kind</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.interharmonics", "UNI", "Interharmonics"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Interharmonics</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.argument", "UNI", "Argument"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Argument</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.timeofuse", "UNI", "Time of use"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Time of use</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.criticalpeakperiod", "UNI", "Critical peak period"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Critical peak period</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.consumptiontier", "UNI", "Consumption tier"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Consumption tier</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.phase", "UNI", "Phase"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Phase</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.multiplier", "UNI", "Multiplier"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Multiplier</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.unitofmeasure", "UNI", "Unit of measure"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Unit of measure</h3>"}
        ]},
        {title: Uni.I18n.translate("window.readingtypewizard.currency", "UNI", "Currency"), xtype: "container", layout: "vbox", items: [
            {xtype: "component", html: "<h3>Currency</h3>"}
        ]}
    ];
    this.callParent(arguments)
}, initMeasuringPeriodForm: function (b, a, c, d) {
    this.populateSimpleTypeStore(b, "window.readingtypewizard.minute", "{0} minutes", [
        [1, 3],
        [2, 10],
        [3, 14],
        [5, 6],
        [10, 1],
        [12, 78],
        [15, 2],
        [20, 31],
        [30, 5],
        [60, 7]
    ]);
    this.populateSimpleTypeStore(a, "window.readingtypewizard.hour", "{0} hours", [
        [2, 79],
        [3, 83],
        [4, 80],
        [6, 81],
        [12, 82],
        [24, 4]
    ]);
    this.populateSimpleTypeStore(c, "window.readingtypewizard.minutefixed", "{0} minutes fixed block", [
        [1, 56],
        [5, 55],
        [10, 54],
        [15, 53],
        [20, 52],
        [30, 51],
        [60, 50]
    ]);
    this.populateRollingBlockStore(d, "window.readingtypewizard.minuterolling", "{0} minutes rolling block with {1} minute subintervals", [
        [
            [60, 30],
            57
        ],
        [
            [60, 20],
            58
        ],
        [
            [60, 15],
            59
        ],
        [
            [60, 12],
            60
        ],
        [
            [60, 10],
            61
        ],
        [
            [60, 6],
            62
        ],
        [
            [60, 5],
            63
        ],
        [
            [60, 4],
            64
        ],
        [
            [30, 15],
            65
        ],
        [
            [30, 10],
            66
        ],
        [
            [30, 6],
            67
        ],
        [
            [30, 5],
            68
        ],
        [
            [30, 3],
            69
        ],
        [
            [30, 2],
            70
        ],
        [
            [15, 5],
            71
        ],
        [
            [15, 3],
            72
        ],
        [
            [15, 1],
            73
        ]
    ])
}, initCommodityForm: function (d, f, c, b, a) {
    var e = "window.readingtypewizard.commodity.";
    this.populateKeyValueStore(d, [
        [e + "electricityprimarymetered", "fallback", 1]
    ]);
    this.populateKeyValueStore(f, [
        ["key", "fallback", 1]
    ]);
    this.populateKeyValueStore(c, [
        ["key", "fallback", 1]
    ]);
    this.populateKeyValueStore(b, [
        ["key", "fallback", 1]
    ]);
    this.populateKeyValueStore(a, [
        ["key", "fallback", 1]
    ])
}, populateSimpleTypeStore: function (h, j, e, c) {
    for (var d = 0; d < c.length; d++) {
        var b = c[d], g = b[0], a = b[1];
        var f = {text: Uni.I18n.translatePlural(j, g, "UNI", e) + " (" + a + ")", value: a};
        h.add(f)
    }
}, populateRollingBlockStore: function (g, h, e, c) {
    for (var d = 0; d < c.length; d++) {
        var b = c[d], j = b[0], a = b[1];
        var f = {text: Uni.I18n.translate(h, "UNI", e, j) + " (" + a + ")", value: a};
        g.add(f)
    }
}, populateKeyValueStore: function (b, f) {
    for (var d = 0; d < f.length; d++) {
        var g = f[d], c = g[0], h = g[1], e = g[2];
        var a = {text: Uni.I18n.translate(c, "UNI", h) + " (" + e + ")", value: e};
        b.add(a)
    }
}});
Ext.define("Ext.ux.Rixo.form.field.GridPickerKeyNav", {extend: "Ext.util.KeyNav", constructor: function (a) {
    this.pickerField = a.pickerField;
    this.grid = a.grid;
    this.callParent([a.target, Ext.apply({}, a, this.defaultHandlers)])
}, defaultHandlers: {up: function () {
    this.goUp(1)
}, down: function () {
    this.goDown(1)
}, pageUp: function () {
    this.goUp(10)
}, pageDown: function () {
    this.goDown(10)
}, home: function () {
    this.highlightAt(0)
}, end: function () {
    var a = this.getGrid().getStore().getCount();
    if (a > 0) {
        this.highlightAt(a - 1)
    }
}, tab: function (a) {
    var b = this.getPickerField();
    if (b.selectOnTab) {
        this.selectHighlighted(a);
        b.triggerBlur()
    }
    return true
}, enter: function (a) {
    this.selectHighlighted(a)
}}, goUp: function (g) {
    var c = this.getGrid(), b = c.getStore(), f = c.getSelectionModel(), e = f.lastSelected, d = b.getCount(), a = d - g;
    if (d > 0) {
        if (e) {
            a = b.indexOf(e) - g;
            if (a < 0) {
                a = d - 1
            }
        }
        this.highlightAt(a)
    }
}, goDown: function (g) {
    var c = this.getGrid(), b = c.getStore(), f = c.getSelectionModel(), e = f.lastSelected, d = b.getCount(), a = 0;
    if (d > 0) {
        if (e) {
            a = b.indexOf(e) + g;
            if (a >= d) {
                a = 0
            }
        }
        this.highlightAt(a)
    }
}, getPickerField: function () {
    return this.pickerField
}, getGrid: function () {
    return this.grid
}, highlightAt: function (a) {
    this.getPickerField().highlightAt(a)
}, selectHighlighted: function (c) {
    var b = this.getGrid().getSelectionModel().getSelection(), a = b && b[0], d = this.pickerField;
    if (a) {
        d.setValue(a.get(d.valueField))
    }
}});
Ext.define("Ext.ux.Rixo.form.field.GridPicker", {extend: "Ext.form.field.ComboBox", alias: "widget.gridpicker", requires: ["Ext.grid.Panel", "Ext.ux.Rixo.form.field.GridPickerKeyNav"], defaultGridConfig: {xclass: "Ext.grid.Panel", floating: true, focusOnToFront: false, resizable: true, hideHeaders: true, stripeRows: false, rowLines: false, initComponent: function () {
    Ext.grid.Panel.prototype.initComponent.apply(this, arguments);
    var a = this.getStore();
    this.query("pagingtoolbar").forEach(function (b) {
        b.bindStore(a)
    })
}}, gridConfig: null, createPicker: function () {
    return this.picker = this.createGrid()
}, createGrid: function () {
    var a = Ext.create(this.getGridConfig());
    this.bindGrid(a);
    return a
}, getGrid: function () {
    return this.getPicker()
}, getGridConfig: function () {
    var a = {};
    Ext.apply(a, this.gridConfig, this.defaultGridConfig);
    Ext.applyIf(a, {store: this.store, columns: [
        {dataIndex: this.displayField || this.valueField, flex: 1}
    ]});
    if (!a.width) {
        a.width = this.inputEl.getWidth()
    }
    return a
}, bindGrid: function (b) {
    this.grid = b;
    b.ownerCt = this;
    b.registerWithOwnerCt();
    this.mon(b, {scope: this, itemclick: this.onItemClick, refresh: this.onListRefresh, beforeselect: this.onBeforeSelect, beforedeselect: this.onBeforeDeselect, selectionchange: this.onListSelectionChange, afterlayout: function (e) {
        if (e.getStore().getCount()) {
            if (!e.fixingTheFuckingLayout) {
                var f = e.getView().el;
                e.fixingTheFuckingLayout = true;
                f.setHeight("100%");
                f.setStyle("overflow-x", "hidden");
                e.fixingTheFuckingLayout = false
            }
        }
    }});
    var c = this, d = b.getSelectionModel(), a = d.deselectAll;
    d.deselectAll = function () {
        if (!c.ignoreSelection) {
            a.apply(this, arguments)
        }
    }
}, highlightRecord: function (c) {
    var e = this.getGrid(), g = e.getSelectionModel(), b = e.getView(), f = b.getNode(c), a = e.plugins, d = a && a.filter(function (h) {
        return h instanceof Ext.grid.plugin.BufferedRenderer
    })[0];
    g.select(c, false, true);
    if (f) {
        Ext.fly(f).scrollIntoView(b.el, false)
    } else {
        if (d) {
            d.scrollTo(e.store.indexOf(c))
        }
    }
}, highlightAt: function (c) {
    var e = this.getGrid(), g = e.getSelectionModel(), b = e.getView(), f = b.getNode(c), a = e.plugins, d = a && a.filter(function (h) {
        return h instanceof Ext.grid.plugin.BufferedRenderer
    })[0];
    g.select(c, false, true);
    if (f) {
        Ext.fly(f).scrollIntoView(b.el, false)
    } else {
        if (d) {
            d.scrollTo(c)
        }
    }
}, onExpand: function () {
    var c = this, a = c.listKeyNav, b = c.selectOnTab;
    if (a) {
        a.enable()
    } else {
        a = c.listKeyNav = Ext.create("Ext.ux.Rixo.form.field.GridPickerKeyNav", {target: this.inputEl, forceKeyDown: true, pickerField: this, grid: this.getGrid()})
    }
    if (b) {
        c.ignoreMonitorTab = true
    }
    Ext.defer(a.enable, 1, a);
    this.focusWithoutSelection(10)
}, focusWithoutSelection: function (b) {
    function a() {
        var d = this, c = d.selectOnFocus;
        d.selectOnFocus = false;
        d.inputEl.focus();
        d.selectOnFocus = c
    }

    return function (c) {
        if (Ext.isNumber(c)) {
            Ext.defer(a, c, this)
        } else {
            a.call(this)
        }
    }
}(), doAutoSelect: function () {
    var b = this, a = b.picker, c, d;
    if (a && b.autoSelect && b.store.getCount() > 0) {
        c = a.getSelectionModel().lastSelected;
        if (c) {
            a.getSelectionModel().select(c, false, true)
        }
    }
}, onTypeAhead: function () {
    var e = this, d = e.displayField, b = e.store.findRecord(d, e.getRawValue()), c = e.getPicker(), f, a, g;
    if (b) {
        f = b.get(d);
        a = f.length;
        g = e.getRawValue().length;
        this.highlightRecord(b);
        if (g !== 0 && g !== a) {
            e.setRawValue(f);
            e.selectText(g, f.length)
        }
    }
}}, function () {
    if (!Array.prototype.forEach) {
        Array.prototype.forEach = function (c) {
            if (this === void 0 || this === null) {
                throw new TypeError()
            }
            var e = Object(this);
            var a = e.length >>> 0;
            if (typeof c !== "function") {
                throw new TypeError()
            }
            var b = arguments.length >= 2 ? arguments[1] : void 0;
            for (var d = 0; d < a; d++) {
                if (d in e) {
                    c.call(b, e[d], d, e)
                }
            }
        }
    }
});
Ext.define("Ext.ux.Rixo.form.field.GridPicker-4-2-0", {override: "Ext.ux.Rixo.form.field.GridPicker", getLoadOptions: function (b) {
    var a = this.queryFilter;
    if (a) {
        a.disabled = false;
        a.setValue(this.enableRegEx ? new RegExp(b) : b);
        return{filters: [a]}
    }
}, loadPage: function (a) {
    this.store.loadPage(a, this.getLoadOptions())
}, doQuery: function (g, c, e) {
    g = g || "";
    var d = this, b = {query: g, forceAll: c, combo: d, cancel: false}, a = d.store, f = d.queryMode === "local";
    if (d.fireEvent("beforequery", b) === false || b.cancel) {
        return false
    }
    g = b.query;
    c = b.forceAll;
    if (c || (g.length >= d.minChars)) {
        d.expand();
        if (!d.queryCaching || d.lastQuery !== g) {
            d.lastQuery = g;
            if (f) {
                if (d.queryFilter) {
                    if (g || !c) {
                        d.queryFilter.disabled = false;
                        d.queryFilter.setValue(d.enableRegEx ? new RegExp(g) : g)
                    } else {
                        d.queryFilter.disabled = true
                    }
                    a.filter()
                }
            } else {
                d.rawQuery = e;
                if (d.pageSize) {
                    d.loadPage(1)
                } else {
                    a.load(this.getLoadOptions(g))
                }
            }
        }
        if (d.getRawValue() !== d.getDisplayValue()) {
            d.ignoreSelection++;
            d.picker.getSelectionModel().deselectAll();
            d.ignoreSelection--
        }
        if (f) {
            d.doAutoSelect()
        }
        if (d.typeAhead) {
            d.doTypeAhead()
        }
    }
    return true
}});