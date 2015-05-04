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
                '<p><b>{author.name}</b> added a comment - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',
                '<p><tpl for="splittedComments">',
                '{.}</br>',
                '</tpl></p>',
                '</tpl>',
                {
                    formatCreationDate: function (date) {
                        date = Ext.isDate(date) ? date : new Date(date);
                        return Uni.DateTime.formatDateTimeLong(date);
                    }
                }
            )
        }
    ]
});

