if (!window.console || !console.firebug)
{
    var names = ["log", "debug", "info", "warn", "error", "assert", "dir", "dirxml",
    "group", "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd"];
    
    var emptyFunction = function() {};
    var count = names.length;

    window.console = {};
    for (var i = 0, count = names.length; i < count; ++i) {
        window.console[names[i]] = emptyFunction;
    }
}