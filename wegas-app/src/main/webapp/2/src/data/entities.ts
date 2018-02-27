const base = [
  'Number',
  'String',
  'List',
  'Text',
  'Boolean',
  'Object',
  'Trigger',
  'Question',
];
export const rootDescriptors: string[] = base.map(b => b + 'Descriptor');
export const descriptors: string[] = rootDescriptors.concat([
  'ChoiceDescriptor',
  'SingleResultChoiceDescriptor',
]);
export const instances = base
  .map(b => b + 'Instance')
  .concat(['ChoiceInstance']);
/**
 * Check if variable has children
 * @param variable Variable to test
 */
export function varIsList(variable: any): variable is IParentDescriptor {
  return Array.isArray(variable.itemsIds);
}
/**
 * Check entity type.
 * @param variable Variable to test
 * @param cls Discriminant, class
 */
export function entityIs<T extends IWegasEntity>(
  variable: any,
  cls: T['@class'],
): variable is T {
  return variable['@class'] === cls;
}