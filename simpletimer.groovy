definition(
	name: "Simple Timer",
	namespace: "jbeavis19",
	author: "jbeavis19@gmail.com",
	description: "Control On/Run/Off times for an outlet",
	category: "My Apps",
	iconUrl: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health7-icn.png",
	iconX2Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health7-icn.png"
)

import groovy.time.TimeCategory

preferences {
    section("Choose the switch/relay to control")
    	{
		input "outlet", "capability.switch", title: "Outlet?", required: true
        log.debug "outlet is $outlet"
   		}
    section("On Time") 
    	{
    	input "state.ontime", "time", title: "Turn on at?", required: false, defaultValue: null
 		}
    section("Run For") 
    	{
    	input "state.runtime", "number", title: "Run For how many hours?", required: false, defaultValue: null
 		}
    section("Off Time") 
    	{
    	input "state.offtime", "time", title: "Turn off at?", required: false, defaultValue: null
		}
	log.debug "preferences: ontime is ${ontime}, offtime is ${offtime}, runtime is ${runtime}"
}

def installed() {
    log.debug "installed: on time is ${ontime}, off time is ${offtime}, runtime is ${runZtime}"
	unsubscribe()
    unschedule()
    initialize()
}

def updated() {
    log.debug "Updated: on time is ${ontime}, off time is ${offtime}, runtime is ${runtime}"
	unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
	subscribe(outlet, "outlet.on", onevent)
	subscribe(outlet, "outlet.off", offevent)
}

def onevent(){
	log.debug "the outlet is on"
	schedule(delayoff, offevent)
}

def offevent(){        //when the outlet gets turned off clear variables and stop program from running again
log.debug "the outlet is off"
state.ontime = 0
state.runtime = 0
state.offtime = 0
state.delayon = 0
state.delayoff = 0
unschedule()
unsubscribe()
}

/*            //testing outlet control
def sw(){
	outlet.on()
    outlet.off([delay: 5])
}
*/

def timer(){             //logic to check input and determine times for on and off
	state.now = new Date()
	if (ontime == null && offtime == null && runtime == null) {
		log.debug "setup error 1"}
	else if (ontime == null && offtime == null) {
		log.debug "setup error 2"}
	else if (offtime == null && runtime == null) {
		log.debug "setup error 3"}
	else if (offtime <= ontime && runtime == null){
		log.debug "setup error 4"}  
    else if (offtime <= now ){
		log.debug "setup error 5: off time in past"}    
	else if (runtime == null){
    	log.debug "step 6"
       state.delayon = state.ontime - state.now
       state.delayoff = state.offtime - state.now
        log.debug "runtime null: delay on is ${delayon}, delay off is ${delayoff}"
        turnon()
		}
	else if (ontime == null){
    	log.debug "step 7"
		state.delayon = state.offtime - state.runtime
		state.delayoff = state.offtime - now
		log.debug "ontime null: delay on is ${delayon}, delay off is ${delayoff}"
		turnon()
        }
    else if(offtime == null){
    log.debug "step 8"
		state.delayon = state.ontime - now
		state.delayoff = state.ontime + state.runtime
		log.debug "offtime null: delay on is ${delayon}, delay off is ${delayoff}"
		turnon()
        }
    else {log.debug "error 9"}
}

def turnon(){
	schedule(delayon, outlet.on)
}
