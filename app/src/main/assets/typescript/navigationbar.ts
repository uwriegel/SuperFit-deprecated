
const tracks = document.getElementById("tracks")
const tracksMarker = document.getElementById("tracksMarker")
const OPENED  = "opened"

tracks.onclick = () => {
    Native.doHapticFeedback()

    if (tracksMarker.classList.contains(OPENED))
        tracksMarker.classList.remove(OPENED)
    else
        tracksMarker.classList.add(OPENED)
}
