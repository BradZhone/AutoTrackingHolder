/*************************************************************
  目标跟踪云台(as server)
  by Brad Z
  ESP8266 with 0.96inch OLED 引脚
      VCC    <--->    VCC
      GND    <--->    GND
     D3(0)   <--->    SDA  
     D4(2)   <--->    SCL         
  
  ESP8266 with servo1,2 引脚
      VCC    <--->    VCC
      GND    <--->    GND
     D5(14)  <--->    servo1(x)  
     D6(12)  <--->    servo2(y)
     
  ESP8266 with buzzer 引脚
      D1(5)  <--->    VCC
      GND    <--->    GND
 *************************************************************/
#include <Servo.h>
#include <ESP8266WiFi.h>
#include <U8g2lib.h>
#include <Wire.h>
#define LED 2
#define servo1 14
#define servo2 12
#define scl 0
#define sda 2
#define buzzer 5
#define D1 262
#define D3 329
#define D4 349
#define D6 440
#define M1 523

//esp8266做服务器
const char *ssid = "ATH";  // 设定的wifi名称
const char *password = "bradzhone";  // wifi密码
const int port = 8266;  // 端口号
WiFiServer server(port);
IPAddress softLocal(192,168,1,1);  // IP地址
IPAddress softGateway(192,168,1,1);
IPAddress softSubnet(255,255,255,0);
String myIP = "192.168.1.1";

/*********************OLED相关部分*************************/
U8G2_SSD1306_128X64_NONAME_F_HW_I2C u8g2(U8G2_R0, /* reset=*/ U8X8_PIN_NONE, /* clock=*/ scl, /* data=*/ sda);
static const unsigned char logo[] U8X8_PROGMEM = {
0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xE0,0x0F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
0x00,0x00,0x00,0x00,0x00,0x00,0xE0,0xFF,0x0F,0x06,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFF,0xFF,0xFF,0xFF,0x83,0x1F,0x00,0x00,0x00,0x00,0x00,0x00,
0x00,0x00,0x00,0xF0,0xFF,0xFF,0xFF,0xFF,0xC0,0x7F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x80,0xFF,0xFF,0x3C,0xFF,0x0F,0x80,0xFF,0x00,0x00,0x00,0x00,0x00,0x00,
0x00,0x00,0xE0,0xFF,0xE1,0x00,0xFF,0x03,0x00,0xFF,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xF0,0xFF,0x77,0xC0,0x7F,0x00,0x00,0xFE,0x07,0x00,0x00,0x00,0x00,0x20,
0x00,0x00,0xFE,0xC1,0x01,0xF8,0x7F,0x00,0x00,0xFE,0x1F,0x00,0x00,0x00,0x00,0x38,0x00,0x80,0x3F,0x00,0x00,0xFC,0x7F,0x00,0x00,0xFF,0x1F,0x00,0x00,0x00,0x00,0x1E,
0x00,0xC0,0x0F,0x00,0x00,0xFC,0xFF,0x00,0xC0,0xFF,0x3F,0x00,0x00,0x00,0x00,0x0F,0x00,0xE0,0x07,0x00,0x80,0x7F,0xFC,0x03,0xF0,0xFF,0x77,0x00,0x00,0x00,0xC0,0x07,
0x00,0xF8,0x03,0x00,0xF0,0x1F,0xF0,0x1F,0xF8,0x01,0xF1,0x01,0x00,0x00,0xF0,0x00,0x80,0x7F,0x00,0x00,0xFC,0x0F,0x00,0x3F,0x3F,0x00,0xF0,0x03,0x00,0x00,0x78,0x00,
0xC0,0x07,0x00,0x00,0xFE,0x07,0x00,0xFE,0x3F,0x00,0xF8,0x0F,0x00,0x00,0x3F,0x00,0xE0,0x01,0x00,0x00,0xFF,0x07,0x00,0xFE,0x1F,0x00,0xF8,0x0F,0x00,0x80,0x1F,0x00,
0xF8,0x00,0x00,0x00,0xFF,0x07,0x00,0xFE,0x3F,0x00,0xFF,0x07,0x00,0xC0,0x07,0x00,0x7E,0x00,0x00,0x00,0xE4,0x0F,0xC0,0xFF,0xFF,0xC0,0xFF,0x00,0x00,0xFF,0x03,0x00,
0x1F,0x00,0x00,0x00,0x00,0x7F,0xE0,0x1F,0xEF,0xF3,0x3F,0x00,0xE0,0xFF,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFE,0xF0,0x07,0xE4,0xFF,0x07,0x00,0xF8,0x3F,0x00,0x00,
0x00,0x00,0x00,0x00,0x00,0xF0,0xFF,0x01,0xC0,0xFF,0x01,0xC0,0xFF,0x03,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFF,0x01,0xE0,0x7F,0x00,0xF8,0x7F,0x00,0x00,0x00,
0x00,0x00,0x00,0x00,0x00,0x00,0xFE,0x03,0xFF,0x7F,0xF8,0xFF,0x1F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xF0,0x03,0xFF,0xFF,0xFF,0x1F,0x00,0x00,0x00,0x00,
0x00,0x00,0x00,0x00,0x00,0x00,0x80,0x03,0xFF,0xFF,0xFF,0x0F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFE,0xFF,0x0F,0x00,0x00,0x00,0x00,0x00,
0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x08,0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/* (128 X 28 )*/
};

