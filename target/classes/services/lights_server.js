var PROTO_PATH = __dirname + '/../../proto/lights.proto';

var grpc = require('grpc');
var protoLoader = require('@grpc/proto-loader');
var packageDefinition = protoLoader.loadSync(
    PROTO_PATH,
    {keepCase: true,
     longs: String,
     enums: String,
     defaults: true,
     oneofs: true
    });
var lights_proto = grpc.loadPackageDefinition(packageDefinition).Lights;


function turnOn(call, callback) {
  var str = "On";

  switch (str) {
    case 0:
     var off = "Off";
      break;
    case 1:
     var on = "On";
  }

  callback(null, {message: str + call.request.message});
  
}

function changeBrightness(call, callback) {
  var brightness = 0;

  // if(brightness <= 56){
    
    call.write(brightness++)
    
    
  // } else {
    call.end();
  // }
  
}

function changeColour(call, callback){
  
  // shuffleArray function taken from: https://stackoverflow.com/questions/2450954/how-to-randomize-shuffle-a-javascript-array?rq=1

  var shuffle = function shuffleArray(array) {
    for (var i = array.length - 1; i > 0; i--) {
        var j = Math.floor(Math.random() * (i + 1));
        var temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        
        return temp;
    }
}
  
  var colours = ['Blue', 'Red', 'Green', 'Orange'];


  callback(null, {message: shuffle(colours) + call.request.message});
}


function main() {
  var server = new grpc.Server();
  server.addService(lights_proto.Lights.service, {turnOn: turnOn ,
                                                  changeBrightness: changeBrightness,
                                                  changeColour: changeColour});

  server.bind('0.0.0.0:3300', grpc.ServerCredentials.createInsecure());
  server.start();
}

main();
