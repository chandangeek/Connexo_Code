Ext.define('Mdc.view.setup.devicehistory.LifeCycle', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-history-life-cycle-panel',
    margin: '20 0 0 20',
    items: [
        {
            xtype: 'dataview',
            width: 600,
            itemId: 'life-cycle-data-view',
            itemSelector: 'div.thumb-wrap',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '{[xindex > 1 ? "<hr>" : ""]}',
                '<p><b>{[(values.type == "lifeCycle" ? Uni.I18n.translate("general.deviceLifeCycle", "MDC", "Device life cycle") : Uni.I18n.translate("searchItems.bulk.state", "MDC", "State")) + " " + (values.from ? Uni.I18n.translate("deviceHistory.changedFrom", "MDC", "changed from") + " " + this.formatHref(values, true) : Uni.I18n.translate("deviceHistory.set", "MDC", "set")) + " " + Uni.I18n.translate("general.unitTo", "MDC", "to") + " " + this.formatHref(values, false)]}</b></p>',
                '<p>{[Uni.I18n.translate("deviceHistory.byOn", "MDC", "by {0} on {1}", [values.author.name, Uni.DateTime.formatDateTimeShort(new Date(values.modTime))])]}</br></p>',
                '</tpl>',
                {
                    formatHref: function (values, isFrom) {
                        var id = isFrom ? values.from.id : values.to.id,
                            value = isFrom ? values.from.name : values.to.name;
                        return values.type == 'lifeCycle' ? '<a href="#/administration/devicelifecycles/' + id + '">' + value + '</a>' : value;
                    }
                }
            )
        }
    ]
});

