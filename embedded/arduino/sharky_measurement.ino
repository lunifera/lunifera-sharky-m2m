const int cmdPin = 13; // pin for requesting measurement

const int pingPin1 = 47; // pins connected to the sensors
const int pingPin2 = 45;
const int pingPin3 = 43;
const int pingPin4 = 41;


long duration, cm1, cm2, cm3, cm4;



void setup() {
  // initialise serial communication
  Serial.begin(9600);
  
  // set up interrupt on request pin 
  attachInterrupt(cmdPin, ping, RISING); // call ping function on request
}



void loop()
{
  // do nothing
}



long microsecondsToCentimeters(long microseconds)
{
  // speed of sound is 29 microseconds per centimetre
  return microseconds / 29 / 2;
}



void ping()
{
  // probe sensor 1 
  pinMode(pingPin1, OUTPUT);
  digitalWrite(pingPin1, LOW);
  delayMicroseconds(2);
  digitalWrite(pingPin1, HIGH);
  delayMicroseconds(5);
  digitalWrite(pingPin1, LOW);

  // catch echo 1
  pinMode(pingPin1, INPUT);
  duration = pulseIn(pingPin1, HIGH);
  cm1 = microsecondsToCentimeters(duration);

  // probe sensor 2 
  pinMode(pingPin2, OUTPUT);
  digitalWrite(pingPin2, LOW);
  delayMicroseconds(2);
  digitalWrite(pingPin2, HIGH);
  delayMicroseconds(5);
  digitalWrite(pingPin2, LOW);

  // catch echo 2
  pinMode(pingPin2, INPUT);
  duration = pulseIn(pingPin2, HIGH);
  cm2 = microsecondsToCentimeters(duration);
 
  // probe sensor 3
  pinMode(pingPin3, OUTPUT);
  digitalWrite(pingPin3, LOW);
  delayMicroseconds(2);
  digitalWrite(pingPin3, HIGH);
  delayMicroseconds(5);
  digitalWrite(pingPin3, LOW);

  // catch echo 3
  pinMode(pingPin3, INPUT);
  duration = pulseIn(pingPin3, HIGH);
  cm3 = microsecondsToCentimeters(duration);
  
  // probe sensor 4
  pinMode(pingPin4, OUTPUT);
  digitalWrite(pingPin4, LOW);
  delayMicroseconds(2);
  digitalWrite(pingPin4, HIGH);
  delayMicroseconds(5);
  digitalWrite(pingPin4, LOW);

  // catch echo 4
  pinMode(pingPin4, INPUT);
  duration = pulseIn(pingPin4, HIGH);
  cm4 = microsecondsToCentimeters(duration);
 
  // output result on serial 
  Serial.print(cm1);
  Serial.print(",");
  Serial.print(cm2);
  Serial.print(",");
  Serial.print(cm3);  
  Serial.print(",");
  Serial.print(cm4);
  Serial.println();  
}

