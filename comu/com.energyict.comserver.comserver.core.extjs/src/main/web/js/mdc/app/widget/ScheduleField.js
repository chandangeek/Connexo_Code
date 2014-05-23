Ext.define('Mdc.widget.ScheduleField', {
    extend: 'Ext.form.FieldContainer',
    requires: [
        'Mdc.store.TimeUnits'
    ],
    stores: [
        'TimeUnits'
    ],
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.scheduleField',
    layout: {
        type: 'hbox'
    },
    msgTarget: 'under',
    submitFormat: 'c',
    flex: 1,

    valueCfg: null,
    unitCfg: null,
    hourCfg:null,
    minuteCfg:null,
    secondCfg:null,

    initComponent: function () {
        var me = this;
        if (!me.valueCfg) {
            me.valueCfg = {};
        }
        if (!me.unitCfg) {
            me.unitCfg = {};
        }
        if (!me.hourCfg) {
            me.hourCfg = {};
        }
        if (!me.minuteCfg) {
            me.minuteCfg = {};
        }
        if (!me.secondCfg) {
            me.secondCfg = {};
        }

        me.buildField();
        me.callParent();
        me.valueField = me.down('#valueField');
        me.unitField = me.down('#unitField');
        me.hourField = me.down('#hourField');
        me.minuteField = me.down('#minuteField');
        me.secondField = me.down('#secondField');
        me.dayField = me.down('#dayField');
        me.dayIndexField = me.down('#dayIndexField');
        me.offSetSeparator = me.down('#offSetSeparator');
        me.secondLabel = me.down('#secondLabel');
        me.minuteLabel = me.down('#minuteLabel');
        me.dayFieldSeparator = me.down('#dayFieldSeparator');
        me.initField();
    },

    //@private
    buildField: function () {
        var timeUnits = Ext.create('Mdc.store.TimeUnits');
        var me = this;
        me.items = [
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'valueField',
                submitValue: false,
                width: 70,
                minValue: 0,
                margin: '0 5 0 0'
            }, me.valueCfg),
            Ext.apply({
                xtype: 'combobox',
                itemId : 'unitField',
                store: new Ext.data.SimpleStore({
                    data : [
                        ['minutes',Uni.I18n.translate('schedulefield.minutes', 'UNI', 'minute(s)')],
                        ['hours',Uni.I18n.translate('schedulefield.hours', 'UNI', 'hour(s)')],
                        ['days',Uni.I18n.translate('schedulefield.days', 'UNI', 'day(s)')],
                        ['weeks',Uni.I18n.translate('schedulefield.weeks', 'UNI', 'week(s)')],
                        ['months',Uni.I18n.translate('schedulefield.months', 'UNI', 'month(s)')]
                    ],
                    id : 0,
                    fields : ['timeUnitKey','translation']
                }),
                queryMode: 'local',
                displayField: 'translation',
                valueField: 'timeUnitKey',
                submitValue: false,
                listeners: {
                    change: {
                        fn: me.adjustOffsetGui,
                        scope: me
                    }
                },
                width: 100,
                margin: '0 5 0 0'
            }, me.unitCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'offSetSeparator',
                submitValue: false,
                hidden: true,
                msgTarget: 'none',
                margin: '0 5 0 0'
            }),
            Ext.apply({
                xtype: 'combobox',
                itemId : 'dayField',
                store: new Ext.data.SimpleStore({
                    data : [
                        ['monday',Uni.I18n.translate('schedulefield.monday', 'UNI', 'monday')],
                        ['tuesday',Uni.I18n.translate('schedulefield.tuesday', 'UNI', 'tuesday')],
                        ['wednesday',Uni.I18n.translate('schedulefield.wednesday', 'UNI', 'wednesday')],
                        ['thursday',Uni.I18n.translate('schedulefield.thursday', 'UNI', 'thursday')],
                        ['friday',Uni.I18n.translate('schedulefield.friday', 'UNI', 'friday')],
                        ['saturday',Uni.I18n.translate('schedulefield.saturday', 'UNI', 'saturday')],
                        ['sunday',Uni.I18n.translate('schedulefield.sunday', 'UNI', 'sunday')]
                    ],
                    id : 0,
                    fields : ['dayKey','translation']
                }),
                queryMode: 'local',
                displayField: 'translation',
                valueField: 'dayKey',
                submitValue: false,
                hidden: true,
                margin: '0 5 0 0'
            }, me.unitCfg),
            Ext.apply({
                xtype: 'combobox',
                itemId : 'dayIndexField',
                store:  new Ext.data.SimpleStore({
                    data : [
                        [1,1],[2,2],[3,3],[4,4],[5,5],[6,6],[7,7],[8,8],[9,9],[10,10],
                        [11,11],[12,12],[13,13],[14,14],[15,15],[16,16],[17,17],[18,18],[19,19],[20,20],
                        [21,21],[22,22],[23,23],[24,24],[25,25],[26,26],[27,27],[28,28],
                        ['last',Uni.I18n.translate('schedulefield.last', 'UNI', 'last')]
                    ],
                    id : 0,
                    fields : ['dayIndexKey','translation']
                }),
                queryMode: 'local',
                displayField: 'translation',
                valueField: 'dayIndexKey',
                submitValue: false,
                hidden: true,
                width: 70,
                margin: '0 5 0 0'
            }, me.unitCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'dayFieldSeparator',
                submitValue: false,
                hidden: true,
                msgTarget: 'none',
                margin: '0 5 0 0'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'hourField',
                maxValue: 24,
                minValue: 0,
                submitValue: false,
                hidden: true,
                margin: '0 5 0 0'
            }, me.hourCfg),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'minuteField',
                maxValue: 60,
                minValue: 0,
                submitValue: false,
                hidden: true,
                margin: '0 5 0 0'
            }, me.minuteCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'minuteLabel',
                submitValue: false,
                hidden: true,
                value: 'minute(s)',
                msgTarget: 'none',
                margin: '0 5 0 0'
            }),
            Ext.apply({
                xtype: 'numberfield',
                itemId: 'secondField',
                maxValue: 60,
                minValue: 0,
                submitValue: false,
                hidden: true
            }, me.secondCfg),
            Ext.apply({
                xtype: 'displayfield',
                itemId: 'secondLabel',
                submitValue: false,
                hidden: true,
                value: 'second(s)',
                msgTarget: 'none',
                margin: '0 5 0 0'
            })

        ]
    },

    adjustOffsetGui:function(combo,newValue){
        this.hideOffsetGui();
        switch(newValue){
            case 'minutes':
                this.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'UNI', 'at'));
                this.offSetSeparator.show();
                this.secondField.show();
                this.secondLabel.show();
                break;
            case 'hours':
                this.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'UNI', 'at'));
                this.offSetSeparator.show();
                this.minuteField.show();
                this.minuteLabel.show();
                this.secondField.show();
                this.secondLabel.show();
                break;
            case 'days':
                this.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'UNI', 'at'));
                this.offSetSeparator.show();
                this.hourField.show();
                this.minuteField.show();
                this.secondField.show();
                break;
            case 'weeks':
                this.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.on', 'UNI', 'on'));
                this.offSetSeparator.show();
                this.dayField.show();
                this.dayFieldSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'UNI', 'at'));
                this.dayFieldSeparator.show();
                this.hourField.show();
                this.minuteField.show();
                this.secondField.show();
                break;
            case 'months':
                this.offSetSeparator.setValue(Uni.I18n.translate('schedulefield.onDay', 'UNI', 'on day'));
                this.offSetSeparator.show();
                this.dayIndexField.show();
                this.dayFieldSeparator.setValue(Uni.I18n.translate('schedulefield.at', 'UNI', 'at'));
                this.dayFieldSeparator.show();
                this.hourField.show();
                this.minuteField.show();
                this.secondField.show();
        }



    },

    clear: function(){
        this.hideOffsetGui();
    },

    hideOffsetGui: function(){
        this.hourField.hide();
        this.hourField.setRawValue(0);
        this.minuteField.hide();
        this.minuteField.setRawValue(0);
        this.secondField.hide();
        this.secondField.setRawValue(0);
        this.dayField.hide();
        this.dayField.setRawValue('');
        this.dayIndexField.hide();
        this.dayIndexField.setRawValue(0);
        this.offSetSeparator.hide();
        this.secondLabel.hide();
        this.minuteLabel.hide();
        this.dayFieldSeparator.hide();
    },

    getValue: function () {
        var offSet = {timeUnit: 'seconds',count:0};

        var everyValue = parseInt(this.valueField.getSubmitValue()) || 0;
        var timeUnit = this.unitField.getSubmitValue() || 'seconds';

        switch(timeUnit){
            case 'minutes':
                offSet.count = this.secondField.getValue();
                break;
            case 'hours':
                offSet.count =  this.minuteField.getValue() * 60 +  this.secondField.getValue();
                break;
            case 'days':
                offSet.count =  this.hourField.getValue() * 3600 + this.minuteField.getValue() * 60 +  this.secondField.getValue();
                break;
            case 'weeks':
                break;
            case 'months':
                var day = this.dayIndexField.getSubmitValue()===Uni.I18n.translate('schedulefield.last', 'UNI', 'last');
                if(day){
                    offSet.count =  this.hourField.getValue() * 3600 + this.minuteField.getValue() * 60 +  this.secondField.getValue();
                } else {
                    offSet.count =  (this.dayIndexField.getValue()-1)*86400 + this.hourField.getValue() * 3600 + this.minuteField.getValue() * 60 +  this.secondField.getValue();
                }
        }


        return {
                every: {
                    count: everyValue,
                    timeUnit: timeUnit
                },
                offset: offSet,
                lastDay: day
        };
    },

    setValue: function (schedule) {
        this.hideOffsetGui();
        var me = this;
        if (schedule) {
            me.valueField.setValue(schedule.every.count);
            me.unitField.setValue(schedule.every.timeUnit);

            var offSet = schedule.offset;
            switch(schedule.every.timeUnit){
                case 'minutes':
                    this.secondField.setValue(offSet.count);
                    break;
                case 'hours':
                    this.minuteField.setValue(Math.floor(offSet.count/60));
                    this.secondField.setValue(offSet.count%60);
                    break;
                case 'days':
                    this.hourField.setValue(Math.floor(offSet.count/3600));
                    this.minuteField.setValue(Math.floor((offSet.count%3600)/60));
                    this.secondField.setValue((offSet.count%3600)%60);
                    break;
                case 'weeks':
                    break;
                case 'months':
                    if(schedule.lastDay === true){
                        this.dayIndexField.setValue(Uni.I18n.translate('schedulefield.last', 'UNI', 'last'));
                    } else {
                        this.dayIndexField.setValue(Math.floor(offSet.count/86400));
                        this.hourField.setValue(Math.floor((offSet.count%86400)/3600));
                        this.minuteField.setValue(Math.floor(((offSet.count%86400)%3600)/60));
                        this.secondField.setValue(((offSet.count%86400)%3600)%60);
                    }
                    break;
            }
        }
    },

    getSubmitData: function () {
        var me = this,
            data = null;
        if (!me.disabled && me.submitValue && !me.isFileUpload()) {
            data = {};
            value = me.getValue();
            data[me.getName()] = '' + value ? value : null;
        }
        return data;
    },

    markInvalid: function(fields){
        this.eachItem(function(field){
            field.markInvalid('');
        });
        this.items.items[0].markInvalid(fields);
    },

    eachItem: function(fn, scope) {
        if(this.items && this.items.each){
            this.items.each(fn, scope || this);
        }
    }
});
