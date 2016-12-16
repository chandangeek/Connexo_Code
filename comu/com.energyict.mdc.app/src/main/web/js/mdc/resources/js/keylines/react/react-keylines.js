//
//     React components KeyLines v3.2.2-2615
//
//     Copyright © 2011-2016 Cambridge Intelligence Limited.
//     All rights reserved.
//

import React from 'react';

/*
  This is the lowest level wrapper of the chart integration
  - deals with loading of the chart, chart options, resizing and raising keylines events up
*/

function invoke (fn, ...args) {
  if (fn && ({}).toString.call(fn) === '[object Function]') {
    return fn(...args);
  }
}

export const Chart = React.createClass({
  propTypes: {
    id: React.PropTypes.string.isRequired, // this is the DOM ID of the KeyLines chart element
    data: React.PropTypes.object,      // this will be the chart format
    animateOnLoad: React.PropTypes.bool,  // this will set the animation flag of the layout on load
    options: React.PropTypes.object,      // chart options
    selection: React.PropTypes.array,  // simple array of ids
    ready: React.PropTypes.func,     // called when the chart is fully loaded
    containerStyle: React.PropTypes.object, // the chart container div style
    containerClassName: React.PropTypes.string, // the chart container CSS class name
    /*
      NOTE: you can add props at the level above this - e.g., 'click'
            i.e.,   <Chart click={this.clickHandler}/>
    */
  },
  componentDidMount() {
    KeyLines.paths();
    KeyLines.create({
      id: this.props.id,
      type: 'chart',
      options: this.props.options }, (err, loaded) => {
      this.chart = loaded;
      this.onResize();
      this.chart.bind('all', this.onEvent);
      this.applyProps(this.props);
      // finally, tell the parent about the chart so it can call functions on it
      invoke(this.props.ready, loaded);
    });
  },
  setSelection(nextProps) {
    if (this.props.selection || nextProps.selection) {
      if (this.props.selection !== nextProps.selection) {
        // this works because the selectionchange is not
        // raised when changing selection programmatically
        let selectedItems = this.chart.selection(nextProps.selection);
        if (selectedItems.length > 0) {
          this.chart.zoom('selection', {animate: true, time: 250});
        }
      }
    }
  },
  componentWillReceiveProps(nextProps) {
    // because the caller might be using display:none to hide this component - in
    // which case the sizes of the component might be wrong (100,100) - we unfortunately
    // need to call setSize at this point
    this.onResize();

    // we also need to intercept the options being set and pass them on to the chart manually
    if (this.chart && nextProps.options) {
      this.chart.options(nextProps.options);  // don't worry about callback here
    }
    const reload = this.props.data !== nextProps.data;
    if (reload) {
      this.applyProps(nextProps);
    }
    else {
      this.setSelection(nextProps);
    }
  },
  // this looks for a handler with the right name on the props, and if it finds
  // one, it will call it with the event arguments.
  onEvent(...args) {
    const name = args[0];
    if (name !== 'redraw') {
      return invoke(this.props[name], ...args.splice(1));
    }
  },
  onResize() {
    // find containing dimensions
    const w = this.refs.container.offsetWidth;
    const h = this.refs.container.offsetHeight;
    if (this.chart) {
      const { width, height } = this.chart.viewOptions();
      if (((width !== w) || (height !== h)) && (w > 0) && (h > 0)) {
        KeyLines.setSize(this.props.id, w, h);
        this.chart.zoom('fit');
      }
    }
  },
  // this applies all the chart related props except the options which are
  // handled differently
  applyProps(props, cb) {
    if (this.chart) {
      this.chart.load(props.data, () => {
        // the next call needs to be careful with undefined:
        //  passing undefined doesn't call the callback
        this.chart.layout('standard', { animate: this.props.animateOnLoad }, () => {
          this.chart.selection(props.selection);
          invoke(cb);
        });
      });
    }
  },
  render() {
    return (
      <div className={this.props.containerClassName} ref="container" style={this.props.containerStyle}>
        <div id={this.props.id}></div>
        {this.props.children}
      </div>
    );
  },
});
