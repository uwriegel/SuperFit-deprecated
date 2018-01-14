
class ButtonClicker {

    constructor(private clickableElement: HTMLElement, 
        private getClicked: (evt: MouseEvent) => HTMLElement, 
        private feedback: () => void,
        private onClick: () => void) {
        clickableElement.onclick = evt => this.beginClick(evt)
    }

    private beginClick(evt: MouseEvent) {
        if (this.inClick)
            return
        const clickedElement = this.getClicked(evt)
        if (!clickedElement)
            return
        this.inClick = true
        if (this.feedback)
            this.feedback()
        this.animateClick(clickedElement, evt)
    }

    private animateClick(clickedElement: HTMLElement, evt: MouseEvent) {
        const canvas = document.createElement("canvas")
        canvas.width = clickedElement.offsetWidth
        canvas.height = clickedElement.offsetHeight
        const context = canvas.getContext("2d")
        let lastindex = 1
        const dateNow = new Date().getTime()
        const x = evt.clientX - clickedElement.offsetLeft
        
        // TODO: in scroll
        const y = evt.pageY - clickedElement.offsetTop // - theScroll.y 
        const centerX = x
        const centerY = y
        let actionExecuted = false
        
        const drawCircle = (index: number) => {
            const alpha = index / 10
            if (!actionExecuted && alpha > 0.6) {
                this.onClick()
                actionExecuted = true
            }
            if (alpha > 1) {
                this.inClick = false
                clickedElement.style.background = ""
                return false
            }
            const radius = (canvas.height / 2 - 6) + alpha * (canvas.width / 2 - (canvas.height / 2 - 6))

            context.fillStyle = getComputedStyle(clickedElement, null).getPropertyValue('background-color')
            context.fillRect(0, 0, canvas.width, canvas.height)
            
            context.beginPath()
            context.arc(centerX, centerY, radius, 0, 2 * Math.PI, false)
            context.fillStyle  = this.clickedColor
            context.globalAlpha = 1 - alpha
            context.fill()
            var url = canvas.toDataURL()
            clickedElement.style.background = `url(${url})`
            return true
        }

        const animate = () => {
            var date = new Date().getTime()
            var index = Math.round((date - dateNow) / 40)
            if (index == lastindex) {
                window.requestAnimationFrame(animate)
                return
            }
            lastindex = index
            if (!drawCircle(index))
                return
            window.requestAnimationFrame(animate)
        }

        requestAnimationFrame(animate)
    }

    private readonly htmlStyles = window.getComputedStyle(document.querySelector("html"));
    private readonly clickedColor = this.htmlStyles.getPropertyValue('--button-clicked'); 
    private readonly backgroundColor = this.clickableElement.style.backgroundColor
    private inClick = false
}


