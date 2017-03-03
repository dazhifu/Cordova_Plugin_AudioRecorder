var exec = require('cordova/exec');

exports.AUDIO_SAMPLINGS = {
    MAX: 44100,
    NORMAL: 22050,
    LOW: 8000
};

var noop = function(){};

exports.startRecord = function(options, success, error) {
    success = success || noop;
    error = error || noop;
    exec(success, error, "AudioRecorder", "startRecord", [options]);
};

exports.stopRecord = function(success, error){
    success = success || noop;
    error = error || noop;
    exec(success, error, "AudioRecorder", 'stopRecord');
};
exports.delRecord = function(options,success, error){
    success = success || noop;
    error = error || noop;

    exec(success, error, "AudioRecorder", "delRecord",[options]);
};

exports.encodeBase64Record = function(path,success, error){
    success = success || noop;
    error = error || noop;
    exec(success, error, "AudioRecorder", 'encodeBase64Record',[path]);
};