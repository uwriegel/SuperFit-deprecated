class Module 
{
    constructor() {
        document.addEventListener("DOMContentLoaded", () => {
            this.heartRateElement = document.getElementById('heartRate')
            this.speedElement = document.getElementById('speed')
            this.distanceElement = document.getElementById('distance')
            this.cadenceElement = document.getElementById('cadence')
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

    private heartRateElement: HTMLElement
    private speedElement: HTMLElement
    private distanceElement: HTMLElement
    private cadenceElement: HTMLElement
}

var moduleInstance = new Module()