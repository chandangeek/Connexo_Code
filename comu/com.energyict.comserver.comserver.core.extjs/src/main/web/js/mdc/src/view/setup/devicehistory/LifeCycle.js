Ext.define('Mdc.view.setup.devicehistory.LifeCycle', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-history-life-cycle-panel',
    margin: '20 0 0 20',
    items: [
        {
            xtype: 'dataview',
            itemId: 'life-cycle-data-view',
            tpl: new Ext.XTemplate(
                '<tpl for=".">',
                '{[xindex > 1 ? "<hr>" : ""]}',
                '<p><b>{[Uni.I18n.translate("searchItems.bulk.state", "MDC", "State") + " " + (values.fromState ? Uni.I18n.translate("deviceHistory.changedFrom", "MDC", "changed from") + " " + values.fromState : Uni.I18n.translate("deviceHistory.set", "MDC", "set")) + " " + Uni.I18n.translate("general.unitTo", "MDC", "to") + " " + values.toState]}</b></p>',
                '<p>{[Uni.I18n.translate("deviceHistory.by", "MDC", "by") + " " + values.author.name + " " + Uni.I18n.translate("deviceHistory.on", "MDC", "on") + " " + Uni.DateTime.formatDateTimeShort(new Date(values.modTime))]}</br></p>',
                '</tpl>'
            )
        }
    ]
});

