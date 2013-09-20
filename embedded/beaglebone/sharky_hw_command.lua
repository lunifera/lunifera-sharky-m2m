-- Lua application for controlling the hardware aspects
-- of the "Flying Shark" demo.
-- Intended to run in a Mihini environment on a BeagleBone Black board.



-- load libraries
local sched = require("sched")
local socket = require("socket")
local MQTT = require("mqtt_library")

-- hardware addresses
UP_PIN = 68
DOWN_PIN = 67
RIGHT_PIN = 44
LEFT_PIN = 26

-- MQTT settings
MQTT_SERVER_URL = "192.168.178.28"
MQTT_SERVER_PORT = "1883"
MQTT_CLIENT_ID = "111"
MQTT_PUBLISH_TOPIC = "sharky_sensors"
MQTT_RECEIVE_TOPIC = "sharky_commands"
MQTT_DISTANCE_TOPIC = "sharky_alarmfence"
MQTT_VALUE_SEPARATOR = ":"

-- constants for possible movements
MAX_PITCH = 5
MAX_SPEED = 5
MAX_ROTATION = 5
FIN_MOVEMENT = 500
FIN_PAUSE = 250
UPDOWN_MOVEMENT = 300
ROTATION_AMOUNT = FIN_MOVEMENT * 0.2

-- global variables for time scheduling
now = socket.gettime()*1000
reqint = 1000 -- interval for position checks
reqtim = now + reqint -- next check due
udpos = 0 -- position of weight
udtarget = 0 -- target of weight
uddur = UPDOWN_MOVEMENT -- duration of weight movement
udtim = now + uddur -- next time for up/down movement change
fwpause = FIN_PAUSE -- pause after left/right fin movement combination
ltdur = FIN_MOVEMENT -- duration of fin movements
rtdur = ltdur
speedcoeff = 1 -- coefficient for fin movement duration
fwdir = "l" -- direction of next fin movement ((l)eft, (r)ight, (n)one)
fwstat = 0 -- status of movement
fwtim = now + fwpause -- time for next fin movement change
rotation = 0 -- current direction change
leftcoeff = 1 -- coefficient for left turn
rightcoeff = 1 -- coefficient for right turn
running = true -- status of application
alarmfence = -1 -- distance for SHARK ALARM; -1 to deactivate



local function main()
  -- setup GPIOs
  print("Enabling Up-GPIO: "..UP_PIN)
  cmd = "sudo /opt/mihini/gpioconf "..UP_PIN
  os.execute(cmd)
  print("Enabling Down-GPIO: "..DOWN_PIN)
  cmd = "sudo /opt/mihini/gpioconf "..DOWN_PIN
  os.execute(cmd)
  print("Enabling Right-GPIO: "..RIGHT_PIN)
  cmd = "sudo /opt/mihini/gpioconf "..RIGHT_PIN
  os.execute(cmd)
  print("Enabling Left-GPIO: "..LEFT_PIN)
  cmd = "sudo /opt/mihini/gpioconf "..LEFT_PIN
  os.execute(cmd)

  -- setup UART for serial communication with Arduino
  os.execute("sudo /opt/mihini/uarton")


  -- setup and connect MQTT client
  --MQTT.Utility.set_debug(true)
  mqtt_client = MQTT.client.create(MQTT_SERVER_URL, MQTT_SERVER_PORT, callback)
  mqtt_client:connect(MQTT_CLIENT_ID)
  mqtt_client:subscribe({ MQTT_RECEIVE_TOPIC })
  mqtt_client:subscribe({ MQTT_DISTANCE_TOPIC })

  -- a tiny scheduler to keep track of up/down and left/right movements at the same time
  while running == true do

    -- check for incoming commands
    mqtt_client:handler()

    -- perform actions that are due
    now = socket.gettime()*1000
    if now > reqtim then request_handler() end
    if now > udtim then ud_handler() end
    if now > fwtim then fw_handler() end
  end

  print("Shutting down.")
  cmd = "sudo /opt/mihini/gpiorm "..UP_PIN
  os.execute(cmd)
  cmd = "sudo /opt/mihini/gpiorm "..DOWN_PIN
  os.execute(cmd)
  cmd = "sudo /opt/mihini/gpiorm "..RIGHT_PIN
  os.execute(cmd)
  cmd = "sudo /opt/mihini/gpiorm "..LEFT_PIN
  os.execute(cmd)
end



function callback(
topic,    -- string
message)  -- string

  print("Received: " .. topic .. ", message: '" .. message .. "'")

  command, value = string.match(message,"(%a+)%s*"..MQTT_VALUE_SEPARATOR.."%s*(-?%d+)",init)

  print("command: ", command)
  print("value: ", value)

  if (command == "request") then reqest_handler() end
  if (command == "stop") then emergency_stop() end
  if (command == "quit") then running = false end

  value = tonumber(value) -- convert value to integer for commands that take quantifiers

  if (command == "pitch") then ud_changer(value) end
  if (command == "speed") then speed_changer(value) end
  if (command == "rotation") then rotation_changer(value) end
  if (command == "distance") then alarmfence_changer(value) end

end



