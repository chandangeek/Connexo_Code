Ext.define('Imt.purpose.view.ScheduleField', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.schedule-field',
    requires: [
        'Ext.ProgressBar',
        'Uni.util.FormInfoMessage'
    ],
    labelAlign: 'top',
    buttonText: Uni.I18n.translate('general.viewSchedule', 'IMT', 'View schedule'),
    progressbarConfig: {
        xtype: 'progressbar',
        itemId: 'progressbar',
        width: '50%'
    },
    emptyText: '',
    store: null,
    route: null,

    initComponent: function () {
        var me = this;

        me.store = Ext.getStore(me.store);
        me.items = {
            xtype: 'button',
            itemId: 'view-button',
            text: me.buttonText,
            listeners: {
                click: me.onViewButtonClick,
                scope: me
            }
        };

        me.callParent(arguments);
    },

    onViewButtonClick: function () {
        var me = this,
            progressbar;

        Ext.suspendLayouts();
        me.removeAll();
        progressbar = me.add(me.progressbarConfig);
        progressbar.wait({
            interval: 50,
            increment: 20
        });
        Ext.resumeLayouts(true);
        me.store.load({
            scope: me,
            callback: me.onStoreLoad
        });
    },

    onStoreLoad: function (records) {
        var me = this;

        Ext.suspendLayouts();
        me.removeAll();
        if (Ext.isEmpty(records)) {
            me.add({
                xtype: 'uni-form-info-message',
                itemId: 'empty-message',
                text: me.emptyText
            });
        } else {
            me.add({
                xtype: 'component',
                itemId: 'data-view',
                data: records,
                tpl: new Ext.XTemplate(
                    '<table>',
                    '<tpl for=".">',
                    '<tr>',
                    '<td>',
                    '<a href="{[ this.getUrl(values) ]}">{[ values.get("name") ]}</a>',
                    '</td>',
                    '<td>{[ this.getRecurrence(values) ]}</td>',
                    '</tr>',
                    '</tpl>',
                    '</table>',
                    {
                        disableFormats: true,
                        getUrl: function (task) {
                            return me.route.buildUrl({
                                taskId: task.get('id')
                            });
                        },
                        getRecurrence: function (task) {
                            return task.getTriggerText ? task.getTriggerText() : task.get('recurrence');
                        }
                    }
                )
            });
        }
        Ext.resumeLayouts(true);
    }
});