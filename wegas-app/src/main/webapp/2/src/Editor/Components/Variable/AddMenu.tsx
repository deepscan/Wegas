import * as React from 'react';
import produce from 'immer';
import { Actions } from '../../../data';
import { getIcon, getLabel, getChildren } from '../../editionConfig';
import { StoreDispatch } from '../../../data/store';
import { Menu } from '../../../Components/Menu';
import { FontAwesome } from '../Views/FontAwesome';
import { asyncSFC } from '../../../Components/HOC/asyncSFC';

function buildMenuItems(variable: IWegasEntity) {
  return getChildren(variable).then(children => {
    return children.map(i => {
      const Label = asyncSFC(async () => {
        const entity = { '@class': i };
        const [icon = 'question', label = ''] = await Promise.all([
          getIcon(entity),
          getLabel(entity),
        ]);
        return (
          <>
            <FontAwesome icon={icon} fixedWidth />
            {label}
          </>
        );
      });
      return {
        label: <Label />,
        value: i,
      };
    });
  });
}
/**
 * handle Add button for List / Question
 */
export const AddMenuParent = asyncSFC(
  async ({
    variable,
    dispatch,
  }: {
    variable: IListDescriptor | IQuestionDescriptor;
    dispatch: StoreDispatch;
  }) => {
    const items = await buildMenuItems(variable);
    return (
      <Menu
        items={items}
        icon="plus"
        onSelect={i =>
          dispatch(Actions.EditorActions.createVariable(i.value, variable))
        }
      />
    );
  },
);
/**
 * Handle Add button for Choice
 */
export const AddMenuChoice = asyncSFC(
  async ({
    variable,
    dispatch,
  }: {
    variable: IChoiceDescriptor;
    dispatch: StoreDispatch;
  }) => {
    const items = await buildMenuItems(variable);
    return (
      <Menu
        items={items}
        icon="plus"
        onSelect={i =>
          dispatch(
            Actions.EditorActions.createVariable(i.value, undefined, {
              save: entity => {
                const newChoice = produce(variable, v => {
                  v.results.push(entity as any);
                });
                const index = newChoice.results.length - 1;
                dispatch(
                  Actions.VariableDescriptorActions.updateDescriptor(newChoice),
                ).then(() =>
                  dispatch(
                    Actions.EditorActions.editVariable(newChoice, [
                      'results',
                      String(index),
                    ]),
                  ),
                );
              },
            }),
          )
        }
      />
    );
  },
);