static const unsigned char connectedLogo[] U8X8_PROGMEM = 
{
0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFC,0x3F,0x00,0x00,0x00,0x80,0xFF,0xFF,0x01,0x00,0x00,0xE0,0xFF,0xFF,0x07,0x00,0x00,0xF0,
0xFF,0xFF,0x0F,0x00,0x00,0xFC,0xFF,0xFF,0x3F,0x00,0x00,0xFE,0xFF,0xFF,0x7F,0x00,0x00,0xFF,0xFF,0xFF,0xFF,0x00,0x80,0xFF,0xFF,0xFF,0xFF,0x01,0xC0,0xFF,0xFF,0xFF,
0xFF,0x03,0xC0,0xFF,0xFF,0xFF,0xFF,0x03,0xE0,0xFF,0xFF,0xFF,0xFF,0x07,0xF0,0xFF,0xFF,0xFF,0xFF,0x0F,0xF0,0xFF,0xFF,0xFF,0xFF,0x0F,0xF8,0xFF,0xFF,0xFF,0xFF,0x1F,
0xF8,0xFF,0xFF,0xFF,0xFF,0x1C,0xF8,0xFF,0xFF,0xFF,0x7F,0x1F,0xFC,0xFF,0xFF,0xFF,0x9F,0x3F,0xFC,0xFF,0xFF,0xFF,0xC7,0x3F,0xFC,0xFF,0xFF,0xFF,0xF1,0x3F,0xFC,0xFF,
0xFF,0x7F,0xF8,0x3F,0xFC,0xFF,0xFF,0x1F,0xFC,0x3F,0xFC,0xFF,0xFF,0x07,0xFF,0x3F,0xFC,0xFD,0xFF,0x83,0xFF,0x3F,0xFC,0xF1,0xFF,0xC0,0xFF,0x3F,0xFC,0xE3,0x3F,0xF0,
0xFF,0x3F,0xFC,0xC7,0x0F,0xF8,0xFF,0x3F,0xFC,0x07,0x03,0xFC,0xFF,0x3F,0xFC,0x0F,0x00,0xFF,0xFF,0x3F,0xF8,0x1F,0x80,0xFF,0xFF,0x1F,0xF8,0x1F,0xC0,0xFF,0xFF,0x1F,
0xF8,0x3F,0xF0,0xFF,0xFF,0x0F,0xF0,0x7F,0xF8,0xFF,0xFF,0x0F,0xF0,0x7F,0xFC,0xFF,0xFF,0x0F,0xE0,0xFF,0xFF,0xFF,0xFF,0x07,0xC0,0xFF,0xFF,0xFF,0xFF,0x03,0xC0,0xFF,
0xFF,0xFF,0xFF,0x03,0x80,0xFF,0xFF,0xFF,0xFF,0x01,0x00,0xFF,0xFF,0xFF,0xFF,0x00,0x00,0xFE,0xFF,0xFF,0x7F,0x00,0x00,0xFC,0xFF,0xFF,0x3F,0x00,0x00,0xF0,0xFF,0xFF,
0x0F,0x00,0x00,0xE0,0xFF,0xFF,0x07,0x00,0x00,0x80,0xFF,0xFF,0x00,0x00,0x00,0x00,0xFC,0x3F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/* (48 X 48 )*/
};

