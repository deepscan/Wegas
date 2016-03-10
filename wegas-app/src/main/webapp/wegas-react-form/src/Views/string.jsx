import React, { PropTypes } from 'react';
import TextField from 'material-ui/lib/text-field';

function StringView(props) {
    const errorMessage = props.errorMessage && props.errorMessage.length ?
        props.errorMessage :
        undefined;
    const onChange = function onChange(event) {
        props.onChange(event.target.value);
    };
    return (<TextField className={ props.view.className }
                       defaultValue={ props.value }
                       floatingLabelText={ props.view.label || props.path[props.path.length - 1] }
                       errorText={ errorMessage }
                       onChange={ onChange }
                       disabled={ props.disabled }
                       multiLine={ props.multiLine }
                       fullWidth />);
}

StringView.propTypes = {
    errorMessage: PropTypes.arrayOf(PropTypes.string),
    onChange: PropTypes.func.isRequired,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    view: PropTypes.shape({
        label: PropTypes.string,
        className: PropTypes.string
    }),
    path: PropTypes.arrayOf(PropTypes.string),
    disabled: PropTypes.bool,
    multiLine: PropTypes.bool
};

export default StringView;
