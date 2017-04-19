import PropTypes from 'prop-types';
import React from 'react';
import { parse, print, types } from 'recast';
import ViewSrc from './Views/ViewSrc';
/**
 * HOC Parse code into AST and reverse onChange
 * @param {React.Component} Comp Component to augment
 */
function parsed(Comp) {
    /**
     * @param {{value:string, onChange:(code:string)=>void}} props Component props
     */
    function Parsed(props) {
        const { value, onChange, ...restProps } = props;
        let error = '';
        let ast;
        try {
            ast = parse(value);
        } catch (e) {
            error = e.description;
            // should show code string instead of falling back to an empty program
            ast = types.builders.file(types.builders.program([]));
        }
        return (
            <ViewSrc value={value} onChange={onChange} error={error}>
                <Comp
                    {...restProps}
                    code={ast.program.body}
                    onChange={v => {
                        ast.program.body = v;
                        onChange(print(ast).code);
                    }}
                />
            </ViewSrc>
        );
    }
    Parsed.propTypes = {
        value: PropTypes.string.isRequired,
        onChange: PropTypes.func
    };
    Parsed.defaultProps = {
        value: ''
    };
    return Parsed;
}
export default parsed;
