#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ThreeWire.h>
#include <RtcDS1302.h>

#define RST D2
#define DATA D1
#define CLOCK D0

#define FOODCONTENT A0
#define WATERCONTENT D8

#define LIGHTS D5
#define WATER D6
#define FOOD D7

#define countof(a) (sizeof(a) / sizeof(a[0]))

ThreeWire myWire(DATA, CLOCK, RST); // IO, SCLK, CE
RtcDS1302<ThreeWire> Rtc(myWire);

bool foodIsEmpty = false;
bool waterIsEmpty = false;

int foodContent = 0;
int waterContent = 0;

unsigned long currentMillis, passMillis, interval = 1000;

String all_command, command, dummyString = "";

int arraySize = 500;

String str = "";
String alarm[500], activeAlarm[500], timerst[500];
int StringCount = 0;

const char* ssid = "Foodiee";
const char* password = "castillo";
WiFiServer server(80);

void setup() {
  Serial.begin(57600);
  WiFi.softAP(ssid, password);
  server.begin();
  Serial.println("Access Point started");
  Serial.print("IP address: ");
  Serial.println(WiFi.softAPIP());

  rtcSetup();

  pinMode(FOODCONTENT, INPUT);
  pinMode(WATERCONTENT, INPUT);

  pinMode(FOOD, OUTPUT);
  pinMode(LIGHTS, OUTPUT);
  pinMode(WATER, OUTPUT);

  digitalWrite(FOOD, HIGH);
  digitalWrite(LIGHTS, LOW);
  digitalWrite(WATER, LOW);

  initiateArray();
}

void loop() {
  all_command = "";

  WiFiClient client = server.available();

  waterContent = digitalRead(WATERCONTENT);
  if (millis() - passMillis >= interval) {
    passMillis = millis();
    runClock();
    foodContent = analogRead(FOODCONTENT);
    if (foodContent > 1) {
      foodIsEmpty = true;
    } else {
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

          Serial.println(command);

          int schedule = command.indexOf("SETSCHEDULE:");
          int statusReport = command.indexOf("REPORTSTATUS:");
          int addAlarms = command.indexOf("ALLALARMS:");
          int sendAlarms = command.indexOf("GETALARM:");
          if (sendAlarms == 0) {
            command.replace("GETALARM:", "");
            sendAllAlarmToApp();
          }
          if (addAlarms == 0) {
            initiateArray();
            command.replace("ALLALARMS:", "");
            split("&", command, alarm);
            int index = 0;
            for (String x : alarm) {
              x.trim();
              if (x != NULL && x != "" && x != "0") {
                timerst[index] = x;
                Serial.println(x);
                index++;
              }
            }
          }

          if (schedule == 0) {
            initiateArray();
            command.replace("SETSCHEDULE:", "");
            split("&", command, alarm);
            for (int i = 0; i < StringCount; i++) {
              if (alarm[i] != NULL && alarm[i] != "") {
                addOrChangeSchedule(alarm[i]);
              }
            }
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

void split(String splitter, String content, String arr[]) {
  StringCount = 0;
  while (content.length() > 0) {
    int index = content.indexOf(splitter);
    if (index == -1) { // No space found
      arr[StringCount++] = content;
      break;
    } else {
      arr[StringCount++] = content.substring(0, index);
      content = content.substring(index + 1);
    }
  }
}

void sendCommand() {
  foodContent = analogRead(FOODCONTENT);
  Serial.println(foodContent);
  if (foodContent > 1) {
    foodIsEmpty = true;
  } else {
    foodIsEmpty = false;
  }
  String lights = String(digitalRead(LIGHTS));
  String food = "EMPTY";
  String water = "GOOD";
  if (waterIsEmpty) {
    water = "EMPTY";
  } else {
    water = "GOOD";
  }
  if (foodIsEmpty) {
    food = "EMPTY";
  } else {
    food = "GOOD";
  }
  command = "REPORTSTATUS:,Light:" + lights + ",Food:" + food + ",Water:" + water;
  all_command = command;
}

void initiateArray() {
  for (int i = 0; i < arraySize; i++) {
    timerst[i] = "0";
    alarm[i] = "0";
  }
}

void addOrChangeSchedule(String schedule) {
  for (int i = 0; i < arraySize; i++) {
    if (timerst[i] == "0") {
      timerst[i] = schedule;
      Serial.println("Schedule added: " + schedule);
      break;
    }
  }
}

void sendAllAlarmToApp() {
  command = "";
  command += "ALARM:,";
  for (int i = 0; i < arraySize; i++) {
    if(i < arraySize - 1){
      if (timerst[i] != "0" && timerst[i + 1] != "0") {
        command += timerst[i] + "&";
      }else if(timerst[i] != "0"){
        command += timerst[i];
      }
    }
  }
  all_command = command;
  Serial.println("Sent alarms: " + command);
}

void runClock() {
  RtcDateTime now = Rtc.GetDateTime();

  // printDateTime(now);
  // if (!now.IsValid()) {
  //   Serial.println("RTC lost confidence in the DateTime!");
  // }
}

void printDateTime(const RtcDateTime& dt) {
  char datestring[20];

  snprintf_P(datestring, countof(datestring), PSTR("%02u/%02u/%04u %02u:%02u:%02u"),
             dt.Month(), dt.Day(), dt.Year(),
             dt.Hour(), dt.Minute(), dt.Second());
}

void rtcSetup() {
  Rtc.Begin();

  RtcDateTime compiled = RtcDateTime(__DATE__, __TIME__);
  printDateTime(compiled);
  Serial.println();
  if (!Rtc.IsDateTimeValid()) {
    // Serial.println("RTC lost confidence in the DateTime!");
    // Rtc.SetDateTime(compiled);
  }

  if (Rtc.GetIsWriteProtected()) {
    Serial.println("RTC was write protected, enabling writing now");
    Rtc.SetIsWriteProtected(false);
  }

  if (!Rtc.GetIsRunning()) {
    Serial.println("RTC was not actively running, starting now");
    Rtc.SetIsRunning(true);
  }

  RtcDateTime now = Rtc.GetDateTime();
  if (now < compiled) {
    Serial.println("RTC is older than compile time!  (Updating DateTime)");
    Rtc.SetDateTime(compiled);
  }
}
