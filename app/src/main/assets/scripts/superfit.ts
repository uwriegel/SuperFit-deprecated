declare class IScroll{
    constructor(element: string, param: any)
}

class Module 
{
    constructor() {
        document.addEventListener("DOMContentLoaded", () => {
            this.theScroll = new IScroll('#wrapper',
            {
                scrollbars: true,
                interactiveScrollbars: true,
                click: true,
                disablePointer: true,
                disableTouch: false,
                fadeScrollbars: true,
                shrinkScrollbars: 'clip'
            })

            this.heartRateElement = document.getElementById('heartRate')
            this.speedElement = document.getElementById('speed')
            this.distanceElement = document.getElementById('distance')
            this.cadenceElement = document.getElementById('cadence')
            this.timeElement = document.getElementById('time')
            this.avgSpeedElement = document.getElementById('avgSpeed')
            this.maxSpeedElement = document.getElementById('maxSpeed')
            this.testElement = document.getElementById('testSpan')
        })
    }

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
            var hour = Math.floor(timeSpan / 3600)
            timeSpan %= 3600
            var minute = Math.floor(timeSpan / 60)
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

    private pad(num, size) {
        var s = num + ""
        while (s.length < size)
            s = "0" + s
        return s
    }

    private heartRateElement: HTMLElement
    private speedElement: HTMLElement
    private distanceElement: HTMLElement
    private cadenceElement: HTMLElement
    private timeElement: HTMLElement
    private avgSpeedElement: HTMLElement
    private maxSpeedElement: HTMLElement
    private testElement: HTMLElement
    private theScroll: any
}

var moduleInstance = new Module()