# Form validation

## Introduction

This document describes how to do back-end validation with extjs forms. We use back-end validation to have a full validation, with all errors for all fields off the form in one call to the backend.
Extjs forms automatically handle errors if they are send to the form in the right format.


## How to use

When doing a request to the backend and you backend validation fails, have your backend send a response with:

http status code: 400 (important that it is 400, because that is the value that will be used by the frontend to determine that is is a form validation)

content:
    {
        success: false
        errors: [{id:storeTaskQueueSize, msg:{MDC.ValueNotInRange}},…]
    }

errors is an array that contains objects that haven an 'id' property which is the name of your form field, and a 'msg' property which is the message that needs to be shown under the form field.

now in your controller do something like this:

    this.comserver.save({
                success: function (record) {
                    me.getComServersStore().reload(
                        {
                            callback: function () {
                                me.showComServerPreview();
                            }
                        });
                },
                failure: function(record,operation){
                    //get the response from the operation (careful: this only works when using unifyingjs, as we needed to override server.js for this)
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        //send the errors to the form, the extjs form will handle them for you
                        me.comServerEditView.down('form').getForm().markInvalid(json.errors);
                    }
                }
            });