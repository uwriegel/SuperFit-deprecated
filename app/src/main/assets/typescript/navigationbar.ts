
declare class TrackData {
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

        return li
    })
    lis.forEach(li => trackList.appendChild(li))
}