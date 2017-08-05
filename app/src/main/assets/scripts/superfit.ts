class Module 
{
    constructor() {
        document.addEventListener("DOMContentLoaded", () => {
            this.heartRateDiv = document.getElementById('heartRate')
        })
    }

    onHeartRate(rate: number) {
        console.log(`Im Module: ${rate}`)
        if (this.heartRateDiv)
            this.heartRateDiv.innerText = rate.toString()
    }

    private heartRateDiv: HTMLElement
}

var moduleInstance = new Module()