// #include <Arduino.h>
#include <ESP8266WiFi.h>
#include <WiFiClient.h>

#define FOODCONTENT A0
#define WATERCONTENT D8
#define LIGHTS D5
#define WATER D6
#define FOOD D7

bool foodIsEmpty = false;
bool waterIsEmpty = false;

int foodContent = 0;
int waterContent = 0;

unsigned long currentMillis, passMillis, interval = 1000;

String all_command, command, dummyString = "";

int arraySize = 50;

String str = "";
String strs[1000], strsCopy[1000], timerst[50], alarms[100];
int StringCount = 0;

const char* ssid = "Foodiee";
const char* password = "castillo";
// const char* wifiSsid = "VMW";
// const char* wifiPassword = "VonGeeRhiane042215";
WiFiServer server(80);

void setup() {
  Serial.begin(115200);
  WiFi.softAP(ssid, password);
  server.begin();
  Serial.println("Access Point started");
  Serial.print("IP address: ");
  Serial.println(WiFi.softAPIP());

  // wifiConnection();

  pinMode(FOODCONTENT, INPUT);
  pinMode(WATERCONTENT, INPUT);

  pinMode(FOOD, OUTPUT);
  pinMode(LIGHTS, OUTPUT);
  pinMode(WATER, OUTPUT);

  digitalWrite(FOOD, HIGH);
  digitalWrite(LIGHTS, LOW);
  digitalWrite(WATER, LOW);

  initiateArray();

  printEverySched();
  
}

// void wifiConnection() {
//   WiFi.begin(wifiSsid, wifiPassword); // Connect to the network
//   Serial.print("Connecting to ");
//   Serial.print(wifiSsid);
//   Serial.println(" ...");
//   int i = 0;
//   while (WiFi.status() != WL_CONNECTED) { // Wait for the Wi-Fi to connect
//     delay(1000);
//     Serial.print(++i);
//     Serial.print(' ');
//   }
//   Serial.println('\n');
//   Serial.println("Connection established!");
//   Serial.print("IP address:\t");
//   Serial.println(WiFi.localIP());
// }

void loop() {

  

  // Serial.println(foodContent);
  

  all_command = "";

  WiFiClient client = server.available();

  
  waterContent = digitalRead(WATERCONTENT);
  currentMillis = millis();
  if(currentMillis - passMillis >= interval){
    passMillis = currentMillis;

    foodContent = analogRead(FOODCONTENT);
    Serial.println(foodContent);
    if(foodContent > 1){
      foodIsEmpty = true;
    }else{
      foodIsEmpty = false;
    }
  }

  if (client) {
    String request = "";
    while (client.connected()) {
      
      if (client.available()) {
        char c = client.read();
        request += c;
        if (c == '\r') {
          Serial.println(request); // full HTTP command line including GET and HTTP 1

          // Extract command from request string
          int start = request.indexOf("GET /") + 5;
          int end = request.indexOf("HTTP/");
          if (start < end) {
            command = request.substring(start, end);
            command.trim(); // trim the command separately
          }
          
          int schedule = command.indexOf("SETSCHEDULE:");
          int statusReport = command.indexOf("REPORTSTATUS:");
          int addAlarms = command.indexOf("ALLALARMS:");
          int sendAlarms = command.indexOf("GETALARM:");

          if (schedule == 0) {
            command.replace("SETSCHEDULE:", "");
          }
          // region SEND STATUS ON APP START
          if (statusReport == 0) {
            sendCommand();
          }
          // endregion
          if (addAlarms == 0) {
            if(command.length() < 15){
              initiateArray();
              arrayCleanup();
              alarmCleanup();
              command = "";
            }else{
              initiateArray();
              int count = 0;
              command.replace("ALLALARMS:", "");
              str = command;
              splitCopy("&");
              String temp = "";
              for (String x : strsCopy) {
                if (!(x == "" || x == NULL || x == " ")) {
                  Serial.println(x + "CHOCO");
                  str = x;
                  split(";");
                  addOrChangeSchedule();
                  count++;
                }
              }              
              str = "";
              arrayCleanup();
              alarmCleanup();
            }
          }
          if (sendAlarms == 0) {
            
            sendAllAlarmToApp();
          }

          // // Purify the command
          // command.replace("\n", "");
          // command.replace("\r", "");
          // command.replace(" ", ""); // removes all space characters
          // command.replace("\t", ""); // removes all tab characters
          // command.trim();

          Serial.println(command);

          all_command = command;

          if (command.equals("food") && !foodIsEmpty) {
              digitalWrite(FOOD, LOW);
              delay(10000);
              digitalWrite(FOOD, HIGH);
              sendCommand();
          }else if(foodIsEmpty){

          }
          if (command.equals("light")) {
              digitalWrite(LIGHTS, !digitalRead(LIGHTS));
              sendCommand();
          }
          if (command.equals("water")) {
              digitalWrite(WATER, HIGH);
              bool isTrue = false;
              for(int i = 0;i< 1000;i++){
                int tempo = digitalRead(WATERCONTENT);
                Serial.print(tempo);
                Serial.println("WATER");
                if(tempo > 0){
                  waterIsEmpty = false;
                  isTrue = true;
                  break;
                }else{
                  waterIsEmpty = true;
                }
                delay(10);
              }
              if(isTrue){
                delay(2000);
              }
              digitalWrite(WATER, LOW);
              sendCommand();
          }else if(waterIsEmpty){
            
          }
          
          if(command.equals("refill")){
            if(waterIsEmpty){
              digitalWrite(WATER, HIGH);
              bool isTrue = false;
              for(int i = 0;i< 1000;i++){
                int tempo = digitalRead(WATERCONTENT);
                Serial.print(tempo);
                Serial.println("REFILL");
                if(tempo > 0){
                  waterIsEmpty = false;
                  isTrue = true;
                  break;
                }else{
                  waterIsEmpty = true;
                }
                delay(10);
              }
              digitalWrite(WATER, LOW);
            }
            sendCommand();
          }

          if (client.peek() == '\n') {
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println();
            String commandWithTags = "<html><body>" + all_command + "</body></html>";
            client.println(commandWithTags);
            break;
          }
        }
      }
    }
  }
}

