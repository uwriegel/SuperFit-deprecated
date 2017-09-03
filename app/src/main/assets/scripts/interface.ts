
function setHeartRate(dataString: string) {
    const data = Number.parseInt(dataString)
    moduleInstance.onHeartRate(data)
}

function setSpeed(dataString: string) {
    const data = Number.parseFloat(dataString)
    moduleInstance.onSpeed(data)
}

function setDistance(dataString: string) {
    const data = Number.parseFloat(dataString)
    moduleInstance.onDistance(data)
}

function setCadence(dataString: string) {   
    const data = Number.parseInt(dataString)
    moduleInstance.onCadence(data)
}

function setMaxSpeed(dataString: string) {
    const data = Number.parseFloat(dataString)
    moduleInstance.onMaxSpeed(data)
}

function setTimeSpan(dataString1: string, dataString2: string) {
    var data = Number.parseInt(dataString1)
    moduleInstance.onTimeSpan(data)
    var speed = Number.parseFloat(dataString2)
    moduleInstance.onAvgSpeed(speed)
}

function setTest(test: string) {
    moduleInstance.onTest(test)
}

function onBackPressed() {
    moduleInstance.onBackPressed()
}