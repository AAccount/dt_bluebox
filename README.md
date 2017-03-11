# dt_bluebox
External ascii logging on android. Allows your server to log to an android device. 

This app is intended for the following scenario:
* You have a home server that can export its logs as plain network ascii strings.
* You want external logging for your home server to add 1 more complication for a hacker to clean up his trail.
* You have an extra android phone >= 4.1 that is in **good** working order. (No random reboots, random freezes, random deaths).
* You're too much of a cheapskape to buy a dedicated logging machine or pay the power bill to keep it running 24/7.

This app listens on a tcp port of your choosing for plain ascii strings. It will write them down to a file in 
/sdcard/bluebox/log_{MMM_dd_yyyy_HH_mm_ss}. It will also rotate logs every X hours of your choosing. Rotation
timing is not strictly enforced due to the nature of android alarms.

For a small scale home server a recentish android phone should be able to keep up. Not recommended for anything more
than a small scale home server used for personal use (not home business or home startup).\

The name itself comes from blackbox + recycle blue bin. It's like a server's black box using a recycled android phone.
