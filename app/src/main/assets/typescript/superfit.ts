declare class IScroll {
    constructor(element: string, param: any)
}

declare var Native: any

(function startRequesting() {
    setInterval(() => request(), 500)
})()

function request() {
    const request = new XMLHttpRequest()
    request.onreadystatechange = () => {
        if (request.readyState == 4 && request.status == 200) {
            const data = JSON.parse(request.responseText)
            heartRateElement.innerText = data.heartRate.toString()
            speedElement.innerText = data.speed.toFixed(1)
            distanceElement.innerText = data.distance.toFixed(2)
            cadenceElement.innerText = data.cadence.toString()
            maxSpeedElement.innerText = data.maxSpeed.toFixed(1)

            let timeSpan = data.timeSpan
            const hour = Math.floor(timeSpan / 3600)
            timeSpan %= 3600
            const minute = Math.floor(timeSpan / 60)
            timeSpan %= 60
            if (hour)
                timeElement.innerText = `${hour}:${pad(minute, 2)}:${pad(timeSpan, 2)}`
            else
                timeElement.innerText = `${pad(minute, 2)}:${pad(timeSpan, 2)}`
            
            avgSpeedElement.innerText = data.averageSpeed.toFixed(1)

            if (data.gps) {
                const gps = document.getElementsByClassName("gps")[0]
                gps.classList.remove("hidden")
            }
        }
    }
    request.open("GET", "http://localhost:9865/", true)
    request.send()
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

