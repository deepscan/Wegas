import { normalizeDatas, NormalizedData } from './normalize/index';
import { ManagedMode } from '../API/rest';
import * as ActionType from './actionTypes';
import { ConfigurationSchema } from '../Editor/editionConfig';

export { ActionType };
function createAction<T extends string, P>(type: T, payload: P) {
  return {
    type,
    payload,
  };
}
export const ActionCreator = {
  ENTITY_UPDATE: (data: NormalizedData) =>
    createAction(ActionType.ENTITY_UPDATE, data),
  VARIABLE_EDIT: <T extends IWegasEntity>(data: {
    id: number;
    config?: ConfigurationSchema<T>;
    path?: string[];
    actions: {
      save?: (entity: T) => void;
      delete?: (entity: T) => void;
    };
  }) => createAction(ActionType.VARIABLE_EDIT, data),
  FSM_EDIT: (data: {
    id: number;
    config?: ConfigurationSchema<IVariableDescriptor>;
    path?: string[];
  }) => createAction(ActionType.FSM_EDIT, data),
  VARIABLE_CREATE: <T extends IWegasEntity>(data: {
    '@class': string;
    parentId?: number;
    actions: {
      save?: (entity: T) => void;
      delete?: (entity: T) => void;
    };
  }) => createAction(ActionType.VARIABLE_CREATE, data),
  MANAGED_MODE: (data: {
    deletedEntities: NormalizedData;
    updatedEntities: NormalizedData;
  }) => createAction(ActionType.MANAGED_MODE, data),
  PAGE_EDIT_MODE: (data: boolean) =>
    createAction(ActionType.PAGE_EDIT_MODE, data),
  PAGE_SRC_MODE: (data: boolean) =>
    createAction(ActionType.PAGE_SRC_MODE, data),
  PAGE_EDIT: (data: { page: string; path: string[] }) =>
    createAction(ActionType.PAGE_EDIT, data),
  PAGE_FETCH: (data: { pages: Pages }) =>
    createAction(ActionType.PAGE_FETCH, data),
  PUSHER_SOCKET: (data: { socket_id: string; status: string }) =>
    createAction(ActionType.PUSHER_SOCKET, data),
};

type ActionsUnion<
  A extends { [key: string]: (...args: any[]) => any }
> = ReturnType<A[keyof A]>;

export type StateActions = ActionsUnion<typeof ActionCreator>;

export function managedMode(payload: ManagedMode) {
  return ActionCreator.MANAGED_MODE({
    deletedEntities: normalizeDatas(payload.deletedEntities as any),
    updatedEntities: normalizeDatas(payload.updatedEntities as any),
  });
}