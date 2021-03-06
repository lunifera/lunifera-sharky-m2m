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
ALARM_PIN = 66

-- MQTT settings
MQTT_SERVER_URL = "10.0.0.40"
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
FIN_MOVEMENT = 700
FIN_PAUSE = 250
UPDOWN_MOVEMENT = 600
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
rotation = 0 -- current direction change (relative value)
direction = 0 -- direction (absolute value)
glidingturn = false -- track different steering commands in gliding mode
resettim = now + ROTATION_AMOUNT -- time for resetting fin in gliding mode
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
  print("Enabling Alarm-GPIO: "..ALARM_PIN)
  cmd = "sudo /opt/mihini/gpioconf "..ALARM_PIN
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
    if glidingturn == true then
      if now > resettim then reset_glideturn() end
    end
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
  cmd = "sudo /opt/mihini/gpiorm "..ALARM_PIN
  os.execute(cmd)
end



function callback(
topic,    -- string
message)  -- string

  print(">>> Received: " .. topic .. ", message: '" .. message .. "'")

  command, value = string.match(message,"(%a+)%s*"..MQTT_VALUE_SEPARATOR.."%s*(-?%d+)",init)

  if command == "request" then reqest_handler() end
  if command == "stop" then emergency_stop() end
  if command == "quit" then running = false end

  value = tonumber(value) -- convert value to integer for commands that take quantifiers

  if command == "pitch" then ud_changer(-value) end
  if command == "speed" then speed_changer(value) end
  if command == "rotation" then
    if fwstat == 0 then
      glidingturn = true
      glide_rotate(value)
    else
      rotation_changer(value)
    end
  end
  if command == "direction" then
    if fwstat == 0 and (value < -2 or value > 2) then
      glidingturn = true
      glide_rotate(value)
    elseif fwstat == 1 then
      direction_changer(value)
    end
  end
  if (command == "distance") then alarmfence_changer(value) end

end



function fw_handler()
  if fwstat == 1 then
    if fwdir == "l" then
      print("\t\t\t\t\t\t\t\tleft "..ltdur)
      cmd = "sudo /opt/mihini/gpiolow "..RIGHT_PIN
      os.execute(cmd)
      cmd = "sudo /opt/mihini/gpiohigh "..LEFT_PIN
      os.execute(cmd)
      fwdir = "r"
      fwtim = now + ltdur
    elseif fwdir == "r" then
      print("\t\t\t\t\t\t\t\tright "..rtdur)
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
      print("\t\t\t\t\t\t\t\tneutral "..fwpause)
      fwdir = "l"
      fwtim = now + fwpause
    end
    glidingturn = false
  elseif fwstat == 0 then
    fwtim = now + fwpause
  else print("Illegal forward status")
  end
end



function ud_handler()
  if udpos == udtarget then
    print("\t\t\t\tnot changing pitch")
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
    print("\t\t\t\tup")
    cmd = "sudo /opt/mihini/gpiolow "..DOWN_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiohigh "..UP_PIN
    os.execute(cmd)
    udpos = udpos + 1
    udtim = now + uddur
  elseif uddir == "d" then
    print("\t\t\t\tdown")
    cmd = "sudo /opt/mihini/gpiolow "..UP_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiohigh "..DOWN_PIN
    os.execute(cmd)
    udpos = udpos - 1
    udtim = now + uddur * 1 -- motor runs slower in back direction
  end
end



function request_handler()
  if alarmfence < 1 then return end  -- do nothing if SHARK ALARM is deactivated

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

  if (a > 0 and a < alarmfence) or (b > 0 and b < alarmfence) or (c > 0 and c < alarmfence) then
    print("SHARK ALARM! "..line)
    cmd = "sudo /opt/mihini/gpiohigh "..ALARM_PIN
    os.execute(cmd)
    mqtt_client:publish(MQTT_PUBLISH_TOPIC, "*** SHARK ALARM ***")
  else
    cmd = "sudo /opt/mihini/gpiolow "..ALARM_PIN
    os.execute(cmd)
  end

  reqtim = now + reqint