function fw_handler()
  if fwstat == 1 then
    if fwdir == "l" then
      print("left "..ltdur)
      cmd = "sudo /opt/mihini/gpiolow "..RIGHT_PIN
      os.execute(cmd)
      cmd = "sudo /opt/mihini/gpiohigh "..LEFT_PIN
      os.execute(cmd)
      fwdir = "r"
      fwtim = now + ltdur
    elseif fwdir == "r" then
      print("right "..rtdur)
      cmd = "sudo /opt/mihini/gpiohigh "..RIGHT_PIN
      os.execute(cmd)
      cmd = "sudo /opt/mihini/gpiolow "..LEFT_PIN
      os.execute(cmd)
      fwdir = "n"
      fwtim = now + rtdur
    elseif fwdir == "n" then
      cmd = "sudo /opt/mihini/gpiolow "..LEFT_PIN
      os.execute(cmd)
      cmd = "sudo /opt/mihini/gpiolow "..RIGHT_PIN
      os.execute(cmd)
      print("neutral "..fwpause)
      fwdir = "l"
      fwtim = now + fwpause
    end
  elseif fwstat == 0 then
    fwtim = now + fwpause
  else print("Illegal forward status")
  end
end



function ud_handler()
  if udpos == udtarget then
    print("not changing pitch")
    uddir = "n"
    cmd = "sudo /opt/mihini/gpiolow "..DOWN_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiolow "..UP_PIN
    os.execute(cmd)
    udtim = now + uddur
  elseif udtarget > udpos then
    uddir = "u"
  elseif udtarget < udpos then
    uddir = "d"
  end
  if uddir == "u" then
    print("up")
    cmd = "sudo /opt/mihini/gpiolow "..DOWN_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiohigh "..UP_PIN
    os.execute(cmd)
    udpos = udpos + 1
    udtim = now + uddur
  elseif uddir == "d" then
    print("down")
    cmd = "sudo /opt/mihini/gpiolow "..UP_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiohigh "..DOWN_PIN
    os.execute(cmd)
    udpos = udpos - 1
    udtim = now + uddur * 1.2 -- motor runs slower in back direction
  end
end



function request_handler()
  if alarmfence == -1 then return end  -- do nothing if SHARK ALARM is deactivated
  
  wserial = io.open("/dev/ttyO4", "w")
  wserial:write("p")
  wserial:flush()
  rserial = io.open("/dev/ttyO4","r")
  line = ""
  repeat
    line = rserial:read()
    rserial:flush()
  until line ~= ""

  print(line)

  a, b, c = string.match(line,"(%d+):(%d+):(%d+)")
  a, b, c = tonumber(a), tonumber(b), tonumber(c)
  
  if (a < alarmfence) or (b < alarmfence) or (c < alarmfence) then
    print("SHARK ALARM!")
    mqtt_client:publish(MQTT_PUBLISH_TOPIC, line)
    mqtt_client:publish(MQTT_PUBLISH_TOPIC, "*** SHARK ALARM ***")
  end

  reqtim = now + reqint
end



function ud_changer(value)
  if value < -MAX_PITCH or value > MAX_PITCH then
    print("Illegal pitch command")
    return
  end
  udtarget = value;
end



function speed_changer(value)
  if value > MAX_SPEED then
    print("Illegal speed command")
    return
  end
  if value == 0 then
    fwstat = 0
    cmd = "sudo /opt/mihini/gpiolow "..RIGHT_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiolow "..LEFT_PIN
    os.execute(cmd)
    ltdur, rtdur = FIN_MOVEMENT
    fwpause = FIN_PAUSE
  elseif value == 1 then
    fwstat = 1
    speedcoeff = 0.6
    fwpause = FIN_PAUSE * 3
  elseif value == 2 then
    fwstat = 1
    speedcoeff = 0.7
    fwpause = FIN_PAUSE * 2
  elseif value == 3 then
    fwstat = 1
    speedcoeff = 0.8
    fwpause = FIN_PAUSE * 1.5
  elseif value == 4 then
    fwstat = 1
    speedcoeff = 0.9
    fwpause = FIN_PAUSE
  elseif value == 5 then
    fwstat = 1
    speedcoeff = 1
    fwpause = FIN_PAUSE * 0.5
  end
  ltdur = FIN_MOVEMENT * speedcoeff * leftcoeff
  rtdur = FIN_MOVEMENT * speedcoeff * rightcoeff
end



function rotation_changer(value)
  if value < -MAX_ROTATION or value > MAX_ROTATION then
    print("Illegal rotation command")
    return
  end
  if value == rotation then return
    print("No rotation change")
  end
  if value == 0 then
    leftcoeff, rightcoeff = 1
  elseif value < 0 then
    leftcoeff = 1 - 0.2 * value
    rightcoeff = 1
  elseif value > 0 then
    leftcoeff = 1
    rightcoeff = 1 + 0.2 * value
  end
  ltdur = FIN_MOVEMENT * speedcoeff * leftcoeff
  rtdur = FIN_MOVEMENT * speedcoeff * rightcoeff
end



function alarmfence_changer(value)
  alarmfence = value
  print("changed alarmfence")
end




function emergency_stop()
  speed_changer(0)
  ud_changer(0)
  rotation_changer(0)
  cmd = "sudo /opt/mihini/gpiolow "..DOWN_PIN
  os.execute(cmd)
  cmd = "sudo /opt/mihini/gpiolow "..UP_PIN
  os.execute(cmd)
  cmd = "sudo /opt/mihini/gpiolow "..RIGHT_PIN
  os.execute(cmd)
  cmd = "sudo /opt/mihini/gpiolow "..LEFT_PIN
  os.execute(cmd)

end



-- start application
sched.run(main)
sched.loop()
