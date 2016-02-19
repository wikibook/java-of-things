var mqttConsoleApp = angular.module("mqttConsoleApp", []);

mqttConsoleApp.controller("mqttConsoleCtrl", 
    ["$scope", "$timeout", function ($scope, $timeout) {
        var timeout = function (fn){
            $timeout(function(){
                fn();
           }, 100);
        };
        
        $scope.connectState = "Connect";
        $scope.mqtt = {
            host: location.hostname,
            port: 61614
        };
        $scope.connect = function () {
            if($scope.client && $scope.client.connected){
                $scope.client.disconnect();
                $scope.client.connected = false;
                $scope.connectState = "Connect";
            }else{
                $scope.connectState = "Connecting";
                $scope.client = new Paho.MQTT.Client(
                        $scope.mqtt.host, 
                        Number($scope.mqtt.port), 
                        $scope.mqtt.clientId);
                $scope.client.onMessageArrived = 
                        function (message) {
                    timeout(function(){
                        if (message.destinationName.endsWith("broadcast")) {
                            if ($scope.covs)
                                $scope.covs = message.payloadString + 
                                    "\n" + $scope.covs;
                            else
                                $scope.covs = message.payloadString;
                        } else {
                            $scope.result = message.payloadString;
                            document.getElementById("command").focus();
                        }
                    });
                };
                $scope.client.onConnectionLost = 
                        function (error) {
                    if (error.errorCode !== 0) {
                        timeout(function(){
                            $scope.client.connected = false;
                            $scope.connectState = "Connect";
                            alert("Connection lost!")
                        });
                    }
                };
                $scope.client.connect({
                    onSuccess: function () {
                        timeout(function(){
                            $scope.client.subscribe(
                                    "jot/chapter4/mqtt/thing/" + 
                                    $scope.mqtt.clientId + "/result");
                            $scope.mqtt.handlerId = 
                                    $scope.mqtt.thingIp.replace(/\./g, '_');
                            $scope.client.subscribe(
                                    "jot/chapter4/mqtt/thing/" + 
                                    $scope.mqtt.handlerId + "/broadcast");
                            $scope.client.connected = true;
                            $scope.connectState = "Connected";
                            document.getElementById("command").focus();
                        });
                    },
                    onFailure: function () {
                        timeout(function(){
                            $scope.client.connected = false;
                            $scope.connectState = "Connect";
                            alert("Connection fail!")
                        });
                    }
                });
            }
        };

        $scope.send = function () {
            if ($scope.client.connected) {
                var message = new Paho.MQTT.Message($scope.command);
                message.destinationName = "jot/chapter4/mqtt/thing/" + 
                        $scope.mqtt.clientId + 
                        "/" + 
                        $scope.mqtt.handlerId + 
                        "/command";
                $scope.client.send(message);
                $scope.command = "";
            }
        };
    }]);