static const unsigned char disconnectedLogo[] U8X8_PROGMEM = 
{
0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFC,0x3F,0x00,0x00,0x00,0x80,0xFF,0xFF,0x01,0x00,0x00,0xE0,0xFF,0xFF,0x07,0x00,0x00,0xF0,
0xFF,0xFF,0x0F,0x00,0x00,0xFC,0xFF,0xFF,0x3F,0x00,0x00,0xFE,0xFF,0xFF,0x7F,0x00,0x00,0xFF,0xFF,0xFF,0xFF,0x00,0x80,0xFF,0xFF,0xFF,0xFF,0x01,0xC0,0xFF,0xFF,0xFF,
0xFF,0x03,0xC0,0xFF,0xFF,0xFF,0xFF,0x03,0xE0,0xBF,0xFF,0xFF,0xFD,0x07,0xF0,0x1F,0xFF,0xFF,0xF8,0x0F,0xF0,0x0F,0xFE,0x7F,0xF0,0x0F,0xF8,0x1F,0xFC,0x3F,0xF8,0x1F,
0xF8,0x3F,0xF8,0x1F,0xFC,0x1F,0xF8,0x7F,0xF0,0x0F,0xFE,0x1F,0xFC,0xFF,0xE0,0x07,0xFF,0x1F,0xFC,0xFF,0xC1,0x83,0xFF,0x3F,0xFC,0xFF,0x83,0xC1,0xFF,0x3F,0xFC,0xFF,
0x07,0xE0,0xFF,0x3F,0xFC,0xFF,0x0F,0xF0,0xFF,0x3F,0xFC,0xFF,0x1F,0xF8,0xFF,0x3F,0xFC,0xFF,0x1F,0xF8,0xFF,0x3F,0xFC,0xFF,0x0F,0xF0,0xFF,0x3F,0xFC,0xFF,0x07,0xE0,
0xFF,0x3F,0xFC,0xFF,0x83,0xC1,0xFF,0x3F,0xFC,0xFF,0xC1,0x83,0xFF,0x3F,0xFC,0xFF,0xE0,0x07,0xFF,0x1F,0xF8,0x7F,0xF0,0x0F,0xFE,0x1F,0xF8,0x3F,0xF8,0x1F,0xFC,0x1F,
0xF8,0x1F,0xFC,0x3F,0xF8,0x1F,0xF0,0x0F,0xFE,0x7F,0xF0,0x0F,0xF0,0x1F,0xFF,0xFF,0xF8,0x0F,0xE0,0xBF,0xFF,0xFF,0xFD,0x07,0xC0,0xFF,0xFF,0xFF,0xFF,0x03,0xC0,0xFF,
0xFF,0xFF,0xFF,0x03,0x80,0xFF,0xFF,0xFF,0xFF,0x01,0x00,0xFF,0xFF,0xFF,0xFF,0x00,0x00,0xFE,0xFF,0xFF,0x7F,0x00,0x00,0xFC,0xFF,0xFF,0x3F,0x00,0x00,0xF0,0xFF,0xFF,
0x0F,0x00,0x00,0xE0,0xFF,0xFF,0x07,0x00,0x00,0x80,0xFF,0xFF,0x01,0x00,0x00,0x00,0xF8,0x1F,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/* (48 X 48 )*/
};

static const unsigned char wifiLogo[] U8X8_PROGMEM = 
{
0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x80,0x1F,0x00,0xE0,0x7F,0x00,0xF0,0xFF,0x00,0x78,0xE0,0x01,0x1C,0x8F,0x03,0xC8,0x3F,0x01,0xE0,0x7F,0x00,0xC0,0x30,
0x00,0x00,0x0F,0x00,0x80,0x1F,0x00,0x00,0x09,0x00,0x00,0x00,0x00,0x00,0x06,0x00,0x00,0x06,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/* (20 X 20 )*/
};

