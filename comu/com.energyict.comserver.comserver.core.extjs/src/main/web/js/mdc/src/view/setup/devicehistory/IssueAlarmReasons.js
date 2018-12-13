/**
 * Created by H251853 on 9/4/2017.
 */
Ext.define('Mdc.view.setup.devicehistory.IssueAlarmReasons', {
    extend: 'Uni.grid.filtertop.ComboBox',
    xtype: 'issue-alarm-reasons',


    tpl: new Ext.create('Ext.XTemplate',
        '<tpl for=".">',
        '<tpl for="issueType" if="this.shouldShowHeader(issueType)"><div style="padding: 4px; font-weight: bold; border-bottom: 1px solid #ddd;" class="group-header">{[this.showHeader(values.issueType)]}</div></tpl>',
        '<div class="x-boundlist-item">',
        '<div class="x-combo-list-item">',
        '<img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" style="top: 2px; left: -2px; position: relative;"/>{name}</div></div>',
        '</tpl>', {
            shouldShowHeader: function (issueType) {
                return this.currentGroup !== issueType;
            },
            showHeader: function (issueType) {
                this.currentGroup = issueType;
                return issueType;
            }
        })


});






