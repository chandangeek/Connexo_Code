/**
 * Created by H251853 on 9/4/2017.
 */
Ext.define('Mdc.view.setup.devicehistory.IssueAlarmReasons', {
    extend: 'Uni.grid.filtertop.ComboBox',
    xtype: 'issue-alarm-reasons',

    tpl: new Ext.create('Ext.XTemplate',
        '<tpl for=".">',
        '<tpl for="group" if="this.shouldShowHeader(group)"><div style="padding: 4px; font-weight: bold; border-bottom: 1px solid #ddd;" class="group-header">{[this.showHeader(values.group)]}</div></tpl>',
        '<div class="x-boundlist-item" ><img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" style="top: 2px; left: -2px; position: relative;"/>{name}</div>',
        '</tpl>', {
            shouldShowHeader: function (group) {
                return this.currentGroup !== group;
            },
            showHeader: function (group) {
                this.currentGroup = group;
                return group;
            }
        }),

    store: new Ext.create('Ext.data.Store', {
        fields: ['group', 'key', 'name'],
        data: [{
            group: 'Alarms',
            key: '2',
            name: 'Reason 2'
        }, {
            group: 'Alarms',
            key: '3',
            name: 'Reason 3'
        }, {
            group: 'Issues',
            key: '0',
            name: 'Reason 0'
        }, {
            group: 'Issues',
            key: '1',
            name: 'Reason 1'
        }]
    })
});