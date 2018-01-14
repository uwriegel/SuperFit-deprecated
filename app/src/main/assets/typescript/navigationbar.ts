
declare class TrackData {
    trackNr: number
    latitude: number
    longitude: number
    time: number
    speed: number
    distance: number
}

const tracks = document.getElementById("tracks")
const tracksMarker = document.getElementById("tracksMarker")
const trackList = document.getElementById("trackList")
const trackFactory = (<HTMLTemplateElement>document.getElementById('trackTemplate')).content.querySelector('li')
const OPENED  = "opened"

tracks.onclick = () => {
    Native.doHapticFeedback()

    trackList.innerHTML = ""

    if (tracksMarker.classList.contains(OPENED))
        tracksMarker.classList.remove(OPENED) 
    else {
        tracksMarker.classList.add(OPENED)
        Native.fillTracks()
    }
}

function onTracks(tracks: TrackData[]) {
    var lis = tracks.map(n => {
        const li = trackFactory.cloneNode(true) as HTMLLIElement
        li.dataset.nr = n.trackNr.toString()

        const row = li.querySelector(".title")
        const date = new Date(n.time)
        row.innerHTML = date.toLocaleString(undefined, {
            year: "numeric", month: "2-digit", 
            day: "2-digit", hour: "2-digit", minute: "2-digit"
        })

        const speed = li.querySelector(".speed")
        speed.innerHTML = `${n.speed.toFixed(1)} km/h`

        const distance = li.querySelector(".distance")
        distance.innerHTML = `${n.distance.toFixed(0)} km`

        const trackClicker = new ButtonClicker(li,
            evt => li, 
            () => Native.doHapticFeedback(), 
            () => Native.onTrackSelected(Number.parseInt(li.dataset.nr))
        )
            
        return li
    })
    
    function fillNext(lisToFill: HTMLLIElement[]) {
        const lis = lisToFill.slice(0, 10)
        lis.forEach(li => trackList.appendChild(li))
        const restLis = lisToFill.slice(10)
        if (restLis.length > 0)
            setTimeout(() => fillNext(restLis), 100)
    }

    fillNext(lis)
}