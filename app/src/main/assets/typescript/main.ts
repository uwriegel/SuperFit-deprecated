
declare var Native: any

const starter = document.getElementsByClassName('starter')[0] as HTMLElement

const starterClicker = new ButtonClicker(starter, evt => starter, 
    typeof Native != undefined ? () => Native.doHapticFeedback() : null, 
    () => {} )