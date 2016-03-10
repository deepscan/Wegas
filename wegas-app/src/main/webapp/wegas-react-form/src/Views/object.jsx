import React, { PropTypes } from 'react';


function ObjectView(props) {
    const style = {
        backgroundColor: 'rgba(0,0,0,0.05)',
        paddingLeft: '30px',
        borderTop: props.view.label ? '1px solid lightgrey' : 'none'
    };
    const legendStyle = {
        textAlign: 'center'
    };
    return (<fieldset className={ props.view.className }
                      style={ style }>
              <legend style={ legendStyle }>
                { props.view.label || props.editKey }
              </legend>
              { props.children }
            </fieldset>);
}

ObjectView.propTypes = {
    children: PropTypes.node,
    view: PropTypes.shape({
        className: PropTypes.string,
        label: PropTypes.string
    })
};
export default ObjectView;
