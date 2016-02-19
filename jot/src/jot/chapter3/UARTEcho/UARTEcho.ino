int inByte = 0;

void setup() {
  Serial.begin(9600);
}

void loop() {
  while(Serial.available() > 0){
    inByte = Serial.read();
    Serial.write(inByte);
  }
}

