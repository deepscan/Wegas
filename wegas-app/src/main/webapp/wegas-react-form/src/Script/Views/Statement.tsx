import { css } from 'glamor';
import React from 'react';

const style = css({
    display: 'inline-block',
    paddingBottom: 5,
    width: 'calc(100% - 22px)',
});
export default function Statement(props: {
    children: (React.ComponentClass<any> | React.SFC<any>)[],
}) {
    return (
        <div className={style.toString()}>
            {props.children}
        </div>
    );
}