void alarmCleanup() {
  int i = 0;
  for (String x : strs) {
    strs[i] = "0";
    i++;
  }
}

void split(String splitter) {
  StringCount = 0;
  while (str.length() > 0) {
    int index = str.indexOf(splitter);
    if (index == -1) { // No space found
      strs[StringCount++] = str;
      break;
    } else {
      strs[StringCount++] = str.substring(0, index);
      str = str.substring(index + 1);
    }
  }
}

void splitCopy(String splitter) {
  StringCount = 0;
  while (str.length() > 0) {
    int index = str.indexOf(splitter);
    if (index == -1) { // No space found
      strsCopy[StringCount++] = str;
      break;
    } else {
      strsCopy[StringCount++] = str.substring(0, index);
      str = str.substring(index + 1);
    }
  }
}

void sendCommand() {
  foodContent = analogRead(FOODCONTENT);
  Serial.println(foodContent);
  if(foodContent > 1){
    foodIsEmpty = true;
  }else{
    foodIsEmpty = false;
  }
  String lights = String(digitalRead(LIGHTS));
  String food = "EMPTY";
  String water = "GOOD";
  if(waterIsEmpty){
    water = "EMPTY";
  }else{
    water = "GOOD";
  }
  if(foodIsEmpty){
    food = "EMPTY";
  }else{
    food = "GOOD";
  }
  command = "REPORTSTATUS:,Light:" + lights + ",Food:" + food + ",Water:" + water;
  all_command = command;
}

void arrayCleanup(){
  for (int i = 0; i < arraySize; i++) {
    for(int j = 0; j < arraySize; j++){
      if(timerst[i] == timerst[j] && !(i == j) && timerst[j] != "0"){
        timerst[j] = "0";
        Serial.println(timerst[j] + "TRUE");
      }
    }
  }
}

void initiateArray() {
  for (int i = 0; i < arraySize; i++) {
    timerst[i] = "0";
  }
}

void addOrChangeSchedule() {
  for (int i = 0; i < arraySize; i++) {
    if (timerst[i] == "0") {
      String meridiem = "";
      if(strs[2] == "0"){
        meridiem = "AM";
      }else{
        meridiem = "PM";
      }
      timerst[i] = strs[0]+":"+strs[1] + ":" + meridiem+ ":" + strs[4] + ":" + strs[3];
      Serial.println(timerst[i]);
      break;
    }
  }
}

void printEverySched() {
  for (int i = 0; i < arraySize; i++) {
    if (timerst[i] != "0") {
      Serial.println(timerst[i]);
    }
  }
}

void sendAllAlarmToApp() {
  command = "";
  command += "ALARM:,";
  for (int i = 0; i < arraySize; i++) {
    if (timerst[i] != "0" ) {
      command += timerst[i] + ">";
    }
  }
  all_command = command;
  Serial.println(command + "NEGRO");
}

void removeSched(String sched) {
  for (int i = 0; i < arraySize; i++) {
    if (timerst[i] == sched) {
      timerst[i] = "0";
    }
  }
}