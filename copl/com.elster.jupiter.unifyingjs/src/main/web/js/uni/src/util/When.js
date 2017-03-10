/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.When', {
    success: null,
    failure: null,
    callback: null,
    toExecute: [],
    context: [],
    args: [],
    results: [],
    simple: [],
    count: null,
    failed: false,


    constructor: function () {
        var me = this;
        this.init();
        me.callParent(arguments);
    },

    init: function(){
        this.success = null;
        this.failure = null;
        this.callback = null;
        this.toExecute = [];
        this.context = [];
        this.args = [];
        this.results = [];
        this.simple = [];
        this.count = null;
        this.failed = false;
    },

    when: function (functionsToExecute) {
        this.init();
        for (var i in functionsToExecute) {
            this.toExecute.push(functionsToExecute[i].action);
            this.context.push(functionsToExecute[i].context);
            this.args.push(functionsToExecute[i].args);
            this.simple.push(functionsToExecute[i].simple);
            this.count++;
        }
        return this;
    },
    then: function (callBackObject) {
        this.success = callBackObject.success;
        this.failure = callBackObject.failure;
        this.callback = callBackObject.callback;

        var me = this;

        for (var i in this.toExecute) {

            var args = [];
            var makeSuccessCallBack = function (i) {
                return function () {
                    me.count--;
                    me.results[i] = arguments;
                    if (me.count === 0) {
                        if (me.failed === false) {
                            me.resolveSuccess(me.success);
                        } else {
                            me.resolveFailure(me.failure);
                        }
                    }
                };
            };


            var makeFailureCallBack = function (i) {
                return function () {
                    me.count--;
                    me.results[i] = arguments;
                    me.failed = true;
                    if (me.count === 0) {
                        me.resolveFailure(me.failure);
                    }
                };
            };

            var makeGeneralCallBack = function (i) {
                return function () {
                    me.count--;
                    me.results[i] = arguments;
                    if (me.count === 0) {
                        if (me.failed === false) {
                            me.resolveSuccess(me.callback);
                        } else {
                            me.resolveFailure(me.callback);
                        }
                    }
                };
            };

            if (typeof this.args[i] != 'undefined') {
                args = this.args[i];
            }


            if (typeof this.callback === 'undefined') {
                if (this.simple[i] === false) {
                    args.push({success: makeSuccessCallBack(i), failure: makeFailureCallBack(i)});
                } else {
                    args.push({callback: makeSuccessCallBack(i)});
                }
            } else {
                if (this.simple[i] === false) {
                    args.push({success: makeGeneralCallBack(i), failure: makeGeneralCallBack(i)});
                } else {
                    args.push({callback: makeGeneralCallBack(i)});
                }
            }
            this.toExecute[i].apply(this.context[i], args);
        }
    },

    resolveSuccess: function (successCallBack) {
        successCallBack(this.results);
    },

    resolveFailure: function (failureCallBack) {
        failureCallBack();

    }
});
