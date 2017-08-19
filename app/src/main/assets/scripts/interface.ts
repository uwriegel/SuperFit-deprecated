
function setHeartRate(dataString: string) {
    //var jason = decodeURIComponent(atob(dataString))
    //var jason = dataString
//    var data =  JSON.parse(jason)
    var data = Number.parseInt(dataString)
    moduleInstance.onHeartRate(data)
}

function setSpeed(dataString: string) {
    var data = Number.parseFloat(dataString)
    moduleInstance.onSpeed(data)
}

function setDistance(dataString: string) {
    var data = Number.parseFloat(dataString)
    moduleInstance.onDistance(data)
}

function setCadence(dataString: string) {
    var data = Number.parseInt(dataString)
    moduleInstance.onCadence(data)
}

function setTest(test: string) {
    moduleInstance.onTest(test)
}