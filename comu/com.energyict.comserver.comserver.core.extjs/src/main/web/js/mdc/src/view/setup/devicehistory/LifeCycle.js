Ext.define('Mdc.view.setup.devicehistory.LifeCycle', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-history-life-cycle-panel',
    margin: '20 0 0 20',
    title: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
    ui: 'small',
    items: [
        {
            xtype: 'dataview',
            width: 600,
            itemId: 'life-cycle-data-view',
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '{[xindex > 1 ? "<hr>" : ""]}',
                '<p><b>{[(values.type == "lifeCycle" && values.from ' +
                    '? Uni.I18n.translate("deviceHistory.deviceLifeCycle.changed", "MDC", "Device life cycle changed from {0} to {1}",[this.formatHref(values, true), this.formatHref(values, false)], false) ' +
                    ': values.type == "lifeCycle" ' +
                        '? Uni.I18n.translate("deviceHistory.deviceLifeCycle.set", "MDC", "Device life cycle set to {0}", [this.formatHref(values, false)], false) ' +
                        ': values.from ' +
                            '? Uni.I18n.translate("deviceHistory.state.changed", "MDC", "State changed from {0} to {1}", [this.formatHref(values, true), this.formatHref(values, false)]) ' +
                            ': Uni.I18n.translate("deviceHistory.state.set", "MDC", "State set to {0}", [this.formatHref(values, false)]) )]}</b></p>',
                '<p>{[Uni.I18n.translate("deviceHistory.byOn", "MDC", "by {0} on {1}", [values.author.name, Uni.DateTime.formatDateTimeShort(new Date(values.modTime))])]}</br></p>',
                '</tpl>',
                {
                    formatHref: function (values, isFrom) {
                        var id = isFrom ? values.from.id : values.to.id,
                            value = isFrom ? values.from.name : values.to.name;
                        return values.type == 'lifeCycle' ? '<a href="#/administration/devicelifecycles/' + id + '">' + Ext.String.htmlEncode(value) + '</a>' : Ext.String.htmlEncode(value);
                    }
                }
            )
        }
    ]
});

