/*
YUI 3.10.1 (build 8bc088e)
Copyright 2013 Yahoo! Inc. All rights reserved.
Licensed under the BSD License.
http://yuilibrary.com/license/
*/

if (typeof __coverage__ === 'undefined') { __coverage__ = {}; }
if (!__coverage__['build/axis-time-base/axis-time-base.js']) {
   __coverage__['build/axis-time-base/axis-time-base.js'] = {"path":"build/axis-time-base/axis-time-base.js","s":{"1":0,"2":0,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0,"10":0,"11":0,"12":0,"13":0,"14":0,"15":0,"16":0,"17":0,"18":0,"19":0,"20":0,"21":0,"22":0,"23":0,"24":0,"25":0,"26":0,"27":0,"28":0,"29":0,"30":0,"31":0,"32":0,"33":0,"34":0,"35":0,"36":0,"37":0,"38":0,"39":0,"40":0,"41":0,"42":0,"43":0,"44":0,"45":0,"46":0,"47":0,"48":0,"49":0,"50":0,"51":0,"52":0,"53":0,"54":0,"55":0,"56":0,"57":0,"58":0,"59":0,"60":0,"61":0,"62":0,"63":0},"b":{"1":[0,0],"2":[0,0],"3":[0,0],"4":[0,0],"5":[0,0],"6":[0,0],"7":[0,0],"8":[0,0],"9":[0,0],"10":[0,0,0],"11":[0,0],"12":[0,0],"13":[0,0],"14":[0,0],"15":[0,0]},"f":{"1":0,"2":0,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0,"10":0,"11":0,"12":0},"fnMap":{"1":{"name":"(anonymous_1)","line":1,"loc":{"start":{"line":1,"column":26},"end":{"line":1,"column":45}}},"2":{"name":"TimeImpl","line":22,"loc":{"start":{"line":22,"column":0},"end":{"line":23,"column":0}}},"3":{"name":"(anonymous_3)","line":73,"loc":{"start":{"line":73,"column":20},"end":{"line":74,"column":4}}},"4":{"name":"(anonymous_4)","line":90,"loc":{"start":{"line":90,"column":20},"end":{"line":91,"column":4}}},"5":{"name":"(anonymous_5)","line":103,"loc":{"start":{"line":103,"column":20},"end":{"line":104,"column":4}}},"6":{"name":"(anonymous_6)","line":120,"loc":{"start":{"line":120,"column":20},"end":{"line":121,"column":4}}},"7":{"name":"(anonymous_7)","line":133,"loc":{"start":{"line":133,"column":16},"end":{"line":134,"column":4}}},"8":{"name":"(anonymous_8)","line":146,"loc":{"start":{"line":146,"column":16},"end":{"line":147,"column":4}}},"9":{"name":"(anonymous_9)","line":160,"loc":{"start":{"line":160,"column":17},"end":{"line":161,"column":4}}},"10":{"name":"(anonymous_10)","line":197,"loc":{"start":{"line":197,"column":18},"end":{"line":198,"column":4}}},"11":{"name":"(anonymous_11)","line":249,"loc":{"start":{"line":249,"column":22},"end":{"line":250,"column":4}}},"12":{"name":"(anonymous_12)","line":287,"loc":{"start":{"line":287,"column":16},"end":{"line":288,"column":4}}}},"statementMap":{"1":{"start":{"line":1,"column":0},"end":{"line":317,"column":42}},"2":{"start":{"line":10,"column":0},"end":{"line":10,"column":20}},"3":{"start":{"line":22,"column":0},"end":{"line":24,"column":1}},"4":{"start":{"line":26,"column":0},"end":{"line":26,"column":27}},"5":{"start":{"line":28,"column":0},"end":{"line":54,"column":2}},"6":{"start":{"line":56,"column":0},"end":{"line":300,"column":2}},"7":{"start":{"line":75,"column":8},"end":{"line":75,"column":52}},"8":{"start":{"line":76,"column":8},"end":{"line":79,"column":9}},"9":{"start":{"line":78,"column":12},"end":{"line":78,"column":59}},"10":{"start":{"line":80,"column":8},"end":{"line":80,"column":31}},"11":{"start":{"line":92,"column":8},"end":{"line":92,"column":50}},"12":{"start":{"line":93,"column":8},"end":{"line":93,"column":21}},"13":{"start":{"line":105,"column":8},"end":{"line":105,"column":52}},"14":{"start":{"line":106,"column":8},"end":{"line":109,"column":9}},"15":{"start":{"line":108,"column":12},"end":{"line":108,"column":59}},"16":{"start":{"line":110,"column":8},"end":{"line":110,"column":31}},"17":{"start":{"line":122,"column":8},"end":{"line":122,"column":50}},"18":{"start":{"line":123,"column":8},"end":{"line":123,"column":21}},"19":{"start":{"line":135,"column":8},"end":{"line":135,"column":52}},"20":{"start":{"line":136,"column":8},"end":{"line":136,"column":38}},"21":{"start":{"line":148,"column":8},"end":{"line":148,"column":52}},"22":{"start":{"line":149,"column":8},"end":{"line":149,"column":38}},"23":{"start":{"line":162,"column":8},"end":{"line":162,"column":41}},"24":{"start":{"line":163,"column":8},"end":{"line":166,"column":9}},"25":{"start":{"line":165,"column":12},"end":{"line":165,"column":64}},"26":{"start":{"line":167,"column":8},"end":{"line":167,"column":19}},"27":{"start":{"line":199,"column":8},"end":{"line":203,"column":30}},"28":{"start":{"line":204,"column":8},"end":{"line":239,"column":9}},"29":{"start":{"line":206,"column":12},"end":{"line":206,"column":31}},"30":{"start":{"line":207,"column":12},"end":{"line":237,"column":13}},"31":{"start":{"line":209,"column":16},"end":{"line":209,"column":36}},"32":{"start":{"line":213,"column":16},"end":{"line":213,"column":36}},"33":{"start":{"line":214,"column":16},"end":{"line":236,"column":17}},"34":{"start":{"line":216,"column":20},"end":{"line":216,"column":40}},"35":{"start":{"line":218,"column":21},"end":{"line":236,"column":17}},"36":{"start":{"line":220,"column":20},"end":{"line":231,"column":21}},"37":{"start":{"line":222,"column":24},"end":{"line":222,"column":46}},"38":{"start":{"line":226,"column":24},"end":{"line":229,"column":25}},"39":{"start":{"line":228,"column":28},"end":{"line":228,"column":38}},"40":{"start":{"line":230,"column":24},"end":{"line":230,"column":54}},"41":{"start":{"line":235,"column":20},"end":{"line":235,"column":30}},"42":{"start":{"line":238,"column":12},"end":{"line":238,"column":30}},"43":{"start":{"line":240,"column":8},"end":{"line":240,"column":24}},"44":{"start":{"line":251,"column":8},"end":{"line":256,"column":14}},"45":{"start":{"line":257,"column":8},"end":{"line":274,"column":9}},"46":{"start":{"line":259,"column":12},"end":{"line":259,"column":30}},"47":{"start":{"line":260,"column":12},"end":{"line":260,"column":32}},"48":{"start":{"line":261,"column":12},"end":{"line":273,"column":13}},"49":{"start":{"line":263,"column":16},"end":{"line":272,"column":17}},"50":{"start":{"line":265,"column":20},"end":{"line":265,"column":34}},"51":{"start":{"line":266,"column":20},"end":{"line":269,"column":21}},"52":{"start":{"line":268,"column":24},"end":{"line":268,"column":33}},"53":{"start":{"line":270,"column":20},"end":{"line":270,"column":45}},"54":{"start":{"line":271,"column":20},"end":{"line":271,"column":45}},"55":{"start":{"line":275,"column":8},"end":{"line":275,"column":32}},"56":{"start":{"line":276,"column":8},"end":{"line":276,"column":32}},"57":{"start":{"line":289,"column":8},"end":{"line":296,"column":9}},"58":{"start":{"line":291,"column":12},"end":{"line":291,"column":32}},"59":{"start":{"line":293,"column":13},"end":{"line":296,"column":9}},"60":{"start":{"line":295,"column":12},"end":{"line":295,"column":42}},"61":{"start":{"line":298,"column":8},"end":{"line":298,"column":19}},"62":{"start":{"line":302,"column":0},"end":{"line":302,"column":22}},"63":{"start":{"line":314,"column":0},"end":{"line":314,"column":73}}},"branchMap":{"1":{"line":76,"type":"if","locations":[{"start":{"line":76,"column":8},"end":{"line":76,"column":8}},{"start":{"line":76,"column":8},"end":{"line":76,"column":8}}]},"2":{"line":106,"type":"if","locations":[{"start":{"line":106,"column":8},"end":{"line":106,"column":8}},{"start":{"line":106,"column":8},"end":{"line":106,"column":8}}]},"3":{"line":163,"type":"if","locations":[{"start":{"line":163,"column":8},"end":{"line":163,"column":8}},{"start":{"line":163,"column":8},"end":{"line":163,"column":8}}]},"4":{"line":207,"type":"if","locations":[{"start":{"line":207,"column":12},"end":{"line":207,"column":12}},{"start":{"line":207,"column":12},"end":{"line":207,"column":12}}]},"5":{"line":214,"type":"if","locations":[{"start":{"line":214,"column":16},"end":{"line":214,"column":16}},{"start":{"line":214,"column":16},"end":{"line":214,"column":16}}]},"6":{"line":218,"type":"if","locations":[{"start":{"line":218,"column":21},"end":{"line":218,"column":21}},{"start":{"line":218,"column":21},"end":{"line":218,"column":21}}]},"7":{"line":220,"type":"if","locations":[{"start":{"line":220,"column":20},"end":{"line":220,"column":20}},{"start":{"line":220,"column":20},"end":{"line":220,"column":20}}]},"8":{"line":226,"type":"if","locations":[{"start":{"line":226,"column":24},"end":{"line":226,"column":24}},{"start":{"line":226,"column":24},"end":{"line":226,"column":24}}]},"9":{"line":257,"type":"if","locations":[{"start":{"line":257,"column":8},"end":{"line":257,"column":8}},{"start":{"line":257,"column":8},"end":{"line":257,"column":8}}]},"10":{"line":257,"type":"binary-expr","locations":[{"start":{"line":257,"column":11},"end":{"line":257,"column":15}},{"start":{"line":257,"column":19},"end":{"line":257,"column":30}},{"start":{"line":257,"column":34},"end":{"line":257,"column":49}}]},"11":{"line":261,"type":"if","locations":[{"start":{"line":261,"column":12},"end":{"line":261,"column":12}},{"start":{"line":261,"column":12},"end":{"line":261,"column":12}}]},"12":{"line":266,"type":"if","locations":[{"start":{"line":266,"column":20},"end":{"line":266,"column":20}},{"start":{"line":266,"column":20},"end":{"line":266,"column":20}}]},"13":{"line":289,"type":"if","locations":[{"start":{"line":289,"column":8},"end":{"line":289,"column":8}},{"start":{"line":289,"column":8},"end":{"line":289,"column":8}}]},"14":{"line":293,"type":"if","locations":[{"start":{"line":293,"column":13},"end":{"line":293,"column":13}},{"start":{"line":293,"column":13},"end":{"line":293,"column":13}}]},"15":{"line":293,"type":"binary-expr","locations":[{"start":{"line":293,"column":16},"end":{"line":293,"column":37}},{"start":{"line":293,"column":41},"end":{"line":293,"column":44}}]}},"code":["(function () { YUI.add('axis-time-base', function (Y, NAME) {","","/**"," * Provides functionality for the handling of time axis data for a chart."," *"," * @module charts"," * @submodule axis-time-base"," */","","var Y_Lang = Y.Lang;","/**"," * TimeImpl contains logic for time data. TimeImpl is used by the following classes:"," * <ul>"," *      <li>{{#crossLink \"TimeAxisBase\"}}{{/crossLink}}</li>"," *      <li>{{#crossLink \"TimeAxis\"}}{{/crossLink}}</li>"," *  </ul>"," *"," * @class TimeImpl"," * @constructor"," * @submodule axis-time-base"," */","function TimeImpl()","{","}","","TimeImpl.NAME = \"timeImpl\";","","TimeImpl.ATTRS =","{","    /**","     * Method used for formatting a label. This attribute allows for the default label formatting method to overridden.","     * The method use would need to implement the arguments below and return a `String` or an `HTMLElement`. The default","     * implementation of the method returns a `String`. The output of this method will be rendered to the DOM using","     * `appendChild`. If you override the `labelFunction` method and return an html string, you will also need to override","     * the Axis' `appendLabelFunction` to accept html as a `String`.","     * <dl>","     *      <dt>val</dt><dd>Label to be formatted. (`String`)</dd>","     *      <dt>format</dt><dd>STRFTime string used to format the label. (optional)</dd>","     * </dl>","     *","     * @attribute labelFunction","     * @type Function","     */","","    /**","     * Pattern used by the `labelFunction` to format a label.","     *","     * @attribute labelFormat","     * @type String","     */","    labelFormat: {","        value: \"%b %d, %y\"","    }","};","","TimeImpl.prototype = {","    /**","     * Type of data used in `Data`.","     *","     * @property _type","     * @readOnly","     * @private","     */","    _type: \"time\",","","    /**","     * Getter method for maximum attribute.","     *","     * @method _maximumGetter","     * @return Number","     * @private","     */","    _maximumGetter: function ()","    {","        var max = this._getNumber(this._setMaximum);","        if(!Y_Lang.isNumber(max))","        {","            max = this._getNumber(this.get(\"dataMaximum\"));","        }","        return parseFloat(max);","    },","","    /**","     * Setter method for maximum attribute.","     *","     * @method _maximumSetter","     * @param {Object} value","     * @private","     */","    _maximumSetter: function (value)","    {","        this._setMaximum = this._getNumber(value);","        return value;","    },","","    /**","     * Getter method for minimum attribute.","     *","     * @method _minimumGetter","     * @return Number","     * @private","     */","    _minimumGetter: function ()","    {","        var min = this._getNumber(this._setMinimum);","        if(!Y_Lang.isNumber(min))","        {","            min = this._getNumber(this.get(\"dataMinimum\"));","        }","        return parseFloat(min);","    },","","    /**","     * Setter method for minimum attribute.","     *","     * @method _minimumSetter","     * @param {Object} value","     * @private","     */","    _minimumSetter: function (value)","    {","        this._setMinimum = this._getNumber(value);","        return value;","    },","","    /**","     * Indicates whether or not the maximum attribute has been explicitly set.","     *","     * @method _getSetMax","     * @return Boolean","     * @private","     */","    _getSetMax: function()","    {","        var max = this._getNumber(this._setMaximum);","        return (Y_Lang.isNumber(max));","    },","","    /**","     * Indicates whether or not the minimum attribute has been explicitly set.","     *","     * @method _getSetMin","     * @return Boolean","     * @private","     */","    _getSetMin: function()","    {","        var min = this._getNumber(this._setMinimum);","        return (Y_Lang.isNumber(min));","    },","","    /**","     * Formats a label based on the axis type and optionally specified format.","     *","     * @method formatLabel","     * @param {Object} value","     * @param {Object} format Pattern used to format the value.","     * @return String","     */","    formatLabel: function(val, format)","    {","        val = Y.DataType.Date.parse(val);","        if(format)","        {","            return Y.DataType.Date.format(val, {format:format});","        }","        return val;","    },","","    /**","     * Constant used to generate unique id.","     *","     * @property GUID","     * @type String","     * @private","     */","    GUID: \"yuitimeaxis\",","","    /**","     * Type of data used in `Axis`.","     *","     * @property _dataType","     * @readOnly","     * @private","     */","    _dataType: \"time\",","","    /**","     * Gets an array of values based on a key.","     *","     * @method _getKeyArray","     * @param {String} key Value key associated with the data array.","     * @param {Array} data Array in which the data resides.","     * @return Array","     * @private","     */","    _getKeyArray: function(key, data)","    {","        var obj,","            keyArray = [],","            i = 0,","            val,","            len = data.length;","        for(; i < len; ++i)","        {","            obj = data[i][key];","            if(Y_Lang.isDate(obj))","            {","                val = obj.valueOf();","            }","            else","            {","                val = new Date(obj);","                if(Y_Lang.isDate(val))","                {","                    val = val.valueOf();","                }","                else if(!Y_Lang.isNumber(obj))","                {","                    if(Y_Lang.isNumber(parseFloat(obj)))","                    {","                        val = parseFloat(obj);","                    }","                    else","                    {","                        if(typeof obj !== \"string\")","                        {","                            obj = obj;","                        }","                        val = new Date(obj).valueOf();","                    }","                }","                else","                {","                    val = obj;","                }","            }","            keyArray[i] = val;","        }","        return keyArray;","    },","","    /**","     * Calculates the maximum and minimum values for the `Axis`.","     *","     * @method _updateMinAndMax","     * @private","     */","    _updateMinAndMax: function()","    {","        var data = this.get(\"data\"),","            max = 0,","            min = 0,","            len,","            num,","            i;","        if(data && data.length && data.length > 0)","        {","            len = data.length;","            max = min = data[0];","            if(len > 1)","            {","                for(i = 1; i < len; i++)","                {","                    num = data[i];","                    if(isNaN(num))","                    {","                        continue;","                    }","                    max = Math.max(num, max);","                    min = Math.min(num, min);","                }","            }","        }","        this._dataMaximum = max;","        this._dataMinimum = min;","    },","","    /**","     * Parses value into a number.","     *","     * @method _getNumber","     * @param val {Object} Value to parse into a number","     * @return Number","     * @private","     */","    _getNumber: function(val)","    {","        if(Y_Lang.isDate(val))","        {","            val = val.valueOf();","        }","        else if(!Y_Lang.isNumber(val) && val)","        {","            val = new Date(val).valueOf();","        }","","        return val;","    }","};","","Y.TimeImpl = TimeImpl;","","/**"," * TimeAxisBase manages time data for an axis."," *"," * @class TimeAxisBase"," * @extends AxisBase"," * @uses TimeImpl"," * @constructor"," * @param {Object} config (optional) Configuration parameters."," * @submodule axis-time-base"," */","Y.TimeAxisBase = Y.Base.create(\"timeAxisBase\", Y.AxisBase, [Y.TimeImpl]);","","","}, '3.10.1', {\"requires\": [\"axis-base\"]});","","}());"]};
}
var __cov_hR9o4LWNW3fNK5ow5CKfkg = __coverage__['build/axis-time-base/axis-time-base.js'];
__cov_hR9o4LWNW3fNK5ow5CKfkg.s['1']++;YUI.add('axis-time-base',function(Y,NAME){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['1']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['2']++;var Y_Lang=Y.Lang;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['3']++;function TimeImpl(){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['2']++;}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['4']++;TimeImpl.NAME='timeImpl';__cov_hR9o4LWNW3fNK5ow5CKfkg.s['5']++;TimeImpl.ATTRS={labelFormat:{value:'%b %d, %y'}};__cov_hR9o4LWNW3fNK5ow5CKfkg.s['6']++;TimeImpl.prototype={_type:'time',_maximumGetter:function(){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['3']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['7']++;var max=this._getNumber(this._setMaximum);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['8']++;if(!Y_Lang.isNumber(max)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['1'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['9']++;max=this._getNumber(this.get('dataMaximum'));}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['1'][1]++;}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['10']++;return parseFloat(max);},_maximumSetter:function(value){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['4']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['11']++;this._setMaximum=this._getNumber(value);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['12']++;return value;},_minimumGetter:function(){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['5']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['13']++;var min=this._getNumber(this._setMinimum);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['14']++;if(!Y_Lang.isNumber(min)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['2'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['15']++;min=this._getNumber(this.get('dataMinimum'));}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['2'][1]++;}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['16']++;return parseFloat(min);},_minimumSetter:function(value){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['6']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['17']++;this._setMinimum=this._getNumber(value);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['18']++;return value;},_getSetMax:function(){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['7']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['19']++;var max=this._getNumber(this._setMaximum);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['20']++;return Y_Lang.isNumber(max);},_getSetMin:function(){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['8']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['21']++;var min=this._getNumber(this._setMinimum);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['22']++;return Y_Lang.isNumber(min);},formatLabel:function(val,format){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['9']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['23']++;val=Y.DataType.Date.parse(val);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['24']++;if(format){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['3'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['25']++;return Y.DataType.Date.format(val,{format:format});}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['3'][1]++;}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['26']++;return val;},GUID:'yuitimeaxis',_dataType:'time',_getKeyArray:function(key,data){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['10']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['27']++;var obj,keyArray=[],i=0,val,len=data.length;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['28']++;for(;i<len;++i){__cov_hR9o4LWNW3fNK5ow5CKfkg.s['29']++;obj=data[i][key];__cov_hR9o4LWNW3fNK5ow5CKfkg.s['30']++;if(Y_Lang.isDate(obj)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['4'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['31']++;val=obj.valueOf();}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['4'][1]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['32']++;val=new Date(obj);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['33']++;if(Y_Lang.isDate(val)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['5'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['34']++;val=val.valueOf();}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['5'][1]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['35']++;if(!Y_Lang.isNumber(obj)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['6'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['36']++;if(Y_Lang.isNumber(parseFloat(obj))){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['7'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['37']++;val=parseFloat(obj);}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['7'][1]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['38']++;if(typeof obj!=='string'){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['8'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['39']++;obj=obj;}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['8'][1]++;}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['40']++;val=new Date(obj).valueOf();}}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['6'][1]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['41']++;val=obj;}}}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['42']++;keyArray[i]=val;}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['43']++;return keyArray;},_updateMinAndMax:function(){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['11']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['44']++;var data=this.get('data'),max=0,min=0,len,num,i;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['45']++;if((__cov_hR9o4LWNW3fNK5ow5CKfkg.b['10'][0]++,data)&&(__cov_hR9o4LWNW3fNK5ow5CKfkg.b['10'][1]++,data.length)&&(__cov_hR9o4LWNW3fNK5ow5CKfkg.b['10'][2]++,data.length>0)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['9'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['46']++;len=data.length;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['47']++;max=min=data[0];__cov_hR9o4LWNW3fNK5ow5CKfkg.s['48']++;if(len>1){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['11'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['49']++;for(i=1;i<len;i++){__cov_hR9o4LWNW3fNK5ow5CKfkg.s['50']++;num=data[i];__cov_hR9o4LWNW3fNK5ow5CKfkg.s['51']++;if(isNaN(num)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['12'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['52']++;continue;}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['12'][1]++;}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['53']++;max=Math.max(num,max);__cov_hR9o4LWNW3fNK5ow5CKfkg.s['54']++;min=Math.min(num,min);}}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['11'][1]++;}}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['9'][1]++;}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['55']++;this._dataMaximum=max;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['56']++;this._dataMinimum=min;},_getNumber:function(val){__cov_hR9o4LWNW3fNK5ow5CKfkg.f['12']++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['57']++;if(Y_Lang.isDate(val)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['13'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['58']++;val=val.valueOf();}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['13'][1]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['59']++;if((__cov_hR9o4LWNW3fNK5ow5CKfkg.b['15'][0]++,!Y_Lang.isNumber(val))&&(__cov_hR9o4LWNW3fNK5ow5CKfkg.b['15'][1]++,val)){__cov_hR9o4LWNW3fNK5ow5CKfkg.b['14'][0]++;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['60']++;val=new Date(val).valueOf();}else{__cov_hR9o4LWNW3fNK5ow5CKfkg.b['14'][1]++;}}__cov_hR9o4LWNW3fNK5ow5CKfkg.s['61']++;return val;}};__cov_hR9o4LWNW3fNK5ow5CKfkg.s['62']++;Y.TimeImpl=TimeImpl;__cov_hR9o4LWNW3fNK5ow5CKfkg.s['63']++;Y.TimeAxisBase=Y.Base.create('timeAxisBase',Y.AxisBase,[Y.TimeImpl]);},'3.10.1',{'requires':['axis-base']});
