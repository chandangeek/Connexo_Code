/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.panel.StepPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.step-panel',
    text: 'Some step text',

    indexText: '12',
    index: null,

    isLastItem: null,
    isFirstItem: null,
    isMiddleItem: null,
    isOneItem: null,

    isActiveStep: null,
    isCompletedStep: null,
    isNonCompletedStep: null,

    state: 'noncompleted',

    layout: {
        type: 'vbox',
        align: 'left'
    },

    states: {
        active: ['step-active', 'step-label-active'],
        completed: ['step-completed', 'step-label-completed'],
        noncompleted: ['step-non-completed', 'step-label-non-completed']
    },

    items: [],

    handler: function () {
    },

    getStepDots: function () {
        return {
            layout: {
                type: 'vbox',
                align: 'left'
            },
            cls: 'x-panel-step-dots',
            items: [
                {
                    xtype: 'box',
                    name: 'bottomdots',
                    cls: 'x-image-step-dots'
                }
            ]
        }
    },


    getStepLabel: function () {
        var me = this;
        return {
            name: 'step-label-side',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    xtype: 'button',
                    name: 'steppanellabel',
                    text: me.text,
                    cls: 'x-label-step',
                    ui: 'step-label-active',
                    handler: me.handler
                }
            ]
        }
    },

    getStepPanelLayout: function () {
        var me = this;
        return {
            name: 'basepanel',
            layout: {
                type: 'hbox',
                align: 'middle'
            },
            items: [
                {
                    name: 'steppanelbutton',
                    xtype: 'step-button',
                    ui: 'step-active',
                    text: me.indexText,
                    handler: me.handler
                },
                me.getStepLabel()
            ]
        }
    },

    doStepLayout: function () {
        var me = this,
            items = null;
        me.isFirstItem && (items = [me.getStepPanelLayout(), me.getStepDots()]);
        me.isLastItem && (items = [me.getStepDots(), me.getStepPanelLayout()]);
        me.isMiddleItem && (items = [me.getStepDots(), me.getStepPanelLayout(), me.getStepDots()]);
        me.isOneItem && (items = [me.getStepPanelLayout()]);
        me.items = items
    },

    afterRender: function (panel) {
        panel.stepButton = this.down('panel[name=basepanel]');
        panel.stepLabel = this.down();
        console.log( this.stepButton, this.stepLabel);
     //   this.setState(this.state);
    },

    setState: function (state) {
        !state && (this.state = state);
        console.log(this, this.stepButton, this.stepLabel);
        this.stepButton.setUI(this.states[this.state][0]);
        this.stepLabel.setUI(this.states[this.state][1]);
    },

    getState: function(){
        return this.state;
    },

    initComponent: function () {
        var me = this;
        me.doStepLayout();
        me.callParent(arguments)
    }
});
