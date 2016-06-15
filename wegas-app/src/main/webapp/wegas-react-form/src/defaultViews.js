import { setDefaultWidgets } from 'jsoninput';
import StringView from './Views/string';
import BooleanView from './Views/boolean';
import UneditableView from './Views/uneditable';
import HiddenView from './Views/hidden';
import TextareaView from './Views/textarea';
import SelectView from './Views/select';
import ObjectView from './Views/object';
import ArrayView from './Views/array';
import HTMLView from './Views/html';
import HashlistView from './Views/hashlist';
import FlatVariableSelect from './Views/Interacting/FlatVariableSelect';
import WegasUrl from './Views/wegas-url';
import WegasImageUrl from './Views/wegas-image-url';

setDefaultWidgets({
    object: ObjectView,
    array: ArrayView,
    string: StringView,
    number: StringView,
    boolean: BooleanView,
    uneditable: UneditableView,
    hidden: HiddenView,
    textarea: TextareaView,
    select: SelectView,
    html: HTMLView,
    hashlist: HashlistView,
    wegasurl: WegasUrl,
    wegasimageurl: WegasImageUrl,
    flatvariableselect: FlatVariableSelect
});
