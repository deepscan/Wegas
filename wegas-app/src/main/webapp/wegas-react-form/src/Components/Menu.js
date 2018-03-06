import PropTypes from 'prop-types';
import React from 'react';
import { css } from 'glamor';
import Menu, { Item, SubMenu } from 'rc-menu';
// eslint-disable-next-line
import '!!style-loader!css-loader!rc-menu/assets/index.css';

// Fix visibility
css.global('body .rc-menu-submenu-popup', {
    zIndex: 10000,
});

const MENU_STYLE = css({
    display: 'inline-block',
    backgroundColor: 'white',
    userSelect: 'none',
    cursor: 'default',
});
const rightPadding = css({
    paddingRight: '2em',
});
function genItems(o, i) {
    const hasSubMenu = Array.isArray(o.children);
    const key = o.value || i;
    if (hasSubMenu) {
        const titleRight = <span {...rightPadding}>{o.label}</span>;
        return (
            <SubMenu key={JSON.stringify(key)} title={titleRight}>
                {o.children.map(genItems)}
            </SubMenu>
        );
    }
    return (
        <Item key={JSON.stringify(key)} disabled={o.disabled}>
            {o.label}
        </Item>
    );
}
function WMenu({ menu, onChange }) {
    const menuItems = menu.map(genItems);
    return (
        <Menu
            className={MENU_STYLE.toString()}
            onClick={value => onChange(JSON.parse(value.key))}
            defaultActiveFirst={false}
        >
            {menuItems}
        </Menu>
    );
}
WMenu.propTypes = {
    menu: PropTypes.arrayOf(
        PropTypes.shape({
            label: PropTypes.string.isRequired,
            value: PropTypes.any,
            disabled: PropTypes.bool,
            children: PropTypes.array,
        })
    ).isRequired,
    onChange: PropTypes.func.isRequired,
};
export default WMenu;