declare var Native: any

(async function () {
    const starter = document.getElementsByClassName('starter')[0] as HTMLElement
    const stopper = document.getElementsByClassName('stopper')[0] as HTMLElement

    const starterClicker = new ButtonClicker(starter, evt => starter,
        () => Native.doHapticFeedback(),
        () => {
            Native.start()
            setTimeout(() => checkConnection(), 500)
        })

    const stopperClicker = new ButtonClicker(stopper, evt => stopper,
        () => Native.doHapticFeedback(),
        async () => {
            await Connection.stop()
            Native.stop()
        })

    function displayStart() {
        starter.classList.remove("hidden")
        stopper.classList.add("hidden")
    }

    function displayStop() {
        stopper.classList.remove("hidden")
        starter.classList.add("hidden")
    }

    async function checkConnection() {
        await Connection.checkConnection() ? displayStop() : displayStart()
    }

    checkConnection()
})()