/*********************OLED相关部分 end*********************/

/*********************蜂鸣器相关部分***********************/
int length1;
int length2;
int connectedTune[]=  //连接成功音调
{
  D4,D6,M1,
};

int disconnectedTune[]=  //连接失败音调
{
  M1,D3,D1
};

float durt[]=  //节拍
{
  2.5,1.5,1.5
};

void connectedMusic(){
  for(int x=0;x<length1;x++)
  {
    tone(buzzer,connectedTune[x]);
    delay(100*durt[x]);   //这里用来根据节拍调节延时，500这个指数可以自己调整，在该音乐中，我发现用500比较合适。
    noTone(buzzer);
  }
}

void disconnectedMusic(){
  for(int x=0;x<length2;x++)
  {
    tone(buzzer,disconnectedTune[x]);
    delay(100*durt[x]);   //这里用来根据节拍调节延时，500这个指数可以自己调整，在该音乐中，我发现用500比较合适。
    noTone(buzzer);
  }
}
/*********************蜂鸣器相关部分 end*******************/

/*********************舵机相关部分*************************/
Servo myservo1,myservo2;   //定义舵机servo类对象,servo1为下面的，servo2为上面的
int pos_x = 90;    //x方向角度  
int pos_y = 90;    //y方向角度
int pos1 = 0;      //x方向接收角度暂存量
int pos2 = 0;      //y方向接收角度暂存量

void servoOrigin(){     //舵机位置复原
  pos_x = 90;
  pos_y = 90;
  myservo1.write(pos_x);
  myservo2.write(pos_y);
  }
  
void servo1_ctrl(){     //控制舵机1角度
  if(pos_x<=180){
    myservo1.write(pos_x);
    }
  }
  
void servo2_ctrl(){     //控制舵机2角度
  if(pos_y<=180){
    myservo2.write(pos_y);
    }
  }

/*********************舵机相关部分 end***********************/

void setup()
{
  // OLED 初始化
  u8g2.begin(); //构造u8g2
  u8g2.enableUTF8Print(); //启用 UTF8打印
  u8g2.setFont(u8g2_font_wqy12_t_gb2312a);
  u8g2.drawXBMP(0,0, 128, 28, logo);
  u8g2.setCursor(10, 45);
  u8g2.print("Auto Tracking Holder");
  u8g2.setCursor(40, 60);
  u8g2.print("by BradZ");
  u8g2.sendBuffer();
  delay(1500);
  
  //LED初始化
  pinMode(LED,OUTPUT);

  //蜂鸣器初始化
  pinMode(buzzer,OUTPUT);
  length1=sizeof(connectedTune)/sizeof(connectedTune[0]);   //连接成功音调长度
  length2=sizeof(disconnectedTune)/sizeof(disconnectedTune[0]);   //连接失败音调计算长度
  
  //舵机初始化
  myservo1.attach(servo1);  //舵机1控制引脚d5
  myservo2.attach(servo2); //舵机2控制引脚d6
  myservo1.write(pos_x);
  myservo2.write(pos_y);

  //OLED显示AP信息
  u8g2.clear();
  u8g2.drawXBMP(100,0, 20, 20, wifiLogo);
  u8g2.setCursor(5, 10);
  u8g2.print("Setting soft-AP：");
  u8g2.setCursor(5, 30);
  u8g2.print("WiFi Name: "+String(ssid));
  u8g2.setCursor(5, 40);
  u8g2.print("Password: " + String(password));
  u8g2.setCursor(5, 50);
  u8g2.print("IP: " + myIP);
  u8g2.setCursor(5, 60);
  u8g2.print("Port: " + String(port));
  u8g2.sendBuffer();
  
  //串口显示AP信息
  Serial.begin(115200);
  Serial.println();
  Serial.print("Setting Soft-AP ... ");
  Serial.print("AP IP address: ");
  Serial.println(myIP);
  
  //建立AP
  WiFi.softAPConfig(softLocal, softGateway, softSubnet);  
  WiFi.softAP(ssid, password);
  server.begin();
  Serial.printf("Web server started, open %s in a web browser\n", WiFi.localIP().toString().c_str());
}

