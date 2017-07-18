//
//     React demo components KeyLines v3.3.1-2702
//
//     Copyright © 2011-2016 Cambridge Intelligence Limited.
//     All rights reserved.
//

import React from 'react';

// We use createClass instead of ES6 class syntax, as createClass automatically binds our methods to 'this': https://facebook.github.io/react/docs/reusable-components.html#no-autobinding
const RightPanel = React.createClass({
  propTypes: {
    name: React.PropTypes.string.isRequired, // this is the name of the demo
    title: React.PropTypes.string.isRequired, // this is the title of the demo
    description: React.PropTypes.string.isRequired, // this is the short description of the demo
  },
  render () {
    // determine if we have a fullscreen function available to show or not the fsBtn
    const fullscreenClass = typeof doFullScreen == 'function' ? '' : 'hide';
    return (
      <div id="rhs" className='span4'>
        <div id="rhscontent" className="citext">
          <ul id="toggleTab" className={`nav nav-tabs vertical-tabs fshide ${fullscreenClass}`}>
            <li className="active"><a id="toggleRight"><i id="toggleIcon" className="icon-chevron-down"></i> </a></li>
          </ul>
          <div style={{ minHeight: "580px", height: "580px" }} className="tab-content">
            <div className="cipad">
              <legend style={{ overflow: "hidden" }}>
                <div className="pull-right">
                  <a href="#" data-toggle="tooltip" style={{ display: "none" }} id="webglButton" className="btn btn-kl btn-small btn-spaced versionmode">Try WebGL</a>
                  <a href="#" data-toggle="tooltip" id="fullscreenDisabled" className={`btn btn-small btn-spaced disabled fshide ${fullscreenClass}`}>
                    <i className="icon-resize-full"></i>
                  </a>
                  <a href="#" data-toggle="tooltip" id="fullscreenButton" className={`btn btn-small btn-spaced ${fullscreenClass}`}>
                    <i className="icon-resize-full"></i>
                  </a>
                </div>
                <div>{this.props.title}</div>
              </legend>
              <p>{this.props.description}</p>
            </div>
            <div className="cicontent cipad">
              <img className='kl-center' src={'images/' + this.props.name + '/reactjs.png'} width='75%' />
              {this.props.children}
            </div>
          </div>
        </div>
      </div>
    );
  }
});

// We use createClass instead of the native ES6 class because it automatically binds 'this': https://facebook.github.io/react/blog/2015/01/27/react-v0.13.0-beta-1.html#autobinding
const LeftPanel = React.createClass({
  render () {
    return (
      <div id='lhs' className='span8'>
        <div id='fullscreen'>
          <div id='keyline' className='cicontainer'>
            {this.props.children}
          </div>
        </div>
      </div>
    );
  }
});

export {LeftPanel, RightPanel};