end



function ud_changer(value)
  if value < -MAX_PITCH or value > MAX_PITCH then
    print("!!! Illegal pitch command")
    return
  end
  udtarget = value;
end



function speed_changer(value)
  if value > MAX_SPEED or value < 0 then
    print("!!! Illegal speed command")
    return
  end
  if value == 0 then
    fwstat = 0
    cmd = "sudo /opt/mihini/gpiolow "..RIGHT_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiolow "..LEFT_PIN
    os.execute(cmd)
    ltdur, rtdur = FIN_MOVEMENT
    rightcoeff = 1
    leftcoeff = 1
    fwpause = FIN_PAUSE
  elseif value == 1 then
    fwstat = 1
    speedcoeff = 0.5
    fwpause = FIN_PAUSE * 1.5
  elseif value == 2 then
    fwstat = 1
    speedcoeff = 0.6
    fwpause = FIN_PAUSE * 1
  elseif value == 3 then
    fwstat = 1
    speedcoeff = 0.7
    fwpause = FIN_PAUSE * 0.6
  elseif value == 4 then
    fwstat = 1
    speedcoeff = 0.8
    fwpause = FIN_PAUSE * 0.2
  elseif value == 5 then
    fwstat = 1
    speedcoeff = 0.8
    fwpause = FIN_PAUSE * 0.0
  end
  ltdur = FIN_MOVEMENT * speedcoeff * leftcoeff
  rtdur = FIN_MOVEMENT * speedcoeff * rightcoeff
end



function rotation_changer(value)
  rotation = rotation + value
  if rotation < -5 then rotation = -5 end
  if rotation > 5 then rotation = 5 end
  if rotation == 0 then
    leftcoeff = 1
    rightcoeff = 1
  elseif rotation < 0 then
    leftcoeff = 1 - 0.2 * rotation
    rightcoeff = 1
  elseif rotation > 0 then
    leftcoeff = 1
    rightcoeff = 1 + 0.2 * rotation
  end
  ltdur = FIN_MOVEMENT * speedcoeff * leftcoeff
  rtdur = FIN_MOVEMENT * speedcoeff * rightcoeff
end



function direction_changer(value)
  if direction < -5 then direction = -5 end
  if direction > 5 then direction = 5 end
  direction = value
  if direction == 0 then
    leftcoeff = 1
    rightcoeff = 1
  elseif direction < 0 then
    leftcoeff = 1 - 0.2 * direction
    rightcoeff = 1
  elseif direction > 0 then
    leftcoeff = 1
    rightcoeff = 1 + 0.2 * direction
  end
  ltdur = FIN_MOVEMENT * speedcoeff * leftcoeff
  rtdur = FIN_MOVEMENT * speedcoeff * rightcoeff
end



function alarmfence_changer(value)
  alarmfence = value
  print("changed alarmfence")
end



function glide_rotate(value)
  print ("--- GLIDEROTATE")
  if value > 0 then
    cmd = "sudo /opt/mihini/gpiohigh "..RIGHT_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiolow "..LEFT_PIN
    os.execute(cmd)
  elseif value < 0 then
    cmd = "sudo /opt/mihini/gpiolow "..RIGHT_PIN
    os.execute(cmd)
    cmd = "sudo /opt/mihini/gpiohigh "..LEFT_PIN
    os.execute(cmd)
  end
  resettim = now + 3 * ROTATION_AMOUNT
end



function reset_glideturn()
  cmd = "sudo /opt/mihini/gpiolow "..RIGHT_PIN
  os.execute(cmd)
  cmd = "sudo /opt/mihini/gpiolow "..LEFT_PIN
  os.execute(cmd)
  print("--- RESET GLIDEROTATE")
  glidingturn = false
end



function emergency_stop()
  speed_changer(0)
  ud_changer(0)
  rotation = 0
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
