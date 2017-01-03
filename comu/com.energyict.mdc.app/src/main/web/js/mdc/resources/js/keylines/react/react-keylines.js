//
//     React components KeyLines v3.3.1-2702
//
//     Copyright © 2011-2016 Cambridge Intelligence Limited.
//     All rights reserved.
//

import React from 'react';

/*
  This is the lowest level wrapper of the KeyLines integration
  - deals with loading of the KeyLines component, options, resizing and raising keylines events up
*/

function invoke (fn, ...args) {
  if (typeof fn === 'function') {
    return fn(...args);
  }
}

function createKeyLinesComponent(type, onLoadFn, onResizeFn) {
  const KlComponent = class extends React.Component {
    
    constructor(props) {
      super(props);
      // save the type of KeyLines component
      this.type = type;
      // save the onLoad function
      this.onLoad = onLoadFn;
      // save the onResize function
      this.onResize = onResizeFn;
      // bind our class specific functions to this
      // See: https://facebook.github.io/react/docs/react-without-es6.html#autobinding
      ['applyProps', 'setSelection', 'onEvent', 'onLoad', 'onResize'].forEach((prop) => {
        this[prop] = this[prop].bind(this);
      });
    }
    
    componentDidMount() {
      // create a definition of KeyLines component
      const def = {
        id: this.props.id,
        type: this.type,
        options: this.props.options 
      };
      
      KeyLines.create(def, (err, loaded) => {
        this.component = loaded;
        this.onResize();
        this.component.bind('all', this.onEvent);
        this.applyProps(this.props, () => {
          // finally, tell the parent about the component so it can call functions on it
          invoke(this.props.ready, this.component);
        });
      });
    }
    
    componentWillUnmount() {
      // cleanup the component
      this.component.unbind('all', this.onEvent);
    }
    
    setSelection(nextProps) {
      if (this.props.selection || nextProps.selection) {
        if (this.props.selection !== nextProps.selection) {
          // this works because the selectionchange is not
          // raised when changing selection programmatically
          let selectedItems = this.component.selection(nextProps.selection);
          if (this.type === 'chart' && selectedItems.length > 0) {
            this.component.zoom('selection', {animate: true, time: 250});
          }
        }
      }
    }
    
    componentWillReceiveProps(nextProps) {
      // because the caller might be using display:none to hide this component - in
      // which case the sizes of the component might be wrong (100,100) - we unfortunately
      // need to call setSize at this point
      this.onResize();

      if (this.component) {
        // we also need to intercept the options being set and pass them on to the component manually
        if (nextProps.options && this.props.options !== nextProps.options) {
          this.component.options(nextProps.options);  // don't worry about callback here
        }
        
        const reload = this.props.data !== nextProps.data;
        if (reload) {
          this.applyProps(nextProps);
        } else {
          this.setSelection(nextProps);
        }
      }
    }
    
    // this looks for a handler with the right name on the props, and if it finds
    // one, it will call it with the event arguments.
    onEvent(...args) {
      const name = args[0];
      if (name !== 'redraw') {
        return invoke(this.props[name], ...args.splice(1));
      }
    }
    
    // this applies all the component related props except the options which are
    // handled differently
    applyProps(props, cb) {
      if (this.component) {
        this.component.load(props.data, () => {
          // the next call needs to be careful with undefined:
          // passing undefined doesn't call the callback
          this.onLoad({ animate: !!this.props.animateOnLoad }, () => {
            this.component.selection(this.props.selection);
            invoke(cb);
          });
        });
      }
    }
    
    render() {
      return (
        <div ref="container" className={this.props.containerClassName}>
          <div ref="klComponent" id={this.props.id} style={this.props.style}></div>
          {this.props.children}
        </div>
      );
    }
  };
  
  // defaultProps has to be a static property of the component class
  KlComponent.defaultProps = {
    data: {},
    animateOnLoad: false,
    options: {},
    selection: []
  };
  
  // propTypes has to be a static property of the component class
  KlComponent.propTypes = {
    id: React.PropTypes.string.isRequired, // this is the DOM ID of the KeyLines component element
    data: React.PropTypes.object,      // this will be the component format
    animateOnLoad: React.PropTypes.bool,  // this will set the animation flag of the layout on load
    options: React.PropTypes.object,      // component options
    selection: React.PropTypes.array,  // simple array of ids
    ready: React.PropTypes.func,     // called when the component is fully loaded
    style: React.PropTypes.object, // the component div style
    containerClassName: React.PropTypes.string, // the component container CSS class name
    /*
      NOTE: you can add props at the level above this - e.g., 'click'
            i.e.,   <Chart click={this.clickHandler}/>
    */
  };
  
  return KlComponent;
};

function createChartComponent() {
  
  function onLoad(options, callback) {
    // default behaviour when loading data in the chart
    this.component.layout('standard', options, callback);
  }
  
  function onResize() {
    // find containing dimensions
    const w = this.refs.klComponent.offsetWidth;
    const h = this.refs.klComponent.offsetHeight;
    if (this.component) {
      const { width, height } = this.component.viewOptions();
      if (((width !== w) || (height !== h)) && (w > 0) && (h > 0)) {
        KeyLines.setSize(this.props.id, w, h);
        this.component.zoom('fit');
      }
    }
  }
  
  return createKeyLinesComponent('chart', onLoad, onResize);
}

// Define the Chart component
export const Chart = createChartComponent();

function createTimebarComponent() {
  
  function onLoad(options, callback) {  
    // default behaviour when loading data in the timebar
    this.component.zoom('fit', options, callback);
  }
  
  function onResize() {
    // find containing dimensions
    const w = this.refs.klComponent.offsetWidth;
    const h = this.refs.klComponent.offsetHeight;
    if (this.component && w > 0 && h > 0) {
      KeyLines.setSize(this.props.id, w, h);
    }
  }
  
  return createKeyLinesComponent('timebar', onLoad, onResize);
}

// Define the Timebar component
export const Timebar = createTimebarComponent();
export default Chart;
