
declare class SensorEvent {
    data: SensorData
    gpsActive?: Boolean
}

declare class SensorData {
    heartRate: number
    speed: number
    distance: number
    cadence: number
    maxSpeed: number
    timeSpan: number
    averageSpeed: number
}

function onSensorData(evt: SensorEvent) {
    heartRateElement.innerText = evt.data.heartRate.toString()
    speedElement.innerText = evt.data.speed.toFixed(1)
    distanceElement.innerText = evt.data.distance.toFixed(2)
    cadenceElement.innerText = evt.data.cadence.toString()
    maxSpeedElement.innerText = evt.data.maxSpeed.toFixed(1)
    let timeSpan = evt.data.timeSpan
    const hour = Math.floor(timeSpan / 3600)
    timeSpan %= 3600
    const minute = Math.floor(timeSpan / 60)
    timeSpan %= 60
    if (hour)
        timeElement.innerText = `${hour}:${pad(minute, 2)}:${pad(timeSpan, 2)}`
    else
        timeElement.innerText = `${pad(minute, 2)}:${pad(timeSpan, 2)}`
    
    avgSpeedElement.innerText = evt.data.averageSpeed.toFixed(1)

    if (evt.gpsActive) {
        const gps = document.getElementsByClassName("gps")[0]
        gps.classList.remove("hidden")
    }
}

function pad(num: number, size: number) {
    let s = num + ""
    while (s.length < size)
        s = "0" + s
    return s
}

const heartRateElement = document.getElementById('heartRate')
const speedElement = document.getElementById('speed')
const distanceElement= document.getElementById('distance')
const cadenceElement = document.getElementById('cadence')
const timeElement = document.getElementById('time')
const avgSpeedElement = document.getElementById('avgSpeed')
const maxSpeedElement = document.getElementById('maxSpeed')
const display = document.getElementById('display')

const displayScroll = new IScroll('#display', {
    scrollbars: true,
    interactiveScrollbars: true,
    click: true,
    disablePointer: true,
    disableTouch: false,
    fadeScrollbars: true,
    shrinkScrollbars: 'clip'
})

Connection.initialize(data => onSensorData(data))