void loop()
{
  WiFiClient client = server.available(); 
  if (client)
  {
    Serial.println("\n[Client connected]");
    u8g2.clear();
    u8g2.drawXBMP(40,0, 48, 48, connectedLogo);
    u8g2.setCursor(20, 60);
    u8g2.print("Client Connected!");
    u8g2.sendBuffer();
    connectedMusic();
    delay(1000);
    
    u8g2.clear();
    u8g2.drawLine(0,32,64,32);
    u8g2.drawLine(32,0,32,64);
    u8g2.drawRFrame(0,0,64,64,7);
    u8g2.drawFrame(28,28,8,8);
    u8g2.setCursor(75, 30);
    u8g2.print("x: 0");
    u8g2.setCursor(75, 50);
    u8g2.print("y: 0");
    u8g2.sendBuffer();
    char pos[10];
    int i=0;
    while (client.connected())
    {
      // 将串口数据打印给TCP(发数据)
      if(Serial.available()){
        size_t len = Serial.available();
        uint8_t sbuf[len];
        Serial.readBytes(sbuf, len);
        client.write(sbuf, len);
        delay(1);
        }
  
        // 将TCP数据打印给串口(收数据)
        if (client.available())
        {
          pos[i++] = client.read();
        }
        //pos[0~2].pos[4~6]为x、y坐标，
        //pos[7]为控制符号，o代表复原，c代表角度模式，b代表xy均为增量模式，x代表x为增量模式，y代表y为增量模式
        //pos[8]为x增量的正负，n为负、p为正，仅在增量模式下有效
        //pos[9]为y增量的正负，n为负、p为正，仅在增量模式下有效
        //例：180.090cnp  x坐标为180，y坐标为90，角度模式，（x增量wei负，y增量为正）
        if(i==10) //收集一组坐标(角度控制指令)
        {
          for(int j=0;j<10;j++) Serial.print(pos[j]);
            Serial.println();
            
            //根据TCP通信获取的字符对应设置x，y的角度暂存量pos1和pos2
            switch(pos[0]){
              case '0':
                pos1 = 0;
                break;
              case '1':
                pos1 = 100;
                break;
              default:
                pos1 = 0;
                break;
            }
            switch(pos[1]){
              case '0':
                pos1 += 0;
                break;
              case '1':
                pos1 += 10;
                break;
              case '2':
                pos1 += 20;
                break;
              case '3':
                pos1 += 30;
                break;
              case '4':
                pos1 += 40;
                break;
              case '5':
                pos1 += 50;
                break;
              case '6':
                pos1 += 60;
                break;
              case '7':
                pos1 += 70;
                break;
              case '8':
                pos1 += 80;
                break;
              case '9':
                pos1 += 90;
                break;
              default:
                pos1 += 0;
                  break;
            }
            switch(pos[2]){
              case '0':
                pos1 += 0;
                break;
              case '1':
                pos1 += 1;
                break;
              case '2':
                pos1 += 2;
                break;
              case '3':
                pos1 += 3;
                break;
              case '4':
                pos1 += 4;
                break;
              case '5':
                pos1 += 5;
                break;
              case '6':
                pos1 += 6;
                break;
              case '7':
                pos1 += 7;
                break;
              case '8':
                pos1 += 8;
                break;
              case '9':
                pos1 += 9;
                break;
              default:
                pos1 += 0;
                  break;
            }
            switch(pos[4]){
              case '0':
                pos2 = 0;
                break;
              case '1':
                pos2 = 100;
                break;
              default:
                pos2 = 0;
                break;
            }
            switch(pos[5]){
              case '0':
                pos2 += 0;
                break;
              case '1':
                pos2 += 10;
                break;
              case '2':
                pos2 += 20;
                break;
              case '3':
                pos2 += 30;
                break;
              case '4':
                pos2 += 40;
                break;
              case '5':
                pos2 += 50;
                break;
              case '6':
                pos2 += 60;
                break;
              case '7':
                pos2 += 70;
                break;
              case '8':
                pos2 += 80;
                break;
              case '9':
                pos2 += 90;
                break;
              default:
                pos2 += 0;
                break;
            }
            switch(pos[6]){
              case '0':
                pos2 += 0;
                break;
              case '1':
                pos2 += 1;
                break;
              case '2':
                pos2 += 2;
                break;
              case '3':
                pos2 += 3;
                break;
              case '4':
                pos2 += 4;
                break;
              case '5':
                pos2 += 5;
                break;
              case '6':
                pos2 += 6;
                break;
              case '7':
                pos2 += 7;
                break;
              case '8':
                pos2 += 8;
                break;
              case '9':
                pos2 += 9;
                break;
              default:
                pos2 += 0;
                break;
            }
          
          if(pos[7]=='c')
          {//角度模式下将角度暂存量pos1、pos2赋值给xy角度坐标pos_x、pos_y，作为实际需要到达的位置坐标
            pos_x = pos1;
            pos_y = pos2;
            servo1_ctrl();
            servo2_ctrl();
            Serial.println("("+String(pos_x)+","+String(pos_y)+")");
          }else if(pos[7]=='o'){
            //复原模式下还原xy角度的初始值
            servoOrigin(); 
            Serial.println("origin");
            Serial.println("("+String(pos_x)+","+String(pos_y)+")");
          }else if(pos[7]=='b'){
            //双增量模式下在当前角度基础上在x和y方向上增加pos1和pos2的角度增量
            if(pos[8]=='n') pos1*=-1;
            if(pos[9]=='n') pos2*=-1;
            pos_x += pos1;
            pos_y += pos2;
            servo1_ctrl();
            servo2_ctrl();
            Serial.println("("+String(pos_x)+","+String(pos_y)+")");
          }else if(pos[7]=='x'){
            //x增量模式下在当前角度基础上在x方向上增加pos1的角度增量,pos1正负由pos[8]决定
            if(pos[8]=='n') pos1*=-1;
            pos_x += pos1;
            servo1_ctrl();
            Serial.println("("+String(pos_x)+","+String(pos_y)+")");
          }else if(pos[7]=='y'){
            //y增量模式下在当前角度基础上在y方向上增加pos2的角度增量,pos2正负由pos[9]决定
            if(pos[9]=='n') pos2*=-1;
            pos_y += pos2;
            servo2_ctrl();
            Serial.println("("+String(pos_x)+","+String(pos_y)+")");
          }
            u8g2.clear();
            u8g2.drawLine(0,32,64,32);
            u8g2.drawLine(32,0,32,64);
            u8g2.drawRFrame(0,0,64,64,7);
            u8g2.drawFrame(int(28+32*(pos_x-90)/90),int(28+32*(pos_y-90)/90),8,8);
            u8g2.setCursor(75, 30);
            u8g2.print("x: " + String(pos_x-90));
            u8g2.setCursor(75, 50);
            u8g2.print("y: " + String(90-pos_y));
            u8g2.sendBuffer();
            i=0;
        }
      }
    delay(1);
 
    // 断开连接
    u8g2.clear();
    u8g2.drawXBMP(40,0, 48, 48, disconnectedLogo);
    u8g2.setCursor(15, 60);
    u8g2.print("Client Disconnected!");
    u8g2.sendBuffer();
    disconnectedMusic();
    delay(1000);
    servoOrigin();
    
    //OLED显示AP信息
    u8g2.clear();
    u8g2.drawXBMP(100,0, 20, 20, wifiLogo);
    u8g2.setCursor(5, 10);
    u8g2.print("Setting soft-AP：");
    u8g2.setCursor(5, 30);
    u8g2.print("WiFi Name: "+String(ssid));
    u8g2.setCursor(5, 40);
    u8g2.print("Password: " + String(password));
    u8g2.setCursor(5, 50);
    u8g2.print("IP: " + myIP);
    u8g2.setCursor(5, 60);
    u8g2.print("Port: " + String(port));
    u8g2.sendBuffer();

    //串口显示AP信息
    Serial.println("[Client disonnected]");
    Serial.print("AP IP address: ");
    Serial.println(myIP);
  }
 }