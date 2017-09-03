declare class IScroll {
    constructor(element: string, param: any)
}

class Module 
{
    onHeartRate(rate: number) {
        if (this.heartRateElement)
            this.heartRateElement.innerText = rate.toString()
    }

    onSpeed(rate: number) {
        if (this.speedElement)
            this.speedElement.innerText = rate.toFixed(1)
    }

    onDistance(rate: number) {
        if (this.distanceElement)
            this.distanceElement.innerText = rate.toFixed(2)
    }

    onCadence(rate: number) {
        if (this.cadenceElement)
            this.cadenceElement.innerText = rate.toString()
    }

    onMaxSpeed(speed: number) {
        if (this.maxSpeedElement)
            this.maxSpeedElement.innerText = speed.toFixed(1)
    }

    onTimeSpan(timeSpan: number) {
        if (this.timeElement) {
            const hour = Math.floor(timeSpan / 3600)
            timeSpan %= 3600
            const minute = Math.floor(timeSpan / 60)
            timeSpan %= 60
            if (hour)
                this.timeElement.innerText = `${hour}:${this.pad(minute, 2)}:${this.pad(timeSpan, 2)}`
            else
                this.timeElement.innerText = `${this.pad(minute, 2)}:${this.pad(timeSpan, 2)}`
        }
    }

    onAvgSpeed(avgSpeed: number) {
        if (this.avgSpeedElement)
            this.avgSpeedElement.innerText = avgSpeed.toFixed(1)
    }

    onTest(test: string) {
        if (this.testElement)
            this.testElement.innerText = test
        else
            alert(test)
    }

    private pad(num: number, size: number) {
        let s = num + ""
        while (s.length < size)
            s = "0" + s
        return s
    }

    private readonly heartRateElement = document.getElementById('heartRate')
    private readonly speedElement = document.getElementById('speed')
    private readonly distanceElement= document.getElementById('distance')
    private readonly cadenceElement = document.getElementById('cadence')
    private readonly timeElement = document.getElementById('time')
    private readonly avgSpeedElement = document.getElementById('avgSpeed')
    private readonly maxSpeedElement = document.getElementById('maxSpeed')
    private readonly testElement = document.getElementById('testSpan')
    private readonly theScroll = new IScroll('#wrapper',
    {
        scrollbars: true,
        interactiveScrollbars: true,
        click: true,
        disablePointer: true,
        disableTouch: false,
        fadeScrollbars: true,
        shrinkScrollbars: 'clip'
    })
}

document.addEventListener("DOMContentLoaded", () => {
    moduleInstance = new Module()
})

var moduleInstance: Module