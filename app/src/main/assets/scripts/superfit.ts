declare class IScroll {
    constructor(element: string, param: any)
}

declare var Native: any

class Module {

    constructor() {
        const goClicker = new ButtonClicker(this.go, evt => this.go, 
            typeof Native != undefined ? () => Native.doHapticFeedback() : null, 
            () => this.onGo())

        const startClicker = new ButtonClicker(this.start, evt => this.start, 
            typeof Native != undefined ? () => Native.doHapticFeedback() : null, 
            () => this.onStart())

        const stopClicker = new ButtonClicker(this.stop, evt => this.stop, 
            typeof Native != undefined ? () => Native.doHapticFeedback() : null,
            () => this.onStop())
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

    onBackPressed() {
        if (this.isDisplayOn) {
            this.display.classList.add('hidden')
            this.isDisplayOn = false    
        }
        else
            Native.close()
    }

    private onGo() {
        this.isDisplayOn = true
        this.display.classList.remove('hidden')

        this.startRequesting()
    }

    private startRequesting() {
        setInterval(() => this.request(), 500)
    }

    private request() {
        const request = new XMLHttpRequest()
        request.onreadystatechange = () => {
            if (request.readyState == 4 && request.status == 200) {
                const data = JSON.parse(request.responseText)
                this.heartRateElement.innerText = data.heartRate.toString()
                this.speedElement.innerText = data.speed.toFixed(1)
                this.distanceElement.innerText = data.distance.toFixed(2)
                this.cadenceElement.innerText = data.cadence.toString()
                this.maxSpeedElement.innerText = data.maxSpeed.toFixed(1)

                let timeSpan = data.timeSpan
                const hour = Math.floor(timeSpan / 3600)
                timeSpan %= 3600
                const minute = Math.floor(timeSpan / 60)
                timeSpan %= 60
                if (hour)
                    this.timeElement.innerText = `${hour}:${this.pad(minute, 2)}:${this.pad(timeSpan, 2)}`
                else
                    this.timeElement.innerText = `${this.pad(minute, 2)}:${this.pad(timeSpan, 2)}`
                
                this.avgSpeedElement.innerText = data.averageSpeed.toFixed(1)
            }
        }
        request.open("GET", "http://localhost:9865/", true)
        request.send()
    }

    private onStart() {
        Native.start()
    }

    private onStop() {
        Native.stop()
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
    private readonly go = document.getElementById('go')
    private readonly start = document.getElementById('start')
    private readonly stop = document.getElementById('stop')
    private readonly display = document.getElementById('wrapper')
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

    private isDisplayOn = false
}

const moduleInstance = new Module()