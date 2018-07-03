import * as React from 'react';
import { StoreConsumer, StoreDispatch } from '../data/store';
import { VariableDescriptor } from '../data/selectors';
import { getInstance } from '../data/methods/VariableDescriptor';

type instanceOf<D> = D extends IVariableDescriptor<infer U> ? U : never;

function selectVar<T extends IVariableDescriptor>(name: string) {
  return function():
    | {
        descriptor: T;
        instance: instanceOf<T>;
      }
    | undefined {
    const descriptor = VariableDescriptor.first<T>('name', name);
    if (descriptor === undefined) {
      return undefined;
    }

    const instance = getInstance(descriptor)() as instanceOf<T>;
    return { descriptor, instance };
  };
}

export function VariableConnect<D extends IVariableDescriptor>(props: {
  name: string;
  children: (
    store: {
      state: { descriptor: D; instance: instanceOf<D> } | undefined;
      dispatch: StoreDispatch;
    },
  ) => React.ReactNode;
}) {
  return (
    <StoreConsumer selector={selectVar<D>(props.name)}>
      {props.children}
    </StoreConsumer>
  );
}
