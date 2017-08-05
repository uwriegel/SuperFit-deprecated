
function setHeartRate(dataString: string) {
    //var jason = decodeURIComponent(atob(dataString))
    //var jason = dataString
//    var data =  JSON.parse(jason)
    var data = Number.parseInt(dataString)
    moduleInstance.onHeartRate(data)
}

function setTest(test: string) {
    alert(test)
}