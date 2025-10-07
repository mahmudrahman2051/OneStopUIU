@echo off
echo Starting OneStopUIU Chat Server...
cd /d "C:\Users\mahmu\OneDrive\Desktop\OneStopUIU\OneStopUIU-Server"
javac -d . src\main\java\com\example\onestopuiu\server\ChatServer.java
java -cp . com.example.onestopuiu.server.ChatServer
pause