import * as React from 'react';
import { css } from 'glamor';
import { WidgetProps } from 'jsoninput/typings/types';
import { Cover } from '../../../Components/Cover';
import { IconButton } from '../../../Components/Button/IconButton';
import { Menu } from '../../../Components/Menu';
import { CommonView } from './commonView';
import { Labeled } from './labeled';

const transparentStyle = css({
  opacity: 0,
  marginTop: '1em',
  transition: 'opacity .5s .1s',
  'div:hover > &': {
    opacity: 1,
  },
});

const listElementContainerStyle = css({
  display: 'flex',
});

const listElementStyle = css({
  flex: 1,
  // Reduce vertical space between array elements:
  '& div': {
    marginTop: 0,
  },
});

interface IArrayProps {
  view: {
    choices?: { label: React.ReactNode }[];
    tooltip?: string;
  };
}

class Adder extends React.Component<
  WidgetProps.ArrayProps & IArrayProps & { id: string },
  { open: boolean }
> {
  constructor(props: WidgetProps.ArrayProps & IArrayProps & { id: string }) {
    super(props);
    this.state = {
      open: false,
    };
  }
  render() {
    if (Array.isArray(this.props.view.choices)) {
      return this.state.open ? (
        <Cover onClick={() => this.setState({ open: false })} zIndex={100}>
          <Menu
            items={this.props.view.choices}
            onSelect={({ value }) =>
              this.setState({ open: false }, () => this.props.onChildAdd(value))
            }
          />
        </Cover>
      ) : (
        <IconButton
          id={this.props.id}
          icon="plus-circle"
          onClick={() => this.setState({ open: true })}
          tooltip={this.props.view.tooltip}
        />
      );
    }
    return (
      <IconButton
        id={this.props.id}
        icon="plus-circle"
        onClick={() => this.props.onChildAdd()}
        tooltip={this.props.view.tooltip}
      />
    );
  }
}
function ArrayWidget(props: WidgetProps.ArrayProps & IArrayProps) {
  const valueLength = Array.isArray(props.value) ? props.value.length : 0;
  const { maxItems = Infinity, minItems = 0 } = props.schema;
  const disabled = props.view.disabled;
  function renderChild(child: React.ReactChild, index: number) {
    return (
      <div className={listElementContainerStyle.toString()}>
        <span className={listElementStyle.toString()}>{child}</span>
        <span className={transparentStyle.toString()}>
          {minItems < valueLength && !disabled ? (
            <IconButton
              icon="trash"
              onClick={() => props.onChildRemove(index)}
              tooltip="Delete this group"
            />
          ) : null}
        </span>
      </div>
    );
  }

  const children = React.Children.map(props.children, renderChild);

  return (
    <CommonView errorMessage={props.errorMessage} view={props.view}>
      <Labeled label={props.view.label} description={props.view.description}>
        {({ inputId, labelNode }) => {
          return (
            <>
              {labelNode}
              {maxItems > valueLength &&
                !disabled && <Adder id={inputId} {...props} />}
              {children}
            </>
          );
        }}
      </Labeled>
    </CommonView>
  );
}

export default ArrayWidget;