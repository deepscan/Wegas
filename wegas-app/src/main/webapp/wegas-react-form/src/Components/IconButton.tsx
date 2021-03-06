import React from 'react';
import classNames from 'classnames';
import { css } from 'glamor';

interface Props {
    icon: string;
    onClick: () => void;
    label?: string;
    disabled?: boolean;
    opacity?: boolean;
    grey?: boolean;
    active?: boolean;
    iconColor?: string;
    tooltip?: string;
    className?: string;
    prefixedLabel?: boolean;
    labelClassName?: string;
    stackedOnIcon?: string; // For FontAwesome stacking feature, name/code of background icon
    stackedOnClassName?: string; // Idem, className of background icon
}
const shapeStyle = css({
    width: 'auto',
    margin: '3px',
    // minWidth: '16px',
    // height: '16px',
    textAlign: 'center',
    display: 'inline-block',
    cursor: 'pointer',
    color: 'gray',
    ':hover': {
        color: 'black',
    },
});

const labelStyle = css({
    marginLeft: '0.35em',
});

const disabledStyle = css({
    color: 'black',
    backgroundColor: 'darkslategrey',
});

const opacityStyle = css({
    opacity: 0,
});
const activeStyle = css({ textShadow: '0 0 4px' });
const grayStyle = css({
    color: 'darkslategray',
    fontWeight: 'bold',
    fontSize: '15px',
});

function renderLabel(label?: string, labelClassName?: string) {
    if (label) {
        return (
            <span
                className={
                    labelClassName ? labelClassName : labelStyle.toString()
                }
            >
                {label}
            </span>
        );
    }
    return null;
}

function renderIcon(icon: string, stackedOnIcon?: string) {
    if (stackedOnIcon) {
        return (
            <span className="fa-stack">
                <span className={classNames(stackedOnIcon, 'fa-stack-2x')} />
                <span className={classNames(icon, 'fa-stack-1x')} />
            </span>
        );
    } else {
        return <span className={classNames(icon)} />;
    }
}

function IconButton({
    icon,
    onClick,
    grey,
    disabled,
    iconColor,
    tooltip,
    opacity,
    active,
    className,
    label,
    prefixedLabel,
    labelClassName,
    stackedOnIcon,
}: Props) {
    return (
        <span
            onClick={onClick}
            className={classNames(className, `${shapeStyle}`, {
                [`${disabledStyle}`]: disabled,
                [`${opacityStyle}`]: opacity,
                [`${grayStyle}`]: grey,
                [`${activeStyle}`]: active,
            })}
            title={tooltip}
        >
            {label && prefixedLabel === true
                ? renderLabel(label, labelClassName)
                : ''}
            {renderIcon(icon, stackedOnIcon)}
            {label && !prefixedLabel ? renderLabel(label, labelClassName) : ''}
        </span>
    );
}

export default IconButton;
